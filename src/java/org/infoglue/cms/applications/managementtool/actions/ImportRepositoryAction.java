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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import org.infoglue.cms.controllers.kernel.impl.simple.CopyRepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ImportController;
import org.infoglue.cms.controllers.kernel.impl.simple.OptimizedImportController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.FileUploadHelper;

import webwork.action.ActionContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * This class handles Exporting of a repository to an XML-file.
 * 
 * @author mattias
 */

public class ImportRepositoryAction extends InfoGlueAbstractAction
{
    public final static Logger logger = Logger.getLogger(ImportRepositoryAction.class.getName());

	private Integer repositoryId = null;
	
	private String onlyLatestVersions = "false";
	private String standardReplacement = null;
	private String replacements = null;
	private Boolean mergeExistingRepositories = false;

	private String processId = null;
	private String exportFormat = "2";

	private VisualFormatter visualFormatter = new VisualFormatter();


	/**
	 * This deletes a process info bean and related files etc.
	 * @return
	 * @throws Exception
	 */	

	public String doDeleteProcessBean() throws Exception
	{
		logUserActionInfo(getClass(), "doDeleteProcessBean");
		if(this.processId != null)
		{
			ProcessBean pb = ProcessBean.getProcessBean(ImportRepositoryAction.class.getName(), processId);
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
		logUserActionInfo(getClass(), "doShowProcesses");
		return "successShowProcesses";
	}

	public String doShowProcessesAsJSON() throws Exception
	{
		logUserActionInfo(getClass(), "doShowProcessesAsJSON");
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
		logUserActionInfo(getClass(), "doInput");
		standardReplacement = "stateYourOldSiteName=stateYourNewSiteName";

		return "input";
	}

	public String doInputCopy() throws Exception
	{
		logUserActionInfo(getClass(), "doInputCopy");
		RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(repositoryId);
		
		standardReplacement = repositoryVO.getName() + "=" + repositoryVO.getName() + " copy";
		
		return "inputCopy";
	}

	/**
	 * This handles the actual importing.
	 */
	
	protected String doExecute() throws SystemException 
	{
		logUserActionInfo(getClass(), "doExecute");
		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			//now restore the value and list what we get
			File file = FileUploadHelper.getUploadedFile(ActionContext.getContext().getMultiPartRequest());
			if(file == null || !file.exists())
			{
				String filePath = ActionContext.getContext().getMultiPartRequest().getParameter("filePath");
				logger.info("filePath:" + filePath);
				if(filePath != null)
				{
					file = new File(filePath);
				}
				else
					throw new SystemException("The file upload must have gone bad as no file reached the import utility.");
			}
			
			if(file.getName().endsWith(".zip"))
				return importV3(file);
			
			String encoding = "UTF-8";
			int version = 1;
			
			//Looking up what kind of dialect this is.
	        
			FileInputStream fisTemp = new FileInputStream(file);
            InputStreamReader readerTemp = new InputStreamReader(fisTemp, encoding);
            BufferedReader bufferedReaderTemp = new BufferedReader(readerTemp);
            String line = bufferedReaderTemp.readLine();
            int index = 0;
            while(line != null && index < 50)
            {
            	logger.info("line:" + line + '\n');
            	if(line.indexOf("contentTypeDefinitionId") > -1)
            	{
            		logger.info("This was a new export...");
            		version = 2;
            		break;
            	}
            	line = bufferedReaderTemp.readLine();
            	index++;
            }
            
            bufferedReaderTemp.close();
            readerTemp.close();
            fisTemp.close();

            Mapping map = new Mapping();
			if(version == 1)
			{
	            logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site.xml").toString());
				map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site.xml").toString());
			}
			else if(version == 2)
			{
			    logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site_2.5.xml").toString());
			    map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site_2.5.xml").toString());	
			}

			// All ODMG database access requires a transaction
			db.begin();
			
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
					
					if(replacements.indexOf("å") == -1 && 
					   replacements.indexOf("ä") == -1 && 
					   replacements.indexOf("ö") == -1 && 
					   replacements.indexOf("Å") == -1 && 
					   replacements.indexOf("Ä") == -1 && 
					   replacements.indexOf("Ö") == -1)
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

			ImportController.getController().importRepository(db, map, file, encoding, version, onlyLatestVersions, false, contentIdMap, siteNodeIdMap, allContentIds, replaceMap, mergeExistingRepositories);
			
			db.commit();
			db.close();
			
			Iterator allContentIdsIterator = allContentIds.iterator();
			while(allContentIdsIterator.hasNext())
			{
				Integer contentId = (Integer)allContentIdsIterator.next();
				try
				{
					db = CastorDatabaseService.getDatabase();
					db.begin();
	
					Content content = ContentController.getContentController().getContentWithId(contentId, db);
					ImportController.getController().updateContentVersions(content, contentIdMap, siteNodeIdMap, onlyLatestVersions, new HashMap());
					//updateContentVersions(content, contentIdMap, siteNodeIdMap);
	
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
			}
		} 
		catch ( Exception e) 
		{
			try
            {
                db.rollback();
                db.close();
            } 
			catch (Exception e1)
            {
                logger.error("An error occurred when importing a repository: " + e.getMessage(), e);
    			throw new SystemException("An error occurred when importing a repository: " + e.getMessage(), e);
            }
			
			logger.error("An error occurred when importing a repository: " + e.getMessage(), e);
			throw new SystemException("An error occurred when importing a repository: " + e.getMessage(), e);
		}
		
		return "success";
	}

	/**
	 * This handles the actual importing.
	 */
	
	protected String importV3(File file) throws Exception 
	{
		String exportId = "Import_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm");
		ProcessBean processBean = ProcessBean.createProcessBean(ImportRepositoryAction.class.getName(), exportId);
		
		OptimizedImportController.importRepositories(file, this.onlyLatestVersions, this.standardReplacement, this.replacements, processBean);
		
		return "successRedirectToProcesses";
	}

	
	/**
	 * This handles copying of a repository.
	 */
	
	public String doCopy() throws Exception 
	{
		logUserActionInfo(getClass(), "doCopy");
		String exportId = "Copy_Repository_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm");
		ProcessBean processBean = ProcessBean.createProcessBean(ImportRepositoryAction.class.getName(), exportId);
		
		CopyRepositoryController.importRepositories(getRequest().getParameterValues("repositoryId"), this.getInfoGluePrincipal(), this.onlyLatestVersions, this.standardReplacement, this.replacements, processBean);
		
		return "successRedirectToProcesses";

		/*
		File file = null;
		
		try 
		{
			Mapping map = new Mapping();
			String exportFormat = CmsPropertyHandler.getExportFormat();
			String requestExportFormat = ""+getRequest().getParameter("exportFormat");
			
			if(exportFormat.equalsIgnoreCase("3") || this.exportFormat.equals("3") || requestExportFormat.equals("3"))
			{
				String[] repositories = getRequest().getParameterValues("repositoryId");
				
				String exportId = "Copy_Import_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm");
				ProcessBean processBean = ProcessBean.createProcessBean(ImportRepositoryAction.class.getName(), exportId);
				
				OptimizedExportController.copy(repositories, -1, false, null, processBean, onlyLatestVersions, standardReplacement, replacements);

				return "successRedirectToProcesses";
			}

			logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site_2.5.xml").toString());
			map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site_2.5.xml").toString());
			
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

		    ExportRepositoryAction.getContentPropertiesAndAccessRights(ps, allContentProperties, allAccessRights, content, db);
		    ExportRepositoryAction.getSiteNodePropertiesAndAccessRights(ps, allSiteNodeProperties, allAccessRights, siteNode, db);
			
			siteNodes.add(siteNode);
			contents.add(content);
			names = names + "_" + repository.getName();
			allRepositoryProperties.putAll(OptimizedExportController.getRepositoryProperties(ps, repositoryId));
			
			List contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionList(db);
			List categories = CategoryController.getController().getAllActiveCategories();
			
			InfoGlueExportImpl infoGlueExportImpl = new InfoGlueExportImpl();
			
			VisualFormatter visualFormatter = new VisualFormatter();
			names = new VisualFormatter().replaceNonAscii(names, '_');

			String fileName = "RepositoryCopy_" + names + "_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd_HHmm") + ".xml";
			
			String filePath = CmsPropertyHandler.getDigitalAssetPath();
			String fileSystemName =  filePath + File.separator + fileName;
			
			String encoding = "UTF-8";
			file = new File(fileSystemName);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            OutputStreamWriter osw = new OutputStreamWriter(bos, encoding);
            Marshaller marshaller = new Marshaller(osw);
            marshaller.setMapping(map);
			marshaller.setEncoding(encoding);
			DigitalAssetBytesHandler.setMaxSize(-1);

			infoGlueExportImpl.getRootContent().addAll(contents);
			infoGlueExportImpl.getRootSiteNode().addAll(siteNodes);
			
			infoGlueExportImpl.setContentTypeDefinitions(new ArrayList());
			infoGlueExportImpl.setCategories(new ArrayList());
			
			infoGlueExportImpl.setRepositoryProperties(allRepositoryProperties);
			infoGlueExportImpl.setContentProperties(allContentProperties);
			infoGlueExportImpl.setSiteNodeProperties(allSiteNodeProperties);
			infoGlueExportImpl.setAccessRights(allAccessRights);
			
			marshaller.marshal(infoGlueExportImpl);
			
			osw.flush();
			osw.close();
			
			db.commit();
			db.close();
			
			db = CastorDatabaseService.getDatabase();
			db.begin();
			
			Map contentIdMap = new HashMap();
			Map siteNodeIdMap = new HashMap();
			List allContentIds = new ArrayList();

			Map<String,String> replaceMap = new HashMap<String,String>();
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
			
			ImportController.getController().importRepository(db, map, file, encoding, 2, onlyLatestVersions, true, contentIdMap, siteNodeIdMap, allContentIds, replaceMap, false);

			db.commit();
			db.close();

			Iterator allContentIdsIterator = allContentIds.iterator();
			while(allContentIdsIterator.hasNext())
			{
				Integer contentId = (Integer)allContentIdsIterator.next();
				try
				{
					db = CastorDatabaseService.getDatabase();
					db.begin();
	
					Content createdContent = ContentController.getContentController().getContentWithId(contentId, db);
					ImportController.getController().updateContentVersions(createdContent, contentIdMap, siteNodeIdMap, onlyLatestVersions, replaceMap);
	
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
			}
		} 
		catch ( Exception e) 
		{
			try
            {
                db.rollback();
                db.close();
            } 
			catch (Exception e1)
            {
                logger.error("An error occurred when importing a repository: " + e.getMessage(), e);
    			throw new SystemException("An error occurred when importing a repository: " + e.getMessage(), e);
            }
			
			logger.error("An error occurred when importing a repository: " + e.getMessage(), e);
			throw new SystemException("An error occurred when importing a repository: " + e.getMessage(), e);
		}
		finally
		{
			if(file != null)
				file.delete();
		}

		return "success";
		*/
	}

	public String getOnlyLatestVersions() 
	{
		return onlyLatestVersions;
	}

	public void setOnlyLatestVersions(String onlyLatestVersions) 
	{
		this.onlyLatestVersions = onlyLatestVersions;
	}

	public Integer getRepositoryId()
	{
		return repositoryId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public String getStandardReplacement()
	{
		return this.standardReplacement;
	}
	
	public void setReplacements(String replacements)
	{
		this.replacements = replacements;
	}
	
	public void setMergeExistingRepositories(Boolean mergeExistingRepositories) 
	{
		this.mergeExistingRepositories = mergeExistingRepositories;
	}
	public String getExportFormat()
	{
		return exportFormat;
	}

	public void setExportFormat(String exportFormat)
	{
		this.exportFormat = exportFormat;
	}

	public void setProcessId(String processId) 
	{
		this.processId = processId;
	}

	public List<ProcessBean> getProcessBeans()
	{
		return ProcessBean.getProcessBeans(ImportRepositoryAction.class.getName());
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
