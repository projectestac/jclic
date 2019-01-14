#!/bin/bash

# Copied from the original entrypoint `/run.sh`:
VOLUME_HOME="/var/lib/mysql"

if [[ ! -d $VOLUME_HOME/mysql ]]; then
    echo "=> An empty or uninitialized MySQL volume is detected in $VOLUME_HOME"
    echo "=> Installing MySQL ..."
    mysqld --initialize-insecure --user=mysql > /dev/null 2>&1
    echo "=> Done!"  
    /create_mysql_admin_user.sh
else
    echo "=> Using an existing volume of MySQL"
fi

# Initialize the MySQL database
cd /root

echo "=> Starting MySQL"
service mysql start

echo "=> Creating JClicReports database and user"
mysql -uroot <createDatabase.sql

echo "=> Filling JClicReports database with sample data"
mysql -uroot <testData.sql

echo "=> Done!"

# Continue with the original entrypoint
exec supervisord -n
