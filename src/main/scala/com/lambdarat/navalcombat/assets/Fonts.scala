package com.lambdarat.navalcombat.assets

import indigo.*
import indigo.json.Json

object Fonts:

  def makeFontInfo(unknownChar: FontChar, fontChars: List[FontChar]): FontInfo =
    val font = FontInfo(
      fontKey = Assets.ponderosaFontKey,
      sheetWidth = 180,
      sheetHeight = 162,
      unknownChar = unknownChar,
      chars = fontChars*
    ).copy(caseSensitive = true)

    font.addChar(FontChar(" ", 180, 162, 13, 25))

  def buildFont(assetCollection: AssetCollection): Option[FontInfo] =
    for
      json        <- assetCollection.findTextDataByName(Assets.ponderosaJsonName)
      chars       <- Json.readFontToolJson(json)
      unknownChar <- chars.find(_.character == "#")
    yield makeFontInfo(unknownChar, chars)
