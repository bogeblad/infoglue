INSERT INTO cmLanguage (languageId, name, languageCode, charset) VALUES
  (1,'English','en', 'utf-8');
--endquery
INSERT INTO cmLanguage (languageId, name, languageCode, charset) VALUES
  (2,'German','de', 'utf-8');
--endquery
INSERT INTO cmLanguage (languageId, name, languageCode, charset) VALUES
  (3,'Swedish','sv', 'utf-8');
--endquery

INSERT INTO cmRepository (repositoryId, name, description, dnsName) VALUES
  (1,'testsite.org','Sample repository','');
--endquery

INSERT INTO cmRepositoryLanguage (repositoryLanguageId, repositoryId, languageId, isPublished, sortOrder) VALUES
  (1,1,1,0,0);
--endquery
INSERT INTO cmRepositoryLanguage (repositoryLanguageId, repositoryId, languageId, isPublished, sortOrder) VALUES
  (2,2,1,0,0);
--endquery
INSERT INTO cmRepositoryLanguage (repositoryLanguageId, repositoryId, languageId, isPublished, sortOrder) VALUES
  (3,3,1,0,0);
--endquery


INSERT INTO cmRole (roleName, description) VALUES
  ('administrators','This is the most priviliged group');
--endquery
INSERT INTO cmRole (roleName, description) VALUES
  ('cmsUser','Must be present to allow any ordinary user to get access.');
--endquery
INSERT INTO cmRole (roleName, description) VALUES
  ('anonymous','Must be present to model the default anonymous extranet role.');
--endquery

INSERT INTO cmGroup (groupName, description) VALUES
  ('anonymous','Must be present to model the default anonymous extranet group.');
--endquery


INSERT INTO cmServDef (ServDefId, className, name, description) VALUES
  (1,'org.infoglue.cms.services.CoreContentService','Core content service','Core content service');
--endquery
INSERT INTO cmServDef (ServDefId, className, name, description) VALUES
  (2,'org.infoglue.cms.services.CoreStructureService','Core structure service','The local structure-service');
--endquery

INSERT INTO cmSiNoTypeDef (SiNoTypeDefId, invokerClassName, name, description) VALUES
  (1,'org.infoglue.deliver.invokers.ComponentBasedHTMLPageInvoker','ComponentPage','The new component type page');
--endquery
INSERT INTO cmSiNoTypeDef (SiNoTypeDefId, invokerClassName, name, description) VALUES
  (2,'org.infoglue.deliver.invokers.HTMLPageInvoker','Normal HTML Page','Normal page');
--endquery

INSERT INTO cmSystemUser (userName, password, firstName, lastName, email) VALUES
  ('administrator','changeit','System','Administrator','administrator@your.domain');
--endquery
INSERT INTO cmSystemUser (userName, password, firstName, lastName, email) VALUES
  ('anonymous','anonymous','Anonymous','User','anonymous@infoglue.org');
--endquery

INSERT INTO cmSystemUserRole (userName, roleName) VALUES
  ('administrator','administrators');
--endquery
INSERT INTO cmSystemUserRole (userName, roleName) VALUES
  ('administrator','cmsUser');
--endquery
INSERT INTO cmSystemUserRole (userName, roleName) VALUES
  ('anonymous','anonymous');
--endquery

INSERT INTO cmSystemUserGroup (userName, groupName) VALUES
  ('anonymous','anonymous');
--endquery

INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (1,'Repository','Repository.Read','Gives a user access to look at a repository',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (2,'ManagementTool','ManagementTool.Read','Gives a user access to the management tool',0);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (3,'ContentTool','ContentTool.Read','Gives a user access to the content tool',0);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (4,'StructureTool','StructureTool.Read','Gives a user access to the structure tool',0);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (5,'PublishingTool','PublishingTool.Read','Gives a user access to the publishing tool',0);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (6,'Content','Content.Read','Intercepts the read of a content',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (7,'Content','Content.Write','Intercepts the write of a content',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (8,'SiteNodeVersion','SiteNodeVersion.Read','Intercepts the read of a SiteNodeVersion',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (9,'SiteNodeVersion','SiteNodeVersion.Write','Intercepts the write of a SiteNodeVersion',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (10,'Content','Content.Create','Intercepts the creation of a new content or folder',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (11,'Content','Content.Delete','Intercepts the deletion of a content',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (12,'Content','Content.Move','Intercepts the movement of a content',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (13,'Content','Content.SubmitToPublish','Intercepts the submittance to publish of all content versions',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (14,'Content','Content.ChangeAccessRights','Intercepts the attempt to change access rights',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (15,'Content','Content.CreateVersion','Intercepts the creation of a new contentversion',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (16,'ContentVersion','ContentVersion.Delete','Intercepts the deletion of a contentversion',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (17,'ContentVersion','ContentVersion.Write','Intercepts the editing of a contentversion',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (18,'ContentVersion','ContentVersion.Read','Intercepts the read of a contentversion',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (19,'SiteNodeVersion','SiteNodeVersion.CreateSiteNode','Intercepts the creation of a new sitenode',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (20,'SiteNodeVersion','SiteNodeVersion.DeleteSiteNode','Intercepts the deletion of a sitenode',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (21,'SiteNodeVersion','SiteNodeVersion.MoveSiteNode','Intercepts the movement of a sitenode',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (22,'SiteNodeVersion','SiteNodeVersion.SubmitToPublish','Intercepts the submittance to publish of all content versions',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (23,'SiteNodeVersion','SiteNodeVersion.ChangeAccessRights','Intercepts the attempt to change access rights',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES
  (24,'ContentVersion','ContentVersion.Publish','Intercepts the direct publishing of a content version',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES
  (25,'SiteNodeVersion','SiteNodeVersion.Publish','Intercepts the direct publishing of a siteNode version',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (26,'MyDesktopTool','MyDesktopTool.Read','Gives a user access to the MyDesktop tool',0);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (27,'ContentTypeDefinition','ContentTypeDefinition.Read','This point checks access to read/use a content type definition',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (28,'Category','Category.Read','This point checks access to read/use a category',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (29,'Publication','Publication.Write','This point intercepts a new publication',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (30,'Repository','Repository.ReadForBinding','This point intercepts when a user tries to read the repository in a binding dialog',1);
--endquery
INSERT INTO cmInterceptionPoint (interceptionPointId, category, name, description, usesExtraDataForAccessControl) VALUES 
  (31,'Workflow','Workflow.Create','This point checks access to creating a new workflow',1);
--endquery

INSERT INTO cmInterceptor (interceptorId, name, className, description) VALUES
  (1,'InfoGlue Common Access Rights Interceptor','org.infoglue.cms.security.interceptors.InfoGlueCommonAccessRightsInterceptor','Takes care of bla');
--endquery

INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (1, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (2, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (3, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (4, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (5, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (6, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (7, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (8, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (9, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (10, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (11, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (12, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (13, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (14, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (15, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (16, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (17, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (18, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (19, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (20, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (21, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (22, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (23, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (24, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (25, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (26, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (27, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (28, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (29, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (30, 1);
--endquery
INSERT INTO cmIntPointInterceptor (interceptionPointId, interceptorId) VALUES
  (31, 1);
--endquery


INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (1, '1', 1);
--endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (2, '2', 1);
--endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (3, '3', 1);
--endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (4, '4', 1);
--endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (5, NULL, 3);
--endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (6, NULL, 2);
--endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (7, NULL, 4);
--endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (8, NULL, 5);
--endquery
INSERT INTO cmAccessRight (accessRightId, parameters, interceptionPointId) VALUES
  (9, NULL, 26);
--endquery

INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (1, 1, 'administrators');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (2, 1, 'cmsUser');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (3, 2, 'administrators');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (4, 2, 'cmsUser');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (5, 3, 'administrators');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (6, 3, 'cmsUser');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (7, 4, 'administrators');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (8, 4, 'cmsUser');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (9, 5, 'administrators');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (10, 5, 'cmsUser');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (11, 6, 'administrators');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (12, 6, 'cmsUser');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (13, 7, 'administrators');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (14, 7, 'cmsUser');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (15, 8, 'administrators');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (16, 8, 'cmsUser');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (17, 9, 'administrators');
--endquery
INSERT INTO cmAccessRightRole (accessRightRoleId, accessRightId, roleName) VALUES
  (18, 9, 'cmsUser');
--endquery
