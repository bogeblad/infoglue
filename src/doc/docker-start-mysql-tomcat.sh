#!/usr/bin/env bash

set -m
set -e

/usr/bin/mysqld_safe &

sleep 10

mysql -h 127.0.0.1 -P 3306 -u root -e "CREATE DATABASE IF NOT EXISTS infoglue"
mysql -h 127.0.0.1 -P 3306 -u root -e "GRANT ALL ON your_db.* to 'infoglue'@'%' IDENTIFIED BY 'changeit'"
mysql -h 127.0.0.1 -P 3306 -u root -e "FLUSH PRIVILEGES"


catalina.sh run