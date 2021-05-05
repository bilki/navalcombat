package com.lambdarat.navalcombat.scenes.placement

import com.lambdarat.navalcombat.assets.Assets
import com.lambdarat.navalcombat.scenes.placement.viewmodel.PlacementViewModel

import indigo.*
import indigo.Material.Bitmap

object PlacementView:

  // Storyboard:
  //   1. Show message for 0.75 seconds
  //   2. Move message to the top in 1 second
  //   3. After 1.75 seconds, paint the grid
  def draw(running: Seconds, viewModel: PlacementViewModel, placementMessage: Text): SceneUpdateFragment =
    val timeSinceEnter   = running - viewModel.startTime
    val placeMsgShowTime = Seconds(0.75)
    val showGridTime     = placeMsgShowTime + Seconds(1)

    val placeMessage = placementMessage.moveTo(viewModel.placeMsgSignal.at(timeSinceEnter - placeMsgShowTime))

    val showGrid = Signal.Time.map(time => if time >= showGridTime then 1.0 else 0.0)
    val grid = viewModel.gridPoints.map(position =>
      Assets.emptyCell.withPosition(position).modifyMaterial { case bm: Bitmap =>
        bm.toImageEffects.withAlpha(showGrid.at(timeSinceEnter))
      }
    )

    SceneUpdateFragment(placeMessage :: grid)
