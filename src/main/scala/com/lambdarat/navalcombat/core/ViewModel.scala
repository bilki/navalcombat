package com.lambdarat.navalcombat.core

import indigo.*
import indigo.shared.temporal.Signal
import indigoextras.ui.Button

final case class LandingViewModel(
    play: Button,
    playMessage: Text,
    welcomeMessage: Text
)

final case class PlacementViewModel(
    startTime: Seconds,
    placeMsgSignal: Signal[Point]
)

final case class NavalCombatViewModel(landing: LandingViewModel, placement: PlacementViewModel)
