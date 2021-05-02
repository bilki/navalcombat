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

object Combat extends Scene[NavalCombatSetupData, NavalCombatModel, NavalCombatViewModel]:
  def modelLens: Lens[NavalCombatModel, NavalCombatModel]             = Lens.keepOriginal
  def viewModelLens: Lens[NavalCombatViewModel, NavalCombatViewModel] = Lens.keepOriginal

  type SceneModel     = NavalCombatModel
  type SceneViewModel = NavalCombatViewModel

  def name: SceneName = SceneName("combat")

  def eventFilters: EventFilters = EventFilters.Permissive

  def subSystems: Set[SubSystem] = Set.empty

  def updateModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel
  ): GlobalEvent => Outcome[NavalCombatModel] = _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: NavalCombatViewModel
  ): GlobalEvent => Outcome[NavalCombatViewModel] = _ => Outcome(viewModel)

  def present(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: NavalCombatViewModel
  ): Outcome[SceneUpdateFragment] =
    val combatMessage = Text(
      "Combat Screen",
      x = context.startUpData.width / 2,
      y = context.startUpData.height / 2,
      1,
      Assets.ponderosaFontKey,
      Material.ImageEffects(Assets.ponderosaImgName)
    ).alignCenter

    Outcome(SceneUpdateFragment.empty.addLayer(combatMessage))
