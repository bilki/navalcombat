package com.lambdarat.navalcombat.scenes

import com.lambdarat.navalcombat.*
import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.assets.Assets

import indigo.*
import indigo.scenes.*
import indigo.shared.*
import indigo.shared.events.*
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigoextras.effectmaterials.*

object Landing extends Scene[NavalCombatSetupData, NavalCombatModel, NavalCombatViewModel]:
  def modelLens: Lens[NavalCombatModel, NavalCombatModel]             = Lens.keepOriginal
  def viewModelLens: Lens[NavalCombatViewModel, NavalCombatViewModel] = Lens.keepOriginal

  type SceneModel     = NavalCombatModel
  type SceneViewModel = NavalCombatViewModel

  def name: SceneName = SceneName("landing")

  def eventFilters: EventFilters = EventFilters.FrameTickOnly

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
    val width  = context.startUpData.width
    val height = context.startUpData.height

    val welcomeMessage = Text(
      "Welcome to Naval Combat",
      x = width / 2,
      y = height / 2,
      1,
      Assets.ponderosaFontKey,
      Material.ImageEffects(Assets.ponderosaImgName)
    ).alignCenter

    Outcome(SceneUpdateFragment.empty.addLayer(welcomeMessage))
