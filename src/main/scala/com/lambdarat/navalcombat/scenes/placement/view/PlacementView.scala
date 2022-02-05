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
import indigo.Material.ImageEffects
import com.lambdarat.navalcombat.draw.{Axis, Graphics, Grid}

object PlacementView:
  def placementViewCellGraphics(
      board: Board,
      highlighted: List[Highlighted]
  )(cell: Cell, coord: Coord, position: Point): Option[Graphic[ImageEffects]] =
    val highlightColor = getHighlightColor(board, highlighted, cell, coord)

    cell match
      case Cell.Unknown => Some(Graphics.empty(highlightColor).withPosition(position))
      case Cell.Miss    => Some(missCell.withPosition(position))
      case Cell.Floating(partOf) =>
        board.ships.get(partOf).flatMap { case ShipOrientation(shipCoords, shipRotation) =>
          if coord == shipCoords then
            val shipGraphic = Graphics.graphicFor(partOf, highlightColor)

            val rotatedShip = shipRotation match
              case Rotation.Horizontal =>
                shipGraphic.withPosition(position).withRotation(shipRotation.angle)
              case Rotation.Vertical =>
                shipGraphic
                  .withPosition(position)
                  .moveBy(CELL_WIDTH, -CELL_WIDTH * (partOf.size.toInt - 1))
                  .withRotation(shipRotation.angle)

            Some(rotatedShip)
          else None
        }
      case Cell.Sunk(partOf) => Some(Graphics.empty(highlightColor).withPosition(position))
  end placementViewCellGraphics

  def getHighlightColor(board: Board, highlighted: List[Highlighted], cell: Cell, coord: Coord): Highlight =
    val maybeCurrentCoordHighlighted = highlighted.find(_.position == coord).map(_.highlight)
    val maybeShipSectionHighlighted =
      cell match
        case Cell.Floating(partOf) =>
          board.ships.get(partOf).flatMap { orientation =>
            val sections = orientation.sections(partOf)
            highlighted.find(hc => sections.contains(hc.position)).map(_.highlight)
          }
        case _ => None

    maybeCurrentCoordHighlighted.orElse(maybeShipSectionHighlighted).getOrElse(Highlight.Neutral)
  end getHighlightColor

  def createMessage(text: Text[ImageEffects])(msg: String): Text[ImageEffects] =
    text
      .withText(msg)
      .withMaterial(text.material.withOverlay(Fill.Color(RGBA.Black)))
      .alignRight

  private val SHIPS_MARGIN         = 20
  private val GRID_TOP_MARGIN      = 70
  private val SHIPS_SPACING        = 50
  private val GRID_WIDTH           = 640
  private val CELL_WIDTH           = 64
  private val DRAG_AND_DROP_HEIGHT = 60
  private val PLACEMENT_MSG_MARGIN = 15

  def computeGridBounds(setupData: NavalCombatSetupData): Rectangle =
    val gridX = (setupData.screenBounds.width - GRID_WIDTH) / 2
    val gridY = GRID_TOP_MARGIN

    Rectangle(gridX, gridY, GRID_WIDTH, GRID_WIDTH)

  def computSidebarShipGraphics(screenWidth: Int, gridMargin: Int): SidebarShipGraphics =
    def sidebarShipPoint(height: Int): Point =
      Point(screenWidth - SHIPS_MARGIN, gridMargin + DRAG_AND_DROP_HEIGHT + height)

    def sidebarShipGraphicFor(ship: Ship, position: Point): Graphic[ImageEffects] =
      Graphics
        .graphicFor(ship)
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

  def sidebarShipGraphicFor(ship: Ship, sidebarShipGraphics: SidebarShipGraphics): Graphic[ImageEffects] =
    ship match
      case Destroyer  => sidebarShipGraphics.destroyer
      case Cruiser    => sidebarShipGraphics.cruiser
      case Submarine  => sidebarShipGraphics.submarine
      case Battleship => sidebarShipGraphics.battleship
      case Carrier    => sidebarShipGraphics.carrier

  def draw(
      model: NavalCombatModel,
      viewModel: PlacementViewModel,
      text: Text[ImageEffects],
      mousePosition: Point
  ): SceneUpdateFragment =
    val putMessage = createMessage(text)

    val originSpace     = viewModel.sceneSettings.modelSpace
    val targetSpace     = viewModel.sceneSettings.gridBounds
    val board           = model.board
    val cellGraphicsFun = placementViewCellGraphics(model.board, viewModel.highlightedCells)

    val grid        = Grid.draw(originSpace, targetSpace, model.board, cellGraphicsFun)
    val lettersAxis = Axis.drawLetters(originSpace, targetSpace, putMessage)
    val numbersAxis = Axis.drawNumbers(originSpace, targetSpace, putMessage)

    val screenWidth = viewModel.sceneSettings.sceneBounds.width
    val gridMargin  = viewModel.sceneSettings.gridBounds.y

    val title = text.moveTo(viewModel.sceneSettings.sceneBounds.center.x, PLACEMENT_MSG_MARGIN)

    val dragAndDropText =
      putMessage("Click and place\nPress R to rotate").withPosition(Point(screenWidth - SHIPS_MARGIN, gridMargin))

    val sidebarShips = viewModel.sidebarShips.map { ship =>
      val sidebarShipGraphic = sidebarShipGraphicFor(ship, viewModel.sidebarShipGraphics)
      viewModel.sidebarShips.find(_ == ship).map(_ => sidebarShipGraphic)
    }

    val basicPlacementSceneNodes =
      dragAndDropText :: title :: grid.toList.flatten ++ lettersAxis ++ numbersAxis ++ sidebarShips.flatten

    val sceneNodes = viewModel.dragging match
      case Some(PlacingShip(ship, rotation)) =>
        val shipGraphic = Graphics.graphicFor(ship)
        val trackingShip = shipGraphic
          .moveTo(mousePosition)
          .withRef(shipGraphic.center)
          .rotateTo(rotation.angle)

        basicPlacementSceneNodes :+ trackingShip
      case None => basicPlacementSceneNodes

    SceneUpdateFragment(sceneNodes)
