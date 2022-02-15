package com.lambdarat.navalcombat.engine

import com.lambdarat.navalcombat.core.{Board, Ship}
import com.lambdarat.navalcombat.core.Cell.*

import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.*

import indigo.Dice

class AutomatonEngineSpec extends ScalaCheckSuite:
  import AutomatonEngine.*
  import BoardEngine.*

  given genDice: Gen[Dice] =
    Gen.chooseNum(Long.MinValue, Long.MaxValue).map(Dice.diceSidesN(Board.BOARD_SIZE, _))
  given arbBoard: Arbitrary[Board] = Arbitrary(genDice.map(AutomatonEngine.placeShips))

  property("any board generated from AI player should contain all ships") {
    forAll { (board: Board) =>
      assertEquals(clue(board.ships.keySet), Ship.values.toSet)
    }
  }

  property("AI player should always shoot a random unknown or floating cell") {
    forAll(arbitrary[Board], genDice) { (board: Board, dice: Dice) =>
      val initialMissesAndSunk = board.cells.flatten.count {
        case Miss | Sunk(_) => true
        case anyOther       => false
      }

      val shot1           = AutomatonEngine.nextShot(dice, board)
      val shot2           = AutomatonEngine.nextShot(dice, shot1)
      val afterShotsBoard = AutomatonEngine.nextShot(dice, shot2)

      val countMissesAndSunk = afterShotsBoard.cells.flatten.count {
        case Miss | Sunk(_) => true
        case anyOther       => false
      }

      assertEquals(initialMissesAndSunk, 0)
      assertEquals(countMissesAndSunk, 3)
    }
  }
