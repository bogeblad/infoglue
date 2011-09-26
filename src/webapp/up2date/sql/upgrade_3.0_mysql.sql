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

ALTER TABLE cmSiteNode ADD COLUMN isDeleted TINYINT NOT NULL DEFAULT 0;
ALTER TABLE cmContent ADD COLUMN isDeleted TINYINT NOT NULL DEFAULT 0;
ALTER TABLE cmRepository ADD COLUMN isDeleted TINYINT NOT NULL DEFAULT 0;

ALTER TABLE cmRedirect ADD COLUMN createdDateTime datetime;
ALTER TABLE cmRedirect ADD COLUMN publishDateTime datetime;
ALTER TABLE cmRedirect ADD COLUMN expireDateTime datetime;
ALTER TABLE cmRedirect ADD COLUMN modifier TEXT;
ALTER TABLE cmRedirect ADD COLUMN isUserManaged TINYINT NOT NULL DEFAULT '1';

ALTER TABLE cmContentTypeDefinition ADD COLUMN parentContentTypeDefinitionId integer DEFAULT '-1';
ALTER TABLE cmContentTypeDefinition ADD COLUMN detailPageResolverClass VARCHAR(255) DEFAULT '';
ALTER TABLE cmContentTypeDefinition ADD COLUMN detailPageResolverData VARCHAR(1024) DEFAULT '';

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

CREATE INDEX assetKeyIndex ON cmDigitalAsset(assetKey(255));
CREATE INDEX assetFileNameIndex ON cmDigitalAsset(assetFileName(255));
CREATE INDEX assetFileSizeIndex ON cmDigitalAsset(assetFileSize);
CREATE INDEX assetContentTypeIndex ON cmDigitalAsset(assetContentType);

CREATE INDEX redirectUrlIndex ON cmRedirect(redirectUrl(255));

ALTER TABLE cmAccessRight CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmAccessRightGroup CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmAccessRightRole CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmAccessRightUser CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmAvailableServiceBinding CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmAvailableServiceBindingSiteNodeTypeDefinition CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmCategory CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmContent CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmContentCategory CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmContentRelation CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmContentTypeDefinition CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmContentVersion CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmContentVersionDigitalAsset CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmDigitalAsset CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmEvent CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmFormEntry CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmFormEntryAsset CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmFormEntryValue CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmGroup CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmGroupContentTypeDefinition CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmGroupProperties CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmGroupPropertiesDigitalAsset CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmInfoGlueProperties CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmInterceptionPoint CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmInterceptionPointInterceptor CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmInterceptor CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmLanguage CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmPropertiesCategory CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmPublication CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmPublicationDetail CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmQualifyer CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmRedirect CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmRegistry CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmRepository CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmRepositoryContentTypeDefinition CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmRepositoryLanguage CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmRole CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmRoleContentTypeDefinition CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmRoleProperties CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmRolePropertiesDigitalAsset CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmServerNode CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmServiceBinding CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmServiceDefinition CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmServiceDefinitionAvailableServiceBinding CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmSiteNode CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmSiteNodeTypeDefinition CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmSiteNodeVersion CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmSubscription CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmSubscriptionFilter CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmSystemUser CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmSystemUserGroup CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmSystemUserRole CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmTransactionHistory CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmUserContentTypeDefinition CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmUserProperties CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmUserPropertiesDigitalAsset CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE cmWorkflowDefinition CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE OS_CURRENTSTEP CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE OS_CURRENTSTEP_PREV CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE OS_HISTORYSTEP CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE OS_HISTORYSTEP_PREV CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE OS_PROPERTYENTRY CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE OS_STEPIDS CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;
ALTER TABLE OS_WFENTRY CONVERT TO CHARACTER SET utf8 COLLATE utf8_general_ci;