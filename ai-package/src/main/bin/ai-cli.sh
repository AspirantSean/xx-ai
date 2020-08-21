#!/bin/bash

#setting locale evn
export LANG=zh_CN.UTF-8
export LC_ALL=zh_CN.UTF-8

NOHUP=${NOHUP:=$(which nohup)}

mainclass="com.dbapp.app.ai.AiApp"

setting_java_env() {
        if [ "x$JAVA_HOME" = "x" ]; then
                export JAVA_HOME=/usr/local/jdk
        fi
        export PATH=$JAVA_HOME/bin:$PATH
        export PATH=/sbin:$PATH
        export PATH=/usr/sbin:$PATH
        export PATH=/bin:$PATH

        #set jvm setting for heap size
        JAVA_OPTS="-Xms6G -Xmx6G -XX:MaxPermSize=256m -Djava.awt.headless=true "

        #set jvm setting: enable script(restart) when OOM
        #JAVA_OPTS="$JAVA_OPTS -XX:OnOutOfMemoryError=\"$PROFILE_HOME/bin/oom.sh\" "

        #set jvm setting: enable heap dump when OOM
        #JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data"

        #set jvm setting: compress ordinary object pointer, disable by default
        JAVA_OPTS="$JAVA_OPTS -XX:+UseCompressedOops "

        #set jvm setting: print gc
        JAVA_OPTS="$JAVA_OPTS -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps "

        #set jvm setting: enable jmx remote profiling, MUST change the java.rmi.server.hostname
        #JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.port=12345 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=127.0.0.1 "

        #JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.port=9832 -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.rmi.port=9832 -Djava.rmi.server.hostname=192.168.30.193 -Djgroups.bind_addr=172.18.0.7 -Djava.rmi.server.logCalls=true "

        #JAVA_OPTS="$JAVA_OPTS -Djava.util.logging.config.file=$PROFILE_HOME/conf/logging.properties"

        JAVA_OPTS="$JAVA_OPTS -Djava.io.tmpdir=/var/tmp"

        # TODO make sure java version is 1.6 or above
}

setting_mirror_evn() {
        cd `dirname $0`/.. 1>/dev/null 2>&1
        base=`pwd`
        export MIRROR_HOME=$base


        #test conf exist
        if [ ! -d $MIRROR_HOME/conf ]; then
                echo "[`date +%Y-%m-%d' '%H:%M:%S`] $MIRROR_HOME/conf not exist, exit"
                exit 1
        fi

        CLASSPATH=$MIRROR_HOME/conf
        LIB_DIR=$MIRROR_HOME/lib
        WEB_DIR=$MIRROR_HOME/web
        for i in `ls $LIB_DIR 2>/dev/null`; do
                CLASSPATH=$CLASSPATH:$LIB_DIR/$i:$WEB_DIR
        done
        export CLASSPATH
}

setting_mirror_evn

setting_java_env

#####change by zhenjie.wang#####
function is_file_contain_key() {
  file=$1
  key=$2
  is_contain=0
  while read line
  do
    if [[ $line =~ $key ]]; then
      is_contain=1
      break
    fi
  done < $file
  echo $is_contain
}

cronTask(){
    file=$2'cron'
    crontab -l > $file
    key="ai-cli.sh"
    is_contain=`is_file_contain_key $file "$key"`

    case $1 in
         add)
           if [ $is_contain = 0 ]; then
                echo "*/1 * * * * $2$key start >>/var/log/mirror_check.log" >> $file
           fi
         ;;
         del)
          if [ $is_contain != 0 ]; then
                 sed -i '/'"$key"'/d' $file
           fi
         ;;
         *)
           echo "nothing..."
         ;;
    esac
    crontab $file
    rm -rf $file
}

start() {
	origin_pid=`ps -ef | grep $mainclass | grep -v grep | awk '{print $2}'`

	if [ ! "x$origin_pid" == "x" ]; then
        echo $origin_pid > /var/run/web.pid
		echo "mirror process has already been running,quit"
		exit 1;
	fi
	
	cronTask 'add' $MIRROR_HOME'/bin/'

	jar_file=""
	for i in `ls $MIRROR_HOME/*.jar 2>/dev/null`; do
                #CLASSPATH=$CLASSPATH:$LIB_DIR/$i
		jar_file=$i
        done
	
	if [ ! -d $MIRROR_HOME/logs ]; then
         mkdir $MIRROR_HOME/logs
    fi

	#java $JAVA_OPTS -jar $jar_file 1>logs/console.log 2>&1 &
	java $JAVA_OPTS $mainclass 1>logs/console.log 2>&1 &
    	pid=$!

    echo $pid > /var/run/web.pid

	sleep 1
    flag=`ps ef |grep $pid |grep logsaas-web`
    if [ ! "x$flag" == "x" ]; then
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] start mirror-web-api(product env) finished, profile's pid: $pid"
                exit
    fi
}

killAIProcess() {
    if [ ! -f $MIRROR_HOME/system/aiProcess.record ]; then
        return 1
    fi
    separator='='
    while read line
    do
        sed -i "s#$line# #g" $MIRROR_HOME/system/aiProcess.record
        if [[ ! $line =~ $separator ]]
        then
            continue
        fi
        position=`awk -v str="$line" -v subStr="$separator" 'BEGIN{print index(str,subStr)}'`
        value=${line:$position}
        if [[ -z $value || $value =~ "-1" ]]
        then
            continue
        fi
        kill -9 $value
        echo "$line is killed"
    done < $MIRROR_HOME/system/aiProcess.record
    return 0
}

stop(){
	pid=`ps -ef | grep $mainclass|grep -v grep|awk '{print $2}'`
	kill -9 $pid
	echo "[`date +%Y-%m-%d' '%H:%M:%S`] stop mirror-web-api finished.pid:$pid"
	killAIProcess
	sleep 5
}



restart() {
        stop
        start
}



usage() {
        echo "Usage: `basename $0` (start|stop|restart|status) [-v|--verbose]"
}


#set default value
action=usage
debug=0
args=""

#process args
while [ $# -gt 0 ]; do
        case "$1" in
                start|stop|restart|status)
                        action=$1
                        ;;
                -v|--verbose)
                        verbose=1
                        ;;
                *)
                        args="$args $1"
        esac
        shift
done


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
        *)
                usage
                ;;
esac



