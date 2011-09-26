CREATE TABLE cmAvailableServiceBinding (
  availableServiceBindingId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  visualizationAction text NOT NULL,
  isMandatory tinyint(4) NOT NULL default '0',
  isUserEditable tinyint(4) NOT NULL default '0',
  isInheritable tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (availableServiceBindingId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;


CREATE TABLE cmAvailableServiceBindingSiteNodeTypeDefinition (
  availableServiceBindingSiteNodeTypeDefinitionId integer(11) unsigned NOT NULL auto_increment,
  availableServiceBindingId integer(11) NOT NULL default '0',
  siteNodeTypeDefinitionId integer(11) NOT NULL default '0',
  PRIMARY KEY  (availableServiceBindingSiteNodeTypeDefinitionId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmContent (
  contentId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  publishDateTime datetime NOT NULL default '1970-01-01 12:00:00',
  expireDateTime datetime NOT NULL default '2070-01-01 12:00:00',
  contentTypeDefinitionId integer(11) default NULL,
  parentContentId integer(11) default NULL,
  creator TEXT NOT NULL,
  repositoryId integer(11) NOT NULL default '0',
  isBranch tinyint(4) NOT NULL default '0',
  isProtected tinyint(4) NOT NULL default '2',
  isDeleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY  (contentId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmContentRelation (
  contentRelationId integer(11) unsigned NOT NULL auto_increment,
  relationInternalName text NOT NULL,
  relationTypeId integer(11) NOT NULL default '0',
  sourceContentId integer(11) NOT NULL default '0',
  destinationContentId integer(11) NOT NULL default '0',
  PRIMARY KEY  (contentRelationId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmContentTypeDefinition (
  contentTypeDefinitionId integer(11) unsigned NOT NULL auto_increment,
  schemaValue longtext NOT NULL,
  name varchar(255) NOT NULL,
  parentContentTypeDefinitionId INT DEFAULT '-1',
  detailPageResolverClass VARCHAR(255) DEFAULT '',
  detailPageResolverData VARCHAR(1024) DEFAULT '',
  type tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (contentTypeDefinitionId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmContentVersion (
  contentVersionId integer(11) unsigned NOT NULL auto_increment,
  stateId tinyint(4) NOT NULL default '0',
  versionValue longtext NOT NULL,
  modifiedDateTime datetime NOT NULL default '1970-01-01 12:00:00',
  versionComment text NOT NULL,
  isCheckedOut tinyint(4) NOT NULL default '0',
  isActive tinyint(4) NOT NULL default '1',
  contentId integer(11) NOT NULL default '0',
  languageId integer(11) NOT NULL default '0',
  versionModifier text NOT NULL,
  PRIMARY KEY  (contentVersionId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmContentVersionDigitalAsset (
  contentVersionDigitalAssetId integer(11) unsigned NOT NULL auto_increment,
  contentVersionId integer(11) unsigned NOT NULL default '0',
  digitalAssetId integer(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (contentVersionDigitalAssetId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmUserPropertiesDigitalAsset (
  userPropertiesDigitalAssetId integer(11) unsigned NOT NULL auto_increment,
  userPropertiesId integer(11) unsigned NOT NULL default '0',
  digitalAssetId integer(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (userPropertiesDigitalAssetId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmRolePropertiesDigitalAsset (
  rolePropertiesDigitalAssetId integer(11) unsigned NOT NULL auto_increment,
  rolePropertiesId integer(11) unsigned NOT NULL default '0',
  digitalAssetId integer(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (rolePropertiesDigitalAssetId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmDigitalAsset (
  digitalAssetId integer(11) unsigned NOT NULL auto_increment,
  assetKey text NOT NULL,
  assetFileName text NOT NULL,
  assetFilePath text NOT NULL,
  assetFileSize int(11) NOT NULL default '0',
  assetContentType varchar(255) NOT NULL,
  assetBlob longblob,
  PRIMARY KEY  (digitalAssetId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmLanguage (
  languageId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  languageCode text NOT NULL,
  charset text NOT NULL,
  PRIMARY KEY  (languageId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmPublication (
  publicationId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  publicationDateTime datetime NOT NULL default '1970-01-01 12:00:00',
  publisher text NOT NULL,
  repositoryId integer(11) NOT NULL default '0',
  PRIMARY KEY  (publicationId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmQualifyer (
  qualifyerId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  value text NOT NULL,
  sortOrder integer(11) NOT NULL default '0',
  serviceBindingId integer(11) NOT NULL default '0',
  PRIMARY KEY  (qualifyerId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmRepository (
  repositoryId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  dnsName text NOT NULL,
  isDeleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY  (repositoryId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmRepositoryContentTypeDefinition (
  repositoryContentTypeDefinitionId integer(11) unsigned NOT NULL auto_increment,
  repositoryId integer(11) NOT NULL default '0',
  contentTypeDefinitionId integer(11) NOT NULL default '0',
  PRIMARY KEY  (repositoryContentTypeDefinitionId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmRepositoryLanguage (
  repositoryLanguageId integer(11) unsigned NOT NULL auto_increment,
  repositoryId integer(11) NOT NULL default '0',
  languageId integer(11) NOT NULL default '0',
  isPublished tinyint(4) NOT NULL default '0',
  sortOrder tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (repositoryLanguageId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmRole (
  roleName varchar(200) NOT NULL,
  description text NOT NULL,
  PRIMARY KEY  (roleName)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmServiceBinding (
  serviceBindingId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  path text NOT NULL,
  bindingTypeId integer(11) NOT NULL default '0',
  serviceDefinitionId integer(11) NOT NULL default '0',
  availableServiceBindingId integer(11) NOT NULL default '0',
  siteNodeVersionId integer(11) NOT NULL default '0',
  PRIMARY KEY  (serviceBindingId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmServiceDefinition (
  serviceDefinitionId integer(11) unsigned NOT NULL auto_increment,
  className text NOT NULL,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  PRIMARY KEY  (serviceDefinitionId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmServiceDefinitionAvailableServiceBinding (
  serviceDefinitionAvailableServiceBindingId integer(11) unsigned NOT NULL auto_increment,
  serviceDefinitionId integer(11) NOT NULL default '0',
  availableServiceBindingId integer(11) NOT NULL default '0',
  PRIMARY KEY  (serviceDefinitionAvailableServiceBindingId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmSiteNode (
  siteNodeId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  publishDateTime datetime NOT NULL default '1970-01-01 12:00:00',
  expireDateTime datetime NOT NULL default '2070-01-01 12:00:00',
  parentSiteNodeId integer(11) default NULL,
  creator text NOT NULL,
  repositoryId integer(11) NOT NULL default '0',
  siteNodeTypeDefinitionId integer(11) default '0',
  isBranch tinyint(4) NOT NULL default '0',
  metaInfoContentId INTEGER NULL DEFAULT '-1',
  isDeleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY  (siteNodeId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmSiteNodeTypeDefinition (
  siteNodeTypeDefinitionId integer(11) unsigned NOT NULL auto_increment,
  invokerClassName text NOT NULL,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  PRIMARY KEY  (siteNodeTypeDefinitionId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmSiteNodeVersion (
  siteNodeVersionId integer(11) unsigned NOT NULL auto_increment,
  stateId tinyint(4) NOT NULL default '0',
  versionNumber integer(11) NOT NULL default '0',
  modifiedDateTime datetime NOT NULL default '1970-01-01 12:00:00',
  versionComment text NOT NULL,
  isCheckedOut tinyint(4) NOT NULL default '0',
  isActive tinyint(4) NOT NULL default '1',
  siteNodeId integer(11) NOT NULL default '0',
  versionModifier text NOT NULL,
  isProtected tinyint(4) NOT NULL default '2',
  disablePageCache tinyint(4) NOT NULL default '2',
  disableEditOnSight tinyint(4) NOT NULL default '2',
  disableLanguages tinyint(4) NOT NULL default '2',
  disableForceIDCheck tinyint(4) NOT NULL default '2',
  forceProtocolChange tinyint(4) NOT NULL default '0',
  contentType varchar(255),
  pageCacheKey varchar(255) NOT NULL default 'default',
  pageCacheTimeout varchar(20) default NULL,
  sortOrder INTEGER NOT NULL DEFAULT -1,
  isHidden TINYINT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY  (siteNodeVersionId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmSystemUser (
  userName varchar(200) NOT NULL,
  password varchar(255) NOT NULL,
  firstName text NOT NULL,
  lastName text NOT NULL,
  email text NOT NULL,
  PRIMARY KEY  (userName)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmSystemUserRole (
  userName varchar(100) NOT NULL,
  roleName varchar(200) NOT NULL,
  PRIMARY KEY  (userName, roleName)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmTransactionHistory (
  transactionHistoryId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  transactionDateTime datetime NOT NULL default '1970-01-01 12:00:00',
  transactionTypeId integer(11) NOT NULL default '0',
  transactionObjectId text NOT NULL,
  transactionObjectName text NOT NULL,
  systemUserName text NOT NULL,
  PRIMARY KEY  (transactionHistoryId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmPublicationDetail (
  publicationDetailId integer(11) NOT NULL auto_increment,
  publicationId integer(11) NOT NULL default '0',
  name varchar(255) NOT NULL,
  description text NOT NULL,
  entityClass text NOT NULL,
  entityId integer(11) NOT NULL default '0',
  creationDateTime timestamp(14) NOT NULL,
  typeId integer(11) NOT NULL default '0',
  publisher text NOT NULL,
  PRIMARY KEY  (publicationDetailId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmEvent (
  eventId integer(11) NOT NULL auto_increment,
  repositoryId integer(11) NOT NULL default '0',
  name varchar(255) NOT NULL,
  description text NOT NULL,
  entityClass text NOT NULL,
  entityId integer(11) NOT NULL default '0',
  creationDateTime timestamp(14) NOT NULL,
  typeId integer(11) NOT NULL default '0',
  creator text NOT NULL,
  PRIMARY KEY  (eventId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmRoleContentTypeDefinition (
  roleContentTypeDefinitionId int(11) NOT NULL auto_increment,
  roleName text NOT NULL,
  contentTypeDefinitionId int(11) NOT NULL default '0',
  PRIMARY KEY  (roleContentTypeDefinitionId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmRoleProperties (
  rolePropertiesId int(11) NOT NULL auto_increment,
  roleName text NOT NULL,
  contentTypeDefinitionId int(11) NOT NULL default '0',
  value text NOT NULL,
  languageId int(11) NOT NULL default '0',
  PRIMARY KEY  (rolePropertiesId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmUserContentTypeDefinition (
  userContentTypeDefinitionId int(11) NOT NULL auto_increment,
  userName text NOT NULL,
  contentTypeDefinitionId int(11) NOT NULL default '0',
  PRIMARY KEY  (userContentTypeDefinitionId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmUserProperties (
  userPropertiesId int(11) NOT NULL auto_increment,
  userName text NOT NULL,
  contentTypeDefinitionId int(11) NOT NULL default '0',
  value text NOT NULL,
  languageId int(11) NOT NULL default '0',
  PRIMARY KEY  (userPropertiesId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmAccessRight (
  accessRightId int(11) NOT NULL auto_increment,
  parameters text NULL,
  interceptionPointId int(11) NOT NULL,
  PRIMARY KEY  (accessRightId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmInterceptionPoint (
  interceptionPointId int(11) NOT NULL auto_increment,
  category text NOT NULL,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  usesExtraDataForAccessControl int(11) default '0' NULL,
  PRIMARY KEY  (interceptionPointId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmInterceptionPointInterceptor (
  interceptionPointId int(11) NOT NULL,
  interceptorId int(11) NOT NULL,
  PRIMARY KEY  (interceptionPointId, interceptorId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmInterceptor (
  interceptorId int(11) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  className text NOT NULL,
  description text NOT NULL,
  PRIMARY KEY  (interceptorId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;





CREATE TABLE OS_PROPERTYENTRY
(
	entity_name varchar(125) not null,
	entity_id bigint not null default '0',
	entity_key varchar(150) not null,
	key_type int,
	boolean_val tinyint,
	string_val varchar(200),
	long_val bigint,
	date_val datetime,
	data_val blob,
	double_val double,
	int_val int,
	primary key (entity_name, entity_id, entity_key)
)TYPE=InnoDB;



CREATE TABLE OS_WFENTRY
(
    ID bigint NOT NULL auto_increment,
    NAME varchar(60),
    STATE integer,
    primary key (ID)
)TYPE=InnoDB;



CREATE TABLE OS_CURRENTSTEP
(
    ID bigint NOT NULL auto_increment,
    ENTRY_ID bigint,
    STEP_ID integer,
    ACTION_ID integer,
    OWNER varchar(255),
    START_DATE datetime,
    FINISH_DATE datetime,
    DUE_DATE datetime,
    STATUS varchar(40),
	CALLER varchar(255),
    primary key (ID),
    index (ENTRY_ID),
    foreign key (ENTRY_ID) references OS_WFENTRY(ID),
    index (OWNER),
    index (CALLER),
    stepIndex integer NOT NULL default '0'
)TYPE=InnoDB;


CREATE TABLE OS_HISTORYSTEP
(
    ID bigint NOT NULL auto_increment,
    ENTRY_ID bigint,
    STEP_ID integer,
    ACTION_ID integer,
    OWNER varchar(255),
    START_DATE datetime,
    FINISH_DATE datetime,
    DUE_DATE datetime,
    STATUS varchar(40),
    CALLER varchar(255),
    primary key (ID),
    index (ENTRY_ID),
    foreign key (ENTRY_ID) references OS_WFENTRY(ID),
    index (OWNER),
    index (CALLER),
    stepIndex integer NOT NULL default '0'
)TYPE=InnoDB;


CREATE TABLE OS_CURRENTSTEP_PREV
(
    ID bigint NOT NULL,
    PREVIOUS_ID bigint NOT NULL,
    primary key (ID, PREVIOUS_ID),
    index (ID),
    foreign key (ID) references OS_CURRENTSTEP(ID),
    index (PREVIOUS_ID),
    foreign key (PREVIOUS_ID) references OS_HISTORYSTEP(ID)
)TYPE=InnoDB;


CREATE TABLE OS_HISTORYSTEP_PREV
(
    ID bigint NOT NULL,
    PREVIOUS_ID bigint NOT NULL,
    primary key (ID, PREVIOUS_ID),
    index (ID),
    foreign key (ID) references OS_HISTORYSTEP(ID),
    index (PREVIOUS_ID),
    foreign key (PREVIOUS_ID) references OS_HISTORYSTEP(ID)
)TYPE=InnoDB;


CREATE TABLE OS_STEPIDS
(
	 ID bigint NOT NULL AUTO_INCREMENT,
	 PRIMARY KEY (id)
 )TYPE=InnoDB;
 




CREATE TABLE cmCategory
(
	categoryId		INTEGER(11) unsigned NOT NULL auto_increment,
	name			VARCHAR(100) NOT NULL,
	displayName		VARCHAR(4096),
	description		TEXT,
	active			TINYINT(4) NOT NULL default '1',
	parentId		INTEGER(11),
	PRIMARY KEY (categoryId)
);




CREATE TABLE cmContentCategory
(
	contentCategoryId	INTEGER(11) unsigned NOT NULL auto_increment,
	attributeName		VARCHAR(100) NOT NULL,
	contentVersionId	INTEGER(11) NOT NULL,
	categoryId			INTEGER(11) NOT NULL,
	PRIMARY KEY (contentCategoryId)
);




CREATE TABLE cmGroupPropertiesDigitalAsset (
  groupPropertiesDigitalAssetId integer(11) unsigned NOT NULL auto_increment,
  groupPropertiesId integer(11) unsigned NOT NULL default '0',
  digitalAssetId integer(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (groupPropertiesDigitalAssetId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmPropertiesCategory
(
	propertiesCategoryId	INTEGER(11) unsigned NOT NULL auto_increment,
	attributeName		VARCHAR(100) NOT NULL,
	entityName			VARCHAR(100) NOT NULL,
	entityId			INTEGER(11) NOT NULL,
	categoryId			INTEGER(11) NOT NULL,
	PRIMARY KEY (propertiesCategoryId)
);




CREATE TABLE cmRegistry
(
	registryId		            INTEGER(11) unsigned NOT NULL auto_increment,
	entityName		            VARCHAR(100) NOT NULL,
	entityId		            VARCHAR(200) NOT NULL,
	referenceType	            TINYINT(4) NOT NULL,
	referencingEntityName		 VARCHAR(100) NOT NULL,
	referencingEntityId		     VARCHAR(200) NOT NULL,
	referencingEntityComplName	 VARCHAR(100) NOT NULL,
	referencingEntityComplId	 VARCHAR(200) NOT NULL,
    PRIMARY KEY (registryId)
);



CREATE TABLE cmGroup (
  groupName varchar(255) NOT NULL default '',
  description text NOT NULL,
  PRIMARY KEY  (groupName)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;



CREATE TABLE cmGroupContentTypeDefinition (
  groupContentTypeDefinitionId int(11) NOT NULL auto_increment,
  groupName text NOT NULL,
  contentTypeDefinitionId int(11) NOT NULL default '0',
  PRIMARY KEY  (groupContentTypeDefinitionId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmGroupProperties (
  groupPropertiesId int(11) NOT NULL auto_increment,
  groupName text NOT NULL,
  contentTypeDefinitionId int(11) NOT NULL default '0',
  value text NOT NULL,
  languageId int(11) NOT NULL default '0',
  PRIMARY KEY  (groupPropertiesId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmSystemUserGroup (
  userName varchar(150) NOT NULL default '',
  groupName varchar(150) NOT NULL default '',
  PRIMARY KEY  (userName,groupName)
) CHARACTER SET utf8 COLLATE utf8_general_ci TYPE=InnoDB;




CREATE TABLE cmAccessRightRole (
  accessRightRoleId int(11) NOT NULL auto_increment,
  accessRightId int(11) NOT NULL default '0',
  roleName varchar(150) NOT NULL default '',
  PRIMARY KEY  (accessRightRoleId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;



CREATE TABLE cmAccessRightGroup (
  accessRightGroupId int(11) NOT NULL auto_increment,
  accessRightId int(11) NOT NULL default '0',
  groupName varchar(150) NOT NULL default '',
  PRIMARY KEY  (accessRightGroupId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;



CREATE TABLE cmAccessRightUser (
  accessRightUserId int(11) NOT NULL auto_increment,
  accessRightId int(11) NOT NULL default '0',
  userName varchar(150) NOT NULL default '',
  PRIMARY KEY  (accessRightUserId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;




CREATE TABLE cmWorkflowDefinition (
  workflowDefinitionId int(11) NOT NULL auto_increment,
  name text NOT NULL,
  value text NOT NULL,
  PRIMARY KEY  (workflowDefinitionId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;
 


CREATE TABLE cmRedirect (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  url TEXT NOT NULL,
  redirectUrl TEXT NOT NULL,
  createdDateTime datetime,
  publishDateTime datetime,
  expireDateTime datetime,
  modifier TEXT,
  isUserManaged TINYINT NOT NULL DEFAULT '1',
  PRIMARY KEY(id)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;



CREATE TABLE cmServerNode (
  serverNodeId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  dnsName text NOT NULL,
  PRIMARY KEY  (serverNodeId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;


CREATE TABLE  cmFormEntry (
  id int(10) unsigned NOT NULL auto_increment,
  userName VARCHAR(255),
  originAddress varchar(1024) NOT NULL,
  formName varchar(255) NOT NULL,
  formContentId int(10) unsigned NOT NULL,
  userIP varchar(20) NOT NULL,
  userAgent varchar(255) NOT NULL,
  registrationDateTime DATETIME,
PRIMARY KEY  (id)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;


CREATE TABLE  cmFormEntryValue (
  id int(10) unsigned NOT NULL auto_increment,
  name varchar(128) NOT NULL,
  value varchar(4096),
  formEntryId int(10) unsigned NOT NULL,
  PRIMARY KEY  (id)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;


CREATE TABLE cmFormEntryAsset (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  formEntryId INTEGER UNSIGNED NOT NULL,
  fileName VARCHAR(255) NOT NULL,
  fileSize INTEGER UNSIGNED NOT NULL,
  assetKey VARCHAR(255) NOT NULL,
  contentType VARCHAR(50) NOT NULL,
  assetBlob BLOB NOT NULL,
  PRIMARY KEY(id)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;


CREATE TABLE cmSubscription (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  interceptionPointId INTEGER UNSIGNED NOT NULL,
  name varchar(100) NOT NULL,
  isGlobal tinyint(4) NOT NULL default '0',
  entityName varchar(100) default NULL,
  entityId varchar(200) default NULL,
  userName varchar(150) NOT NULL,
  userEmail varchar(150) default NULL,
  lastNotifiedDateTime timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY(id)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;



CREATE TABLE cmSubscriptionFilter (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  subscriptionId INTEGER UNSIGNED NOT NULL,
  filterType varchar(50) NOT NULL,
  filterCondition varchar(255) NOT NULL,
  isAndCondition tinyint(4) NOT NULL default '1',
  PRIMARY KEY(id)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;



CREATE TABLE cmInfoGlueProperties (
  propertyId int(11) NOT NULL auto_increment,
  name text NOT NULL,
  value text NOT NULL,
  PRIMARY KEY  (propertyId)
) CHARACTER SET utf8 COLLATE utf8_general_ci ENGINE=InnoDB;

INSERT INTO cmInfoGlueProperties(name, value) VALUES
  ('version', '2.9.8.7');
 
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
CREATE INDEX attributeName_categoryId on cmContentCategory (attributeName, categoryId);
CREATE INDEX contentVersionId on cmContentCategory (contentVersionId);
 
CREATE INDEX CS_OWNER ON OS_CURRENTSTEP(OWNER);
CREATE INDEX CS_CALLER ON OS_CURRENTSTEP(CALLER);
CREATE INDEX HS_OWNER ON OS_HISTORYSTEP(OWNER);
CREATE INDEX HS_CALLER ON OS_HISTORYSTEP(CALLER);

CREATE INDEX referencingEntityName ON cmRegistry(referencingEntityName);
CREATE INDEX referencingEntityId ON cmRegistry(referencingEntityId);
CREATE INDEX entityName ON cmRegistry(entityName);
CREATE INDEX entityId ON cmRegistry(entityId);
CREATE INDEX referencingEntityComplName ON cmRegistry(referencingEntityComplName);
CREATE INDEX referencingEntityComplId ON cmRegistry(referencingEntityComplId);
CREATE INDEX categoryContVersionId ON cmContentCategory(contentVersionId);
CREATE INDEX contVerDigAssetDigAssId ON cmContentVersionDigitalAsset(digitalAssetId);
CREATE INDEX contVerDigAssetContVerId ON cmContentVersionDigitalAsset(contentVersionId);
CREATE INDEX redirectUrl ON cmRedirect(redirectUrl(255));
 
create index propCategoryAttrNameIndex on cmPropertiesCategory(attributeName(100));
create index propCategoryEntityNameIndex on cmPropertiesCategory(entityName(100));
create index propCategoryEntityIdIndex on cmPropertiesCategory(entityId);
create index propCategoryCategoryIdIndex on cmPropertiesCategory(categoryId);
create index categoryParentIdIndex on cmCategory(parentId);
create index categoryNameIndex on cmCategory(name(100));

COMMIT;



