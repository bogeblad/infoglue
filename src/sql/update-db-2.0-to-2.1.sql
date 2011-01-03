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
-- $Id: update-db-1.3-to-2.0.sql,v 1.14 2005/06/29 08:52:46 mattias Exp $
--
-- This script contains the database updates required to go from 2.0 to 2.1.
----------------------------------------------------------------------------------
-- Updates OSWorkflow tables so caller and owner can be longer strings.
----------------------------------------------------------------------------------
alter table OS_CURRENTSTEP change OWNER OWNER varchar(255);
alter table OS_HISTORYSTEP change OWNER OWNER varchar(255);
alter table OS_CURRENTSTEP change CALLER CALLER varchar(255);
alter table OS_HISTORYSTEP change CALLER CALLER varchar(255);

DROP INDEX OWNER ON OS_CURRENTSTEP;
DROP INDEX CALLER ON OS_CURRENTSTEP;
DROP INDEX OWNER ON OS_HISTORYSTEP;
DROP INDEX CALLER ON OS_HISTORYSTEP;

CREATE INDEX OWNER ON OS_CURRENTSTEP(OWNER);
CREATE INDEX CALLER ON OS_CURRENTSTEP(CALLER);
CREATE INDEX OWNER ON OS_HISTORYSTEP(OWNER);
CREATE INDEX CALLER ON OS_HISTORYSTEP(CALLER);

DROP INDEX referencingEntityName ON cmRegistry;
DROP INDEX referencingEntityId ON cmRegistry;
DROP INDEX entityName ON cmRegistry;
DROP INDEX entityId ON cmRegistry;
DROP INDEX referencingEntityComplName ON cmRegistry;
DROP INDEX referencingEntityComplId ON cmRegistry;
DROP INDEX categoryContVersionId ON cmContentCategory;
DROP INDEX contVerDigAssetDigAssId ON cmContentVersionDigitalAsset;
DROP INDEX contVerDigAssetContVerId ON cmContentVersionDigitalAsset;

CREATE INDEX referencingEntityName ON cmRegistry(referencingEntityName);
CREATE INDEX referencingEntityId ON cmRegistry(referencingEntityId);
CREATE INDEX entityName ON cmRegistry(entityName);
CREATE INDEX entityId ON cmRegistry(entityId);
CREATE INDEX referencingEntityComplName ON cmRegistry(referencingEntityComplName);
CREATE INDEX referencingEntityComplId ON cmRegistry(referencingEntityComplId);
CREATE INDEX categoryContVersionId ON cmContentCategory(contentVersionId);
CREATE INDEX contVerDigAssetDigAssId ON cmContentVersionDigitalAsset(digitalAssetId);
CREATE INDEX contVerDigAssetContVerId ON cmContentVersionDigitalAsset(contentVersionId);

----------------------------------------------------------------------------------
-- Add new interception point for workflows				                        --
----------------------------------------------------------------------------------
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (31,'Workflow','Workflow.Create','This point checks access to creating a new workflow',1);
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (31, 1);

-- ---------------------------------------------------------------------------- --
-- Adding new column to siteNodeVersion for special pageCacheKey                --
-- ---------------------------------------------------------------------------- --
alter table cmSiteNodeVersion add pageCacheKey varchar(255) NOT NULL default 'default';


-- ---------------------------------------------------------------------------- --
-- Adding new table to support redirecting old pages                            --
-- ---------------------------------------------------------------------------- --
CREATE TABLE cmRedirect (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  url TEXT NOT NULL,
  redirectUrl TEXT NOT NULL,
  PRIMARY KEY(id)
) TYPE = MYISAM;

ALTER TABLE cmRedirect DROP INDEX redirectUrl, ADD INDEX redirectUrl(redirectUrl(255));

ALTER TABLE cmSiteNode TYPE = InnoDB;
ALTER TABLE cmSiteNodeVersion TYPE = InnoDB;
ALTER TABLE cmServiceBinding TYPE = InnoDB;
ALTER TABLE cmQualifyer TYPE = InnoDB;
ALTER TABLE cmContent TYPE = InnoDB;
ALTER TABLE cmContentVersion TYPE = InnoDB;
ALTER TABLE cmContentVersionDigitalAsset TYPE = InnoDB;
ALTER TABLE cmDigitalAsset TYPE = InnoDB;
ALTER TABLE cmPublication TYPE = InnoDB;
ALTER TABLE cmPublicationDetail TYPE = InnoDB;
ALTER TABLE cmEvent TYPE = InnoDB;
ALTER TABLE cmRegistry TYPE = InnoDB;
ALTER TABLE cmAccessRight TYPE = InnoDB;
ALTER TABLE cmAccessRightGroup TYPE = InnoDB;
ALTER TABLE cmAccessRightRole TYPE = InnoDB;
ALTER TABLE cmAvailableServiceBinding TYPE = InnoDB;
ALTER TABLE cmAvailableServiceBindingSiteNodeTypeDefinition TYPE = InnoDB;
ALTER TABLE cmContentRelation TYPE = InnoDB;
ALTER TABLE cmContentTypeDefinition TYPE = InnoDB;
ALTER TABLE cmUserPropertiesDigitalAsset TYPE = InnoDB;
ALTER TABLE cmRolePropertiesDigitalAsset TYPE = InnoDB;
ALTER TABLE cmLanguage TYPE = InnoDB;
ALTER TABLE cmRepository TYPE = InnoDB;
ALTER TABLE cmRepositoryContentTypeDefinition TYPE = InnoDB;
ALTER TABLE cmRepositoryLanguage TYPE = InnoDB;
ALTER TABLE cmRole TYPE = InnoDB;
ALTER TABLE cmServiceDefinition TYPE = InnoDB;
ALTER TABLE cmServiceDefinitionAvailableServiceBinding TYPE = InnoDB;
ALTER TABLE cmSiteNodeTypeDefinition TYPE = InnoDB;
ALTER TABLE cmSystemUser TYPE = InnoDB;
ALTER TABLE cmSystemUserRole TYPE = InnoDB;
ALTER TABLE cmTransactionHistory TYPE = InnoDB;
ALTER TABLE cmRoleContentTypeDefinition TYPE = InnoDB;
ALTER TABLE cmRoleProperties TYPE = InnoDB;
ALTER TABLE cmUserContentTypeDefinition TYPE = InnoDB;
ALTER TABLE cmUserProperties TYPE = InnoDB;
ALTER TABLE cmInterceptionPoint TYPE = InnoDB;
ALTER TABLE cmInterceptionPointInterceptor TYPE = InnoDB;
ALTER TABLE cmInterceptor TYPE = InnoDB;
ALTER TABLE cmCategory TYPE = InnoDB;
ALTER TABLE cmContentCategory TYPE = InnoDB;
ALTER TABLE cmGroupPropertiesDigitalAsset TYPE = InnoDB;
ALTER TABLE cmPropertiesCategory TYPE = InnoDB;
ALTER TABLE cmGroup TYPE = InnoDB;
ALTER TABLE cmGroupContentTypeDefinition TYPE = InnoDB;
ALTER TABLE cmGroupProperties TYPE = InnoDB;
ALTER TABLE cmSystemUserGroup TYPE = InnoDB;
ALTER TABLE cmWorkflowDefinition TYPE = InnoDB;
ALTER TABLE cmRedirect TYPE = InnoDB;
ALTER TABLE cmInfoGlueProperties TYPE = InnoDB;



-- This adds meta info to cmSiteNode for efficiency and security.. meta info binding will go away.
Alter table cmSiteNode add metaInfoContentId INTEGER NULL DEFAULT '-1';
  
update 
cmAvailableServiceBinding asb,
cmServiceBinding sb,
cmQualifyer q,
cmSiteNodeVersion snv,
cmSiteNode sn
set sn.metaInfoContentId = q.value
where
asb.availableServiceBindingId = sb.availableServiceBindingId AND
asb.name = "Meta information" AND
snv.siteNodeVersionId = sb.siteNodeVersionId AND
sb.serviceBindingId = q.serviceBindingId AND
snv.siteNodeId = sn.siteNodeId;  