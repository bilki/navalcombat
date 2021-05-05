package com.lambdarat.navalcombat.scenes.placement

import com.lambdarat.navalcombat.assets.Assets
import com.lambdarat.navalcombat.scenes.placement.viewmodel.PlacementViewModel

import indigo.*

object PlacementView:

  // 10x10 grid positions
  val gridPoints =
    for
      i <- 0 until 640 by 64
      j <- 0 until 640 by 64
    yield Point(i, j)

  // Storyboard:
  //   1. Show message for 0.75 seconds
  //   2. Move message to the top in 1 second
  //   3. After 1.75 seconds, paint the grid
  def draw(running: Seconds, viewModel: PlacementViewModel, placementMessage: Text): SceneUpdateFragment =
    val timeSinceEnter   = running - viewModel.startTime
    val placeMsgShowTime = Seconds(0.75)

    val cells = gridPoints.map(Assets.emptyCell.withPosition)

    val placeMessage = placementMessage.moveTo(viewModel.placeMsgSignal.at(timeSinceEnter - placeMsgShowTime))

    SceneUpdateFragment(cells*)
