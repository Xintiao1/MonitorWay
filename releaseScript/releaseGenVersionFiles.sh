#!/bin/sh

VERSION=`cat $1/version/1.VersionManage/ReleaseVersion.txt`
DOWNLOAD_DIR=$1
VERSION_DIR=$DOWNLOAD_DIR/version
CURRENT_VERSION=$VERSION_DIR/$VERSION

if [ -d "/$CURRENT_VERSION" ]; then
  echo "$VERSION_DIR/$VERSION is exist!"
  exit 0
fi

cd $VERSION_DIR
mkdir $VERSION
cp $DOWNLOAD_DIR/init-tar.gz $CURRENT_VERSION
cp -r $DOWNLOAD_DIR/standard/* $CURRENT_VERSION

