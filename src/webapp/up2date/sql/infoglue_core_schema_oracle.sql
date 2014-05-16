DROP SEQUENCE cmAvailServBind_seq;
CREATE SEQUENCE cmAvailServBind_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmAvailServBind (
  AvailServBindId number  NOT NULL,
  name varchar2(100) NOT NULL,
  description varchar2(255) NOT NULL,
  visualizationAction varchar2(100) NOT NULL,
  isMandatory number default 0 NOT NULL,
  isUserEditable number default 0 NOT NULL,
  isInheritable number default 0 NOT NULL,
  PRIMARY KEY  (AvailServBindId)
);


DROP SEQUENCE cmAvailServBindSiNoTypeDef_seq;
CREATE SEQUENCE cmAvailServBindSiNoTypeDef_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmAvailServBindSiNoTypeDef (
  AvailServBindId number default 0 NOT NULL,
  SiNoTypeDefId number default 0 NOT NULL,
  PRIMARY KEY  (AvailServBindId, SiNoTypeDefId)
);


DROP SEQUENCE cmCont_seq;
CREATE SEQUENCE cmCont_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmCont (
  ContId number  NOT NULL,
  name varchar2(100) NOT NULL,
  publishDateTime date  NOT NULL,
  expireDateTime date  NOT NULL,
  contentTypeDefId number default NULL,
  parentContId number default NULL,
  creator varchar2(100) NOT NULL,
  repositoryId number default 0 NOT NULL,
  isBranch number default 0 NOT NULL,
  isProtected number default 2 NOT NULL,
  isDeleted number DEFAULT 0 NOT NULL,PRIMARY KEY  (ContId)
);


DROP SEQUENCE cmContRelation_seq;
CREATE SEQUENCE cmContRelation_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmContRelation (
  ContRelationId number  NOT NULL,
  relationInternalName varchar2(100) NOT NULL,
  relationTypeId number default 0 NOT NULL,
  sourceContId number default 0 NOT NULL,
  destinationContId number default 0 NOT NULL,
  PRIMARY KEY  (ContRelationId)
);


DROP SEQUENCE cmContentTypeDef_seq;
CREATE SEQUENCE cmContentTypeDef_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmContentTypeDef (
  contentTypeDefId number  NOT NULL,
  schemaValue clob NOT NULL,
  name varchar2(100) NOT NULL,  parentContTypeDefId number DEFAULT -1,  detailPageResolverClass varchar2(255) DEFAULT '',  detailPageResolverData varchar2(1024) DEFAULT '',  type number default 0 NOT NULL,
  PRIMARY KEY  (contentTypeDefId)
);


DROP SEQUENCE cmContVer_seq;
CREATE SEQUENCE cmContVer_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmContVer (
  ContVerId number  NOT NULL,
  stateId number default 0 NOT NULL,
  VerValue clob NOT NULL,
  modifiedDateTime date  NOT NULL,
  VerComment varchar2(1024),
  isCheckedOut number default 0 NOT NULL,
  isActive number default 1 NOT NULL,
  ContId number default 0 NOT NULL,
  languageId number default 0 NOT NULL,
  versionModifier varchar2(1024) NOT NULL,
  PRIMARY KEY  (ContVerId)
);


DROP SEQUENCE cmContVerDigAsset_seq;
CREATE SEQUENCE cmContVerDigAsset_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmContVerDigAsset (
  ContVerId number default 0 NOT NULL,
  DigAssetId number default 0 NOT NULL,
  PRIMARY KEY  (ContVerId, DigAssetId)
);

DROP SEQUENCE cmDigAsset_seq;
CREATE SEQUENCE cmDigAsset_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmDigAsset (
  DigAssetId number  NOT NULL,
  assetKey varchar2(255) NOT NULL,
  assetFileName varchar2(1024) NOT NULL,
  assetFilepath varchar2(1024) NOT NULL,
  assetFileSize number default 0 NOT NULL,
  assetContentType varchar2(255) NOT NULL,
  assetBlob blob,
  PRIMARY KEY  (DigAssetId)
);

DROP SEQUENCE cmLanguage_seq;
CREATE SEQUENCE cmLanguage_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmLanguage (
  languageId number  NOT NULL,
  name varchar2(100) NOT NULL,
  languageCode varchar2(5) NOT NULL,
  charset varchar2(30) NOT NULL,
  PRIMARY KEY  (languageId)
);


DROP SEQUENCE cmPublication_seq;
CREATE SEQUENCE cmPublication_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmPublication (
  publicationId number  NOT NULL,
  name varchar2(100) NOT NULL,
  description varchar2(255) NOT NULL,
  publicationDateTime date  NOT NULL,
  publisher varchar2(100) NOT NULL,
  repositoryId number default 0 NOT NULL,
  PRIMARY KEY  (publicationId)
);


DROP SEQUENCE cmQualifyer_seq;
CREATE SEQUENCE cmQualifyer_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmQualifyer (
  qualifyerId number  NOT NULL,
  name varchar2(100) NOT NULL,
  value varchar2(100) NOT NULL,
  sortOrder number default 0 NOT NULL,
  ServBindId number default 0 NOT NULL,
  PRIMARY KEY  (qualifyerId)
);


DROP SEQUENCE cmRepository_seq;
CREATE SEQUENCE cmRepository_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmRepository (
  repositoryId number  NOT NULL,
  name varchar2(100) NOT NULL,
  description varchar2(255) NOT NULL,
  dnsName varchar2(255),  isDeleted number DEFAULT 0 NOT NULL,
  PRIMARY KEY  (repositoryId)
);


DROP SEQUENCE cmRepositoryContTypeDef_seq;
CREATE SEQUENCE cmRepositoryContTypeDef_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmRepositoryContTypeDef (
  repositoryId number default 0 NOT NULL,
  contentTypeDefId number default 0 NOT NULL,
  PRIMARY KEY  (repositoryId, contentTypeDefId)
);


DROP SEQUENCE cmRepositoryLanguage_seq;
CREATE SEQUENCE cmRepositoryLanguage_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmRepositoryLanguage (
  repositoryLanguageId number  NOT NULL,
  repositoryId number default 0 NOT NULL,
  languageId number default 0 NOT NULL,
  isPublished number default 0 NOT NULL,  sortOrder number default 0 NOT NULL,
  PRIMARY KEY  (repositoryLanguageId)
);


DROP SEQUENCE cmRole_seq;
CREATE SEQUENCE cmRole_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmRole (
  roleName varchar2(100) NOT NULL,
  description varchar2(255) NOT NULL,
  PRIMARY KEY  (roleName)
);


DROP SEQUENCE cmServBind_seq;
CREATE SEQUENCE cmServBind_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmServBind (
  ServBindId number  NOT NULL,
  name varchar2(100) NOT NULL,
  path varchar2(255) NOT NULL,
  BindTypeId number default 0 NOT NULL,
  ServDefId number default 0 NOT NULL,
  AvailServBindId number default 0 NOT NULL,
  SiNoVerId number default 0 NOT NULL,
  PRIMARY KEY  (ServBindId)
);


DROP SEQUENCE cmServDef_seq;
CREATE SEQUENCE cmServDef_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmServDef (
  ServDefId number  NOT NULL,
  className varchar2(100) NOT NULL,
  name varchar2(100) NOT NULL,
  description varchar2(255) NOT NULL,
  PRIMARY KEY  (ServDefId)
);


DROP SEQUENCE cmServDefAvailServBind_seq;
CREATE SEQUENCE cmServDefAvailServBind_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmServDefAvailServBind (
  ServDefId number default 0 NOT NULL,
  AvailServBindId number default 0 NOT NULL,
  PRIMARY KEY  (ServDefId, AvailServBindId)
);


DROP SEQUENCE cmSiNo_seq;
CREATE SEQUENCE cmSiNo_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmSiNo (
  SiNoId number NOT NULL,
  name varchar2(100) NOT NULL,
  publishDateTime date NOT NULL,
  expireDateTime date NOT NULL,
  parentSiNoId number default NULL,
  creator varchar2(100) NOT NULL,
  repositoryId number default 0 NOT NULL,
  SiNoTypeDefId number default 0,
  isBranch number default 0 NOT NULL,  metaInfoContentId number DEFAULT -1,  isDeleted number DEFAULT 0 NOT NULL,
  PRIMARY KEY  (SiNoId)
);


DROP SEQUENCE cmSiNoTypeDef_seq;
CREATE SEQUENCE cmSiNoTypeDef_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmSiNoTypeDef (
  SiNoTypeDefId number  NOT NULL,
  invokerClassName varchar2(100) NOT NULL,
  name varchar2(100) NOT NULL,
  description varchar2(255) NOT NULL,
  PRIMARY KEY  (SiNoTypeDefId)
);


DROP SEQUENCE cmSiNoVer_seq;
CREATE SEQUENCE cmSiNoVer_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmSiNoVer (  SiNoVerId number NOT NULL,  stateId number default 0 NOT NULL,  VerNumber number default 0 NOT NULL,  modifiedDateTime date NOT NULL,  VerComment varchar2(1024) NOT NULL,  isCheckedOut number default 0 NOT NULL,  isActive number default 1 NOT NULL,  SiNoId number default 0 NOT NULL,  versionModifier varchar2(100) NOT NULL,  isProtected number default 2 NOT NULL,  disablePageCache number default 2 NOT NULL,  disableEditOnSight number default 2 NOT NULL,  disableLanguages number default 2 NOT NULL,  disableForceIDCheck number default 2 NOT NULL,  forceProtocolChange number default 0 NOT NULL,  contentType varchar2(255) DEFAULT 'text/html' NOT NULL,  pageCacheKey varchar2(255) DEFAULT 'default' NOT NULL,  pageCacheTimeout VARCHAR2(20) default NULL,  sortOrder number DEFAULT '-1' NOT NULL,  isHidden number DEFAULT 0 NOT NULL,  PRIMARY KEY  (SiNoVerId));

DROP SEQUENCE cmSystemUser_seq;
CREATE SEQUENCE cmSystemUser_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmSystemUser (
  userName varchar2(100) NOT NULL,
  password varchar2(100) NOT NULL,
  firstName varchar2(100) NOT NULL,
  lastName varchar2(100) NOT NULL,
  email varchar2(255) NOT NULL,
  PRIMARY KEY  (userName)
);


DROP SEQUENCE cmSystemUserRole_seq;
CREATE SEQUENCE cmSystemUserRole_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmSystemUserRole (
  userName varchar2(100) NOT NULL,
  roleName varchar2(100) NOT NULL,
  PRIMARY KEY  (userName, roleName)
);


DROP SEQUENCE cmTransactionHistory_seq;
CREATE SEQUENCE cmTransactionHistory_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmTransactionHistory (
  transactionHistoryId number  NOT NULL,
  name varchar2(200) NOT NULL,
  transactionDateTime date  NOT NULL,
  transactionTypeId number default 0 NOT NULL,
  transactionObjectId varchar2(200) NOT NULL,
  transactionObjectName varchar2(200) NOT NULL,
  systemUserName varchar2(200) NOT NULL,
  PRIMARY KEY  (transactionHistoryId)
);


DROP SEQUENCE cmPublicationDetail_seq;
CREATE SEQUENCE cmPublicationDetail_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmPublicationDetail (
  publicationDetailId number  NOT NULL,
  publicationId number default 0 NOT NULL,
  name varchar2(100) NOT NULL,
  description varchar2(255) NOT NULL,
  entityClass varchar2(255) NOT NULL,
  entityId number default 0 NOT NULL,
  creationDateTime date NOT NULL,
  typeId number default 0 NOT NULL,
  publisher varchar2(100) NOT NULL,
  PRIMARY KEY  (publicationDetailId)
);


DROP SEQUENCE cmEvent_seq;
CREATE SEQUENCE cmEvent_seq START WITH 100 INCREMENT BY 10;

CREATE TABLE cmEvent (
  eventId number  NOT NULL,
  repositoryId number default 0 NOT NULL,
  name varchar2(100) NOT NULL,
  description varchar2(255) NOT NULL,
  entityClass varchar2(255) NOT NULL,
  entityId number default 0 NOT NULL,
  creationDateTime date NOT NULL,
  typeId number default 0 NOT NULL,
  creator varchar2(100) NOT NULL,
  PRIMARY KEY  (eventId)
);

DROP SEQUENCE cmRoleContentTypeDef_seq;CREATE SEQUENCE cmRoleContentTypeDef_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmRoleContentTypeDef (  roleContentTypeDefId integer NOT NULL,  roleName varchar(100) NOT NULL,  contentTypeDefId number default 0 NOT NULL,  PRIMARY KEY  (roleContentTypeDefId));DROP SEQUENCE cmRoleProperties_seq;CREATE SEQUENCE cmRoleProperties_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmRoleProperties (  rolePropertiesId integer NOT NULL,  roleName varchar(100) NOT NULL,  contentTypeDefId integer default 0 NOT NULL,  value clob NOT NULL,  languageId integer default 0 NOT NULL,  PRIMARY KEY  (rolePropertiesId));DROP SEQUENCE cmUserContentTypeDef_seq;CREATE SEQUENCE cmUserContentTypeDef_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmUserContentTypeDef (  userContentTypeDefId integer NOT NULL,  userName varchar(255) NOT NULL,  contentTypeDefId integer default 0 NOT NULL,  PRIMARY KEY  (userContentTypeDefId));DROP SEQUENCE cmUserProperties_seq;CREATE SEQUENCE cmUserProperties_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmUserProperties (  userPropertiesId integer NOT NULL,  userName varchar(255) NOT NULL,  contentTypeDefId integer default 0 NOT NULL,  value clob NOT NULL,  languageId integer default 0 NOT NULL,  PRIMARY KEY  (userPropertiesId));
DROP SEQUENCE cmAccessRight_seq;CREATE SEQUENCE cmAccessRight_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmAccessRight (  accessRightId number NOT NULL,  parameters varchar2(2048) NULL,  interceptionPointId number NOT NULL,  PRIMARY KEY  (accessRightId));DROP SEQUENCE cmInterceptionPoint_seq;CREATE SEQUENCE cmInterceptionPoint_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmInterceptionPoint (  interceptionPointId number NOT NULL,  category varchar2(100) NOT NULL,  name varchar2(100) NOT NULL,  description varchar2(1024) NOT NULL,  usesExtraDataForAccessControl number default 0 NULL,  PRIMARY KEY  (interceptionPointId));DROP SEQUENCE cmIntPointInterceptor_seq;CREATE SEQUENCE cmIntPointInterceptor_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmIntPointInterceptor (  interceptionPointId number NOT NULL,  interceptorId number NOT NULL,  PRIMARY KEY  (interceptionPointId, interceptorId));DROP SEQUENCE cmInterceptor_seq;CREATE SEQUENCE cmInterceptor_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmInterceptor (  interceptorId number NOT NULL,  name varchar2(100) NOT NULL,  className varchar2(255) NOT NULL,  description varchar2(1024) NOT NULL,  PRIMARY KEY  (interceptorId));CREATE TABLE OS_PROPERTYENTRY(	entity_name varchar2(125) not null,	entity_id number DEFAULT 0 not null,	entity_key varchar2(150) not null,	key_type smallint,	boolean_val smallint,	string_val varchar2(255),	long_val number,	date_val date,	data_val long raw,	double_val float,	int_val int,	primary key (entity_name, entity_id, entity_key));create table OS_WFENTRY(    ID number,    NAME varchar(100),    STATE integer,    primary key (ID));create table OS_CURRENTSTEP(    ID number,    ENTRY_ID number,    STEP_ID integer,    ACTION_ID integer,    OWNER varchar(255),    START_DATE date,    FINISH_DATE date,    DUE_DATE date,    STATUS varchar(20),    CALLER varchar(255),    STEPINDEX number DEFAULT 0,    primary key (ID),    foreign key (ENTRY_ID) references OS_WFENTRY(ID));create table OS_HISTORYSTEP(    ID number,    ENTRY_ID number,    STEP_ID integer,    ACTION_ID integer,    OWNER varchar(255),    START_DATE date,    FINISH_DATE date,    DUE_DATE date,    STATUS varchar(20),    CALLER varchar(255),    STEPINDEX number DEFAULT 0,    primary key (ID),    foreign key (ENTRY_ID) references OS_WFENTRY(ID));create table OS_CURRENTSTEP_PREV(    ID number,    PREVIOUS_ID number,    primary key (ID, PREVIOUS_ID),    foreign key (ID) references OS_CURRENTSTEP(ID),    foreign key (PREVIOUS_ID) references OS_HISTORYSTEP(ID));create table OS_HISTORYSTEP_PREV(    ID number,    PREVIOUS_ID number,    primary key (ID, PREVIOUS_ID),    foreign key (ID) references OS_HISTORYSTEP(ID),    foreign key (PREVIOUS_ID) references OS_HISTORYSTEP(ID));DROP SEQUENCE hibernate_sequence;DROP SEQUENCE seq_os_wfentry;DROP SEQUENCE seq_os_currentsteps;create sequence hibernate_sequence;create sequence seq_os_wfentry minvalue 10 increment by 10;create sequence seq_os_currentsteps; DROP SEQUENCE cmCategory_seq;CREATE SEQUENCE cmCategory_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmCategory(	categoryId		number NOT NULL,	name			VARCHAR2(100) NOT NULL,	displayName 	VARCHAR2(4000),	description		varchar2(1024),	active 			number default 1 NOT NULL,	parentId		number,	PRIMARY KEY (categoryId));DROP SEQUENCE cmContentCategory_seq;CREATE SEQUENCE cmContentCategory_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmContentCategory(	contentCategoryId	number NOT NULL,	attributeName		VARCHAR2(100) NOT NULL,	ContVerId			number NOT NULL,	categoryId			number NOT NULL,	PRIMARY KEY (contentCategoryId));DROP SEQUENCE cmUserPropDigAsset_seq;CREATE SEQUENCE cmUserPropDigAsset_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmUserPropDigAsset (  userPropDigAssetId number NOT NULL,  userPropertiesId number NOT NULL,  digAssetId number NOT NULL,  PRIMARY KEY  (userPropDigAssetId));DROP SEQUENCE cmRolePropDigAsset_seq;CREATE SEQUENCE cmRolePropDigAsset_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmRolePropDigAsset (  rolePropDigAssetId number NOT NULL,  rolePropertiesId number NOT NULL,  digAssetId number NOT NULL,  PRIMARY KEY  (rolePropDigAssetId));DROP SEQUENCE cmGroupPropDigAsset_seq;CREATE SEQUENCE cmGroupPropDigAsset_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmGroupPropDigAsset (  groupPropDigAssetId number NOT NULL,  groupPropertiesId number NOT NULL,  digAssetId number NOT NULL,  PRIMARY KEY  (groupPropDigAssetId));DROP SEQUENCE cmPropertiesCategory_seq;CREATE SEQUENCE cmPropertiesCategory_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmPropertiesCategory(	propertiesCategoryId number NOT NULL,	attributeName		VARCHAR2(100) NOT NULL,	entityName			VARCHAR2(100) NOT NULL,	entityId			number NOT NULL,	categoryId			number NOT NULL,	PRIMARY KEY (propertiesCategoryId));DROP SEQUENCE cmRegistry_seq;CREATE SEQUENCE cmRegistry_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmRegistry(	registryId		            number NOT NULL,	entityName		            VARCHAR2(100) NOT NULL,	entityId		            VARCHAR2(200) NOT NULL,	referenceType	            number NOT NULL,	referencingEntityName		VARCHAR2(100) NOT NULL,	referencingEntityId		    VARCHAR2(200) NOT NULL,	referencingEntityComplName	VARCHAR2(100) NOT NULL,	referencingEntityComplId	VARCHAR2(200) NOT NULL,    PRIMARY KEY (registryId));CREATE TABLE cmGroup (  groupName varchar2(255) NOT NULL,  description varchar2(1024) NOT NULL,  PRIMARY KEY  (groupName));DROP SEQUENCE cmGroupContTypeDef_seq;CREATE SEQUENCE cmGroupContTypeDef_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmGroupContTypeDef (  groupContTypeDefId number NOT NULL,  groupName varchar2(255) NOT NULL,  contentTypeDefId integer default 0 NOT NULL,  PRIMARY KEY  (groupContTypeDefId));DROP SEQUENCE cmGroupProperties_seq;CREATE SEQUENCE cmGroupProperties_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmGroupProperties (  groupPropertiesId number NOT NULL,  groupName varchar2(255) NOT NULL,  contentTypeDefId integer default 0 NOT NULL,  value clob NOT NULL,  languageId number NOT NULL,  PRIMARY KEY  (groupPropertiesId));DROP SEQUENCE cmSystemUserGroup_seq;CREATE SEQUENCE cmSystemUserGroup_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmSystemUserGroup (  userName varchar2(100) NOT NULL,  groupName varchar2(200) NOT NULL,  PRIMARY KEY  (userName,groupName));DROP SEQUENCE cmAccessRightRole_seq;CREATE SEQUENCE cmAccessRightRole_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmAccessRightRole (  accessRightRoleId number NOT NULL,  accessRightId number NOT NULL,  roleName varchar2(150) NOT NULL,  PRIMARY KEY  (accessRightRoleId));DROP SEQUENCE cmAccessRightGroup_seq;CREATE SEQUENCE cmAccessRightGroup_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmAccessRightGroup (  accessRightGroupId number NOT NULL,  accessRightId number NOT NULL,  groupName varchar2(150) NOT NULL,  PRIMARY KEY  (accessRightGroupId));DROP SEQUENCE cmAccessRightUser_seq;CREATE SEQUENCE cmAccessRightUser_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmAccessRightUser (  accessRightUserId number NOT NULL,  accessRightId number NOT NULL,  userName varchar2(150) NOT NULL,  PRIMARY KEY  (accessRightUserId));DROP SEQUENCE cmWorkflowDefinition_seq;CREATE SEQUENCE cmWorkflowDefinition_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmWorkflowDefinition (  workflowDefinitionId number NOT NULL,  name varchar2(100) NOT NULL,  value clob NOT NULL,  PRIMARY KEY  (workflowDefinitionId));DROP SEQUENCE cmRedirect_seq;CREATE SEQUENCE cmRedirect_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmRedirect(	id				number NOT NULL,	url				VARCHAR2(1024) NOT NULL,	redirectUrl		varchar2(1024) NOT NULL,	createdDateTime date,	publishDateTime date,	expireDateTime date,	modifier varchar2(1024) DEFAULT 'system' NOT NULL,	isUserManaged number DEFAULT 1 NOT NULL,	siteNodeId number NULL,	languageId number NULL,	PRIMARY KEY (id));DROP SEQUENCE cmServerNode_seq;CREATE SEQUENCE cmServerNode_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmServerNode (  serverNodeId number NOT NULL,  name varchar2(255) NOT NULL,  description varchar2(1024) NOT NULL,  dnsName varchar2(1024) NOT NULL,  PRIMARY KEY  (serverNodeId));DROP SEQUENCE cmFormEntry_seq;CREATE SEQUENCE cmFormEntry_seq START WITH 100 INCREMENT BY 1;CREATE TABLE cmFormEntry(	id					number NOT NULL,	originAddress		VARCHAR2(1024) NOT NULL,	formName			VARCHAR2(255) NOT NULL,	formContentId		number NOT NULL,	userIP				VARCHAR2(20) NOT NULL,	userAgent			VARCHAR2(1024) NOT NULL,	PRIMARY KEY (id));DROP SEQUENCE cmFormEntryValue_seq;CREATE SEQUENCE cmFormEntryValue_seq START WITH 100 INCREMENT BY 1;CREATE TABLE cmFormEntryValue(	id			number NOT NULL,	name		VARCHAR2(128) NOT NULL,	value		VARCHAR2(4000),	formEntryId	number NOT NULL,	PRIMARY KEY (id));DROP SEQUENCE cmFormEntryAsset_seq;CREATE SEQUENCE cmFormEntryAsset_seq START WITH 100 INCREMENT BY 1;CREATE TABLE cmFormEntryAsset(	id					number NOT NULL,	formEntryId			number NOT NULL,	fileName			VARCHAR2(255) NOT NULL,	fileSize			number NOT NULL,	assetKey			VARCHAR2(255) NOT NULL,	contentType			VARCHAR2(50) NOT NULL,	assetBlob 			blob NOT NULL,	PRIMARY KEY (id));DROP SEQUENCE cmSubscription_seq;CREATE SEQUENCE cmSubscription_seq START WITH 100 INCREMENT BY 1;CREATE TABLE cmSubscription(	id						number NOT NULL,	interceptionPointId		number NOT NULL,	name					VARCHAR2(100) NOT NULL,	isGlobal				number default 0 NOT NULL,	entityName				VARCHAR2(100) default NULL,	entityId				VARCHAR2(200) default NULL,	userName				VARCHAR2(150) NOT NULL,	userEmail				VARCHAR2(150),	lastNotifiedDateTime	date default sysdate,	PRIMARY KEY (id));DROP SEQUENCE cmSubscriptionFilter_seq;CREATE SEQUENCE cmSubscriptionFilter_seq START WITH 100 INCREMENT BY 1;CREATE TABLE cmSubscriptionFilter(	id						number NOT NULL,	subscriptionId			number NOT NULL,	filterType				VARCHAR2(50) NOT NULL,	filterCondition			VARCHAR2(255) NOT NULL,	isAndCondition			number default 0 NOT NULL,	PRIMARY KEY (id));DROP SEQUENCE cmInfoGlueProperties_seq;CREATE SEQUENCE cmInfoGlueProperties_seq START WITH 100 INCREMENT BY 10;CREATE TABLE cmInfoGlueProperties (  propertyId number NOT NULL,  name varchar2(100) NOT NULL,  value varchar2(1024) NOT NULL,  PRIMARY KEY  (propertyId));INSERT INTO cmInfoGlueProperties(propertyId, name, value) VALUES  (1, 'version', '3.0');CREATE INDEX "qualifyerServBindIdINDEX" ON cmQualifyer(servBindId);CREATE INDEX "servBindServDefIdINDEX" ON cmServBind(servDefId);CREATE INDEX "servBindAvailServBindIdINDEX" ON cmServBind(availServBindId);CREATE INDEX "servBindSiteNodeVerIdINDEX" ON cmServBind(siNoVerId);CREATE INDEX "contTypeNameINDEX" ON cmContentTypeDef(name);CREATE INDEX "contentVersionContentIdINDEX" ON cmContVer(contId);CREATE INDEX "siteNodeVerSiteNodeIdINDEX" ON cmSiNoVer(siNoId);CREATE INDEX "contentTypeDefinitionIdINDEX" ON cmCont(contentTypeDefId);CREATE INDEX "parentContentIdINDEX" ON cmCont(parentContId);CREATE INDEX "publicationIdINDEX" ON cmPublicationDetail(publicationId);DROP INDEX "OS_CURRENTSTEP_OWNERINDEX";DROP INDEX "OS_CURRENTSTEP_OWNERCALLER";DROP INDEX "OS_HISTORYSTEP_OWNERINDEX";DROP INDEX "OS_HISTORYSTEP_CALLERINDEX";CREATE INDEX "OS_CURRENTSTEP_OWNERINDEX" ON OS_CURRENTSTEP(OWNER);CREATE INDEX "OS_CURRENTSTEP_OWNERCALLER" ON OS_CURRENTSTEP(CALLER);CREATE INDEX "OS_HISTORYSTEP_OWNERINDEX" ON OS_HISTORYSTEP(OWNER);CREATE INDEX "OS_HISTORYSTEP_CALLERINDEX" ON OS_HISTORYSTEP(CALLER);CREATE INDEX "referencingEntityNameIndex" ON cmRegistry(referencingEntityName);CREATE INDEX "referencingEntityIdIndex" ON cmRegistry(referencingEntityId);CREATE INDEX "entityNameIndex" ON cmRegistry(entityName);CREATE INDEX "entityIdIndex" ON cmRegistry(entityId);CREATE INDEX "refEntityComplNameIndex" ON cmRegistry(referencingEntityComplName);CREATE INDEX "refEntityComplIdIndex" ON cmRegistry(referencingEntityComplId);CREATE INDEX "categoryContVerIdIndex" ON cmContentCategory(contVerId);CREATE INDEX "attributeName_categoryId" on cmContentCategory (attributeName, categoryId);CREATE INDEX "contVerDigAssetDigAssIdIndex" ON cmContVerDigAsset(digAssetId);CREATE INDEX "contVerDigAssetContVerIdIndex" ON cmContVerDigAsset(contVerId);CREATE INDEX "redirectUrlIndex" ON cmRedirect(redirectUrl);drop sequence cmPageDeliveryMetaData_seq;drop sequence cmPageDeliveryMetaDataEnt_seq;CREATE SEQUENCE cmPageDeliveryMetaData_seq START WITH 1 INCREMENT BY 1;CREATE SEQUENCE cmPageDeliveryMetaDataEnt_seq START WITH 1 INCREMENT BY 1;ALTER TABLE cmSystemUser ADD source varchar2(255) default 'infoglue' NOT NULL;ALTER TABLE cmSystemUser ADD modifiedDateTime date default sysdate;ALTER TABLE cmSystemUser ADD isActive number default 1 NOT NULL;ALTER TABLE cmRole ADD source varchar2(255) default 'infoglue' NOT NULL;ALTER TABLE cmRole ADD modifiedDateTime date  default sysdate;ALTER TABLE cmRole ADD isActive number default 1 NOT NULL;ALTER TABLE cmGroup ADD groupType varchar2(255) default ' ' NOT NULL;ALTER TABLE cmGroup ADD source varchar2(255) default 'infoglue' NOT NULL;ALTER TABLE cmGroup ADD modifiedDateTime date default sysdate;ALTER TABLE cmGroup ADD isActive number default 1 NOT NULL;
COMMIT;