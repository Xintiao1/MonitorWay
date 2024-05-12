#!/bin/sh
ROOT_DIR=`pwd`

BUILD_DIR=$ROOT_DIR/build
WEB_DIR=$ROOT_DIR/monitorweb
NGINX_LOG_DIR=$ROOT_DIR/nginx-monitor
SCRIPT_DIR=$ROOT_DIR/script
BAK_DIR=$ROOT_DIR/backup
INIT_TAR=init-tar.gz
INIT_DIR=$ROOT_DIR/init-tar
KAFKA_SRC_DIR=kafka_2.13-3.4.0
KAFKA_DIR=kafka

KAFKA_MANAGER_SRC_DIR=kafka-manager-2.0.0.2
KAFKA_MANAGER_DIR=kafka-manager

mkdir -p $BUILD_DIR
mkdir -p $ROOT_DIR/new
mkdir -p $ROOT_DIR/data/excel
mkdir -p $ROOT_DIR/data/screen
mkdir -p $ROOT_DIR/data/files
mkdir -p $NGINX_LOG_DIR

cd $ROOT_DIR
source $ROOT_DIR/init.properties

if [ "download" = "$1" ];then
  wget http://sede0899.monitorway.net:26666/download/$INIT_TAR
fi

if [ ! -f "$INIT_TAR" ]; then
  echo "$INIT_TAR is not exist!"
  exit 0
fi

tar -xf $INIT_TAR
cp -r init-tar/* .
chmod -R 755 $WEB_DIR
ln -s $KAFKA_SRC_DIR $KAFKA_DIR
ln -s $KAFKA_MANAGER_SRC_DIR $KAFKA_MANAGER_DIR
sed -i "s/@host-port@/PLAINTEXT:\/\/$MW_HOST:$MW_KAFKA_PORT,PLAINTEXT1:\/\/localhost:9093/g" `grep @host-port@ -rl $KAFKA_DIR`

echo "-------copy shell----------"
cp -r $INIT_DIR/initScript/shell/script/*.sh $SCRIPT_DIR
cp -r $INIT_DIR/initScript/shell/bin $ROOT_DIR
cp -r $INIT_DIR/initScript/shell/monitor-system $ROOT_DIR
cp -r $INIT_DIR/initScript/shell/monitor-timer $ROOT_DIR
mkdir -p $ROOT_DIR/monitor-system/lib
mkdir -p $ROOT_DIR/monitor-timer/lib
cp -r $INIT_DIR/initScript/lib/centos/* $ROOT_DIR/monitor-system/lib
cp -r $INIT_DIR/initScript/lib/centos/* $ROOT_DIR/monitor-timer/lib

chmod 755 $SCRIPT_DIR/*.sh
chmod 755 $ROOT_DIR/bin/*.sh
chmod 755 $ROOT_DIR/monitor-system/*.sh
chmod 755 $ROOT_DIR/monitor-timer/*.sh

echo "-------copy nginx----------"
rm -rf $BUILD_DIR/nginx
cp -r $INIT_DIR/initScript/nginx $BUILD_DIR
SERVERS=$BUILD_DIR/nginx/conf/servers
sed -i "s!@MW_HOST@!$MW_HOST!g" `grep @MW_HOST@ -rl $SERVERS`
sed -i "s!@monitorweb@!$WEB_DIR!g" `grep @monitorweb@ -rl $SERVERS`
sed -i "s!@nginx-monitor@!$NGINX_LOG_DIR!g" `grep @nginx-monitor@ -rl $SERVERS`
cp -r $BUILD_DIR/nginx/conf/servers $MW_NGINX_DIR/conf
cp -r $BUILD_DIR/nginx/conf/certs $MW_NGINX_DIR/conf


echo "-----import table---------"
mysql -h$MW_DB_IP -u$MW_DB_USER -p$MW_DB_PASSWD -Dmonitor < $INIT_DIR/initScript/syncDB.sql;

:<<!
echo "-----truncate table---------"
which mysql
for file in `find $INIT_DIR/initScript/db -name "cleanTable.sql"`
do
  mysql -h$MW_DB_IP -u$MW_DB_USER -p$MW_DB_PASSWD -Dmonitor < $file;
done

echo "-----update table---------"
for file in `find $INIT_DIR/initScript/db -name "updateTable.sql"`
do
  mysql -h$MW_DB_IP -u$MW_DB_USER -p$MW_DB_PASSWD -Dmonitor < $file;
done
!



