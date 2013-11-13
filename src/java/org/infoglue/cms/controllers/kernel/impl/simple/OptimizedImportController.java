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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.TransactionNotInProgressException;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.applications.managementtool.actions.ExportRepositoryAction;
import org.infoglue.cms.applications.managementtool.actions.ImportRepositoryAction;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentCategory;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.DigitalAssetImpl;
import org.infoglue.cms.entities.content.impl.simple.ExportContentVersionImpl;
import org.infoglue.cms.entities.content.impl.simple.SmallDigitalAssetImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.AccessRightGroup;
import org.infoglue.cms.entities.management.AccessRightRole;
import org.infoglue.cms.entities.management.AvailableServiceBinding;
import org.infoglue.cms.entities.management.Category;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.InterceptionPoint;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.RepositoryLanguage;
import org.infoglue.cms.entities.management.ServiceDefinition;
import org.infoglue.cms.entities.management.SiteNodeTypeDefinition;
import org.infoglue.cms.entities.management.impl.simple.AvailableServiceBindingImpl;
import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.InfoGlueExportImpl;
import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.management.impl.simple.ServiceDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.SiteNodeTypeDefinitionImpl;
import org.infoglue.cms.entities.structure.Qualifyer;
import org.infoglue.cms.entities.structure.ServiceBinding;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
import org.infoglue.cms.entities.structure.impl.simple.SiteNodeVersionImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.CompressionHelper;
import org.infoglue.deliver.util.Timer;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
* This class handles Importing of a repository - not finished by a long shot.
* 
* @author mattias
*/

public class OptimizedImportController extends BaseController implements Runnable
{
    public final static Logger logger = Logger.getLogger(OptimizedImportController.class.getName());

    private File file;
    private String onlyLatestVersions;
    private String standardReplacement;
    private String replacements;
    private ProcessBean processBean;
    
	public synchronized void run()
	{
		logger.info("Starting Optimized Import....");
		try
		{
			importFile(file, onlyLatestVersions, standardReplacement, replacements, processBean);
		}
		catch (Exception e) 
		{
//			processBean.setStatus(ProcessBean.FAILED);
			//TODO: Fix this error message better. Support illegal xml-chars
			processBean.setError("Something went wrong with the import. Please consult the logfiles.");
			logger.error("Error in monitor:" + e.getMessage(), e);
		}
	}
	
	private OptimizedImportController(File file, String onlyLatestVersions, String standardReplacement, String replacements, ProcessBean processBean)
	{
		this.file = file;
		this.onlyLatestVersions = onlyLatestVersions;
		this.standardReplacement = standardReplacement;
		this.replacements = replacements;
		this.processBean = processBean;
	}
	
	/**
	 * Factory method to get object
	 */
	
	public static void importRepositories(File file, String onlyLatestVersions, String standardReplacement, String replacements, ProcessBean processBean) throws Exception
	{
		OptimizedImportController importController = new OptimizedImportController(file, onlyLatestVersions, standardReplacement, replacements, processBean);
		Thread thread = new Thread(importController);
		thread.start();
	}
	
	private void importFile(File file, String onlyLatestVersions, String standardReplacement, String replacements, ProcessBean processBean) throws Exception
	{
		Timer t = new Timer();
		processBean.setStatus(ProcessBean.RUNNING);
		
		CompressionHelper ch = new CompressionHelper();
		String extractFolder = CmsPropertyHandler.getDigitalAssetUploadPath() + File.separator + "ImportArchive_" + System.currentTimeMillis();
		logger.info("Extracting " + file.getPath() + " to " + extractFolder);
		File importFolder = new File(extractFolder);
		importFolder.mkdir();
		ch.unzip(file, importFolder);
		
		processBean.updateProcess("Unzip of archive took " + (t.getElapsedTime() / 1000) + " seconds");
		
		try 
		{
			String encoding = "UTF-8";
			
			Map contentIdMap = new HashMap();
			Map siteNodeIdMap = new HashMap();
			List allContentIds = new ArrayList();

			Map<String,String> replaceMap = new HashMap<String,String>();
			try
			{
				boolean isUTF8 = false;
				boolean hasUnicodeChars = false;
				if(replacements.indexOf((char)65533) > -1)
					isUTF8 = true;
				
				for(int i=0; i<replacements.length(); i++)
				{
					int c = (int)replacements.charAt(i);
					if(c > 255 && c < 65533)
						hasUnicodeChars = true;
				}

				if(!isUTF8 && !hasUnicodeChars)
				{
					String fromEncoding = CmsPropertyHandler.getUploadFromEncoding();
					if(fromEncoding == null)
						fromEncoding = "iso-8859-1";
					
					String toEncoding = CmsPropertyHandler.getUploadToEncoding();
					if(toEncoding == null)
						toEncoding = "utf-8";
					
					if(replacements.indexOf("�") == -1 && 
					   replacements.indexOf("�") == -1 && 
					   replacements.indexOf("�") == -1 && 
					   replacements.indexOf("�") == -1 && 
					   replacements.indexOf("�") == -1 && 
					   replacements.indexOf("�") == -1)
					{
						replacements = new String(replacements.getBytes(fromEncoding), toEncoding);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			Properties properties = new Properties();
			try
			{
				properties.load(new ByteArrayInputStream(replacements.getBytes("ISO-8859-1")));
				
				Iterator propertySetIterator = properties.keySet().iterator();
				while(propertySetIterator.hasNext())
				{
					String key = (String)propertySetIterator.next();
					String value = properties.getProperty(key);
					replaceMap.put(key, value);
				}
			}	
			catch(Exception e)
			{
			    logger.error("Error loading properties from string. Reason:" + e.getMessage());
				e.printStackTrace();
			}

			logger.info("replaceMap:" + replaceMap);
			importRepository(importFolder, encoding, onlyLatestVersions, false, contentIdMap, siteNodeIdMap, allContentIds, replaceMap, processBean);
			
			processBean.updateProcess("Main import completed in " + (t.getElapsedTime() / 1000) + " seconds");

			// All ODMG database access requires a transaction
			Database db = CastorDatabaseService.getDatabase();

			try
			{
				db.begin();
				
				Iterator allContentIdsIterator = allContentIds.iterator();
				while(allContentIdsIterator.hasNext())
				{
					Integer contentId = (Integer)allContentIdsIterator.next();
				
					Content content = ContentController.getContentController().getContentWithId(contentId, db);
					updateContentVersions(content, contentIdMap, siteNodeIdMap, onlyLatestVersions, replaceMap);
				}
				db.commit();
			}
			catch(Exception e)
			{
				try
				{
					db.rollback();
				}
				catch(Exception e2) { e2.printStackTrace(); }
                logger.error("An error occurred when updating content version for content: " + e.getMessage(), e);					
			}
			finally
			{
				db.close();					
			}
			
			processBean.updateProcess("Update of all imported content versions completed in " + (t.getElapsedTime() / 1000) + " seconds");

			Thread.sleep(3000);
			processBean.setStatus(ProcessBean.FINISHED);
		} 
		catch ( Exception e) 
		{
			logger.error("An error occurred when importing a repository: " + e.getMessage(), e);
			throw new SystemException("An error occurred when importing a repository: " + e.getMessage(), e);
		}
	}
	
	public void importRepository(File archiveFolder, String encoding, String onlyLatestVersions, boolean isCopyAction, Map contentIdMap, Map siteNodeIdMap, List allContentIds, Map replaceMap, ProcessBean processBean) throws Exception
	{
		File mainExportFile = null;

		File[] files = archiveFolder.listFiles();
		for(File file : files)
		{
			if(file.getName().equals("ExportMain.xml"))
				mainExportFile = file;
		}
		
		logger.info("mainExportFile:" + mainExportFile);

		if(mainExportFile == null)
			throw new Exception("No main export file found. Looking for ExportMain.xml in archive");

		copyRepository(archiveFolder, mainExportFile/*, contentVersionsFile, contentsFile, siteNodeVersonsFile, siteNodesFile*/, onlyLatestVersions, isCopyAction, contentIdMap, siteNodeIdMap, allContentIds, replaceMap, encoding, processBean);
	}
	
	private InfoGlueExportImpl getInfoGlueExportImpl(File file, String encoding) throws Exception
	{
		Mapping map = new Mapping();
		logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site_3.0.xml").toString());
		map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site_3.0.xml").toString());

		FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        InputStreamReader reader = new InputStreamReader(bis, encoding);

		Unmarshaller unmarshaller = new Unmarshaller(map);
		unmarshaller.setWhitespacePreserve(true);
		unmarshaller.setValidation(false);
		InfoGlueExportImpl infoGlueExportImplRead = (InfoGlueExportImpl)unmarshaller.unmarshal(reader);
		reader.close();
		return infoGlueExportImplRead;
	}
	
	private Map<Integer,List<ExportContentVersionImpl>> getAllContentVersionMap(File contentVersionsFile, String encoding) throws Exception
	{
		InfoGlueExportImpl contentVersions = getInfoGlueExportImpl(contentVersionsFile, encoding);
		logger.info("contentVersions:" + contentVersions);

		float memoryLeft = ((float)Runtime.getRuntime().maxMemory() - (float)Runtime.getRuntime().totalMemory()) / 1024f / 1024f;
		logger.info("Memory after contentVersions-file:" + memoryLeft);

		Map<Integer,List<ExportContentVersionImpl>> allContentVersionMap = new HashMap<Integer,List<ExportContentVersionImpl>>(); 
		for(ExportContentVersionImpl contentVersion : contentVersions.getContentVersions())
		{
			List<ExportContentVersionImpl> versions = allContentVersionMap.get(contentVersion.getValueObject().getContentId());
			if(versions == null)
			{
				versions = new ArrayList<ExportContentVersionImpl>();
				logger.info("Creating versions for " + contentVersion.getValueObject().getContentId());
				allContentVersionMap.put(contentVersion.getValueObject().getContentId(), versions);
			}
			//logger.info("Adding version:" + contentVersion.getValueObject().getContentVersionId() + ":" + contentVersion.getValueObject().getContentId());
			versions.add(contentVersion);
			//logger.info("readContentCandidate debug...:" + readContentCandidate.getName() + ":" + readContentCandidate.getId() + ":" + readContentCandidate.getRepositoryId());
		}
		
		return allContentVersionMap;
	}
	
	public void copyRepository(File archiveFolder, File mainExportFile/*, File contentVersionsFile, File contentsFile, File siteNodeVersionsFile, File siteNodesFile*/, String onlyLatestVersions, boolean isCopyAction, Map contentIdMap, Map siteNodeIdMap, List allContentIds, Map replaceMap, String encoding, ProcessBean processBean) throws Exception
	{
		Timer t = new Timer();
		
		Map categoryIdMap = new HashMap();
		Map contentTypeIdMap = new HashMap();
		Map repositoryIdMap = new HashMap();
		Map siteNodeVersionIdMap = new HashMap();
		List allContents = new ArrayList();
		List allSiteNodes = new ArrayList();
		List<SmallDigitalAssetImpl> allSmallAssets = new ArrayList<SmallDigitalAssetImpl>();
		Map<Integer,List<Integer>> assetVersionsMap = new HashMap<Integer,List<Integer>>();
		Map<String, AvailableServiceBinding> readAvailableServiceBindings = new HashMap<String, AvailableServiceBinding>();
		Map<Integer,Language> languages = new HashMap<Integer,Language>();
		Map<Integer,SiteNodeTypeDefinition> siteNodeTypeDefinitions = new HashMap<Integer,SiteNodeTypeDefinition>();
		
		InfoGlueExportImpl master = getInfoGlueExportImpl(mainExportFile, encoding);
		logger.info("master:" + master);
		
		for(Language lang : master.getLanguages())
		{
			languages.put(lang.getId(), lang);
		}
		
		for(SiteNodeTypeDefinition siteNodeTypeDefinition : master.getSiteNodeTypeDefinitions())
		{
			siteNodeTypeDefinitions.put(siteNodeTypeDefinition.getId(), siteNodeTypeDefinition);
		}
		
		importFoundation(master, encoding, isCopyAction, replaceMap, categoryIdMap, repositoryIdMap);

		processBean.updateProcess("Foundation imported in " + (t.getElapsedTime() / 1000) + " seconds");
		
		createContent(archiveFolder, /*contentsFile, contentVersionsFile, */encoding, repositoryIdMap, master.getRepositories(), contentIdMap, contentTypeIdMap, allContents, languages, master.getContentTypeDefinitions(), categoryIdMap, allSmallAssets, assetVersionsMap, onlyLatestVersions, isCopyAction, replaceMap);

		processBean.updateProcess("Content created in " + (t.getElapsedTime() / 1000) + " seconds");
		
		CacheController.clearCastorCaches();
		CacheController.clearCache("contentVersionCache");
		CacheController.clearCache("contentCache");
		
		processStructure(archiveFolder, /*siteNodeVersionsFile, siteNodesFile, */encoding, onlyLatestVersions, contentIdMap, siteNodeIdMap, replaceMap, repositoryIdMap, siteNodeVersionIdMap, allSiteNodes, readAvailableServiceBindings, master.getRepositories(), siteNodeTypeDefinitions);

		processBean.updateProcess("Structure imported in " + (t.getElapsedTime() / 1000) + " seconds");
		
		CacheController.clearCastorCaches();
		CacheController.clearCache("siteNodeVersionCache");
		CacheController.clearCache("siteNodeCache");
		
		Database dbStructure = CastorDatabaseService.getDatabase();
		try
		{
			dbStructure.begin();
			
			for(SmallDigitalAssetImpl asset : allSmallAssets)
			{
				List<ContentVersion> assetRelatedContentVersions = new ArrayList<ContentVersion>();
				List<Integer> versionIdsUsingAsset = assetVersionsMap.get(asset.getId());
				for(Integer cvId : versionIdsUsingAsset)
				{
					ContentVersion cv = ContentVersionController.getContentVersionController().getContentVersionWithId(cvId, dbStructure);
					assetRelatedContentVersions.add(cv);
				}
	
				File assetFile = new File(archiveFolder.getPath() + File.separator + asset.getId() + ".file");
				//logger.info("assetFile:" + assetFile.exists());
				if(assetFile.exists())
				{
					InputStream is = new FileInputStream(assetFile);
					create(asset.getValueObject(), is, assetRelatedContentVersions, dbStructure);
				}
				else
				{
					logger.info("Missing file...:" + assetFile.getPath());
				}
			}

			dbStructure.commit();
		}
		catch (Exception e) 
		{
			dbStructure.rollback();
		}
		finally
		{
			dbStructure.close();
		}

		processBean.updateProcess("Assets imported in " + (t.getElapsedTime() / 1000) + " seconds");

		CacheController.clearCastorCaches();
		CacheController.clearCache("contentVersionCache");
		CacheController.clearCache("contentCache");
		
		//List allContentIds = new ArrayList();
		Iterator allContentsIterator = allContents.iterator();
		while(allContentsIterator.hasNext())
		{
			Content content = (Content)allContentsIterator.next();
			allContentIds.add(content.getContentId());
		}

		//TEST
		Map args = new HashMap();
	    args.put("globalKey", "infoglue");
	    PropertySet ps = PropertySetManager.getInstance("jdbc", args);

		Map<String,String> repositoryProperties = master.getRepositoryProperties();
		Iterator<String> repositoryPropertiesIterator = repositoryProperties.keySet().iterator();
		while(repositoryPropertiesIterator.hasNext())
		{
			String key = repositoryPropertiesIterator.next();
			String value = repositoryProperties.get(key);
			String[] splittedString = key.split("_");
			if(splittedString.length == 3)
			{
				String oldRepId = splittedString[1];
				key = key.replaceAll(oldRepId, "" + repositoryIdMap.get(oldRepId));
				
				if(value != null && !value.equals("null"))
				{
					if(key.indexOf("_WYSIWYGConfig") > -1 || key.indexOf("_StylesXML") > -1 || key.indexOf("_extraProperties") > -1)
						ps.setData(key, value.getBytes("utf-8"));
					else
						ps.setString(key, value);
				}
			}
		}

		Map<String,String> contentProperties = master.getContentProperties();
		Iterator<String> contentPropertiesIterator = contentProperties.keySet().iterator();
		while(contentPropertiesIterator.hasNext())
		{
			String key = contentPropertiesIterator.next();
			String value = contentProperties.get(key);
			String[] splittedString = key.split("_");
			if(splittedString.length == 3)
			{
				String oldContentId = splittedString[1];
				key = key.replaceAll(oldContentId, (String)contentIdMap.get(oldContentId));
				if(value != null && !value.equals("null"))
					ps.setString(key, value);
			}
		}

		Map<String,String> siteNodeProperties = master.getSiteNodeProperties();
		Iterator<String> siteNodePropertiesIterator = siteNodeProperties.keySet().iterator();
		while(siteNodePropertiesIterator.hasNext())
		{
			String key = siteNodePropertiesIterator.next();
			String value = siteNodeProperties.get(key);
			String[] splittedString = key.split("_");
			if(splittedString.length == 3)
			{
				String oldSiteNodeId = splittedString[1];
				key = key.replaceAll(oldSiteNodeId, (String)siteNodeIdMap.get(oldSiteNodeId));
				if(value != null && !value.equals("null"))
					ps.setString(key, value);
			}
		}

		processBean.updateProcess("Properties imported in " + (t.getElapsedTime() / 1000) + " seconds");

		Database dbAccessRight = CastorDatabaseService.getDatabase();
		try
		{
			dbAccessRight.begin();
			Collection<AccessRight> accessRights = master.getAccessRights();
			Iterator<AccessRight> accessRightsIterator = accessRights.iterator();
			while(accessRightsIterator.hasNext())
			{
				AccessRight accessRight = accessRightsIterator.next();
	
				InterceptionPoint interceptionPoint = InterceptionPointController.getController().getInterceptionPointWithName(accessRight.getInterceptionPointName(), dbAccessRight);
				accessRight.setInterceptionPoint(interceptionPoint);
				if(interceptionPoint.getName().indexOf("Content") > -1)
					accessRight.setParameters((String)contentIdMap.get(accessRight.getParameters()));
				else if(interceptionPoint.getName().indexOf("SiteNodeVersion") > -1)
					accessRight.setParameters((String)siteNodeVersionIdMap.get(accessRight.getParameters()));
				else if(interceptionPoint.getName().indexOf("SiteNode") > -1)
					accessRight.setParameters((String)siteNodeIdMap.get(accessRight.getParameters()));
				else if(interceptionPoint.getName().indexOf("Repository") > -1)
					accessRight.setParameters(""+repositoryIdMap.get(accessRight.getParameters()));
	
				dbAccessRight.create(accessRight);
	
				Iterator accessRightRoleIterator = accessRight.getRoles().iterator();
				while(accessRightRoleIterator.hasNext())
				{
					AccessRightRole accessRightRole = (AccessRightRole)accessRightRoleIterator.next();
					accessRightRole.setAccessRight(accessRight);
					dbAccessRight.create(accessRightRole);
				}
	
				Iterator accessRightGroupIterator = accessRight.getGroups().iterator();
				while(accessRightGroupIterator.hasNext())
				{
					AccessRightGroup accessRightGroup = (AccessRightGroup)accessRightGroupIterator.next();
					accessRightGroup.setAccessRight(accessRight);
					dbAccessRight.create(accessRightGroup);
				}
			}
	
			dbAccessRight.commit();
		}
		catch (Exception e) 
		{
			dbAccessRight.rollback();
		}
		finally
		{
			dbAccessRight.close();
		}
		
		processBean.updateProcess("Access rights imported in " + (t.getElapsedTime() / 1000) + " seconds");
	}

	public void processStructure(File archiveFolder, 
								 /*
								 File siteNodeVersionsFile, 
								 File siteNodesFile, 
								 */
								 String encoding, 
								 String onlyLatestVersions, 
								 Map contentIdMap,
								 Map siteNodeIdMap, 
								 Map replaceMap, 
								 Map repositoryIdMap,
								 Map siteNodeVersionIdMap, 
								 List allSiteNodes,
								 Map<String, AvailableServiceBinding> readAvailableServiceBindings, 
								 Collection<Repository> repositories, 
								 Map<Integer,SiteNodeTypeDefinition> siteNodeTypeDefinitions)
								 throws Exception 
	{
		Map<String, SiteNode> repositorySiteNodeMap = new HashMap<String, SiteNode>();
		Map<Integer, SiteNode> siteNodeMap = new HashMap<Integer, SiteNode>();
		Map<Integer,List<SiteNodeVersion>> allSiteNodeVersionMap = new HashMap<Integer,List<SiteNodeVersion>>(); 

		for(Repository repositoryRead : repositories)
		{
			Integer oldRepoId = (Integer)repositoryIdMap.get("" + repositoryRead.getId() + "_old");

			File siteNodeVersionsFile = null;
			File siteNodesFile = null;
	
			File[] files = archiveFolder.listFiles();
			for(File file : files)
			{
				if(file.getName().equals("SiteNodeVersions_" + oldRepoId + ".xml"))
					siteNodeVersionsFile = file;
				if(file.getName().equals("SiteNodes_" + oldRepoId + ".xml"))
					siteNodesFile = file;
			}
			
			logger.info("siteNodeVersonsFile:" + siteNodeVersionsFile);
			logger.info("siteNodesFile:" + siteNodesFile);
			if(siteNodeVersionsFile == null)
				throw new Exception("No siteNodeVersons file found. Looking for SiteNodeVersions_" + oldRepoId + ".xml in archive");
			if(siteNodesFile == null)
				throw new Exception("No siteNodes file found. Looking for SiteNodes_" + oldRepoId + ".xml in archive");
		
			InfoGlueExportImpl siteNodes = getInfoGlueExportImpl(siteNodesFile, encoding);
			logger.info("siteNodesFile:" + siteNodesFile.getName());
			logger.info("siteNodes:" + siteNodes);
			logger.info("siteNodes:" + siteNodes.getSiteNodes().size());
	
			for(SiteNode siteNode : siteNodes.getSiteNodes())
			{
				siteNodeMap.put(siteNode.getSiteNodeId(), siteNode);
				logger.info("putting as parent siteNode on:" + siteNode.getName() + ":" + siteNode.getValueObject().getParentSiteNodeId());
	
				if(siteNode.getValueObject().getParentSiteNodeId() == null)
				{
					logger.info("siteNode was root:" + siteNode + ":" + siteNode.getValueObject().getId() + ":" + siteNode.getValueObject().getRepositoryId());
					repositorySiteNodeMap.put("" + siteNode.getValueObject().getRepositoryId(), siteNode);
				}
				//logger.info("readContentCandidate debug...:" + readContentCandidate.getName() + ":" + readContentCandidate.getId() + ":" + readContentCandidate.getRepositoryId());
			}
			
			for(SiteNode siteNode : siteNodes.getSiteNodes())
			{
				if(siteNode.getValueObject().getParentSiteNodeId() != null)
				{
					logger.info("Looking for parent siteNode on:" + siteNode.getName() + ":" + siteNode.getValueObject().getParentSiteNodeId());
					SiteNode parentSiteNode = siteNodeMap.get(siteNode.getValueObject().getParentSiteNodeId());
					parentSiteNode.getChildSiteNodes().add(siteNode);
					siteNode.setParentSiteNode((SiteNodeImpl)parentSiteNode);
				}
				
				logger.info("Looking for repo:" + siteNode.getValueObject().getRepositoryId());
				Repository newRepository = (Repository)repositoryIdMap.get("" + siteNode.getValueObject().getRepositoryId() + "_repository");
				logger.info("newRepository:" + newRepository);
				siteNode.setRepository((RepositoryImpl)newRepository);
				siteNode.getValueObject().setRepositoryId(newRepository.getId());
				//logger.info("readContentCandidate debug...:" + readContentCandidate.getName() + ":" + readContentCandidate.getId() + ":" + readContentCandidate.getRepositoryId());
			}
	
			InfoGlueExportImpl siteNodeVersions = getInfoGlueExportImpl(siteNodeVersionsFile, encoding);
			logger.info("siteNodeVersions:" + siteNodeVersions);
	
			for(SiteNodeVersion siteNodeVersion : siteNodeVersions.getSiteNodeVersions())
			{
				List<SiteNodeVersion> versions = allSiteNodeVersionMap.get(siteNodeVersion.getValueObject().getSiteNodeId());
				if(versions == null)
				{
					versions = new ArrayList<SiteNodeVersion>();
					logger.info("Creating versions for " + siteNodeVersion.getValueObject().getSiteNodeId());
					allSiteNodeVersionMap.put(siteNodeVersion.getValueObject().getSiteNodeId(), versions);
				}
				logger.info("Adding version:" + siteNodeVersion.getValueObject().getSiteNodeVersionId() + ":" + siteNodeVersion.getValueObject().getSiteNodeId());
				versions.add(siteNodeVersion);
			}
		}
		
		for(Repository repositoryRead : repositories)
		{
			logger.info("Getting root siteNode for: " + repositoryRead.getId());
			SiteNode rootSiteNode = (SiteNode)repositorySiteNodeMap.get("" + repositoryRead.getId());
			logger.info("rootSiteNode: " + rootSiteNode);
			if(rootSiteNode == null)
			{
				Integer oldRepoId = (Integer)repositoryIdMap.get("" + repositoryRead.getId() + "_old");
				logger.info("Getting root siteNode for: " + oldRepoId);
				rootSiteNode = (SiteNode)repositorySiteNodeMap.get("" + oldRepoId);
				logger.info("rootSiteNode: " + rootSiteNode);
			}

			Database db = CastorDatabaseService.getDatabase();
			try
			{
				db.begin();
			
				createStructure(rootSiteNode, allSiteNodeVersionMap, contentIdMap, siteNodeIdMap, siteNodeVersionIdMap, readAvailableServiceBindings, allSiteNodes, db, onlyLatestVersions, replaceMap, siteNodeTypeDefinitions);
				
				db.commit();
			}
			catch (Exception e) 
			{
				e.printStackTrace();
				db.rollback();
			}
			finally
			{
				db.close();
			}
		}
	}

	public void createContent(File archiveFolder, /*File contentsFile, File contentVersionsFile, */String encoding, Map repositoryIdMap, Collection<Repository> repositories, Map contentIdMap, Map contentTypeIdMap, List allContents, Map<Integer,Language> languages, Collection contentTypeDefinitions, Map categoryIdMap, List<SmallDigitalAssetImpl> allSmallAssets, Map<Integer,List<Integer>> assetVersionsMap, String onlyLatestVersions, boolean isCopyAction, Map<String,String> replaceMap) throws Exception 
	{
		Map<String, Content> repositoryContentMap = new HashMap<String, Content>();
		Map<Integer, Content> contentMap = new HashMap<Integer, Content>();
		Map<Integer,List<ExportContentVersionImpl>> allContentVersionMap = new HashMap<Integer,List<ExportContentVersionImpl>>(); 

		for(Repository repositoryRead : repositories)
		{
			Integer oldRepoId = (Integer)repositoryIdMap.get("" + repositoryRead.getId() + "_old");
			
			File contentVersionsFile = null;
			File contentsFile = null;

			File[] files = archiveFolder.listFiles();
			for(File file : files)
			{
				//logger.info("file:" + file.getName());
				if(file.getName().equals("ContentVersions_" + oldRepoId + ".xml"))
					contentVersionsFile = file;
				if(file.getName().equals("Contents_" + oldRepoId + ".xml"))
					contentsFile = file;
			}
			
			logger.info("archiveFolder:" + archiveFolder);
			logger.info("contentVersionsFile:" + contentVersionsFile);
			logger.info("contentsFile:" + contentsFile);
		
			if(contentVersionsFile == null)
				throw new Exception("No contentVersions file found. Looking for ContentVersions_" + oldRepoId + ".xml in archive");
			if(contentsFile == null)
				throw new Exception("No contents file found. Looking for Contents_" + oldRepoId + ". in archive");

			InfoGlueExportImpl contents = getInfoGlueExportImpl(contentsFile, encoding);
			logger.info("contents:" + contents);
	
			Iterator<Content> contentsIterator = contents.getContents().iterator();
			while(contentsIterator.hasNext())
			{
				Content content = contentsIterator.next();
				logger.info("content:" + content + ":" + content.getValueObject().getId() + ":" + content.getValueObject().getRepositoryId() + ":" + content.getRepositoryId());
				contentMap.put(content.getContentId(), content);
				if(content.getValueObject().getParentContentId() == null)
				{
					logger.info("content was root:" + content + ":" + content.getValueObject().getId() + ":" + content.getValueObject().getRepositoryId() + ":" + content.getRepositoryId());
					if(!repositoryContentMap.containsKey("" + content.getRepositoryId()))
						repositoryContentMap.put("" + content.getRepositoryId(), content);
					else
						logger.info("content was root but skipping as registration allready there:" + content + ":" + content.getValueObject().getId() + ":" + content.getValueObject().getRepositoryId() + ":" + content.getRepositoryId());
								
				}
				//logger.info("readContentCandidate debug...:" + readContentCandidate.getName() + ":" + readContentCandidate.getId() + ":" + readContentCandidate.getRepositoryId());
			}
			
			contentsIterator = contents.getContents().iterator();
			while(contentsIterator.hasNext())
			{
				Content content = contentsIterator.next();
				logger.info("content:" + content.getName() + ":" + content.getValueObject().getParentContentId());
				
				if(content.getValueObject().getParentContentId() != null)
				{
					Content parentContent = contentMap.get(content.getValueObject().getParentContentId());
					if(parentContent != null)
					{
						logger.info("parentContent:" + parentContent.getName() + ":" + parentContent);
						parentContent.getChildren().add(content);
						content.setParentContent((ContentImpl)parentContent);
						
						logger.info("Children after: " + parentContent.getChildren());
					}
					else
					{
						logger.error("Something is strange with parent content id:"+content.getValueObject().getParentContentId() +" it doesn't have a parent content id:"+content.getContentId());
					}
				}
				
				logger.info("repositoryIdMap:" + repositoryIdMap);
				//logger.info("Looking for repo:" + content.getRepositoryId());
				Repository newRepository = (Repository)repositoryIdMap.get("" + content.getRepositoryId() + "_repository");
				//logger.info("newRepository:" + newRepository);
				content.setRepository((RepositoryImpl)newRepository);
				//logger.info("readContentCandidate debug...:" + readContentCandidate.getName() + ":" + readContentCandidate.getId() + ":" + readContentCandidate.getRepositoryId());
			}
			
			InfoGlueExportImpl contentVersions = getInfoGlueExportImpl(contentVersionsFile, encoding);
			logger.info("contentVersions:" + contentVersions);
			float memoryLeft = ((float)Runtime.getRuntime().maxMemory() - (float)Runtime.getRuntime().totalMemory()) / 1024f / 1024f;
			logger.info("Memory after contentVersions-file:" + memoryLeft);
			
			for(ExportContentVersionImpl contentVersion : contentVersions.getContentVersions())
			{
				List<ExportContentVersionImpl> versions = allContentVersionMap.get(contentVersion.getValueObject().getContentId());
				if(versions == null)
				{
					versions = new ArrayList<ExportContentVersionImpl>();
					logger.info("Creating versions for " + contentVersion.getValueObject().getContentId());
					allContentVersionMap.put(contentVersion.getValueObject().getContentId(), versions);
				}
				//logger.info("Adding version:" + contentVersion.getValueObject().getContentVersionId() + ":" + contentVersion.getValueObject().getContentId());
				versions.add(contentVersion);
				//logger.info("readContentCandidate debug...:" + readContentCandidate.getName() + ":" + readContentCandidate.getId() + ":" + readContentCandidate.getRepositoryId());
			}
		}
		
		for(Repository repositoryRead : repositories)
		{
			logger.info("Getting root content for: " + repositoryRead.getId());
			Content rootContent = (Content)repositoryContentMap.get("" + repositoryRead.getId());
			logger.info("rootContent: " + rootContent);
			if(rootContent == null)
			{
				Integer oldRepoId = (Integer)repositoryIdMap.get("" + repositoryRead.getId() + "_old");
				logger.info("Getting root content for: " + oldRepoId);
				rootContent = (Content)repositoryContentMap.get("" + oldRepoId);
				logger.info("rootContent: " + rootContent);
			}
			
			Database db = CastorDatabaseService.getDatabase();
			try
			{
				db.begin();
			
				createContents(rootContent, allContentVersionMap, contentIdMap, contentTypeIdMap, allContents, languages, Collections.unmodifiableCollection(contentTypeDefinitions), categoryIdMap, allSmallAssets, assetVersionsMap, db, onlyLatestVersions, isCopyAction, replaceMap);
				
				logger.info("Contents created...");
				
				db.commit();
			}
			catch (Exception e) 
			{
				logger.error("Problem creating contents: " + e.getMessage(), e);
				db.rollback();
			}
			finally
			{
				db.close();
			}
		}

	}

	
	public void importFoundation(InfoGlueExportImpl master, String encoding, boolean isCopyAction, Map replaceMap, Map categoryIdMap, Map repositoryIdMap) throws Exception 
	{
		//Collection contentTypeDefinitions = master.getContentTypeDefinitions();
		Collection contentTypeDefinitions = master.getContentTypeDefinitions();
		Collection<Repository> repositories = master.getRepositories();
		logger.info("Found " + repositories.size() + " repositories");

		logger.info("Found " + contentTypeDefinitions.size() + " content type definitions");
		Collection categories = master.getCategories();
		logger.info("Found " + categories.size() + " categories");
		
		Database db = CastorDatabaseService.getDatabase();
		
		try
		{
			db.begin();
		
			if(!isCopyAction)
			{
				importCategories(categories, null, categoryIdMap, db);
				updateContentTypeDefinitions(contentTypeDefinitions, categoryIdMap);
			}
			logger.info("Categories and content types imported");
	
			//Collection<Repository> repositories = master.getRepositories();
			for(Repository repositoryRead : repositories)
			{
				logger.info(repositoryRead.getName());
				logger.info("Repo:" + repositoryRead.getName());
		
				repositoryRead.setName(substituteStrings(repositoryRead.getName(), replaceMap));
				repositoryRead.setDescription(substituteStrings(repositoryRead.getDescription(), replaceMap));
				repositoryRead.setDnsName(substituteStrings(repositoryRead.getDnsName(), replaceMap));
					
				Integer repositoryIdBefore = repositoryRead.getId();
				db.create(repositoryRead);
				Integer repositoryIdAfter = repositoryRead.getId();
				repositoryIdMap.put("" + repositoryIdBefore, repositoryIdAfter);
				repositoryIdMap.put("" + repositoryIdAfter + "_old", repositoryIdBefore);
				repositoryIdMap.put("" + repositoryIdBefore + "_repository", repositoryRead);
		
				Collection repositoryLanguages = repositoryRead.getRepositoryLanguages();
				Iterator repositoryLanguagesIterator = repositoryLanguages.iterator();
				while(repositoryLanguagesIterator.hasNext())
				{
					RepositoryLanguage repositoryLanguage = (RepositoryLanguage)repositoryLanguagesIterator.next();
					Language originalLanguage = repositoryLanguage.getLanguage();
					
					Language language = LanguageController.getController().getLanguageWithCode(originalLanguage.getLanguageCode(), db);
					if(language == null)
					{
					    db.create(originalLanguage);
					    language = originalLanguage;
					}
					
					repositoryLanguage.setLanguage(language);
					repositoryLanguage.setRepository(repositoryRead);
		
					db.create(repositoryLanguage);
					
					logger.info("language:" + language);
					logger.info("language.getRepositoryLanguages():" + language.getRepositoryLanguages());
					language.getRepositoryLanguages().add(repositoryLanguage);
				}
			}
			db.commit();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			db.rollback();
		}
		finally
		{
			db.close();
		}
	}

	private String substituteStrings(String originalValue, Map<String,String> replaceMap)
	{
		String newValue = originalValue;
		Iterator<String> replaceMapIterator = replaceMap.keySet().iterator();
		while(replaceMapIterator.hasNext())
		{
			String key = replaceMapIterator.next();
			if(originalValue.indexOf("298") > -1)
			{
				logger.info("key:" + key);
			}
			String value = replaceMap.get(key);
			//key = key.replaceAll("\"", "\""); //Escaping "-sign
			newValue = newValue.replaceAll(key, value);
		}
		return newValue;
	}

	
	private void updateContentTypeDefinitions(Collection contentTypeDefinitions, Map categoryIdMap) 
	{
		Iterator contentTypeDefinitionsIterator = contentTypeDefinitions.iterator();
		while(contentTypeDefinitionsIterator.hasNext())
		{
			ContentTypeDefinition contentTypeDefinition = (ContentTypeDefinition)contentTypeDefinitionsIterator.next();
			String schema = contentTypeDefinition.getSchemaValue();
			Iterator categoryIdMapIterator = categoryIdMap.keySet().iterator();
			while(categoryIdMapIterator.hasNext())
			{
				Integer oldId = (Integer)categoryIdMapIterator.next();
				Integer newId = (Integer)categoryIdMap.get(oldId);
				schema = schema.replaceAll("<categoryId>" + oldId + "</categoryId>", "<categoryId>new_" + newId + "</categoryId>");
			}
			schema = schema.replaceAll("<categoryId>new_", "<categoryId>");
			contentTypeDefinition.setSchemaValue(schema);
		}
	}

	private void importCategories(Collection categories, CategoryVO parentCategory, Map categoryIdMap, Database db) throws SystemException
	{
		logger.info("We want to create a list of categories if not existing under the parent category " + parentCategory);
		Iterator categoryIterator = categories.iterator();
		while(categoryIterator.hasNext())
		{
			CategoryVO categoryVO = (CategoryVO)categoryIterator.next();
			CategoryVO newParentCategory = null;
			
			List<CategoryVO> existingCategories = null;
			if(parentCategory != null)
				existingCategories = CategoryController.getController().getActiveCategoryVOListByParent(parentCategory.getCategoryId(), db);
				//existingCategories = CategoryController.getController().findByParent(parentCategory.getCategoryId(), db);
			else
				existingCategories = CategoryController.getController().findRootCategoryVOList(db);
				
			Iterator<CategoryVO> existingCategoriesIterator = existingCategories.iterator();
			while(existingCategoriesIterator.hasNext())
			{
				CategoryVO existingCategory = existingCategoriesIterator.next();
				logger.info("existingCategory:" + existingCategory.getName());
				if(existingCategory.getName().equals(categoryVO.getName()))
				{
					logger.info("Existed... setting " + existingCategory.getName() + " to new parent category.");
					newParentCategory = existingCategory;
					break;
				}
			}

			if(newParentCategory == null)
			{
				logger.info("No existing category - we create it: " + categoryVO);
				Integer oldId = categoryVO.getId();
				categoryVO.setCategoryId(null);
				if(parentCategory != null)	
					categoryVO.setParentId(parentCategory.getCategoryId());
				else
					categoryVO.setParentId(null);
					
				Category newCategory = CategoryController.getController().save(categoryVO, db);
				categoryIdMap.put(oldId, newCategory.getCategoryId());
				newParentCategory = newCategory.getValueObject();
			}
			else
			{
				categoryIdMap.put(categoryVO.getId(), newParentCategory.getCategoryId());
			}
				
			importCategories(categoryVO.getChildren(), newParentCategory, categoryIdMap, db);
		}
	}
	
	/**
	 * This method copies a sitenode and all it relations.
	 * 
	 * @param siteNode
	 * @param db
	 * @throws Exception
	 */
	private void createStructure(SiteNode siteNode, Map<Integer,List<SiteNodeVersion>> allSiteNodeVersionMap, Map contentIdMap, Map siteNodeIdMap, Map siteNodeVersionIdMap, Map readAvailableServiceBindings, List allSiteNodes, Database db, String onlyLatestVersions, Map<String,String> replaceMap, Map<Integer,SiteNodeTypeDefinition> siteNodeTypeDefinitions) throws Exception
	{
		if(siteNode != null)
			logger.info("createStructure with siteNode:" + siteNode.getName());
		else
			logger.info("createStructure with siteNode:" + siteNode);
		Integer originalSiteNodeId = siteNode.getValueObject().getSiteNodeId();

		logger.info("originalSiteNodeId:" + originalSiteNodeId);

		SiteNodeTypeDefinition originalSiteNodeTypeDefinition = siteNode.getSiteNodeTypeDefinition();
		logger.info("originalSiteNodeTypeDefinition:" +originalSiteNodeTypeDefinition);
		
		if(originalSiteNodeTypeDefinition == null)
		{
			Integer siteNodeTypeDefinitionId = siteNode.getValueObject().getSiteNodeTypeDefinitionId();
			logger.info("siteNodeTypeDefinitionId: " + siteNodeTypeDefinitionId);
			originalSiteNodeTypeDefinition = siteNodeTypeDefinitions.get(siteNodeTypeDefinitionId);
			logger.info("siteNodeTypeDefinitions: " + siteNodeTypeDefinitions);
			logger.info("originalSiteNodeTypeDefinition: " + originalSiteNodeTypeDefinition);
		}
		
		SiteNodeTypeDefinition siteNodeTypeDefinition = null;
		if(originalSiteNodeTypeDefinition != null)
		{
			logger.info("originalSiteNodeTypeDefinition:" + originalSiteNodeTypeDefinition);
			siteNodeTypeDefinition = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionWithName(originalSiteNodeTypeDefinition.getName(), db, false);
			logger.info("siteNodeTypeDefinition:" + siteNodeTypeDefinition);
			if(siteNodeTypeDefinition == null)
			{
			    db.create(originalSiteNodeTypeDefinition);
			    siteNodeTypeDefinition = originalSiteNodeTypeDefinition;
				logger.info("originalSiteNodeTypeDefinition ID:" + originalSiteNodeTypeDefinition.getId());
			}
			else
				logger.info("siteNodeTypeDefinition ID:" + siteNodeTypeDefinition.getId());

			siteNode.setSiteNodeTypeDefinition((SiteNodeTypeDefinitionImpl)siteNodeTypeDefinition);
			siteNode.getValueObject().setSiteNodeTypeDefinitionId(siteNodeTypeDefinition.getId());
		}
		
		
		String mappedMetaInfoContentId = "-1";
		if(siteNode.getMetaInfoContentId() != null)
		{
			if(contentIdMap.containsKey(siteNode.getMetaInfoContentId().toString()))
				mappedMetaInfoContentId = (String)contentIdMap.get(siteNode.getMetaInfoContentId().toString());
		}
		siteNode.setMetaInfoContentId(new Integer(mappedMetaInfoContentId));
		
		siteNode.setName(substituteStrings(siteNode.getName(), replaceMap));
		db.create(siteNode);
		
		allSiteNodes.add(siteNode);
		    
		Integer newSiteNodeId = siteNode.getSiteNodeId();
		logger.info(originalSiteNodeId + ":" + newSiteNodeId);
		siteNodeIdMap.put(originalSiteNodeId.toString(), newSiteNodeId.toString());
		
		List<SiteNodeVersion> siteNodeVersions = allSiteNodeVersionMap.get(originalSiteNodeId);
		logger.info("new allSiteNodeVersionMap:" + allSiteNodeVersionMap.size());
		logger.info("Getting versions for " + siteNodeVersions + ":" + originalSiteNodeId);
		
		if(onlyLatestVersions.equalsIgnoreCase("true"))
		{
			SiteNodeVersion lastSiteNodeVersion = null;
			if(siteNodeVersions != null)
			{
				for(SiteNodeVersion siteNodeVersion : siteNodeVersions)
				{
					if(siteNodeVersion.getIsActive().booleanValue())
					{
						lastSiteNodeVersion = lastSiteNodeVersion;
					}						
				}
			}	
			
			if(lastSiteNodeVersion != null)
			{
				siteNodeVersions.clear();
				siteNodeVersions.add(lastSiteNodeVersion);
			}
		}

		if(siteNodeVersions != null)
		{
			for(SiteNodeVersion siteNodeVersion : siteNodeVersions)
			{
				logger.info("Creating version:" + siteNodeVersion.getValueObject().getId() + " on " + siteNode.getName());
				siteNodeVersion.setOwningSiteNode((SiteNodeImpl)siteNode);
				siteNodeVersion.getValueObject().setSiteNodeId(siteNode.getId());
				
				Integer oldSiteNodeVersionId = siteNodeVersion.getId();
	
				db.create(siteNodeVersion);
	
				Integer newSiteNodeVersionId = siteNodeVersion.getId();
				siteNodeVersionIdMap.put(oldSiteNodeVersionId.toString(), newSiteNodeVersionId.toString());
				
				/*
				Collection serviceBindings = siteNodeVersion.getServiceBindings();
				Iterator serviceBindingsIterator = serviceBindings.iterator();
				while(serviceBindingsIterator.hasNext())
				{
					ServiceBinding serviceBinding = (ServiceBinding)serviceBindingsIterator.next();
					logger.info("serviceBinding:" + serviceBinding.getName());
					ServiceDefinition originalServiceDefinition = serviceBinding.getServiceDefinition();
					if(originalServiceDefinition == null)
					{
						logger.error("Skipping serviceBinding:" + serviceBinding.getName() + ":" + "serviceBinding:" + serviceBinding.getId() + " " + serviceBinding.getServiceDefinition());
						continue;
					}
					
					String serviceDefinitionName = originalServiceDefinition.getName();
					ServiceDefinition serviceDefinition = ServiceDefinitionController.getController().getServiceDefinitionWithName(serviceDefinitionName, db, false);
					if(serviceDefinition == null)
					{
					    db.create(originalServiceDefinition);
					    serviceDefinition = originalServiceDefinition;
					    //availableServiceBinding.getServiceDefinitions().add(serviceDefinition);
					}
					
					serviceBinding.setServiceDefinition((ServiceDefinitionImpl)serviceDefinition);
	
					AvailableServiceBinding originalAvailableServiceBinding = serviceBinding.getAvailableServiceBinding();
					String availableServiceBindingName = originalAvailableServiceBinding.getName();
					logger.info("availableServiceBindingName:" + availableServiceBindingName);
					logger.info("readAvailableServiceBindings:" + readAvailableServiceBindings.size() + ":" + readAvailableServiceBindings.containsKey(availableServiceBindingName));
					AvailableServiceBinding availableServiceBinding = (AvailableServiceBinding)readAvailableServiceBindings.get(availableServiceBindingName);
					logger.info("availableServiceBinding:" + availableServiceBinding);
					if(availableServiceBinding == null)
					{
						availableServiceBinding = AvailableServiceBindingController.getController().getAvailableServiceBindingWithName(availableServiceBindingName, db, false);
						logger.info("Read availableServiceBinding from database:" + availableServiceBindingName + "=" + availableServiceBinding);
						readAvailableServiceBindings.put(availableServiceBindingName, availableServiceBinding);
						logger.info("readAvailableServiceBindings:" + readAvailableServiceBindings.size() + ":" + readAvailableServiceBindings.containsKey(availableServiceBindingName));
					}
					
					if(availableServiceBinding == null)
					{
					    logger.info("There was no availableServiceBinding registered under:" + availableServiceBindingName);
					    logger.info("originalAvailableServiceBinding:" + originalAvailableServiceBinding.getName() + ":" + originalAvailableServiceBinding.getIsInheritable());
					    db.create(originalAvailableServiceBinding);
					    availableServiceBinding = originalAvailableServiceBinding;
					    readAvailableServiceBindings.put(availableServiceBindingName, availableServiceBinding);
					    
					    logger.info("Notifying:" + siteNodeTypeDefinition.getName() + " about the new availableServiceBinding " + availableServiceBinding.getName());
					    if(siteNodeTypeDefinition != null)
					    {
						    siteNodeTypeDefinition.getAvailableServiceBindings().add((AvailableServiceBindingImpl)availableServiceBinding);
						    serviceDefinition.getAvailableServiceBindings().add((AvailableServiceBindingImpl)availableServiceBinding);
						    availableServiceBinding.getSiteNodeTypeDefinitions().add((SiteNodeTypeDefinitionImpl)siteNodeTypeDefinition);
						    availableServiceBinding.getServiceDefinitions().add((ServiceDefinitionImpl)serviceDefinition);
					    }
					}
					else
					{
						if(siteNodeTypeDefinition != null && !siteNodeTypeDefinition.getAvailableServiceBindings().contains(availableServiceBinding))
						{
							siteNodeTypeDefinition.getAvailableServiceBindings().add((AvailableServiceBindingImpl)availableServiceBinding);
							availableServiceBinding.getSiteNodeTypeDefinitions().add(siteNodeTypeDefinition);
						}
					}
					
					serviceBinding.setAvailableServiceBinding((AvailableServiceBindingImpl)availableServiceBinding);
					
					
					Collection qualifyers = serviceBinding.getBindingQualifyers();
					Iterator qualifyersIterator = qualifyers.iterator();
					while(qualifyersIterator.hasNext())
					{
						Qualifyer qualifyer = (Qualifyer)qualifyersIterator.next();
						qualifyer.setServiceBinding((ServiceBindingImpl)serviceBinding);
						
						String entityName 	= qualifyer.getName();
						String entityId		= qualifyer.getValue();
						
						if(entityName.equalsIgnoreCase("contentId"))
						{
							String mappedContentId = (String)contentIdMap.get(entityId);
							qualifyer.setValue((mappedContentId == null) ? entityId : mappedContentId);
						}
						else if(entityName.equalsIgnoreCase("siteNodeId"))
						{
							String mappedSiteNodeId = (String)siteNodeIdMap.get(entityId);
							qualifyer.setValue((mappedSiteNodeId == null) ? entityId : mappedSiteNodeId);						
						}
					}
	
					serviceBinding.setSiteNodeVersion((SiteNodeVersionImpl)siteNodeVersion);				
	
					db.create(serviceBinding);
	
				}
				*/
			}
		}	
		
		Collection childSiteNodes = siteNode.getChildSiteNodes();
		if(childSiteNodes != null)
		{
			Iterator childSiteNodesIterator = childSiteNodes.iterator();
			while(childSiteNodesIterator.hasNext())
			{
				SiteNode childSiteNode = (SiteNode)childSiteNodesIterator.next();
				//childSiteNode.setRepository(siteNode.getRepository());
				childSiteNode.setParentSiteNode((SiteNodeImpl)siteNode);
				childSiteNode.getValueObject().setParentSiteNodeId(siteNode.getValueObject().getSiteNodeId());
				createStructure(childSiteNode, allSiteNodeVersionMap, contentIdMap, siteNodeIdMap, siteNodeVersionIdMap, readAvailableServiceBindings, allSiteNodes, db, onlyLatestVersions, replaceMap, siteNodeTypeDefinitions);
			}
		}
		
	}


	/**
	 * This method copies a content and all it relations.
	 * 
	 * @param siteNode
	 * @param db
	 * @throws Exception
	 */
	
	private List createContents(Content content, Map<Integer,List<ExportContentVersionImpl>> allContentVersionMap, Map idMap, Map contentTypeDefinitionIdMap, List allContents, Map<Integer,Language> languages, Collection contentTypeDefinitions, Map categoryIdMap, List<SmallDigitalAssetImpl> allSmallAssets, Map<Integer,List<Integer>> assetVersionsMap, Database db, String onlyLatestVersions, boolean isCopyAction, Map<String,String> replaceMap) throws Exception
	{
		Map<Integer,Boolean> handledSmallAssets = new HashMap<Integer,Boolean>();
    	//logger.info("createContents:" + content + ":" + allContentVersionMap.size());

		ContentTypeDefinition contentTypeDefinition = null;
			
	    Integer originalContentId = content.getContentId();
	    Integer contentTypeDefinitionId = ((ContentImpl)content).getContentTypeDefinitionId();
		    
		if(contentTypeDefinitionId != null)
		{
			if(!isCopyAction)
			{
    			if(contentTypeDefinitionIdMap.containsKey(contentTypeDefinitionId))
    				contentTypeDefinitionId = (Integer)contentTypeDefinitionIdMap.get(contentTypeDefinitionId);
				
		    	ContentTypeDefinition originalContentTypeDefinition = null;
		    	Iterator contentTypeDefinitionsIterator = contentTypeDefinitions.iterator();
		    	while(contentTypeDefinitionsIterator.hasNext())
		    	{
		    		ContentTypeDefinition contentTypeDefinitionCandidate = (ContentTypeDefinition)contentTypeDefinitionsIterator.next();		    		
		    		if(contentTypeDefinitionCandidate.getId().intValue() == contentTypeDefinitionId.intValue())
		    		{
		    			originalContentTypeDefinition = contentTypeDefinitionCandidate;
		    			break;
		    		}
		    	}

		    	if(originalContentTypeDefinition != null)
		    	{
			    	contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithName(originalContentTypeDefinition.getName(), db);

			    	if(contentTypeDefinition == null)
					{
			    		Integer before = originalContentTypeDefinition.getId();
			    		db.create(originalContentTypeDefinition);
			    		contentTypeDefinition = originalContentTypeDefinition;
			    		Integer after = originalContentTypeDefinition.getId();
			    		contentTypeDefinitionIdMap.put(before, after);
			    	}
				
		    		content.setContentTypeDefinition((ContentTypeDefinitionImpl)contentTypeDefinition);
			    	content.setContentTypeDefinitionId(contentTypeDefinition.getId());
		    	}
			    else
			    {
			    	logger.error("The content " + content.getName() + " had a content type not found amongst the listed ones:" + contentTypeDefinitionId);
			    }
			}
			else
			{
		    	contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithId(contentTypeDefinitionId, db);
		    	content.setContentTypeDefinition((ContentTypeDefinitionImpl)contentTypeDefinition);
		    	content.setContentTypeDefinitionId(contentTypeDefinition.getId());
			}
	    }
	    else
	    	logger.warn("The content " + content.getName() + " had no content type at all");
    
	    
	    if(content.getContentTypeDefinition() == null)
	    	logger.warn("No content type definition for content:" + content.getId());
	    	
	    content.setName(substituteStrings(content.getName(), replaceMap));
	    
	    //if(content.getParentContent() != null)
	    //	logger.info("content to create:" + content.getName() + ":" + content.getParentContent().getContentId());

	    db.create(content);
		
	    //if(content.getParentContent() != null)
	    //	logger.info("content was created:" + content.getName() + ":" + content.getParentContent().getContentId());
	    
	    allContents.add(content);
		
		Integer newContentId = content.getContentId();
	    logger.info("Creating content:" + content.getName() + ". " + originalContentId + "-->" + newContentId);
		idMap.put(originalContentId.toString(), newContentId.toString());
	
		//logger.info("Getting versions for " + originalContentId);
		
		List<ExportContentVersionImpl> contentVersions = allContentVersionMap.get(originalContentId);
		logger.info("new allContentVersionMap:" + allContentVersionMap.size());
		//logger.info("Getting versions for " + contentVersions + ":" + originalContentId);
		
		/*
		if(onlyLatestVersions.equalsIgnoreCase("true"))
		{
			List selectedContentVersions = new ArrayList();
			Iterator realContentVersionsIterator = contentVersions.iterator();
			while(realContentVersionsIterator.hasNext())
			{
				ExportContentVersionImpl contentVersion = (ExportContentVersionImpl)realContentVersionsIterator.next();			
				Iterator selectedContentVersionsIterator = selectedContentVersions.iterator();
				boolean addLanguageVersion = true;
				boolean noLanguageVersionFound = true;
				while(selectedContentVersionsIterator.hasNext())
				{
					ContentVersion currentContentVersion = (ContentVersion)selectedContentVersionsIterator.next();
					logger.info("" + currentContentVersion.getLanguage().getLanguageCode() + "=" + contentVersion.getLanguage().getLanguageCode());
					if(currentContentVersion.getLanguage().getLanguageCode().equals(contentVersion.getLanguage().getLanguageCode()))
					{
						noLanguageVersionFound = false;
						
						logger.info("" + contentVersion.getIsActive() + "=" + contentVersion.getLanguage().getLanguageCode());
						if(contentVersion.getIsActive().booleanValue() && contentVersion.getContentVersionId().intValue() > currentContentVersion.getContentVersionId().intValue())
						{
							logger.info("A later version was found... removing this one..");
							selectedContentVersionsIterator.remove();
							addLanguageVersion = true;
						}						
					}
				}
	
				if(addLanguageVersion || noLanguageVersionFound)
					selectedContentVersions.add(contentVersion);
			}	
			
			contentVersions = selectedContentVersions;
		}
		*/
		
		if(contentVersions != null)
		{
			Iterator contentVersionsIterator = contentVersions.iterator();
			while(contentVersionsIterator.hasNext())
			{
				ExportContentVersionImpl contentVersion = (ExportContentVersionImpl)contentVersionsIterator.next();
				Integer languageId = contentVersion.getLanguageId();
				Language oldLanguage = languages.get(languageId);
				if(oldLanguage == null)
				{
					logger.warn("A null language.... strange... setting master language");
					LanguageVO oldLanguageVO = LanguageController.getController().getMasterLanguage(content.getRepositoryId(), db);
					oldLanguage = LanguageController.getController().getLanguageWithId(oldLanguageVO.getId(), db);
				}
				
				Language language = LanguageController.getController().getLanguageWithCode(oldLanguage.getLanguageCode(), db);
				logger.info("Creating contentVersion for language:" + oldLanguage + " on content " + content.getName());
	
				ContentVersion cv = new ContentVersionImpl();
				cv.setValueObject(contentVersion.getValueObject());
				
				cv.setOwningContent((ContentImpl)content);
				cv.setLanguage((LanguageImpl)language);
				
				db.create(cv);

				List<SmallDigitalAssetImpl> smallDigitalAssets = (List)contentVersion.getSmallDigitalAssets();
				//logger.info("AAAAAAAAAAAAA: on " + content.getName() + "/" + contentVersion.getId() + " was " + (smallDigitalAssets == null ? "null" : smallDigitalAssets.size()) + " assets");
				if(smallDigitalAssets != null)
				{
					List initialDigitalAssets = new ArrayList();
						
					for(SmallDigitalAssetImpl asset : smallDigitalAssets)
					{
						if(!handledSmallAssets.containsKey(asset.getId()))
						{
							allSmallAssets.add(asset);
							handledSmallAssets.put(asset.getId(), true);
						}

						List<Integer> versionIdsUsingAsset = assetVersionsMap.get(asset.getId());
						if(versionIdsUsingAsset == null)
						{
							versionIdsUsingAsset = new ArrayList<Integer>();
							assetVersionsMap.put(asset.getId(), versionIdsUsingAsset);
						}
						versionIdsUsingAsset.add(cv.getId());
					}
				}
	
				Collection contentCategories = contentVersion.getContentCategories();
				logger.info("contentCategories:" + contentCategories.size());
				
				if(contentCategories != null)
				{
					List initialContentCategories = new ArrayList();
						
					Iterator contentCategoriesIterator = contentCategories.iterator();
					while(contentCategoriesIterator.hasNext())
					{
						ContentCategory contentCategory = (ContentCategory)contentCategoriesIterator.next();
						logger.info("contentCategory:" + contentCategory);
						contentCategory.setContentVersion((ContentVersionImpl)cv);
						
						Integer oldCategoryId = contentCategory.getCategoryId();
						logger.info("oldCategoryId:" + oldCategoryId);
						if(!isCopyAction)
						{
							Integer newCategoryId = (Integer)categoryIdMap.get(oldCategoryId);
							logger.info("newCategoryId:" + newCategoryId);
							if(newCategoryId == null)
								newCategoryId = oldCategoryId;
							
							if(newCategoryId != null)
							{
								Category category = CategoryController.getController().findById(newCategoryId, db);
								logger.info("Got category:" + category);
								if(category != null)
								{
									contentCategory.setCategory((CategoryImpl)category);
									logger.info("Creating content category:" + contentCategory);
									
									db.create(contentCategory);
								
									initialContentCategories.add(contentCategory);
								}
							}
						}
						else
						{
							Category category = CategoryController.getController().findById(oldCategoryId, db);
							logger.info("Got category:" + category);
							if(category != null)
							{
								contentCategory.setCategory((CategoryImpl)category);
								logger.info("Creating content category:" + contentCategory);
								
								db.create(contentCategory);
							
								initialContentCategories.add(contentCategory);
							}
						}
					}
					
					contentVersion.setContentCategories(initialContentCategories);
				}
		
			}		
		}
		
		Collection childContents = content.getChildren();
		logger.info("childContents:" + childContents);
		if(childContents != null)
		{
			Iterator childContentsIterator = childContents.iterator();
			while(childContentsIterator.hasNext())
			{
				Content childContent = (Content)childContentsIterator.next();
				//childContent.setRepository(content.getRepository());
				logger.info("Setting parentContent on child: " + childContent.getName() + " to:" + content.getId());
				childContent.setParentContent((ContentImpl)content);
				childContent.getValueObject().setParentContentId(content.getValueObject().getContentId());
				createContents(childContent, allContentVersionMap, idMap, contentTypeDefinitionIdMap, allContents, languages, contentTypeDefinitions, categoryIdMap, allSmallAssets, assetVersionsMap, db, onlyLatestVersions, isCopyAction, replaceMap);
			}
		}
		
		
		return allContents;
	}


	/**
	 * This method updates all the bindings in content-versions to reflect the move. 
	 */
	public void updateContentVersions(Content content, Map contentIdMap, Map siteNodeIdMap, String onlyLatestVersions, Map replaceMap) throws Exception
	{
	    logger.info("content:" + content.getName());

	    Collection contentVersions = content.getContentVersions();
	        
		if(onlyLatestVersions.equalsIgnoreCase("true"))
		{
			logger.info("org contentVersions:" + contentVersions.size());
			List selectedContentVersions = new ArrayList();
			Iterator realContentVersionsIterator = contentVersions.iterator();
			while(realContentVersionsIterator.hasNext())
			{
				ContentVersion contentVersion = (ContentVersion)realContentVersionsIterator.next();			
				Iterator selectedContentVersionsIterator = selectedContentVersions.iterator();
				boolean addLanguageVersion = true;
				boolean noLanguageVersionFound = true;
				while(selectedContentVersionsIterator.hasNext())
				{
					ContentVersion currentContentVersion = (ContentVersion)selectedContentVersionsIterator.next();
					logger.info("" + currentContentVersion.getLanguage().getLanguageCode() + "=" + contentVersion.getLanguage().getLanguageCode());
					if(currentContentVersion.getLanguage().getLanguageCode().equals(contentVersion.getLanguage().getLanguageCode()))
					{
						noLanguageVersionFound = false;
						
						logger.info("" + contentVersion.getIsActive() + "=" + contentVersion.getLanguage().getLanguageCode());
						if(contentVersion.getIsActive().booleanValue() && contentVersion.getContentVersionId().intValue() > currentContentVersion.getContentVersionId().intValue())
						{
							logger.info("A later version was found... removing this one..");
							selectedContentVersionsIterator.remove();
							addLanguageVersion = true;
						}						
					}
				}
	
				if(addLanguageVersion || noLanguageVersionFound)
				{
					selectedContentVersions.add(contentVersion);
				}	
			}	
			
			contentVersions = selectedContentVersions;
		}

        
        Iterator contentVersionIterator = contentVersions.iterator();
        while(contentVersionIterator.hasNext())
        {
            ContentVersion contentVersion = (ContentVersion)contentVersionIterator.next();
            try
            {
	            String contentVersionValue = contentVersion.getVersionValue();
	            
	            contentVersionValue = substituteStrings(contentVersionValue, replaceMap);
	            
	            contentVersionValue = contentVersionValue.replaceAll("contentId=\"", "contentId=\"oldContentId_");
	            contentVersionValue = contentVersionValue.replaceAll("\\?contentId=", "\\?contentId=oldContentId_");
	            contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(", "getInlineAssetUrl\\(oldContentId_");
	            contentVersionValue = contentVersionValue.replaceAll("languageId,", "languageId,oldContentId_");
	            contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"", "entity=\"Content\" entityId=\"oldContentId_");
	            //contentVersionValue = contentVersionValue.replaceAll("entity='Content'><id>", "entity='Content'><id>oldContentId_");
	            contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"", "siteNodeId=\"oldSiteNodeId_");
	            contentVersionValue = contentVersionValue.replaceAll("detailSiteNodeId=\"", "detailSiteNodeId=\"oldSiteNodeId_");
	            contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\((\\d)", "getPageUrl\\(oldSiteNodeId_$1");
	            contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"", "entity=\"SiteNode\" entityId=\"oldSiteNodeId_");
	            //contentVersionValue = contentVersionValue.replaceAll("entity='SiteNode'><id>", "entity='SiteNode'><id>old_");
	            
	            Iterator<String> replaceMapIterator = replaceMap.keySet().iterator();
	            while(replaceMapIterator.hasNext())
	            {
	            	String key = replaceMapIterator.next();
	            	String value = (String)replaceMap.get(key);
	            	contentVersionValue = contentVersionValue.replaceAll(key, value);
	            }
	            
	            contentVersionValue = this.prepareAllRelations(contentVersionValue);
	            	            
	            
	            //logger.info("contentVersionValue before:" + contentVersionValue);
	            
	            Iterator contentIdMapIterator = contentIdMap.keySet().iterator();
	            while (contentIdMapIterator.hasNext()) 
	            {
	                String oldContentId = (String)contentIdMapIterator.next();
	                String newContentId = (String)contentIdMap.get(oldContentId);
	                
	                //logger.info("Replacing all:" + oldContentId + " with " + newContentId);
	                
	                contentVersionValue = contentVersionValue.replaceAll("contentId=\"oldContentId_" + oldContentId + "\"", "contentId=\"" + newContentId + "\"");
	                contentVersionValue = contentVersionValue.replaceAll("\\?contentId=oldContentId_" + oldContentId + "&", "\\?contentId=" + newContentId + "&");
	                contentVersionValue = contentVersionValue.replaceAll("getInlineAssetUrl\\(oldContentId_" + oldContentId + ",", "getInlineAssetUrl\\(" + newContentId + ",");
	                contentVersionValue = contentVersionValue.replaceAll("languageId,oldContentId_" + oldContentId + "\\)", "languageId," + newContentId + "\\)");
	                contentVersionValue = contentVersionValue.replaceAll("entity=\"Content\" entityId=\"oldContentId_" + oldContentId + "\"", "entity=\"Content\" entityId=\"" + newContentId + "\"");
	                contentVersionValue = contentVersionValue.replaceAll("<id>oldContentId_" + oldContentId + "</id>", "<id>" + newContentId + "</id>");
	                //contentVersionValue = contentVersionValue.replaceAll("entity='Content'><id>old_" + oldContentId + "</id>", "entity='Content'><id>" + newContentId + "</id>");
	                //contentVersionValue = contentVersionValue.replaceAll("<id>" + oldContentId + "</id>", "<id>" + newContentId + "</id>");
	            }
	            
	            Iterator siteNodeIdMapIterator = siteNodeIdMap.keySet().iterator();
	            while (siteNodeIdMapIterator.hasNext()) 
	            {
	                String oldSiteNodeId = (String)siteNodeIdMapIterator.next();
	                String newSiteNodeId = (String)siteNodeIdMap.get(oldSiteNodeId);
	                
	                //logger.info("Replacing all:" + oldSiteNodeId + " with " + newSiteNodeId);
	                
	                contentVersionValue = contentVersionValue.replaceAll("siteNodeId=\"oldSiteNodeId_" + oldSiteNodeId + "\"", "siteNodeId=\"" + newSiteNodeId + "\"");
	                contentVersionValue = contentVersionValue.replaceAll("detailSiteNodeId=\"oldSiteNodeId_" + oldSiteNodeId + "\"", "detailSiteNodeId=\"" + newSiteNodeId + "\"");
	                contentVersionValue = contentVersionValue.replaceAll("getPageUrl\\(oldSiteNodeId_" + oldSiteNodeId + ",", "getPageUrl\\(" + newSiteNodeId + ",");
	                contentVersionValue = contentVersionValue.replaceAll("entity=\"SiteNode\" entityId=\"oldSiteNodeId_" + oldSiteNodeId + "\"", "entity=\"SiteNode\" entityId=\"" + newSiteNodeId + "\"");
	                //contentVersionValue = contentVersionValue.replaceAll("entity='SiteNode'><id>old_" + oldSiteNodeId + "</id>", "entity='SiteNode'><id>" + newSiteNodeId + "</id>");
	                contentVersionValue = contentVersionValue.replaceAll("<id>oldSiteNodeId_" + oldSiteNodeId + "</id>", "<id>" + newSiteNodeId + "</id>");
	            }
	            
	            //logger.info("contentVersionValue after:" + contentVersionValue);
	            
	            //Now replace all occurrances of old as they should never be there.
	            contentVersionValue = contentVersionValue.replaceAll("oldContentId_", "");
	            contentVersionValue = contentVersionValue.replaceAll("oldSiteNodeId_", "");
	
	            logger.info("new contentVersionValue:" + contentVersionValue);
	            contentVersion.setVersionValue(contentVersionValue);
	            contentVersion.getValueObject().setVersionValue(contentVersionValue);
            }
            catch(Exception e)
            {
            	logger.error("Problem substituting content ids: " + e.getMessage(), e);
            }
        }
	}

	private String prepareAllRelations(String xml) throws Exception
	{
		StringBuffer newXML = new StringBuffer();
		
		logger.info("xml: " + xml);
		
    	String after = xml;
    	String before = "";
    	String qualifyer = "";
    	boolean changed = false; 
    	
    	int startIndex = xml.indexOf("<qualifyer");
    	while(startIndex > -1)
    	{
    		int stopIndex = xml.indexOf("</qualifyer>", startIndex);
    		if(stopIndex > -1)
    		{
    			changed = true;
	    		before = xml.substring(0, startIndex);
	    		after = xml.substring(stopIndex + 12);
	    		qualifyer = xml.substring(startIndex, stopIndex + 12);
	    		
	    		String newQualifyer = qualifyer;
	    		
	    		if(qualifyer.indexOf("entity='Content'") > 0)
	    			newQualifyer = qualifyer.replaceAll("<id>", "<id>oldContentId_");
	    		else if(qualifyer.indexOf("entity='SiteNode'") > 0)
	    			newQualifyer = qualifyer.replaceAll("<id>", "<id>oldSiteNodeId_");
	    			
	    		newXML.append(before);
	    		newXML.append(newQualifyer);
	    		xml = after;
    		}
    		else
    		{
    			throw new Exception("Error in xml - qualifyer tag broken in " + xml);
    		}
    		
    		startIndex = xml.indexOf("<qualifyer");
    	}

		newXML.append(after);
		
		if(changed)
			logger.info("newXML:" + newXML);
		
		return newXML.toString();
	}
	
   	/**
   	 * This method creates a new digital asset in the database and connects it to the contentVersion it belongs to.
   	 * The asset is send in as an InputStream which castor inserts automatically.
   	 */

   	public static DigitalAssetVO create(DigitalAssetVO digitalAssetVO, InputStream is, List<ContentVersion> contentVersions, Database db) throws SystemException, Exception
   	{
		DigitalAsset digitalAsset = null;
		
		digitalAsset = new DigitalAssetImpl();
		digitalAsset.setValueObject(digitalAssetVO.createCopy());
		if(CmsPropertyHandler.getEnableDiskAssets().equals("false"))
			digitalAsset.setAssetBlob(is);
		digitalAsset.setContentVersions(contentVersions);

		db.create(digitalAsset);
        
		//if(contentVersion.getDigitalAssets() == null)
		//    contentVersion.setDigitalAssets(new ArrayList());
		
		for(ContentVersion cv : contentVersions)
			cv.getDigitalAssets().add(digitalAsset);
						
        return digitalAsset.getValueObject();
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
