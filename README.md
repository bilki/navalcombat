Development tips
===

Hot reload development flow

1. Open sbt and run `~buildGame`
2. Spin up an http server with live reload. I use [livereload](https://github.com/lepture/python-livereload).
Launch this from the game source code folder:
```console
livereload -p 9000 -t target/indigoBuild/scripts target/indigoBuild
```
3. Open a browser, go to http://localhost:9000, open console/inspector, disable network cache
4. Change some code, save files, check browser

Random notes
---

I use [peek](https://github.com/phw/peek). Recording as `.mp4` requires converting it later to `.gif` using `ffmpeg`:

```console 
ffmpeg -i naval_combat.mp4 -vf "fps=24,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse" -loop -1 naval_combat.gif
```

Assets attributions
---

* Simple buttons: https://adwitr.itch.io/button-asset-pack
* Ponderosa font: https://www.1001fonts.com/ponderosa-font.html
