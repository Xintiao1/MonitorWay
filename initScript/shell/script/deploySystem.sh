#!/bin/sh

VERSION=$1
ROOT_DIR=/opt/app
APP_DIR=$ROOT_DIR/monitor-system
TIMER_DIR=$ROOT_DIR/monitor-timer
APP_JAR=monitor-aggregator-1.0-SNAPSHOT.jar
cd $ROOT_DIR/new

echo "version:$VERSION"
rm -f $APP_JAR
if [ -n "$VERSION" ]; then
  wget http://sede0899.monitorway.net:26666/download/version/$VERSION/$APP_JAR
else
  wget http://sede0899.monitorway.net:26666/download/standard/$APP_JAR
fi

if [ ! -f "$APP_JAR" ]; then
  echo "$APP_JAR is not exist!"
  exit 0
fi

cp $APP_JAR $APP_DIR
cp $APP_JAR $TIMER_DIR/TimerTask-1.0-SNAPSHOT.jar
echo "-----restart system-------"
cd $APP_DIR
./shutdown.sh
./startup.sh

echo "-----restart timer-------"
cd $TIMER_DIR
./shutdown.sh
./startup.sh

