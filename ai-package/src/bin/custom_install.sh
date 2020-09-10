#!/bin/bash

base1=$(cd `dirname $0`; pwd)
base=$(cd $base1/..; pwd)
app_home=/usr/hdp/2.5.3.0-37/bigdata/ailpha-app
PyPath=$app_home/python2.7
if [ -d "$PyPath" ]; then
 echo 'python installed'
else
 if [ ! -d '$base/py2_7' ]; then
    tar xvzf $base/py2_7/python2.7.tar.gz -C $app_home
    echo 'copy python2.7.tar.gz to '$app_home
    mv $app_home/usr/lib/python2.7 $app_home
    echo 'mv python2.7 to '$app_home
    rm -rf $app_home/usr
 else
    echo 'py2_7 is not exist!'
 fi
fi

if [ -z `$app_home/python2.7/bin/python $app_home/python2.7/bin/pip list --format columns|awk 'NR>2{print $1}'|grep "statsmodels"` ]; then
    unzip -o -d $app_home/python2.7/lib/python2.7/site-packages $app_home/conf/ai_model_ailpha/config/statsmodels.zip
fi

if [ -z `command -v cpulimit` ]; then
    echo 'cpulimit does not exist, unzip its into '$app_home >> $app_home/update.log
    unzip -o -d $app_home $app_home/conf/cpulimit-master.zip
    echo 'install cpulimit, and create symlink into /usr/bin/cpulimit' >> $app_home/update.log
    cd $app_home/cpulimit-master
    make
    echo 'install cpulimit over, cpulimit: '"$app_home/cpulimit-master/src/cpulimit" >> $app_home/update.log
    rm -rf /usr/bin/cpulimit
    ln -s $app_home/cpulimit-master/src/cpulimit /usr/bin/cpulimit
fi