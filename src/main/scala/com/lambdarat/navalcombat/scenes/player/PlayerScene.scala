package com.lambdarat.navalcombat.scenes.player

import com.lambdarat.navalcombat.assets.Assets
import com.lambdarat.navalcombat.engine.AutomatonEngine
import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.scenes.player.viewmodel.PlayerViewModel
import com.lambdarat.navalcombat.scenes.placement.viewmodel.SceneSettings

import indigo.*
import indigo.scenes.*
import indigo.scenes.SceneEvent.SceneChange

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
    case SceneChange(_, _, time) =>
      val generateDice = Dice.fromSeed(time.toMillis.toLong)
      val enemyBoard   = AutomatonEngine.placeShips(generateDice)

      Outcome(model.copy(enemy = enemyBoard))
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
