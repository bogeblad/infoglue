ALTER TABLE cmSubscription change entityName entityName varchar(100) DEFAULT NULL;
ALTER TABLE cmSubscription change entityId entityId varchar(200) DEFAULT NULL;

ALTER TABLE cmDigitalAsset change  assetContentType assetContentType VARCHAR(255) ;

--ALTER TABLE cmSiteNode ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT '-1';
--ALTER TABLE cmSiteNode ADD COLUMN isHidden TINYINT NOT NULL DEFAULT 0;

ALTER TABLE cmSiteNodeVersion ADD COLUMN sortOrder INT(10) NOT NULL DEFAULT -1;
ALTER TABLE cmSiteNodeVersion ADD COLUMN isHidden TINYINT UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE cmSiteNodeVersion ADD COLUMN forceProtocolChange TINYINT(4) UNSIGNED NOT NULL DEFAULT 2;

ALTER TABLE cmSiteNode ADD COLUMN isDeleted TINYINT NOT NULL DEFAULT 0;
ALTER TABLE cmContent ADD COLUMN isDeleted TINYINT NOT NULL DEFAULT 0;
ALTER TABLE cmRepository ADD COLUMN isDeleted TINYINT NOT NULL DEFAULT 0;

ALTER TABLE cmContentTypeDefinition ADD COLUMN parentContentTypeDefinitionId INT DEFAULT '-1';
ALTER TABLE cmContentTypeDefinition ADD COLUMN detailPageResolverClass VARCHAR(255) DEFAULT '';
ALTER TABLE cmContentTypeDefinition ADD COLUMN detailPageResolverData VARCHAR(1024) DEFAULT '';

OBS::::::: add index på alla isDeleted, sortOrder och isHidden

drop index propCategoryAttrNameIndex;
drop index propCategoryEntityNameIndex;
drop index propCategoryEntityNameIndex;
drop index propCategoryCategoryIdIndex;
drop index categoryParentIdIndex;
drop index categoryNameIndex;

create index propCategoryAttrNameIndex on cmPropertiesCategory(attributeName(100));
create index propCategoryEntityNameIndex on cmPropertiesCategory(entityName(100));
create index propCategoryEntityIdIndex on cmPropertiesCategory(entityId(255));
create index propCategoryCategoryIdIndex on cmPropertiesCategory(categoryId);
create index categoryParentIdIndex on cmCategory(parentId);
create index categoryNameIndex on cmCategory(name(100));

ALTER TABLE cmContentVersion CHANGE versionValue versionValue longtext;

ALTER TABLE cmFormEntry ADD COLUMN userName VARCHAR(255) NULL;
ALTER TABLE cmFormEntry ADD COLUMN registrationDateTime DATETIME NULL;
