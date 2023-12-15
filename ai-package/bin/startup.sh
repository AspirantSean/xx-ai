#!/bin/bash

#setting locale evn
export LANG=zh_CN.UTF-8
export LC_ALL=zh_CN.UTF-8

NOHUP=${NOHUP:=$(command -v nohup)}

#-------------程序方法实现--------------#

#java环境变量配置方法
setting_java_env() {
    export PATH=$JAVA_HOME/bin:$PATH
    export PATH=/sbin:$PATH
    export PATH=/usr/sbin:$PATH
    export PATH=/bin:$PATH

    #set jvm setting for heap size
    JAVA_OPTS="-Xms1G -Xmx1G -Xss1m -Djava.awt.headless=true "

    #set jvm setting: compress ordinary object pointer, disable by default
    JAVA_OPTS="$JAVA_OPTS -XX:+UseCompressedOops "

    #set jvm setting: print gc
    JAVA_OPTS="$JAVA_OPTS -Xlog:gc -Xlog:gc* "

    JAVA_OPTS="$JAVA_OPTS -Djava.io.tmpdir=/var/tmp"

    JAVA_OPTS="$JAVA_OPTS --add-exports=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED"


    # TODO make sure java version is 17 or above
}

#Extension环境变量配置方法
setting_ailpha_ext_evn() {
    #进入bin的上级目录
    cd `dirname $0`/.. 1>/dev/null 2>&1
    base=`pwd`
    export EXT_HOME=$base

    #test conf exist
    if [ ! -d $EXT_HOME/conf ]; then
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] $EXT_HOME/conf not exist, exit"
        exit 1
    fi

    CLASSPATH=$CLASSPATH:$EXT_HOME/conf
    for i in `ls $EXT_HOME/lib/*.jar 2>/dev/null`; do
        CLASSPATH=$CLASSPATH:$i
    done

    export CLASSPATH
}

#启动方法
start_d() {
    origin_pid=`get_pid`

    #判断是否已启动
    if [[ -n "$origin_pid" ]]; then
        echo "Ailpha Extension process has started already"
        exit 1;
    fi

    #日志目录
    if [ ! -d /data/var/log/ai ]; then
        mkdir -p /data/var/log/ai
    fi
    if [ ! -L $EXT_HOME/logs ]; then
         ln -s /data/var/log/ai $EXT_HOME/logs
    fi

    #启动程序
    java $JAVA_OPTS $main_class 1>$EXT_HOME/logs/console.log 2>&1 &
    pid=$!

    sleep 1
    flag=`ps -ef | grep $pid | grep $main_class`
    if [[ -n "$flag" ]]; then
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] start Ailpha Extension finished, profile's pid: $pid"
        exit 0
    else
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] failed to start Ailpha Extension"
        exit 1
    fi
}

#启动方法
start() {
    origin_pid=`get_pid`

    #判断是否已启动
    if [[ -n "$origin_pid" ]]; then
        echo "Ailpha Extension process has started already"
        exit 1;
    fi

    #日志目录
    if [ ! -d /data/var/log/ai ]; then
        mkdir -p /data/var/log/ai
    fi
    if [ ! -L $EXT_HOME/logs ]; then
         ln -s /data/var/log/ai $EXT_HOME/logs
    fi

    #启动程序
    java $JAVA_OPTS $main_class
}

#获取进程号
get_pid() {
    #进程ID
    pid=`ps -ef | grep $main_class | grep -v grep | awk '{print $2}'`
    echo $pid
}

#-------------程序启动逻辑--------------#

#设置Java环境变量
setting_java_env
#设置程序环境变量
setting_ailpha_ext_evn

#程序运行jar包
ext_jar=`ls $EXT_HOME/lib | grep -E "^ailpha-ext.*.jar$" | awk 'NR==1{print $1}'`
#main-class名称读取
main_class='com.dbapp.extension.ai.ExtApplication'
if [ "$1" == "-d" ]; then
  start_d
else
  start
fi
