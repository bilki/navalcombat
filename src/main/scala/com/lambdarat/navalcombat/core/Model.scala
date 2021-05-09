package com.lambdarat.navalcombat.core

import scala.collection.immutable.ArraySeq
import scala.annotation.targetName

// X is the horizontal axis
opaque type XCoord = Int

object XCoord:
  def apply(x: Int): XCoord = x

  extension (x: XCoord)
    @targetName("addX")
    def +(dx: Int): XCoord         = XCoord(x + dx)
    def <(other: XCoord): Boolean  = x < other
    def eq(other: XCoord): Boolean = x == other
    def toInt: Int                 = x.toInt

// Y is the vertical axis
opaque type YCoord = Int

object YCoord:
  def apply(y: Int): YCoord = y

  extension (y: YCoord)
    @targetName("addY")
    def +(dy: Int): YCoord         = YCoord(y + dy)
    def <(other: YCoord): Boolean  = y < other
    def eq(other: YCoord): Boolean = y == other
    def toInt: Int                 = y.toInt

final case class Coord(x: XCoord, y: YCoord)

opaque type ShipSize = Int
given CanEqual[ShipSize, Int] = CanEqual.derived

object ShipSize:
  def apply(size: Int): ShipSize = size

  extension (shipShize: ShipSize)
    def toInt: Int       = shipShize.toInt
    def toDouble: Double = shipShize.toDouble

enum Ship(val size: ShipSize):
  case Destroyer  extends Ship(ShipSize(2))
  case Submarine  extends Ship(ShipSize(3))
  case Cruiser    extends Ship(ShipSize(3))
  case Battleship extends Ship(ShipSize(4))
  case Carrier    extends Ship(ShipSize(5))

given CanEqual[Ship, Ship] = CanEqual.derived

enum Cell:
  case Unknown
  case Miss
  case Sunk(partOf: Ship)
  case Floating(partOf: Ship)

given CanEqual[Cell, Cell] = CanEqual.derived

enum Rotation:
  case Horizontal
  case Vertical

extension (rotation: Rotation)

  def reverse: Rotation =
    rotation match
      case Rotation.Horizontal => Rotation.Vertical
      case Rotation.Vertical   => Rotation.Horizontal

given CanEqual[Rotation, Rotation] = CanEqual.derived

final case class Board(cells: ArraySeq[ArraySeq[Cell]])

object Board:
  def empty: Board = Board(ArraySeq.fill(10, 10)(Cell.Unknown))

final case class NavalCombatModel(board: Board)
