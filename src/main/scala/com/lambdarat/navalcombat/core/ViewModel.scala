package com.lambdarat.navalcombat.core

import com.lambdarat.navalcombat.scenes.placement.viewmodel.PlacementViewModel

import indigo.*
import indigo.shared.temporal.Signal
import indigoextras.ui.Button

final case class LandingViewModel(
    play: Button,
    playMessage: Text,
    welcomeMessage: Text
)

final case class NavalCombatViewModel(landing: LandingViewModel, placement: PlacementViewModel)
