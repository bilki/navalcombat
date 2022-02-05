package com.lambdarat.navalcombat.core

import com.lambdarat.navalcombat.scenes.placement.viewmodel.PlacementViewModel
import com.lambdarat.navalcombat.scenes.player.viewmodel.PlayerViewModel

import indigo.*
import indigo.Material.ImageEffects
import indigoextras.ui.Button

extension (rotation: Rotation)
  def angle: Radians =
    rotation match
      case Rotation.Horizontal => Radians.zero
      case Rotation.Vertical   => Radians.PIby2

final case class LandingViewModel(
    play: Button,
    playMessage: Text[ImageEffects],
    welcomeMessage: Text[ImageEffects]
)

final case class NavalCombatViewModel(landing: LandingViewModel, placement: PlacementViewModel, player: PlayerViewModel)
