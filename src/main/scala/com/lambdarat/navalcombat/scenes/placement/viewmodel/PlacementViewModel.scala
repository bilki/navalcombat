package com.lambdarat.navalcombat.scenes.placement.viewmodel

import indigo.*

final case class PlacementViewModel(
    bounds: Rectangle,
    startTime: Seconds,
    gridPoints: List[Point],
    placeMsgSignal: Signal[Point]
)
