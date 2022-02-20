package com.lambdarat.navalcombat.scenes.result.viewmodel

import com.lambdarat.navalcombat.core.Cell

enum CombatResult derives CanEqual:
  case Win, Lose

final case class SideResult(
    destroyer: List[Cell],
    submarine: List[Cell],
    cruiser: List[Cell],
    battleship: List[Cell],
    carrier: List[Cell]
)

final case class ResultViewModel(
    result: CombatResult,
    player: SideResult,
    enemy: SideResult
)
