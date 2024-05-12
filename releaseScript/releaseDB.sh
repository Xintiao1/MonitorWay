#!/bin/sh

CUR_DIR=`pwd`
source $CUR_DIR/release.properties
rm -f syncDB_temp.sql
rm -f syncDB.sql

echo "DEV_DB_USER:$DEV_DB_USER"
echo "DEV_DB_PASSWD:$DEV_DB_PASSWD"
echo "DEV_DB_IP:$DEV_DB_IP"
echo "DB_NAME:$DB_NAME"

echo "REL_DB_USER:$REL_DB_USER"
echo "REL_DB_PASSWD:$REL_DB_PASSWD"
echo "REL_DB_IP:$REL_DB_IP"

mysqldump -u$DEV_DB_USER -p$DEV_DB_PASSWD -h$DEV_DB_IP --databases $DB_NAME  > syncDB_temp.sql;
sed -i -e '/DEFINER/d' syncDB_temp.sql

mysql -u$REL_DB_USER -p$REL_DB_PASSWD -h$REL_DB_IP  $DB_NAME < syncDB_temp.sql

echo "-----before action---------"
mysql -u$REL_DB_USER -p$REL_DB_PASSWD -h$REL_DB_IP $DB_NAME < $1/initScript/db/initBefore.sql;

echo "-----truncate table---------"
which mysql
for file in `find $1/initScript/db -name "cleanTable.sql"`
do
  echo $file;
  cat $file;
  mysql -u$REL_DB_USER -p$REL_DB_PASSWD -h$REL_DB_IP $DB_NAME < $file;
  sleep 10s
done

echo "-----update table---------"
for file in `find $1/initScript/db -name "updateTable.sql"`
do
  echo $file;
  cat $file;
  mysql -u$REL_DB_USER -p$REL_DB_PASSWD -h$REL_DB_IP $DB_NAME < $file;
done

echo "-----after action---------"
mysql -u$REL_DB_USER -p$REL_DB_PASSWD -h$REL_DB_IP $DB_NAME < $1/initScript/db/initAfter.sql;

mysqldump -u$REL_DB_USER -p$REL_DB_PASSWD -h$REL_DB_IP --databases $DB_NAME  > syncDB.sql;
sed -i -e '/DEFINER/d' syncDB.sql