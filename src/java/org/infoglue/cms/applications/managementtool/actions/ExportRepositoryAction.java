/* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */

package org.infoglue.cms.applications.managementtool.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.impl.simple.InfoGlueExportImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.handlers.DigitalAssetBytesHandler;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;


/**
 * This class handles Exporting of a repository to an XML-file.
 * 
 * @author mattias
 */

public class ExportRepositoryAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ExportRepositoryAction.class.getName());

	private Integer repositoryId = null;
	private List repositories = new ArrayList();
	
	private String fileUrl 	= "";
	private String fileName = "";
	private String exportFileName = null;
	private int assetMaxSize = -1;
	
	/**
	 * This shows the dialog before export.
	 * @return
	 * @throws Exception
	 */	

	public String doInput() throws Exception
	{
		repositories = RepositoryController.getController().getRepositoryVOListNotMarkedForDeletion();
		
		return "input";
	}
	
	/**
	 * This handles the actual exporting.
	 */
	
	protected String doExecute() throws Exception 
	{
		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			Mapping map = new Mapping();
			String exportFormat = CmsPropertyHandler.getExportFormat();

			if(exportFormat.equalsIgnoreCase("2"))
			{
				logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site_2.5.xml").toString());
				map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site_2.5.xml").toString());
			}
			else
			{
				logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site.xml").toString());
				map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site.xml").toString());
			}
			
			// All ODMG database access requires a transaction
			db.begin();

			List<SiteNode> siteNodes = new ArrayList<SiteNode>();
			List<Content> contents = new ArrayList<Content>();
			Hashtable<String,String> allRepositoryProperties = new Hashtable<String,String>();
			Hashtable<String,String> allSiteNodeProperties = new Hashtable<String,String>();
			Hashtable<String,String> allContentProperties = new Hashtable<String,String>();
			List<AccessRight> allAccessRights = new ArrayList<AccessRight>();
			//List<AccessRight> allAccessRights = AccessRightController.getController().getAllAccessRightListForExportReadOnly(db);
			
			//TEST
			Map args = new HashMap();
		    args.put("globalKey", "infoglue");
		    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
		    //END TEST
			
			String names = "";
			String[] repositories = getRequest().getParameterValues("repositoryId");
			for(int i=0; i<repositories.length; i++)
			{
				Integer repositoryId = new Integer(repositories[i]);
				Repository repository 	= RepositoryController.getController().getRepositoryWithId(repositoryId, db);
				SiteNode siteNode 		= SiteNodeController.getController().getRootSiteNode(repositoryId, db);
				Content content 		= ContentController.getContentController().getRootContent(repositoryId, db);

			    InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Repository.Read", db);
			    if(interceptionPointVO != null)
			    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), repository.getId().toString(), db));

			    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Repository.ReadForBinding", db);
			    if(interceptionPointVO != null)
			    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), repository.getId().toString(), db));

				getContentPropertiesAndAccessRights(ps, allContentProperties, allAccessRights, content, db);
				getSiteNodePropertiesAndAccessRights(ps, allSiteNodeProperties, allAccessRights, siteNode, db);
				
				siteNodes.add(siteNode);
				contents.add(content);
				names = names + "_" + repository.getName();
				allRepositoryProperties.putAll(getRepositoryProperties(ps, repositoryId));
			}
			
			List contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionList(db);
			List categories = CategoryController.getController().findAllActiveCategories();
			
			InfoGlueExportImpl infoGlueExportImpl = new InfoGlueExportImpl();
			
			VisualFormatter visualFormatter = new VisualFormatter();
			names = new VisualFormatter().replaceNonAscii(names, '_');

			if(repositories.length > 2 || names.length() > 40)
				names = "" + repositories.length + "_repositories";
			
			String fileName = "Export_" + names + "_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm") + ".xml";
			if(exportFileName != null && !exportFileName.equals(""))
				fileName = exportFileName;
			
			String filePath = CmsPropertyHandler.getDigitalAssetPath();
			String fileSystemName =  filePath + File.separator + fileName;
						
			fileUrl = CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + fileName;
			this.fileName = fileName;
						
			String encoding = "UTF-8";
			File file = new File(fileSystemName);
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
            Marshaller marshaller = new Marshaller(osw);
            marshaller.setMapping(map);
			marshaller.setEncoding(encoding);
			DigitalAssetBytesHandler.setMaxSize(assetMaxSize);

			infoGlueExportImpl.getRootContent().addAll(contents);
			infoGlueExportImpl.getRootSiteNode().addAll(siteNodes);
			
			infoGlueExportImpl.setContentTypeDefinitions(contentTypeDefinitions);
			infoGlueExportImpl.setCategories(categories);
			
			infoGlueExportImpl.setRepositoryProperties(allRepositoryProperties);
			infoGlueExportImpl.setContentProperties(allContentProperties);
			infoGlueExportImpl.setSiteNodeProperties(allSiteNodeProperties);
			infoGlueExportImpl.setAccessRights(allAccessRights);
			
			marshaller.marshal(infoGlueExportImpl);
			
			osw.flush();
			osw.close();
			
			db.rollback();
			db.close();

		} 
		catch (Exception e) 
		{
			logger.error("An error was found exporting a repository: " + e.getMessage(), e);
			db.rollback();
		}
		
		return "success";
	}


	public static void getContentPropertiesAndAccessRights(PropertySet ps, Hashtable<String, String> allContentProperties, List<AccessRight> allAccessRights, Content content, Database db) throws SystemException
	{
		String allowedContentTypeNames = ps.getString("content_" + content.getId() + "_allowedContentTypeNames");
		if ( allowedContentTypeNames != null && !allowedContentTypeNames.equals(""))
	    {
        	allContentProperties.put("content_" + content.getId() + "_allowedContentTypeNames", allowedContentTypeNames);
	    }

		if(ps.exists("content_" + content.getId() + "_defaultContentTypeName"))
			allContentProperties.put("content_" + content.getId() + "_defaultContentTypeName", "" + ps.getString("content_" + content.getId() + "_defaultContentTypeName"));
	    if(ps.exists("content_" + content.getId() + "_initialLanguageId"))
	    	allContentProperties.put("content_" + content.getId() + "_initialLanguageId", "" + ps.getString("content_" + content.getId() + "_initialLanguageId"));

	    InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Content.Read", db);
	    if(interceptionPointVO != null)
	    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), content.getId().toString(), db));

	    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Content.Write", db);
	    if(interceptionPointVO != null)
	    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), content.getId().toString(), db));

	    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Content.Create", db);
	    if(interceptionPointVO != null)
	    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), content.getId().toString(), db));

	    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Content.Delete", db);
	    if(interceptionPointVO != null)
	    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), content.getId().toString(), db));

	    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Content.Move", db);
	    if(interceptionPointVO != null)
	    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), content.getId().toString(), db));

	    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Content.SubmitToPublish", db);
	    if(interceptionPointVO != null)
	    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), content.getId().toString(), db));
	    
	    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Content.ChangeAccessRights", db);
	    if(interceptionPointVO != null)
	    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), content.getId().toString(), db));

	    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Content.CreateVersion", db);
	    if(interceptionPointVO != null)
	    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), content.getId().toString(), db));

	    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("ContentVersion.Delete", db);
	    if(interceptionPointVO != null)
	    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), content.getId().toString(), db));

	    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("ContentVersion.Write", db);
	    if(interceptionPointVO != null)
	    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), content.getId().toString(), db));

	    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("ContentVersion.Read", db);
	    if(interceptionPointVO != null)
	    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), content.getId().toString(), db));

	    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("ContentVersion.Publish", db);
	    if(interceptionPointVO != null)
	    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), content.getId().toString(), db));
        
        Iterator childContents = content.getChildren().iterator();
        while(childContents.hasNext())
        {
        	Content childContent = (Content)childContents.next();
        	getContentPropertiesAndAccessRights(ps, allContentProperties, allAccessRights, childContent, db);
        }
	}

	public static void getSiteNodePropertiesAndAccessRights(PropertySet ps, Hashtable<String, String> allSiteNodeProperties, List<AccessRight> allAccessRights, SiteNode siteNode, Database db) throws SystemException, Exception
	{
	    String disabledLanguagesString = "" + ps.getString("siteNode_" + siteNode.getId() + "_disabledLanguages");
	    String enabledLanguagesString = "" + ps.getString("siteNode_" + siteNode.getId() + "_enabledLanguages");

	    if(disabledLanguagesString != null && !disabledLanguagesString.equals("") && !disabledLanguagesString.equals("null"))
	    	allSiteNodeProperties.put("siteNode_" + siteNode.getId() + "_disabledLanguages", disabledLanguagesString);
	    if(enabledLanguagesString != null && !enabledLanguagesString.equals("") && !enabledLanguagesString.equals("null"))
		    allSiteNodeProperties.put("siteNode_" + siteNode.getId() + "_enabledLanguages", enabledLanguagesString);
        
        SiteNodeVersionVO latestSiteNodeVersionVO = SiteNodeVersionController.getController().getLatestActiveSiteNodeVersionVO(db, siteNode.getId());
        
        if(latestSiteNodeVersionVO != null)
        {
	        InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("SiteNodeVersion.Read", db);
		    if(interceptionPointVO != null)
		    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), latestSiteNodeVersionVO.getId().toString(), db));
	
		    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("SiteNodeVersion.Write", db);
		    if(interceptionPointVO != null)
		    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), latestSiteNodeVersionVO.getId().toString(), db));
	
		    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("SiteNodeVersion.CreateSiteNode", db);
		    if(interceptionPointVO != null)
		    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), latestSiteNodeVersionVO.getId().toString(), db));
	
		    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("SiteNodeVersion.DeleteSiteNode", db);
		    if(interceptionPointVO != null)
		    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), latestSiteNodeVersionVO.getId().toString(), db));
	
		    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("SiteNodeVersion.MoveSiteNode", db);
		    if(interceptionPointVO != null)
		    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), latestSiteNodeVersionVO.getId().toString(), db));
	
		    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("SiteNodeVersion.SubmitToPublish", db);
		    if(interceptionPointVO != null)
		    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), latestSiteNodeVersionVO.getId().toString(), db));
	
		    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("SiteNodeVersion.ChangeAccessRights", db);
		    if(interceptionPointVO != null)
		    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), latestSiteNodeVersionVO.getId().toString(), db));
	
		    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("SiteNodeVersion.Publish", db);
		    if(interceptionPointVO != null)
		    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), latestSiteNodeVersionVO.getId().toString(), db));
        }
        
        Iterator childSiteNodes = siteNode.getChildSiteNodes().iterator();
        while(childSiteNodes.hasNext())
        {
        	SiteNode childSiteNode = (SiteNode)childSiteNodes.next();
        	getSiteNodePropertiesAndAccessRights(ps, allSiteNodeProperties, allAccessRights, childSiteNode, db);
        }
	}

	public static Hashtable<String,String> getRepositoryProperties(PropertySet ps, Integer repositoryId) throws Exception
	{
		Hashtable<String,String> properties = new Hashtable<String,String>();
			    
	    byte[] WYSIWYGConfigBytes = ps.getData("repository_" + repositoryId + "_WYSIWYGConfig");
	    if(WYSIWYGConfigBytes != null)
	    	properties.put("repository_" + repositoryId + "_WYSIWYGConfig", new String(WYSIWYGConfigBytes, "utf-8"));

	    byte[] StylesXMLBytes = ps.getData("repository_" + repositoryId + "_StylesXML");
	    if(StylesXMLBytes != null)
	    	properties.put("repository_" + repositoryId + "_StylesXML", new String(StylesXMLBytes, "utf-8"));

	    byte[] extraPropertiesBytes = ps.getData("repository_" + repositoryId + "_extraProperties");
	    if(extraPropertiesBytes != null)
	    	properties.put("repository_" + repositoryId + "_extraProperties", new String(extraPropertiesBytes, "utf-8"));
	    
	    if(ps.exists("repository_" + repositoryId + "_defaultFolderContentTypeName"))
	    	properties.put("repository_" + repositoryId + "_defaultFolderContentTypeName", "" + ps.getString("repository_" + repositoryId + "_defaultFolderContentTypeName"));
	    if(ps.exists("repository_" + repositoryId + "_defaultTemplateRepository"))
		    properties.put("repository_" + repositoryId + "_defaultTemplateRepository", "" + ps.getString("repository_" + repositoryId + "_defaultTemplateRepository"));
	    if(ps.exists("repository_" + repositoryId + "_parentRepository"))
		    properties.put("repository_" + repositoryId + "_parentRepository", "" + ps.getString("repository_" + repositoryId + "_parentRepository"));

		return properties;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public String getFileName()
	{
		return fileName;
	}

	public String getFileUrl()
	{
		return fileUrl;
	}

	public Integer getRepositoryId()
	{
		return repositoryId;
	}

	public List getRepositories() 
	{
		return repositories;
	}

	public String getExportFileName()
	{
		return exportFileName;
	}

	public void setExportFileName(String exportFileName)
	{
		this.exportFileName = exportFileName;
	}

	public int getAssetMaxSize()
	{
		return assetMaxSize;
	}

	public void setAssetMaxSize(int assetMaxSize)
	{
		this.assetMaxSize = assetMaxSize;
	}

}
