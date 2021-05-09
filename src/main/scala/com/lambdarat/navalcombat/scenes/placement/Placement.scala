package com.lambdarat.navalcombat.scenes.placement

import com.lambdarat.navalcombat.core.given
import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.engine.BoardEngine.*
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

object Placement extends Scene[NavalCombatSetupData, NavalCombatModel, NavalCombatViewModel]:
  def modelLens: Lens[NavalCombatModel, NavalCombatModel] = Lens.keepOriginal

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

  val movePlacementMsg = SignalReader[Point, Point](start => Signal.Lerp(start, Point(start.x, 20), Seconds(1)))

  def initialPlacementViewModel(setupData: NavalCombatSetupData): PlacementViewModel =
    val center = Point(setupData.width / 2, setupData.height / 2)

    PlacementViewModel(
      bounds = Rectangle(0, 0, setupData.width, setupData.height),
      startTime = Seconds.zero,
      placeMsgSignal = Placement.movePlacementMsg.run(center),
      grid = QuadTree.empty[CellPosition](100, 100),
      sidebarShips = List.empty[SidebarShip],
      gridShips = List.empty[SidebarShip],
      dragging = None
    )

  def updateModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel
  ): GlobalEvent => Outcome[NavalCombatModel] = _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: PlacementViewModel
  ): GlobalEvent => Outcome[PlacementViewModel] =
    case PaintGrid =>
      val gridGraphics = PlacementView.computeGridGraphics(context.startUpData.width, context.startUpData.height)
      val sidebarShips = PlacementView.computeSidebarShips(context.startUpData.width)

      val gridCoords =
        for
          i <- 0 until 10
          j <- 0 until 10
        yield Coord(XCoord(i), YCoord(j))

      val gridGraphicsWithCoord = gridGraphics.zip(gridCoords)

      val initialCellPositions = gridGraphicsWithCoord.map { case (gridPoint, coord) =>
        (
          CellPosition(model.board.get(coord.x, coord.y).get, coord, gridPoint),
          Vertex(coord.x.toInt * 5 + 5, coord.y.toInt * 5 + 5)
        )
      }

      val initialGrid = viewModel.grid.insertElements(initialCellPositions.toList)

      Outcome(viewModel.copy(startTime = context.running, grid = initialGrid, sidebarShips = sidebarShips))
    case FrameTick =>
      val nextPlacingShip = (context.mouse.mouseClicked, viewModel.dragging) match
        case (true, None) =>
          viewModel.sidebarShips.find { case SidebarShip(shipType, shipGraphic) =>
            context.mouse.wasMouseClickedWithin(shipGraphic.bounds.scaleBy(0.5, 0.5))
          }.map(sbs =>
            val draggedSidebarShip =
              sbs.copy(shipGraphic = sbs.shipGraphic.withScale(Vector2(1.0, 1.0)).centerAt(context.mouse.position))
            PlacingShip(draggedSidebarShip, Rotation.Horizontal)
          )
        case (false, Some(dragged)) =>
          val sbs = dragged.sidebarShip
          val newRotation =
            if context.keyboard.keysAreUp(Key.KEY_R) then dragged.rotation.reverse else dragged.rotation
          val draggedSidebarShip =
            sbs.copy(shipGraphic = sbs.shipGraphic.rotateTo(newRotation.angle).centerAt(context.mouse.position))

          Some(PlacingShip(draggedSidebarShip, newRotation))
        case (true, _: Some[PlacingShip]) => None
        case _                            => viewModel.dragging

      Outcome(viewModel.copy(dragging = nextPlacingShip))
    case _ =>
      Outcome(viewModel)

  def present(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: PlacementViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(PlacementView.draw(context.running, viewModel, placementMessage))

case object PaintGrid extends GlobalEvent

given CanEqual[PaintGrid.type, GlobalEvent] = CanEqual.derived
