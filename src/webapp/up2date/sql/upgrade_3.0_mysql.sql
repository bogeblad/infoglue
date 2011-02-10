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
-- $Id: sqlserver-update-db-2.9-to-3.0.sql,v 1.1 2010/09/08 15:35:20 mattias Exp $
--
-- This script contains the database updates required to go from 2.9 to 3.0.

ALTER TABLE cmSubscription CHANGE entityName entityName varchar(100) DEFAULT NULL;
ALTER TABLE cmSubscription CHANGE entityId entityId varchar(200) DEFAULT NULL;

ALTER TABLE cmDigitalAsset CHANGE assetContentType assetContentType VARCHAR(255);

ALTER TABLE cmSiteNodeVersion ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT -1;
ALTER TABLE cmSiteNodeVersion ADD COLUMN isHidden TINYINT UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE cmSiteNodeVersion ADD COLUMN forceProtocolChange TINYINT(4) UNSIGNED NOT NULL DEFAULT 2;

ALTER TABLE cmSiteNode ADD COLUMN isDeleted TINYINT NOT NULL DEFAULT 0;
ALTER TABLE cmContent ADD COLUMN isDeleted TINYINT NOT NULL DEFAULT 0;
ALTER TABLE cmRepository ADD COLUMN isDeleted TINYINT NOT NULL DEFAULT 0;

ALTER TABLE cmPropertiesCategory DROP INDEX propCategoryAttrNameIndex;
ALTER TABLE cmPropertiesCategory DROP INDEX propCategoryEntityNameIndex;
ALTER TABLE cmPropertiesCategory DROP INDEX propCategoryEntityIdIndex;
ALTER TABLE cmPropertiesCategory DROP INDEX propCategoryCategoryIdIndex;
ALTER TABLE cmCategory DROP INDEX categoryParentIdIndex;
ALTER TABLE cmCategory DROP INDEX categoryNameIndex;

create index propCategoryAttrNameIndex on cmPropertiesCategory(attributeName(100));
create index propCategoryEntityNameIndex on cmPropertiesCategory(entityName(100));
create index propCategoryEntityIdIndex on cmPropertiesCategory(entityId);
create index propCategoryCategoryIdIndex on cmPropertiesCategory(categoryId);
create index categoryParentIdIndex on cmCategory(parentId);
create index categoryNameIndex on cmCategory(name(100));