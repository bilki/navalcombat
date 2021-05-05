Some random notes
===

Convert screen record to GIF

ffmpeg -i naval_combat.mp4 -vf "fps=24,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse" -loop -1 naval_combat.gif
