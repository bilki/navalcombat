package com.lambdarat.navalcombat.scenes.placement.view

import com.lambdarat.navalcombat.assets.Assets.*
import com.lambdarat.navalcombat.core.given
import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.core.Ship.*
import com.lambdarat.navalcombat.engine.BoardEngine.*
import com.lambdarat.navalcombat.scenes.placement.viewmodel.given
import com.lambdarat.navalcombat.scenes.placement.viewmodel.*
import com.lambdarat.navalcombat.utils.given
import com.lambdarat.navalcombat.utils.*

import indigo.*
import indigo.Material.Bitmap
import indigoextras.effectmaterials.*

object PlacementView:

  private val NUMBER_OF_LETTERS = 10
  private val FIRST_LETTER      = 'A'
  private val LAST_LETTER       = 'J'
  private val NUMBER_OF_NUMBERS = 10

  // These values should be relative to magnification...
  private val LETTER_MARGIN        = 16
  private val NUMBER_MARGIN        = 24
  private val SHIPS_MARGIN         = 20
  private val GRID_TOP_MARGIN      = 70
  private val SHIPS_SPACING        = 50
  private val GRID_WIDTH           = 640
  private val CELL_WIDTH           = 64
  private val DRAG_AND_DROP_HEIGHT = 60
  private val PLACEMENT_MSG_MARGIN = 15

  val movePlacementMsg =
    SignalReader[Point, Point](start => Signal.Lerp(start, Point(start.x, PLACEMENT_MSG_MARGIN), Seconds(1)))

  def computeGridBounds(setupData: NavalCombatSetupData): Rectangle =
    val gridX = (setupData.screenBounds.width - GRID_WIDTH) / 2
    val gridY = GRID_TOP_MARGIN

    Rectangle(gridX, gridY, GRID_WIDTH, GRID_WIDTH)

  def computSidebarShipGraphics(screenWidth: Int, gridMargin: Int): SidebarShipGraphics =
    def sidebarShipPoint(height: Int): Point =
      Point(screenWidth - SHIPS_MARGIN, gridMargin + DRAG_AND_DROP_HEIGHT + height)

    def sidebarShipGraphicFor(ship: Ship, position: Point): Graphic =
      graphicFor(ship)
        .scaleBy(0.5, 0.5)
        .withPosition(position)
        .alignRight

    SidebarShipGraphics(
      destroyer = sidebarShipGraphicFor(Destroyer, sidebarShipPoint(Destroyer.ordinal * SHIPS_SPACING)),
      cruiser = sidebarShipGraphicFor(Cruiser, sidebarShipPoint(Cruiser.ordinal * SHIPS_SPACING)),
      submarine = sidebarShipGraphicFor(Submarine, sidebarShipPoint(Submarine.ordinal * SHIPS_SPACING)),
      battleship = sidebarShipGraphicFor(Battleship, sidebarShipPoint(Battleship.ordinal * SHIPS_SPACING)),
      carrier = sidebarShipGraphicFor(Carrier, sidebarShipPoint(Carrier.ordinal * SHIPS_SPACING))
    )

  def graphicFor(ship: Ship): Graphic =
    ship match
      case Destroyer  => destroyer
      case Cruiser    => cruiser
      case Submarine  => submarine
      case Battleship => battleship
      case Carrier    => carrier

  def sidebarShipGraphicFor(ship: Ship, sidebarShipGraphics: SidebarShipGraphics): Graphic =
    ship match
      case Destroyer  => sidebarShipGraphics.destroyer
      case Cruiser    => sidebarShipGraphics.cruiser
      case Submarine  => sidebarShipGraphics.submarine
      case Battleship => sidebarShipGraphics.battleship
      case Carrier    => sidebarShipGraphics.carrier

  // Storyboard:
  //   1. Show message for 0.75 seconds
  //   2. Move message to the top in 1 second
  //   3. After 1.75 seconds, paint the grid and the ships to be placed
  def draw(
      current: Seconds,
      model: NavalCombatModel,
      viewModel: PlacementViewModel,
      placementMessage: Text,
      mousePosition: Point
  ): SceneUpdateFragment =
    val timeSinceEnter   = current - viewModel.startTime
    val placeMsgShowTime = Seconds(0.75)
    val showGridTime     = placeMsgShowTime + Seconds(1)

    val placeMessage = placementMessage.moveTo(viewModel.placeMsgSignal.at(timeSinceEnter - placeMsgShowTime))

    val showGrid = Signal.Time.when(_ >= showGridTime, positive = 1.0, negative = 0.0)

    val modelSpace = viewModel.sceneSettings.modelSpace

    val grid =
      for
        x <- 0 until modelSpace.width
        y <- 0 until modelSpace.height
      yield
        val cellCoord      = Coord(XCoord(x), YCoord(y))
        val cellPoint      = cellCoord.toPoint
        val gridSpacePoint = cellPoint.transform(modelSpace, viewModel.sceneSettings.gridBounds)

        val cell = model.board.get(cellCoord.x, cellCoord.y).get

        val maybeCellGraphic = cell match
          case Cell.Unknown => Some(emptyCell.withPosition(gridSpacePoint))
          case Cell.Miss    => Some(missCell.withPosition(gridSpacePoint))
          case Cell.Floating(partOf) =>
            model.board.ships.get(partOf).flatMap { case ShipOrientation(shipCoords, shipRotation) =>
              if cellCoord == shipCoords then
                val shipGraphic = graphicFor(partOf)

                val rotatedShip = shipRotation match
                  case Rotation.Horizontal =>
                    shipGraphic.withPosition(gridSpacePoint).withRotation(shipRotation.angle)
                  case Rotation.Vertical =>
                    shipGraphic
                      .withPosition(gridSpacePoint)
                      .moveBy(CELL_WIDTH, -CELL_WIDTH * (partOf.size.toInt - 1))
                      .withRotation(shipRotation.angle)

                Some(rotatedShip)
              else None
            }
          case Cell.Sunk(partOf) => Some(emptyCell.withPosition(gridSpacePoint))

        val maybeCurrentCoordHighlighted = viewModel.highlightedCells.find(_.position == cellCoord).map(_.highlight)
        val maybeShipSectionHighlighted =
          cell match
            case Cell.Floating(partOf) =>
              model.board.ships.get(partOf).flatMap { orientation =>
                val sections = orientation.sections(partOf)
                viewModel.highlightedCells.find(hc => sections.contains(hc.position)).map(_.highlight)
              }
            case _ => None

        val highlightColor =
          maybeCurrentCoordHighlighted.orElse(maybeShipSectionHighlighted).getOrElse(Highlight.Neutral) match
            case Highlight.Neutral  => RGBA.Zero
            case Highlight.NotValid => RGBA.Yellow
            case Highlight.Valid    => RGBA.White

        maybeCellGraphic.map(_.modifyMaterial { case bm: Bitmap =>
          bm.toImageEffects
            .withOverlay(Fill.Color(highlightColor))
            .withAlpha(showGrid.at(timeSinceEnter))
        })
    end grid

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
      for (letter, y) <- (FIRST_LETTER to LAST_LETTER).zip(0 until modelSpace.height)
      yield
        val cellCoord = Coord(XCoord(0), YCoord(y))
        val cellPoint = cellCoord.toPoint
        val position  = cellPoint.transform(modelSpace, viewModel.sceneSettings.gridBounds)

        postGridMessage(letter.toString, position.withX(position.x - LETTER_MARGIN).withY(position.y + LETTER_MARGIN))

    // Column numbers
    val gridNumbers =
      for (number, x) <- (1 to NUMBER_OF_NUMBERS).zip(0 until modelSpace.width)
      yield
        val cellCoord = Coord(XCoord(x), YCoord(0))
        val cellPoint = cellCoord.toPoint
        val position  = cellPoint.transform(modelSpace, viewModel.sceneSettings.gridBounds)

        postGridMessage(
          number.toString,
          position.withX(position.x + NUMBER_MARGIN * 2).withY(position.y - NUMBER_MARGIN)
        )

    val screenWidth = viewModel.sceneSettings.sceneBounds.width
    val gridMargin  = viewModel.sceneSettings.gridBounds.y

    val dragAndDropText =
      postGridMessage(
        "Click and place\nPress R to rotate",
        Point(screenWidth - SHIPS_MARGIN, gridMargin),
        RGBA.Black
      )

    val sidebarShips = viewModel.sidebarShips.map { ship =>
      val sidebarShipGraphic = sidebarShipGraphicFor(ship, viewModel.sidebarShipGraphics).modifyMaterial {
        case bm: Bitmap =>
          bm.toImageEffects.withAlpha(showGrid.at(timeSinceEnter))
      }

      viewModel.sidebarShips.find(_ == ship).map(_ => sidebarShipGraphic)
    }

    val basicPlacementSceneNodes =
      dragAndDropText :: placeMessage :: grid.toList.flatten ++ gridLetters.toList ++ gridNumbers ++ sidebarShips.flatten

    val sceneNodes = viewModel.dragging match
      case Some(PlacingShip(ship, rotation)) =>
        val trackingShip = graphicFor(ship)
          .rotateTo(rotation.angle)
          .centerAt(mousePosition)

        trackingShip :: basicPlacementSceneNodes
      case None => basicPlacementSceneNodes

    SceneUpdateFragment(sceneNodes)
