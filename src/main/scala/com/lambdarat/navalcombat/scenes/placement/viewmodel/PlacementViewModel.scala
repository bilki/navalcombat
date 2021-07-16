package com.lambdarat.navalcombat.scenes.placement.viewmodel

import com.lambdarat.navalcombat.core.*

import indigo.*
import indigo.shared.materials.Material.Bitmap

enum Highlight derives CanEqual:
  case NotValid
  case Valid
  case Neutral

final case class SceneSettings(sceneBounds: Rectangle, gridBounds: Rectangle, modelSpace: Rectangle)

final case class Highlighted(position: Coord, highlight: Highlight)

final case class PlacingShip(ship: Ship, rotation: Rotation)

final case class SidebarShipGraphics(
    destroyer: Graphic[Bitmap],
    cruiser: Graphic[Bitmap],
    submarine: Graphic[Bitmap],
    battleship: Graphic[Bitmap],
    carrier: Graphic[Bitmap]
)

final case class PlacementViewModel(
    sceneSettings: SceneSettings,
    sidebarShipGraphics: SidebarShipGraphics,
    startTime: Seconds,
    placeMsgSignal: Signal[Point],
    highlightedCells: List[Highlighted],
    sidebarShips: List[Ship],
    dragging: Option[PlacingShip]
)
