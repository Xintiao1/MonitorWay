#!/bin/sh
APP=/opt/app/monitor-timer
APPLOG_DIR=$APP/logs
name=$(id |awk -F'[()]' '{print $2}')

servName=TimerTask
appuser=root
scriptName=`basename $0`

dirPath=`dirname $0`

cd $dirPath


Log="$APPLOG_DIR/shutdown_"$servName"_"`date +%Y%m%d`".log"


echo "--------------------The script of $0 is running.--------------------" > $Log
echo "----------------------------`date`-------------------------" >> $Log

pid=`cat $APPLOG_DIR/pid`
echo "pid : $pid" >> $Log
for((i=1;i<5;i++));do

        if [[ "a" != "a${pid}" ]]
        then
          if [ $i -eq 10 ]
                then
                        echo "The server of $servName whose pid is $pid is still running, will be killed  ." >> $Log
                        echo "ps -ef | grep $appuser[^0-9]*${pid} | awk {print kill,$2} | sh" >> $Log
                        ps -ef | grep "$appuser[^0-9]*${pid}" | awk '{print "kill -9", $2}' | sh
                        sleep 3
          fi
          if [ $i -lt 10 ]
                then
                        echo "The server of $servName whose pid is $pid will be shutdown for $i time." >> $Log
                        echo "ps -ef | grep $appuser[^0-9]*${pid} | awk {print kill,$2} | sh" >> $Log
                        ps -ef | grep "$appuser[^0-9]*${pid}" | awk '{print "kill",$2}' | sh
                        echo "Sleep 10 secs" >> $Log
                        sleep 10
          fi
        else
          echo "The $servName is not running!" >> $Log
        break
        fi
done
echo "delete $APPLOG_DIR/pid" >> $Log
echo "" > $APPLOG_DIR/pid
echo "The stopping of $servName is finished." >> $Log
echo "--------------------------------`date`-------------------------------" >> $Log
echo "--------------------The script of $0 is finished.--------------------" >> $Log
