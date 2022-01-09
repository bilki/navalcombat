package com.lambdarat.navalcombat.scenes.player

import indigo.scenes.Scene
import indigo.scenes.Lens
import com.lambdarat.navalcombat.core.NavalCombatSetupData
import com.lambdarat.navalcombat.core.NavalCombatModel
import com.lambdarat.navalcombat.core.NavalCombatViewModel
import com.lambdarat.navalcombat.scenes.player.viewmodel.PlayerViewModel
import indigo.scenes.SceneName
import indigo.shared.events.EventFilters
import indigo.shared.subsystems.SubSystem
import indigo.shared.events.GlobalEvent
import indigo.shared.Outcome
import indigo.shared.FrameContext
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.datatypes.Rectangle
import com.lambdarat.navalcombat.scenes.placement.viewmodel.SceneSettings
import com.lambdarat.navalcombat.assets.Assets

import indigo.*

object PlayerScene extends Scene[NavalCombatSetupData, NavalCombatModel, NavalCombatViewModel]:
  def modelLens: Lens[NavalCombatModel, NavalCombatModel] = Lens.keepLatest

  def viewModelLens: Lens[NavalCombatViewModel, PlayerViewModel] =
    Lens(_.player, (ncvm, pvm) => ncvm.copy(player = pvm))

  type SceneModel     = NavalCombatModel
  type SceneViewModel = PlayerViewModel

  def name: SceneName = SceneName("player")

  def eventFilters: EventFilters = EventFilters.Permissive

  def subSystems: Set[SubSystem] = Set.empty

  def initialPlayerViewModel(setupData: NavalCombatSetupData) =
    val center = setupData.screenBounds.center

    val gridBounds = PlayerView.computeGridBounds(setupData)
    val modelSpace = Rectangle(0, 0, setupData.boardSize, setupData.boardSize)

    PlayerViewModel(
      sceneSettings = SceneSettings(setupData.screenBounds, gridBounds, modelSpace)
    )
  end initialPlayerViewModel

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: PlayerViewModel
  ): GlobalEvent => Outcome[PlayerViewModel] =
    case _ => Outcome(viewModel)

  def updateModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel
  ): GlobalEvent => Outcome[NavalCombatModel] =
    case _ => Outcome(model)

  val playerTurnMsg = Text(
    "Player turn",
    Assets.ponderosaFontKey,
    Material.ImageEffects(Assets.ponderosaImgName)
  ).alignCenter

  def present(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: PlayerViewModel
  ): Outcome[SceneUpdateFragment] = Outcome(
    PlayerView.draw(model, viewModel, playerTurnMsg)
  )
