#!/usr/bin/env bash

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

export JAVA_OPTS

#进入bin的上级目录
cd `dirname $0`/.. 1>/dev/null 2>&1
base='/usr/hdp/20231103/app-ai'
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

if [ ! -d $EXT_HOME/logs ]; then
        mkdir $EXT_HOME/logs
fi

exec "$@"