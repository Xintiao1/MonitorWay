#!/bin/sh
ROOT_DIR=/opt/app
APP_DIR=$ROOT_DIR/security
APP_NEW=$ROOT_DIR/new
cd $APP_NEW
rm -rf dist dist.tar.gz
wget http://sede0899.monitorway.net:26666/download/cernet/dist.tar.gz
tar -xf dist.tar.gz
cd $APP_DIR
rm -rf *
cp -r  $APP_NEW/dist/* $APP_DIR
chmod -R 775 $APP_DIR