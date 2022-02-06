package com.lambdarat.navalcombat.engine

import com.lambdarat.navalcombat.core.{Board, Ship}

import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.*

import indigo.Dice

class AutomatonEngineSpec extends ScalaCheckSuite:
  import AutomatonEngine.*

  private val genDice: Gen[Dice] =
    Gen.chooseNum(Long.MinValue, Long.MaxValue).map(Dice.arbitrary(1, Board.BOARD_SIZE, _))
  given arbBoard: Arbitrary[Board] = Arbitrary(genDice.map(AutomatonEngine.placeShips))

  property("any board generated from AI player should contain all ships") {
    forAll { (board: Board) =>
      assertEquals(board.ships.keySet.size, Ship.values.size)
    }
  }
