package com.lambdarat.navalcombat.assets

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
  val cellsImg  = AssetType.Image(simpleButtonName, AssetPath(s"$baseUrl/two_simple_buttons.png"))

  val emptyCell  = Graphic(64, 64, Material.Bitmap(cellsName))
  val missCell   = Graphic(64, 64, Material.Bitmap(cellsName)).withCrop(0, 64, 64, 64)
  val sunkCell   = Graphic(64, 64, Material.Bitmap(cellsName)).withCrop(64, 0, 64, 64)
  val hitCell    = Graphic(64, 64, Material.Bitmap(cellsName)).withCrop(64, 64, 64, 64)
  val cruiser    = Graphic(192, 64, Material.Bitmap(cellsName)).withCrop(128, 0, 192, 64)
  val submarine  = Graphic(192, 64, Material.Bitmap(cellsName)).withCrop(128, 64, 192, 64)
  val carrier    = Graphic(320, 64, Material.Bitmap(cellsName)).withCrop(0, 128, 320, 64)
  val battleship = Graphic(256, 64, Material.Bitmap(cellsName)).withCrop(0, 192, 256, 64)
  val destroyer  = Graphic(128, 64, Material.Bitmap(cellsName)).withCrop(0, 256, 128, 64)

  val simpleButtonGraphic =
    Graphic(34, 10, Material.Bitmap(simpleButtonName).toImageEffects.withSaturation(0))

  val simpleButtonAssets =
    ButtonAssets(
      up = simpleButtonGraphic.withCrop(0, 0, 34, 10).scaleBy(4, 4),
      over = simpleButtonGraphic.withCrop(0, 0, 34, 10).scaleBy(4, 4),
      down = simpleButtonGraphic.withCrop(0, 10, 34, 10).scaleBy(4, 4)
    )

  val assets: Set[AssetType] = Set(ponderosaImgAsset, ponderosaJsonAsset, simpleButtonImg, cellsImg)
