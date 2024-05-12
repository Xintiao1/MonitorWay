#!/bin/sh

servName=monitor-timer
APP_DIR=/opt/app/monitor-timer
APPLOG_DIR=$APP_DIR/logs
JAVA_OPS=" -Xms1024m -Xmx2048m -XX:ErrorFile=$APPLOG_DIR/hs_err_pid<pid>.log -Xloggc:$APPLOG_DIR/gc.log -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC "
JAVA_OPS=" $JAVA_OPS -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M"
Log="$APPLOG_DIR/startup_"$servName"_"`date +%Y%m%d`".log"
AGENT_PATH=$APP_DIR/lib/mwc.so

pid=`ps -ef|grep $servName|grep java |awk '{print $2}'`
if [[ "a" != "a${pid}" ]];then
    echo "exist pid:$pid"
    exit 0
fi

touch $APPLOG_DIR/pid
which java
nohup java -agentpath:$AGENT_PATH $JAVA_OPS -jar $APP_DIR/TimerTask-1.0-SNAPSHOT.jar --logging.config=classpath:logback-monitor.xml --CUSTOM-ENV=$APP_DIR/timer.properties >$Log 2>&1 &
sleep 5
pid=`ps -ef|grep $servName|grep java |awk '{print $2}'`
echo "$pid" > $APPLOG_DIR/pid
echo "" > $APPLOG_DIR/start_result.log
for((i=1;i<10;i++));do
    ret=`curl -I -m 60 -o /dev/null -s -w %{http_code} http://localhost:10091/login`
    echo "$ret" >> $APPLOG_DIR/start_result.log
    if [ "a200" = "a$ret" ];then
        echo "start finish!" >> $APPLOG_DIR/start_result.log
        break
    else
        sleep 20
    fi
done
