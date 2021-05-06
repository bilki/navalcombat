package com.lambdarat.navalcombat.scenes.placement

import com.lambdarat.navalcombat.assets.Assets.*
import com.lambdarat.navalcombat.scenes.placement.viewmodel.PlacementViewModel
import com.lambdarat.navalcombat.utils.*

import indigo.*
import indigo.Material.Bitmap

object PlacementView:

  // Storyboard:
  //   1. Show message for 0.75 seconds
  //   2. Move message to the top in 1 second
  //   3. After 1.75 seconds, paint the grid and the dragable ships
  def draw(running: Seconds, viewModel: PlacementViewModel, placementMessage: Text): SceneUpdateFragment =
    val timeSinceEnter   = running - viewModel.startTime
    val placeMsgShowTime = Seconds(0.75)
    val showGridTime     = placeMsgShowTime + Seconds(1)

    val placeMessage = placementMessage.moveTo(viewModel.placeMsgSignal.at(timeSinceEnter - placeMsgShowTime))

    val showGrid = Signal.Time.when(_ >= showGridTime, 1.0, 0.0)
    val grid = viewModel.gridPoints.map(position =>
      emptyCell.withPosition(position).modifyMaterial { case bm: Bitmap =>
        bm.toImageEffects.withAlpha(showGrid.at(timeSinceEnter))
      }
    )

    val gridHeight = grid.head.position.y

    def postGridMessage(msg: String, position: Point, color: RGBA = RGBA.Black): Text =
      placementMessage
        .withText(msg)
        .withPosition(position)
        .withMaterial(
          Material
            .ImageEffects(ponderosaImgName)
            .withOverlay(Fill.Color(color))
            .withAlpha(showGrid.at(timeSinceEnter))
        )
        .alignRight

    // Row letters
    val gridLetters = viewModel.gridPoints.take(10).zip('A' to 'J').map { case (position, letter) =>
      postGridMessage(letter.toString, position.withX(position.x - 16).withY(position.y + 16))
    }

    // Column numbers
    val gridNumbers =
      viewModel.gridPoints.zipWithIndex.filter(_._2 % 10 == 0).zip(1 to 10).map { case ((position, _), number) =>
        postGridMessage(number.toString, position.withX(position.x + 48).withY(position.y - 24))
      }

    val dragAndDropText =
      postGridMessage("Drag and drop\nPress R to rotate", Point(viewModel.bounds.width - 20, gridHeight), RGBA.Red)

    val boatAlignPoints =
      (0 until 250 by 50).map(height => Point(viewModel.bounds.width - 20, gridHeight + 60 + height))

    val boats = List(destroyer, submarine, cruiser, battleship, carrier)
      .zip(boatAlignPoints)
      .map { case (boat, point) => boat.scaleBy(0.5, 0.5).withPosition(point).alignRight }
      .map(_.modifyMaterial { case bm: Bitmap =>
        bm.toImageEffects.withAlpha(showGrid.at(timeSinceEnter))
      })

    SceneUpdateFragment(dragAndDropText :: placeMessage :: grid ++ gridLetters ++ gridNumbers ++ boats)
