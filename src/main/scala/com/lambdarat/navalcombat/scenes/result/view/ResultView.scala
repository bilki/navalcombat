package com.lambdarat.navalcombat.scenes.result.view

import com.lambdarat.navalcombat.assets.Assets
import com.lambdarat.navalcombat.core.NavalCombatSetupData
import com.lambdarat.navalcombat.scenes.result.viewmodel.ResultViewModel

import indigo.*

object ResultView:

  private val RESULT_TITLE_MARGIN   = 15
  private val PLAYERS_RESULT_MARGIN = 50

  def draw(context: FrameContext[NavalCombatSetupData], result: ResultViewModel): SceneUpdateFragment =
    val sceneCenter = context.startUpData.screenBounds.horizontalCenter

    val title = Text("Result", Assets.ponderosaFontKey, Material.ImageEffects(Assets.ponderosaImgName))
      .withPosition(Point(sceneCenter, RESULT_TITLE_MARGIN))
      .alignCenter

    val playerTitlePositionX = sceneCenter / 2
    val playerTitlePositionY = RESULT_TITLE_MARGIN + PLAYERS_RESULT_MARGIN
    val playerTitle          = title.withText("Player").withPosition(Point(playerTitlePositionX, playerTitlePositionY))

    val enemyTitlePositionX = sceneCenter + sceneCenter / 2
    val enemyTitlePositionY = RESULT_TITLE_MARGIN + PLAYERS_RESULT_MARGIN
    val enemyTitle          = title.withText("Enemy").withPosition(Point(enemyTitlePositionX, enemyTitlePositionY))

    val sceneNodes = title :: playerTitle :: enemyTitle :: Nil

    SceneUpdateFragment(sceneNodes)
