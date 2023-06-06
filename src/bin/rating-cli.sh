#!/bin/bash

#setting locale evn
export LANG=zh_CN.UTF-8
export LC_ALL=zh_CN.UTF-8

NOHUP=${NOHUP:=$(which nohup)}

#-------------程序方法实现--------------#

#java环境变量配置方法
setting_java_env() {
    if [[ -z "$JAVA_HOME" ]]; then
        export JAVA_HOME=/usr/local/jdk
    fi
    export PATH=$JAVA_HOME/bin:$PATH
    export PATH=/sbin:$PATH
    export PATH=/usr/sbin:$PATH
    export PATH=/bin:$PATH

    #set jvm setting for heap size
    JAVA_OPTS="-Xms1G -Xmx1G -Xss1m -Djava.awt.headless=true "

    #set jvm setting: enable script(restart) when OOM
    #JAVA_OPTS="$JAVA_OPTS -XX:OnOutOfMemoryError=\"$PROFILE_HOME/bin/oom.sh\" "

    #set jvm setting: enable heap dump when OOM
    #JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data"

    #set jvm setting: compress ordinary object pointer, disable by default
    JAVA_OPTS="$JAVA_OPTS -XX:+UseCompressedOops "

    #set jvm setting: print gc
    JAVA_OPTS="$JAVA_OPTS -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps "

    JAVA_OPTS="$JAVA_OPTS -Djava.io.tmpdir=/var/tmp"
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

    CLASSPATH=$EXT_HOME/conf
    for i in `ls $EXT_HOME/lib 2>/dev/null`; do
        CLASSPATH=$CLASSPATH:$EXT_HOME/lib/$i
    done

    export CLASSPATH
}

#启动方法
start() {
    origin_pid=`get_pid`

    #判断是否已启动
    if [[ -n "$origin_pid" ]]; then
        echo $origin_pid > /var/run/mirror_tools.pid
        echo "Ailpha extension mirror asset rating tool has started already"
        exit 1;
    fi

    #日志目录
    if [ ! -d /data/var/log/ai ]; then
        mkdir -p /data/var/log/tools
    fi
    if [ ! -L $EXT_HOME/logs ]; then
         ln -s /data/var/log/tools $EXT_HOME/logs
    fi

    #启动程序
    java $JAVA_OPTS $main_class $*
    pid=$!
    echo $pid > /var/run/mirror_tools.pid

    sleep 1
    flag=`ps -ef | grep $pid | grep $main_class`
    if [[ -n "$flag" ]]; then
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] start asset rating tool finished, profile's pid: $pid"
        exit 0
    else
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] failed to start asset rating tool"
        exit 1
    fi
}

#停止方法
stop() {
    pid=`get_pid`
    if [[ -n $pid ]]; then
        kill -9 $pid
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] stop asset rating tool finished. pid:$pid"
    else
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] asset rating tool has already finished"
    fi
}

#重启方法
restart() {
    stop
    start $*
}

#状态
status() {
    #ext
    medium_ext_name=${ext_jar#*ailpha-tools-asset-rating}
    ext_name=${medium_ext_name%%-*}
    #进程号
    pid=`get_pid`
    #程序状态
    status=null
    if [[ -n "$pid" ]]; then
        status=running
    else
        status="not started"
        pid=null
    fi
    #程序健康度
    health=red
    if [[ $status == "running" ]]; then
        health=green
    fi
    #输出结果
    printf "%-15s%-15s%-15s%-15s\n" "ext name" pid status health
    echo "---------------------------------------------------"
    printf "%-15s%-15s%-15s" "$ext_name" "$pid" "$status"
    if [[ $health == "red" ]]; then
        echo -e "\e[1;31m$health\e[0m"
    elif [[ $health == "green" ]]; then
        echo -e "\e[1;32m$health\e[0m"
    else
        echo $health
    fi
}

#用法
help() {
    echo "用法：`basename $0` (start|stop|restart|status) [-h|--help] [args...]"
    echo "包含的命令："
    echo "  start[ -ratingTimes=yyyy-MM-dd[ -assetIds={id1}[,{id2}[,{id3}...]][ -baasUrl={url}]]]"
    echo "                  开启资产评级。示例：-ratingTimes=2023-04-01,2023-04-02 -assetIds=asset_1,asset_2,asset_3 -baasUrl=http://1.flink1:8999 -save=false"
    echo "                      参数解释："
    echo "                          -ratingTimes    评级时间，需评级当天日期，可逗号分隔输入多天，会自动将评级起止时间定位到ratingTime的当天0时0分0秒-23时59分59秒"
    echo "                          -assetIds       待评级资产id，英文逗号分隔，不可有空格"
    echo "                          -baasUrl        baas服务地址"
    echo "                          -save           评级结果是否存入数据库"
    echo "  stop            停止资产评级"
    echo "  restart         重启评级工具"
    echo "  status          获取评级状态"
    echo "  -h|--help       帮助"
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
ext_jar=`ls $EXT_HOME/lib | grep -E "^ailpha-tools-asset-rating.*.jar$" | awk 'NR==1{print $1}'`
#main-class名称读取
main_class='com.tool.asset.application.Starter'

#默认动作
action=""

#action预处理
if [ $# -gt 0 ]; then
    case "$1" in
        start|stop|restart|status)
            action=$1
            ;;
        -v|--version)
            action=version
            ;;
        -h|--help)
            action=help
            ;;
        *)
            action=$1
            ;;
    esac
    shift
fi

#完成action
case "$action" in
    start)
        start $*
        ;;
    stop)
        stop
        ;;
    restart)
        restart $*
        ;;
    status)
        status
        ;;
    help)
        help
        ;;
    *)
        echo "没有此命令: "$action
        echo "使用 `basename $0` -h|--help 获取帮助信息"
        ;;
esac