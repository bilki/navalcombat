package com.lambdarat.navalcombat.scenes.placement

import com.lambdarat.navalcombat.assets.Assets
import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.core.given
import com.lambdarat.navalcombat.draw.Graphics
import com.lambdarat.navalcombat.engine.BoardEngine.*
import com.lambdarat.navalcombat.scenes.placement.view.*
import com.lambdarat.navalcombat.scenes.placement.viewmodel.*
import com.lambdarat.navalcombat.scenes.player.PlayerScene
import com.lambdarat.navalcombat.scenes.result.ResultScene
import com.lambdarat.navalcombat.utils.*
import com.lambdarat.navalcombat.utils.given

import indigo.*
import indigo.scenes.*
import indigo.scenes.SceneEvent.SceneChange
import indigoextras.geometry.*
import indigoextras.subsystems.*
import indigoextras.trees.QuadTree

object PlacementScene extends Scene[NavalCombatSetupData, NavalCombatModel, NavalCombatViewModel]:
  def modelLens: Lens[NavalCombatModel, NavalCombatModel] = Lens.keepLatest

  def viewModelLens: Lens[NavalCombatViewModel, PlacementViewModel] =
    Lens(_.placement, (ncvm, pvm) => ncvm.copy(placement = pvm))

  type SceneModel     = NavalCombatModel
  type SceneViewModel = PlacementViewModel

  val name: SceneName = SceneName("combat")

  def eventFilters: EventFilters = EventFilters.Permissive

  def subSystems: Set[SubSystem] = Set.empty

  val placementMessage = Text(
    "Place ships",
    Assets.ponderosaFontKey,
    Material.ImageEffects(Assets.ponderosaImgName)
  ).alignCenter

  def initialPlacementViewModel(setupData: NavalCombatSetupData): PlacementViewModel =
    val center = setupData.screenBounds.center

    val gridBounds = PlacementView.computeGridBounds(setupData)
    val modelSpace = Rectangle(0, 0, setupData.boardSize, setupData.boardSize)

    PlacementViewModel(
      sceneSettings = SceneSettings(setupData.screenBounds, gridBounds, modelSpace),
      sidebarShipGraphics = PlacementView.computSidebarShipGraphics(setupData.screenBounds.width, gridBounds.y),
      highlightedCells = List.empty[Highlighted],
      sidebarShips = Ship.values.toList,
      dragging = None
    )

  def updateModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel
  ): GlobalEvent => Outcome[NavalCombatModel] =
    case PlaceShip(ship, coord, rotation) =>
      val updatedBoard = model.player.place(ship, rotation, coord.x, coord.y)

      updatedBoard match
        case Some(board) =>
          val updatedBoardOutcome = Outcome(model.copy(player = board))
          if (Ship.values.forall(board.ships.contains))
            updatedBoardOutcome.addGlobalEvents(SceneEvent.JumpTo(PlayerScene.name))
          else
            updatedBoardOutcome
        case None =>
          Outcome(model)
    case _ =>
      Outcome(model)

  private def highlightedCells(
      dragged: PlacingShip,
      gridBounds: Rectangle,
      modelSpace: Rectangle,
      mousePosition: Point,
      model: NavalCombatModel
  ): List[Highlighted] =
    val shipSize   = dragged.ship.size
    val shipBounds = Graphics.graphicFor(dragged.ship).bounds
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
        if model.player.canPlace(dragged.ship, dragged.rotation, firstCoord.x, firstCoord.y) then
          overlapping.map(Highlighted(_, Highlight.Valid))
        else
          overlapping.map(coord =>
            Highlighted(
              coord,
              if model.player.isEmpty(coord.x, coord.y) then Highlight.Valid else Highlight.NotValid
            )
          )
  end highlightedCells

  private def handlePlacement(
      mouse: Mouse,
      keyboard: Keyboard,
      model: NavalCombatModel,
      viewModel: PlacementViewModel
  ): Outcome[PlacementViewModel] =
    val gridBounds = viewModel.sceneSettings.gridBounds
    val modelSpace = viewModel.sceneSettings.modelSpace

    (mouse.mouseClicked, viewModel.dragging) match
      case (true, None) =>
        val nextPlacingShip = viewModel.sidebarShips.find { ship =>
          val shipGraphic = PlacementView.sidebarShipGraphicFor(ship, viewModel.sidebarShipGraphics)
          mouse.wasMouseClickedWithin(shipGraphic.bounds)
        }.map(PlacingShip(_, Rotation.Horizontal))

        val nextSidebarShips = nextPlacingShip.fold(viewModel.sidebarShips) { dragging =>
          viewModel.sidebarShips.filterNot(_ == dragging.ship)
        }

        Outcome(
          viewModel.copy(dragging = nextPlacingShip, highlightedCells = List.empty, sidebarShips = nextSidebarShips)
        )
      case (false, Some(dragged)) =>
        val highlighted = highlightedCells(dragged, gridBounds, modelSpace, mouse.position, model)
        val newRotation =
          if keyboard.keysAreUp(Key.KEY_R) || mouse.pressed(MouseButton.RightMouseButton) then dragged.rotation.reverse
          else dragged.rotation
        val nextPlacingShip = Some(PlacingShip(dragged.ship, newRotation))

        Outcome(viewModel.copy(dragging = nextPlacingShip, highlightedCells = highlighted))
      case (true, Some(dragged)) =>
        val highlighted = highlightedCells(dragged, gridBounds, modelSpace, mouse.position, model)

        val maybeUpdateBoard =
          for
            highlightedCell <- Option
              .when(highlighted.size == dragged.ship.size.toInt)(highlighted.headOption)
              .flatten
            maybePlaceShip = PlaceShip(dragged.ship, highlightedCell.position, dragged.rotation)
            position       = highlightedCell.position
            placeShip <- Option.when(model.player.canPlace(dragged.ship, dragged.rotation, position.x, position.y))(
              maybePlaceShip
            )
          yield placeShip

        Outcome(viewModel.copy(dragging = None, highlightedCells = List.empty)).flatMap { nextViewModel =>
          maybeUpdateBoard match
            case None =>
              Outcome(nextViewModel.copy(sidebarShips = dragged.ship :: nextViewModel.sidebarShips))
            case Some(placeShip) =>
              Outcome(
                nextViewModel.copy(sidebarShips = nextViewModel.sidebarShips.filterNot(_ == placeShip.shipType))
              ).addGlobalEvents(placeShip)
        }

      case (false, None) =>
        Outcome(viewModel)

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: PlacementViewModel
  ): GlobalEvent => Outcome[PlacementViewModel] =
    case FrameTick =>
      handlePlacement(context.mouse, context.keyboard, model, viewModel)
    case SceneChange(ResultScene.name, _, _) =>
      Outcome(initialPlacementViewModel(context.startUpData))
    case _ =>
      Outcome(viewModel)

  def present(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: PlacementViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(PlacementView.draw(model, viewModel, placementMessage, context.mouse.position))

case class PlaceShip(shipType: Ship, coord: Coord, rotation: Rotation) extends GlobalEvent
