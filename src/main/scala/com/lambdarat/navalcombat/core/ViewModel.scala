package com.lambdarat.navalcombat.core

import com.lambdarat.navalcombat.scenes.placement.viewmodel.PlacementViewModel

import indigo.*
import indigo.shared.temporal.Signal
import indigoextras.ui.Button
import com.lambdarat.navalcombat.scenes.player.viewmodel.PlayerViewModel

extension (rotation: Rotation)

  def angle: Radians =
    rotation match
      case Rotation.Horizontal => Radians.zero
      case Rotation.Vertical   => Radians.PIby2

final case class LandingViewModel(
    play: Button,
    playMessage: Text,
    welcomeMessage: Text
)

final case class NavalCombatViewModel(landing: LandingViewModel, placement: PlacementViewModel, player: PlayerViewModel)
