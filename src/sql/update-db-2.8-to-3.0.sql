DROP TABLE IF EXISTS cmFormEntry;
CREATE TABLE  cmFormEntry (
  id int(10) unsigned NOT NULL auto_increment,
  originAddress varchar(1024) NOT NULL,
  formName varchar(255) NOT NULL,
  formContentId int(10) unsigned NOT NULL,
  userIP varchar(20) NOT NULL,
  userAgent varchar(255) NOT NULL,
  PRIMARY KEY  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS cmFormEntryValue;
CREATE TABLE  cmFormEntryValue (
  id int(10) unsigned NOT NULL auto_increment,
  name varchar(128) NOT NULL,
  value varchar(4096) NOT NULL,
  PRIMARY KEY  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;