alter table OS_CURRENTSTEP change OWNER OWNER varchar(255);
alter table OS_HISTORYSTEP change OWNER OWNER varchar(255);
alter table OS_CURRENTSTEP change CALLER CALLER varchar(255);
alter table OS_HISTORYSTEP change CALLER CALLER varchar(255);

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

INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (31,'Workflow','Workflow.Create','This point checks access to creating a new workflow',1);
INSERT INTO cmInterceptionPointInterceptor (interceptionPointId, interceptorId) VALUES
  (31, 1);

alter table cmSiteNodeVersion add pageCacheKey varchar(255) NOT NULL default 'default';

CREATE TABLE cmRedirect (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  url TEXT NOT NULL,
  redirectUrl TEXT NOT NULL,
  PRIMARY KEY(id)
) TYPE = MYISAM;

ALTER TABLE cmRedirect DROP INDEX redirectUrl;
ALTER TABLE cmRedirect ADD INDEX redirectUrl(redirectUrl(255));

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