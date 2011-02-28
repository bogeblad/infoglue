CREATE TABLE cmAccessRightUser (
  accessRightUserId int(11) NOT NULL auto_increment,
  accessRightId int(11) NOT NULL default '0',
  userName varchar(150) NOT NULL default '',
  PRIMARY KEY  (accessRightUserId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;;

CREATE TABLE cmServerNode (
  serverNodeId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  dnsName text NOT NULL,
  PRIMARY KEY  (serverNodeId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;;

alter table cmSiteNodeVersion add disableLanguages tinyint(4) NOT NULL default '2';
