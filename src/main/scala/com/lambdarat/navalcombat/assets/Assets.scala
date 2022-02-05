package com.lambdarat.navalcombat.assets

import com.lambdarat.navalcombat.utils.*

import indigo.*
import indigoextras.ui.*

object Assets:
  private val baseUrl = "assets"

  val ponderosaImgName  = AssetName("ponderosa_image")
  val ponderosaImgAsset = AssetType.Image(ponderosaImgName, AssetPath(s"$baseUrl/ponderosa.png"))

  val ponderosaJsonName  = AssetName("ponderosa_json")
  val ponderosaJsonAsset = AssetType.Text(ponderosaJsonName, AssetPath(s"$baseUrl/ponderosa.json"))
  val ponderosaFontKey   = FontKey("ponderosa")

  val simpleButtonName = AssetName("simple_button")
  val simpleButtonImg  = AssetType.Image(simpleButtonName, AssetPath(s"$baseUrl/two_simple_buttons.png"))

  val cellsName = AssetName("cells")
  val cellsImg  = AssetType.Image(cellsName, AssetPath(s"$baseUrl/cells.png"))

  private val cellBitmap = Material.Bitmap(cellsName).toZeroGraphic

  val emptyCell  = cellBitmap.withCrop(0, 0, 64, 64).modifyMaterial(_.toImageEffects)
  val missCell   = cellBitmap.withCrop(0, 64, 64, 64).modifyMaterial(_.toImageEffects)
  val sunkCell   = cellBitmap.withCrop(64, 0, 64, 64).modifyMaterial(_.toImageEffects)
  val hitCell    = cellBitmap.withCrop(64, 64, 64, 64).modifyMaterial(_.toImageEffects)
  val cruiser    = cellBitmap.withCrop(128, 0, 192, 64).modifyMaterial(_.toImageEffects)
  val submarine  = cellBitmap.withCrop(128, 64, 192, 64).modifyMaterial(_.toImageEffects)
  val carrier    = cellBitmap.withCrop(0, 128, 320, 64).modifyMaterial(_.toImageEffects)
  val battleship = cellBitmap.withCrop(0, 192, 256, 64).modifyMaterial(_.toImageEffects)
  val destroyer  = cellBitmap.withCrop(0, 256, 128, 64).modifyMaterial(_.toImageEffects)

  val notValidEmptyCell = emptyCell.modifyMaterial(_.withOverlay(Fill.Color(RGBA.Yellow)))
  val validEmptyCell    = emptyCell.modifyMaterial(_.withOverlay(Fill.Color(RGBA.White)))

  val notValidCruiser    = cruiser.modifyMaterial(_.withOverlay(Fill.Color(RGBA.Yellow)))
  val notValidSubmarine  = submarine.modifyMaterial(_.withOverlay(Fill.Color(RGBA.Yellow)))
  val notValidCarrier    = carrier.modifyMaterial(_.withOverlay(Fill.Color(RGBA.Yellow)))
  val notValidBattleship = battleship.modifyMaterial(_.withOverlay(Fill.Color(RGBA.Yellow)))
  val notValidDestroyer  = destroyer.modifyMaterial(_.withOverlay(Fill.Color(RGBA.Yellow)))

  val simpleButtonGraphic =
    Graphic(34, 10, Material.Bitmap(simpleButtonName).toImageEffects.withSaturation(0))

  val simpleButtonAssets =
    ButtonAssets(
      up = simpleButtonGraphic.withCrop(0, 0, 34, 10).scaleBy(4, 4),
      over = simpleButtonGraphic.withCrop(0, 0, 34, 10).scaleBy(4, 4),
      down = simpleButtonGraphic.withCrop(0, 10, 34, 10).scaleBy(4, 4)
    )

  val assets: Set[AssetType] = Set(ponderosaImgAsset, ponderosaJsonAsset, simpleButtonImg, cellsImg)
