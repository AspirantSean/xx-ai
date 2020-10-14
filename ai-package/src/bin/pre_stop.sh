#!/bin/bash

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

killAIProcess






