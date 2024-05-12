#!/bin/sh
APP=/opt/app/monitor-system
APPLOG_DIR=$APP/logs
name=$(id |awk -F'[()]' '{print $2}')

servName=monitor-aggregator
appuser=root
scriptName=`basename $0`

dirPath=`dirname $0`

cd $dirPath


Log="$APPLOG_DIR/shutdown_"$servName"_"`date +%Y%m%d`".log"


echo -e "--------------------The script of $0 is running.--------------------" >> $Log
echo -e "----------------------------`date`-------------------------\n" >> $Log

pid=`cat $APPLOG_DIR/pid`
echo "pid : $pid" >> $Log
for((i=1;i<5;i++));do

        pid=`ps -ef|grep $servName|grep java |grep $appuser |awk '{print $2}'`
        if [[ "a" != "a${pid}" ]]
        then
          if [ $i -eq 4 ]
                then
                        echo -e "The server of $servName whose pid is $pid is still running, will be killed  ." >> $Log
                        echo -e "ps -ef | grep "$appuser[^0-9]*${pid}" | awk '{print "kill -9", $2}' | sh\n" >> $Log
                        ps -ef | grep "$appuser[^0-9]*${pid}" | awk '{print "kill -9", $2}' | sh
                        sleep 3
          fi
          if [ $i -lt 4 ]
                then
                        echo -e "The server of $servName whose pid is $pid will be shutdown for $i time." >> $Log
                        echo -e "ps -ef | grep $appuser[^0-9]*${pid} | awk {print kill,$2} | sh\n" >> $Log
                        ps -ef | grep "$appuser[^0-9]*${pid}" | awk '{print "kill",$2}' | sh
                        echo -e "Sleep 10 secs\n" >> $Log
                        sleep 10
          fi
        else
        echo -e "The $servName is not running!\n" >> $Log
        break
        fi
done

echo -e "The stopping of $servName is finished.\n" >> $Log
echo -e "--------------------------------`date`-------------------------------" >> $Log
echo -e "--------------------The script of $0 is finished.--------------------\n\n" >> $Log
