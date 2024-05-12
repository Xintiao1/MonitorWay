#!/bin/sh
servName=monitor-aggregator
APP_DIR=/opt/app/monitor-system
APPLOG_DIR=$APP_DIR/logs
JAVA_OPS=" -Xms2048m -Xmx4096m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+ParallelRefProcEnabled -XX:InitiatingHeapOccupancyPercent=75 -XX:ErrorFile=$APPLOG_DIR/hs_err_pid<pid>.log -Xloggc:$APPLOG_DIR/gc.log -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC "
JAVA_OPS=" $JAVA_OPS -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M"
AGENT_PATH=$APP_DIR/lib/mwc.so
Log="$APPLOG_DIR/startup_"$servName"_"`date +%Y%m%d`".log"
which java
nohup java -agentpath:$AGENT_PATH $JAVA_OPS -jar $APP_DIR/monitor-aggregator-1.0-SNAPSHOT.jar --logging.config=classpath:logback-monitor.xml --CUSTOM-ENV=$APP_DIR/config.properties >/dev/null 2>&1 1>/dev/null &
for((i=1;i<10;i++));do
    ret=`curl -I -m 60 -o /dev/null -s -w %{http_code} http://localhost:10081/login`
    echo "$ret"
    if [ "a200" = "a$ret" ];then
        echo "start finish!"
        break
    else
        sleep 20
    fi
done
