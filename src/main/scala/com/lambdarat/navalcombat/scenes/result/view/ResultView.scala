package com.lambdarat.navalcombat.scenes.result.view

import com.lambdarat.navalcombat.assets.Assets
import com.lambdarat.navalcombat.core.NavalCombatSetupData

import indigo.*

object ResultView:

  private val RESULT_TITLE_MARGIN = 15

  def draw(context: FrameContext[NavalCombatSetupData]): SceneUpdateFragment =
    SceneUpdateFragment(
      Text(
        "Result",
        Assets.ponderosaFontKey,
        Material.ImageEffects(Assets.ponderosaImgName)
      )
        .withPosition(Point(context.startUpData.screenBounds.horizontalCenter, RESULT_TITLE_MARGIN))
        .alignCenter
    )
