package com.lambdarat.navalcombat

import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.scenes.landing.*
import com.lambdarat.navalcombat.scenes.placement.*
import com.lambdarat.navalcombat.assets.*

import com.lambdarat.navalcombat.utils.given
import com.lambdarat.navalcombat.utils.ExtraColors.*

import indigo.*
import indigo.scenes.*
import indigo.shared.events.EventFilters

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object NavalCombat extends IndigoGame[GameConfig, NavalCombatSetupData, NavalCombatModel, NavalCombatViewModel]:

  def boot(flags: Map[String, String]): Outcome[BootResult[GameConfig]] =
    val initialScreen = GameConfig.default
      .withViewport(GameViewport.at720p)
      .withClearColor(LightGrey)

    Outcome(BootResult(initialScreen, initialScreen).withAssets(Assets.assets))

  def initialScene(bootData: GameConfig): Option[SceneName] =
    Some(LandingScene.name)

  def scenes(bootData: GameConfig): NonEmptyList[Scene[NavalCombatSetupData, NavalCombatModel, NavalCombatViewModel]] =
    NonEmptyList(LandingScene, PlacementScene)

  def setup(
      bootData: GameConfig,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[NavalCombatSetupData]] =
    val maybeFont = Fonts.buildFont(assetCollection)

    val startup = maybeFont match
      case Some(font) =>
        Startup
          .Success(NavalCombatSetupData(bootData.screenDimensions))
          .addFonts(font)
      case None: Option[FontInfo] => Startup.Failure("Failed to load font")

    Outcome(startup)

  def initialModel(startupData: NavalCombatSetupData): Outcome[NavalCombatModel] = Outcome(
    NavalCombatModel(Board.empty)
  )

  def initialViewModel(startupData: NavalCombatSetupData, model: NavalCombatModel): Outcome[NavalCombatViewModel] =
    Outcome(
      NavalCombatViewModel(
        landing = LandingScene.initialLandingViewModel(startupData),
        placement = PlacementScene.initialPlacementViewModel(startupData)
      )
    )

  def eventFilters: EventFilters = EventFilters.BlockAll

  def updateModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel
  ): GlobalEvent => Outcome[NavalCombatModel] =
    _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: NavalCombatViewModel
  ): GlobalEvent => Outcome[NavalCombatViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: NavalCombatViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
