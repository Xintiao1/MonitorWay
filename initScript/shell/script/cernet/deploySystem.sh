#!/bin/sh
ROOT_DIR=/opt/app
APP_DIR=$ROOT_DIR/monitor-security
APP_NEW=$ROOT_DIR/new
cd $APP_NEW
rm -f monitor-security-aggregator-1.0-SNAPSHOT.jar
wget http://sede0899.monitorway.net:26666/download/cernet/monitor-security-aggregator-1.0-SNAPSHOT.jar
cd $APP_DIR
rm -f monitor-security-aggregator-1.0-SNAPSHOT.jar
cp $APP_NEW/monitor-security-aggregator-1.0-SNAPSHOT.jar $APP_DIR
./shutdown.sh
./startup.sh