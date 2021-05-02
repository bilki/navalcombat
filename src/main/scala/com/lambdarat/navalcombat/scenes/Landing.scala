package com.lambdarat.navalcombat.scenes

import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.assets.Assets
import com.lambdarat.navalcombat.scenes.Combat
import com.lambdarat.navalcombat.utils.given_CanEqual_FrameTick_GlobalEvent
import com.lambdarat.navalcombat.utils.given_CanEqual_PlayCombat_type_GlobalEvent
import com.lambdarat.navalcombat.utils.*

import indigo.*
import indigo.scenes.*
import indigo.shared.*
import indigo.shared.events.*
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigoextras.effectmaterials.*
import indigoextras.ui.Button
import indigoextras.ui.ButtonAssets

object Landing extends Scene[NavalCombatSetupData, NavalCombatModel, NavalCombatViewModel]:
  import LandingEvents.*

  def modelLens: Lens[NavalCombatModel, NavalCombatModel] = Lens.keepOriginal

  def viewModelLens: Lens[NavalCombatViewModel, LandingViewModel] =
    Lens(_.landing, (ncvm, lvm) => ncvm.copy(landing = lvm))

  type SceneModel     = NavalCombatModel
  type SceneViewModel = LandingViewModel

  def name: SceneName = SceneName("landing")

  def eventFilters: EventFilters = EventFilters.Permissive

  def subSystems: Set[SubSystem] = Set.empty

  def initialLandingViewModel(setupData: NavalCombatSetupData): LandingViewModel =
    val welcomeMessage = Text(
      "Welcome to Naval Combat",
      x = setupData.width / 2,
      y = setupData.height / 2,
      1,
      Assets.ponderosaFontKey,
      Material.ImageEffects(Assets.ponderosaImgName)
    ).alignCenter

    val playButton = Button(
      buttonAssets = Assets.simpleButtonAssets,
      bounds = Rectangle(
        welcomeMessage.x,
        welcomeMessage.y + 10,
        Assets.simpleButtonGraphic.bounds.width,
        Assets.simpleButtonGraphic.bounds.height
      ).scaleBy(4, 4).alignCenter,
      depth = Depth(2)
    ).withUpActions(PlayCombat)

    val playMessage = Text(
      "PLAY",
      x = playButton.bounds.center.x,
      y = playButton.bounds.center.y - 10,
      2,
      Assets.ponderosaFontKey,
      Material.ImageEffects(Assets.ponderosaImgName)
    ).alignCenter

    LandingViewModel(
      play = playButton,
      playMessage = playMessage,
      welcomeMessage = welcomeMessage
    )

  def updateModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel
  ): GlobalEvent => Outcome[NavalCombatModel] = _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: LandingViewModel
  ): GlobalEvent => Outcome[LandingViewModel] =
    case FrameTick =>
      viewModel.play.update(context.inputState.mouse).map { btn =>
        viewModel.copy(play = btn)
      }
    case PlayCombat =>
      Outcome(viewModel).addGlobalEvents(SceneEvent.JumpTo(Combat.name))
    case _ =>
      Outcome(viewModel)

  def present(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: LandingViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
        .addLayers(
          Layer(viewModel.welcomeMessage),
          Layer(viewModel.play.draw),
          Layer(viewModel.playMessage)
        )
    )

object LandingEvents:
  case object PlayCombat extends GlobalEvent
