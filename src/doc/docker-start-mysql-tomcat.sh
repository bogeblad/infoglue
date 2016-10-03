#!/usr/bin/env bash

set -m
set -e

/usr/bin/mysqld_safe --lower_case_table_names=1 --max_allowed_packet=32M &

sleep 10

mysql -h 127.0.0.1 -P 3306 -u root -pchangeit -e "CREATE DATABASE IF NOT EXISTS infoglue"
mysql -h 127.0.0.1 -P 3306 -u root -pchangeit -e "GRANT ALL ON your_db.* to 'infoglue'@'%' IDENTIFIED BY 'changeit'"
mysql -h 127.0.0.1 -P 3306 -u root -pchangeit -e "FLUSH PRIVILEGES"

mysql -h 127.0.0.1 -P 3306 -u root -pchangeit infoglue < /tmp/infoglue3.sql

echo "Created database"

export JAVA_OPTS="-Dfile.encoding=ISO-8859-1 -XX:MaxPermSize=256m -Xms128M -Xmx1024M"
catalina.sh run
