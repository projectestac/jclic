#!/bin/sh

MYSQL_SAMPLE_DATA=${MYSQL_SAMPLE_DATA:-"/root/testData.sql"}

  # Returns true once mysql can connect.
mysql_ready() {
    /usr/bin/mysqladmin ping > /dev/null 2>&1
}

if [ -f $MYSQL_SAMPLE_DATA ]; then
  while !(mysql_ready)
  do
     sleep 3
     echo "[info] Waiting for mysql ..."
  done  

  echo "[info] Filling MySQL database with sample data"

  /usr/bin/mysql -u root JClicReports < $MYSQL_SAMPLE_DATA

  echo "[info] Done!"

else
  
  echo "[err] Unable to locate \"$MYSQL_SAMPLE_DATA\"" 
  
fi
