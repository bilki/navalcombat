package com.lambdarat.navalcombat.engine

import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.engine.BoardEngine.*
import com.lambdarat.navalcombat.utils.given

import indigo.Dice

import scala.annotation.tailrec

object AutomatonEngine:

  private def generateShipOrientation(dice: Dice): ShipOrientation =
    val x             = XCoord(dice.roll(Board.BOARD_SIZE))
    val y             = YCoord(dice.roll(Board.BOARD_SIZE))
    val coord         = Coord(x, y)
    val rotationValue = dice.roll(Rotation.values.size) - 1
    val rotation      = Rotation.fromOrdinal(rotationValue)

    ShipOrientation(coord, rotation)
  end generateShipOrientation

  @tailrec
  private def placeBoardShips(dice: Dice, board: Board, ships: List[Ship]): Board =
    ships match
      case Nil => board
      case ship :: rest =>
        val shipOrientation = generateShipOrientation(dice)
        board.place(ship, shipOrientation.rotation, shipOrientation.coord.x, shipOrientation.coord.y) match
          case Some(updatedBoard) => placeBoardShips(dice, updatedBoard, rest)
          case None               => placeBoardShips(dice, board, ships)

  def placeShips(dice: Dice): Board =
    placeBoardShips(dice, Board.empty, Ship.values.toList)
