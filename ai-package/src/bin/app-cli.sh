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

    # TODO make sure java version is 1.6 or above
}

#app环境变量配置方法
setting_ailpha_app_evn() {
    #进入bin的上级目录
    cd `dirname $0`/.. 1>/dev/null 2>&1
    base=`pwd`
    export APP_HOME=$base

    #test conf exist
    if [ ! -d $APP_HOME/conf ]; then
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] $APP_HOME/conf not exist, exit"
        exit 1
    fi

    CLASSPATH=$APP_HOME/conf:$APP_HOME/web
    for i in `ls $APP_HOME/lib 2>/dev/null`; do
        CLASSPATH=$CLASSPATH:$APP_HOME/lib/$i
    done

    export CLASSPATH
}

#启动方法
start() {
    origin_pid=`get_pid`

    #判断是否已启动
    if [[ -n "$origin_pid" ]]; then
        echo $origin_pid > /var/run/web.pid
        echo "Ailpha App process has started already"
        exit 1;
    fi

    #日志目录
    if [ ! -d $APP_HOME/logs ]; then
         mkdir $APP_HOME/logs
    fi

    #启动前处理
    pre_start

    #启动程序
    java $JAVA_OPTS $main_class 1>$APP_HOME/logs/console.log 2>&1 &
    pid=$!
    echo $pid > /var/run/web.pid

    sleep 1
    flag=`ps -ef | grep $pid | grep $main_class`
    if [[ -n "$flag" ]]; then
        #启动后处理
        post_start

        echo "[`date +%Y-%m-%d' '%H:%M:%S`] start Ailpha App finished, profile's pid: $pid"
        exit 0
    else
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] failed to start Ailpha App"
        exit 1
    fi
}

#启动前处理
pre_start() {
    pre_start_file=$APP_HOME/bin/pre_start.sh
    if [[ -f $pre_start_file ]]; then
        sh $pre_start_file
    fi
}

#启动后处理
post_start() {
    post_start_file=$APP_HOME/bin/post_start.sh
    if [[ -f $post_start_file ]]; then
        sh $post_start_file
    fi
}

#停止方法
stop() {
    pid=`get_pid`
    if [[ -n $pid ]]; then
        #停止前处理
        pre_stop

        kill -9 $pid

        #停止后处理
        post_stop
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] stop Ailpha App finished. pid:$pid"
    else
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] Ailpha App has already finished"
    fi
}

#启动前处理
pre_stop() {
    pre_stop_file=$APP_HOME/bin/pre_stop.sh
    if [[ -f $pre_stop_file ]]; then
        sh $pre_stop_file
    fi
}

#启动后处理
post_stop() {
    post_stop_file=$APP_HOME/bin/post_stop.sh
    if [[ -f $post_stop_file ]]; then
        sh $post_stop_file
    fi
}

#重启方法
restart() {
    stop
    start
}

#状态
status() {
    #app名称
    medium_app_name=${app_jar#*ailpha-app-}
    app_name=${medium_app_name%%-*}
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
    printf "%-15s%-15s%-15s%-15s\n" "app name" pid status health
    echo "---------------------------------------------------"
    printf "%-15s%-15s%-15s" "$app_name" "$pid" "$status"
    if [[ $health == "red" ]]; then
        echo -e "\e[1;31m$health\e[0m"
    elif [[ $health == "green" ]]; then
        echo -e "\e[1;32m$health\e[0m"
    else
        echo $health
    fi
}

#版本号
version() {
    app_name=${app_jar#*ailpha-app-}
    tag=${app_name#*-}
    version=${tag%%-*}
    echo $version
}

#用法
help() {
    echo "Usage: `basename $0` (start|stop|restart|status) [-v|--version|-h|--help]"
    echo "where options include:"
    echo "  start           start the app"
    echo "  stop            stop the app"
    echo "  restart         restart the app"
    echo "  status          get the app's status"
    echo "  -v|--version    print app version and exit"
    echo "  -h|--help       use help to obtain more information"
}

#获取进程号
get_pid() {
    #进程ID
    pid=`ps -ef | grep $main_class | grep -v grep | awk 'NR==1{print $2}'`
    echo $pid
}

#-------------程序启动逻辑--------------#

#设置Java环境变量
setting_java_env
#设置程序环境变量
setting_ailpha_app_evn

#程序运行jar包
app_jar=`ls $APP_HOME/lib | grep -E "ailpha-app.*.jar$" | awk 'NR==1{print $1}'`
#main-class名称读取
main_class=`cat $APP_HOME/conf/meta_info.data |jq -r '.mainClass'`

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
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    version)
        version
        ;;
    help)
        help
        ;;
    *)
        echo "no such command: "$action
        echo "use `basename $0` -h|--help to get more information"
        ;;
esac