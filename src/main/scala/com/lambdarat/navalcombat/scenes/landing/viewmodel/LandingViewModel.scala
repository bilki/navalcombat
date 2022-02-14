package com.lambdarat.navalcombat.scenes.landing.viewmodel

import indigo.*
import indigo.Material.ImageEffects
import indigoextras.ui.Button

final case class LandingViewModel(
    play: Button,
    playMessage: Text[ImageEffects],
    welcomeMessage: Text[ImageEffects]
)
