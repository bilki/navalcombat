package com.lambdarat.navalcombat.scenes.result.view

import com.lambdarat.navalcombat.assets.Assets
import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.core.Ship.*
import com.lambdarat.navalcombat.core.Cell.*
import com.lambdarat.navalcombat.draw.Graphics
import com.lambdarat.navalcombat.scenes.result.ResultEvents
import com.lambdarat.navalcombat.scenes.result.viewmodel.{ResultViewModel, SideResult}
import com.lambdarat.navalcombat.utils.*

import indigo.*
import indigo.Material.ImageEffects
import indigoextras.ui.Button

object ResultView:

  private val RESULT_TITLE_TOP_MARGIN     = 15
  private val PLAYERS_RESULT_TOP_PADDING  = 50
  private val PLAYERS_RESULT_LEFT_PADDING = 100
  private val SHIP_SECTION_WIDTH          = 64
  private val SHIP_RESULT_TOP_PADDING     = SHIP_SECTION_WIDTH + 25
  private val SIDE_RESULT_TOP_PADDING     = 50
  private val BUTTON_TEXT_BOTTOM_PADDING  = 10

  def drawShipResults(ship: Ship, cells: List[Cell], position: Point): List[Graphic[ImageEffects]] =
    val (results, _) = cells.foldLeft((List.empty[Graphic[ImageEffects]], position)) {
      case ((acc, nextPosition), nextCell) =>
        val rowResults = nextCell match
          case Floating(floating, section) if floating == ship =>
            Graphics.graphicFor(ship, section).withPosition(nextPosition) +: acc
          case Sunk(sunk, _) if sunk == ship =>
            Assets.hitCell.withPosition(nextPosition) +: acc
          case _ =>
            Assets.missCell +: acc

        (rowResults, nextPosition + Point(SHIP_SECTION_WIDTH, 0))
    }

    results
  end drawShipResults

  def drawSideResult(sideResult: SideResult, position: Point): List[SceneNode] =
    val destroyerResultGraphics = drawShipResults(Destroyer, sideResult.destroyer, position)
    val submarineResultGraphics =
      drawShipResults(Submarine, sideResult.submarine, position + Point(0, SHIP_RESULT_TOP_PADDING))
    val cruiserResultGraphics =
      drawShipResults(Cruiser, sideResult.cruiser, position + Point(0, SHIP_RESULT_TOP_PADDING * 2))
    val battleshipResultGraphics =
      drawShipResults(Battleship, sideResult.battleship, position + Point(0, SHIP_RESULT_TOP_PADDING * 3))
    val carrierResultGraphics =
      drawShipResults(Carrier, sideResult.carrier, position + Point(0, SHIP_RESULT_TOP_PADDING * 4))

    destroyerResultGraphics ++ submarineResultGraphics ++ cruiserResultGraphics ++
      battleshipResultGraphics ++ carrierResultGraphics
  end drawSideResult

  def draw(context: FrameContext[NavalCombatSetupData], result: ResultViewModel): SceneUpdateFragment =
    val sceneCenter = context.startUpData.screenBounds.horizontalCenter

    val title = Text("Result", Assets.ponderosaFontKey, Material.ImageEffects(Assets.ponderosaImgName))
      .withPosition(Point(sceneCenter, RESULT_TITLE_TOP_MARGIN))
      .alignCenter

    val playerTitlePositionX  = sceneCenter / 2 - PLAYERS_RESULT_LEFT_PADDING
    val playerTitlePositionY  = RESULT_TITLE_TOP_MARGIN + PLAYERS_RESULT_TOP_PADDING
    val playerTitlePosition   = Point(playerTitlePositionX, playerTitlePositionY)
    val playerTitle           = title.withText("Player").withPosition(playerTitlePosition).alignLeft
    val playerResultsPosition = playerTitlePosition + Point(0, SIDE_RESULT_TOP_PADDING)

    val enemyTitlePositionX  = sceneCenter + sceneCenter / 2 - PLAYERS_RESULT_LEFT_PADDING
    val enemyTitlePositionY  = RESULT_TITLE_TOP_MARGIN + PLAYERS_RESULT_TOP_PADDING
    val enemyTitlePosition   = Point(enemyTitlePositionX, enemyTitlePositionY)
    val enemyTitle           = title.withText("Enemy").withPosition(enemyTitlePosition).alignLeft
    val enemyResultsPosition = enemyTitlePosition + Point(0, SIDE_RESULT_TOP_PADDING)

    val titlesNodes       = title :: playerTitle :: enemyTitle :: Nil
    val playerResultNodes = drawSideResult(result.player, playerResultsPosition)
    val enemyResultNodes  = drawSideResult(result.enemy, enemyResultsPosition)

    val backMessage = Text(
      "PLAY",
      Assets.ponderosaFontKey,
      Material.ImageEffects(Assets.ponderosaImgName)
    ).moveTo(
      result.backButton.bounds.center.x,
      result.backButton.bounds.center.y - BUTTON_TEXT_BOTTOM_PADDING
    ).alignCenter

    val backButtonNodes = result.backButton.draw :: backMessage :: Nil

    SceneUpdateFragment(
      titlesNodes ++ playerResultNodes ++ enemyResultNodes ++ backButtonNodes
    )
