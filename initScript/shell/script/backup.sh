#!/bin/sh
ROOT_DIR=/opt/app
source $ROOT_DIR/init.properties
APP_DIR=$ROOT_DIR/monitor-system
TIMER_DIR=$ROOT_DIR/monitor-timer
WEB_DIR=$ROOT_DIR/monitorweb
BACK_DIR=$ROOT_DIR/backup
DATE_DIR=`date  '+%Y-%m-%d'`
DATE_DIR=$BACK_DIR/$DATE_DIR

cd $BACK_DIR
mkdir -p $DATE_DIR
cp -r $APP_DIR $DATE_DIR
cp -r $TIMER_DIR $DATE_DIR
cp -r $WEB_DIR $DATE_DIR
cd $DATE_DIR/monitor-system/logs
rm -rf gc.log* *.sql *-*-* *.log
cd $DATE_DIR/monitor-timer/logs
rm -rf gc.log* *.sql *-*-* *.log

cd $DATE_DIR
mysqldump -h$MW_DB_IP -u$MW_DB_USER -p$MW_DB_PASSWD monitor > backupDB.sql;

