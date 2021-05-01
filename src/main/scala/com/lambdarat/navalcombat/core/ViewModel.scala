package com.lambdarat.navalcombat.core

import indigo.Text
import indigoextras.ui.Button

final case class LandingViewModel(
    play: Button,
    playMessage: Text,
    welcomeMessage: Text
)
final case class NavalCombatViewModel(landing: LandingViewModel)
