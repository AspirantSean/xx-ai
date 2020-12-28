#!/bin/bash

#进入bin的父目录
base=$(cd `dirname $0`/..; pwd)
ext_home=`cat base/conf/meta_info.data |jq -r '.deployPath'`
python_path=$ext_home/python2.7
if [ -d $python_path ]; then
    echo 'python installed'
else
    if [[ -f $base/conf/python2.7.tar.gz ]]; then
        tar -zxvf $base/conf/python2.7.tar.gz -C $ext_home
        echo 'decompression python2.7.tar.gz to '$ext_home
    else
        echo 'py2_7 is not exist!'
    fi
fi

if [ -z `$ext_home/python2.7/bin/python $ext_home/python2.7/bin/pip list --format columns | awk 'NR>2{print $1}' | grep "statsmodels"` ]; then
    unzip -o -d $ext_home/python2.7/lib/python2.7/site-packages $ext_home/conf/ai_model_ailpha/config/statsmodels.zip
fi

if [ -z `command -v cpulimit` ]; then
    echo 'cpulimit does not exist, unzip its into '$ext_home
    unzip -o -d $ext_home $ext_home/conf/cpulimit-master.zip
    echo 'install cpulimit, and create symlink into /usr/bin/cpulimit'
    cd $ext_home/cpulimit-master
    make
    echo 'install cpulimit over, cpulimit: '"$ext_home/cpulimit-master/src/cpulimit"
    rm -rf /usr/bin/cpulimit
    ln -s $ext_home/cpulimit-master/src/cpulimit /usr/bin/cpulimit
fi