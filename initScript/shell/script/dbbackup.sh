#!/bin/sh
ROOT_DIR=/opt/app
source $ROOT_DIR/bin/init.properties
BACK_DIR=$ROOT_DIR/backup
DATE_DIR=`date  '+%Y-%m-%d'`
DATE_DIR=$BACK_DIR/$DATE_DIR
cd $DATE_DIR
mysqldump -u$MW_DB_USER -p$MW_DB_PASSWD -h$MW_DB_IP --skip-lock-tables $MW_DB > backupDB.sql;