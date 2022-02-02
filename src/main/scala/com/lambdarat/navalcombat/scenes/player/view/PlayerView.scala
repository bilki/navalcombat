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
import com.lambdarat.navalcombat.draw.{Axis, Grid}

object PlayerView:
  def playerViewCellGraphics(cell: Cell, coord: Coord, position: Point): Option[Graphic[Bitmap]] =
    val graphic = cell match
      case Cell.Unknown          => emptyCell.withPosition(position)
      case Cell.Miss             => missCell.withPosition(position)
      case Cell.Floating(partOf) => emptyCell.withPosition(position)
      case Cell.Sunk(partOf)     => hitCell.withPosition(position)

    Some(graphic)
  end playerViewCellGraphics

  private val GRID_TOP_MARGIN   = 70
  private val GRID_WIDTH        = 640
  private val PLAYER_MSG_MARGIN = 15

  def computeGridBounds(setupData: NavalCombatSetupData): Rectangle =
    val gridX = (setupData.screenBounds.width - GRID_WIDTH) / 2
    val gridY = GRID_TOP_MARGIN

    Rectangle(gridX, gridY, GRID_WIDTH, GRID_WIDTH)
  end computeGridBounds

  def createMessage(text: Text[ImageEffects])(msg: String): Text[ImageEffects] =
    text
      .withText(msg)
      .withMaterial(text.material.withOverlay(Fill.Color(RGBA.Black)))
      .alignRight

  def draw(
      model: NavalCombatModel,
      viewModel: PlayerViewModel,
      text: Text[ImageEffects]
  ): SceneUpdateFragment =
    val placeMessage = text.moveTo(viewModel.sceneSettings.sceneBounds.center.x, PLAYER_MSG_MARGIN)

    val putMessage = createMessage(text)

    val originSpace     = viewModel.sceneSettings.modelSpace
    val targetSpace     = viewModel.sceneSettings.gridBounds
    val board           = model.board
    val cellGraphicsFun = playerViewCellGraphics

    val grid        = Grid.draw(originSpace, targetSpace, board, cellGraphicsFun)
    val lettersAxis = Axis.drawLetters(originSpace, targetSpace, putMessage)
    val numbersAxis = Axis.drawNumbers(originSpace, targetSpace, putMessage)

    val sceneNodes = placeMessage :: grid.toList.flatten ++ lettersAxis ++ numbersAxis

    SceneUpdateFragment(sceneNodes)
  end draw
