package com.lambdarat.navalcombat.scenes.placement

import com.lambdarat.navalcombat.assets.Assets.*
import com.lambdarat.navalcombat.core.given
import com.lambdarat.navalcombat.core.Ship.*
import com.lambdarat.navalcombat.scenes.placement.viewmodel.*
import com.lambdarat.navalcombat.utils.given
import com.lambdarat.navalcombat.utils.*

import indigo.*
import indigo.Material.Bitmap

object PlacementView:

  private val NUMBER_OF_LETTERS    = 10
  private val FIRST_LETTER         = 'A'
  private val LAST_LETTER          = 'J'
  private val NUMBER_OF_NUMBERS    = 10
  private val LETTER_MARGIN        = 16
  private val NUMBER_MARGIN        = 24
  private val BOATS_MARGIN         = 20
  private val GRID_TOP_MARGIN      = 80
  private val BOAT_SPACING         = 50
  private val GRID_WIDTH           = 630
  private val CELL_WIDTH           = 63
  private val DRAG_AND_DROP_HEIGHT = 60

  def computeGridPoints(width: Int, height: Int): List[Point] =
    val center = Point(width / 2, height / 2)

    val gridIndent = (width - GRID_WIDTH) / 2

    // 10x10 grid positions
    val gridPoints =
      for
        i <- 0 until GRID_WIDTH by CELL_WIDTH
        j <- 0 until GRID_WIDTH by CELL_WIDTH
      yield Point(i + gridIndent, j + GRID_TOP_MARGIN)

    gridPoints.toList

  def computeBoats(width: Int): List[SidebarShip] =
    val boatAlignPoints =
      (0 until BOAT_SPACING * 5 by BOAT_SPACING).map(height =>
        Point(width - BOATS_MARGIN, GRID_TOP_MARGIN + DRAG_AND_DROP_HEIGHT + height)
      )

    List(
      (Destroyer, destroyer),
      (Submarine, submarine),
      (Cruiser, cruiser),
      (Battleship, battleship),
      (Carrier, carrier)
    ).zip(boatAlignPoints).map { case ((ship, boat), point) =>
      SidebarShip(ship, boat.scaleBy(0.5, 0.5).withPosition(point).alignRight)
    }

  // Storyboard:
  //   1. Show message for 0.75 seconds
  //   2. Move message to the top in 1 second
  //   3. After 1.75 seconds, paint the grid and the dragable ships
  def draw(current: Seconds, viewModel: PlacementViewModel, placementMessage: Text): SceneUpdateFragment =
    val timeSinceEnter   = current - viewModel.startTime
    val placeMsgShowTime = Seconds(0.75)
    val showGridTime     = placeMsgShowTime + Seconds(1)

    val placeMessage = placementMessage.moveTo(viewModel.placeMsgSignal.at(timeSinceEnter - placeMsgShowTime))

    val showGrid = Signal.Time.when(_ >= showGridTime, positive = 1.0, negative = 0.0)
    val grid = viewModel.gridPoints.map(position =>
      emptyCell.withPosition(position).modifyMaterial { case bm: Bitmap =>
        bm.toImageEffects.withAlpha(showGrid.at(timeSinceEnter))
      }
    )

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
    val gridLetters =
      viewModel.gridPoints.take(NUMBER_OF_LETTERS).zip(FIRST_LETTER to LAST_LETTER).map { case (position, letter) =>
        postGridMessage(letter.toString, position.withX(position.x - LETTER_MARGIN).withY(position.y + LETTER_MARGIN))
      }

    // Column numbers
    val gridNumbers =
      viewModel.gridPoints.zipWithIndex.filter(_._2 % NUMBER_OF_NUMBERS == 0).zip(1 to NUMBER_OF_NUMBERS).map {
        case ((position, _), number) =>
          postGridMessage(
            number.toString,
            position.withX(position.x + NUMBER_MARGIN * 2).withY(position.y - NUMBER_MARGIN)
          )
      }

    val GRID_HEIGHT = grid.head.position.y

    val dragAndDropText =
      postGridMessage(
        "Drag and drop\nPress R to rotate",
        Point(viewModel.bounds.width - BOATS_MARGIN, GRID_HEIGHT),
        RGBA.Red
      )

    val sidebarBoats = viewModel.boats.map { case SidebarShip(shipType, shipGraphic) =>
      shipGraphic.modifyMaterial { case bm: Bitmap =>
        if viewModel.dragging.exists(_.shipType == shipType) then bm.toImageEffects.withAlpha(0.0)
        else bm.toImageEffects.withAlpha(showGrid.at(timeSinceEnter))
      }
    }

    val basicPlacementSceneNodes = dragAndDropText :: placeMessage :: grid ++ gridLetters ++ gridNumbers ++ sidebarBoats

    val sceneNodes = viewModel.dragging match
      case Some(SidebarShip(_, shipGraphic)) => shipGraphic :: basicPlacementSceneNodes
      case None                              => basicPlacementSceneNodes

    SceneUpdateFragment(sceneNodes)
