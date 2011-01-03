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
-- This script contains the database updates required to go from 1.3 to 2.0.
----------------------------------------------------------------------------------
-- Update class names of invokers in cmSiNoTypeDef to reflect that
-- deliver stuff was moved into a separate package.  You will have to restart
-- your web server for the change to be visible in the management tool.
----------------------------------------------------------------------------------
update cmSiNoTypeDef
set invokerClassName = 'org.infoglue.deliver.invokers.ComponentBasedHTMLPageInvoker'
where invokerClassName = 'org.infoglue.cms.invokers.ComponentBasedHTMLPageInvoker';

update cmSiNoTypeDef
set invokerClassName = 'org.infoglue.deliver.invokers.HTMLPageInvoker'
where invokerClassName = 'org.infoglue.cms.invokers.HTMLPageInvoker' OR 
invokerClassName = 'StandardHTMLInvoker' OR
invokerClassName = 'se.sprawl.services.invokers.HtmlInvoker' OR
invokerClassName = 'HTMLInvoker';

----------------------------------------------------------------------------------
-- Add table for Category
----------------------------------------------------------------------------------
DROP SEQUENCE cmCategory_seq;

CREATE SEQUENCE cmCategory_seq START WITH 100000 INCREMENT BY 1;

DROP TABLE cmCategory;

CREATE TABLE cmCategory
(
	categoryId		number NOT NULL,
	name			VARCHAR2(100) NOT NULL,
	description		varchar2(1024),
	active 			number default 1 NOT NULL,
	parentId		number,
	PRIMARY KEY (categoryId)
);

DROP SEQUENCE cmContentCategory_seq;

CREATE SEQUENCE cmContentCategory_seq START WITH 100000 INCREMENT BY 1;

DROP TABLE cmContentCategory;

CREATE TABLE cmContentCategory
(
	contentCategoryId	number NOT NULL,
	attributeName		VARCHAR2(100) NOT NULL,
	ContVerId			number NOT NULL,
	categoryId			number NOT NULL,
	PRIMARY KEY (contentCategoryId)
);

create index contentCategoryAttributeName on cmContentCategory (attributeName);
create index contentCategoryCategoryId on cmContentCategory (categoryId);

create index attributeName_categoryId on cmContentCategory (attributeName, categoryId);
create index contVerId on cmContentCategory (contVerId);


DROP SEQUENCE cmUserPropDigAsset_seq;

CREATE SEQUENCE cmUserPropDigAsset_seq START WITH 100000 INCREMENT BY 1;

DROP TABLE cmUserPropDigAsset;

CREATE TABLE cmUserPropDigAsset (
  userPropDigAssetId number NOT NULL,
  userPropertiesId number NOT NULL,
  digAssetId number NOT NULL,
  PRIMARY KEY  (userPropDigAssetId)
);



DROP SEQUENCE cmRolePropDigAsset_seq;

CREATE SEQUENCE cmRolePropDigAsset_seq START WITH 100000 INCREMENT BY 1;

DROP TABLE cmRolePropDigAsset;

CREATE TABLE cmRolePropDigAsset (
  rolePropDigAssetId number NOT NULL,
  rolePropertiesId number NOT NULL,
  digAssetId number NOT NULL,
  PRIMARY KEY  (rolePropDigAssetId)
);


DROP SEQUENCE cmGroupPropDigAsset_seq;

CREATE SEQUENCE cmGroupPropDigAsset_seq START WITH 100000 INCREMENT BY 1;

DROP TABLE cmGroupPropDigAsset;

CREATE TABLE cmGroupPropDigAsset (
  groupPropDigAssetId number NOT NULL,
  groupPropertiesId number NOT NULL,
  digAssetId number NOT NULL,
  PRIMARY KEY  (groupPropDigAssetId)
);


DROP SEQUENCE cmPropertiesCategory_seq;

CREATE SEQUENCE cmPropertiesCategory_seq START WITH 100000 INCREMENT BY 1;

DROP TABLE cmPropertiesCategory;

CREATE TABLE cmPropertiesCategory
(
	propertiesCategoryId number NOT NULL,
	attributeName		VARCHAR2(100) NOT NULL,
	entityName			VARCHAR2(100) NOT NULL,
	entityId			number NOT NULL,
	categoryId			number NOT NULL,
	PRIMARY KEY (propertiesCategoryId)
);


DROP SEQUENCE cmRegistry_seq;

CREATE SEQUENCE cmRegistry_seq START WITH 100000 INCREMENT BY 1;

DROP TABLE cmRegistry;

CREATE TABLE cmRegistry
(
	registryId		            number NOT NULL,
	entityName		            VARCHAR2(100) NOT NULL,
	entityId		            VARCHAR2(200) NOT NULL,
	referenceType	            number NOT NULL,
	referencingEntityName		VARCHAR2(100) NOT NULL,
	referencingEntityId		    VARCHAR2(200) NOT NULL,
	referencingEntityComplName	VARCHAR2(100) NOT NULL,
	referencingEntityComplId	VARCHAR2(200) NOT NULL,
    PRIMARY KEY (registryId)
);


DROP TABLE cmGroup;

CREATE TABLE cmGroup (
  groupName varchar2(255) NOT NULL,
  description varchar2(1024) NOT NULL,
  PRIMARY KEY  (groupName)
);


DROP SEQUENCE cmGroupContTypeDef_seq;

CREATE SEQUENCE cmGroupContTypeDef_seq START WITH 100000 INCREMENT BY 1;

DROP TABLE cmGroupContTypeDef;

CREATE TABLE cmGroupContTypeDef (
  groupContTypeDefId number NOT NULL,
  groupName varchar2(255) NOT NULL,
  contentTypeDefId integer default 0 NOT NULL,
  PRIMARY KEY  (groupContTypeDefId)
);


DROP SEQUENCE cmGroupProperties_seq;

CREATE SEQUENCE cmGroupProperties_seq START WITH 100000 INCREMENT BY 1;

DROP TABLE cmGroupProperties;

CREATE TABLE cmGroupProperties (
  groupPropertiesId number NOT NULL,
  groupName varchar2(255) NOT NULL,
  contentTypeDefId integer default 0 NOT NULL,
  value clob NOT NULL,
  languageId number NOT NULL,
  PRIMARY KEY  (groupPropertiesId)
);


DROP SEQUENCE cmSystemUserGroup_seq;

CREATE SEQUENCE cmSystemUserGroup_seq START WITH 100000 INCREMENT BY 1;

DROP TABLE cmSystemUserGroup;

CREATE TABLE cmSystemUserGroup (
  userName varchar2(100) NOT NULL,
  groupName varchar2(200) NOT NULL,
  PRIMARY KEY  (userName,groupName)
);


DROP SEQUENCE cmAccessRightRole_seq;

CREATE SEQUENCE cmAccessRightRole_seq START WITH 100000 INCREMENT BY 1;

DROP TABLE cmAccessRightRole;

CREATE TABLE cmAccessRightRole (
  accessRightRoleId number NOT NULL,
  accessRightId number NOT NULL,
  roleName varchar2(150) NOT NULL,
  PRIMARY KEY  (accessRightRoleId)
);


DROP SEQUENCE cmAccessRightGroup_seq;

CREATE SEQUENCE cmAccessRightGroup_seq START WITH 100000 INCREMENT BY 1;

DROP TABLE cmAccessRightGroup;

CREATE TABLE cmAccessRightGroup (
  accessRightGroupId number NOT NULL,
  accessRightId number NOT NULL,
  groupName varchar2(150) NOT NULL,
  PRIMARY KEY  (accessRightGroupId)
);


DROP SEQUENCE cmWorkflowDefinition_seq;

CREATE SEQUENCE cmWorkflowDefinition_seq START WITH 100000 INCREMENT BY 1;

DROP TABLE cmWorkflowDefinition;

CREATE TABLE cmWorkflowDefinition (
  workflowDefinitionId number NOT NULL,
  name varchar2(100) NOT NULL,
  value clob NOT NULL,
  PRIMARY KEY  (workflowDefinitionId)
);


DROP SEQUENCE cmInfoGlueProperties_seq;

CREATE SEQUENCE cmInfoGlueProperties_seq START WITH 100000 INCREMENT BY 1;

DROP TABLE cmInfoGlueProperties;

CREATE TABLE cmInfoGlueProperties (
  propertyId number NOT NULL,
  name varchar2(100) NOT NULL,
  value varchar2(1024) NOT NULL,
  PRIMARY KEY  (propertyId)
);

INSERT INTO cmInfoGlueProperties(propertyId, name, value) VALUES
  (1, 'version', '2.0RC1');

CREATE INDEX "qualifyerServBindIdINDEX" ON cmQualifyer(servBindId);
CREATE INDEX "servBindServDefIdINDEX" ON cmServBind(servDefId);
CREATE INDEX "servBindAvailServBindIdINDEX" ON cmServBind(availServBindId);
CREATE INDEX "servBindSiteNodeVerIdINDEX" ON cmServBind(siNoVerId);
CREATE INDEX "contTypeNameINDEX" ON cmContentTypeDef(name);
CREATE INDEX "contentVersionContentIdINDEX" ON cmContVer(contId);
CREATE INDEX "siteNodeVerSiteNodeIdINDEX" ON cmSiNoVer(siNoId);
CREATE INDEX "contentTypeDefinitionIdINDEX" ON cmCont(contentTypeDefId);
CREATE INDEX "parentContentIdINDEX" ON cmCont(parentContId);
CREATE INDEX "publicationIdINDEX" ON cmPublicationDetail(publicationId);

----------------------------------------------------------------------------------
-- Added sort possibility to repository languages
----------------------------------------------------------------------------------

ALTER TABLE cmRepositoryLanguage ADD sortOrder integer default 0 NOT NULL;


----------------------------------------------------------------------------------
-- Add new interception point for content type definitions                      --
----------------------------------------------------------------------------------
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (27,'ContentTypeDefinition','ContentTypeDefinition.Read','This point checks access to read/use a content type definition',1);
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (27, 1);
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (28,'Category','Category.Read','This point checks access to read/use a category',1);
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (28, 1);
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (29,'Publication','Publication.Write','This point intercepts a new publication',1);
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (29, 1);
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (30,'Repository','Repository.ReadForBinding','This point intercepts when a user tries to read the repository in a binding dialog',1);
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (30, 1);
