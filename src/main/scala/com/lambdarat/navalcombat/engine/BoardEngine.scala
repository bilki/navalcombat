package com.lambdarat.navalcombat.engine

import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.core.Rotation.*
import com.lambdarat.navalcombat.core.Cell.*
import com.lambdarat.navalcombat.core.given

import scala.language.implicitConversions

object BoardEngine:

  private def validCoords(x: XCoord, y: YCoord): Boolean = x >= 0 && x < 10 && y >= 0 && y < 10

  extension (board: Board)

    def get(x: XCoord, y: YCoord): Option[Cell] =
      Option.when(validCoords(x, y))(board.cells(x)(y))

    def update(x: XCoord, y: YCoord, value: Cell): Board =
      Option
        .when(validCoords(x, y)) {
          val row          = board.cells(x)
          val updatedRow   = row.updated(y, value)
          val updatedCells = board.cells.updated(x, updatedRow)

          board.copy(cells = updatedCells)
        }
        .getOrElse(board)

    // x,_ for Horizontal rotation starts from the left-most cell
    // _,y for Vertical rotation starts from the bottom-most cell
    def canPlace(ship: Ship, rotation: Rotation, x: XCoord, y: YCoord): Boolean =
      val maybeCanPlace = Option.when(validCoords(x, y)) {
        rotation match
          case Vertical =>
            (0 until ship.size)
              .forall(shipY =>
                validCoords(x, y + shipY) && board.get(x, y + shipY).fold(false) {
                  case Unknown => true
                  case _       => false
                }
              )
          case Horizontal =>
            (0 until ship.size)
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
            (0 until ship.size).foldLeft(board) { case (oldBoard, shipY) =>
              oldBoard.update(x, y + shipY, Floating(ship))
            }
          case Horizontal =>
            (0 until ship.size).foldLeft(board) { case (oldBoard, shipX) =>
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
