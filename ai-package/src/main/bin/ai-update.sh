#!/bin/bash
det_mirror_home=/usr/hdp/2.5.3.0-37/bigdata/mirror-web-api
echo "[`date +%Y-%m-%d' '%H:%M:%S`] 开始执行脚本" >> $det_mirror_home/update.log

echo "[`date +%Y-%m-%d' '%H:%M:%S`] 删除定时任务" >> $det_mirror_home/update.log
#更新前先删除定时启动任务
sed -i '/ai-cli.sh start/d' /var/spool/cron/root
#先停止应用
$det_mirror_home/bin/ai-cli.sh stop

lastVar=`ls $det_mirror_home/logsaas* 2>/dev/null`
if [ x$lastVar = x ]; then
    lastVar=`ls $det_mirror_home/lib/logsaas* 2>/dev/null`
fi
lastVar=${lastVar##*/}
#lastVerson=${lastVar:12:4}

date=`date +%Y%m%d`
if [ ! -d /data/backup ];then
    mkdir /data/backup
fi

#备份程序
mkdir -p /data/backup/$date
mkdir -p /data/backup/$date/mirror-web-api
echo "[`date +%Y-%m-%d' '%H:%M:%S`] 开始备份" >> $det_mirror_home/update.log
cp -rf $det_mirror_home/bin /data/backup/$date/mirror-web-api/
cp -rf $det_mirror_home/conf /data/backup/$date/mirror-web-api/
cp -rf $det_mirror_home/lib /data/backup/$date/mirror-web-api/
cp -rf $det_mirror_home/system /data/backup/$date/mirror-web-api/
echo "[`date +%Y-%m-%d' '%H:%M:%S`] 结束备份" >> $det_mirror_home/update.log
base1=$(cd `dirname $0`; pwd)
echo $base1 >> $det_mirror_home/update.log

#升级jar包
base=$(cd $base1/..; pwd)
echo $base >> $det_mirror_home/update.log
echo "[`date +%Y-%m-%d' '%H:%M:%S`] 删除lib/jar" >> $det_mirror_home/update.log
rm -rf $det_mirror_home/lib/*.jar
echo "[`date +%Y-%m-%d' '%H:%M:%S`] 删除完成" >> $det_mirror_home/update.log

echo "[`date +%Y-%m-%d' '%H:%M:%S`] 开始复制jar包" >> $det_mirror_home/update.log
cp  $base/lib/* $det_mirror_home/lib
echo "[`date +%Y-%m-%d' '%H:%M:%S`] 复制jar包完成" >> $det_mirror_home/update.log

cp -Rp -f $base/bin $det_mirror_home/

nowVar=`ls $base/lib/logsaas*`
nowVar=${nowVar##*/}
echo 'lastVerson:'$lastVar',nowVerson:'$nowVar  >> $det_mirror_home/update.log

rm -rf $det_mirror_home/system
echo "[`date +%Y-%m-%d' '%H:%M:%S`] 删除system完成" >> $det_mirror_home/update.log
\cp -rf $base/system $det_mirror_home
echo 'copy system/ to '$det_mirror_home/ >> $det_mirror_home/update.log
echo "[`date +%Y-%m-%d' '%H:%M:%S`] 复制system/完成" >> $det_mirror_home/update.log

PyPath=$det_mirror_home/python2.7
if [ -d "$PyPath" ]; then
 echo 'python installed'
else
 if [ ! -d '$base/py2_7' ]; then
    tar xvzf $base/py2_7/python2.7.tar.gz -C $det_mirror_home
    echo 'copy python2.7.tar.gz to '$det_mirror_home
    mv $det_mirror_home/usr/lib/python2.7 $det_mirror_home
    echo 'mv python2.7 to '$det_mirror_home
    rm -rf $det_mirror_home/usr
 else
    echo 'py2_7 is not exist!'
 fi
fi

if [ -z `$det_mirror_home/python2.7/bin/python $det_mirror_home/python2.7/bin/pip list --format columns|awk 'NR>2{print $1}'|grep "statsmodels"` ]; then
    unzip -o -d $det_mirror_home/python2.7/lib/python2.7/site-packages $det_mirror_home/system/ai_model_ailpha/config/statsmodels.zip
fi

if [ -z `command -v cpulimit` ]; then
    echo 'cpulimit does not exist, unzip its into '$det_mirror_home >> $det_mirror_home/update.log
    unzip -o -d $det_mirror_home $det_mirror_home/system/cpulimit-master.zip
    echo 'install cpulimit, and create symlink into /usr/bin/cpulimit' >> $det_mirror_home/update.log
    cd $det_mirror_home/cpulimit-master
    make
    echo 'install cpulimit over, cpulimit: '"$det_mirror_home/cpulimit-master/src/cpulimit" >> $det_mirror_home/update.log
    rm -rf /usr/bin/cpulimit
    ln -s $det_mirror_home/cpulimit-master/src/cpulimit /usr/bin/cpulimit
fi

rsync -avP --exclude-from=$base/bin/exclude.list $base/conf/ $det_mirror_home/conf >> $det_mirror_home/update.log
#处理conf配置目录
if [ ! -f "$det_mirror_home/conf/global.properties" ]; then
    \cp -rf $base/conf/global.properties $det_mirror_home/conf
    #同步配置文件
    if [ -f "$det_mirror_home/conf/gloal.properties" ]; then
        separator='='
        while read line
        do
            if [[ ! $line =~ $separator ]]
            then
                continue
            fi
            position=`awk -v str="$line" -v subStr="$separator" 'BEGIN{print index(str,subStr)}'`
            key=${line:0:$position}
            if [[ -z `cat $det_mirror_home/conf/global.properties | grep $key` ]]
            then
                continue
            fi
            sed -i "s#$key.*#$line#g" $det_mirror_home/conf/global.properties
        done < $det_mirror_home/conf/gloal.properties
        #移除旧配置
        rm -rf $det_mirror_home/conf/gloal.properties
    fi
    echo '配置修改完成'
fi

if [[ -z `cat $det_mirror_home/conf/application.properties | grep "feign.okhttp.enabled"` ]]; then
  echo -e "\nfeign.okhttp.enabled=true" >> $det_mirror_home/conf/application.properties
fi
if [[ -z `cat $det_mirror_home/conf/application.properties | grep "hystrix"` ]]; then
  echo -e "\nhystrix.command.default.circuitBreaker.enabled=false" >> $det_mirror_home/conf/application.properties
  echo -e "\nhystrix.command.default.execution.isolation.strategy=SEMAPHORE" >> $det_mirror_home/conf/application.properties
  echo -e "\nhystrix.command.default.execution.timeout.enabled=false" >> $det_mirror_home/conf/application.properties
fi

#赋予jar包权限
chmod 755 $det_mirror_home/lib/*.jar
echo "[`date +%Y-%m-%d' '%H:%M:%S`] 脚本执行完毕" >> $det_mirror_home/update.log

#启动应用
chmod +x $det_mirror_home/bin/ai-cli.sh
sh $det_mirror_home/bin/ai-cli.sh start
