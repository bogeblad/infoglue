CREATE TABLE  cmFormEntry (
  id int(10) unsigned NOT NULL auto_increment,
  originAddress varchar(1024) NOT NULL,
  formName varchar(255) NOT NULL,
  formContentId int(10) unsigned NOT NULL,
  userIP varchar(20) NOT NULL,
  userAgent varchar(255) NOT NULL,
  PRIMARY KEY  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE  cmFormEntryValue (
  id int(10) unsigned NOT NULL auto_increment,
  name varchar(128) NOT NULL,
  value varchar(4096) NOT NULL,
  formEntryId int(10) unsigned NOT NULL,
  PRIMARY KEY  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE cmSubscription (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  interceptionPointId INTEGER UNSIGNED NOT NULL,
  name varchar(100) NOT NULL,
  isGlobal tinyint(4) NOT NULL default '0',
  entityName varchar(100),
  entityId varchar(200) DEFAULT NULL,
  userName varchar(150) NOT NULL,
  userEmail varchar(150),
  lastNotifiedDateTime timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY(id)
) TYPE = InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE cmSubscriptionFilter (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  subscriptionId INTEGER UNSIGNED NOT NULL,
  filterType varchar(50) NOT NULL,
  filterCondition varchar(255) NOT NULL,
  isAndCondition tinyint(4) NOT NULL default '1',
  PRIMARY KEY(id)
) TYPE = InnoDB DEFAULT CHARSET=utf8;