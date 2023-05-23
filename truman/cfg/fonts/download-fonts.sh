#!/usr/bin/env bash
#
# Download the fonts
#
# @author D瓜哥 · https://www.diguage.com

declare -a fonts=('https://github.com/diguage/open-fonts/releases/download/latest/NotoEmoji-Regular.ttf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/SourceCodePro-Regular.otf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/SourceCodePro-Semibold.otf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/SourceCodePro-It.otf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/SourceCodePro-BoldIt.otf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/JetBrainsMono-Regular.ttf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/JetBrainsMono-SemiBold.ttf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/JetBrainsMono-Bold.ttf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/SourceHanSerifSC-Regular.otf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/SourceHanSerifSC-Bold.otf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/SourceHanSerifSC-Medium.otf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/SourceHanSerifSC-SemiBold.otf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/SourceHanSansSC-Regular.otf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/SourceHanSansSC-Bold.otf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/SourceHanSansSC-Medium.otf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/SourceHanSansSC-Heavy.otf' \
                  'https://github.com/diguage/open-fonts/releases/download/latest/Symbola.ttf')

for font in "${fonts[@]}"
do
  name=`echo $font | awk -F'/' '{print $NF}'`

  if [ -f "$name" ]; then
    echo "$name exists."
  else
    until wget -c -t 5 $font
    do
      echo "Try to download $name again..."
    done
  fi
done
