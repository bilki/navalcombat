package com.lambdarat.navalcombat.scenes.placement

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
    val center     = Point(setupData.width / 2, setupData.height / 2)
    val gridPoints = PlacementView.computeGridPoints(setupData.width, setupData.height)
    val boats      = PlacementView.computeBoats(setupData.width)

    PlacementViewModel(
      bounds = Rectangle(0, 0, setupData.width, setupData.height),
      startTime = Seconds.zero,
      gridPoints = gridPoints.toList,
      placeMsgSignal = Placement.movePlacementMsg.run(center),
      grid = QuadTree.empty[CellPosition](100, 100),
      boats = boats,
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
      val initialCellPositions =
        for
          i <- 0 until 10
          j <- 0 until 10
          coord = Coord(XCoord(i), YCoord(j))
        yield (CellPosition(model.board.get(coord.x, coord.y).get, coord), Vertex(i * 5 + 5, j * 5 + 5))

      val initialGrid = viewModel.grid.insertElements(initialCellPositions.toList)

      Outcome(viewModel.copy(startTime = context.running, grid = initialGrid))
    case FrameTick =>
      val nextDraggingShip = (context.mouse.mousePressed, context.mouse.mouseReleased, viewModel.dragging) match
        case (true, false, None) =>
          viewModel.boats.find { case SidebarShip(shipType, shipGraphic) =>
            context.mouse.wasMouseDownWithin(shipGraphic.bounds.scaleBy(0.5, 0.5))
          }.map(sbs =>
            sbs.copy(shipGraphic = sbs.shipGraphic.withScale(Vector2(1.0, 1.0)).centerAt(context.mouse.position))
          )
        case (false, false, Some(sbs))           => Some(sbs.copy(shipGraphic = sbs.shipGraphic.centerAt(context.mouse.position)))
        case (false, true, _: Some[SidebarShip]) => None
        case _                                   => viewModel.dragging

      Outcome(viewModel.copy(dragging = nextDraggingShip))
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
