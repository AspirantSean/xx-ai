#!/bin/bash

#进入bin的父目录
base=$(cd `dirname $0`/..; pwd)
app_home=/usr/hdp/2.5.3.0-37/bigdata/ailpha-app/ai
python_path=$app_home/python2.7
if [ -d $python_path ]; then
    echo 'python installed'
else
    if [[ -f $base/conf/python2.7.tar.gz ]]; then
        tar -zxvf $base/conf/python2.7.tar.gz -C $app_home
        echo 'decompression python2.7.tar.gz to '$app_home
    else
        echo 'py2_7 is not exist!'
    fi
fi

if [ -z `$app_home/python2.7/bin/python $app_home/python2.7/bin/pip list --format columns | awk 'NR>2{print $1}' | grep "statsmodels"` ]; then
    unzip -o -d $app_home/python2.7/lib/python2.7/site-packages $app_home/conf/ai_model_ailpha/config/statsmodels.zip
fi

if [ -z `command -v cpulimit` ]; then
    echo 'cpulimit does not exist, unzip its into '$app_home
    unzip -o -d $app_home $app_home/conf/cpulimit-master.zip
    echo 'install cpulimit, and create symlink into /usr/bin/cpulimit'
    cd $app_home/cpulimit-master
    make
    echo 'install cpulimit over, cpulimit: '"$app_home/cpulimit-master/src/cpulimit"
    rm -rf /usr/bin/cpulimit
    ln -s $app_home/cpulimit-master/src/cpulimit /usr/bin/cpulimit
fi