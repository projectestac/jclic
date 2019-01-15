#!/bin/sh

if [ -f /var/lib/mysql/.initialized ]; then
  echo "[i] MySQL already initialized, skipping creation"
  echo "[i] Starting MySQL..."
  exec /usr/bin/mysqld --user=root --console &
else
  echo "[i] MySQL data directory not found or not initialized, creating initial DBs"

  mysql_install_db --user=root > /dev/null

  touch /var/lib/mysql/.initialized

  if [ "$MYSQL_ROOT_PASSWORD" = "" ]; then
    MYSQL_ROOT_PASSWORD=mysql_jclic_pwd
    echo "[i] MySQL root Password: $MYSQL_ROOT_PASSWORD"
  fi

  MYSQL_DATABASE=${MYSQL_DATABASE:-""}
  MYSQL_USER=${MYSQL_USER:-""}
  MYSQL_PASSWORD=${MYSQL_PASSWORD:-""}
  MYSQL_SAMPLE_DATA=${MYSQL_SAMPLE_DATA:-""}

  if [ ! -d "/run/mysqld" ]; then
    mkdir -p /run/mysqld
  fi

  tfile=`mktemp`
  if [ ! -f "$tfile" ]; then
      return 1
  fi

  cat << EOF > $tfile
USE mysql;
FLUSH PRIVILEGES;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY "$MYSQL_ROOT_PASSWORD" WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;
UPDATE user SET password=PASSWORD("") WHERE user='root' AND host='localhost';
EOF

  if [ "$MYSQL_DATABASE" != "" ]; then
    echo "[i] Creating database: $MYSQL_DATABASE"
    echo "CREATE DATABASE IF NOT EXISTS \`$MYSQL_DATABASE\` CHARACTER SET utf8 COLLATE utf8_general_ci;" >> $tfile

    if [ "$MYSQL_USER" != "" ]; then
      echo "[i] Creating user: $MYSQL_USER with password $MYSQL_PASSWORD"
      echo "GRANT ALL ON \`$MYSQL_DATABASE\`.* to '$MYSQL_USER'@'%' IDENTIFIED BY '$MYSQL_PASSWORD';" >> $tfile
      echo "GRANT ALL ON \`$MYSQL_DATABASE\`.* to '$MYSQL_USER'@localhost IDENTIFIED BY '$MYSQL_PASSWORD';" >> $tfile
    fi
  fi

  /usr/bin/mysqld --user=root --bootstrap --verbose=0 < $tfile  
  rm -f $tfile

  echo "[i] Starting MySQL..."
  /usr/bin/mysqld --user=root --console &


  if [ -f $MYSQL_SAMPLE_DATA ]; then  
    # Returns true once mysql can connect.
    mysql_ready() {
        /usr/bin/mysqladmin ping > /dev/null 2>&1
    }

    while !(mysql_ready)
    do
       sleep 3
       echo "[i] Waiting for mysql ..."
    done  

    echo "[i] Filling MySQL database with sample data"
    /usr/bin/mysql -u root JClicReports < $MYSQL_SAMPLE_DATA
    rm -f $MYSQL_SAMPLE_DATA
  fi

fi

echo "[i] Starting Tomcat"
exec /usr/local/tomcat/bin/catalina.sh run
