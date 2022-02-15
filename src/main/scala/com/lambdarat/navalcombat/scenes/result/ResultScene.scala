package com.lambdarat.navalcombat.scenes.result

import com.lambdarat.navalcombat.assets.Assets
import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.scenes.result.view.ResultView

import indigo.*
import indigo.scenes.*

object ResultScene extends Scene[NavalCombatSetupData, NavalCombatModel, NavalCombatViewModel]:
  def modelLens: Lens[NavalCombatModel, NavalCombatModel] = Lens.keepLatest

  def viewModelLens: Lens[NavalCombatViewModel, NavalCombatViewModel] = Lens.keepLatest

  type SceneModel     = NavalCombatModel
  type SceneViewModel = NavalCombatViewModel

  def name: SceneName = SceneName("result")

  def eventFilters: EventFilters = EventFilters.Permissive

  def subSystems: Set[SubSystem] = Set.empty

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: NavalCombatViewModel
  ): GlobalEvent => Outcome[NavalCombatViewModel] = Function.const(Outcome(viewModel))

  def updateModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel
  ): GlobalEvent => Outcome[NavalCombatModel] = Function.const(Outcome(model))

  def present(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: NavalCombatViewModel
  ): Outcome[SceneUpdateFragment] = Outcome(
    ResultView.draw(context)
  )
