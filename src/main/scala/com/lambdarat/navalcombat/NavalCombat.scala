package com.lambdarat.navalcombat

import com.lambdarat.navalcombat.scenes.*
import com.lambdarat.navalcombat.assets.*

import com.lambdarat.navalcombat.utils.given_CanEqual_Option_Option

import indigo.*
import indigo.scenes.*
import indigo.shared.events.EventFilters

import scala.scalajs.js.annotation.JSExportTopLevel

final case class NavalCombatSetupData(config: GameConfig)
final case class Board()

@JSExportTopLevel("IndigoGame")
object NavalCombat extends IndigoGame[GameConfig, NavalCombatSetupData, Board, Unit]:

  def boot(flags: Map[String, String]): Outcome[BootResult[GameConfig]] =
    val initialScreen = GameConfig.default
      .withViewport(GameViewport.at720p)
      .withClearColor(RGBA.White)

    Outcome(BootResult(initialScreen, initialScreen).withAssets(Assets.assets))

  def initialScene(bootData: GameConfig): Option[SceneName] =
    Some(Landing.name)

  def scenes(bootData: GameConfig): NonEmptyList[Scene[NavalCombatSetupData, Board, Unit]] =
    NonEmptyList(Landing)

  def setup(
      bootData: GameConfig,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[NavalCombatSetupData]] =
    val maybeFont = Fonts.buildFont(assetCollection)

    val startup = maybeFont match
      case Some(font) =>
        Startup
          .Success(NavalCombatSetupData(bootData))
          .addFonts(font)
      case None: Option[FontInfo] => Startup.Failure("Failed to load font")

    Outcome(startup)

  def initialModel(startupData: NavalCombatSetupData): Outcome[Board] = Outcome(Board())

  def initialViewModel(startupData: NavalCombatSetupData, model: Board): Outcome[Unit] = Outcome(())

  def eventFilters: EventFilters = EventFilters.FrameTickOnly

  def updateModel(context: FrameContext[NavalCombatSetupData], model: Board): GlobalEvent => Outcome[Board] =
    _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: Board,
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(
      context: FrameContext[NavalCombatSetupData],
      model: Board,
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
