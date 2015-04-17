-- ===============================================================================
--
-- Part of the InfoGlue Content Management Platform (www.infoglue.org)
--
-- ===============================================================================
--
--  Copyright (C)
--
-- This program is free software; you can redistribute it and/or modify it under
-- the terms of the GNU General Public License version 2, as published by the
-- Free Software Foundation. See the file LICENSE.html for more information.
--
-- This program is distributed in the hope that it will be useful, but WITHOUT
-- ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License along with
-- this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
-- Place, Suite 330 / Boston, MA 02111-1307 / USA.
--
-- ===============================================================================
--
-- $Id: update-db-2.9-to-2.9.7.1.sql,v 1.1 2008/07/03 15:35:20 mattias Exp $
--
-- This script contains the database updates required to go from 2.9 to 3.0.

ALTER TABLE cmSubscription MODIFY ( entityName VARCHAR2(100) default NULL );
ALTER TABLE cmSubscription MODIFY ( entityId VARCHAR2(200) default NULL );

ALTER TABLE cmDigAsset MODIFY ( assetContentType VARCHAR2(255) default NULL );

ALTER TABLE cmSiNoVer ADD sortOrder number DEFAULT '-1' NOT NULL;
ALTER TABLE cmSiNoVer ADD isHidden number DEFAULT 0 NOT NULL;

ALTER TABLE cmSiNo ADD isDeleted number DEFAULT 0 NOT NULL;
ALTER TABLE cmCont ADD isDeleted number DEFAULT 0 NOT NULL;
ALTER TABLE cmRepository ADD isDeleted number DEFAULT 0 NOT NULL;

ALTER TABLE cmContentTypeDef ADD parentContTypeDefId number DEFAULT -1;
ALTER TABLE cmContentTypeDef ADD detailPageResolverClass varchar2(255) DEFAULT '';
ALTER TABLE cmContentTypeDef ADD detailPageResolverData varchar2(1024) DEFAULT '';

ALTER TABLE cmRedirect ADD createdDateTime date;
ALTER TABLE cmRedirect ADD publishDateTime date;
ALTER TABLE cmRedirect ADD expireDateTime date;
ALTER TABLE cmRedirect ADD modifier varchar2(1024) DEFAULT 'system' NOT NULL;
ALTER TABLE cmRedirect ADD isUserManaged number DEFAULT 1 NOT NULL;

ALTER TABLE cmSystemUser ADD source varchar2(255) default 'infoglue' NOT NULL;
ALTER TABLE cmSystemUser ADD modifiedDateTime date default sysdate;
ALTER TABLE cmSystemUser ADD isActive number default 1 NOT NULL;

ALTER TABLE cmRole ADD source varchar2(255) default 'infoglue' NOT NULL;
ALTER TABLE cmRole ADD modifiedDateTime date  default sysdate;
ALTER TABLE cmRole ADD isActive number default 1 NOT NULL;

ALTER TABLE cmGroup ADD groupType varchar2(255) default ' ' NOT NULL;
ALTER TABLE cmGroup ADD source varchar2(255) default 'infoglue' NOT NULL;
ALTER TABLE cmGroup ADD modifiedDateTime date default sysdate;
ALTER TABLE cmGroup ADD isActive number default 1 NOT NULL;

CREATE SEQUENCE cmPageDeliveryMetaData_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE cmPageDeliveryMetaData (
  pageDeliveryMetaDataId number NOT NULL,
  siteNodeId number NOT NULL,
  languageId number NOT NULL,
  contentId number NOT NULL,
  lastModifiedDateTime date NOT NULL,
  selectiveCacheUpdateNotAppl number NOT NULL,
  lastModifiedTimeout date default -1 NOT NULL,
  PRIMARY KEY (pageDeliveryMetaDataId) 
);

CREATE SEQUENCE cmPageDeliveryMetaDataEnt_seq START WITH 1 INCREMENT BY 1;

CREATE  TABLE cmPageDeliveryMetaDataEnt (
  pageDeliveryMetaDataEntityId number NOT NULL,
  pageDeliveryMetaDataId number NOT NULL,
  siteNodeId number NOT NULL,
  contentId number NOT NULL,
  PRIMARY KEY (pageDeliveryMetaDataEntityId) 
);

create index pageDeliveryMetaDataIDX on cmPageDeliveryMetaData(siteNodeId, languageId, contentId);
drop index propCategoryAttrNameIndex;
drop index propCategoryEntityNameIndex;
drop index propCategoryEntityNameIndex;
drop index propCategoryCategoryIdIndex;
drop index categoryParentIdIndex;
drop index categoryNameIndex;

create index propCategoryAttrNameIndex on cmPropertiesCategory(attributeName);
create index propCategoryEntityNameIndex on cmPropertiesCategory(entityName);
create index propCategoryEntityIdIndex on cmPropertiesCategory(entityId);
create index propCategoryCategoryIdIndex on cmPropertiesCategory(categoryId);
create index categoryParentIdIndex on cmCategory(parentId);
create index categoryNameIndex on cmCategory(name);

create index assetKeyIndex on cmDigAsset(assetKey);
create index assetFileNameIndex on cmDigAsset(assetFileName);
create index assetFileSizeIndex on cmDigAsset(assetFileSize);
create index assetContentTypeIndex on cmDigAsset(assetContentType);

CREATE INDEX redirectUrlIndex ON cmRedirect(redirectUrl);

CREATE INDEX "OS_PROPERTYENTRY_ENTNAMEINDEX" ON OS_PROPERTYENTRY(entity_name);
CREATE INDEX "OS_PROPERTYENTRY_ENTIDINDEX" ON OS_PROPERTYENTRY(entity_id);

CREATE INDEX publicationDateIndex ON cmPublication(publicationDateTime);
CREATE INDEX publicationDetailPublIDIndex ON cmPublicationDetail(publicationId);

CREATE INDEX groupPropDigAssetIdIndex ON cmGroupPropDigAsset(digAssetId);
CREATE INDEX rolePropDigAssetIdIndex ON cmRolePropDigAsset(digAssetId);
CREATE INDEX userPropDigAssetIdIndex ON cmUserPropDigAsset(digAssetId);
CREATE INDEX contVerDigAssetIdIndex ON cmContVerDigAsset(digAssetId);
CREATE INDEX contVerContVerIdIndex ON cmContVerDigAsset(contVerId);

ALTER TABLE cmSiNoVer MODIFY MODIFIEDDATETIME TIMESTAMP;
ALTER TABLE cmContVer MODIFY MODIFIEDDATETIME TIMESTAMP;

CREATE INDEX "contentVersionStateIndex" ON cmContVer(stateId);
CREATE INDEX "contentVersionIsActiveIndex" ON cmContVer(isActive);
CREATE INDEX "contentVersionLangIndex" ON cmContVer(languageId);

CREATE INDEX "contentPublIndex" ON cmCont(publishDateTime);
CREATE INDEX "contentExpIndex" ON cmCont(expireDateTime);
CREATE INDEX "contentREPOSITORYIndex" ON cmCont(REPOSITORYID);
CREATE INDEX "categoryActiveIndex" ON cmCategory(active);
CREATE INDEX "parentIdIndex" ON cmCategory(parentId);
CREATE INDEX "contCONTENTTYPEDEFIDIndex" ON cmCont(CONTENTTYPEDEFID);
CREATE INDEX "siteNodeVersionStateIndex" ON cmSiNoVer(stateId);
CREATE INDEX "siteNodeVersionIsActiveIndex" ON cmSiNoVer(isActive);
CREATE INDEX "siteNodePublIndex" ON cmSiNo(publishDateTime);
CREATE INDEX "siteNodeExpIndex" ON cmSiNo(expireDateTime);
CREATE INDEX "siteNodeREPOSITORYIndex" ON cmSiNo(repositoryId);
CREATE INDEX "siteNodeMetaContentIdIndex" ON cmSiNo(metaInfoContentId);
CREATE INDEX "siteNodeParentSiteNodeIdIndex" ON cmSiNo(parentSiNoId);
CREATE INDEX "siteNodeIsDeletedIndex" ON cmSiNo(isDeleted);

create index "cmARRARIDIDX" ON cmAccessRightRole(accessRightId);
create index "cmARGARIDIDX" ON cmAccessRightGroup(accessRightId);
create index "cmARUARIDIDX" ON cmAccessRightUser(accessRightId);
