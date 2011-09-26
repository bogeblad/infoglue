DROP TABLE IF EXISTS cmAvailableServiceBinding;

CREATE TABLE cmAvailableServiceBinding (
  availableServiceBindingId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  visualizationAction text NOT NULL,
  isMandatory tinyint(4) NOT NULL default '0',
  isUserEditable tinyint(4) NOT NULL default '0',
  isInheritable tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (availableServiceBindingId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmAvailableServiceBindingSiteNodeTypeDefinition;

CREATE TABLE cmAvailableServiceBindingSiteNodeTypeDefinition (
  availableServiceBindingSiteNodeTypeDefinitionId integer(11) unsigned NOT NULL auto_increment,
  availableServiceBindingId integer(11) NOT NULL default '0',
  siteNodeTypeDefinitionId integer(11) NOT NULL default '0',
  PRIMARY KEY  (availableServiceBindingSiteNodeTypeDefinitionId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmContent;

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
  PRIMARY KEY  (contentId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmContentRelation;

CREATE TABLE cmContentRelation (
  contentRelationId integer(11) unsigned NOT NULL auto_increment,
  relationInternalName text NOT NULL,
  relationTypeId integer(11) NOT NULL default '0',
  sourceContentId integer(11) NOT NULL default '0',
  destinationContentId integer(11) NOT NULL default '0',
  PRIMARY KEY  (contentRelationId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmContentTypeDefinition;

CREATE TABLE cmContentTypeDefinition (
  contentTypeDefinitionId integer(11) unsigned NOT NULL auto_increment,
  schemaValue longtext NOT NULL,
  name varchar(255) NOT NULL,
  type tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (contentTypeDefinitionId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmContentVersion;

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
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmContentVersionDigitalAsset;

CREATE TABLE cmContentVersionDigitalAsset (
  contentVersionDigitalAssetId integer(11) unsigned NOT NULL auto_increment,
  contentVersionId integer(11) unsigned NOT NULL default '0',
  digitalAssetId integer(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (contentVersionDigitalAssetId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmUserPropertiesDigitalAsset;

CREATE TABLE cmUserPropertiesDigitalAsset (
  userPropertiesDigitalAssetId integer(11) unsigned NOT NULL auto_increment,
  userPropertiesId integer(11) unsigned NOT NULL default '0',
  digitalAssetId integer(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (userPropertiesDigitalAssetId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmRolePropertiesDigitalAsset;

CREATE TABLE cmRolePropertiesDigitalAsset (
  rolePropertiesDigitalAssetId integer(11) unsigned NOT NULL auto_increment,
  rolePropertiesId integer(11) unsigned NOT NULL default '0',
  digitalAssetId integer(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (rolePropertiesDigitalAssetId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmDigitalAsset;

CREATE TABLE cmDigitalAsset (
  digitalAssetId integer(11) unsigned NOT NULL auto_increment,
  assetKey text NOT NULL,
  assetFileName text NOT NULL,
  assetFilePath text NOT NULL,
  assetFileSize int(11) NOT NULL default '0',
  assetContentType text NOT NULL,
  assetBlob longblob,
  PRIMARY KEY  (digitalAssetId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmLanguage;

CREATE TABLE cmLanguage (
  languageId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  languageCode text NOT NULL,
  charset text NOT NULL,
  PRIMARY KEY  (languageId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmPublication;

CREATE TABLE cmPublication (
  publicationId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  publicationDateTime datetime NOT NULL default '1970-01-01 12:00:00',
  publisher text NOT NULL,
  repositoryId integer(11) NOT NULL default '0',
  PRIMARY KEY  (publicationId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmQualifyer;

CREATE TABLE cmQualifyer (
  qualifyerId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  value text NOT NULL,
  sortOrder integer(11) NOT NULL default '0',
  serviceBindingId integer(11) NOT NULL default '0',
  PRIMARY KEY  (qualifyerId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmRepository;

CREATE TABLE cmRepository (
  repositoryId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  dnsName text NOT NULL,
  PRIMARY KEY  (repositoryId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmRepositoryContentTypeDefinition;

CREATE TABLE cmRepositoryContentTypeDefinition (
  repositoryContentTypeDefinitionId integer(11) unsigned NOT NULL auto_increment,
  repositoryId integer(11) NOT NULL default '0',
  contentTypeDefinitionId integer(11) NOT NULL default '0',
  PRIMARY KEY  (repositoryContentTypeDefinitionId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmRepositoryLanguage;

CREATE TABLE cmRepositoryLanguage (
  repositoryLanguageId integer(11) unsigned NOT NULL auto_increment,
  repositoryId integer(11) NOT NULL default '0',
  languageId integer(11) NOT NULL default '0',
  isPublished tinyint(4) NOT NULL default '0',
  sortOrder tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (repositoryLanguageId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmRole;

CREATE TABLE cmRole (
  roleName varchar(200) NOT NULL,
  description text NOT NULL,
  PRIMARY KEY  (roleName)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmServiceBinding;

CREATE TABLE cmServiceBinding (
  serviceBindingId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  path text NOT NULL,
  bindingTypeId integer(11) NOT NULL default '0',
  serviceDefinitionId integer(11) NOT NULL default '0',
  availableServiceBindingId integer(11) NOT NULL default '0',
  siteNodeVersionId integer(11) NOT NULL default '0',
  PRIMARY KEY  (serviceBindingId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmServiceDefinition;

CREATE TABLE cmServiceDefinition (
  serviceDefinitionId integer(11) unsigned NOT NULL auto_increment,
  className text NOT NULL,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  PRIMARY KEY  (serviceDefinitionId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmServiceDefinitionAvailableServiceBinding;

CREATE TABLE cmServiceDefinitionAvailableServiceBinding (
  serviceDefinitionAvailableServiceBindingId integer(11) unsigned NOT NULL auto_increment,
  serviceDefinitionId integer(11) NOT NULL default '0',
  availableServiceBindingId integer(11) NOT NULL default '0',
  PRIMARY KEY  (serviceDefinitionAvailableServiceBindingId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmSiteNode;

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
  PRIMARY KEY  (siteNodeId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmSiteNodeTypeDefinition;

CREATE TABLE cmSiteNodeTypeDefinition (
  siteNodeTypeDefinitionId integer(11) unsigned NOT NULL auto_increment,
  invokerClassName text NOT NULL,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  PRIMARY KEY  (siteNodeTypeDefinitionId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmSiteNodeVersion;

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
  pageCacheTimeout varchar(20) default NULL,
  disableForceIDCheck tinyint(4) NOT NULL default '2',
  contentType text,
  pageCacheKey varchar(255) NOT NULL default 'default',
  PRIMARY KEY  (siteNodeVersionId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmSystemUser;

CREATE TABLE cmSystemUser (
  userName varchar(200) NOT NULL,
  password varchar(255) NOT NULL,
  firstName text NOT NULL,
  lastName text NOT NULL,
  email text NOT NULL,
  PRIMARY KEY  (userName)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmSystemUserRole;

CREATE TABLE cmSystemUserRole (
  userName varchar(100) NOT NULL,
  roleName varchar(200) NOT NULL,
  PRIMARY KEY  (userName, roleName)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmTransactionHistory;

CREATE TABLE cmTransactionHistory (
  transactionHistoryId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  transactionDateTime datetime NOT NULL default '1970-01-01 12:00:00',
  transactionTypeId integer(11) NOT NULL default '0',
  transactionObjectId text NOT NULL,
  transactionObjectName text NOT NULL,
  systemUserName text NOT NULL,
  PRIMARY KEY  (transactionHistoryId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmPublicationDetail;

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
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmEvent;

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
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmRoleContentTypeDefinition;

CREATE TABLE cmRoleContentTypeDefinition (
  roleContentTypeDefinitionId int(11) NOT NULL auto_increment,
  roleName text NOT NULL,
  contentTypeDefinitionId int(11) NOT NULL default '0',
  PRIMARY KEY  (roleContentTypeDefinitionId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmRoleProperties;

CREATE TABLE cmRoleProperties (
  rolePropertiesId int(11) NOT NULL auto_increment,
  roleName text NOT NULL,
  contentTypeDefinitionId int(11) NOT NULL default '0',
  value text NOT NULL,
  languageId int(11) NOT NULL default '0',
  PRIMARY KEY  (rolePropertiesId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmUserContentTypeDefinition;

CREATE TABLE cmUserContentTypeDefinition (
  userContentTypeDefinitionId int(11) NOT NULL auto_increment,
  userName text NOT NULL,
  contentTypeDefinitionId int(11) NOT NULL default '0',
  PRIMARY KEY  (userContentTypeDefinitionId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmUserProperties;

CREATE TABLE cmUserProperties (
  userPropertiesId int(11) NOT NULL auto_increment,
  userName text NOT NULL,
  contentTypeDefinitionId int(11) NOT NULL default '0',
  value text NOT NULL,
  languageId int(11) NOT NULL default '0',
  PRIMARY KEY  (userPropertiesId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmAccessRight;

CREATE TABLE cmAccessRight (
  accessRightId int(11) NOT NULL auto_increment,
  parameters text NULL,
  interceptionPointId int(11) NOT NULL,
  PRIMARY KEY  (accessRightId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmInterceptionPoint;

CREATE TABLE cmInterceptionPoint (
  interceptionPointId int(11) NOT NULL auto_increment,
  category text NOT NULL,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  usesExtraDataForAccessControl int(11) default '0' NULL,
  PRIMARY KEY  (interceptionPointId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmInterceptionPointInterceptor;

CREATE TABLE cmInterceptionPointInterceptor (
  interceptionPointId int(11) NOT NULL,
  interceptorId int(11) NOT NULL,
  PRIMARY KEY  (interceptionPointId, interceptorId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmInterceptor;

CREATE TABLE cmInterceptor (
  interceptorId int(11) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  className text NOT NULL,
  description text NOT NULL,
  PRIMARY KEY  (interceptorId)
) TYPE=InnoDB;



DROP TABLE IF EXISTS OS_PROPERTYENTRY cascade;

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


DROP TABLE IF EXISTS OS_WFENTRY cascade;
CREATE TABLE OS_WFENTRY
(
    ID bigint NOT NULL auto_increment,
    NAME varchar(60),
    STATE integer,
    primary key (ID)
)TYPE=InnoDB;


DROP TABLE IF EXISTS OS_CURRENTSTEP;
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

DROP TABLE IF EXISTS OS_HISTORYSTEP;
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

DROP TABLE IF EXISTS OS_CURRENTSTEP_PREV;
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

DROP TABLE IF EXISTS OS_HISTORYSTEP_PREV;
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

DROP TABLE IF EXISTS OS_STEPIDS;
CREATE TABLE OS_STEPIDS
(
	 ID bigint NOT NULL AUTO_INCREMENT,
	 PRIMARY KEY (id)
 )TYPE=InnoDB;
 


DROP TABLE IF EXISTS cmCategory;

CREATE TABLE cmCategory
(
	categoryId		INTEGER(11) unsigned NOT NULL auto_increment,
	name			VARCHAR(100) NOT NULL,
	description		TEXT,
	active			TINYINT(4) NOT NULL default '1',
	parentId		INTEGER(11),
	PRIMARY KEY (categoryId)
);


DROP TABLE IF EXISTS cmContentCategory;

CREATE TABLE cmContentCategory
(
	contentCategoryId	INTEGER(11) unsigned NOT NULL auto_increment,
	attributeName		VARCHAR(100) NOT NULL,
	contentVersionId	INTEGER(11) NOT NULL,
	categoryId			INTEGER(11) NOT NULL,
	PRIMARY KEY (contentCategoryId)
);


DROP TABLE IF EXISTS cmGroupPropertiesDigitalAsset;

CREATE TABLE cmGroupPropertiesDigitalAsset (
  groupPropertiesDigitalAssetId integer(11) unsigned NOT NULL auto_increment,
  groupPropertiesId integer(11) unsigned NOT NULL default '0',
  digitalAssetId integer(11) unsigned NOT NULL default '0',
  PRIMARY KEY  (groupPropertiesDigitalAssetId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmPropertiesCategory;

CREATE TABLE cmPropertiesCategory
(
	propertiesCategoryId	INTEGER(11) unsigned NOT NULL auto_increment,
	attributeName		VARCHAR(100) NOT NULL,
	entityName			VARCHAR(100) NOT NULL,
	entityId			INTEGER(11) NOT NULL,
	categoryId			INTEGER(11) NOT NULL,
	PRIMARY KEY (propertiesCategoryId)
);


DROP TABLE IF EXISTS cmRegistry;

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

DROP TABLE IF EXISTS cmGroup;

CREATE TABLE cmGroup (
  groupName varchar(255) NOT NULL default '',
  description text NOT NULL,
  PRIMARY KEY  (groupName)
) TYPE=InnoDB;

DROP TABLE IF EXISTS cmGroupContentTypeDefinition;

CREATE TABLE cmGroupContentTypeDefinition (
  groupContentTypeDefinitionId int(11) NOT NULL auto_increment,
  groupName text NOT NULL,
  contentTypeDefinitionId int(11) NOT NULL default '0',
  PRIMARY KEY  (groupContentTypeDefinitionId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmGroupProperties;

CREATE TABLE cmGroupProperties (
  groupPropertiesId int(11) NOT NULL auto_increment,
  groupName text NOT NULL,
  contentTypeDefinitionId int(11) NOT NULL default '0',
  value text NOT NULL,
  languageId int(11) NOT NULL default '0',
  PRIMARY KEY  (groupPropertiesId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmSystemUserGroup;

CREATE TABLE cmSystemUserGroup (
  userName varchar(150) NOT NULL default '',
  groupName varchar(150) NOT NULL default '',
  PRIMARY KEY  (userName,groupName)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmAccessRightRole;

CREATE TABLE cmAccessRightRole (
  accessRightRoleId int(11) NOT NULL auto_increment,
  accessRightId int(11) NOT NULL default '0',
  roleName varchar(150) NOT NULL default '',
  PRIMARY KEY  (accessRightRoleId)
) TYPE=InnoDB;

DROP TABLE IF EXISTS cmAccessRightGroup;

CREATE TABLE cmAccessRightGroup (
  accessRightGroupId int(11) NOT NULL auto_increment,
  accessRightId int(11) NOT NULL default '0',
  groupName varchar(150) NOT NULL default '',
  PRIMARY KEY  (accessRightGroupId)
) TYPE=InnoDB;

DROP TABLE IF EXISTS cmAccessRightUser;

CREATE TABLE cmAccessRightUser (
  accessRightUserId int(11) NOT NULL auto_increment,
  accessRightId int(11) NOT NULL default '0',
  userName varchar(150) NOT NULL default '',
  PRIMARY KEY  (accessRightUserId)
) TYPE=InnoDB;


DROP TABLE IF EXISTS cmWorkflowDefinition;

CREATE TABLE cmWorkflowDefinition (
  workflowDefinitionId int(11) NOT NULL auto_increment,
  name text NOT NULL,
  value text NOT NULL,
  PRIMARY KEY  (workflowDefinitionId)
) TYPE=InnoDB;
 
DROP TABLE IF EXISTS cmRedirect;

CREATE TABLE cmRedirect (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  url TEXT NOT NULL,
  redirectUrl TEXT NOT NULL,
  PRIMARY KEY(id)
) TYPE = InnoDB;

DROP TABLE IF EXISTS cmAccessRightUser;

CREATE TABLE cmAccessRightUser (
  accessRightUserId int(11) NOT NULL auto_increment,
  accessRightId int(11) NOT NULL default '0',
  userName varchar(150) NOT NULL default '',
  PRIMARY KEY  (accessRightUserId)
) TYPE=InnoDB;

DROP TABLE IF EXISTS cmServerNode;

CREATE TABLE cmServerNode (
  serverNodeId integer(11) unsigned NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  dnsName text NOT NULL,
  PRIMARY KEY  (serverNodeId)
) TYPE=InnoDB;

DROP TABLE IF EXISTS cmInfoGlueProperties;

CREATE TABLE cmInfoGlueProperties (
  propertyId int(11) NOT NULL auto_increment,
  name text NOT NULL,
  value text NOT NULL,
  PRIMARY KEY  (propertyId)
) TYPE=InnoDB;

DROP TABLE IF EXISTS cmSubscription;

CREATE TABLE cmSubscription (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  interceptionPointId INTEGER UNSIGNED NOT NULL,
  name varchar(100) NOT NULL,
  isGlobal tinyint(4) NOT NULL default '0',
  entityName varchar(100),
  entityId varchar(200) DEFAULT NULL,
  userName varchar(150) NOT NULL,
  userEmail varchar(150),
  lastNotifiedDateTime timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY(id)
) TYPE = InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS cmSubscriptionFilter;

CREATE TABLE cmSubscriptionFilter (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  subscriptionId INTEGER UNSIGNED NOT NULL,
  filterType varchar(50) NOT NULL,
  filterCondition varchar(255) NOT NULL,
  isAndCondition tinyint(4) NOT NULL default '1',
  PRIMARY KEY(id)
) TYPE = InnoDB DEFAULT CHARSET=utf8;

INSERT INTO cmInfoGlueProperties(name, value) VALUES
  ('version', '2.9');
 
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
 
COMMIT;

INSERT INTO cmContentTypeDefinition (contentTypeDefinitionId, schemaValue, name) VALUES
  ('1','<?xml version=\"1.0\" encoding=\"utf-8\"?><xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" version=\"2.0\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:simpleType name=\"textarea\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:simpleType name=\"radiobutton\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:simpleType name=\"checkbox\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:simpleType name=\"select\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:simpleType name=\"textfield\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:complexType name=\"Image\"><xs:all><xs:element name=\"Attributes\"><xs:complexType><xs:all><xs:element name=\"Title\" type=\"textfield\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined11\" label=\"Title - required\"/></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined87\" label=\"This is the image title\"/></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined80\" label=\"longtextfield\"/></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"NavigationTitle\" type=\"textfield\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined27\" label=\"Navigation title - required\"/></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined73\" label=\"This is the label shown on links to this image\"/></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined28\" label=\"longtextfield\"/></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"Alt\" type=\"textfield\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined75\" label=\"Alt text\"/></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined20\" label=\"This is the tooltip text for an image\"/></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined86\" label=\"longtextfield\"/></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"FullText\" type=\"textarea\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined93\" label=\"Full text\"/></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined77\" label=\"Here you can put in a image description\"/></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined52\" label=\"normaltextarea\"/></values></param><param id=\"width\" inputTypeId=\"0\"><values><value id=\"width\" label=\"700\"/></values></param><param id=\"height\" inputTypeId=\"0\"><values><value id=\"height\" label=\"150\"/></values></param><param id=\"enableWYSIWYG\" inputTypeId=\"0\"><values><value id=\"enableWYSIWYG\" label=\"false\"/></values></param><param id=\"enableTemplateEditor\" inputTypeId=\"0\"><values><value id=\"enableTemplateEditor\" label=\"false\"/></values></param></params></xs:appinfo></xs:annotation></xs:element></xs:all></xs:complexType></xs:element></xs:all></xs:complexType></xs:schema>','Image');
#endquery
INSERT INTO cmContentTypeDefinition (contentTypeDefinitionId, schemaValue, name) VALUES
  ('2','<?xml version=\"1.0\" encoding=\"utf-8\"?><xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" version=\"2.0\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:simpleType name=\"textarea\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"radiobutton\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"checkbox\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"select\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"textfield\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:complexType name=\"Meta info\"><xs:all><xs:element name=\"Attributes\"><xs:complexType><xs:all><xs:element name=\"Title\" type=\"textfield\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined75\" label=\"Title - required\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined52\" label=\"Used in the page title\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined94\" label=\"longtextfield\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"NavigationTitle\" type=\"textfield\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined35\" label=\"Navigation title (required)\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined0\" label=\"Used in navigation elements pointing to the page\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined20\" label=\"longtextfield\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"Description\" type=\"textfield\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined31\" label=\"Description\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined86\" label=\"A short description of the page\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined15\" label=\"longtextfield\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"MetaInfo\" type=\"textarea\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined85\" label=\"Meta Information\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined67\" label=\"Keywords made for search engines etc.\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined70\" label=\"normaltextarea\"></value></values></param><param id=\"width\" inputTypeId=\"0\"><values><value id=\"width\" label=\"700\"></value></values></param><param id=\"height\" inputTypeId=\"0\"><values><value id=\"height\" label=\"150\"></value></values></param><param id=\"enableWYSIWYG\" inputTypeId=\"0\"><values><value id=\"enableWYSIWYG\" label=\"false\"></value></values></param><param id=\"enableTemplateEditor\" inputTypeId=\"0\"><values><value id=\"enableTemplateEditor\" label=\"false\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="ComponentStructure" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined67" label="ComponentStructure"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined38" label="ComponentStructure"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined73" label="normaltextarea"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param><param id="enableFormEditor" inputTypeId="0"><values><value id="enableFormEditor" label="false"></value></values></param><param id="enableRelationEditor" inputTypeId="0"><values><value id="enableRelationEditor" label="false"></value></values></param></params></xs:appinfo></xs:annotation></xs:element></xs:all></xs:complexType></xs:element></xs:all></xs:complexType></xs:schema>','Meta info');
#endquery
INSERT INTO cmContentTypeDefinition (contentTypeDefinitionId, schemaValue, name) VALUES
  ('3','<?xml version=\"1.0\" encoding=\"utf-8\"?><xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" version=\"2.0\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:simpleType name=\"textarea\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"radiobutton\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"checkbox\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"select\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"textfield\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:complexType name=\"Article\"><xs:all><xs:element name=\"Attributes\"><xs:complexType><xs:all><xs:element name=\"Title\" type=\"textfield\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined34\" label=\"Title\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined53\" label=\"This represents the article title\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined94\" label=\"longtextfield\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"NavigationTitle\" type=\"textfield\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined58\" label=\"Navigation title\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined37\" label=\"This represents the article linktitle\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined95\" label=\"longtextfield\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"Leadin\" type=\"textarea\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined28\" label=\"Lead in text\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined79\" label=\"This is an introduction to the full text\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined70\" label=\"normaltextarea\"></value></values></param><param id=\"width\" inputTypeId=\"0\"><values><value id=\"width\" label=\"700\"></value></values></param><param id=\"height\" inputTypeId=\"0\"><values><value id=\"height\" label=\"150\"></value></values></param><param id=\"enableWYSIWYG\" inputTypeId=\"0\"><values><value id=\"enableWYSIWYG\" label=\"true\"></value></values></param><param id=\"enableTemplateEditor\" inputTypeId=\"0\"><values><value id=\"enableTemplateEditor\" label=\"false\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"FullText\" type=\"textarea\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined45\" label=\"Full text\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined8\" label=\"This is the article fulltext\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined54\" label=\"hugetextfield\"></value></values></param><param id=\"width\" inputTypeId=\"0\"><values><value id=\"width\" label=\"700\"></value></values></param><param id=\"height\" inputTypeId=\"0\"><values><value id=\"height\" label=\"500\"></value></values></param><param id=\"enableWYSIWYG\" inputTypeId=\"0\"><values><value id=\"enableWYSIWYG\" label=\"true\"></value></values></param><param id=\"enableTemplateEditor\" inputTypeId=\"0\"><values><value id=\"enableTemplateEditor\" label=\"false\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"RelatedArticles\" type=\"textarea\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined75\" label=\"Related Articles\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined5\" label=\"Here you can add related articles\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined57\" label=\"normaltextarea\"></value></values></param><param id=\"width\" inputTypeId=\"0\"><values><value id=\"width\" label=\"700\"></value></values></param><param id=\"height\" inputTypeId=\"0\"><values><value id=\"height\" label=\"150\"></value></values></param><param id=\"enableWYSIWYG\" inputTypeId=\"0\"><values><value id=\"enableWYSIWYG\" label=\"false\"></value></values></param><param id=\"enableTemplateEditor\" inputTypeId=\"0\"><values><value id=\"enableTemplateEditor\" label=\"false\"></value></values></param><param id=\"enableFormEditor\" inputTypeId=\"0\"><values><value id=\"enableFormEditor\" label=\"false\"></value></values></param><param id=\"enableRelationEditor\" inputTypeId=\"0\"><values><value id=\"enableRelationEditor\" label=\"true\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="RelatedAreas" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined93" label="Related areas"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined30" label="Points out related areas on the site"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined83" label="normaltextfield"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param><param id="enableFormEditor" inputTypeId="0"><values><value id="enableFormEditor" label="false"></value></values></param><param id="enableContentRelationEditor" inputTypeId="0"><values><value id="enableContentRelationEditor" label="false"></value></values></param><param id="enableStructureRelationEditor" inputTypeId="0"><values><value id="enableStructureRelationEditor" label="true"></value></values></param></params></xs:appinfo></xs:annotation></xs:element></xs:all></xs:complexType></xs:element></xs:all></xs:complexType></xs:schema>','Article');
#endquery
INSERT INTO cmContentTypeDefinition (contentTypeDefinitionId, schemaValue, name) VALUES
  ('4','<?xml version=\"1.0\" encoding=\"utf-8\"?><xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" version=\"2.0\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:simpleType name=\"textarea\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:simpleType name=\"radiobutton\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:simpleType name=\"checkbox\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:simpleType name=\"select\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:simpleType name=\"textfield\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:complexType name=\"HTMLTemplate\"><xs:all><xs:element name=\"Attributes\"><xs:complexType><xs:all><xs:element name=\"Name\" type=\"textfield\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined7\" label=\"Name\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined82\" label=\"This is the name of the template\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined61\" label=\"longtextfield\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"Template\" type=\"textarea\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined16\" label=\"Template HTML\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined90\" label=\"This is the html for the template \"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined12\" label=\"hugetextfield\"></value></values></param><param id=\"width\" inputTypeId=\"0\"><values><value id=\"width\" label=\"700\"></value></values></param><param id=\"height\" inputTypeId=\"0\"><values><value id=\"height\" label=\"500\"></value></values></param><param id=\"enableWYSIWYG\" inputTypeId=\"0\"><values><value id=\"enableWYSIWYG\" label=\"false\"></value></values></param><param id=\"enableTemplateEditor\" inputTypeId=\"0\"><values><value id=\"enableTemplateEditor\" label=\"false\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="ComponentProperties" type="textarea"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined89" label="ComponentProperties"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined40" label="ComponentProperties"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined93" label="normaltextarea"></value></values></param><param id="width" inputTypeId="0"><values><value id="width" label="700"></value></values></param><param id="height" inputTypeId="0"><values><value id="height" label="150"></value></values></param><param id="enableWYSIWYG" inputTypeId="0"><values><value id="enableWYSIWYG" label="false"></value></values></param><param id="enableTemplateEditor" inputTypeId="0"><values><value id="enableTemplateEditor" label="false"></value></values></param><param id="enableFormEditor" inputTypeId="0"><values><value id="enableFormEditor" label="false"></value></values></param><param id="enableRelationEditor" inputTypeId="0"><values><value id="enableRelationEditor" label="false"></value></values></param><param id="enableComponentPropertiesEditor" inputTypeId="0"><values><value id="enableComponentPropertiesEditor" label="true"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name="GroupName" type="select"><xs:annotation><xs:appinfo><params><param id="title" inputTypeId="0"><values><value id="undefined89" label="Group Name"></value></values></param><param id="description" inputTypeId="0"><values><value id="undefined94" label="The name of the group the component should be in"></value></values></param><param id="class" inputTypeId="0"><values><value id="undefined63" label="normaltextfield"></value></values></param><param id="values" inputTypeId="1"><values><value id="Basic Pages" label="Basic Pages"></value><value id="Single Content" label="Single Content"></value><value id="Content Iterators" label="Content Iterators"></value><value id="Navigation" label="Navigation"></value><value id="Layout" label="Layout"></value><value id="Templates" label="Templates"></value><value id="Other" label="Other"></value></values></param></params></xs:appinfo></xs:annotation></xs:element></xs:all></xs:complexType></xs:element></xs:all></xs:complexType></xs:schema>','HTMLTemplate');
#endquery
INSERT INTO cmContentTypeDefinition (contentTypeDefinitionId, schemaValue, name) VALUES
  ('5','<?xml version=\"1.0\" encoding=\"utf-8\"?><xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" version=\"2.0\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:simpleType name=\"textarea\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"radiobutton\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"checkbox\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"select\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"textfield\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:complexType name=\"Article\"><xs:all><xs:element name=\"Attributes\"><xs:complexType><xs:all><xs:element name=\"HTMLFormular\" type=\"textarea\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined29\" label=\"HTMLFormular\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined28\" label=\"This area contains the formular\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined15\" label=\"normaltextarea\"></value></values></param><param id=\"width\" inputTypeId=\"0\"><values><value id=\"width\" label=\"700\"></value></values></param><param id=\"height\" inputTypeId=\"0\"><values><value id=\"height\" label=\"150\"></value></values></param><param id=\"enableWYSIWYG\" inputTypeId=\"0\"><values><value id=\"enableWYSIWYG\" label=\"false\"></value></values></param><param id=\"enableTemplateEditor\" inputTypeId=\"0\"><values><value id=\"enableTemplateEditor\" label=\"false\"></value></values></param><param id=\"enableFormEditor\" inputTypeId=\"0\"><values><value id=\"enableFormEditor\" label=\"true\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"FormName\" type=\"textfield\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined25\" label=\"FormName\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined62\" label=\"This name is used to reach a form by name in Javascript for example\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined77\" label=\"longtextfield\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"FormMethod\" type=\"select\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined27\" label=\"Method\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined54\" label=\"This is the method used for sending data\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined24\" label=\"longtextfield\"></value></values></param><param id=\"values\" inputTypeId=\"1\"><values><value id=\"post\" label=\"POST\"></value><value id=\"get\" label=\"GET\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"FormAction\" type=\"select\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined44\" label=\"Action\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined29\" label=\"This is the action we send the form values to\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined36\" label=\"longtextfield\"></value></values></param><param id=\"values\" inputTypeId=\"1\"><values><value id=\"InfoGlueDefaultInputHandler.action\" label=\"Default Handler\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"InputHandlerClassName\" type=\"select\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined70\" label=\"Input handler\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined68\" label=\"This decides what procedure to invoke with the data\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined77\" label=\"longtextfield\"></value></values></param><param id=\"values\" inputTypeId=\"1\"><values><value id=\"org.infoglue.cms.applications.deliver.inputhandlers.MailSender\" label=\"Simple Mail Handler\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"MailSender_fromAddress\" type=\"textfield\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined63\" label=\"MailSender_fromAddress\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined18\" label=\"The address to give as sender in case it is sent by mail \"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined71\" label=\"longtextfield\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"MailSender_toAddress\" type=\"textfield\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined58\" label=\"MailSender_toAddress\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined33\" label=\"The address to send the form data to\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined10\" label=\"longtextfield\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"MailSender_subject\" type=\"textfield\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined26\" label=\"MailSender_subject\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined85\" label=\"The subject to give if the data is sent as mail\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined42\" label=\"longtextfield\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"MailSender_template\" type=\"textarea\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined68\" label=\"MailSender_template\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined50\" label=\"This is the template that formats the mail\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined55\" label=\"normaltextarea\"></value></values></param><param id=\"width\" inputTypeId=\"0\"><values><value id=\"width\" label=\"700\"></value></values></param><param id=\"height\" inputTypeId=\"0\"><values><value id=\"height\" label=\"150\"></value></values></param><param id=\"enableWYSIWYG\" inputTypeId=\"0\"><values><value id=\"enableWYSIWYG\" label=\"false\"></value></values></param><param id=\"enableTemplateEditor\" inputTypeId=\"0\"><values><value id=\"enableTemplateEditor\" label=\"true\"></value></values></param><param id=\"enableFormEditor\" inputTypeId=\"0\"><values><value id=\"enableFormEditor\" label=\"false\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element></xs:all>\r\n\t    </xs:complexType>\r\n\t  </xs:element>\r\n\t  </xs:all>\r\n\t</xs:complexType>\r\n<xs:simpleType name=\"assetKeys\"><xs:restriction base=\"xs:string\"></xs:restriction></xs:simpleType></xs:schema>','HTMLFormular');
#endquery
INSERT INTO cmContentTypeDefinition (contentTypeDefinitionId, schemaValue, name) VALUES
  ('6','<?xml version=\"1.0\" encoding=\"utf-8\"?><xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" version=\"2.0\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:simpleType name=\"textarea\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"radiobutton\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"checkbox\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"select\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:simpleType name=\"textfield\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"></xs:maxLength></xs:restriction></xs:simpleType><xs:complexType name=\"Content\"><xs:all><xs:element name=\"Attributes\"><xs:complexType><xs:all><xs:element name=\"UserInputHTML\" type=\"textarea\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined64\" label=\"UserInputHTML\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined98\" label=\"UserInputHTML\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined26\" label=\"normaltextarea\"></value></values></param><param id=\"width\" inputTypeId=\"0\"><values><value id=\"width\" label=\"700\"></value></values></param><param id=\"height\" inputTypeId=\"0\"><values><value id=\"height\" label=\"150\"></value></values></param><param id=\"enableWYSIWYG\" inputTypeId=\"0\"><values><value id=\"enableWYSIWYG\" label=\"false\"></value></values></param><param id=\"enableTemplateEditor\" inputTypeId=\"0\"><values><value id=\"enableTemplateEditor\" label=\"false\"></value></values></param><param id=\"enableFormEditor\" inputTypeId=\"0\"><values><value id=\"enableFormEditor\" label=\"false\"></value></values></param><param id=\"enableRelationEditor\" inputTypeId=\"0\"><values><value id=\"enableRelationEditor\" label=\"false\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"ScriptCode\" type=\"textarea\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined22\" label=\"ScriptCode\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined90\" label=\"The code\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined99\" label=\"normaltextarea\"></value></values></param><param id=\"width\" inputTypeId=\"0\"><values><value id=\"width\" label=\"700\"></value></values></param><param id=\"height\" inputTypeId=\"0\"><values><value id=\"height\" label=\"600\"></value></values></param><param id=\"enableWYSIWYG\" inputTypeId=\"0\"><values><value id=\"enableWYSIWYG\" label=\"false\"></value></values></param><param id=\"enableTemplateEditor\" inputTypeId=\"0\"><values><value id=\"enableTemplateEditor\" label=\"false\"></value></values></param><param id=\"enableFormEditor\" inputTypeId=\"0\"><values><value id=\"enableFormEditor\" label=\"false\"></value></values></param><param id=\"enableRelationEditor\" inputTypeId=\"0\"><values><value id=\"enableRelationEditor\" label=\"false\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"UserOutputHTML\" type=\"textarea\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined63\" label=\"UserOutputHTML\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined22\" label=\"UserOutputHTML\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined28\" label=\"normaltextarea\"></value></values></param><param id=\"width\" inputTypeId=\"0\"><values><value id=\"width\" label=\"700\"></value></values></param><param id=\"height\" inputTypeId=\"0\"><values><value id=\"height\" label=\"150\"></value></values></param><param id=\"enableWYSIWYG\" inputTypeId=\"0\"><values><value id=\"enableWYSIWYG\" label=\"false\"></value></values></param><param id=\"enableTemplateEditor\" inputTypeId=\"0\"><values><value id=\"enableTemplateEditor\" label=\"false\"></value></values></param><param id=\"enableFormEditor\" inputTypeId=\"0\"><values><value id=\"enableFormEditor\" label=\"false\"></value></values></param><param id=\"enableRelationEditor\" inputTypeId=\"0\"><values><value id=\"enableRelationEditor\" label=\"false\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element></xs:all></xs:complexType></xs:element></xs:all></xs:complexType></xs:schema>','TaskDefinition');
#endquery
INSERT INTO cmContentTypeDefinition (contentTypeDefinitionId, schemaValue, name) VALUES
  ('7','<?xml version=\"1.0\" encoding=\"utf-8\"?><xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" version=\"2.0\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:simpleType name=\"textarea\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:simpleType name=\"radiobutton\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:simpleType name=\"checkbox\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:simpleType name=\"select\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:simpleType name=\"textfield\"><xs:restriction base=\"xs:string\"><xs:maxLength value=\"100\"/></xs:restriction></xs:simpleType><xs:complexType name=\"PageTemplate\"><xs:all><xs:element name=\"Attributes\"><xs:complexType><xs:all><xs:element name=\"Name\" type=\"textfield\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined7\" label=\"Name\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined82\" label=\"This is the name of the page template\"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined61\" label=\"longtextfield\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element><xs:element name=\"ComponentStructure\" type=\"textarea\"><xs:annotation><xs:appinfo><params><param id=\"title\" inputTypeId=\"0\"><values><value id=\"undefined16\" label=\"ComponentStructure\"></value></values></param><param id=\"description\" inputTypeId=\"0\"><values><value id=\"undefined90\" label=\"This is the page template structure \"></value></values></param><param id=\"class\" inputTypeId=\"0\"><values><value id=\"undefined12\" label=\"hugetextfield\"></value></values></param><param id=\"width\" inputTypeId=\"0\"><values><value id=\"width\" label=\"700\"></value></values></param><param id=\"height\" inputTypeId=\"0\"><values><value id=\"height\" label=\"500\"></value></values></param><param id=\"enableWYSIWYG\" inputTypeId=\"0\"><values><value id=\"enableWYSIWYG\" label=\"false\"></value></values></param><param id=\"enableTemplateEditor\" inputTypeId=\"0\"><values><value id=\"enableTemplateEditor\" label=\"false\"></value></values></param></params></xs:appinfo></xs:annotation></xs:element></xs:all></xs:complexType></xs:element></xs:all></xs:complexType></xs:schema>','PageTemplate');
#endquery

INSERT INTO cmAvailableServiceBinding (availableServiceBindingId, name, description, visualizationAction, isMandatory, isUserEditable, isInheritable) VALUES
  ('1','Template','The page template-file','ViewListTemplate.action','1','1','1');
#endquery
INSERT INTO cmAvailableServiceBinding (availableServiceBindingId, name, description, visualizationAction, isMandatory, isUserEditable, isInheritable) VALUES
  ('2','Meta information','The keywords and other metainfo for this page','ViewContentTreeForServiceBinding.action','1','1','1');
#endquery

INSERT INTO cmAvailableServiceBindingSiteNodeTypeDefinition (availableServiceBindingSiteNodeTypeDefinitionId, availableServiceBindingId, siteNodeTypeDefinitionId) VALUES
  ('1','1','2');
#endquery
INSERT INTO cmAvailableServiceBindingSiteNodeTypeDefinition (availableServiceBindingSiteNodeTypeDefinitionId, availableServiceBindingId, siteNodeTypeDefinitionId) VALUES
  ('2','2','2');
#endquery
INSERT INTO cmAvailableServiceBindingSiteNodeTypeDefinition (availableServiceBindingSiteNodeTypeDefinitionId, availableServiceBindingId, siteNodeTypeDefinitionId) VALUES
  ('3','2','1');
#endquery

INSERT INTO cmLanguage (languageId, name, languageCode, charset) VALUES
  ('1','English','en', 'utf-8');
#endquery
INSERT INTO cmLanguage (languageId, name, languageCode, charset) VALUES
  ('2','German','de', 'utf-8');
#endquery
INSERT INTO cmLanguage (languageId, name, languageCode, charset) VALUES
  ('3','Swedish','sv', 'utf-8');
#endquery

INSERT INTO cmRepository (repositoryId, name, description, dnsName) VALUES
  ('1','testsite.org','Sample repository','');
#endquery

INSERT INTO cmRepositoryLanguage (repositoryLanguageId, repositoryId, languageId, isPublished, sortOrder) VALUES
  ('1','1','1','0','0');
#endquery
INSERT INTO cmRepositoryLanguage (repositoryLanguageId, repositoryId, languageId, isPublished, sortOrder) VALUES
  ('2','1','2','0','0');
#endquery
INSERT INTO cmRepositoryLanguage (repositoryLanguageId, repositoryId, languageId, isPublished, sortOrder) VALUES
  ('3','1','3','0','0');
#endquery

INSERT INTO cmRole (roleName, description) VALUES
  ('administrators','This is the most priviliged group');
#endquery
INSERT INTO cmRole (roleName, description) VALUES
  ('cmsUser','Must be present to allow any ordinary user to get access.');
#endquery
INSERT INTO cmRole (roleName, description) VALUES
  ('anonymous','Must be present to model the default anonymous extranet role.');
#endquery

INSERT INTO cmServiceDefinition (serviceDefinitionId, className, name, description) VALUES
  ('1','org.infoglue.cms.services.CoreContentService','Core content service','Core content service');
#endquery
INSERT INTO cmServiceDefinition (serviceDefinitionId, className, name, description) VALUES
  ('2','org.infoglue.cms.services.CoreStructureService','Core structure service','The local structure-service');
#endquery

INSERT INTO cmServiceDefinitionAvailableServiceBinding (serviceDefinitionAvailableServiceBindingId, serviceDefinitionId, availableServiceBindingId) VALUES
  ('1','1','1');
#endquery
INSERT INTO cmServiceDefinitionAvailableServiceBinding (serviceDefinitionAvailableServiceBindingId, serviceDefinitionId, availableServiceBindingId) VALUES
  ('2','1','2');
#endquery

INSERT INTO cmSiteNodeTypeDefinition (siteNodeTypeDefinitionId, invokerClassName, name, description) VALUES
  ('1','org.infoglue.deliver.invokers.ComponentBasedHTMLPageInvoker','ComponentPage','A component based page type');
#endquery
INSERT INTO cmSiteNodeTypeDefinition (siteNodeTypeDefinitionId, invokerClassName, name, description) VALUES
  ('2','org.infoglue.deliver.invokers.HTMLPageInvoker','HTMLPage','Old template based page type');
#endquery

INSERT INTO cmSystemUser (userName, password, firstName, lastName, email) VALUES
  ('administrator','changeit','System','Administrator','administrator@your.domain');
#endquery
INSERT INTO cmSystemUser (userName, password, firstName, lastName, email) VALUES
  ('anonymous','anonymous','Anonymous','User','anonymous@infoglue.org');
#endquery

INSERT INTO cmSystemUserRole (userName, roleName) VALUES
  ('administrator','administrators');
#endquery
INSERT INTO cmSystemUserRole (userName, roleName) VALUES
  ('administrator','cmsUser');
#endquery
INSERT INTO cmSystemUserRole (userName, roleName) VALUES
  ('anonymous','anonymous');
#endquery

INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (1,'Repository','Repository.Read','Gives a user access to look at a repository',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (2,'ManagementTool','ManagementTool.Read','Gives a user access to the management tool',0);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (3,'ContentTool','ContentTool.Read','Gives a user access to the content tool',0);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (4,'StructureTool','StructureTool.Read','Gives a user access to the structure tool',0);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (5,'PublishingTool','PublishingTool.Read','Gives a user access to the publishing tool',0);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (6,'Content','Content.Read','Intercepts the read of a content',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (7,'Content','Content.Write','Intercepts the write of a content',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (8,'SiteNodeVersion','SiteNodeVersion.Read','Intercepts the read of a SiteNodeVersion',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (9,'SiteNodeVersion','SiteNodeVersion.Write','Intercepts the write of a SiteNodeVersion',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (10,'Content','Content.Create','Intercepts the creation of a new content or folder',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (11,'Content','Content.Delete','Intercepts the deletion of a content',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (12,'Content','Content.Move','Intercepts the movement of a content',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (13,'Content','Content.SubmitToPublish','Intercepts the submittance to publish of all content versions',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (14,'Content','Content.ChangeAccessRights','Intercepts the attempt to change access rights',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (15,'Content','Content.CreateVersion','Intercepts the creation of a new contentversion',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (16,'ContentVersion','ContentVersion.Delete','Intercepts the deletion of a contentversion',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (17,'ContentVersion','ContentVersion.Write','Intercepts the editing of a contentversion',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (18,'ContentVersion','ContentVersion.Read','Intercepts the read of a contentversion',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (19,'SiteNodeVersion','SiteNodeVersion.CreateSiteNode','Intercepts the creation of a new sitenode',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (20,'SiteNodeVersion','SiteNodeVersion.DeleteSiteNode','Intercepts the deletion of a sitenode',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (21,'SiteNodeVersion','SiteNodeVersion.MoveSiteNode','Intercepts the movement of a sitenode',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (22,'SiteNodeVersion','SiteNodeVersion.SubmitToPublish','Intercepts the submittance to publish of all content versions',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (23,'SiteNodeVersion','SiteNodeVersion.ChangeAccessRights','Intercepts the attempt to change access rights',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES
  (24,'ContentVersion','ContentVersion.Publish','Intercepts the direct publishing of a content version',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES
  (25,'SiteNodeVersion','SiteNodeVersion.Publish','Intercepts the direct publishing of a siteNode version',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (26,'MyDesktopTool','MyDesktopTool.Read','Gives a user access to the MyDesktop tool',0);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (27,'ContentTypeDefinition','ContentTypeDefinition.Read','This point checks access to read/use a content type definition',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (28,'Category','Category.Read','This point checks access to read/use a category',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (29,'Publication','Publication.Write','This point intercepts a new publication',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (30,'Repository','Repository.ReadForBinding','This point intercepts when a user tries to read the repository in a binding dialog',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (31,'Workflow','Workflow.Create','This point checks access to creating a new workflow',1);
#endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (32,'StructureTool','StructureTool.SaveTemplate','This interception point limits who get the save-button in the toolbar',0);
#endquery
 
INSERT INTO cmInterceptor (interceptorId, name, className, description) VALUES
  (1,'InfoGlue Common Access Rights Interceptor','org.infoglue.cms.security.interceptors.InfoGlueCommonAccessRightsInterceptor','Takes care of bla');
#endquery

INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (1, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (2, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (3, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (4, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (5, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (6, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (7, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (8, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (9, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (10, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (11, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (12, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (13, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (14, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (15, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (16, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (17, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (18, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (19, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (20, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (21, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (22, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (23, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (24, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (25, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (26, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (27, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (28, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (29, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (30, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (31, 1);
#endquery
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (32, 1);
#endquery

INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (1, '1', 1);
#endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (2, '2', 1);
#endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (3, '3', 1);
#endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (4, '4', 1);
#endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (5, NULL, 3);
#endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (6, NULL, 2);
#endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (7, NULL, 4);
#endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (8, NULL, 5);
#endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (9, NULL, 26);
#endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (10, NULL, 32);
#endquery

INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (1, 1, 'administrators');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (2, 1, 'cmsUser');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (3, 2, 'administrators');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (4, 2, 'cmsUser');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (5, 3, 'administrators');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (6, 3, 'cmsUser');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (7, 4, 'administrators');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (8, 4, 'cmsUser');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (9, 5, 'administrators');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (10, 5, 'cmsUser');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (11, 6, 'administrators');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (12, 6, 'cmsUser');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (13, 7, 'administrators');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (14, 7, 'cmsUser');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (15, 8, 'administrators');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (16, 8, 'cmsUser');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (17, 9, 'administrators');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (18, 9, 'cmsUser');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (19, 10, 'administrators');
#endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (20, 10, 'cmsUser');
#endquery



