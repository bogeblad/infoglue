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

package org.infoglue.cms.controllers.kernel.impl.simple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.applications.managementtool.actions.ExportRepositoryAction;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.content.impl.simple.ExportContentVersionImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.impl.simple.InfoGlueExportImpl;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.handlers.DigitalAssetBytesHandler;
import org.infoglue.deliver.util.CompressionHelper;
import org.infoglue.deliver.util.Timer;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
* This class handles exporting one or many repositories.
* 
* @author Mattias Bogeblad
*/

public class OptimizedExportController extends BaseController implements Runnable
{
    public final static Logger logger = Logger.getLogger(OptimizedExportController.class.getName());

    private String[] repositories;
    private int assetMaxSize;
    private boolean onlyPublishedVersions;
    private String exportFileName;
    private ProcessBean processBean;
    private boolean copy = false;
	private String onlyLatestVersions = "true";
	private String standardReplacement = null;
	private String replacements = null;

	public synchronized void run()
	{
		logger.info("Starting Optimized Export....");
		try
		{
			exportRepository(repositories, assetMaxSize, onlyPublishedVersions, exportFileName, processBean, copy, this.onlyLatestVersions, this.standardReplacement, this.replacements);
		}
		catch (Exception e) 
		{
			logger.error("Error in export thread:" + e.getMessage(), e);
		}
	}
	
	private OptimizedExportController(String[] repositories, int assetMaxSize, boolean onlyPublishedVersions, String exportFileName, ProcessBean processBean, boolean copy, String onlyLatestVersions, String standardReplacement, String replacements)
	{
		this.repositories = repositories;
		this.assetMaxSize = assetMaxSize;
		this.onlyPublishedVersions = onlyPublishedVersions;
		this.exportFileName = exportFileName;
		this.processBean = processBean;
		this.copy = copy;
		this.onlyLatestVersions = onlyLatestVersions;
		this.standardReplacement = standardReplacement;
		this.replacements = replacements;
	}
	
	/**
	 * Factory method to get object
	 */
	
	public static void export(String[] repositories, int assetMaxSize, boolean onlyPublishedVersions, String exportFileName, ProcessBean processBean) throws Exception
	{
		OptimizedExportController exportController = new OptimizedExportController(repositories, assetMaxSize, onlyPublishedVersions, exportFileName, processBean, false, "false", "", "");
		Thread thread = new Thread(exportController);
		thread.start();
	}

	/**
	 * Factory method to get object
	 */
	
	public static void copy(String[] repositories, int assetMaxSize, boolean onlyPublishedVersions, String exportFileName, ProcessBean processBean, String onlyLatestVersions, String standardReplacement, String replacements) throws Exception
	{
		OptimizedExportController exportController = new OptimizedExportController(repositories, assetMaxSize, onlyPublishedVersions, exportFileName, processBean, true, onlyLatestVersions, standardReplacement, replacements);
		Thread thread = new Thread(exportController);
		thread.start();
	}

	public void exportRepository(String[] repositories, int assetMaxSize, boolean onlyPublishedVersions, String exportFileName, ProcessBean processBean, boolean copy, String onlyLatestVersions, String standardReplacement, String replacements) throws Exception
	{
		Timer t = new Timer();
		
		VisualFormatter visualFormatter = new VisualFormatter();

		String exportId = "Export_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm");
		processBean.setStatus(ProcessBean.RUNNING);
		
		String folderName = CmsPropertyHandler.getDigitalAssetPath() + File.separator + exportId + "Archive";
		File folder = new File(folderName);
		folder.mkdirs();

		Database db = null;
		
		try 
		{
			db = CastorDatabaseService.getDatabase();
			
			Mapping map = new Mapping();
			logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site_3.0.xml").toString());
			map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site_3.0.xml").toString());
			
			processBean.updateProcess("Export configuration initialized");
			
			// All ODMG database access requires a transaction
			db.begin();

			Hashtable<String,String> allRepositoryProperties = new Hashtable<String,String>();
			Hashtable<String,String> allSiteNodeProperties = new Hashtable<String,String>();
			Hashtable<String,String> allContentProperties = new Hashtable<String,String>();
			List<AccessRight> allAccessRights = new ArrayList<AccessRight>();
			
			Map args = new HashMap();
		    args.put("globalKey", "infoglue");
		    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
		    //END TEST
			Collection keys = ps.getKeys();
			logger.info("keys:" + keys.size());

			processBean.updateProcess("Propertyset fetched in " + (t.getElapsedTime() / 1000) + " seconds");

			String names = "";
			
			List<Content> allExportedContents = new ArrayList<Content>();
			List<SiteNode> allExportedSiteNodes = new ArrayList<SiteNode>();
			
			int exportedEntities = exportHeavyEntities(repositories, "Contents", folderName, assetMaxSize, onlyPublishedVersions, exportFileName, allExportedSiteNodes, allExportedContents);
			processBean.updateProcess("Contents exported: " + exportedEntities + " in " + (t.getElapsedTime() / 1000) + " seconds");

		    exportedEntities = exportHeavyEntities(repositories, "ContentVersions", folderName, assetMaxSize, onlyPublishedVersions, exportFileName, allExportedSiteNodes, allExportedContents);
			processBean.updateProcess("Content versions exported: " + exportedEntities + " in " + (t.getElapsedTime() / 1000) + " seconds");

		    exportedEntities = exportHeavyEntities(repositories, "SiteNodes", folderName, assetMaxSize, onlyPublishedVersions, exportFileName, allExportedSiteNodes, allExportedContents);
			processBean.updateProcess("Sitenodes exported: " + exportedEntities + " in " + (t.getElapsedTime() / 1000) + " seconds");
		
			exportedEntities = exportHeavyEntities(repositories, "SiteNodeVersions", folderName, assetMaxSize, onlyPublishedVersions, exportFileName, allExportedSiteNodes, allExportedContents);
			processBean.updateProcess("Sitenode versions exported: " + exportedEntities + " in " + (t.getElapsedTime() / 1000) + " seconds");
			
			exportedEntities = exportHeavyEntities(repositories, "DigitalAssets", folderName, assetMaxSize, onlyPublishedVersions, exportFileName, allExportedSiteNodes, allExportedContents);
			processBean.updateProcess("Assets exported: " + exportedEntities + " in " + (t.getElapsedTime() / 1000) + " seconds");
			
		    List<Repository> repositoryList = new ArrayList<Repository>();

			for(int i=0; i<repositories.length; i++)
			{
				Integer repositoryId = new Integer(repositories[i]);
				Repository repository 	= RepositoryController.getController().getRepositoryWithId(repositoryId, db);
				logger.info("Read repo");
				
				/*
				SiteNode siteNode 		= SiteNodeController.getController().getRootSiteNode(repositoryId, db);
				logger.info("Read siteNode");
				Content content 		= ContentController.getContentController().getRootContent(repositoryId, db);
				logger.info("Read content");
				*/

				InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Repository.Read", db);
			    if(interceptionPointVO != null)
			    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), repository.getId().toString(), db));
			    logger.info("Read allAccessRights 1");

			    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Repository.Write", db);
			    if(interceptionPointVO != null)
			    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), repository.getId().toString(), db));
			    logger.info("Read allAccessRights 2");

			    interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName("Repository.ReadForBinding", db);
			    if(interceptionPointVO != null)
			    	allAccessRights.addAll(AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointVO.getId(), repository.getId().toString(), db));
			    logger.info("Read allAccessRights 3:" + allAccessRights.size());

			    allAccessRights.addAll(AccessRightController.getController().getContentAccessRightListOnlyReadOnly(repository.getId(), db));
			    logger.info("Read allAccessRights 4:" + allAccessRights.size());

			    allAccessRights.addAll(AccessRightController.getController().getSiteNodeAccessRightListOnlyReadOnly(repository.getId(), db));
			    logger.info("Read allAccessRights 5:" + allAccessRights.size());
				
				getContentProperties(ps, allContentProperties, allExportedContents, db);
				logger.info("getContentPropertiesAndAccessRights");
				
				getSiteNodeProperties(ps, allSiteNodeProperties, allExportedSiteNodes, db);
				logger.info("getSiteNodePropertiesAndAccessRights");
				
				//siteNodes.add(siteNode);
				//contents.add(content);
				names = names + "_" + repository.getName();
				allRepositoryProperties.putAll(getRepositoryProperties(ps, repositoryId));
				
				repositoryList.add(repository);
			}
			processBean.updateProcess("Access rights: " + allAccessRights.size() + " exported in " + (t.getElapsedTime() / 1000) + " seconds");

			List languages = LanguageController.getController().getLanguageList(db);
			List contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionList(db);
			logger.info("contentTypeDefinitions");
			List categories = CategoryController.getController().getAllActiveCategories();
			logger.info("categories");
			processBean.updateProcess("Content type and categories exported in " + (t.getElapsedTime() / 1000) + " seconds");
			
			InfoGlueExportImpl infoGlueExportImpl = new InfoGlueExportImpl();
			
			names = new VisualFormatter().replaceNonAscii(names, '_');

			if(repositories.length > 2 || names.length() > 40)
				names = "" + repositories.length + "_repositories";
			
			String fileName = exportId;
			if(exportFileName != null && !exportFileName.equals(""))
				fileName = exportFileName;
			
			//String filePath = CmsPropertyHandler.getDigitalAssetPath();
			String fileSystemName =  folderName + File.separator + "ExportMain.xml";
			String archiveFileSystemName =  CmsPropertyHandler.getDigitalAssetPath() + File.separator + fileName;
						
			String encoding = "UTF-8";
			File file = new File(fileSystemName);
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
            Marshaller marshaller = new Marshaller(osw);
            marshaller.setMapping(map);
			marshaller.setEncoding(encoding);
			marshaller.setValidation(false);
			DigitalAssetBytesHandler.setMaxSize(assetMaxSize);

			processBean.updateProcess("Starting marshalling main export file");

			//infoGlueExportImpl.getRootContent().addAll(contents);
			//infoGlueExportImpl.getRootSiteNode().addAll(siteNodes);
			
			logger.info("repositoryList:" + repositoryList.size());
			infoGlueExportImpl.setRepositories(repositoryList);
			infoGlueExportImpl.setLanguages(languages);
			infoGlueExportImpl.setContentTypeDefinitions(contentTypeDefinitions);
			infoGlueExportImpl.setCategories(categories);

			infoGlueExportImpl.setRepositoryProperties(allRepositoryProperties);
			infoGlueExportImpl.setContentProperties(allContentProperties);
			infoGlueExportImpl.setSiteNodeProperties(allSiteNodeProperties);
			infoGlueExportImpl.setAccessRights(allAccessRights);

			//infoGlueExportImpl.setContents(allContents);
			//infoGlueExportImpl.setContentVersions(allContentVersions);
			//infoGlueExportImpl.setSiteNodes(allSiteNodes);
			//infoGlueExportImpl.setSiteNodeVersions(allSiteNodeVersions);
			
			marshaller.marshal(infoGlueExportImpl);

			processBean.updateProcess("Marshalling main export file done in " + (t.getElapsedTime() / 1000) + " seconds");

			osw.flush();
			osw.close();
			
			db.rollback();
			db.close();

			
			//Here we zip the dir
			String fileUrl = CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + fileName + "_" + names + ".zip";
			//this.fileName = fileName + "_" + names + ".zip";
			
			try 
			{ 
			    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archiveFileSystemName + "_" + names + ".zip")); 
				CompressionHelper ch = new CompressionHelper();
				ch.zipFolder(folderName, zos);
			    zos.close(); 
			    
			    deleteFolder(new File(folderName));
			} 
			catch(Exception e) 
			{ 
				e.printStackTrace();
			} 
			
			logger.info("File:" + archiveFileSystemName + "_" + names + ".zip");
			processBean.updateProcess("Zipping export done in " + (t.getElapsedTime() / 1000) + " seconds");
			processBean.updateProcessArtifacts("file", fileUrl, new File(archiveFileSystemName + "_" + names + ".zip"));

			if(copy)
			{
				logger.info("Copying it...");
				OptimizedImportController.importRepositories(new File(archiveFileSystemName + "_" + names + ".zip"), onlyLatestVersions, standardReplacement, replacements, processBean);
			}
			Thread.sleep(3000);
			
			processBean.setStatus(ProcessBean.FINISHED);
		} 
		catch (Throwable e) 
		{
			processBean.setError("An error occured while exporting repository. Error message: " + e.getClass(), e);
			logger.error("An error was found exporting a repository: " + e.getMessage(), e);
			db.rollback();
		}
	}
	

	protected int exportHeavyEntities(String[] repositories, String type, String folderName, Integer assetMaxSize, boolean onlyPublishedVersions, String exportFileName, List<SiteNode> allReturningSiteNodes, List<Content> allReturningContents) throws Exception 
	{
		int exportedEntities = 0;

		Mapping map = new Mapping();
		logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site_3.0.xml").toString());
		map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site_3.0.xml").toString());
		
		// All ODMG database access requires a transaction
		for(int i=0; i<repositories.length; i++)
		{
			Database db = CastorDatabaseService.getDatabase();
			try 
			{
				db.begin();
				
				Integer repositoryId = new Integer(repositories[i]);
				Repository repository = RepositoryController.getController().getRepositoryWithId(repositoryId, db);
				logger.info("Read repo");

				List<SiteNode> allSiteNodes = new ArrayList<SiteNode>();
				List<SiteNodeVersion> allSiteNodeVersions = new ArrayList<SiteNodeVersion>();
				List<Content> allContents = new ArrayList<Content>();
				List<ExportContentVersionImpl> allContentVersions = new ArrayList<ExportContentVersionImpl>();
				List<DigitalAsset> allDigitalAssets = new ArrayList<DigitalAsset>();

				if(type.equals("Contents"))
				{
					List<Content> contents = ContentController.getContentController().getContentList(repositoryId, 0, 5000, db);
					while(contents.size() > 0)
					{
						allContents.addAll(contents);
						contents = ContentController.getContentController().getContentList(repositoryId, contents.get(contents.size()-1).getContentId(), 5000, db);
						logger.info(".");
					}
					exportedEntities = allContents.size();
					logger.info("Read all contents");
				}
				if(type.equals("ContentVersions"))
				{
					List<ExportContentVersionImpl> contentVersions = ContentVersionController.getContentVersionController().getContentVersionList(repositoryId, 0, 5000, onlyPublishedVersions, db);
					while(contentVersions.size() > 0)
					{
						allContentVersions.addAll(contentVersions);
						contentVersions = ContentVersionController.getContentVersionController().getContentVersionList(repositoryId, contentVersions.get(contentVersions.size()-1).getContentVersionId(), 5000, onlyPublishedVersions, db);
						logger.info(".");
					}
					exportedEntities = allContentVersions.size();
					logger.info("Read all content versions");
				}

				if(type.equals("SiteNodes"))
				{
					List<SiteNode> siteNodes = SiteNodeController.getController().getSiteNodeList(repositoryId, 0, 5000, db);
					while(siteNodes.size() > 0)
					{
						allSiteNodes.addAll(siteNodes);
						siteNodes = SiteNodeController.getController().getSiteNodeList(repositoryId, siteNodes.get(siteNodes.size()-1).getSiteNodeId(), 5000, db);
						logger.info(".");
					}
					exportedEntities = allSiteNodes.size();
					logger.info("Read all siteNodes");
				}
				
				if(type.equals("SiteNodeVersions"))
				{
					List<SiteNodeVersion> siteNodeVersions = SiteNodeVersionController.getController().getSiteNodeVersionList(repositoryId, 0, 5000, onlyPublishedVersions, db);
					while(siteNodeVersions.size() > 0)
					{
						allSiteNodeVersions.addAll(siteNodeVersions);
						siteNodeVersions = SiteNodeVersionController.getController().getSiteNodeVersionList(repositoryId, siteNodeVersions.get(siteNodeVersions.size()-1).getSiteNodeVersionId(), 5000, onlyPublishedVersions, db);
						logger.info(".");
					}
					exportedEntities = allSiteNodeVersions.size();
				}
				if(type.equals("DigitalAssets"))
				{
					int assetCount = 0;
					List<DigitalAssetVO> assets = DigitalAssetController.getController().dumpDigitalAssetList(repositoryId, 0, 50, assetMaxSize, onlyPublishedVersions, folderName);
					while(assets.size() > 0)
					{
						assetCount += assets.size();
						//allDigitalAssets.addAll(assets);
						assets = DigitalAssetController.getController().dumpDigitalAssetList(repositoryId, assets.get(assets.size()-1).getDigitalAssetId(), 50, assetMaxSize, onlyPublishedVersions, folderName);
						logger.info(".");
					}
					exportedEntities = assetCount;
				}
				
				if(!type.equals("DigitalAssets"))
				{
					InfoGlueExportImpl infoGlueExportImpl = new InfoGlueExportImpl();
					
					String name = new VisualFormatter().replaceNonAscii(repository.getName(), '_');
					
					String fileName = type + "_" + repository.getId() + ".xml";
					if(exportFileName != null && !exportFileName.equals(""))
						fileName = exportFileName;
					
					String fileSystemName =  folderName + File.separator + fileName;
								
					String encoding = "UTF-8";
					File file = new File(fileSystemName);
		            FileOutputStream fos = new FileOutputStream(file);
		            OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
		            Marshaller marshaller = new Marshaller(osw);
		            marshaller.setMapping(map);
					marshaller.setEncoding(encoding);
					marshaller.setValidation(false);
					if(type.equals("ContentVersions"))
						DigitalAssetBytesHandler.setMaxSize(0);
					else
						DigitalAssetBytesHandler.setMaxSize(assetMaxSize);
							
					infoGlueExportImpl.setContents(allContents);
					infoGlueExportImpl.setContentVersions(allContentVersions);
					infoGlueExportImpl.setSiteNodes(allSiteNodes);
					infoGlueExportImpl.setSiteNodeVersions(allSiteNodeVersions);
					infoGlueExportImpl.setDigitalAssets(allDigitalAssets);
					
					allReturningSiteNodes.addAll(allSiteNodes);
					allReturningContents.addAll(allContents);
					marshaller.marshal(infoGlueExportImpl);
					
					osw.flush();
					osw.close();
				}
				
				db.rollback();
			} 
			catch (Exception e) 
			{
				logger.error("An error was found exporting a repository: " + e.getMessage(), e);
				db.rollback();
			}
			finally
			{
				db.close();
			}
		}
		
		return exportedEntities;
	}
	
	public static void getContentProperties(PropertySet ps, Hashtable<String, String> allContentProperties, List<Content> allContents, Database db) throws SystemException
	{
		for(Content content : allContents)
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
		}		
	}

	public static void getSiteNodeProperties(PropertySet ps, Hashtable<String, String> allSiteNodeProperties, List<SiteNode> allSiteNodes, Database db) throws SystemException, Exception
	{
		for(SiteNode siteNode : allSiteNodes)
		{
		    String disabledLanguagesString = "" + ps.getString("siteNode_" + siteNode.getId() + "_disabledLanguages");
		    String enabledLanguagesString = "" + ps.getString("siteNode_" + siteNode.getId() + "_enabledLanguages");
	
		    if(disabledLanguagesString != null && !disabledLanguagesString.equals("") && !disabledLanguagesString.equals("null"))
		    	allSiteNodeProperties.put("siteNode_" + siteNode.getId() + "_disabledLanguages", disabledLanguagesString);
		    if(enabledLanguagesString != null && !enabledLanguagesString.equals("") && !enabledLanguagesString.equals("null"))
			    allSiteNodeProperties.put("siteNode_" + siteNode.getId() + "_enabledLanguages", enabledLanguagesString);
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

	public void deleteFolder(File folder) 
	{
	    File[] files = folder.listFiles();
	    if(files!=null) 
	    { 
	        for(File f: files) 
	        {
	            if(f.isDirectory()) 
	            {
	                deleteFolder(f);
	            } 
	            else 
	            {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}

   	
    /* (non-Javadoc)
     * @see org.infoglue.cms.controllers.kernel.impl.simple.BaseController#getNewVO()
     */
    public BaseEntityVO getNewVO()
    {
        // TODO Auto-generated method stub
        return null;
    }


}
