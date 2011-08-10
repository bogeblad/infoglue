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

ALTER TABLE cmContentTypeDef ADD parentContentTypeDefinitionId number DEFAULT -1;
ALTER TABLE cmContentTypeDef ADD detailPageResolverClass varchar2(255) DEFAULT '';
ALTER TABLE cmContentTypeDef ADD detailPageResolverData varchar2(1024) DEFAULT '';

ALTER TABLE cmRedirect ADD createdDateTime date;
ALTER TABLE cmRedirect ADD publishDateTime date;
ALTER TABLE cmRedirect ADD expireDateTime date;
ALTER TABLE cmRedirect ADD modifier varchar2(1024) DEFAULT 'system' NOT NULL;
ALTER TABLE cmRedirect ADD isUserManaged number DEFAULT 1 NOT NULL;

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

create index assetKeyIndex on cmDigitalAsset(assetKey);
create index assetFileNameIndex on cmDigitalAsset(assetFileName);
create index assetFileSizeIndex on cmDigitalAsset(assetFileSize);
create index assetContentTypeIndex on cmDigitalAsset(assetContentType);

CREATE INDEX redirectUrlIndex ON cmRedirect(redirectUrl);








