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
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
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
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.OptimizedExportController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.impl.simple.InfoGlueExportImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.handlers.DigitalAssetBytesHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
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

	private VisualFormatter visualFormatter = new VisualFormatter();

	private Integer repositoryId = null;
	private List repositories = new ArrayList();
	
	private String fileUrl 	= "";
	private String fileName = "";
	private String exportFileName = null;
	private String exportFormat = "2";
	private Boolean onlyPublishedVersions = false;
	private int assetMaxSize = -1;
	
	private String processId = null;
	private int processStatus = -1;

	/**
	 * This deletes a process info bean and related files etc.
	 * @return
	 * @throws Exception
	 */	

	public String doDeleteProcessBean() throws Exception
	{
		if(this.processId != null)
		{
			ProcessBean pb = ProcessBean.getProcessBean(ExportRepositoryAction.class.getName(), processId);
			if(pb != null)
				pb.removeProcess();
		}
		
		return "successRedirectToProcesses";
	}

	/**
	 * This refreshes the view.
	 * @return
	 * @throws Exception
	 */	

	public String doShowProcesses() throws Exception
	{
		return "successShowProcesses";
	}

	
	public String doShowProcessesAsJSON() throws Exception
	{
		// TODO it would be nice we could write JSON to the OutputStream but we get a content already transmitted exception then.
		return "successShowProcessesAsJSON";
	}

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
	
	protected String doExecuteV3() throws Exception 
	{
		String[] repositories = getRequest().getParameterValues("repositoryId");

		String exportId = "Export_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm");
		ProcessBean processBean = ProcessBean.createProcessBean(ExportRepositoryAction.class.getName(), exportId);
		processBean.setStatus(ProcessBean.RUNNING);
		
		OptimizedExportController.export(repositories, assetMaxSize, onlyPublishedVersions, exportFileName, processBean);
		
		return "successRedirectToProcesses";
	}

	/**
	 * This handles the actual exporting.
	 */
	
	protected String doExecute() throws Exception 
	{
		String exportFormat = CmsPropertyHandler.getExportFormat();
		System.out.println("exportFormat:" + exportFormat);
		System.out.println("this.exportFormat:" + this.exportFormat);
		if(exportFormat.equalsIgnoreCase("3") || this.exportFormat.equals("3"))
		{
			logger.info("exportFormat:" + exportFormat);
			logger.info("this.exportFormat:" + this.exportFormat);
			return doExecuteV3();
		}

		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			Mapping map = new Mapping();

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

			    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Repository.Write", db);
			    if(interceptionPointVO != null)
			    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), repository.getId().toString(), db));

			    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Repository.ReadForBinding", db);
			    if(interceptionPointVO != null)
			    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), repository.getId().toString(), db));

				getContentPropertiesAndAccessRights(ps, allContentProperties, allAccessRights, content, db);
				if(siteNode != null)
					getSiteNodePropertiesAndAccessRights(ps, allSiteNodeProperties, allAccessRights, siteNode, db);
				
				if(siteNode != null)
					siteNodes.add(siteNode);
				contents.add(content);
				names = names + "_" + repository.getName();
				allRepositoryProperties.putAll(OptimizedExportController.getRepositoryProperties(ps, repositoryId));
			}
			
			List contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionList(db);
			List categories = CategoryController.getController().getAllActiveCategories();
			
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

	
	public static void getContentPropertiesAndAccessRights(PropertySet ps, Hashtable<String, String> allContentProperties, List<AccessRight> allAccessRights, Collection keys, Content content, Database db) throws SystemException
	{
		String allowedContentTypeNames = ps.getString("content_" + content.getId() + "_allowedContentTypeNames");
		if ( allowedContentTypeNames != null && !allowedContentTypeNames.equals(""))
	    {
        	allContentProperties.put("content_" + content.getId() + "_allowedContentTypeNames", allowedContentTypeNames);
	    }

		//if(ps.exists("content_" + content.getId() + "_defaultContentTypeName"))
		if(keys.contains("content_" + content.getId() + "_defaultContentTypeName"))
			allContentProperties.put("content_" + content.getId() + "_defaultContentTypeName", "" + ps.getString("content_" + content.getId() + "_defaultContentTypeName"));
	    //if(ps.exists("content_" + content.getId() + "_initialLanguageId"))
	    if(keys.contains("content_" + content.getId() + "_initialLanguageId"))
	    	allContentProperties.put("content_" + content.getId() + "_initialLanguageId", "" + ps.getString("content_" + content.getId() + "_initialLanguageId"));

        Iterator childContents = content.getChildren().iterator();
        while(childContents.hasNext())
        {
        	Content childContent = (Content)childContents.next();
        	getContentPropertiesAndAccessRights(ps, allContentProperties, allAccessRights, keys, childContent, db);
        }
	}

	public static void getSiteNodePropertiesAndAccessRights(PropertySet ps, Hashtable<String, String> allSiteNodeProperties, List<AccessRight> allAccessRights, Collection keys, SiteNode siteNode, Database db) throws SystemException, Exception
	{
		if(keys.contains("siteNode_" + siteNode.getId() + "_disabledLanguages"))
		{
		    String disabledLanguagesString = "" + ps.getString("siteNode_" + siteNode.getId() + "_disabledLanguages");
			if(disabledLanguagesString != null && !disabledLanguagesString.equals("") && !disabledLanguagesString.equals("null"))
		    	allSiteNodeProperties.put("siteNode_" + siteNode.getId() + "_disabledLanguages", disabledLanguagesString);
		}   
		
		if(keys.contains("siteNode_" + siteNode.getId() + "_enabledLanguages"))
		{
			String enabledLanguagesString = "" + ps.getString("siteNode_" + siteNode.getId() + "_enabledLanguages");
		    if(enabledLanguagesString != null && !enabledLanguagesString.equals("") && !enabledLanguagesString.equals("null"))
			    allSiteNodeProperties.put("siteNode_" + siteNode.getId() + "_enabledLanguages", enabledLanguagesString);
		}
		
        Iterator childSiteNodes = siteNode.getChildSiteNodes().iterator();
        while(childSiteNodes.hasNext())
        {
        	SiteNode childSiteNode = (SiteNode)childSiteNodes.next();
        	getSiteNodePropertiesAndAccessRights(ps, allSiteNodeProperties, allAccessRights, keys, childSiteNode, db);
        }
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

	public String getExportFormat()
	{
		return exportFormat;
	}

	public void setExportFormat(String exportFormat)
	{
		this.exportFormat = exportFormat;
	}

	public Boolean getOnlyPublishedVersions()
	{
		return this.onlyPublishedVersions;
	}

	public void setOnlyPublishedVersions(Boolean onlyPublishedVersions)
	{
		this.onlyPublishedVersions = onlyPublishedVersions;
	}
	
	public void setProcessId(String processId) 
	{
		this.processId = processId;
	}

	public void setProcessStatus(String processStatusString) 
	{
		if ("running".equals(processStatusString))
		{
			this.processStatus = ProcessBean.RUNNING;
		}
		else if ("finished".equals(processStatusString))
		{
			this.processStatus = ProcessBean.FINISHED;
		}
		else if ("error".equals(processStatusString))
		{
			this.processStatus = ProcessBean.ERROR;
		}
		else if ("notStarted".equals(processStatusString))
		{
			this.processStatus = ProcessBean.NOT_STARTED;
		}
		else
		{
			logger.warn("Got unknown process status parameter. Ignoring value. Parameter: " + processStatusString);
			this.processStatus = -1;
		}
	}

	public List<ProcessBean> getProcessBeans()
	{
		return ProcessBean.getProcessBeans(ExportRepositoryAction.class.getName());
	}

	public List<ProcessBean> getFilteredProcessBeans()
	{
		List<ProcessBean> processes = ProcessBean.getProcessBeans(ExportRepositoryAction.class.getName());

		if (logger.isDebugEnabled())
		{
			logger.debug("Number of processes before filtering: " + processes.size());
		}
		if (this.processStatus != -1)
		{
			Iterator<ProcessBean> processIterator = processes.iterator();
			ProcessBean process;
			while (processIterator.hasNext())
			{
				process = processIterator.next();
				if (process.getStatus() != this.processStatus)
				{
					logger.debug("Removing (filtering) process with Id: " + process.getProcessId());
					processIterator.remove();
				}
			}
		}
		if (logger.isDebugEnabled())
		{
			logger.debug("Number of processes after filtering: " + processes.size());
		}

		return processes;
	}

	public String getStatusAsJSON()
	{
		Gson gson = new GsonBuilder()
			.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
			.setDateFormat("dd MMM HH:mm:ss").create();
		JsonObject object = new JsonObject();

		try
		{
			List<ProcessBean> processes = getProcessBeans();
			Type processBeanListType = new TypeToken<List<ProcessBean>>() {}.getType();
			JsonElement list = gson.toJsonTree(processes, processBeanListType);
			object.add("processes", list);
			object.addProperty("memoryMessage", getMemoryUsageAsText());
		}
		catch (Throwable t)
		{
			logger.error("Error when generating repository export status report as JSON.", t);
			JsonObject error = new JsonObject(); 
			error.addProperty("message", t.getMessage());
			error.addProperty("type", t.getClass().getSimpleName());
			object.add("error", error);
		}

		return gson.toJson(object);
	}

}
