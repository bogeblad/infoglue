


CREATE TABLE cmAvailableServiceBinding (
  availableServiceBindingId [int] IDENTITY (1, 1) NOT NULL,
  name varchar(100) NOT NULL,
  description varchar(255) NOT NULL,
  visualizationAction varchar(100) NOT NULL,
  isMandatory tinyint NOT NULL default '0',
  isUserEditable tinyint NOT NULL default '0',
  isInheritable tinyint NOT NULL default '0',
  PRIMARY KEY  (availableServiceBindingId)
)





CREATE TABLE cmAvailableServiceBindingSiteNodeTypeDefinition (
  availableServiceBindingSiteNodeTypeDefinitionId [int] IDENTITY (1, 1) NOT NULL,
  availableServiceBindingId integer NOT NULL default '0',
  siteNodeTypeDefinitionId integer NOT NULL default '0',
  PRIMARY KEY  (availableServiceBindingSiteNodeTypeDefinitionId)
)





CREATE TABLE cmContent (
  contentId [int] IDENTITY (1, 1) NOT NULL,
  name varchar(100) NOT NULL,
  publishDateTime datetime NOT NULL default '0000-00-00 00:00:00',
  expireDateTime datetime NOT NULL default '0000-00-00 00:00:00',
  contentTypeDefinitionId integer default NULL,
  parentContentId integer default NULL,
  creator varchar(255) NOT NULL,
  repositoryId integer NOT NULL default '0',
  isBranch tinyint NOT NULL default '0',
  isProtected tinyint NOT NULL default '0',
  isDeleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY  (contentId)
)





CREATE TABLE cmContentRelation (
  contentRelationId [int] IDENTITY (1, 1) NOT NULL,
  relationInternalname varchar(100) NOT NULL,
  relationTypeId integer NOT NULL default '0',
  sourceContentId integer NOT NULL default '0',
  destinationContentId integer NOT NULL default '0',
  PRIMARY KEY  (contentRelationId)
)





CREATE TABLE cmContentTypeDefinition (
  contentTypeDefinitionId [int] IDENTITY (1, 1) NOT NULL,
  name varchar(100) NOT NULL,
  schemaValue ntext NOT NULL,
  parentContentTypeDefinitionId integer DEFAULT '-1',
  detailPageResolverClass varchar(255) DEFAULT '',
  detailPageResolverData varchar(1024) DEFAULT '',
  type integer NOT NULL default '0',
  PRIMARY KEY  (contentTypeDefinitionId)
)





CREATE TABLE cmContentVersion (
  contentVersionId [int] IDENTITY (1, 1) NOT NULL,
  stateId tinyint NOT NULL default '0',
  modifiedDateTime datetime NOT NULL default '0000-00-00 00:00:00',
  versionComment varchar(1024) NOT NULL,
  isCheckedOut tinyint NOT NULL default '0',
  isActive tinyint NOT NULL default '1',
  contentId integer NOT NULL default '0',
  languageId integer NOT NULL default '0',
  versionModifier varchar(255) NOT NULL,
  versionValue ntext NOT NULL,
  PRIMARY KEY  (contentVersionId)
) 





CREATE TABLE cmContentVersionDigitalAsset (
  contentVersionDigitalAssetId [int] IDENTITY (1, 1) NOT NULL,
  contentVersionId integer NOT NULL default '0',
  digitalAssetId integer NOT NULL default '0',
  PRIMARY KEY  (contentVersionDigitalAssetId)
)





CREATE TABLE cmDigitalAsset (
  digitalAssetId [int] IDENTITY (1, 1) NOT NULL,
  assetKey varchar(255) NOT NULL,
  assetFilename varchar(1024) NOT NULL,
  assetFilePath varchar(1024) NOT NULL,
  assetFileSize integer NOT NULL default '0',
  assetContentType varchar(255) NOT NULL,
  assetBlob image,
  PRIMARY KEY  (digitalAssetId)
)





CREATE TABLE cmLanguage (
  languageId [int] IDENTITY (1, 1) NOT NULL,
  name varchar(100) NOT NULL,
  languageCode varchar(5) NOT NULL,
  charset varchar(30) NOT NULL,
  PRIMARY KEY  (languageId)
)





CREATE TABLE cmPublication (
  publicationId [int] IDENTITY (1, 1) NOT NULL,
  name varchar(100) NOT NULL,
  description varchar(255) NOT NULL,
  publicationDateTime datetime NOT NULL default '0000-00-00 00:00:00',
  publisher varchar(255) NOT NULL,
  repositoryId integer NOT NULL default '0',
  PRIMARY KEY  (publicationId)
)





CREATE TABLE cmQualifyer (
  qualifyerId [int] IDENTITY (1, 1) NOT NULL,
  name varchar(100) NOT NULL,
  value varchar(100) NOT NULL,
  sortOrder integer NOT NULL default '0',
  serviceBindingId integer NOT NULL default '0',
  PRIMARY KEY  (qualifyerId)
)





CREATE TABLE cmRepository (
  repositoryId [int] IDENTITY (1, 1) NOT NULL,
  name varchar(100) NOT NULL,
  description varchar(255) NOT NULL,
  dnsName varchar(255) NOT NULL,
  isDeleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY  (repositoryId)
)





CREATE TABLE cmRepositoryContentTypeDefinition (
  repositoryContentTypeDefinitionId [int] IDENTITY (1, 1) NOT NULL,
  repositoryId integer NOT NULL default '0',
  contentTypeDefinitionId integer NOT NULL default '0',
  PRIMARY KEY  (repositoryContentTypeDefinitionId)
)





CREATE TABLE cmRepositoryLanguage (
  repositoryLanguageId [int] IDENTITY (1, 1) NOT NULL,
  repositoryId integer NOT NULL default '0',
  languageId integer NOT NULL default '0',
  isPublished tinyint NOT NULL default '0',
  sortOrder tinyint NOT NULL default '0',
  PRIMARY KEY  (repositoryLanguageId)
)





CREATE TABLE cmRole (
  roleName varchar(100) NOT NULL,
  description varchar(255) NOT NULL,
  PRIMARY KEY  (roleName)
)





CREATE TABLE cmServiceBinding (
  serviceBindingId [int] IDENTITY (1, 1) NOT NULL,
  name varchar(100) NOT NULL,
  path varchar(255) NOT NULL,
  bindingTypeId integer NOT NULL default '0',
  serviceDefinitionId integer NOT NULL default '0',
  availableServiceBindingId integer NOT NULL default '0',
  siteNodeVersionId integer NOT NULL default '0',
  PRIMARY KEY  (serviceBindingId)
)





CREATE TABLE cmServiceDefinition (
  serviceDefinitionId [int] IDENTITY (1, 1) NOT NULL,
  classname varchar(100) NOT NULL,
  name varchar(100) NOT NULL,
  description varchar(255) NOT NULL,
  PRIMARY KEY  (serviceDefinitionId)
)





CREATE TABLE cmServiceDefinitionAvailableServiceBinding (
  serviceDefinitionAvailableServiceBindingId [int] IDENTITY (1, 1) NOT NULL,
  serviceDefinitionId integer NOT NULL default '0',
  availableServiceBindingId integer NOT NULL default '0',
  PRIMARY KEY  (serviceDefinitionAvailableServiceBindingId)
)





CREATE TABLE cmSiteNode (
  siteNodeId [int] IDENTITY (1, 1) NOT NULL,
  name varchar(100) NOT NULL,
  publishDateTime datetime NOT NULL default '0000-00-00 00:00:00',
  expireDateTime datetime NOT NULL default '0000-00-00 00:00:00',
  parentSiteNodeId integer default NULL,
  creator varchar(255) NOT NULL,
  repositoryId integer NOT NULL default '0',
  siteNodeTypeDefinitionId integer default '0',
  isBranch tinyint NOT NULL default '0',
  metaInfoContentId integer NULL default '-1',
  isDeleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY  (siteNodeId)
)





CREATE TABLE cmSiteNodeTypeDefinition (
  siteNodeTypeDefinitionId [int] IDENTITY (1, 1) NOT NULL,
  invokerClassname varchar(100) NOT NULL,
  name varchar(100) NOT NULL,
  description varchar(255) NOT NULL,
  PRIMARY KEY  (siteNodeTypeDefinitionId)
)





CREATE TABLE cmSiteNodeVersion (
  siteNodeVersionId [int] IDENTITY (1, 1) NOT NULL,
  stateId tinyint NOT NULL default '0',
  versionNumber integer NOT NULL default '0',
  modifiedDateTime datetime NOT NULL default '0000-00-00 00:00:00',
  versionComment varchar(1024) NOT NULL,
  isCheckedOut tinyint NOT NULL default '0',
  isActive tinyint NOT NULL default '1',
  siteNodeId integer NOT NULL default '0',
  versionModifier varchar(255) NOT NULL,
  isProtected tinyint NOT NULL default '2',
  disablePageCache tinyint NOT NULL default '2',
  disableEditOnSight tinyint NOT NULL default '2',
  disableLanguages tinyint NOT NULL default '2',
  disableForceIDCheck tinyint NOT NULL default '2',
  forceProtocolChange tinyint NOT NULL default '0',
  contentType varchar(100),
  pageCacheKey varchar(255) NOT NULL default 'default',
  pageCacheTimeout varchar(20) default NULL,
  sortOrder INTEGER NOT NULL DEFAULT -1,
  isHidden TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY  (siteNodeVersionId)
)





CREATE TABLE cmSystemUser (
  userName varchar(100) NOT NULL,
  password varchar(100) NOT NULL,
  firstName varchar(100) NOT NULL,
  lastName varchar(100) NOT NULL,
  email varchar(255) NOT NULL,
  PRIMARY KEY  (userName)
)





CREATE TABLE cmSystemUserRole (
  userName varchar(100) NOT NULL,
  roleName varchar(100) NOT NULL,
  PRIMARY KEY  (userName, roleName)
)





CREATE TABLE cmTransactionHistory (
  transactionHistoryId [int] IDENTITY (1, 1) NOT NULL,
  name varchar(200) NOT NULL,
  transactionDateTime datetime NOT NULL default '0000-00-00 00:00:00',
  transactionTypeId integer NOT NULL default '0',
  transactionObjectId varchar(255) NOT NULL default '0',
  transactionObjectname varchar(255) NOT NULL,
  systemUserName varchar(100) NOT NULL,
  PRIMARY KEY  (transactionHistoryId)
)





CREATE TABLE cmPublicationDetail (
  publicationDetailId [int] IDENTITY (1, 1) NOT NULL,
  publicationId integer NOT NULL default '0',
  name varchar(100) NOT NULL,
  description varchar(255) NOT NULL,
  entityClass varchar(255) NOT NULL,
  entityId integer NOT NULL default '0',
  creationDateTime datetime NOT NULL,
  typeId integer NOT NULL default '0',
  publisher varchar(255) NOT NULL,
  PRIMARY KEY  (publicationDetailId)
)





CREATE TABLE cmEvent (
  eventId [int] IDENTITY (1, 1) NOT NULL,
  repositoryId integer NOT NULL default '0',
  name varchar(100) NOT NULL,
  description varchar(255) NOT NULL,
  entityClass varchar(255) NOT NULL,
  entityId integer NOT NULL default '0',
  creationDateTime datetime NOT NULL,
  typeId integer NOT NULL default '0',
  creator varchar(255) NOT NULL,
  PRIMARY KEY  (eventId)
)





CREATE TABLE cmRoleContentTypeDefinition (
  roleContentTypeDefinitionId [int] IDENTITY (1, 1) NOT NULL,
  roleName varchar(100) NOT NULL,
  contentTypeDefinitionId integer NOT NULL default '0',
  PRIMARY KEY  (roleContentTypeDefinitionId)
)





CREATE TABLE cmRoleProperties (
  rolePropertiesId [int] IDENTITY (1, 1) NOT NULL,
  roleName varchar(100) NOT NULL,
  contentTypeDefinitionId integer NOT NULL default '0',
  value ntext NOT NULL,
  languageId integer NOT NULL default '0',
  PRIMARY KEY  (rolePropertiesId)
)




CREATE TABLE cmRolePropertiesDigitalAsset (
  rolePropertiesDigitalAssetId [int] IDENTITY (1, 1) NOT NULL,
  rolePropertiesId integer NOT NULL default '0',
  digitalAssetId integer NOT NULL default '0',
  PRIMARY KEY  (rolePropertiesDigitalAssetId)
)





CREATE TABLE cmUserContentTypeDefinition (
  userContentTypeDefinitionId [int] IDENTITY (1, 1) NOT NULL,
  userName varchar(100) NOT NULL,
  contentTypeDefinitionId integer NOT NULL default '0',
  PRIMARY KEY  (userContentTypeDefinitionId)
)





CREATE TABLE cmUserProperties (
  userPropertiesId [int] IDENTITY (1, 1) NOT NULL,
  userName varchar(100) NOT NULL,
  contentTypeDefinitionId integer NOT NULL default '0',
  value ntext NOT NULL,
  languageId integer NOT NULL default '0',
  PRIMARY KEY  (userPropertiesId)
)




CREATE TABLE cmUserPropertiesDigitalAsset (
  userPropertiesDigitalAssetId [int] IDENTITY (1, 1) NOT NULL,
  userPropertiesId integer NOT NULL default '0',
  digitalAssetId integer NOT NULL default '0',
  PRIMARY KEY  (userPropertiesDigitalAssetId)
)





CREATE TABLE cmAccessRight (
  accessRightId [int] IDENTITY (1, 1) NOT NULL,
  parameters varchar(1024) NULL,
  interceptionPointId integer NOT NULL default '0',
  PRIMARY KEY  (accessRightId)
)





CREATE TABLE cmInterceptionPoint (
  interceptionPointId [int] IDENTITY (1, 1) NOT NULL,
  category varchar(100) NOT NULL,
  name varchar(100) NOT NULL,
  description varchar(1024) NOT NULL,
  usesExtraDataForAccessControl integer NOT NULL default '0',
  PRIMARY KEY  (interceptionPointId)
)





CREATE TABLE cmInterceptionPointInterceptor (
  interceptionPointId integer NOT NULL default '0',
  interceptorId integer NOT NULL default '0',
  PRIMARY KEY  (interceptionPointId, interceptorId)
)





CREATE TABLE cmInterceptor (
  interceptorId [int] IDENTITY (1, 1) NOT NULL,
  name varchar(255) NOT NULL,
  className varchar(255) NOT NULL,
  description varchar(1024) NOT NULL,
  PRIMARY KEY  (interceptorId)
)


CREATE TABLE OS_PROPERTYENTRY
(
	entity_name varchar(125) not null,
	entity_id int not null default (0),
	entity_key varchar(150) not null,
	key_type int,
	boolean_val tinyint,
	string_val varchar(200),
	long_val int,
	date_val datetime,
	data_val image,
	double_val NUMERIC,
	int_val int,
	primary key (entity_name, entity_id, entity_key)
);


create table OS_WFENTRY
(
    ID [int] IDENTITY (1, 1) NOT NULL,
    NAME varchar(128),
    STATE smallint,
    primary key (ID)
);

create table OS_CURRENTSTEP
(
    ID [int] IDENTITY (1, 1) NOT NULL,
    ENTRY_ID int,
    STEP_ID smallint,
    ACTION_ID smallint,
    OWNER varchar(255),
    START_DATE datetime,
    FINISH_DATE datetime,
    DUE_DATE datetime,
    STATUS varchar(20),
    CALLER varchar(255),
    stepIndex int default '0',
    primary key (ID),
    foreign key (ENTRY_ID) references OS_WFENTRY(ID)
);

create table OS_HISTORYSTEP
(
    ID [int] IDENTITY (1, 1) NOT NULL,
    ENTRY_ID int,
    STEP_ID smallint,
    ACTION_ID smallint,
    OWNER varchar(255),
    START_DATE datetime,
    FINISH_DATE datetime,
    DUE_DATE datetime,
    STATUS varchar(20),
    CALLER varchar(255),
    stepIndex int default '0',
    primary key (ID),
    foreign key (ENTRY_ID) references OS_WFENTRY(ID)
);

create table OS_CURRENTSTEP_PREV
(
    ID [int] IDENTITY (1, 1) NOT NULL,
    PREVIOUS_ID int,
    primary key (ID, PREVIOUS_ID),
    foreign key (ID) references OS_CURRENTSTEP(ID),
    foreign key (PREVIOUS_ID) references OS_HISTORYSTEP(ID)
);

create table OS_HISTORYSTEP_PREV
(
    ID [int] IDENTITY (1, 1) NOT NULL,
    PREVIOUS_ID int,
    primary key (ID, PREVIOUS_ID),
    foreign key (ID) references OS_HISTORYSTEP(ID),
    foreign key (PREVIOUS_ID) references OS_HISTORYSTEP(ID)
);

 



CREATE TABLE cmCategory
(
  categoryId [int] IDENTITY (1, 1) NOT NULL,
  name varchar(100) NOT NULL,
  displayName varchar(4096),
  description varchar(255) NOT NULL,
  active integer NOT NULL default '1',
  parentId integer,
  PRIMARY KEY (categoryId)
)




CREATE TABLE cmContentCategory
(
	contentCategoryId	[int] IDENTITY (1, 1) NOT NULL,
	attributeName		VARCHAR(100) NOT NULL,
	contentVersionId	INTEGER NOT NULL,
	categoryId		INTEGER NOT NULL,
	PRIMARY KEY (contentCategoryId)
)





CREATE TABLE cmGroupPropertiesDigitalAsset (
  groupPropertiesDigitalAssetId [int] IDENTITY (1, 1) NOT NULL,
  groupPropertiesId integer NOT NULL default '0',
  digitalAssetId integer NOT NULL default '0',
  PRIMARY KEY  (groupPropertiesDigitalAssetId)
)





CREATE TABLE cmPropertiesCategory
(
	propertiesCategoryId	[int] IDENTITY (1, 1) NOT NULL,
	attributeName		VARCHAR(100) NOT NULL,
	entityName			VARCHAR(100) NOT NULL,
	entityId			integer NOT NULL,
	categoryId			integer NOT NULL,
	PRIMARY KEY (propertiesCategoryId)
)





CREATE TABLE cmRegistry
(
	registryId		            [int] IDENTITY (1, 1) NOT NULL,
	entityName		            VARCHAR(100) NOT NULL,
	entityId		            VARCHAR(200) NOT NULL,
	referenceType	            integer NOT NULL,
	referencingEntityName		VARCHAR(100) NOT NULL,
	referencingEntityId		    VARCHAR(200) NOT NULL,
	referencingEntityComplName	VARCHAR(100) NOT NULL,
	referencingEntityComplId	VARCHAR(200) NOT NULL,
    PRIMARY KEY (registryId)
)



  
CREATE TABLE cmGroup (
  groupName varchar(255) NOT NULL default '',
  description varchar(255) NOT NULL,
  PRIMARY KEY  (groupName)
)




CREATE TABLE cmGroupContentTypeDefinition (
  groupContentTypeDefinitionId [int] IDENTITY (1, 1) NOT NULL,
  groupName varchar(255) NOT NULL,
  contentTypeDefinitionId integer NOT NULL default '0',
  PRIMARY KEY  (groupContentTypeDefinitionId)
)





CREATE TABLE cmGroupProperties (
  groupPropertiesId [int] IDENTITY (1, 1) NOT NULL,
  groupName varchar(255) NOT NULL,
  contentTypeDefinitionId integer NOT NULL default '0',
  value ntext NOT NULL,
  languageId integer NOT NULL default '0',
  PRIMARY KEY  (groupPropertiesId)
)




CREATE TABLE cmSystemUserGroup (
  userName varchar(100) NOT NULL default '',
  groupName varchar(200) NOT NULL default '',
  PRIMARY KEY  (userName,groupName)
)




CREATE TABLE cmAccessRightRole (
  accessRightRoleId [int] IDENTITY (1, 1) NOT NULL,
  accessRightId integer NOT NULL default '0',
  roleName varchar(150) NOT NULL default '',
  PRIMARY KEY  (accessRightRoleId)
)




CREATE TABLE cmAccessRightGroup (
  accessRightGroupId [int] IDENTITY (1, 1) NOT NULL,
  accessRightId integer NOT NULL default '0',
  groupName varchar(150) NOT NULL default '',
  PRIMARY KEY  (accessRightGroupId)
)





CREATE TABLE cmWorkflowDefinition (
  workflowDefinitionId [int] IDENTITY (1, 1) NOT NULL,
  name varchar(100) NOT NULL,
  value ntext NOT NULL,
  PRIMARY KEY  (workflowDefinitionId)
)




CREATE TABLE cmRedirect
(
	id			[int] IDENTITY (1, 1) NOT NULL,
	url			VARCHAR(1024) NOT NULL,
	redirectUrl	VARCHAR(1024) NOT NULL,
	createdDateTime datetime NOT NULL default '2001-01-01 01:01:01',
	publishDateTime datetime NOT NULL default '2001-01-01 01:01:01',
	expireDateTime datetime NOT NULL default '2050-01-01 01:01:01',
	modifier VARCHAR(1024) NOT NULL default 'system',
	isUserManaged INTEGER NOT NULL DEFAULT '1',
	PRIMARY KEY (id)
)  

CREATE TABLE cmAccessRightUser
(
	accessRightUserId	[int] IDENTITY (1, 1) NOT NULL,
	accessRightId		[int] NOT NULL default '0',
	userName	VARCHAR(150) NOT NULL default '',
  	PRIMARY KEY (accessRightUserId)
)  

CREATE TABLE cmServerNode
(
	serverNodeId	[int] IDENTITY (1, 1) NOT NULL,
	name	VARCHAR(255) NOT NULL,
	description	VARCHAR(1024) NOT NULL,
	dnsName	VARCHAR(255) NOT NULL,
  	PRIMARY KEY (serverNodeId)
)  




CREATE TABLE  cmFormEntry (
  id [int] IDENTITY (1, 1) NOT NULL,
  originAddress varchar(1024) NOT NULL,
  formName varchar(255) NOT NULL,
  formContentId int NOT NULL,
  userIP varchar(20) NOT NULL,
  userAgent varchar(255) NOT NULL,
  PRIMARY KEY  (id)
)
 


 
CREATE TABLE  cmFormEntryValue (
  id [int] IDENTITY (1, 1) NOT NULL,
  name varchar(128) NOT NULL,
  value varchar(4096),
  formEntryId int NOT NULL,
  PRIMARY KEY  (id)
)
 


 
CREATE TABLE cmFormEntryAsset (
  id [int] IDENTITY (1, 1),
  formEntryId int NOT NULL,
  fileName VARCHAR(255) NOT NULL,
  fileSize int NOT NULL,
  assetKey VARCHAR(255) NOT NULL,
  contentType VARCHAR(50) NOT NULL,
  assetBlob image,
  PRIMARY KEY  (id)
)



 
CREATE TABLE cmSubscription (
  id [int] IDENTITY (1, 1) NOT NULL,
  interceptionPointId int NOT NULL,
  name varchar(100) NOT NULL,
  isGlobal tinyint NOT NULL default '0',
  entityName varchar(100) default NULL,
  entityId varchar(200) default NULL,
  userName varchar(150) NOT NULL,
  userEmail varchar(150),
  lastNotifiedDateTime datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY(id)
)
 


 
CREATE TABLE cmSubscriptionFilter (
  id [int] IDENTITY (1, 1) NOT NULL,
  subscriptionId int NOT NULL,
  filterType varchar(50) NOT NULL,
  filterCondition varchar(255) NOT NULL,
  isAndCondition tinyint NOT NULL default '1',
  PRIMARY KEY(id)
)




CREATE TABLE cmInfoGlueProperties (
  propertyId [int] IDENTITY (1, 1) NOT NULL,
  name varchar(100) NOT NULL,
  value ntext NOT NULL,
  PRIMARY KEY  (propertyId)
)

INSERT INTO cmInfoGlueProperties(name, value) VALUES
  ('version', '3.0');

CREATE INDEX serviceBindingId ON cmQualifyer(serviceBindingId);
CREATE INDEX serviceDefinitionId ON cmServiceBinding(serviceDefinitionId);
CREATE INDEX availableServiceBindingId ON cmServiceBinding(availableServiceBindingId);
CREATE INDEX siteNodeVersionId ON cmServiceBinding(siteNodeVersionId);
CREATE INDEX name ON cmContentTypeDefinition(name);
CREATE INDEX contentId ON cmContentVersion(contentId);
CREATE INDEX contentTypeDefinitionId ON cmContent(contentTypeDefinitionId);
CREATE INDEX siteNodeId ON cmSiteNodeVersion(siteNodeId);
CREATE INDEX parentContentId ON cmContent (parentContentId);
CREATE INDEX publicationId ON cmPublicationDetail (publicationId);
create index attributeName_categoryId on cmContentCategory (attributeName, categoryId);
create index contentVersionId on cmContentCategory (contentVersionId);

CREATE INDEX OWNER ON OS_CURRENTSTEP(OWNER);
CREATE INDEX CALLER ON OS_CURRENTSTEP(CALLER);
CREATE INDEX OWNER ON OS_HISTORYSTEP(OWNER);
CREATE INDEX CALLER ON OS_HISTORYSTEP(CALLER);

CREATE INDEX referencingEntityName ON cmRegistry(referencingEntityName);
CREATE INDEX referencingEntityId ON cmRegistry(referencingEntityId);
CREATE INDEX entityName ON cmRegistry(entityName);
CREATE INDEX entityId ON cmRegistry(entityId);
CREATE INDEX referencingEntityComplName ON cmRegistry(referencingEntityComplName);
CREATE INDEX referencingEntityComplId ON cmRegistry(referencingEntityComplId);
CREATE INDEX categoryContVersionId ON cmContentCategory(contentVersionId);
CREATE INDEX contVerDigAssetDigAssId ON cmContentVersionDigitalAsset(digitalAssetId);
CREATE INDEX contVerDigAssetContVerId ON cmContentVersionDigitalAsset(contentVersionId);

