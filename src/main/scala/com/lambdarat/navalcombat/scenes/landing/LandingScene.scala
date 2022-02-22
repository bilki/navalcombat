package com.lambdarat.navalcombat.scenes.landing

import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.assets.Assets
import com.lambdarat.navalcombat.scenes.landing.viewmodel.LandingViewModel
import com.lambdarat.navalcombat.scenes.placement.PlacementScene
import com.lambdarat.navalcombat.utils.*
import com.lambdarat.navalcombat.utils.given

import indigo.*
import indigo.scenes.*
import indigoextras.effectmaterials.*
import indigoextras.ui.{Button, ButtonAssets}

import LandingEvents.*

object LandingScene extends Scene[NavalCombatSetupData, NavalCombatModel, NavalCombatViewModel]:
  def modelLens: Lens[NavalCombatModel, NavalCombatModel] = Lens.keepOriginal

  def viewModelLens: Lens[NavalCombatViewModel, LandingViewModel] =
    Lens(_.landing, (ncvm, lvm) => ncvm.copy(landing = lvm))

  type SceneModel     = NavalCombatModel
  type SceneViewModel = LandingViewModel

  val name: SceneName = SceneName("landing")

  def eventFilters: EventFilters = EventFilters.Permissive

  def subSystems: Set[SubSystem] = Set.empty

  def initialLandingViewModel(setupData: NavalCombatSetupData): LandingViewModel =
    val center = setupData.screenBounds.center

    val welcomeMessage = Text(
      "Welcome to Naval Combat",
      Assets.ponderosaFontKey,
      Material.ImageEffects(Assets.ponderosaImgName)
    ).withPosition(center).alignCenter

    val playBounds = Rectangle(
      welcomeMessage.x,
      welcomeMessage.y + 10,
      Assets.simpleButtonGraphic.bounds.width,
      Assets.simpleButtonGraphic.bounds.height
    ).scaleBy(4, 4)

    val playButton = Button(
      buttonAssets = Assets.simpleButtonAssets,
      bounds = playBounds.alignCenter,
      depth = Depth(1)
    ).withUpActions(PlaceShips)

    val playMessage = Text(
      "PLAY",
      Assets.ponderosaFontKey,
      Material.ImageEffects(Assets.ponderosaImgName)
    ).moveTo(
      playButton.bounds.center.x,
      playButton.bounds.center.y - 10
    ).alignCenter

    LandingViewModel(
      play = playButton,
      playMessage = playMessage,
      welcomeMessage = welcomeMessage
    )

  def updateModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel
  ): GlobalEvent => Outcome[NavalCombatModel] =
    case PlaceShips => Outcome(model.copy(player = Board.empty))
    case _          => Outcome(model)

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: LandingViewModel
  ): GlobalEvent => Outcome[LandingViewModel] =
    case _: MouseEvent.MouseUp | _: MouseEvent.MouseDown =>
      for updatedButton <- viewModel.play.update(context.inputState.mouse)
      yield viewModel.copy(play = updatedButton)
    case PlaceShips =>
      Outcome(viewModel).addGlobalEvents(SceneEvent.JumpTo(PlacementScene.name))
    case _ =>
      Outcome(viewModel)

  def present(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: LandingViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        viewModel.welcomeMessage,
        viewModel.play.draw,
        viewModel.playMessage
      )
    )

object LandingEvents:
  case object PlaceShips extends GlobalEvent derives CanEqual
