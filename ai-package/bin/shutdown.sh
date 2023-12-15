#!/bin/bash
#!/bin/bash

#setting locale evn
export LANG=zh_CN.UTF-8
export LC_ALL=zh_CN.UTF-8

NOHUP=${NOHUP:=$(command -v nohup)}

killAIProcess() {
    ##进入bin的上级目录
    cd `dirname $0`/.. 1>/dev/null 2>&1
    base=`pwd`
    if [ ! -f $base/conf/aiProcess.record ]; then
        return 1
    fi
    separator='='
    while read line
    do
        sed -i "s#$line# #g" $base/conf/aiProcess.record
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
    done < $base/conf/aiProcess.record
    return 0
}

#获取进程号
get_pid() {
    #进程ID
    pid=`ps -ef | grep $main_class | grep -v grep | awk '{print $2}'`
    echo $pid
}
#停止方法
stop() {
    pid=`get_pid`
    if [[ -n $pid ]]; then
        #停止前处理
        killAIProcess

        kill -9 $pid

        echo "[`date +%Y-%m-%d' '%H:%M:%S`] stop Ailpha Extension finished. pid:$pid"
    else
        echo "[`date +%Y-%m-%d' '%H:%M:%S`] Ailpha Extension has already finished"
    fi
}

main_class='com.dbapp.extension.ai.ExtApplication'


stop
