package com.lambdarat.navalcombat.scenes

import com.lambdarat.navalcombat.*
import com.lambdarat.navalcombat.assets.Assets

import indigo.*
import indigo.scenes.*
import indigo.shared.events.*
import indigo.shared.*
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem

object Landing extends Scene[NavalCombatSetupData, Board, Unit]:
  def modelLens: Lens[Board, Board] = Lens.keepLatest

  def viewModelLens: Lens[Unit, Unit] = Lens.keepLatest

  type SceneModel     = Board
  type SceneViewModel = Unit

  def name: SceneName = SceneName("landing")

  def eventFilters: EventFilters = EventFilters.FrameTickOnly

  def subSystems: Set[SubSystem] = Set.empty

  def updateModel(context: FrameContext[NavalCombatSetupData], model: Board): GlobalEvent => Outcome[Board] = _ =>
    Outcome(model)

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: Board,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] = _ => Outcome(viewModel)

  def present(
      context: FrameContext[NavalCombatSetupData],
      model: Board,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    val welcomeMessage = Text(
      "Welcome to naval combat",
      0,
      0,
      1,
      Assets.ponderosaFontKey,
      Material.ImageEffects(Assets.ponderosaImgName)
    )

    Outcome(SceneUpdateFragment.empty.addLayer(welcomeMessage))
