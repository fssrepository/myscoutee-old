#!/bin/sh

i=0;

for a in *.png; do printf ".${a%.png} .img\n \
{\n \
	background-position: $((-i % 8 * 5))vmax $((-i / 8 * 5))vmax\n \
}\n \
\n"; $((i+=1)); done > _icons.scss

FILE_NAMES="$(for b in *.png; do printf "$b "; done)"

montage $FILE_NAMES -background transparent -geometry +0+0 _icons.png

