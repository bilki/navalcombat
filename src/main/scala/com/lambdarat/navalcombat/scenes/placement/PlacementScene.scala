package com.lambdarat.navalcombat.scenes.placement

import com.lambdarat.navalcombat.core.given
import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.engine.BoardEngine.*
import com.lambdarat.navalcombat.scenes.placement.view.*
import com.lambdarat.navalcombat.scenes.placement.viewmodel.*
import com.lambdarat.navalcombat.assets.Assets
import com.lambdarat.navalcombat.utils.given
import com.lambdarat.navalcombat.utils.*

import indigo.*
import indigo.scenes.*
import indigo.shared.*
import indigo.shared.events.*
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigo.shared.temporal.*

import indigoextras.geometry.*
import indigoextras.subsystems.*
import indigoextras.trees.QuadTree

object PlacementScene extends Scene[NavalCombatSetupData, NavalCombatModel, NavalCombatViewModel]:
  def modelLens: Lens[NavalCombatModel, NavalCombatModel] = Lens.keepLatest

  def viewModelLens: Lens[NavalCombatViewModel, PlacementViewModel] =
    Lens(_.placement, (ncvm, pvm) => ncvm.copy(placement = pvm))

  type SceneModel     = NavalCombatModel
  type SceneViewModel = PlacementViewModel

  def name: SceneName = SceneName("combat")

  def eventFilters: EventFilters = EventFilters.Permissive

  def subSystems: Set[SubSystem] = Set.empty

  val placementMessage = Text(
    "Placement Screen",
    Assets.ponderosaFontKey,
    Material.ImageEffects(Assets.ponderosaImgName)
  ).alignCenter

  def initialPlacementViewModel(setupData: NavalCombatSetupData): PlacementViewModel =
    val center = setupData.screenBounds.center

    val gridBounds = PlacementView.computeGridBounds(setupData)
    val modelSpace = Rectangle(0, 0, setupData.boardSize, setupData.boardSize)

    PlacementViewModel(
      sceneSettings = SceneSettings(setupData.screenBounds, gridBounds, modelSpace),
      startTime = Seconds.zero,
      placeMsgSignal = PlacementView.movePlacementMsg.run(center),
      highlightedCells = List.empty[Highlighted],
      sidebarShips = List.empty[SidebarShip],
      dragging = None
    )

  def updateModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel
  ): GlobalEvent => Outcome[NavalCombatModel] =
    case PlaceShip(ship, coord, rotation) =>
      val updatedBoard = model.board.place(ship, rotation, coord.x, coord.y)

      if model.board.canPlace(ship, rotation, coord.x, coord.y) then
        Outcome(model.copy(board = updatedBoard, ships = model.ships + (ship -> ShipOrientation(coord, rotation))))
      else Outcome(model)
    case _ =>
      Outcome(model)

  private def highlightedCells(
      dragged: PlacingShip,
      gridBounds: Rectangle,
      modelSpace: Rectangle,
      mousePosition: Point,
      model: NavalCombatModel
  ): List[Highlighted] =
    val sbs        = dragged.sidebarShip
    val shipSize   = sbs.shipType.size
    val shipBounds = sbs.shipGraphic.bounds
    val shipCenter = mousePosition
    val holeSize   = shipBounds.width / shipSize.toInt

    val shipHolesPoints: List[Point] = dragged.rotation match
      case Rotation.Horizontal =>
        val firstHoleCenter = shipCenter.withX(shipCenter.x - shipBounds.width / 2 + holeSize / 2)
        val restOfHoleCenters = (1 until shipSize.toInt)
          .map(holeMultiplier => firstHoleCenter.withX(firstHoleCenter.x + holeSize * holeMultiplier))
          .toList

        firstHoleCenter :: restOfHoleCenters
      case Rotation.Vertical =>
        val firstHoleCenter = shipCenter.withY(shipCenter.y + shipBounds.width / 2 - holeSize / 2)
        val restOfHoleCenters = (1 until shipSize.toInt)
          .map(holeMultiplier => firstHoleCenter.withY(firstHoleCenter.y - holeSize * holeMultiplier))
          .toList

        firstHoleCenter :: restOfHoleCenters
    end shipHolesPoints

    val overlapping = shipHolesPoints.filter(gridBounds.isPointWithin).map(_.transform(gridBounds, modelSpace).toCoord)

    overlapping match
      case Nil                                        => List.empty[Highlighted]
      case overlaps if overlaps.size < shipSize.toInt => overlapping.map(Highlighted(_, Highlight.NotValid))
      case firstCoord :: rest =>
        if model.board.canPlace(dragged.sidebarShip.shipType, dragged.rotation, firstCoord.x, firstCoord.y) then
          overlapping.map(Highlighted(_, Highlight.Valid))
        else
          overlapping.map(coord =>
            Highlighted(
              coord,
              if model.board.isEmpty(coord.x, coord.y) then Highlight.Valid else Highlight.NotValid
            )
          )
  end highlightedCells

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: PlacementViewModel
  ): GlobalEvent => Outcome[PlacementViewModel] =
    case PaintGrid =>
      val gridBounds  = viewModel.sceneSettings.gridBounds
      val sceneBounds = viewModel.sceneSettings.sceneBounds

      val sidebarShips = PlacementView.computeSidebarShips(sceneBounds, gridBounds)

      val boardSize = context.startUpData.boardSize

      Outcome(viewModel.copy(startTime = context.running, sidebarShips = sidebarShips))
    case FrameTick =>
      val gridBounds = viewModel.sceneSettings.gridBounds
      val modelSpace = viewModel.sceneSettings.modelSpace

      (context.mouse.mouseClicked, viewModel.dragging) match
        case (true, None) =>
          val nextPlacingShip = viewModel.sidebarShips.find { case SidebarShip(shipType, shipGraphic) =>
            context.mouse.wasMouseClickedWithin(shipGraphic.bounds.scaleBy(0.5, 0.5))
          }.map(PlacingShip(_, Rotation.Horizontal))

          Outcome(viewModel.copy(dragging = nextPlacingShip, highlightedCells = List.empty))
        case (false, Some(dragged)) =>
          val highlighted = highlightedCells(dragged, gridBounds, modelSpace, context.mouse.position, model)
          val sbs         = dragged.sidebarShip
          val newRotation =
            if context.keyboard.keysAreUp(Key.KEY_R) then dragged.rotation.reverse else dragged.rotation
          val nextPlacingShip = Some(PlacingShip(sbs, newRotation))

          Outcome(viewModel.copy(dragging = nextPlacingShip, highlightedCells = highlighted))
        case (true, Some(dragged)) =>
          val highlighted = highlightedCells(dragged, gridBounds, modelSpace, context.mouse.position, model)

          val maybePlacePosition =
            if highlighted.size == dragged.sidebarShip.shipType.size.toInt && highlighted.forall(
              _.highlight == Highlight.Valid
            ) then
              highlighted.headOption.map(highlightedCell =>
                PlaceShip(dragged.sidebarShip.shipType, highlightedCell.position, dragged.rotation)
              )
            else None

          Outcome(viewModel.copy(dragging = None, highlightedCells = List.empty)).flatMap { nextViewModel =>
            maybePlacePosition match
              case None =>
                Outcome(nextViewModel)
              case Some(placeShip) =>
                Outcome(
                  nextViewModel.copy(sidebarShips =
                    nextViewModel.sidebarShips.filterNot(_.shipType == placeShip.shipType)
                  )
                ).addGlobalEvents(placeShip)
          }

        case (false, None) =>
          Outcome(viewModel)
    case _ =>
      Outcome(viewModel)

  def present(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: PlacementViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(PlacementView.draw(context.running, model, viewModel, placementMessage, context.mouse.position))

case object PaintGrid                                                  extends GlobalEvent
case class PlaceShip(shipType: Ship, coord: Coord, rotation: Rotation) extends GlobalEvent

given CanEqual[PaintGrid.type, GlobalEvent] = CanEqual.derived
