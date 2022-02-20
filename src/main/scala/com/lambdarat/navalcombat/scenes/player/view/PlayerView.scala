package com.lambdarat.navalcombat.scenes.player

import com.lambdarat.navalcombat.assets.Assets.*
import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.core.Cell.*
import com.lambdarat.navalcombat.core.Ship.*
import com.lambdarat.navalcombat.draw.*
import com.lambdarat.navalcombat.engine.BoardEngine.*
import com.lambdarat.navalcombat.scenes.player.viewmodel.PlayerViewModel
import com.lambdarat.navalcombat.utils.*

import indigo.*
import indigo.Material.ImageEffects

object PlayerView:
  def enemyViewCellGraphics(cell: Cell, coord: Coord, position: Point): Option[Graphic[ImageEffects]] =
    val graphic = cell match
      case Cell.Unknown     => emptyCell.withPosition(position)
      case Cell.Miss        => missCell.withPosition(position)
      case _: Cell.Floating => emptyCell.withPosition(position)
      case _: Cell.Sunk     => hitCell.withPosition(position)

    Some(graphic)
  end enemyViewCellGraphics

  private val GRID_TOP_MARGIN   = 70
  private val GRID_WIDTH        = 640
  private val GRID_OFFSET       = 50
  private val MINI_GRID_WIDTH   = 250
  private val MINI_GRID_SCALE   = 0.390625
  private val MINI_GRID_SPACER  = 75
  private val PLAYER_MSG_MARGIN = 15
  private val CELL_WIDTH        = 64

  def miniViewCellGraphics(board: Board, scale: Vector2, cellWidth: Int)(
      cell: Cell,
      coord: Coord,
      position: Point
  ): Option[Graphic[ImageEffects]] =
    cell match
      case Cell.Unknown => Some(emptyCell.scaleBy(scale).withPosition(position))
      case Cell.Miss    => Some(missCell.scaleBy(scale).withPosition(position))
      case Cell.Floating(partOf, section) =>
        board.ships.get(partOf).flatMap { case ShipLocation(_, shipRotation) =>
          val shipGraphic = Graphics.graphicFor(partOf, section)

          val rotatedShip = shipRotation match
            case Rotation.Horizontal =>
              shipGraphic
                .scaleBy(scale)
                .withPosition(position)
                .withRotation(shipRotation.angle)
            case Rotation.Vertical =>
              shipGraphic
                .scaleBy(scale)
                .withPosition(position)
                .withRef(shipGraphic.bounds.topRight)
                .withRotation(shipRotation.angle + Radians.PI)

          Some(rotatedShip)
        }
      case s: Cell.Sunk => Some(hitCell.scaleBy(scale).withPosition(position))
  end miniViewCellGraphics

  def computeGridBounds(setupData: NavalCombatSetupData): Rectangle =
    val gridX = (setupData.screenBounds.width - GRID_WIDTH) / 2 - GRID_OFFSET
    val gridY = GRID_TOP_MARGIN

    Rectangle(gridX, gridY, GRID_WIDTH, GRID_WIDTH)
  end computeGridBounds

  def draw(
      model: NavalCombatModel,
      viewModel: PlayerViewModel,
      text: Text[ImageEffects]
  ): SceneUpdateFragment =
    val placeMessage = text.moveTo(viewModel.sceneSettings.sceneBounds.center.x, PLAYER_MSG_MARGIN)

    val putMessage = text.alignRight.withText

    // Enemy grid
    val originSpace     = viewModel.sceneSettings.modelSpace
    val targetSpace     = viewModel.sceneSettings.gridBounds
    val board           = model.enemy
    val cellGraphicsFun = enemyViewCellGraphics

    val grid        = Grid.draw(originSpace, targetSpace, board, cellGraphicsFun)
    val lettersAxis = Axis.drawLetters(originSpace, targetSpace, putMessage)
    val numbersAxis = Axis.drawNumbers(originSpace, targetSpace, putMessage)

    val playerGridNodes = grid.toList.flatten ++ lettersAxis ++ numbersAxis

    // Mini view
    val miniOriginSpace = viewModel.sceneSettings.modelSpace
    val miniTargetSpace =
      Rectangle(targetSpace.x + GRID_WIDTH + MINI_GRID_SPACER, targetSpace.y, MINI_GRID_WIDTH, MINI_GRID_WIDTH)
    val miniBoard       = model.player
    val miniScale       = Vector2(MINI_GRID_SCALE, MINI_GRID_SCALE)
    val miniGraphicsFun = miniViewCellGraphics(miniBoard, miniScale, CELL_WIDTH)

    val miniGrid        = Grid.draw(miniOriginSpace, miniTargetSpace, miniBoard, miniGraphicsFun)
    val miniLetterAxis  = Axis.drawLetters(miniOriginSpace, miniTargetSpace, putMessage, miniScale)
    val miniNumbersAxis = Axis.drawNumbers(miniOriginSpace, miniTargetSpace, putMessage, miniScale)

    val miniGridNodes = miniGrid.toList.flatten ++ miniLetterAxis ++ miniNumbersAxis

    val sceneNodes = placeMessage :: playerGridNodes ++ miniGridNodes

    SceneUpdateFragment(sceneNodes)
  end draw
