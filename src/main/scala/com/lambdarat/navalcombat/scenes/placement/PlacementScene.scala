package com.lambdarat.navalcombat.scenes.placement

import com.lambdarat.navalcombat.core.given
import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.engine.BoardEngine.*
import com.lambdarat.navalcombat.scenes.placement.view.*
import com.lambdarat.navalcombat.scenes.placement.viewmodel.*
import com.lambdarat.navalcombat.assets.Assets
import com.lambdarat.navalcombat.utils.given
import com.lambdarat.navalcombat.utils.*

import indigo.*
import indigo.scenes.*
import indigo.shared.*
import indigo.shared.events.*
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigo.shared.temporal.*

import indigoextras.geometry.*
import indigoextras.subsystems.*
import indigoextras.trees.QuadTree

object PlacementScene extends Scene[NavalCombatSetupData, NavalCombatModel, NavalCombatViewModel]:
  def modelLens: Lens[NavalCombatModel, NavalCombatModel] = Lens.keepOriginal

  def viewModelLens: Lens[NavalCombatViewModel, PlacementViewModel] =
    Lens(_.placement, (ncvm, pvm) => ncvm.copy(placement = pvm))

  type SceneModel     = NavalCombatModel
  type SceneViewModel = PlacementViewModel

  def name: SceneName = SceneName("combat")

  def eventFilters: EventFilters = EventFilters.Permissive

  def subSystems: Set[SubSystem] = Set.empty

  val placementMessage = Text(
    "Placement Screen",
    Assets.ponderosaFontKey,
    Material.ImageEffects(Assets.ponderosaImgName)
  ).alignCenter

  def initialPlacementViewModel(setupData: NavalCombatSetupData): PlacementViewModel =
    val center = setupData.screenBounds.center

    val gridBounds = PlacementView.computeGridBounds(setupData)
    val modelSpace = Rectangle(0, 0, setupData.boardSize, setupData.boardSize)

    PlacementViewModel(
      sceneSettings = SceneSettings(setupData.screenBounds, gridBounds, modelSpace),
      startTime = Seconds.zero,
      placeMsgSignal = PlacementView.movePlacementMsg.run(center),
      grid = QuadTree.empty[CellPosition](setupData.boardSize, setupData.boardSize),
      highlightedCells = List.empty[CellPosition],
      sidebarShips = List.empty[SidebarShip],
      gridShips = List.empty[SidebarShip],
      dragging = None
    )

  def updateModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel
  ): GlobalEvent => Outcome[NavalCombatModel] = _ => Outcome(model)

  extension (coord: Coord)

    def toVertex: Vertex =
      Vertex(coord.x.toInt, coord.y.toInt)
  end extension

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: PlacementViewModel
  ): GlobalEvent => Outcome[PlacementViewModel] =
    case PaintGrid =>
      val gridBounds  = viewModel.sceneSettings.gridBounds
      val sceneBounds = viewModel.sceneSettings.sceneBounds

      val gridGraphics = PlacementView.computeGridGraphics(gridBounds)
      val sidebarShips = PlacementView.computeSidebarShips(sceneBounds, gridBounds)

      val boardSize = context.startUpData.boardSize

      val gridCoords =
        for
          i <- 0 until boardSize
          j <- 0 until boardSize
        yield Coord(XCoord(i), YCoord(j))

      val gridGraphicsWithCoord = gridGraphics.zip(gridCoords)

      val initialCellPositions = gridGraphicsWithCoord.map { (gridPoint, coord) =>
        (
          CellPosition(model.board.get(coord.x, coord.y).get, coord, gridPoint, Highlight.Neutral),
          coord.toVertex
        )
      }

      val initialGrid = viewModel.grid.insertElements(initialCellPositions.toList)

      Outcome(viewModel.copy(startTime = context.running, grid = initialGrid, sidebarShips = sidebarShips))
    case FrameTick =>
      val nextPlacingShip = (context.mouse.mouseClicked, viewModel.dragging) match
        case (true, None) =>
          viewModel.sidebarShips.find { case SidebarShip(shipType, shipGraphic) =>
            context.mouse.wasMouseClickedWithin(shipGraphic.bounds.scaleBy(0.5, 0.5))
          }.map(PlacingShip(_, Rotation.Horizontal))
        case (false, Some(dragged)) =>
          val sbs = dragged.sidebarShip
          val newRotation =
            if context.keyboard.keysAreUp(Key.KEY_R) then dragged.rotation.reverse else dragged.rotation
          Some(PlacingShip(sbs, newRotation))
        case (true, _: Some[PlacingShip]) => None
        case _                            => viewModel.dragging
      end nextPlacingShip

      val (nextGrid, highlighted) = nextPlacingShip match
        case None =>
          val resetGrid = viewModel.highlightedCells.foldLeft(viewModel.grid) { (oldGrid, highlighted) =>
            val vertex = highlighted.position.toVertex
            oldGrid.insertElement(highlighted.copy(highlight = Highlight.Neutral), vertex)
          }

          (resetGrid, List.empty[CellPosition])
        case Some(dragged) =>
          val sbs        = dragged.sidebarShip
          val shipSize   = sbs.shipType.size
          val shipBounds = sbs.shipGraphic.bounds
          val shipCenter = context.mouse.position
          val holeSize   = (shipBounds.width / shipSize.toInt) - 1

          val shipHolesPoints: List[Point] = dragged.rotation match
            case Rotation.Horizontal =>
              val firstHoleCenter = shipCenter.withX(shipCenter.x - shipBounds.width / 2 + holeSize / 2)
              val restOfHoleCenters = (1 until shipSize.toInt)
                .map(holeMultiplier => firstHoleCenter.withX(firstHoleCenter.x + holeSize * holeMultiplier))
                .toList

              firstHoleCenter :: restOfHoleCenters
            case Rotation.Vertical =>
              val firstHoleCenter = shipCenter.withY(shipCenter.y + shipBounds.width / 2 - holeSize / 2)
              val restOfHoleCenters = (1 until shipSize.toInt)
                .map(holeMultiplier => firstHoleCenter.withY(firstHoleCenter.y - holeSize * holeMultiplier))
                .toList

              firstHoleCenter :: restOfHoleCenters
          end shipHolesPoints

          val overlappingCells = shipHolesPoints
            .filter(viewModel.sceneSettings.gridBounds.isPointWithin)
            .map { hole =>
              val normalizedVertex = hole
                .transform(viewModel.sceneSettings.gridBounds, viewModel.sceneSettings.modelSpace)

              viewModel.grid.fetchElementAt(normalizedVertex).map(_.copy(highlight = Highlight.Valid))
            }
            .flatten

          val resetGrid = viewModel.highlightedCells.foldLeft(viewModel.grid) { (oldGrid, highlighted) =>
            val vertex = highlighted.position.toVertex
            oldGrid.insertElement(highlighted.copy(highlight = Highlight.Neutral), vertex)
          }

          val overlappedGrid = overlappingCells.foldLeft(resetGrid) { (oldGrid, overlapping) =>
            val vertex = overlapping.position.toVertex
            oldGrid.insertElement(overlapping, vertex)
          }

          (overlappedGrid, overlappingCells)
      end val

      Outcome(viewModel.copy(dragging = nextPlacingShip, grid = nextGrid, highlightedCells = highlighted))
    case _ =>
      Outcome(viewModel)

  def present(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: PlacementViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(PlacementView.draw(context.running, viewModel, placementMessage, context.mouse.position))

case object PaintGrid extends GlobalEvent

given CanEqual[PaintGrid.type, GlobalEvent] = CanEqual.derived
