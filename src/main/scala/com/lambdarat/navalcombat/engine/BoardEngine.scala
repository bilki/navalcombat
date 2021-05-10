package com.lambdarat.navalcombat.engine

import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.core.XCoord.*
import com.lambdarat.navalcombat.core.YCoord.*
import com.lambdarat.navalcombat.core.Rotation.*
import com.lambdarat.navalcombat.core.Cell.*
import com.lambdarat.navalcombat.core.given

object BoardEngine:

  private def validCoords(x: XCoord, y: YCoord): Boolean = x.toInt >= 0 && x.toInt < 10 && y.toInt >= 0 && y.toInt < 10

  extension (board: Board)

    def get(x: XCoord, y: YCoord): Option[Cell] =
      Option.when(validCoords(x, y))(board.cells(x.toInt)(y.toInt))

    def update(x: XCoord, y: YCoord, value: Cell): Board =
      Option
        .when(validCoords(x, y)) {
          val row          = board.cells(x.toInt)
          val updatedRow   = row.updated(y.toInt, value)
          val updatedCells = board.cells.updated(x.toInt, updatedRow)

          board.copy(cells = updatedCells)
        }
        .getOrElse(board)

    // x,_ for Horizontal rotation starts from the left-most cell
    // _,y for Vertical rotation starts from the bottom-most cell
    def canPlace(ship: Ship, rotation: Rotation, x: XCoord, y: YCoord): Boolean =
      val maybeCanPlace = Option.when(validCoords(x, y)) {
        rotation match
          case Vertical =>
            (0 until ship.size.toInt)
              .forall(shipY =>
                validCoords(x, y + shipY) && board.get(x, y + shipY).fold(false) {
                  case Unknown => true
                  case _       => false
                }
              )
          case Horizontal =>
            (0 until ship.size.toInt)
              .forall(shipX =>
                validCoords(x + shipX, y) && board.get(x + shipX, y).fold(false) {
                  case Unknown => true
                  case _       => false
                }
              )
      }

      maybeCanPlace.getOrElse(false)

    // Only places the ship if validation is successful
    def place(ship: Ship, rotation: Rotation, x: XCoord, y: YCoord): Board =
      if canPlace(ship, rotation, x, y) then
        rotation match
          case Vertical =>
            (0 until ship.size.toInt).foldLeft(board) { (oldBoard, shipY) =>
              oldBoard.update(x, y + shipY, Floating(ship))
            }
          case Horizontal =>
            (0 until ship.size.toInt).foldLeft(board) { (oldBoard, shipX) =>
              oldBoard.update(x + shipX, y, Floating(ship))
            }
      else board

    def isCompletelySunk(ship: Ship): Boolean =
      val partsSunk = board.cells.flatten.count {
        case Sunk(partOf) => partOf == ship
        case _            => false
      }

      ship.size == partsSunk

    def shoot(x: XCoord, y: YCoord): Option[Miss.type | Sunk] =
      board.get(x, y).map {
        case Unknown | Miss => Miss
        case Floating(ship) => Sunk(ship)
        case s: Sunk        => s
      }
