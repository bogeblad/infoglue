package org.infoglue.cms.controllers.kernel.impl.simple;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.TransactionNotInProgressException;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentCategory;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Category;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.entities.management.ContentTypeDefinition;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.impl.simple.CategoryImpl;
import org.infoglue.cms.entities.management.impl.simple.ContentTypeDefinitionImpl;
import org.infoglue.cms.entities.management.impl.simple.InfoGlueExportImpl;
import org.infoglue.cms.entities.management.impl.simple.LanguageImpl;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.handlers.DigitalAssetBytesHandler;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.util.Timer;

public class ExportImportController extends BaseController
{
    private final static Logger logger = Logger.getLogger(ExportImportController.class.getName());

	public static ExportImportController getController()
	{
		return new ExportImportController();
	}
	
	public String exportContent(List contentIdList, String path, String fileNamePrefix, boolean includeContentTypes, boolean includeCategories) throws Exception
	{
		VisualFormatter vf = new VisualFormatter();
		fileNamePrefix = vf.replaceNonAscii(fileNamePrefix, '_');
		
		String fileName = null;
			
		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			Mapping map = new Mapping();
			String exportFormat = CmsPropertyHandler.getExportFormat();
			
			map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_content_2.5.xml").toString());
			
			db.begin();
	
			List contents = new ArrayList();
			
			Iterator i = contentIdList.iterator();
			while(i.hasNext())
			{
				Integer contentId = (Integer)i.next();
				Content content   = ContentController.getContentController().getContentWithId(contentId, db);
				contents.add(content);
			}
			
			List contentTypeDefinitions = new ArrayList();
			if(includeContentTypes)
				contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionList(db);
			
			List categories = new ArrayList();
			if(includeCategories)
				categories = CategoryController.getController().getAllActiveCategories();
			
			InfoGlueExportImpl infoGlueExportImpl = new InfoGlueExportImpl();
			
			String filePath = CmsPropertyHandler.getDigitalAssetPath();
			String tempFileName = "Export_tmp_" + Thread.currentThread().getId() + "_" + fileNamePrefix + ".xml";
			String tempFileSystemName = filePath + File.separator + tempFileName;
						
			String encoding = "UTF-8";
			File tempFile = new File(tempFileSystemName);
	        FileOutputStream fos = new FileOutputStream(tempFile);
	        OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
	        Marshaller marshaller = new Marshaller(osw);
	        marshaller.setMapping(map);
			marshaller.setEncoding(encoding);
			DigitalAssetBytesHandler.setMaxSize(-1);
	
			infoGlueExportImpl.getRootContent().addAll(contents);
			
			infoGlueExportImpl.setContentTypeDefinitions(contentTypeDefinitions);
			infoGlueExportImpl.setCategories(categories);
			
			marshaller.marshal(infoGlueExportImpl);
			
			osw.flush();
			osw.close();
			
			fileName = fileNamePrefix + "_" + tempFile.length() + ".xml";
			String fileSystemName = filePath + File.separator + fileName;
			File file = new File(fileSystemName);
			tempFile.renameTo(file);
			
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
		
		return fileName;
	}

	public void exportContents(Integer contentId, Integer newParentContentId, Integer assetMaxSize, String onlyLatestVersions) throws SystemException, Bug, Exception 
	{
		Timer t = new Timer();
		logger.info("onlyLatestVersions:" + onlyLatestVersions);
		File file = exportContents(contentId, assetMaxSize, false);
		t.printElapsedTime("Exporting file of " + (file != null ? file.length() / 1000 + " KB" : " error size ") + " took:");
		if(file != null)
			importContent(file, newParentContentId, onlyLatestVersions);
		t.printElapsedTime("Importing file of " + (file != null ? file.length() / 1000 + " KB" : " error size ") + " took:");
	}
	
	private File exportContents(Integer contentId, Integer assetMaxSize, boolean includeSystemTypes) throws SystemException, Bug, TransactionNotInProgressException, PersistenceException 
	{
		File file = null;
		
		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			Mapping map = new Mapping();
			String exportFormat = CmsPropertyHandler.getExportFormat();
			
			logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_content_2.5.xml").toString());
			map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_content_2.5.xml").toString());
			
			// All ODMG database access requires a transaction
			db.begin();
		
			//List siteNodes = new ArrayList();
			List contents = new ArrayList();
			
			String[] contentIds = new String[]{"" + contentId};
			for(int i=0; i<contentIds.length; i++)
			{
				Integer currentContentId = new Integer(contentIds[i]);
				Content content 		= ContentController.getContentController().getContentWithId(currentContentId, db);
				contents.add(content);
			}
			
			List contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionList(db);
			List categories = CategoryController.getController().getAllActiveCategories();
			List languages = LanguageController.getController().getLanguageList(db);
			
			InfoGlueExportImpl infoGlueExportImpl = new InfoGlueExportImpl();
			
			VisualFormatter visualFormatter = new VisualFormatter();
			String fileName = "Export_contents_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd") + ".xml";
			String filePath = CmsPropertyHandler.getDigitalAssetPath();
			String fileSystemName =  filePath + File.separator + fileName;
									
			String encoding = "UTF-8";
			file = new File(fileSystemName);
		    FileOutputStream fos = new FileOutputStream(file);
		    OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
		    Marshaller marshaller = new Marshaller(osw);
		    marshaller.setMapping(map);
			marshaller.setEncoding(encoding);
			marshaller.setValidation(false);
			DigitalAssetBytesHandler.setMaxSize(assetMaxSize);
		
			infoGlueExportImpl.getRootContent().addAll(contents);
		
			if(includeSystemTypes)
			{
				infoGlueExportImpl.setLanguages(languages);
				infoGlueExportImpl.setContentTypeDefinitions(contentTypeDefinitions);
				infoGlueExportImpl.setCategories(categories);
			}
			
			marshaller.marshal(infoGlueExportImpl);
			
			osw.flush();
			osw.close();
			
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
		
		return file;
	}
	
	
	public void importContent(File contentFile, Integer parentContentId, String onlyLatestVersions) throws SystemException, Exception
	{
		Database db = CastorDatabaseService.getDatabase();
		
		try 
		{
			//now restore the value and list what we get
			if(contentFile == null || !contentFile.exists())
				throw new SystemException("The file upload must have gone bad as no file reached the import utility.");
			
			String encoding = "UTF-8";
			int version = 2;
			
	        Mapping map = new Mapping();
		    logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_content_2.5.xml").toString());
			map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_content_2.5.xml").toString());	
	
			// All ODMG database access requires a transaction
			db.begin();
	
			Content parentContent = ContentController.getContentController().getContentWithId(parentContentId, db);
			logger.info("parentContent:" + parentContent.getName());
			Integer repositoryId = parentContent.getRepositoryId();
			
			//String encoding = "ISO-8859-1";
	        FileInputStream fis = new FileInputStream(contentFile);
	        InputStreamReader reader = new InputStreamReader(fis, encoding);
			//Reader reader = new FileReader(file);
	
			Unmarshaller unmarshaller = new Unmarshaller(map);
			unmarshaller.setWhitespacePreserve(true);
			InfoGlueExportImpl infoGlueExportImplRead = (InfoGlueExportImpl)unmarshaller.unmarshal(reader);
			Collection contentTypeDefinitions = infoGlueExportImplRead.getContentTypeDefinitions();
			logger.info("Found " + contentTypeDefinitions.size() + " content type definitions");
			Collection categories = infoGlueExportImplRead.getCategories();
			logger.info("Found " + categories.size() + " categories");
	
			Map categoryIdMap = new HashMap();
			Map contentTypeIdMap = new HashMap();
	
			importCategories(categories, null, categoryIdMap, db);
			
			updateContentTypeDefinitions(contentTypeDefinitions, categoryIdMap);
			
			List readContents = infoGlueExportImplRead.getRootContent();
	
			Map contentIdMap = new HashMap();
			Map siteNodeIdMap = new HashMap();
			
			List allContents = new ArrayList();
			
			Iterator readContentsIterator = readContents.iterator();
			while(readContentsIterator.hasNext())
			{
				Content readContent = (Content)readContentsIterator.next();
				
				readContent.setRepository((RepositoryImpl)parentContent.getRepository());
				readContent.setParentContent((ContentImpl)parentContent);
				
				createContents(readContent, contentIdMap, contentTypeIdMap, allContents, Collections.unmodifiableCollection(contentTypeDefinitions), categoryIdMap, version, db, onlyLatestVersions);
				parentContent.getChildren().add(readContent);
			}
					
			List allContentIds = new ArrayList();
			Iterator allContentsIterator = allContents.iterator();
			while(allContentsIterator.hasNext())
			{
				Content content = (Content)allContentsIterator.next();
				allContentIds.add(content.getContentId());
			}
	
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
					updateContentVersions(content, contentIdMap, siteNodeIdMap, onlyLatestVersions);
	
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
			
			reader.close();			
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
	
	private void importCategories(Collection categories, Category parentCategory, Map categoryIdMap, Database db) throws SystemException
	{
		logger.info("We want to create a list of categories if not existing under the parent category " + parentCategory);
		Iterator categoryIterator = categories.iterator();
		while(categoryIterator.hasNext())
		{
			CategoryVO categoryVO = (CategoryVO)categoryIterator.next();
			Category newParentCategory = null;
			
			List existingCategories = null;
			if(parentCategory != null)
				existingCategories = CategoryController.getController().findByParent(parentCategory.getCategoryId(), db);
			else
				existingCategories = CategoryController.getController().findRootCategories(db);
				
			Iterator existingCategoriesIterator = existingCategories.iterator();
			while(existingCategoriesIterator.hasNext())
			{
				Category existingCategory = (Category)existingCategoriesIterator.next();
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
				logger.info("No existing category - we create it.");
				Integer oldId = categoryVO.getId();
				categoryVO.setCategoryId(null);
				if(parentCategory != null)	
					categoryVO.setParentId(parentCategory.getCategoryId());
				else
					categoryVO.setParentId(null);
					
				Category newCategory = CategoryController.getController().save(categoryVO, db);
				categoryIdMap.put(oldId, newCategory.getCategoryId());
				newParentCategory = newCategory;
			}
			else
			{
				categoryIdMap.put(categoryVO.getId(), newParentCategory.getCategoryId());
			}
				
			importCategories(categoryVO.getChildren(), newParentCategory, categoryIdMap, db);
		}
	}
	
	/**
	 * This method copies a content and all it relations.
	 * 
	 * @param siteNode
	 * @param db
	 * @throws Exception
	 */
	
	private List createContents(Content content, Map idMap, Map contentTypeDefinitionIdMap, List allContents, Collection contentTypeDefinitions, Map categoryIdMap, int version, Database db, String onlyLatestVersions) throws Exception
	{
		ContentTypeDefinition contentTypeDefinition = null;
		
	    Integer originalContentId = content.getContentId();
	    Integer contentTypeDefinitionId = ((ContentImpl)content).getContentTypeDefinitionId();
	    
		if(contentTypeDefinitionId != null)
		{
			if(contentTypeDefinitionIdMap.containsKey(contentTypeDefinitionId))
				contentTypeDefinitionId = (Integer)contentTypeDefinitionIdMap.get(contentTypeDefinitionId);
			
			if(contentTypeDefinitions == null || contentTypeDefinitions.size() == 0)
			{
				contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithId(contentTypeDefinitionId, db);
				content.setContentTypeDefinition((ContentTypeDefinitionImpl)contentTypeDefinition);
			}				
			else
			{
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
	
		    	}
			    else
			    	logger.error("The content " + content.getName() + " had a content type not found amongst the listed ones:" + contentTypeDefinitionId);
			}
			
		}
	    else
	    	logger.error("The content " + content.getName() + " had no content type at all");
	    
	    if(content.getContentTypeDefinition() == null)
	    	logger.error("No content type definition for content:" + content.getId());
	    	
	    logger.info("Creating content:" + content.getName());

	    db.create(content);
		
		allContents.add(content);
		
		Integer newContentId = content.getContentId();
		idMap.put(originalContentId.toString(), newContentId.toString());
		
		Collection contentVersions = content.getContentVersions();
	    
		logger.info("onlyLatestVersions in createContents...:" + onlyLatestVersions);
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
					selectedContentVersions.add(contentVersion);
			}	
			
			contentVersions = selectedContentVersions;
		}
		
		if(logger.isInfoEnabled())
		{
			logger.info("new contentVersions:" + contentVersions.size());
			Iterator contentVersionsIteratorDebug = contentVersions.iterator();
			while(contentVersionsIteratorDebug.hasNext())
			{
				ContentVersion contentVersion = (ContentVersion)contentVersionsIteratorDebug.next();
				logger.info("debug contentVersion:" + contentVersion.getId());
			}
		}
		
		Collections.sort((List)contentVersions, new ReflectionComparator("id"));

		if(logger.isInfoEnabled())
		{
			logger.info("new contentVersions:" + contentVersions.size());
			Iterator contentVersionsIteratorDebug = contentVersions.iterator();
			while(contentVersionsIteratorDebug.hasNext())
			{
				ContentVersion contentVersion = (ContentVersion)contentVersionsIteratorDebug.next();
				logger.info("debug contentVersion:" + contentVersion.getId());
			}
		}

		//Collection contentVersions = content.getContentVersions();
		Iterator contentVersionsIterator = contentVersions.iterator();
		while(contentVersionsIterator.hasNext())
		{
			ContentVersion contentVersion = (ContentVersion)contentVersionsIterator.next();
			Language language = null;
			if(contentVersion.getLanguage() != null)
				language = LanguageController.getController().getLanguageWithCode(contentVersion.getLanguage().getLanguageCode(), db);
			else
				language = LanguageController.getController().getLanguageWithId(contentVersion.getLanguageId(), db);
				
			logger.info("Creating contentVersion for language:" + language.getLanguageCode() + " on content " + content.getName());

			contentVersion.setOwningContent((ContentImpl)content);
			contentVersion.setLanguage((LanguageImpl)language);
			
			Collection digitalAssets = contentVersion.getDigitalAssets();
			if(digitalAssets != null)
			{
				List initialDigitalAssets = new ArrayList();
					
				Iterator digitalAssetsIterator = digitalAssets.iterator();
				while(digitalAssetsIterator.hasNext())
				{
					DigitalAsset digitalAsset = (DigitalAsset)digitalAssetsIterator.next();
					
					List initialContentVersions = new ArrayList();
					initialContentVersions.add(contentVersion);
					digitalAsset.setContentVersions(initialContentVersions);
	
					db.create(digitalAsset);
					
					initialDigitalAssets.add(digitalAsset);
				}
				
				contentVersion.setDigitalAssets(initialDigitalAssets);
			}

			Collection contentCategories = contentVersion.getContentCategories();
			logger.info("contentCategories:" + contentCategories.size());
			
			db.create(contentVersion);
			
			if(contentCategories != null)
			{
				List initialContentCategories = new ArrayList();
					
				Iterator contentCategoriesIterator = contentCategories.iterator();
				while(contentCategoriesIterator.hasNext())
				{
					ContentCategory contentCategory = (ContentCategory)contentCategoriesIterator.next();
					logger.info("contentCategory:" + contentCategory);
					contentCategory.setContentVersion((ContentVersionImpl)contentVersion);
					
					Integer oldCategoryId = contentCategory.getCategoryId();
					logger.info("oldCategoryId:" + oldCategoryId);
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
				
				contentVersion.setContentCategories(initialContentCategories);
			}

		}		
		
		Collection childContents = content.getChildren();
		if(childContents != null)
		{
			Iterator childContentsIterator = childContents.iterator();
			while(childContentsIterator.hasNext())
			{
				Content childContent = (Content)childContentsIterator.next();
				childContent.setRepository(content.getRepository());
				childContent.setParentContent((ContentImpl)content);
				createContents(childContent, idMap, contentTypeDefinitionIdMap, allContents, contentTypeDefinitions, categoryIdMap, version, db, onlyLatestVersions);
			}
		}
		
		return allContents;
	}


	/**
	 * This method updates all the bindings in content-versions to reflect the move. 
	 */
	private void updateContentVersions(Content content, Map contentIdMap, Map siteNodeIdMap, String onlyLatestVersions) throws Exception
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
					selectedContentVersions.add(contentVersion);
			}	
			
			contentVersions = selectedContentVersions;
		}

        
		if(logger.isInfoEnabled())
		{
			logger.info("new contentVersions:" + contentVersions.size());
			Iterator contentVersionsIteratorDebug = contentVersions.iterator();
			while(contentVersionsIteratorDebug.hasNext())
			{
				ContentVersion contentVersion = (ContentVersion)contentVersionsIteratorDebug.next();
				logger.info("debug contentVersion:" + contentVersion.getId());
			}
		}
		
		List<ContentVersion> sortedVersions = new ArrayList<ContentVersion>(); 
		sortedVersions.addAll(contentVersions);
		
		Collections.sort(sortedVersions, new ReflectionComparator("id"));

		if(true)
		{
			logger.info("sortedVersions:" + sortedVersions.size());
			Iterator contentVersionsIteratorDebug = sortedVersions.iterator();
			while(contentVersionsIteratorDebug.hasNext())
			{
				ContentVersion contentVersion = (ContentVersion)contentVersionsIteratorDebug.next();
				logger.info("debug sortedVersion 2:" + contentVersion.getId() + ":" + contentVersion.getOwningContent());
			}
		}
		
		contentVersions.clear();
		contentVersions.addAll(sortedVersions);

		if(logger.isInfoEnabled())
		{
			logger.info("new contentVersions:" + contentVersions.size());
			Iterator contentVersionsIteratorDebug = contentVersions.iterator();
			while(contentVersionsIteratorDebug.hasNext())
			{
				ContentVersion contentVersion = (ContentVersion)contentVersionsIteratorDebug.next();
				logger.info("debug contentVersion:" + contentVersion.getId());
			}
		}

        Iterator contentVersionIterator = contentVersions.iterator();
        while(contentVersionIterator.hasNext())
        {
            ContentVersion contentVersion = (ContentVersion)contentVersionIterator.next();
			logger.info("contentVersion:" + contentVersion.getId() + ":" + contentVersion.getContentVersionId());

			String contentVersionValue = contentVersion.getVersionValue();

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
	
	public BaseEntityVO getNewVO()
	{
		return null;
	}
	
}
