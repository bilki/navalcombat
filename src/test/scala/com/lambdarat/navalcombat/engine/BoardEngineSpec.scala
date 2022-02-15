package com.lambdarat.navalcombat.engine

import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.*

import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.generators.ModelGen
import com.lambdarat.navalcombat.generators.ModelGen.validCoord
import com.lambdarat.navalcombat.generators.ModelGen.{
  given Arbitrary[Ship],
  given Arbitrary[Rotation],
  given Arbitrary[ShipOrientation]
}
import indigo.Dice

class BoardEngineSpec extends ScalaCheckSuite:
  import BoardEngine.*

  property("valid coords must return a cell for the empty board") {
    forAll { (coord: Coord) =>
      val expected = Some(Cell.Unknown)

      val cell = Board.empty.get(coord.x, coord.y)

      assertEquals(clue(cell), expected)
    }
  }

  property("negative X coords must return a non-defined cell for the empty board") {
    forAll(ModelGen.invalidXcoord.arbitrary, ModelGen.validYCoord.arbitrary) { (x, y) =>
      val expected = None

      val cell = Board.empty.get(x, y)

      assertEquals(clue(cell), expected)
    }
  }

  property("negative Y coords must return a non-defined cell for the empty board") {
    forAll(ModelGen.validXcoord.arbitrary, ModelGen.invalidYcoord.arbitrary) { (x, y) =>
      val expected = None

      val cell = Board.empty.get(x, y)

      assertEquals(clue(cell), expected)
    }
  }

  property("X coords bigger than board size must return a non-defined cell for the empty board") {
    val tooBigXCoord = Gen.chooseNum(Board.BOARD_SIZE, Int.MaxValue).map(XCoord(_))

    forAll(tooBigXCoord, ModelGen.validYCoord.arbitrary) { (x, y) =>
      val expected = None

      val cell = Board.empty.get(x, y)

      assertEquals(clue(cell), expected)
    }
  }

  property("Y coords bigger than board size must return a non-defined cell for the empty board") {
    val tooBigYCoord = Gen.chooseNum(Board.BOARD_SIZE, Int.MaxValue).map(YCoord(_))

    forAll(ModelGen.validXcoord.arbitrary, tooBigYCoord) { (x, y) =>
      val expected = None

      val cell = Board.empty.get(x, y)

      assertEquals(clue(cell), expected)
    }
  }

  property("sections for ship orientation contain the correct number of coords") {
    forAll { (shipOrientation: ShipOrientation, ship: Ship) =>
      val expectedNumberOfCoords = clue(ship).size.toInt

      val sections = shipOrientation.sections(ship)

      assertEquals(clue(sections).size, expectedNumberOfCoords)
    }
  }

  property("placing a ship into a valid position returns the updated board") {
    val validPlaceCoordGen = ModelGen.validPlacement(Board.empty)

    forAll(validPlaceCoordGen.arbitrary) { (ship: Ship, coord: Coord, rotation: Rotation) =>
      val board = Board.empty

      val updatedBoard    = board.place(ship, rotation, coord.x, coord.y)
      val shipOrientation = ShipOrientation(coord, rotation)

      val coords = shipOrientation.sections(ship)
      val cells  = coords.flatMap(coord => updatedBoard.map(_.get(coord.x, coord.y))).flatten

      val restOfBoard =
        for
          x <- (0 until Board.BOARD_SIZE).map(XCoord(_))
          y <- (0 until Board.BOARD_SIZE).map(YCoord(_))
          coord = Coord(x, y)
          if !coords.contains(coord)
        yield updatedBoard.flatMap(_.get(coord.x, coord.y))
      val expectedRestOfCells = Cell.Unknown

      val expectedCellsNumber = clue(ship).size.toInt
      val expectedCell        = Cell.Floating(ship)
      val expectedPlaced      = Map(ship -> ShipOrientation(coord, rotation))

      assertEquals(clue(cells).size, expectedCellsNumber)
      assert(clue(cells).forall(_ == expectedCell))
      assert(restOfBoard.flatten.forall(_ == expectedRestOfCells))
      assertEquals(clue(updatedBoard.map(_.ships)), Some(expectedPlaced))
    }
  }

  property("placing a ship into an invalid position returns empty") {
    val validPlaceCoordGen = ModelGen.invalidPlacement(Board.empty)

    forAll(validPlaceCoordGen.arbitrary) { (ship: Ship, coord: Coord, rotation: Rotation) =>
      val board = Board.empty

      val updatedBoard = board.place(ship, rotation, coord.x, coord.y)

      assert(clue(updatedBoard).isEmpty)
    }
  }

  property("cannot place two or more times the same ship") {
    val validPlaceCoordGen = ModelGen.validPlacement(Board.empty)

    forAll(validPlaceCoordGen.arbitrary, Gen.chooseNum(2, 10)) {
      case ((ship: Ship, coord: Coord, rotation: Rotation), times: Int) =>
        val placedFirst = Board.empty.place(ship, rotation, coord.x, coord.y)

        val updatedBoard = (1 until times).foldLeft(placedFirst) { (acc, _) =>
          for
            previous                        <- acc
            (toPlaceCoord, toPlaceRotation) <- ModelGen.validPlacementFor(ship, previous).arbitrary.sample
            updated                         <- previous.place(ship, toPlaceRotation, toPlaceCoord.x, toPlaceCoord.y)
          yield updated
        }

        assert(clue(updatedBoard).isEmpty)
    }
  }

  property("cannot place a ship overlapping another one") {
    val overlappingGen =
      for
        (ship, coord, rotation) <- ModelGen.validPlacement(Board.empty).arbitrary
        placed = Board.empty.place(ship, rotation, coord.x, coord.y)
        overlapping <- placed.fold(Gen.fail)(ModelGen.overlapping(_).arbitrary)
      yield (placed, overlapping, ship)

    forAll(overlappingGen) {
      case (maybeBoard: Option[Board], (ship: Ship, coord: Coord, rotation: Rotation), previous: Ship) =>
        val failedUpdate = maybeBoard.flatMap(_.place(ship, rotation, coord.x, coord.y))

        val expected = Cell.Floating(previous)

        val overlapped = maybeBoard.flatMap(_.get(coord.x, coord.y))

        assert(clue(failedUpdate).isEmpty)
        assertEquals(clue(overlapped), Some(expected))
    }
  }

  property("shooting to all sections of a ship sinks that ship and it is removed from board") {
    val randomBoard = Gen.long.map(seed => AutomatonEngine.placeShips(Dice.fromSeed(seed)))
    val randomShip  = Gen.oneOf(Ship.values)

    forAll(randomBoard, randomShip) { (board: Board, ship: Ship) =>
      val maybeUpdatedBoard =
        for
          sections <- board.ships.get(ship).map(_.sections(ship))
          updatedBoard <- sections.foldLeft(Option(board)) { case (prevBoard, nextSection) =>
            prevBoard.flatMap(_.shoot(nextSection.x, nextSection.y))
          }
        yield updatedBoard

      assertEquals(maybeUpdatedBoard.map(_.ships), Some(board.ships - ship))
      assertEquals(maybeUpdatedBoard.map(_.isCompletelySunk(ship)), Some(true))
    }
  }

  property("shooting to all sections but one of a ship does not sink it") {
    val randomBoard = Gen.long.map(seed => AutomatonEngine.placeShips(Dice.fromSeed(seed)))
    val randomShip  = Gen.oneOf(Ship.values)

    forAll(randomBoard, randomShip) { (board: Board, ship: Ship) =>
      val maybeUpdatedBoard =
        for
          sections <- board.ships.get(ship).map(_.sections(ship).drop(1))
          updatedBoard <- sections.foldLeft(Option(board)) { case (prevBoard, nextSection) =>
            prevBoard.flatMap(_.shoot(nextSection.x, nextSection.y))
          }
        yield updatedBoard

      assertEquals(maybeUpdatedBoard.map(_.ships), Some(board.ships))
      assertEquals(maybeUpdatedBoard.map(_.isCompletelySunk(ship)), Some(false))
    }
  }

  property("shooting and sinking all ships ends the game") {
    val randomBoard = Gen.long.map(seed => AutomatonEngine.placeShips(Dice.fromSeed(seed)))

    forAll(randomBoard) { board =>
      val maybeUpdatedBoard =
        board.ships.foldLeft(Option(board)) { case (previousBoard, (ship, location)) =>
          val sections = location.sections(ship)

          sections.foldLeft(previousBoard) { case (sectionsBoard, nextSection) =>
            sectionsBoard.flatMap(_.shoot(nextSection.x, nextSection.y))
          }
        }

      assertEquals(maybeUpdatedBoard.map(_.ships), Some(Map.empty))
      assertEquals(maybeUpdatedBoard.map(_.isEndGame), Some(true))
    }
  }

  property("shooting and sinking all ships but one does not end the game") {
    val randomBoard = Gen.long.map(seed => AutomatonEngine.placeShips(Dice.fromSeed(seed)))
    val randomShip  = Gen.oneOf(Ship.values)

    forAll(randomBoard, randomShip) { (board: Board, lucky: Ship) =>
      val allButOneShip = board.ships - lucky

      val maybeUpdatedBoard =
        allButOneShip.foldLeft(Option(board)) { case (previousBoard, (ship, location)) =>
          val sections = location.sections(ship)

          sections.foldLeft(previousBoard) { case (sectionsBoard, nextSection) =>
            sectionsBoard.flatMap(_.shoot(nextSection.x, nextSection.y))
          }
        }

      assertEquals(
        maybeUpdatedBoard.map(_.ships),
        board.ships.get(lucky).map(luckyLocation => Map(lucky -> luckyLocation))
      )
      assertEquals(maybeUpdatedBoard.map(_.isEndGame), Some(false))
    }
  }
