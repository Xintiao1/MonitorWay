#!/bin/sh

VERSION=$1
ROOT_DIR=/opt/app
WEB_DIR=$ROOT_DIR/monitorweb/public
BASIC_DIR=$ROOT_DIR/monitorweb/mwapi/basics

WEB_TAR=dist.tar.gz
WEB_TAR_DIR=dist
BASIC_TAR=basics.tar.gz
BASIC_TAR_DIR=basics

echo "version:$VERSION"

cd $ROOT_DIR/new
rm -rf $WEB_TAR_DIR $WEB_TAR $BASIC_TAR_DIR $BASIC_TAR

if [ -n "$VERSION" ]; then
  wget http://sede0899.monitorway.net:26666/download/version/$VERSION/$WEB_TAR
  wget http://sede0899.monitorway.net:26666/download/version/$VERSION/$BASIC_TAR
else
  wget http://sede0899.monitorway.net:26666/download/standard/$WEB_TAR
  wget http://sede0899.monitorway.net:26666/download/standard/$BASIC_TAR
fi


if [ ! -f "$WEB_TAR" ]; then
  echo "$WEB_TAR is not exist!"
  exit 0
fi

if [ ! -f "$BASIC_TAR" ]; then
  echo "$BASIC_TAR is not exist!"
  exit 0
fi


tar -xf $WEB_TAR
cd $WEB_DIR
rm -rf *
cp -r $ROOT_DIR/new/dist/* .
chmod -R 755 $WEB_DIR

cd $ROOT_DIR/new
tar -xf $BASIC_TAR
mkdir -p $BASIC_DIR
cd $BASIC_DIR
rm -rf *
cp -r $ROOT_DIR/new/basics/* .
chmod -R 755 $BASIC_DIR
