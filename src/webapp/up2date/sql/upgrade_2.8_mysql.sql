alter table cmSiteNodeVersion add pageCacheTimeout varchar(20) default NULL;
alter table cmSiteNodeVersion add disableForceIDCheck tinyint(4) NOT NULL default '2';
 
ALTER TABLE cmQualifyer ADD INDEX qualifyerNameIndex(name(50));
ALTER TABLE cmQualifyer ADD INDEX qualifyerValueIndex(value(50));

alter table cmDigitalAsset change assetBlob assetBlob longblob;