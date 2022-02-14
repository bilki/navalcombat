package com.lambdarat.navalcombat.scenes.player.viewmodel

import com.lambdarat.navalcombat.scenes.placement.viewmodel.SceneSettings

enum Turn derives CanEqual:
  case Player, Enemy

case class PlayerViewModel(sceneSettings: SceneSettings, turn: Turn)
