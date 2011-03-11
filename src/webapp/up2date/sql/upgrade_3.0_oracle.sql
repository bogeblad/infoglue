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

ALTER TABLE cmSubscription MODIFY ( entityName VARCHAR2(100) NULL );
ALTER TABLE cmSubscription MODIFY ( entityId VARCHAR2(200) NULL );

ALTER TABLE cmDigAsset MODIFY ( assetContentType VARCHAR2(255) NULL );

ALTER TABLE cmSiNoVer ADD COLUMN sortOrder int NOT NULL DEFAULT '-1';
ALTER TABLE cmSiNoVer ADD COLUMN isHidden int NOT NULL DEFAULT 0;
ALTER TABLE cmSiNoVer ADD COLUMN forceProtocolChange int NOT NULL DEFAULT 2;

ALTER TABLE cmSiNo ADD COLUMN isDeleted int NOT NULL DEFAULT 0;
ALTER TABLE cmCont ADD COLUMN isDeleted int NOT NULL DEFAULT 0;
ALTER TABLE cmRepository ADD COLUMN isDeleted int NOT NULL DEFAULT 0;

ALTER TABLE cmContentTypeDef ADD COLUMN parentContentTypeDefinitionId int DEFAULT '-1';
ALTER TABLE cmContentTypeDef ADD COLUMN detailPageResolverClass VARCHAR2(255) DEFAULT '';
ALTER TABLE cmContentTypeDef ADD COLUMN detailPageResolverData VARCHAR2(1024) DEFAULT '';

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

