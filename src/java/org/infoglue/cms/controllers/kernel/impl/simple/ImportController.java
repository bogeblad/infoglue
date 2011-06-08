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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentCategory;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
import org.infoglue.cms.entities.content.impl.simple.ContentVersionImpl;
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
import org.infoglue.cms.exception.SystemException;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
* This class handles Importing of a repository - not finished by a long shot.
* 
* @author mattias
*/

public class ImportController extends BaseController
{
    public final static Logger logger = Logger.getLogger(ImportController.class.getName());

	/**
	 * Factory method to get object
	 */
	
	public static ImportController getController()
	{
		return new ImportController();
	}

	public void importRepository(Database db, Mapping map, File file, String encoding, int version, String onlyLatestVersions, boolean isCopyAction, Map contentIdMap, Map siteNodeIdMap, List allContentIds, Map replaceMap, Boolean mergeExistingRepositories) throws Exception
	{
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        InputStreamReader reader = new InputStreamReader(bis, encoding);

		Unmarshaller unmarshaller = new Unmarshaller(map);
		unmarshaller.setWhitespacePreserve(true);
		unmarshaller.setValidation(false);
		InfoGlueExportImpl infoGlueExportImplRead = (InfoGlueExportImpl)unmarshaller.unmarshal(reader);

		//if(mergeExistingRepositories && !isCopyAction)
		//	mergeCopyRepository(db, infoGlueExportImplRead, version, onlyLatestVersions, isCopyAction, contentIdMap, siteNodeIdMap, allContentIds, replaceMap, mergeExistingRepositories);
		//else
			copyRepository(db, infoGlueExportImplRead, version, onlyLatestVersions, isCopyAction, contentIdMap, siteNodeIdMap, allContentIds, replaceMap);

		reader.close();
	}
	
	public void mergeCopyRepository(Database db, InfoGlueExportImpl infoGlueExportImplRead, int version, String onlyLatestVersions, boolean isCopyAction, Map contentIdMap, Map siteNodeIdMap, List allContentIds, Map replaceMap) throws Exception
	{
		Collection contentTypeDefinitions = infoGlueExportImplRead.getContentTypeDefinitions();
		logger.info("Found " + contentTypeDefinitions.size() + " content type definitions");
		Collection categories = infoGlueExportImplRead.getCategories();
		logger.info("Found " + categories.size() + " categories");

		Map categoryIdMap = new HashMap();
		Map contentTypeIdMap = new HashMap();

		if(!isCopyAction)
		{
			importCategories(categories, null, categoryIdMap, db);
			updateContentTypeDefinitions(contentTypeDefinitions, categoryIdMap);
		}
		
		List readSiteNodes = infoGlueExportImplRead.getRootSiteNode();
		List readContents = infoGlueExportImplRead.getRootContent();
		
		Map repositoryIdMap = new HashMap();
		Map siteNodeVersionIdMap = new HashMap();
		
		List allContents = new ArrayList();
		List allSiteNodes = new ArrayList();
		
		Map<String, AvailableServiceBinding> readAvailableServiceBindings = new HashMap<String, AvailableServiceBinding>();

		Map repositoryContentMap = new HashMap();
		Iterator readContentsIteratorDebug = readContents.iterator();
		while(readContentsIteratorDebug.hasNext())
		{
			Content readContentCandidate = (Content)readContentsIteratorDebug.next();
			repositoryContentMap.put("" + readContentCandidate.getRepositoryId(), readContentCandidate);
		}

		Iterator readSiteNodesIterator = readSiteNodes.iterator();
		while(readSiteNodesIterator.hasNext())
		{
			SiteNode readSiteNode = (SiteNode)readSiteNodesIterator.next();

			Repository repositoryRead = readSiteNode.getRepository();
			logger.info(repositoryRead.getName());
			
			repositoryRead.setName(substituteStrings(repositoryRead.getName(), replaceMap));
			repositoryRead.setDescription(substituteStrings(repositoryRead.getDescription(), replaceMap));
			repositoryRead.setDnsName(substituteStrings(repositoryRead.getDnsName(), replaceMap));
			
			Content readContent = null;

			readContent = (Content)repositoryContentMap.get("" + repositoryRead.getId());
			//logger.info("readContent:" + readContent.getName() + ":" + readContent.getId());
			
			readContent.setRepository((RepositoryImpl)repositoryRead);

			Integer repositoryIdBefore = repositoryRead.getId();
			db.create(repositoryRead);
			Integer repositoryIdAfter = repositoryRead.getId();
			repositoryIdMap.put("" + repositoryIdBefore, "" + repositoryIdAfter);

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
			
			readSiteNode.setRepository((RepositoryImpl)repositoryRead);
			
			logger.info("***************************************\nreadContent:" + readContent.getName());
			createContents(readContent, contentIdMap, contentTypeIdMap, allContents, Collections.unmodifiableCollection(contentTypeDefinitions), categoryIdMap, version, db, onlyLatestVersions, isCopyAction, replaceMap);
			createStructure(readSiteNode, contentIdMap, siteNodeIdMap, siteNodeVersionIdMap, readAvailableServiceBindings, allSiteNodes, db, onlyLatestVersions, replaceMap);
		}
					
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

		Map<String,String> repositoryProperties = infoGlueExportImplRead.getRepositoryProperties();
		Iterator<String> repositoryPropertiesIterator = repositoryProperties.keySet().iterator();
		while(repositoryPropertiesIterator.hasNext())
		{
			String key = repositoryPropertiesIterator.next();
			String value = repositoryProperties.get(key);
			String[] splittedString = key.split("_");
			if(splittedString.length == 3)
			{
				String oldRepId = splittedString[1];
				key = key.replaceAll(oldRepId, (String)repositoryIdMap.get(oldRepId));
				
				if(value != null && !value.equals("null"))
				{
					if(key.indexOf("_WYSIWYGConfig") > -1 || key.indexOf("_StylesXML") > -1 || key.indexOf("_extraProperties") > -1)
						ps.setData(key, value.getBytes("utf-8"));
					else
						ps.setString(key, value);
				}
			}
		}

		Map<String,String> contentProperties = infoGlueExportImplRead.getContentProperties();
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

		Map<String,String> siteNodeProperties = infoGlueExportImplRead.getSiteNodeProperties();
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

		Collection<AccessRight> accessRights = infoGlueExportImplRead.getAccessRights();
		Iterator<AccessRight> accessRightsIterator = accessRights.iterator();
		while(accessRightsIterator.hasNext())
		{
			AccessRight accessRight = accessRightsIterator.next();

			InterceptionPoint interceptionPoint = InterceptionPointController.getController().getInterceptionPointWithName(accessRight.getInterceptionPointName(), db);
			accessRight.setInterceptionPoint(interceptionPoint);
			if(interceptionPoint.getName().indexOf("Content") > -1)
				accessRight.setParameters((String)contentIdMap.get(accessRight.getParameters()));
			else if(interceptionPoint.getName().indexOf("SiteNodeVersion") > -1)
				accessRight.setParameters((String)siteNodeVersionIdMap.get(accessRight.getParameters()));
			else if(interceptionPoint.getName().indexOf("SiteNode") > -1)
				accessRight.setParameters((String)siteNodeIdMap.get(accessRight.getParameters()));
			else if(interceptionPoint.getName().indexOf("Repository") > -1)
				accessRight.setParameters((String)repositoryIdMap.get(accessRight.getParameters()));

			db.create(accessRight);

			Iterator accessRightRoleIterator = accessRight.getRoles().iterator();
			while(accessRightRoleIterator.hasNext())
			{
				AccessRightRole accessRightRole = (AccessRightRole)accessRightRoleIterator.next();
				accessRightRole.setAccessRight(accessRight);
				db.create(accessRightRole);
			}

			Iterator accessRightGroupIterator = accessRight.getGroups().iterator();
			while(accessRightGroupIterator.hasNext())
			{
				AccessRightGroup accessRightGroup = (AccessRightGroup)accessRightGroupIterator.next();
				accessRightGroup.setAccessRight(accessRight);
				db.create(accessRightGroup);
			}
		}
	}
	
	public void copyRepository(Database db, InfoGlueExportImpl infoGlueExportImplRead, int version, String onlyLatestVersions, boolean isCopyAction, Map contentIdMap, Map siteNodeIdMap, List allContentIds, Map replaceMap) throws Exception
	{
		Collection contentTypeDefinitions = infoGlueExportImplRead.getContentTypeDefinitions();
		logger.info("Found " + contentTypeDefinitions.size() + " content type definitions");
		Collection categories = infoGlueExportImplRead.getCategories();
		logger.info("Found " + categories.size() + " categories");

		Map categoryIdMap = new HashMap();
		Map contentTypeIdMap = new HashMap();

		if(!isCopyAction)
		{
			importCategories(categories, null, categoryIdMap, db);
			updateContentTypeDefinitions(contentTypeDefinitions, categoryIdMap);
		}
		
		List readSiteNodes = infoGlueExportImplRead.getRootSiteNode();
		//SiteNode readSiteNode = infoGlueExportImplRead.getRootSiteNode();
		//logger.info(readSiteNode.getName());
		List readContents = infoGlueExportImplRead.getRootContent();
		//Content readContent = infoGlueExportImplRead.getRootContent();
		//logger.info(readContent.getName());
		
		Map repositoryIdMap = new HashMap();
		//Map contentIdMap = new HashMap();
		Map siteNodeVersionIdMap = new HashMap();
		//Map siteNodeIdMap = new HashMap();
		
		List allContents = new ArrayList();
		List allSiteNodes = new ArrayList();
		
		Map<String, AvailableServiceBinding> readAvailableServiceBindings = new HashMap<String, AvailableServiceBinding>();

		Map repositoryContentMap = new HashMap();
		Iterator readContentsIteratorDebug = readContents.iterator();
		while(readContentsIteratorDebug.hasNext())
		{
			Content readContentCandidate = (Content)readContentsIteratorDebug.next();
			repositoryContentMap.put("" + readContentCandidate.getRepositoryId(), readContentCandidate);
			//logger.info("readContentCandidate debug...:" + readContentCandidate.getName() + ":" + readContentCandidate.getId() + ":" + readContentCandidate.getRepositoryId());
		}

		Iterator readSiteNodesIterator = readSiteNodes.iterator();
		while(readSiteNodesIterator.hasNext())
		{
			SiteNode readSiteNode = (SiteNode)readSiteNodesIterator.next();

			Repository repositoryRead = readSiteNode.getRepository();
			logger.info(repositoryRead.getName());
			
			repositoryRead.setName(substituteStrings(repositoryRead.getName(), replaceMap));
			repositoryRead.setDescription(substituteStrings(repositoryRead.getDescription(), replaceMap));
			repositoryRead.setDnsName(substituteStrings(repositoryRead.getDnsName(), replaceMap));
			
			Content readContent = null;

			readContent = (Content)repositoryContentMap.get("" + repositoryRead.getId());
			//logger.info("readContent:" + readContent.getName() + ":" + readContent.getId());
			
			readContent.setRepository((RepositoryImpl)repositoryRead);

			Integer repositoryIdBefore = repositoryRead.getId();
			db.create(repositoryRead);
			Integer repositoryIdAfter = repositoryRead.getId();
			repositoryIdMap.put("" + repositoryIdBefore, "" + repositoryIdAfter);

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
			
			readSiteNode.setRepository((RepositoryImpl)repositoryRead);
			
			logger.info("***************************************\nreadContent:" + readContent.getName());
			createContents(readContent, contentIdMap, contentTypeIdMap, allContents, Collections.unmodifiableCollection(contentTypeDefinitions), categoryIdMap, version, db, onlyLatestVersions, isCopyAction, replaceMap);
			createStructure(readSiteNode, contentIdMap, siteNodeIdMap, siteNodeVersionIdMap, readAvailableServiceBindings, allSiteNodes, db, onlyLatestVersions, replaceMap);
		}
					
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

		Map<String,String> repositoryProperties = infoGlueExportImplRead.getRepositoryProperties();
		Iterator<String> repositoryPropertiesIterator = repositoryProperties.keySet().iterator();
		while(repositoryPropertiesIterator.hasNext())
		{
			String key = repositoryPropertiesIterator.next();
			String value = repositoryProperties.get(key);
			String[] splittedString = key.split("_");
			if(splittedString.length == 3)
			{
				String oldRepId = splittedString[1];
				String replacement = (String)repositoryIdMap.get(oldRepId);
				if(replacement != null)
					key = key.replaceAll(oldRepId, replacement);
				
				if(value != null && !value.equals("null"))
				{
					if(key.indexOf("_WYSIWYGConfig") > -1 || key.indexOf("_StylesXML") > -1 || key.indexOf("_extraProperties") > -1)
						ps.setData(key, value.getBytes("utf-8"));
					else
						ps.setString(key, value);
				}
			}
		}

		Map<String,String> contentProperties = infoGlueExportImplRead.getContentProperties();
		Iterator<String> contentPropertiesIterator = contentProperties.keySet().iterator();
		while(contentPropertiesIterator.hasNext())
		{
			String key = contentPropertiesIterator.next();
			String value = contentProperties.get(key);
			String[] splittedString = key.split("_");
			if(splittedString.length == 3)
			{
				String oldContentId = splittedString[1];
				String replacement = (String)contentIdMap.get(oldContentId);
				if(replacement != null)
					key = key.replaceAll(oldContentId, replacement);
				if(value != null && !value.equals("null"))
					ps.setString(key, value);
			}
		}

		Map<String,String> siteNodeProperties = infoGlueExportImplRead.getSiteNodeProperties();
		Iterator<String> siteNodePropertiesIterator = siteNodeProperties.keySet().iterator();
		while(siteNodePropertiesIterator.hasNext())
		{
			String key = siteNodePropertiesIterator.next();
			String value = siteNodeProperties.get(key);
			String[] splittedString = key.split("_");
			if(splittedString.length == 3)
			{
				String oldSiteNodeId = splittedString[1];
				String replacement = (String)siteNodeIdMap.get(oldSiteNodeId);
				if(replacement != null)
					key = key.replaceAll(oldSiteNodeId, replacement);
				if(value != null && !value.equals("null"))
					ps.setString(key, value);
			}
		}

		Collection<AccessRight> accessRights = infoGlueExportImplRead.getAccessRights();
		Iterator<AccessRight> accessRightsIterator = accessRights.iterator();
		while(accessRightsIterator.hasNext())
		{
			AccessRight accessRight = accessRightsIterator.next();

			InterceptionPoint interceptionPoint = InterceptionPointController.getController().getInterceptionPointWithName(accessRight.getInterceptionPointName(), db);
			if(interceptionPoint != null)
			{
				accessRight.setInterceptionPoint(interceptionPoint);
				if(interceptionPoint.getName().indexOf("Content") > -1)
					accessRight.setParameters((String)contentIdMap.get(accessRight.getParameters()));
				else if(interceptionPoint.getName().indexOf("SiteNodeVersion") > -1)
					accessRight.setParameters((String)siteNodeVersionIdMap.get(accessRight.getParameters()));
				else if(interceptionPoint.getName().indexOf("SiteNode") > -1)
					accessRight.setParameters((String)siteNodeIdMap.get(accessRight.getParameters()));
				else if(interceptionPoint.getName().indexOf("Repository") > -1)
					accessRight.setParameters((String)repositoryIdMap.get(accessRight.getParameters()));
	
				db.create(accessRight);
	
				Iterator accessRightRoleIterator = accessRight.getRoles().iterator();
				while(accessRightRoleIterator.hasNext())
				{
					AccessRightRole accessRightRole = (AccessRightRole)accessRightRoleIterator.next();
					accessRightRole.setAccessRight(accessRight);
					db.create(accessRightRole);
				}
	
				Iterator accessRightGroupIterator = accessRight.getGroups().iterator();
				while(accessRightGroupIterator.hasNext())
				{
					AccessRightGroup accessRightGroup = (AccessRightGroup)accessRightGroupIterator.next();
					accessRightGroup.setAccessRight(accessRight);
					db.create(accessRightGroup);
				}
			}
		}
	}

	private String substituteStrings(String originalValue, Map<String,String> replaceMap)
	{
		String newValue = originalValue;
		Iterator<String> replaceMapIterator = replaceMap.keySet().iterator();
		while(replaceMapIterator.hasNext())
		{
			String key = replaceMapIterator.next();
			String value = replaceMap.get(key);
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
				logger.info("No existing category - we create it: " + categoryVO);
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
	 * This method copies a sitenode and all it relations.
	 * 
	 * @param siteNode
	 * @param db
	 * @throws Exception
	 */
	private void createStructure(SiteNode siteNode, Map contentIdMap, Map siteNodeIdMap, Map siteNodeVersionIdMap, Map readAvailableServiceBindings, List allSiteNodes, Database db, String onlyLatestVersions, Map<String,String> replaceMap) throws Exception
	{
		logger.info("siteNode:" + siteNode.getName());

		Integer originalSiteNodeId = siteNode.getSiteNodeId();

		logger.info("originalSiteNodeId:" + originalSiteNodeId);

		SiteNodeTypeDefinition originalSiteNodeTypeDefinition = siteNode.getSiteNodeTypeDefinition();
		SiteNodeTypeDefinition siteNodeTypeDefinition = null;
		if(originalSiteNodeTypeDefinition != null)
		{
			logger.info("originalSiteNodeTypeDefinition:" + originalSiteNodeTypeDefinition);
			siteNodeTypeDefinition = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionWithName(siteNode.getSiteNodeTypeDefinition().getName(), db, false);
			logger.info("siteNodeTypeDefinition:" + siteNodeTypeDefinition);
			if(siteNodeTypeDefinition == null)
			{
			    db.create(originalSiteNodeTypeDefinition);
			    siteNodeTypeDefinition = originalSiteNodeTypeDefinition;
			}
			
			siteNode.setSiteNodeTypeDefinition((SiteNodeTypeDefinitionImpl)siteNodeTypeDefinition);
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
		
		Collection childSiteNodes = siteNode.getChildSiteNodes();
		if(childSiteNodes != null)
		{
			Iterator childSiteNodesIterator = childSiteNodes.iterator();
			while(childSiteNodesIterator.hasNext())
			{
				SiteNode childSiteNode = (SiteNode)childSiteNodesIterator.next();
				childSiteNode.setRepository(siteNode.getRepository());
				childSiteNode.setParentSiteNode((SiteNodeImpl)siteNode);
				createStructure(childSiteNode, contentIdMap, siteNodeIdMap, siteNodeVersionIdMap, readAvailableServiceBindings, allSiteNodes, db, onlyLatestVersions, replaceMap);
			}
		}

		Collection siteNodeVersions = siteNode.getSiteNodeVersions();
		
		if(onlyLatestVersions.equalsIgnoreCase("true"))
		{
		    logger.info("org siteNodeVersions:" + siteNodeVersions.size());
			List selectedSiteNodeVersions = new ArrayList();
			Iterator realSiteNodeVersionsIterator = siteNodeVersions.iterator();
			while(realSiteNodeVersionsIterator.hasNext())
			{
				SiteNodeVersion siteNodeVersion = (SiteNodeVersion)realSiteNodeVersionsIterator.next();			
				Iterator selectedSiteNodeVersionsIterator = selectedSiteNodeVersions.iterator();
				boolean addVersion = true;
				while(selectedSiteNodeVersionsIterator.hasNext())
				{
					SiteNodeVersion currentSiteNodeVersion = (SiteNodeVersion)selectedSiteNodeVersionsIterator.next();
					if(siteNodeVersion.getIsActive().booleanValue() && siteNodeVersion.getSiteNodeVersionId().intValue() > currentSiteNodeVersion.getSiteNodeVersionId().intValue())
					{
						logger.info("A later version was found... removing this one..");
						selectedSiteNodeVersionsIterator.remove();
						addVersion = true;
					}						
				}
	
				if(addVersion)
					selectedSiteNodeVersions.add(siteNodeVersion);
			}	
			
			siteNodeVersions = selectedSiteNodeVersions;
		}

		Iterator siteNodeVersionsIterator = siteNodeVersions.iterator();
		while(siteNodeVersionsIterator.hasNext())
		{
			SiteNodeVersion siteNodeVersion = (SiteNodeVersion)siteNodeVersionsIterator.next();
			
			Collection serviceBindings = siteNodeVersion.getServiceBindings();

			siteNodeVersion.setOwningSiteNode((SiteNodeImpl)siteNode);
			
			Integer oldSiteNodeVersionId = siteNodeVersion.getId();

			db.create(siteNodeVersion);

			Integer newSiteNodeVersionId = siteNodeVersion.getId();
			siteNodeVersionIdMap.put(oldSiteNodeVersionId.toString(), newSiteNodeVersionId.toString());

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
		}		
		
	}


	/**
	 * This method copies a content and all it relations.
	 * 
	 * @param siteNode
	 * @param db
	 * @throws Exception
	 */
	
	private List createContents(Content content, Map idMap, Map contentTypeDefinitionIdMap, List allContents, Collection contentTypeDefinitions, Map categoryIdMap, int version, Database db, String onlyLatestVersions, boolean isCopyAction, Map<String,String> replaceMap) throws Exception
	{
    	//logger.info("createContents:" + content.getName() + ":" + content.getId());

		ContentTypeDefinition contentTypeDefinition = null;
		
	    Integer originalContentId = content.getContentId();
	    if(version == 2)
	    {
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
    			}
		    }
		    else
		    	logger.warn("The content " + content.getName() + " had no content type at all");
	    }
	    else if(version == 1)
	    {
			ContentTypeDefinition originalContentTypeDefinition = content.getContentTypeDefinition();
			if(originalContentTypeDefinition != null)
			{
			    contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionWithName(originalContentTypeDefinition.getName(), db);
				if(contentTypeDefinition == null)
				{
				    db.create(originalContentTypeDefinition);
				    contentTypeDefinition = originalContentTypeDefinition;
				}

	    		content.setContentTypeDefinition((ContentTypeDefinitionImpl)contentTypeDefinition);
			}
	    }
	    
	    if(content.getContentTypeDefinition() == null)
	    	logger.warn("No content type definition for content:" + content.getId());
	    	
	    logger.info("Creating content:" + content.getName());

	    content.setName(substituteStrings(content.getName(), replaceMap));

	    db.create(content);
		
		allContents.add(content);
		
		Integer newContentId = content.getContentId();
		idMap.put(originalContentId.toString(), newContentId.toString());
		
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
		
		logger.info("new contentVersions:" + contentVersions.size());
		//Collection contentVersions = content.getContentVersions();
		Iterator contentVersionsIterator = contentVersions.iterator();
		while(contentVersionsIterator.hasNext())
		{
			ContentVersion contentVersion = (ContentVersion)contentVersionsIterator.next();
			Language language = LanguageController.getController().getLanguageWithCode(contentVersion.getLanguage().getLanguageCode(), db);
			logger.info("Creating contentVersion for language:" + contentVersion.getLanguage().getLanguageCode() + " on content " + content.getName());

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
					
					if(digitalAsset.getAssetFileSize() == -1)
					{
						logger.info("digitalAsset:" + digitalAsset.getId() + "-" + digitalAsset.getAssetKey() + " was archived - let's fake it..");
						digitalAsset.setAssetBytes("archived".getBytes());						
					}
					
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
		
		Collection childContents = content.getChildren();
		if(childContents != null)
		{
			Iterator childContentsIterator = childContents.iterator();
			while(childContentsIterator.hasNext())
			{
				Content childContent = (Content)childContentsIterator.next();
				childContent.setRepository(content.getRepository());
				childContent.setParentContent((ContentImpl)content);
				createContents(childContent, idMap, contentTypeDefinitionIdMap, allContents, contentTypeDefinitions, categoryIdMap, version, db, onlyLatestVersions, isCopyAction, replaceMap);
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
					selectedContentVersions.add(contentVersion);
			}	
			
			contentVersions = selectedContentVersions;
		}

        
        Iterator contentVersionIterator = contentVersions.iterator();
        while(contentVersionIterator.hasNext())
        {
            ContentVersion contentVersion = (ContentVersion)contentVersionIterator.next();
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
	
    /* (non-Javadoc)
     * @see org.infoglue.cms.controllers.kernel.impl.simple.BaseController#getNewVO()
     */
    public BaseEntityVO getNewVO()
    {
        // TODO Auto-generated method stub
        return null;
    }


}
