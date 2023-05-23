#!/usr/bin/env bash
#
# 生成图片
#

# get base dir
baseDir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
echo "baseDir=${baseDir}"

## 声明一个数字变量，可以带引号
declare -a files=($(ls -l *.puml | awk '{print $9}'))

# 获取数组长度
arraylength=${#files[@]}
echo "arraylength=$arraylength"

# 遍历数组，获取下标以及各个元素
for (( i=1; i<${arraylength}+1; i++  ));
do
    echo $i  ": " ${files[$i-1]}
done

read -p "请跟上面的数字选择需要构建的简历 [1] : " index
index=${index:-1} # 默认值
echo "index=$index"

if [[ "$index" -gt "$arraylength"  || "$index" -lt "1" ]]; then
  echo "输入错误，请重新开始！"
  exit 0
fi

origin_file_name=${files[$index-1]}

echo "start to convert ${baseDir}/${origin_file_name}"
plantuml -tsvg \
         -SdefaultFontSize=18 \
         -StitleFontSize=36 \
         -StitleFontName='Source Han Sans SC' \
         -SheaderFontSize=20 \
         -SheaderFontName='Source Han Serif SC' \
         -SfooterFontSize=20 \
         -SfooterFontColor='#e22d30' \
         -SfooterFontName='Source Han Sans SC' \
         -SnoteFontName='Source Han Serif SC' \
         -SdefaultMonospacedFontName='JetBrains Mono' \
         -v \
         "${baseDir}/${origin_file_name}"
