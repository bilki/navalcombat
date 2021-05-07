package com.lambdarat.navalcombat.scenes.placement

import com.lambdarat.navalcombat.core.*
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

    PlacementViewModel(
      bounds = Rectangle(0, 0, setupData.width, setupData.height),
      startTime = Seconds.zero,
      gridPoints = gridPoints.toList,
      placeMsgSignal = Placement.movePlacementMsg.run(center),
      grid = QuadTree.empty[CellPosition](100, 100)
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
      Outcome(viewModel.copy(startTime = context.running))
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
