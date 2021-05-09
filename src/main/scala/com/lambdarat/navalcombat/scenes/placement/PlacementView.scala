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
  private val SHIPS_MARGIN         = 20
  private val GRID_TOP_MARGIN      = 80
  private val SHIPS_SPACING        = 50
  private val GRID_WIDTH           = 630
  private val CELL_WIDTH           = 63
  private val DRAG_AND_DROP_HEIGHT = 60

  def computeGridGraphics(width: Int, height: Int): List[Graphic] =
    val center = Point(width / 2, height / 2)

    val gridIndent = (width - GRID_WIDTH) / 2

    // 10x10 grid positions
    val gridGraphics =
      for
        i <- 0 until GRID_WIDTH by CELL_WIDTH
        j <- 0 until GRID_WIDTH by CELL_WIDTH
      yield emptyCell.withPosition(Point(i + gridIndent, j + GRID_TOP_MARGIN))

    gridGraphics.toList

  def computeSidebarShips(width: Int): List[SidebarShip] =
    val shipAlignPoints =
      (0 until SHIPS_SPACING * 5 by SHIPS_SPACING).map(height =>
        Point(width - SHIPS_MARGIN, GRID_TOP_MARGIN + DRAG_AND_DROP_HEIGHT + height)
      )

    List(
      (Destroyer, destroyer),
      (Submarine, submarine),
      (Cruiser, cruiser),
      (Battleship, battleship),
      (Carrier, carrier)
    ).zip(shipAlignPoints).map { case ((ship, shipGraphic), point) =>
      SidebarShip(ship, shipGraphic.scaleBy(0.5, 0.5).withPosition(point).alignRight)
    }

  // Storyboard:
  //   1. Show message for 0.75 seconds
  //   2. Move message to the top in 1 second
  //   3. After 1.75 seconds, paint the grid and the ships to be placed
  def draw(current: Seconds, viewModel: PlacementViewModel, placementMessage: Text): SceneUpdateFragment =
    val timeSinceEnter   = current - viewModel.startTime
    val placeMsgShowTime = Seconds(0.75)
    val showGridTime     = placeMsgShowTime + Seconds(1)

    val placeMessage = placementMessage.moveTo(viewModel.placeMsgSignal.at(timeSinceEnter - placeMsgShowTime))

    val showGrid = Signal.Time.when(_ >= showGridTime, positive = 1.0, negative = 0.0)

    val gridElements = viewModel.grid.asElementList.sortWith { case (c1, c2) =>
      if c1.position.x eq c2.position.x then c1.position.y < c2.position.y
      else c1.position.x < c2.position.x
    }

    val grid = gridElements.map(cell =>
      cell.cellGraphic.modifyMaterial { case bm: Bitmap =>
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
      gridElements.map(_.cellGraphic).take(NUMBER_OF_LETTERS).zip(FIRST_LETTER to LAST_LETTER).map {
        case (cellGraphic, letter) =>
          val position = cellGraphic.position
          postGridMessage(letter.toString, position.withX(position.x - LETTER_MARGIN).withY(position.y + LETTER_MARGIN))
      }

    // Column numbers
    val gridNumbers =
      gridElements
        .map(_.cellGraphic)
        .zipWithIndex
        .filter(_._2 % NUMBER_OF_NUMBERS == 0)
        .zip(1 to NUMBER_OF_NUMBERS)
        .map { case ((cellGraphic, _), number) =>
          val position = cellGraphic.position
          postGridMessage(
            number.toString,
            position.withX(position.x + NUMBER_MARGIN * 2).withY(position.y - NUMBER_MARGIN)
          )
        }

    val GRID_HEIGHT = grid.head.position.y

    val dragAndDropText =
      postGridMessage(
        "Click and place\nPress R to rotate",
        Point(viewModel.bounds.width - SHIPS_MARGIN, GRID_HEIGHT),
        RGBA.Red
      )

    val sidebarShips = viewModel.sidebarShips.map { case SidebarShip(shipType, shipGraphic) =>
      shipGraphic.modifyMaterial { case bm: Bitmap =>
        if viewModel.dragging.exists(_.sidebarShip.shipType == shipType) then bm.toImageEffects.withAlpha(0.0)
        else bm.toImageEffects.withAlpha(showGrid.at(timeSinceEnter))
      }
    }

    val basicPlacementSceneNodes = dragAndDropText :: placeMessage :: grid ++ gridLetters ++ gridNumbers ++ sidebarShips

    val sceneNodes = viewModel.dragging match
      case Some(PlacingShip(SidebarShip(_, shipGraphic), _)) => shipGraphic :: basicPlacementSceneNodes
      case None                                              => basicPlacementSceneNodes

    SceneUpdateFragment(sceneNodes)
