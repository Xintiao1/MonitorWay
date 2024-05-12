#!/bin/sh
ROOT_DIR=/opt/app
APP_DIR=$ROOT_DIR/monitor-security
WEB_DIR=$ROOT_DIR/security
BACK_DIR=$ROOT_DIR/bak
DATE_DIR=`date  '+%Y-%m-%d'`

cd $BACK_DIR
mkdir $DATE_DIR
cp -r $APP_DIR/monitor-security-aggregator-1.0-SNAPSHOT.jar $DATE_DIR
cp -r $WEB_DIR $DATE_DIR