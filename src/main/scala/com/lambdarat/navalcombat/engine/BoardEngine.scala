package com.lambdarat.navalcombat.engine

import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.core.XCoord.*
import com.lambdarat.navalcombat.core.YCoord.*
import com.lambdarat.navalcombat.core.Rotation.*
import com.lambdarat.navalcombat.core.Cell.*
import com.lambdarat.navalcombat.core.given

object BoardEngine:

  private def validCoords(x: XCoord, y: YCoord): Boolean =
    x.toInt >= 0 && x.toInt < Board.BOARD_SIZE && y.toInt >= 0 && y.toInt < Board.BOARD_SIZE

  extension (board: Board)

    def pretty: String =
      board.cells.zipWithIndex.map { (row, idx) =>
        s"$idx [${row.mkString(";")}]"
      }.mkString("\n")

    def get(x: XCoord, y: YCoord): Option[Cell] =
      Option.when(validCoords(x, y))(board.cells(x.toInt)(y.toInt))

    private[engine] def update(x: XCoord, y: YCoord, value: Cell): Option[Board] =
      Option
        .when(validCoords(x, y)) {
          val row          = board.cells(x.toInt)
          val updatedRow   = row.updated(y.toInt, value)
          val updatedCells = board.cells.updated(x.toInt, updatedRow)

          board.copy(cells = updatedCells)
        }

    def isEmpty(x: XCoord, y: YCoord): Boolean = get(x, y).exists(_ == Cell.Unknown)

    // x,_ for Horizontal rotation starts from the left-most cell
    // _,y for Vertical rotation starts from the bottom-most cell
    def canPlace(ship: Ship, rotation: Rotation, x: XCoord, y: YCoord): Boolean =
      lazy val alreadyPlaced = board.ships.keys.toList.contains(ship)
      lazy val hasValidCoords = rotation match
        case Vertical =>
          (0 until ship.size.toInt).forall(yShift => isEmpty(x, y - yShift))
        case Horizontal =>
          (0 until ship.size.toInt).forall(xShift => isEmpty(x + xShift, y))

      !alreadyPlaced && hasValidCoords

    // Only places the ship if validation is successful
    def place(ship: Ship, rotation: Rotation, x: XCoord, y: YCoord): Option[Board] =
      val (xUpdate, yUpdate) =
        rotation match
          case Vertical   => (_ => x, y - _): (Int => XCoord, Int => YCoord)
          case Horizontal => (x + _, _ => y): (Int => XCoord, Int => YCoord)

      if canPlace(ship, rotation, x, y) then
        val maybeUpdatedBoard = (0 until ship.size.toInt).foldLeft(Option(board)) { (oldBoard, shipCoordInc) =>
          oldBoard.flatMap(_.update(xUpdate(shipCoordInc), yUpdate(shipCoordInc), Floating(ship)))
        }

        maybeUpdatedBoard.map(updated =>
          updated.copy(ships = updated.ships + (ship -> ShipOrientation(Coord(x, y), rotation)))
        )
      else None

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
