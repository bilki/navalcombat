package com.lambdarat.navalcombat.scenes.result

import com.lambdarat.navalcombat.assets.Assets
import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.core.Cell.Sunk
import com.lambdarat.navalcombat.core.Ship.*
import com.lambdarat.navalcombat.engine.BoardEngine.*
import com.lambdarat.navalcombat.scenes.result.view.ResultView
import com.lambdarat.navalcombat.scenes.result.viewmodel.ResultViewModel
import com.lambdarat.navalcombat.scenes.result.viewmodel.CombatResult.{Win, Lose}

import indigo.*
import indigo.scenes.*
import com.lambdarat.navalcombat.scenes.result.viewmodel.SideResult
import indigo.scenes.SceneEvent.SceneChange

object ResultScene extends Scene[NavalCombatSetupData, NavalCombatModel, NavalCombatViewModel]:
  def modelLens: Lens[NavalCombatModel, NavalCombatModel] = Lens.keepLatest

  def viewModelLens: Lens[NavalCombatViewModel, ResultViewModel] =
    Lens(_.result, (ncvm, rvm) => ncvm.copy(result = rvm))

  type SceneModel     = NavalCombatModel
  type SceneViewModel = ResultViewModel

  def name: SceneName = SceneName("result")

  def eventFilters: EventFilters = EventFilters.Permissive

  def subSystems: Set[SubSystem] = Set.empty

  private def shipResultFor(board: Board, ship: Ship): List[Cell] =
    if board.isCompletelySunk(ship) then
      (1 to ship.size.toInt)
        .zip(Section.values)
        .toList
        .map { case (_, section) => Sunk(ship, section) }
    else
      val maybeCells = for
        location <- board.ships.get(ship)
        coords = location.sections(ship)
      yield coords.reverse.foldLeft(List.empty[Cell]) { case (sectionsCells, nextCoord) =>
        board.get(nextCoord.x, nextCoord.y).fold(sectionsCells)(_ :: sectionsCells)
      }

      maybeCells.getOrElse(List.empty)

  private def sideResultFor(board: Board): SideResult =
    SideResult(
      destroyer = shipResultFor(board, Destroyer),
      submarine = shipResultFor(board, Submarine),
      cruiser = shipResultFor(board, Cruiser),
      battleship = shipResultFor(board, Battleship),
      carrier = shipResultFor(board, Carrier)
    )

  def resultViewModelFromBoards(model: NavalCombatModel): ResultViewModel =
    val combatResult = if model.player.isEndGame then Win else Lose
    val playerResult = sideResultFor(model.player)
    val enemyResult  = sideResultFor(model.enemy)

    ResultViewModel(
      combatResult,
      playerResult,
      enemyResult
    )

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: ResultViewModel
  ): GlobalEvent => Outcome[ResultViewModel] =
    case _: SceneChange =>
      Outcome(resultViewModelFromBoards(model))
    case _ =>
      Outcome(viewModel)

  def updateModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel
  ): GlobalEvent => Outcome[NavalCombatModel] = Function.const(Outcome(model))

  def present(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: ResultViewModel
  ): Outcome[SceneUpdateFragment] = Outcome(
    ResultView.draw(context, viewModel)
  )
