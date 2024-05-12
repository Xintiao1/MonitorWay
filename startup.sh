#!/bin/sh
servName=monitor-aggregator
APP_DIR=/opt/app/monitor-system
APPLOG_DIR=$APP_DIR/logs
JAVA_OPS=" -Xms1024m -Xmx2048m -XX:ErrorFile=$APPLOG_DIR/hs_err_pid<pid>.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$APPLOG_DIR -Xloggc:$APPLOG_DIR/gc.log -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintHeapAtGC "
JAVA_OPS=" $JAVA_OPS -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M"
AGENT_PATH=$APP_DIR/lib/mwc.so
Log="$APPLOG_DIR/startup_"$servName"_"`date +%Y%m%d`".log"
which java
java -agentpath:$AGENT_PATH $JAVA_OPS -jar $APP_DIR/monitor-aggregator-1.0-SNAPSHOT.jar --logging.config=classpath:logback-monitor.xml --CUSTOM-ENV=$APP_DIR/config.properties 
