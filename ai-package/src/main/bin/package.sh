#!/bin/bash

mirror_home=/download/release_download/bdweb-V1.3.1
ailpha_home=/download/ailpha
bdsp_home=/download/bdsp
zip_passwd=dbapp_ailpha@2017


function help(){
    echo "==================必须输入参数1：打包方式；参数2：mirror 地址flag 参数3：bdsp地址flag================"
    echo ""
    echo "===========================                  参数1选择           ===================================="
    echo "===========================          0:全部，包含mirror包与bdsp包         ==========================="
    echo "===========================          1:只打mirror包，不包含bdsp包         ==========================="
    echo "===========================          2:只打bdsp包，不包含mirror包         ==========================="
    echo ""
    echo "===========================               参数2 flag选择                  ==========================="
    echo "===========================          1:bdweb dev分支                      ==========================="
    echo "===========================          2:bdweb V1.3.1分支                   ==========================="
    echo ""
    echo "===========================               参数3 flag选择                  ==========================="
    echo "===========================          1:bdsp 1.3分支                       ==========================="
    exit 1
}

if [ "$1x" == "-helpx" ];then
    help
fi

if [ "$#" -lt 3 ];then
     echo "[`date +%Y-%m-%d' '%H:%M:%S`] must input params : 1.package pattern 2. mirror address flag 3 bdsp address flag"
     echo "[`date +%Y-%m-%d' '%H:%M:%S`] you can use -help for help"
     exit
fi

function getMirrorHome(){
    bd_home=

    case $1 in
        1)
        bd_home=/download/release_download/bigdata_web_new
        ;;
        2)
        bd_home=/download/release_download/bdweb-V1.3.1
        ;;
        *)
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] mirror flag must be in 1,2 not exist, exit"
        exit 1
    esac

    echo $bd_home
}

function getBdspHome(){
    bdspHome=

    case $1 in
        1)
        bdspHome=/download/bdsp/V2.0
        ;;
        *)
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] bdsp flag must be in 1 not exist, exit"
        exit 1
    esac

    echo $bdspHome
}

mirror_home=`getMirrorHome $2`
mirror_file=`ls -t $mirror_home | head -1`
echo "[`date +%Y-%m-%d' '%H:%M:%S`] mirror_home is $mirror_home, file is $mirror_file"

bdsp_home=`getBdspHome $3`
bdsp_file=`ls -t $bdsp_home | head -1`
echo "[`date +%Y-%m-%d' '%H:%M:%S`] bdsp_home is $bdsp_home,file is $bdsp_file"

if [ ! -d $ailpha_home/tmp ];then
    mkdir -p $ailpha_home/tmp
    echo "[`date +%Y-%m-%d' '%H:%M:%S`] $ailpha_home/tmp not exist, create "
fi

arr_mirror=(${mirror_file//-/ })
arr_bdsp=(${bdsp_file//-/ })
version=${arr_mirror[2]}

git_mirror_version=${arr_mirror[3]}
git_bdsp_version=${arr_bdsp[2]}
echo "[`date +%Y-%m-%d' '%H:%M:%S`] bdweb_mirror version : $version, mirror git v:$git_mirror_version, bdsp git v: $git_bdsp_version"

suffix=
if [ $1 == 1 ];then
    cp $mirror_home/$mirror_file $ailpha_home/tmp
    suffix="mirror_v"$git_mirror_version
elif [ $1 == 2 ];then
    cp $bdsp_home/$bdsp_file $ailpha_home/tmp
    suffix="bdsp_v"$git_bdsp_version
else
    cp $mirror_home/$mirror_file $ailpha_home/tmp
    cp $bdsp_home/$bdsp_file $ailpha_home/tmp
    suffix="mirror_v"$git_mirror_version"_bdsp_v"$git_bdsp_version"-all"
fi

cd $ailpha_home/tmp
if [ -e $ailpha_home/tmp/$mirror_file ];then
    unzip -P $zip_passwd $mirror_file
    rm -rf $mirror_file
fi


if [ ! -d $ailpha_home/$version ];then
    mkdir -p $ailpha_home/$version
    echo "[`date +%Y-%m-%d' '%H:%M:%S`] $ailpha_home/$version not exist, create "
fi

packageTime=`date "+%Y%m%d%H%M"`
ptime=${packageTime:2}
finalName="bigdata-web-"$version"-"$suffix"-"$ptime.zip
echo "[`date +%Y-%m-%d' '%H:%M:%S`] begin to zip file  $finalName in $ailpha_home/$version"
zip -P $zip_passwd  $ailpha_home/$version/$finalName *

rm -rf $ailpha_home/tmp/*

echo "[`date +%Y-%m-%d' '%H:%M:%S`] package finished final package is $finalName!!!"