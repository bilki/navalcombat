package com.lambdarat.navalcombat

import indigo.*
import indigo.scenes.*
import indigo.shared.events.EventFilters

import com.lambdarat.navalcombat.scenes.*
import com.lambdarat.navalcombat.assets.*

import scala.scalajs.js.annotation.JSExportTopLevel

final case class NavalCombatSetupData()
final case class Board()

@JSExportTopLevel("IndigoGame")
object NavalCombat extends IndigoGame[Unit, NavalCombatSetupData, Board, Unit]:

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit]] =
    val initialScreen = GameConfig.default
      .withViewport(GameViewport.at720p)
      .withClearColor(RGBA.Blue)

    Outcome(BootResult(initialScreen, ()).withAssets(Assets.assets))

  def initialScene(bootData: Unit): Option[SceneName] =
    Some(Landing.name)

  def scenes(bootData: Unit): NonEmptyList[Scene[NavalCombatSetupData, Board, Unit]] =
    NonEmptyList(Landing)

  def setup(
      bootData: Unit,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[NavalCombatSetupData]] =
    val maybeFont = Fonts.buildFont(assetCollection)

    val startup = maybeFont match {
      case Some(font) =>
        Startup
          .Success(NavalCombatSetupData())
          .addFonts(font)
      case None => Startup.Failure("Failed to load font")
    }

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
