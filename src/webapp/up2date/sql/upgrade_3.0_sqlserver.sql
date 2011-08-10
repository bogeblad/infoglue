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

ALTER TABLE cmSubscription ALTER COLUMN entityName VARCHAR(100) NULL;
ALTER TABLE cmSubscription ALTER COLUMN entityId varchar(200) NULL;

ALTER TABLE cmDigitalAsset ALTER COLUMN assetContentType VARCHAR(255);

ALTER TABLE cmSiteNodeVersion ADD sortOrder INTEGER NOT NULL DEFAULT -1;
ALTER TABLE cmSiteNodeVersion ADD isHidden INTEGER NOT NULL DEFAULT 0;

ALTER TABLE cmSiteNode ADD isDeleted INTEGER NOT NULL DEFAULT 0;
ALTER TABLE cmContent ADD isDeleted INTEGER NOT NULL DEFAULT 0;
ALTER TABLE cmRepository ADD isDeleted INTEGER NOT NULL DEFAULT 0;

ALTER TABLE cmContentTypeDefinition ADD parentContentTypeDefinitionId integer DEFAULT '-1';
ALTER TABLE cmContentTypeDefinition ADD detailPageResolverClass VARCHAR(255) DEFAULT '';
ALTER TABLE cmContentTypeDefinition ADD detailPageResolverData VARCHAR(1024) DEFAULT '';

ALTER TABLE cmRedirect ADD createdDateTime datetime NOT NULL default '2000-01-01 01:01:01';
ALTER TABLE cmRedirect ADD publishDateTime datetime NOT NULL default '2000-01-01 01:01:01';
ALTER TABLE cmRedirect ADD expireDateTime datetime NOT NULL default '2050-01-01 01:01:01';
ALTER TABLE cmRedirect ADD modifier VARCHAR(1024) NOT NULL default 'system';
ALTER TABLE cmRedirect ADD isUserManaged INTEGER NOT NULL DEFAULT '1';

DROP INDEX cmPropertiesCategory.propCategoryAttrNameIndex;
DROP INDEX cmPropertiesCategory.propCategoryEntityNameIndex;
DROP INDEX cmPropertiesCategory.propCategoryEntityIdIndex;
DROP INDEX cmPropertiesCategory.propCategoryCategoryIdIndex;
DROP INDEX cmCategory.categoryParentIdIndex;
DROP INDEX cmCategory.categoryNameIndex;

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