package com.lambdarat.navalcombat.scenes

import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.assets.Assets
import com.lambdarat.navalcombat.utils.given_CanEqual_FrameTick_GlobalEvent
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

  def updateModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel
  ): GlobalEvent => Outcome[NavalCombatModel] = _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: PlacementViewModel
  ): GlobalEvent => Outcome[PlacementViewModel] =
    case sc: SceneEvent.SceneChange =>
      Outcome(viewModel.copy(startTime = sc.at))
    case _ =>
      Outcome(viewModel)

  def present(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: PlacementViewModel
  ): Outcome[SceneUpdateFragment] =
    val timeSinceEnter   = context.running - viewModel.startTime
    val placeMsgShowTime = Seconds(0.75)

    val placeMessage = placementMessage.moveTo(viewModel.placeMsgSignal.at(timeSinceEnter - placeMsgShowTime))

    Outcome(SceneUpdateFragment(placeMessage))

case object PaintGrid extends GlobalEvent
