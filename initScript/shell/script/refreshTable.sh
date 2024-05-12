#!/bin/sh

VERSION=$1
ROOT_DIR=/opt/app
cd $ROOT_DIR/new
MW_SYNC_FILE=syncTable.sql

echo "version:$VERSION"
rm -f $MW_SYNC_FILE
if [ -n "$VERSION" ]; then
  wget http://sede0899.monitorway.net:26666/download/version/$VERSION/$MW_SYNC_FILE
else
  wget http://sede0899.monitorway.net:26666/download/standard/$MW_SYNC_FILE
fi

if [ ! -f "$MW_SYNC_FILE" ]; then
  echo "$MW_SYNC_FILE is not exist!"
  exit 0
fi

source $ROOT_DIR/bin/init.properties
mysql -u$MW_DB_USER -p$MW_DB_PASSWD -h$MW_DB_IP $MW_DB < $MW_SYNC_FILE