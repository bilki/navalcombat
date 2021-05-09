package com.lambdarat.navalcombat.scenes.placement

import com.lambdarat.navalcombat.core.given
import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.engine.BoardEngine.*
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

object Placement extends Scene[NavalCombatSetupData, NavalCombatModel, NavalCombatViewModel]:
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

  val movePlacementMsg = SignalReader[Point, Point](start => Signal.Lerp(start, Point(start.x, 20), Seconds(1)))

  def initialPlacementViewModel(setupData: NavalCombatSetupData): PlacementViewModel =
    val center = Point(setupData.width / 2, setupData.height / 2)

    val gridBounds = PlacementView.computeGridBounds(setupData)

    PlacementViewModel(
      screenSettings = ScreenSettings(Rectangle(0, 0, setupData.width, setupData.height), gridBounds),
      startTime = Seconds.zero,
      placeMsgSignal = Placement.movePlacementMsg.run(center),
      grid = QuadTree.empty[CellPosition](100, 100),
      highlightedCells = List.empty[CellPosition],
      sidebarShips = List.empty[SidebarShip],
      gridShips = List.empty[SidebarShip],
      dragging = None
    )

  def updateModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel
  ): GlobalEvent => Outcome[NavalCombatModel] = _ => Outcome(model)

  // Vertex normalized to match exactly the quad tree grid (x, y)
  extension (vertex: Vertex)

    def toExact: Vertex =
      val modX = vertex.x - (vertex.x % 10) + 5
      val modY = vertex.y - (vertex.y % 10) + 5

      Vertex(modX, modY)
  end extension

  // 100x100 grid is divided into 10x10 cells and center (5, 5), used to find them in quad tree
  extension (coord: Coord)

    def toGridVertex: Vertex =
      Vertex(coord.x.toInt * 10 + 5, coord.y.toInt * 10 + 5)
  end extension

  def updateViewModel(
      context: FrameContext[NavalCombatSetupData],
      model: NavalCombatModel,
      viewModel: PlacementViewModel
  ): GlobalEvent => Outcome[PlacementViewModel] =
    case PaintGrid =>
      val gridGraphics = PlacementView.computeGridGraphics(viewModel.screenSettings.gridBounds)
      val sidebarShips = PlacementView.computeSidebarShips(context.startUpData.width)

      val gridCoords =
        for
          i <- 0 until 10
          j <- 0 until 10
        yield Coord(XCoord(i), YCoord(j))

      val gridGraphicsWithCoord = gridGraphics.zip(gridCoords)

      val initialCellPositions = gridGraphicsWithCoord.map { case (gridPoint, coord) =>
        (
          CellPosition(model.board.get(coord.x, coord.y).get, coord, gridPoint, Highlight.Neutral),
          coord.toGridVertex
        )
      }

      val initialGrid = viewModel.grid.insertElements(initialCellPositions.toList)

      Outcome(viewModel.copy(startTime = context.running, grid = initialGrid, sidebarShips = sidebarShips))
    case FrameTick =>
      val nextPlacingShip = (context.mouse.mouseClicked, viewModel.dragging) match
        case (true, None) =>
          viewModel.sidebarShips.find { case SidebarShip(shipType, shipGraphic) =>
            context.mouse.wasMouseClickedWithin(shipGraphic.bounds.scaleBy(0.5, 0.5))
          }.map(sbs =>
            val draggedSidebarShip =
              sbs.copy(shipGraphic = sbs.shipGraphic.withScale(Vector2(1.0, 1.0)).centerAt(context.mouse.position))
            PlacingShip(draggedSidebarShip, Rotation.Horizontal)
          )
        case (false, Some(dragged)) =>
          val sbs = dragged.sidebarShip
          val newRotation =
            if context.keyboard.keysAreUp(Key.KEY_R) then dragged.rotation.reverse else dragged.rotation
          val draggedSidebarShip =
            sbs.copy(shipGraphic = sbs.shipGraphic.rotateTo(newRotation.angle).centerAt(context.mouse.position))
          Some(PlacingShip(draggedSidebarShip, newRotation))
        case (true, _: Some[PlacingShip]) => None
        case _                            => viewModel.dragging
      end nextPlacingShip

      val (nextGrid, highlighted) = nextPlacingShip match
        case None =>
          val resetGrid = viewModel.highlightedCells.foldLeft(viewModel.grid) { case (oldGrid, highlighted) =>
            val vertex = highlighted.position.toGridVertex
            oldGrid.insertElement(highlighted.copy(highlight = Highlight.Neutral), vertex)
          }

          (resetGrid, List.empty[CellPosition])
        case Some(dragged) =>
          val sbs          = dragged.sidebarShip
          val shipSize     = sbs.shipType.size
          val shipBounds   = sbs.shipGraphic.bounds
          val shipPosition = sbs.shipGraphic.position
          val shipCenter   = context.mouse.position

          val shipHolesPoints: List[Point] = dragged.rotation match
            case Rotation.Horizontal =>
              val holeSize = shipBounds.width / shipSize.toInt

              val firstHoleCenter = shipCenter.withX(shipCenter.x - shipBounds.width / 2 + holeSize / 2)
              val restOfHoleCenters = (1 until shipSize.toInt)
                .map(holeMultiplier => firstHoleCenter.withX(firstHoleCenter.x + holeSize * holeMultiplier))
                .toList

              firstHoleCenter :: restOfHoleCenters
            case Rotation.Vertical =>
              val holeSize = shipBounds.width / shipSize.toInt

              val firstHoleCenter = shipCenter.withY(shipCenter.y + shipBounds.width / 2 - holeSize / 2)
              val restOfHoleCenters = (1 until shipSize.toInt)
                .map(holeMultiplier => firstHoleCenter.withY(firstHoleCenter.y - holeSize * holeMultiplier))
                .toList

              firstHoleCenter :: restOfHoleCenters
          end shipHolesPoints

          val overlappingCells = shipHolesPoints
            .filter(viewModel.screenSettings.gridBounds.isPointWithin)
            .map { hole =>
              val normalizedVertex = hole
                .transform(viewModel.screenSettings.gridBounds)
                .toExact

              viewModel.grid.fetchElementAt(normalizedVertex).map(_.copy(highlight = Highlight.Green))
            }
            .flatten

          val resetGrid = viewModel.highlightedCells.foldLeft(viewModel.grid) { case (oldGrid, highlighted) =>
            val vertex = highlighted.position.toGridVertex
            oldGrid.insertElement(highlighted.copy(highlight = Highlight.Neutral), vertex)
          }

          val overlappedGrid = overlappingCells.foldLeft(resetGrid) { case (oldGrid, overlapping) =>
            val vertex = overlapping.position.toGridVertex
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
    Outcome(PlacementView.draw(context.running, viewModel, placementMessage))

case object PaintGrid extends GlobalEvent

given CanEqual[PaintGrid.type, GlobalEvent] = CanEqual.derived
