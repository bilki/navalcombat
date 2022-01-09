package com.lambdarat.navalcombat.scenes.player

import com.lambdarat.navalcombat.scenes.player.viewmodel.PlayerViewModel
import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.engine.BoardEngine.*
import com.lambdarat.navalcombat.core.Cell.*
import com.lambdarat.navalcombat.core.Cell
import com.lambdarat.navalcombat.assets.Assets.*
import com.lambdarat.navalcombat.utils.*
import com.lambdarat.navalcombat.core.ShipOrientation
import com.lambdarat.navalcombat.core.Rotation
import com.lambdarat.navalcombat.core.Ship
import com.lambdarat.navalcombat.core.Ship.*

import indigo.*
import indigo.Material.Bitmap
import indigo.shared.materials.Material.ImageEffects

object PlayerView:
  private val NUMBER_OF_LETTERS = 10
  private val FIRST_LETTER      = 'A'
  private val LAST_LETTER       = 'J'
  private val NUMBER_OF_NUMBERS = 10

  // These values should be relative to magnification...
  private val LETTER_MARGIN     = 16
  private val NUMBER_MARGIN     = 24
  private val GRID_TOP_MARGIN   = 70
  private val GRID_WIDTH        = 640
  private val CELL_WIDTH        = 64
  private val PLAYER_MSG_MARGIN = 15

  def graphicFor(ship: Ship): Graphic[Bitmap] =
    ship match
      case Destroyer  => destroyer
      case Cruiser    => cruiser
      case Submarine  => submarine
      case Battleship => battleship
      case Carrier    => carrier

  def computeGridBounds(setupData: NavalCombatSetupData): Rectangle =
    val gridX = (setupData.screenBounds.width - GRID_WIDTH) / 2
    val gridY = GRID_TOP_MARGIN

    Rectangle(gridX, gridY, GRID_WIDTH, GRID_WIDTH)
  end computeGridBounds

  def draw(
      model: NavalCombatModel,
      viewModel: PlayerViewModel,
      placementMessage: Text[ImageEffects]
  ): SceneUpdateFragment =
    val placeMessage = placementMessage.moveTo(viewModel.sceneSettings.sceneBounds.center.x, PLAYER_MSG_MARGIN)

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
          case Cell.Unknown          => Some(emptyCell.withPosition(gridSpacePoint))
          case Cell.Miss             => Some(missCell.withPosition(gridSpacePoint))
          case Cell.Floating(partOf) => Some(emptyCell.withPosition(gridSpacePoint))
          case Cell.Sunk(partOf)     => Some(emptyCell.withPosition(gridSpacePoint))

        maybeCellGraphic
    end grid

    def postGridMessage(msg: String, position: Point, color: RGBA = RGBA.Black): Text[ImageEffects] =
      placementMessage
        .withText(msg)
        .withPosition(position)
        .withMaterial(
          Material
            .ImageEffects(ponderosaImgName)
            .withOverlay(Fill.Color(color))
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

    val sceneNodes = placeMessage :: grid.toList.flatten ++ gridLetters.toList ++ gridNumbers

    SceneUpdateFragment(sceneNodes)
  end draw
