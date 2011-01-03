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
 
package org.infoglue.deliver.controllers.kernel.impl.simple;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.databeans.ComponentPropertyDefinition;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.XMLHelper;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.deliver.applications.actions.InfoGlueComponent;
import org.infoglue.deliver.applications.databeans.ComponentDeliveryContext;
import org.infoglue.deliver.util.NullObject;
import org.infoglue.deliver.applications.databeans.ComponentBinding;
import org.infoglue.deliver.applications.databeans.ComponentProperty;
import org.infoglue.deliver.applications.databeans.ComponentPropertyOption;
import org.infoglue.deliver.applications.databeans.Slot;
import org.infoglue.deliver.applications.databeans.WebPage;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.Support;
import org.infoglue.deliver.util.Timer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.builder.XmlAttribute;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.xpath.Xb1XPath;

public class ComponentLogic 
{
    private final static Logger logger = Logger.getLogger(ComponentLogic.class.getName());

	private final static DOMBuilder domBuilder = new DOMBuilder();
 
	private final String separator = System.getProperty("line.separator");

	private TemplateController templateController = null;
	private InfoGlueComponent infoGlueComponent = null;
	private Integer includedComponentContentId = null;
	private boolean useInheritance = true;
	private boolean useRepositoryInheritance = true;
	private boolean useStructureInheritance = true;
	private boolean useEditOnSight = true;
	private boolean threatFoldersAsContents = false;
	private ComponentDeliveryContext componentDeliveryContext;
	
 	public ComponentLogic(TemplateController templateController, InfoGlueComponent infoGlueComponent)
 	{
 		this.templateController = templateController;
 		this.infoGlueComponent 	= infoGlueComponent;
 		this.componentDeliveryContext = ComponentDeliveryContext.getComponentDeliveryContext(templateController.getDeliveryContext(), infoGlueComponent);
 		this.componentDeliveryContext.addUsedContent("content_" + infoGlueComponent.getContentId());
 	}
 	
 	/*
	public void getDatabaseStatus(String debug)
	{
		try
		{
		    this.templateController.getDatabaseStatus(debug);
		}
		catch(Exception e) 
		{
		    e.printStackTrace();
		}
	}
	*/

	/**
	 * The method returns a list of ContentVO-objects that is children to the bound content of named binding on the siteNode sent in. 
	 * The method is great for collection-pages on any site where you want to bind to a folder containing all contents to list.
	 * You can also state if the method should recurse into subfolders and how the contents should be sorted.
	 * The recursion only deals with three levels at the moment for performance-reasons. 
	 *
	 * @param propertyName the name of the content binding property
	 * @param searchRecursive if true the search is made recursive
	 * @param sortAttribute the attribute to sort the resulting List
	 * @param sortOrder if desc sorting is descendend otherwise ascending.
	 * @return a List of ContentVO objects, 
	 * @throws Exception if an error occures
	 */
	public List getBoundFolderContents(String propertyName, boolean searchRecursive, String sortAttribute, String sortOrder) throws Exception
	{
	    return getBoundFolderContents(propertyName, searchRecursive, sortAttribute, sortOrder, false);
	}

	/**
	 * The method returns a list of ContentVO-objects that is children to the bound content of named binding on the siteNode sent in. 
	 * The method is great for collection-pages on any site where you want to bind to a folder containing all contents to list.
	 * You can also state if the method should recurse into subfolders and how the contents should be sorted.
	 * The recursion only deals with three levels at the moment for performance-reasons. 
     *
	 * @param propertyName the name of the content binding property
	 * @param searchRecursive if true the search is made recursive
	 * @param sortAttribute the attribute to sort the rsulting List
	 * @param sortOrder  if desc sorting is descendend otherwise ascending.
	 * @param includeFolders true if the folders should be added to the List
	 * @return a List of ContentVO objects,
	 * @throws Exception if an error occures
	 */
	public List getBoundFolderContents(String propertyName, boolean searchRecursive, String sortAttribute, String sortOrder, boolean includeFolders) throws Exception
	{
		return getBoundFolderContents(propertyName, searchRecursive, sortAttribute, sortOrder, includeFolders, true, true);
	}

	public List getBoundFolderContents(String propertyName, boolean searchRecursive, String sortAttribute, String sortOrder, boolean includeFolders, boolean useRepositoryInheritance) throws Exception
	{
		return getBoundFolderContents(propertyName, searchRecursive, sortAttribute, sortOrder, includeFolders, useRepositoryInheritance, true);
	}

	public List getBoundFolderContents(String propertyName, boolean searchRecursive, String sortAttribute, String sortOrder, boolean includeFolders, boolean useRepositoryInheritance, boolean useStructureInheritance) throws Exception
	{
		List childContents = new ArrayList();
		
		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, this.useInheritance, useRepositoryInheritance, useStructureInheritance);
		
		Integer contentId = getContentId(property);
		if(contentId != null)
			childContents = this.templateController.getChildContents(contentId, searchRecursive, sortAttribute, sortOrder, includeFolders);
		
		return childContents;
	}
	
	/**
	 * The method returns a list of ContentVO-objects that are related to the category of named binding on the siteNode sent in.
	 * The method is great for collection-pages on any site where you want to bind a category.
	 */
	public List getBoundCategoryContents(String categoryAttribute, String typeAttribute) throws Exception
	{
		Map categoryComponent = getInheritedComponentProperty(infoGlueComponent, categoryAttribute, this.useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
		Map attributeComponent = getInheritedComponentProperty(infoGlueComponent, typeAttribute, this.useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
		if(categoryComponent != null && attributeComponent != null)
		{
			String attr = (String)attributeComponent.get("path");
			Integer categoryId = getSingleBindingAsInteger(categoryComponent);
			final List contentVersionsByCategory = templateController.getContentVersionsByCategory(categoryId, attr);
			return contentVersionsByCategory;
		}

		return Collections.EMPTY_LIST;
	}

	public Collection getAssets(String propertyName, boolean useInheritance, boolean useRepositoryInheritance) throws Exception
	{
		return getAssets(propertyName, useInheritance, useRepositoryInheritance, true);
	}
	
	
	public List getContentAssets(String propertyName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance) throws Exception
	{
		List assets = new ArrayList();

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer contentId = getContentId(property);
		
		assets = templateController.getAssets(contentId);

		return assets;
	}
	
	public List getAssets(String propertyName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance) throws Exception
	{
		List assets = new ArrayList();
	
		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		
		if(property != null)
		{	
			List<ComponentBinding> bindings = (List<ComponentBinding>)property.get("bindings");
			Iterator<ComponentBinding> bindingsIterator = bindings.iterator();
			while(bindingsIterator.hasNext())
			{
				ComponentBinding componentBinding = bindingsIterator.next();
				Integer boundContentId 	= componentBinding.getEntityId();
				String assetKey 		= componentBinding.getAssetKey();
				
				if(assetKey != null && !assetKey.equals(""))
					assets.add(templateController.getAsset(boundContentId, assetKey));
				else
					assets.addAll(templateController.getAssets(boundContentId));
			}
		}
		
		return assets;
	}
	
	public Collection getAssetUrls(String propertyName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance) throws Exception
	{
		List assetUrls = new ArrayList();

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		if(property != null)
		{	
			List<ComponentBinding> bindings = (List<ComponentBinding>)property.get("bindings");
			Iterator<ComponentBinding> bindingsIterator = bindings.iterator();
			while(bindingsIterator.hasNext())
			{
				ComponentBinding componentBinding = bindingsIterator.next();
				Integer boundContentId 	= componentBinding.getEntityId();
				String assetKey 		= componentBinding.getAssetKey();
		
				if(assetKey != null && !assetKey.equals(""))
					assetUrls.add(templateController.getAssetUrl(boundContentId, assetKey));
				else
					assetUrls.addAll(templateController.getAssetUrls(boundContentId));
			}
		}
		
		return assetUrls;
	}

	public String getAssetUrl(String propertyName) throws Exception
	{
		String assetUrl = "";
		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, this.useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
		Integer contentId = getContentId(property);
		String assetKey = getAssetKey(property);
		if(contentId != null && ( assetKey == null || "".equals(assetKey) ))
			assetUrl = templateController.getAssetUrl(contentId);
		else if(contentId != null )
			assetUrl = templateController.getAssetUrl(contentId, assetKey);

		return assetUrl;
	}

	public String getAssetUrl(String propertyName, boolean useInheritance) throws Exception
	{
		return getAssetUrl(propertyName, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getAssetUrl(String propertyName, boolean useInheritance, boolean useRepositoryInheritance) throws Exception
	{
		return getAssetUrl(propertyName, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getAssetUrl(String propertyName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance) throws Exception
	{
		String assetUrl = "";

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer contentId = getContentId(property);
		String assetKey = getAssetKey(property);
		if(contentId != null && (assetKey == null || assetKey.equals("")))
			assetUrl = templateController.getAssetUrl(contentId);
		else if(contentId != null)
			assetUrl = templateController.getAssetUrl(contentId, assetKey);

		return assetUrl;
	}

	public String getAssetUrl(String propertyName, String assetKey) throws Exception
	{
		String assetUrl = "";
		 		
		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, this.useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
		Integer contentId = getContentId(property);
		if(contentId != null)
			assetUrl = templateController.getAssetUrl(contentId, assetKey);
		
		return assetUrl;
	}

	public String getAssetUrl(String propertyName, String assetKey, boolean useInheritance) throws Exception
	{
		return getAssetUrl(propertyName, assetKey, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getAssetUrl(String propertyName, String assetKey, boolean useInheritance, boolean useRepositoryInheritance) throws Exception
	{
		return getAssetUrl(propertyName, assetKey, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getAssetUrl(String propertyName, String assetKey, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance) throws Exception
	{
		String assetUrl = "";
		 		
		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer contentId = getContentId(property);
		if(contentId != null)
			assetUrl = templateController.getAssetUrl(contentId, assetKey);

		return assetUrl;
	}

	public String getAssetThumbnailUrl(String propertyName, int width, int height) throws Exception
	{
	    return getAssetThumbnailUrl(propertyName, width, height, this.useInheritance, this.useStructureInheritance);
	}

	public String getAssetThumbnailUrl(String propertyName, int width, int height, boolean useInheritance) throws Exception
	{
	    return getAssetThumbnailUrl(propertyName, width, height, useInheritance, this.useStructureInheritance);
	}

	public String getAssetThumbnailUrl(String propertyName, int width, int height, boolean useInheritance, boolean useStructureInheritance) throws Exception
	{
		return getAssetThumbnailUrl(propertyName, width, height, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}
	
	public String getAssetThumbnailUrl(String propertyName, int width, int height, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance) throws Exception
	{
		String assetUrl = "";
		 		
		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer contentId = getContentId(property);
		if(contentId != null)
			assetUrl = templateController.getAssetThumbnailUrl(contentId, width, height);

		return assetUrl;
	}

 	public String getAssetThumbnailUrl(String propertyName, String assetKey, int width, int height) throws Exception
 	{
 	   return getAssetThumbnailUrl(propertyName, assetKey, width, height, this.useInheritance);
 	}
 	
	public String getAssetThumbnailUrl(String propertyName, String assetKey, int width, int height, boolean useInheritance) throws Exception
 	{
		return getAssetThumbnailUrl(propertyName, assetKey, width, height, useInheritance, true, true);
 	}

	public String getAssetThumbnailUrl(String propertyName, String assetKey, int width, int height, boolean useInheritance, boolean useRepositoryInheritance) throws Exception
 	{
		return getAssetThumbnailUrl(propertyName, assetKey, width, height, useInheritance, useRepositoryInheritance, true);
 	}

	public String getAssetThumbnailUrl(String propertyName, String assetKey, int width, int height, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance) throws Exception
 	{
		String assetUrl = "";

		try
		{
			Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
			Integer contentId = getContentId(property);
			if(contentId != null)
				assetUrl = templateController.getAssetThumbnailUrl(contentId, assetKey, width, height);
		}
		catch(Exception e)
		{
			logger.warn("Error getting getAssetThumbnailUrl: " + propertyName + " on assetKey " + assetKey, e);
		}
		
		return assetUrl;
 	}

	public String getAssetThumbnailUrl(Integer contentId, int width, int height) throws Exception
	{
		String assetUrl = templateController.getAssetThumbnailUrl(contentId, width, height);
		return assetUrl;
	}

	public String getAssetThumbnailUrl(Integer contentId, String assetKey, int width, int height) throws Exception
	{
		String assetUrl = templateController.getAssetThumbnailUrl(contentId, assetKey, width, height);
		return assetUrl;
	}

 	
	public String getAssetUrl(Integer contentId, String assetKey)
	{
		String assetUrl = templateController.getAssetUrl(contentId, assetKey);

		return assetUrl;
	}
	
	public String getContentAttribute(String propertyName, String attributeName)
	{
	    return getContentAttribute(propertyName, attributeName, !this.useEditOnSight, this.useInheritance);
	}

	public String getContentAttribute(String propertyName, String attributeName, boolean disableEditOnSight)
	{
	    return getContentAttribute(propertyName, attributeName, disableEditOnSight, this.useInheritance);
	}

	public List getAssignedCategories(String propertyName, String categoryKey, Integer languageId, boolean useInheritance, boolean useLanguageFallback)
	{
		return getAssignedCategories(propertyName, categoryKey, languageId, useInheritance, useLanguageFallback, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public List getAssignedCategories(String propertyName, String categoryKey, Integer languageId, boolean useInheritance, boolean useLanguageFallback, boolean useRepositoryInheritance)
	{
		return getAssignedCategories(propertyName, categoryKey, languageId, useInheritance, useLanguageFallback, useRepositoryInheritance, this.useStructureInheritance);
	}

	public List getAssignedCategories(String propertyName, String categoryKey, Integer languageId, boolean useInheritance, boolean useLanguageFallback, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		List assignedCategories = new ArrayList();

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer contentId = getContentId(property);
		if(contentId != null)
			assignedCategories = templateController.getAssignedCategories(contentId, categoryKey, languageId, useLanguageFallback);

		return assignedCategories;
	}

	public String getContentAttribute(String propertyName, String attributeName, boolean disableEditOnSight, boolean useInheritance)
	{
		return getContentAttribute(propertyName, attributeName, disableEditOnSight, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getContentAttribute(String propertyName, String attributeName, boolean disableEditOnSight, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getContentAttribute(propertyName, attributeName, disableEditOnSight, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getContentAttribute(String propertyName, String attributeName, boolean disableEditOnSight, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		String attributeValue = "";

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer contentId = getContentId(property);
		if(contentId != null)
		{
			if(disableEditOnSight)
			    attributeValue = templateController.getContentAttribute(contentId, attributeName, disableEditOnSight);
			else
			    attributeValue = templateController.getContentAttribute(contentId, attributeName);
		}

		return attributeValue;
	}

	public String getContentAttribute(String propertyName, Integer languageId, String attributeName, boolean disableEditOnSight, boolean useInheritance)
	{
		return getContentAttribute(propertyName, languageId, attributeName, disableEditOnSight, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getContentAttribute(String propertyName, Integer languageId, String attributeName, boolean disableEditOnSight, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getContentAttribute(propertyName, languageId, attributeName, disableEditOnSight, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getContentAttribute(String propertyName, Integer languageId, String attributeName, boolean disableEditOnSight, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		String attributeValue = "";

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer contentId = getContentId(property);
		if(contentId != null)
			attributeValue = templateController.getContentAttribute(contentId, languageId, attributeName, disableEditOnSight);

		return attributeValue;
	}

	public String getParsedContentAttribute(String propertyName, String attributeName)
	{
		return getParsedContentAttribute(propertyName, attributeName, !this.useEditOnSight, this.useInheritance);
	}

	public String getParsedContentAttribute(String propertyName, String attributeName, boolean disableEditOnSight, boolean useInheritance)
	{
		return getParsedContentAttribute(propertyName, attributeName, disableEditOnSight, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getParsedContentAttribute(String propertyName, String attributeName, boolean disableEditOnSight, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getParsedContentAttribute(propertyName, attributeName, disableEditOnSight, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}
	
	public String getParsedContentAttribute(String propertyName, String attributeName, boolean disableEditOnSight, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		String attributeValue = "";

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer contentId = getContentId(property);
		if(contentId != null)
			attributeValue = templateController.getParsedContentAttribute(contentId, attributeName, disableEditOnSight);

		return attributeValue;
	}

	public String getParsedContentAttribute(String propertyName, Integer languageId, String attributeName, boolean disableEditOnSight, boolean useInheritance)
	{
		return getParsedContentAttribute(propertyName, languageId, attributeName, disableEditOnSight, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getParsedContentAttribute(String propertyName, Integer languageId, String attributeName, boolean disableEditOnSight, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getParsedContentAttribute(propertyName, languageId, attributeName, disableEditOnSight, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getParsedContentAttribute(String propertyName, Integer languageId, String attributeName, boolean disableEditOnSight, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		String attributeValue = "";

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer contentId = getContentId(property);
		if(contentId != null)
			attributeValue = templateController.getParsedContentAttribute(contentId, languageId, attributeName, disableEditOnSight);
			
		return attributeValue;
	}

	public List getFormAttributes(String propertyName, String attributeName)
	{
		return getFormAttributes(propertyName, attributeName, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public List getFormAttributes(String propertyName, String attributeName, boolean useRepositoryInheritance)
	{
		return getFormAttributes(propertyName, attributeName, useRepositoryInheritance, this.useStructureInheritance);
	}

	public List getFormAttributes(String propertyName, String attributeName, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		List formAttributes = new ArrayList();

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, this.useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer contentId = getContentId(property);
		if(contentId != null)
		{
			String formDefinition = templateController.getContentAttribute(contentId, attributeName, true);
			formAttributes = FormDeliveryController.getFormDeliveryController().getContentTypeAttributes(formDefinition);
		}
					
		return formAttributes;
	}
	
	public String getPropertyValue(String propertyName) throws SystemException
	{
		return getPropertyValue(propertyName, true);
	}

	public String getPropertyValue(String propertyName, boolean useLangaugeFallback) throws SystemException
	{
		return getPropertyValue(propertyName, useLangaugeFallback, this.useInheritance);
	}

	public String getPropertyValue(String propertyName, boolean useLangaugeFallback, boolean useInheritance) throws SystemException
	{
		return getPropertyValue(propertyName, useLangaugeFallback, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getPropertyValue(String propertyName, boolean useLangaugeFallback, boolean useInheritance, boolean useRepositoryInheritance) throws SystemException
	{
		return getPropertyValue(propertyName, useLangaugeFallback, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getPropertyValue(String propertyName, boolean useLangaugeFallback, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance) throws SystemException
	{
		String propertyValue = "";
		
		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		if(property != null)
		{	
			propertyValue = (String)property.get("path_" + this.templateController.getLocale().getLanguage());
			if(propertyValue == null)
			{
				propertyValue = (String)property.get("path");
			}
		}
		
		if(propertyValue == null || propertyValue.equals(""))
		{
			try
			{
				ContentVO contentVO = templateController.getContent(this.infoGlueComponent.getContentId());
				LanguageVO masterLanguage = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(templateController.getDatabase(), contentVO.getRepositoryId());
	
				ComponentPropertyDefinition propertyDefinition = getComponentPropertyDefinition(this.infoGlueComponent.getContentId(), propertyName, templateController.getSiteNodeId(), masterLanguage.getId(), templateController.getContentId(), templateController.getDatabase(), templateController.getPrincipal());
				if(propertyDefinition != null && propertyDefinition.getDefaultValue() != null)
					propertyValue = propertyDefinition.getDefaultValue();
			}
			catch (Exception e) 
			{
				logger.error("Error getting propertyValue on + " + propertyName + ":" + e.getMessage(), e);
			}
		}
				
		if(propertyValue != null)
			propertyValue = propertyValue.replaceAll("igbr", separator);

		return propertyValue;
	}

	public String getPropertyValue(String propertyName, boolean useLangaugeFallback, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance, boolean useComponentInheritance, InfoGlueComponent component) throws SystemException
	{
		String propertyValue = "";

		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), templateController.getLanguageId());
		
		Map property = getInheritedComponentProperty(component, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance, useComponentInheritance);
		if(property != null)
		{	
			propertyValue = (String)property.get("path_" + locale.getLanguage());
			if(property != null)
			{
				propertyValue = (String)property.get("path");
			}
		}

		if(propertyValue == null || propertyValue.equals(""))
		{
			try
			{
				ContentVO contentVO = templateController.getContent(component.getContentId());
				LanguageVO masterLanguage = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(templateController.getDatabase(), contentVO.getRepositoryId());
	
				ComponentPropertyDefinition propertyDefinition = getComponentPropertyDefinition(component.getContentId(), propertyName, templateController.getSiteNodeId(), masterLanguage.getId(), templateController.getContentId(), templateController.getDatabase(), templateController.getPrincipal());
				if(propertyDefinition != null && propertyDefinition.getDefaultValue() != null)
					propertyValue = propertyDefinition.getDefaultValue();
			}
			catch (Exception e) 
			{
				logger.error("Error getting propertyValue on + " + propertyName + ":" + e.getMessage(), e);
			}
		}

		if(propertyValue != null)
			propertyValue = propertyValue.replaceAll("igbr", separator);

		return propertyValue;
	}

	public String getPropertyValue(Integer siteNodeId, String propertyName, boolean useLangaugeFallback, boolean useInheritance) throws SystemException
	{
		String propertyValue = "";

		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), templateController.getLanguageId());

		Map property = getInheritedComponentProperty(siteNodeId, this.infoGlueComponent, propertyName, useInheritance);
		if(property != null)
		{	
			if(property != null)
			{
				propertyValue = (String)property.get("path");
				if(propertyValue == null)
				{
					Iterator keysIterator = property.keySet().iterator();
					while(keysIterator.hasNext())
					{
						String key = (String)keysIterator.next();
					}
				}
			}
		}

		if(propertyValue == null || propertyValue.equals(""))
		{
			try
			{
				ContentVO contentVO = templateController.getContent(this.infoGlueComponent.getContentId());
				LanguageVO masterLanguage = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(templateController.getDatabase(), contentVO.getRepositoryId());
	
				ComponentPropertyDefinition propertyDefinition = getComponentPropertyDefinition(this.infoGlueComponent.getContentId(), propertyName, templateController.getSiteNodeId(), masterLanguage.getId(), templateController.getContentId(), templateController.getDatabase(), templateController.getPrincipal());
				if(propertyDefinition != null && propertyDefinition.getDefaultValue() != null)
					propertyValue = propertyDefinition.getDefaultValue();
			}
			catch (Exception e) 
			{
				logger.error("Error getting propertyValue on + " + propertyName + ":" + e.getMessage(), e);
			}
		}

		if(propertyValue != null)
			propertyValue = propertyValue.replaceAll("igbr", separator);

		return propertyValue;
	}

	public String getPropertyValue(Integer siteNodeId, String propertyName, boolean useLangaugeFallback, boolean useInheritance, InfoGlueComponent component) throws SystemException
	{
		String propertyValue = "";

		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), templateController.getLanguageId());

		Map property = getInheritedComponentProperty(siteNodeId, component, propertyName, useInheritance);
		if(property != null)
		{	
			if(property != null)
			{
				propertyValue = (String)property.get("path");
				if(propertyValue == null)
				{
					Iterator keysIterator = property.keySet().iterator();
					while(keysIterator.hasNext())
					{
						String key = (String)keysIterator.next();
					}
				}
			}
		}

		if(propertyValue == null || propertyValue.equals(""))
		{
			try
			{
				ContentVO contentVO = templateController.getContent(component.getContentId());
				LanguageVO masterLanguage = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(templateController.getDatabase(), contentVO.getRepositoryId());
	
				ComponentPropertyDefinition propertyDefinition = getComponentPropertyDefinition(component.getContentId(), propertyName, templateController.getSiteNodeId(), masterLanguage.getId(), templateController.getContentId(), templateController.getDatabase(), templateController.getPrincipal());
				if(propertyDefinition != null && propertyDefinition.getDefaultValue() != null)
					propertyValue = propertyDefinition.getDefaultValue();
			}
			catch (Exception e) 
			{
				logger.error("Error getting propertyValue on + " + propertyName + ":" + e.getMessage(), e);
			}
		}

		if(propertyValue != null)
			propertyValue = propertyValue.replaceAll("igbr", separator);

		return propertyValue;
	}

	
	public ContentVO getBoundContent(String propertyName)
	{
	    return getBoundContent(propertyName, true);
	}

	public ContentVO getBoundContent(String propertyName, boolean useInheritance)
	{
		return getBoundContent(propertyName, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public ContentVO getBoundContent(String propertyName, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getBoundContent(propertyName, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public ContentVO getBoundContent(String propertyName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		ContentVO content = null;

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer contentId = getContentId(property);
		if(contentId != null)
			content = this.templateController.getContent(contentId);
			
		return content;
	}

	public ContentVO getBoundContentWithDetailSiteNodeId(String propertyName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		ContentVO content = null;

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Map detailSiteNodeIdProperty = getInheritedComponentProperty(this.infoGlueComponent, propertyName + "_detailSiteNodeId", useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer contentId = getContentId(property);
		if(contentId != null)
			content = this.templateController.getContent(contentId);
		
		if(detailSiteNodeIdProperty != null && detailSiteNodeIdProperty.get("path") != null && !detailSiteNodeIdProperty.get("path").equals("Undefined"))
			content.getExtraProperties().put("detailSiteNodeId", new Integer((String)detailSiteNodeIdProperty.get("path")));
			
		return content;
	}

	public ContentVO getBoundContent(Integer siteNodeId, String propertyName, boolean useInheritance)
	{
		ContentVO content = null;

		Map property = getInheritedComponentProperty(siteNodeId, this.infoGlueComponent, propertyName, useInheritance);
		Integer contentId = getContentId(property);
		if(contentId != null)
			content = this.templateController.getContent(contentId);
		
		return content;
	}

	public ContentVO getBoundContentWithDetailSiteNodeId(Integer siteNodeId, String propertyName, boolean useInheritance)
	{
		ContentVO content = null;

		Map property = getInheritedComponentProperty(siteNodeId, this.infoGlueComponent, propertyName, useInheritance);
		Map detailSiteNodeIdProperty = getInheritedComponentProperty(this.infoGlueComponent, propertyName + "_detailSiteNodeId", useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer contentId = getContentId(property);
		if(contentId != null)
			content = this.templateController.getContent(contentId);

		if(detailSiteNodeIdProperty != null && detailSiteNodeIdProperty.get("path") != null && !detailSiteNodeIdProperty.get("path").equals("Undefined"))
			content.getExtraProperties().put("detailSiteNodeId", new Integer((String)detailSiteNodeIdProperty.get("path")));

		return content;
	}

	public List<ContentVO> getBoundContents(Integer siteNodeId, String propertyName, boolean useInheritance)
	{
		List<ContentVO> contents = new ArrayList<ContentVO>();

		Map property = getInheritedComponentProperty(siteNodeId, this.infoGlueComponent, propertyName, useInheritance);
		contents = getBoundContents(property);
		
		return contents;
	}

	public Integer getBoundContentId(String propertyName)
	{
	    return getBoundContentId(propertyName, true);
	}

	public Integer getBoundContentId(String propertyName, boolean useInheritance)
	{
		return getBoundContentId(propertyName, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public Integer getBoundContentId(String propertyName, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getBoundContentId(propertyName, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public Integer getBoundContentId(String propertyName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		Integer contentId = null;

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		contentId = getContentId(property);
		
		return contentId;
	}

	public List getBoundContents(String propertyName)
	{
	    return getBoundContents(propertyName, this.useInheritance);
	}

	public List getBoundContents(String propertyName, boolean useInheritance)
	{
		return getBoundContents(propertyName, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public List getBoundContents(String propertyName, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getBoundContents(propertyName, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public List getBoundContents(String propertyName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		List contents = new ArrayList();

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		contents = getBoundContents(property);

		return contents;
	}
	
	/**
	 * This method returns a list of childContents using inheritence as default.
	 */

	public List getChildContents(String propertyName)
	{
	    return getChildContents(propertyName, this.useInheritance, false, "id", "asc", false);
	}
	
	/**
	 * This method returns a list of childcontents.
	 */
	public List getChildContents(String propertyName, boolean useInheritance, boolean searchRecursive, String sortAttribute, String sortOrder, boolean includeFolders)
	{
		return getChildContents(propertyName, useInheritance, searchRecursive, sortAttribute, sortOrder, includeFolders, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	/**
	 * This method returns a list of childcontents.
	 */
	public List getChildContents(String propertyName, boolean useInheritance, boolean searchRecursive, String sortAttribute, String sortOrder, boolean includeFolders, boolean useRepositoryInheritance)
	{
		return getChildContents(propertyName, useInheritance, searchRecursive, sortAttribute, sortOrder, includeFolders, useRepositoryInheritance, this.useStructureInheritance);
	}

	public List getChildContents(String propertyName, boolean useInheritance, boolean searchRecursive, String sortAttribute, String sortOrder, boolean includeFolders, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
	    List childContents = new ArrayList();
	    
	    Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		if(property != null)
		{	
			List<ComponentBinding> bindings = (List<ComponentBinding>)property.get("bindings");
			Iterator<ComponentBinding> bindingsIterator = bindings.iterator();
			while(bindingsIterator.hasNext())
			{
				Integer contentId = bindingsIterator.next().getEntityId();
				childContents.addAll(this.templateController.getChildContents(contentId, searchRecursive, sortAttribute, sortOrder, includeFolders));
			}
		}	
		return childContents;
	}

	public WebPage getBoundPage(String propertyName)
	{
	    return getBoundPage(propertyName, this.useInheritance);
	}
	
	/**
	 * This method returns a page bound to the component.
	 */
	public WebPage getBoundPage(String propertyName, boolean useInheritance)
	{
		return getBoundPage(propertyName, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	/**
	 * This method returns a page bound to the component.
	 */
	public WebPage getBoundPage(String propertyName, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getBoundPage(propertyName, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public WebPage getBoundPage(String propertyName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		WebPage webPage = null;
		
		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		webPage = getBoundPage(property);

		return webPage;
	}

	
	public List getBoundPages(String propertyName)
	{
	    return getBoundPages(propertyName, this.useInheritance);
	}
	
	/**
	 * This method returns a list of pages bound to the component.
	 */
	public List getBoundPages(String propertyName, boolean useInheritance)
	{
		return getBoundPages(propertyName, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	/**
	 * This method returns a list of pages bound to the component.
	 */
	public List getBoundPages(String propertyName, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getBoundPages(propertyName, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public List getBoundPages(String propertyName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		List pages = new ArrayList();

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		pages = getBoundPages(property);
		
		return pages;
	}

	/**
	 * This method returns a list of pages bound to the component.
	 */
	public SiteNodeVO getBoundSiteNode(String propertyName, boolean useInheritance)
	{
		return getBoundSiteNode(propertyName, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	/**
	 * This method returns a list of pages bound to the component.
	 */
	public SiteNodeVO getBoundSiteNode(String propertyName, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getBoundSiteNode(propertyName, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public SiteNodeVO getBoundSiteNode(String propertyName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
	    SiteNodeVO siteNodeVO = null;
	    
	    Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer siteNodeId = getSiteNodeId(property);
		if(siteNodeId != null)
			siteNodeVO = templateController.getSiteNode(siteNodeId);
		
		return siteNodeVO;
	}

	/**
	 * This method returns a single page bound to the component on the given siteNode.
	 */

	public SiteNodeVO getBoundSiteNode(Integer targetSiteNodeId, String propertyName, boolean useInheritance)
	{
	    SiteNodeVO siteNodeVO = null;
	    
	    Map property = getInheritedComponentProperty(targetSiteNodeId, this.infoGlueComponent, propertyName, useInheritance);
	    Integer siteNodeId = getSiteNodeId(property);
		if(siteNodeId != null)
			siteNodeVO = templateController.getSiteNode(siteNodeId);
			
		return siteNodeVO;
	}

	/**
	 * This method returns a single page bound to the component on the given siteNode.
	 */

	public List<SiteNodeVO> getBoundSiteNodes(Integer targetSiteNodeId, String propertyName, boolean useInheritance)
	{
	    List<SiteNodeVO> siteNodeVOList = new ArrayList();
	    
	    Map property = getInheritedComponentProperty(targetSiteNodeId, this.infoGlueComponent, propertyName, useInheritance);
	    siteNodeVOList = getBoundSiteNodes(property);
			
		return siteNodeVOList;
	}
	
	/**
	 * This method returns a list of childpages using inheritence as default.
	 */

	public List getChildPages(String propertyName)
	{
	    return getChildPages(propertyName, this.useInheritance);
	}

	public List getChildPages(String propertyName, boolean useInheritance)
	{
	    return getChildPages(propertyName, useInheritance, false, false);
	}

	public List getChildPages(String propertyName, boolean useInheritance, boolean escapeHTML)
	{
	    return getChildPages(propertyName, this.useInheritance, escapeHTML, false);
	}

	/**
	 * This method returns a list of childpages.
	 */
	public List getChildPages(String propertyName, boolean useInheritance, boolean escapeHTML, boolean hideUnauthorizedPages)
	{
		return getChildPages(propertyName, useInheritance, escapeHTML, hideUnauthorizedPages, true, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public List getChildPages(String propertyName, boolean useInheritance, boolean escapeHTML, boolean hideUnauthorizedPages, boolean useRepositoryInheritance)
	{
		return getChildPages(propertyName, useInheritance, escapeHTML, hideUnauthorizedPages, true, useRepositoryInheritance, this.useStructureInheritance);
	}

	public List getChildPages(String propertyName, boolean useInheritance, boolean escapeHTML, boolean hideUnauthorizedPages, boolean includeHidden, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
	    List childPages = new ArrayList();
	    
	    Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
	    if(property != null)
		{	
			List<ComponentBinding> bindings = (List<ComponentBinding>)property.get("bindings");
			Iterator<ComponentBinding> bindingsIterator = bindings.iterator();
			while(bindingsIterator.hasNext())
			{
				Integer siteNodeId = bindingsIterator.next().getEntityId();
				childPages.addAll(getChildPages(siteNodeId, escapeHTML, hideUnauthorizedPages, includeHidden));
			}
		}

	    return childPages;
	}

	
	/**
	 * This method returns a list of childpages.
	 */

	public List getChildPages(Integer siteNodeId)
	{
		List pages = templateController.getChildPages(siteNodeId);

		Iterator pagesIterator = pages.iterator();
		while(pagesIterator.hasNext())
		{
			WebPage webPage = (WebPage)pagesIterator.next();
			webPage.setUrl(getPageUrl(webPage.getSiteNodeId()));
		}
	
		return pages;
	}

	/**
	 * This method returns a list of childpages.
	 */

	public List getChildPages(Integer siteNodeId, boolean escapeHTML, boolean hideUnauthorizedPages, boolean showHidden)
	{
		List pages = templateController.getChildPages(siteNodeId, escapeHTML, hideUnauthorizedPages, showHidden);

		Iterator pagesIterator = pages.iterator();
		while(pagesIterator.hasNext())
		{
			WebPage webPage = (WebPage)pagesIterator.next();
			webPage.setUrl(getPageUrl(webPage.getSiteNodeId()));
		}
	
		return pages;
	}

	public String getPageUrl(String propertyName) throws Exception
	{
	    return getPageUrl(propertyName, this.useInheritance);
	}

	public String getPageUrl(String propertyName, boolean useInheritance)
	{
		return getPageUrl(propertyName, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getPageUrl(String propertyName, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getPageUrl(propertyName, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getPageUrl(String propertyName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		String pageUrl = "";

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer siteNodeId = getSiteNodeId(property);
		if(siteNodeId != null)
			pageUrl = this.getPageUrl(siteNodeId, templateController.getLanguageId(), templateController.getContentId());
		
		return pageUrl;		
	}
	
	public String getPageUrl(Integer siteNodeId)
	{
		String pageUrl = "";

		pageUrl = this.getPageUrl(siteNodeId, templateController.getLanguageId(), null);

		return pageUrl;
	}

	public String getPageUrl(String propertyName, Integer contentId)
	{
	    return getPageUrl(propertyName, contentId, this.useInheritance);
	}

	public String getPageUrl(String propertyName, Integer contentId, boolean useInheritance)
	{
		return getPageUrl(propertyName, contentId, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getPageUrl(String propertyName, Integer contentId, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getPageUrl(propertyName, contentId, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getPageUrl(String propertyName, Integer contentId, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		String pageUrl = "";

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer siteNodeId = getSiteNodeId(property);
		if(siteNodeId != null)
			pageUrl = this.getPageUrl(siteNodeId, templateController.getLanguageId(), contentId);
			
		return pageUrl;
	}

	public String getPageUrl(String propertyName, Integer contentId, Integer languageId, boolean useInheritance)
	{
		return getPageUrl(propertyName, contentId, languageId, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getPageUrl(String propertyName, Integer contentId, Integer languageId, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getPageUrl(propertyName, contentId, languageId, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getPageUrl(String propertyName, Integer contentId, Integer languageId, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		String pageUrl = "";

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer siteNodeId = getSiteNodeId(property);
		if(siteNodeId != null)
			pageUrl = this.getPageUrl(siteNodeId, languageId, contentId);
			
		return pageUrl;
	}

	/**
	 * This method calls an page and stores it as an digitalAsset - that way one can avoid having to 
	 * serve javascript-files and css-files through InfoGlue. Not suitable for use if you have very dynamic
	 * css:es or scripts which includes logic depending on user info etc.. mostly usable if you have a static css
	 * or controls it on the pageCache parameters.
	 */
	 
	public String getPageAsDigitalAssetUrl(String propertyName) throws Exception
	{
	    return getPageAsDigitalAssetUrl(propertyName, this.useInheritance, "");
	}

	/**
	 * This method calls an page and stores it as an digitalAsset - that way one can avoid having to 
	 * serve javascript-files and css-files through InfoGlue. Not suitable for use if you have very dynamic
	 * css:es or scripts which includes logic depending on user info etc.. mostly usable if you have a static css
	 * or controls it on the pageCache parameters.
	 */
	public String getPageAsDigitalAssetUrl(String propertyName, boolean useInheritance, String fileSuffix)
	{
		return getPageAsDigitalAssetUrl(propertyName, useInheritance, fileSuffix, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getPageAsDigitalAssetUrl(String propertyName, boolean useInheritance, String fileSuffix, boolean useRepositoryInheritance)
	{
		return getPageAsDigitalAssetUrl(propertyName, useInheritance, fileSuffix, useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getPageAsDigitalAssetUrl(String propertyName, boolean useInheritance, String fileSuffix, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		String pageUrl = "";

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer siteNodeId = getSiteNodeId(property);
		if(siteNodeId != null)
			pageUrl = this.getPageAsDigitalAssetUrl(siteNodeId, templateController.getLanguageId(), templateController.getContentId(), fileSuffix);
		
		return pageUrl;		
	}

	/**
	 * This method calls an page and stores it as an digitalAsset - that way one can avoid having to 
	 * serve javascript-files and css-files through InfoGlue. Not suitable for use if you have very dynamic
	 * css:es or scripts which includes logic depending on user info etc.. mostly usable if you have a static css
	 * or controls it on the pageCache parameters.
	 */
	public String getPageAsDigitalAssetUrl(String propertyName, Integer languageId, Integer contentId, boolean useInheritance, String fileSuffix, boolean cacheUrl)
	{
		return getPageAsDigitalAssetUrl(propertyName, languageId, contentId, useInheritance, fileSuffix, cacheUrl, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getPageAsDigitalAssetUrl(String propertyName, Integer languageId, Integer contentId, boolean useInheritance, String fileSuffix, boolean cacheUrl, boolean useRepositoryInheritance)
	{
		return getPageAsDigitalAssetUrl(propertyName, languageId, contentId, useInheritance, fileSuffix, cacheUrl, useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getPageAsDigitalAssetUrl(String propertyName, Integer languageId, Integer contentId, boolean useInheritance, String fileSuffix, boolean cacheUrl, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		String pageUrl = "";

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer siteNodeId = getSiteNodeId(property);
		if(siteNodeId != null)
			pageUrl = this.getPageAsDigitalAssetUrl(siteNodeId, languageId, contentId, fileSuffix, cacheUrl);

		return pageUrl;		
	}

	/**
	 * This method calls an page and stores it as an digitalAsset - that way one can avoid having to 
	 * serve javascript-files and css-files through InfoGlue. Not suitable for use if you have very dynamic
	 * css:es or scripts which includes logic depending on user info etc.. mostly usable if you have a static css
	 * or controls it on the pageCache parameters.
	 */
	public String getPageAsDigitalAssetUrl(Integer siteNodeId, Integer languageId, Integer contentId, String fileSuffix)
	{
		String pageUrl = getPageAsDigitalAssetUrl(siteNodeId, languageId, contentId, fileSuffix, true);
		
		return pageUrl;
	}


	/**
	 * This method calls an page and stores it as an digitalAsset - that way one can avoid having to 
	 * serve javascript-files and css-files through InfoGlue. Not suitable for use if you have very dynamic
	 * css:es or scripts which includes logic depending on user info etc.. mostly usable if you have a static css
	 * or controls it on the pageCache parameters.
	 */
	public String getPageAsDigitalAssetUrl(Integer siteNodeId, Integer languageId, Integer contentId, String fileSuffix, boolean cacheUrl)
	{
		String pageUrl = this.templateController.getPageAsDigitalAssetUrl(siteNodeId, languageId, contentId, fileSuffix, cacheUrl);
		
		return pageUrl;
	}

	public String getPageNavTitle(String propertyName)
	{
		return getPageNavTitle(propertyName, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getPageNavTitle(String propertyName, boolean useRepositoryInheritance)
	{
		return getPageNavTitle(propertyName, useRepositoryInheritance, this.useStructureInheritance);
	}

	public String getPageNavTitle(String propertyName, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		String pageUrl = "";

		Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, this.useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer siteNodeId = getSiteNodeId(property);
		if(siteNodeId != null)
			pageUrl = templateController.getPageNavTitle(siteNodeId);

		return pageUrl;
	}

	public List getRelatedPages(String propertyName, String attributeName)
	{
		return getRelatedPages(propertyName, attributeName, this.useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public List getRelatedPages(String propertyName, String attributeName, boolean useInheritance)
	{
		return getRelatedPages(propertyName, attributeName, useInheritance, this.useRepositoryInheritance, this.useStructureInheritance);
	}

	public List getRelatedPages(String propertyName, String attributeName, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getRelatedPages(propertyName, attributeName, useInheritance, useRepositoryInheritance, this.useStructureInheritance);
	}

	public List getRelatedPages(String propertyName, String attributeName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
	    List relatedPages = new ArrayList();
	    
	    Map property = getInheritedComponentProperty(this.infoGlueComponent, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance);
		Integer contentId = getContentId(property);
		if(contentId != null)
			relatedPages = templateController.getRelatedPages(contentId, attributeName);
			
		return relatedPages;
	}

	/**
	 * This method gets a property from the component and if not found there checks in parent components.
	 */
/*
	public Map getInheritedComponentProperty(InfoGlueComponent component, String propertyName, boolean useInheritance)
	{
		return getInheritedComponentProperty(component, propertyName, useInheritance, true);
	}
*/	

	/**
	 * This method gets a property from the component and if not found there checks in parent components.
	 */

	public Map AgetInheritedComponentProperty(InfoGlueComponent component, String propertyName, boolean useInheritance, boolean useRepositoryInheritance)
	{
		return getInheritedComponentProperty(component, propertyName, useInheritance, useRepositoryInheritance, true);
	}

	/**
	 * This method gets a property from the component and if not found there checks in parent components.
	 */

	public Map getInheritedComponentProperty(InfoGlueComponent component, String propertyName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance)
	{
		return getInheritedComponentProperty(component, propertyName, useInheritance, useRepositoryInheritance, useStructureInheritance, true);
	}
	
	/**
	 * This method gets a property from the component and if not found there checks in parent components.
	 */

	public Map getInheritedComponentProperty(InfoGlueComponent component, String propertyName, boolean useInheritance, boolean useRepositoryInheritance, boolean useStructureInheritance, boolean useComponentInheritance)
	{
		Map property = null;
		
	    Set contentVersionIdList = new HashSet();
	    if(templateController.getDeliveryContext().getUsedPageComponentsMetaInfoContentVersionIdSet().size() > 0)
	    	contentVersionIdList.addAll(templateController.getDeliveryContext().getUsedPageComponentsMetaInfoContentVersionIdSet());
	    	
	    try
		{
		    String key = "" + templateController.getSiteNodeId() + "_" + templateController.getLanguageId() + "_" + component.getName() + "_" + component.getSlotName() + "_" + component.getContentId() + "_" + component.getId() + "_" + component.getIsInherited() + "_" + propertyName + "_" + useInheritance + "_" + useRepositoryInheritance + "_" + useStructureInheritance + "_" + useComponentInheritance; 
		    String versionKey = key + "_contentVersionIds";
			Object propertyCandidate = CacheController.getCachedObjectFromAdvancedCache("componentPropertyCache", key);
			Set propertyCandidateVersions = (Set)CacheController.getCachedObjectFromAdvancedCache("componentPropertyVersionIdCache", versionKey);
			
			if(propertyCandidate != null)
			{
				if(propertyCandidate instanceof NullObject)
					property = null;				
				else
					property = (Map)propertyCandidate;
					
				if(propertyCandidateVersions != null)
					contentVersionIdList.addAll(propertyCandidateVersions);				
			}
			else
			{
				property = getComponentProperty(propertyName, useInheritance, useStructureInheritance, contentVersionIdList, useRepositoryInheritance);
		    	if(property == null)
				{	
					property = (Map)component.getProperties().get(propertyName);
					InfoGlueComponent parentComponent = component.getParentComponent();
					//logger.info("parentComponent: " + parentComponent);
					while(property == null && parentComponent != null && useComponentInheritance)
					{
						property = (Map)parentComponent.getProperties().get(propertyName);
						parentComponent = parentComponent.getParentComponent();
					}
				}
				
			    Set groups = new HashSet();
				Iterator contentVersionIdListIterator = contentVersionIdList.iterator();
			    while(contentVersionIdListIterator.hasNext())
			    {
					Integer contentVersionId = (Integer)contentVersionIdListIterator.next();
					ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, this.templateController.getDatabase());
				    groups.add("contentVersion_" + contentVersionId);
				    groups.add("content_" + contentVersionVO.getContentId());
				}

			    //System.out.println("Adding group: " + "siteNode_" + templateController.getSiteNodeId());
			    groups.add("siteNode_" + templateController.getSiteNodeId());
			    
			    if(groups.size() < 26)
			    {
			    	CacheController.cacheObjectInAdvancedCacheWithGroupsAsSet("componentPropertyCache", key, property, groups, true);
				    CacheController.cacheObjectInAdvancedCacheWithGroupsAsSet("componentPropertyVersionIdCache", versionKey, contentVersionIdList, groups, true);
			    }
			}
		}
		catch(Exception e)
		{
			logger.warn("Error getting property:" + propertyName, e);
		}
		
		Iterator contentVersionIdListIterator = contentVersionIdList.iterator();
		while(contentVersionIdListIterator.hasNext())
		{
			Integer currentContentVersionId = (Integer)contentVersionIdListIterator.next();
			templateController.getDeliveryContext().addUsedContentVersion("contentVersion_" + currentContentVersionId);
		}

		return property;
	}

	/**
	 * This method gets a property from the component and if not found there checks in parent components.
	 */

	public Map getInheritedComponentProperty(Integer siteNodeId, InfoGlueComponent component, String propertyName, boolean useInheritance)
	{
	    try
		{
			Map property1 = getComponentProperty(siteNodeId, propertyName, useInheritance);
			if(property1 != null)
				return property1;
			/*	
			Map property = (Map)component.getProperties().get(propertyName);
			InfoGlueComponent parentComponent = component.getParentComponent();
			//logger.info("parentComponent: " + parentComponent);
			while(property == null && parentComponent != null)
			{
				property = (Map)parentComponent.getProperties().get(propertyName);
				parentComponent = parentComponent.getParentComponent();
			}
			*/
			
			return null;
		}
		catch(Exception e)
		{
			logger.error("Error getting propertyValue on + " + propertyName + ":" + e.getMessage(), e);
		}
		
		return null;
	}

	/**
	 * This method gets a property from the component and if not found there checks in parent components.
	 */

	public List getInheritedComponentProperties(String propertyName, boolean useInheritance)
	{
		return getInheritedComponentProperties(this.templateController.getSiteNodeId(), propertyName, useInheritance);
	}

	/**
	 * This method gets a property from the component and if not found there checks in parent components.
	 */

	public List getInheritedComponentProperties(String propertyName, boolean useInheritance, boolean skipRepositoryInheritance)
	{
		return getInheritedComponentProperties(this.templateController.getSiteNodeId(), propertyName, useInheritance, skipRepositoryInheritance);
	}
	
	/**
	 * This method gets a property from the component and if not found there checks in parent components.
	 */

	public List getInheritedComponentProperties(Integer siteNodeId, String propertyName, boolean useInheritance)
	{
	    return getInheritedComponentProperties(siteNodeId, propertyName, useInheritance, false);
	}


	/**
	 * This method gets a property from the component and if not found there checks in parent components.
	 */

	public List getInheritedComponentProperties(Integer siteNodeId, String propertyName, boolean useInheritance, boolean skipRepositoryInheritance)
	{
	    try
		{
			List properties = getComponentProperties(siteNodeId, propertyName, useInheritance, skipRepositoryInheritance);
			if(properties != null)
				return properties;
			
			return null;
		}
		catch(Exception e)
		{
			logger.error("Error getting inherited component properties on + " + propertyName + ":" + e.getMessage(), e);
		}
		
		return null;
	}

	/**
	 * This method gets if a property is defined and available in the given page.
	 */
	public boolean getHasDefinedProperty(Integer siteNodeId, Integer languageId, String propertyName, boolean useInheritance)
	{
	    Map property = getComponentProperty(siteNodeId, languageId, propertyName, useInheritance);
	    
	    return property == null ? false : true;
	}   

	
	/**
	 * This method gets a property from the sitenode given and also looks recursively upwards.
	 */
	
	public Map getComponentProperty(Integer siteNodeId, Integer languageId, String propertyName, boolean useInheritance)
	{
	    Map componentProperty = getComponentProperty(siteNodeId, languageId, propertyName);
	    SiteNodeVO parentSiteNodeVO = this.templateController.getParentSiteNode(siteNodeId);
	    while(componentProperty == null && useInheritance && parentSiteNodeVO != null)
	    {
	        componentProperty = getComponentProperty(parentSiteNodeVO.getId(), languageId, propertyName);
	        parentSiteNodeVO = this.templateController.getParentSiteNode(parentSiteNodeVO.getId());
	    }
	    
	    return componentProperty;
	}

	    
	/**
	 * This method gets a property from the sitenode given	.
	 */
 
	public Map getComponentProperty(Integer siteNodeId, Integer languageId, String propertyName)
	{
		Map property = null;
		
	    Set contentVersionIdList = new HashSet();

	    try
		{
	        String componentPropertiesXML = getPageComponentsString(this.templateController, siteNodeId, languageId, new Integer(-1), contentVersionIdList);
	        
		    String key = "" + siteNodeId + "_" + languageId + "_" + propertyName;
		    String versionKey = key + "_contentVersionIds";
			Object propertyCandidate = CacheController.getCachedObjectFromAdvancedCache("componentPropertyCache", key);
			Set propertyCandidateVersions = (Set)CacheController.getCachedObjectFromAdvancedCache("componentPropertyVersionIdCache", versionKey);

			if(propertyCandidate != null)
			{
				if(propertyCandidate instanceof NullObject)
					property = null;				
				else
					property = (Map)propertyCandidate;
					
				if(propertyCandidateVersions != null)
					contentVersionIdList.addAll(propertyCandidateVersions);
			}
			else
			{
			    logger.info("Have to fetch property from XML...:" + key);
	        
				if(componentPropertiesXML != null && componentPropertiesXML.length() > 0)
				{
					Document document = XMLHelper.readDocumentFromByteArray(componentPropertiesXML.getBytes("UTF-8"));
					String propertyXPath = "//component/properties/property[@name='" + propertyName + "']";
					NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), propertyXPath);
					//logger.info("*********************************************************anl:" + anl.getLength());
									
					for(int i=0; i < anl.getLength(); i++)
					{
						Element propertyElement = (Element)anl.item(i);
	
						String name		= propertyElement.getAttribute("name");
						String type		= propertyElement.getAttribute("type");
						String entity 	= propertyElement.getAttribute("entity");
						boolean isMultipleBinding = new Boolean(propertyElement.getAttribute("multiple")).booleanValue();
						
						String value = null;
						
						if(type.equalsIgnoreCase("textfield") || type.equalsIgnoreCase("textarea") || type.equalsIgnoreCase("select"))
						{
						    value = propertyElement.getAttribute("path");
	
						    Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
	
						    if(propertyElement.hasAttribute("path_" + locale.getLanguage()))
							    value = propertyElement.getAttribute("path_" + locale.getLanguage());
						    
							if(value != null)
								value = value.replaceAll("igbr", separator);
						}
						
						property = new HashMap();
						property.put("name", name);
						property.put("path", value);
						property.put("type", type);
						
						NamedNodeMap attributes = propertyElement.getAttributes();
						for(int j=0; j<attributes.getLength(); j++)
						{
							Node attribute = attributes.item(j);
							if(attribute.getNodeName().startsWith("path_"))
								property.put(attribute.getNodeName(), attribute.getNodeValue());
						}

						List bindings = new ArrayList();
						NodeList bindingNodeList = propertyElement.getElementsByTagName("binding");
						//logger.info("bindingNodeList:" + bindingNodeList.getLength());
						for(int j=0; j < bindingNodeList.getLength(); j++)
						{
							Element bindingElement = (Element)bindingNodeList.item(j);
							String entityName = bindingElement.getAttribute("entity");
							String entityId = bindingElement.getAttribute("entityId");
							String assetKey = bindingElement.getAttribute("assetKey");
							//logger.info("Binding found:" + entityName + ":" + entityId);
							
							ComponentBinding componentBinding = new ComponentBinding();
							//componentBinding.setId(new Integer(id));
							//componentBinding.setComponentId(componentId);
							componentBinding.setEntityClass(entity);
							componentBinding.setEntityId(new Integer(entityId));
							componentBinding.setAssetKey(assetKey);
							//componentBinding.setBindingPath(path);
							
							bindings.add(componentBinding);
							/*
							if(entityName.equalsIgnoreCase("Content"))
							{
								bindings.add(entityId);
							}
							else
							{
								bindings.add(entityId); 
							}
							*/ 
						}
	
						property.put("bindings", bindings);
					}
					
					if(property != null && contentVersionIdList.size() > 0)
			        {
					    Set groups = new HashSet();
						Iterator contentVersionIdListIterator = contentVersionIdList.iterator();
					    while(contentVersionIdListIterator.hasNext())
					    {
							Integer contentVersionId = (Integer)contentVersionIdListIterator.next();
							try
							{
								ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, this.templateController.getDatabase());
							    groups.add("contentVersion_" + contentVersionId);
							    groups.add("content_" + contentVersionVO.getContentId());
							}
							catch (Exception e) 
							{
								logger.error("Could not load version...");
							}
					    }
					    
					    groups.add("siteNode_" + templateController.getSiteNodeId());
					    
					    if(groups.size() < 20)
					    {
						    CacheController.cacheObjectInAdvancedCacheWithGroupsAsSet("componentPropertyCache", key, property, groups, true);
						    CacheController.cacheObjectInAdvancedCacheWithGroupsAsSet("componentPropertyVersionIdCache", versionKey, contentVersionIdList, groups, true);
					    }
			        }
					//TODO - TEST
					/*
					else
					{
						if(property == null && contentVersionIdList.size() > 0)
						{
						    Set groups = new HashSet();
							Iterator contentVersionIdListIterator = contentVersionIdList.iterator();
						    while(contentVersionIdListIterator.hasNext())
						    {
								Integer contentVersionId = (Integer)contentVersionIdListIterator.next();
								//if(contentVersionId != null)
								//{
									ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, this.templateController.getDatabase());
								    groups.add("contentVersion_" + contentVersionId);
								    groups.add("content_" + contentVersionVO.getContentId());
								//}
							}
						    
							if(templateController.getDeliveryContext().getUsedContents().contains("selectiveCacheUpdateNonApplicable"))
			    				groups.add("selectiveCacheUpdateNonApplicable");

//						  	TODO - TEST - NOT SAFE							
						    CacheController.cacheObjectInAdvancedCacheWithGroupsAsSet("componentPropertyCache", key, new NullObject(), groups, true);
						    CacheController.cacheObjectInAdvancedCacheWithGroupsAsSet("componentPropertyVersionIdCache", versionKey, contentVersionIdList, groups, true);
						}
						else
						{
//							TODO - TEST - NOT SAFE
							CacheController.cacheObjectInAdvancedCache("componentPropertyCache", key, new NullObject(), new String[]{}, false);
						}
					}
					*/	
				}							
			}
		}
		catch(Exception e)
		{
			logger.warn("Error getting property:" + e.getMessage(), e);
		}
		
		Iterator contentVersionIdListIterator = contentVersionIdList.iterator();
		while(contentVersionIdListIterator.hasNext())
		{
			Integer currentContentVersionId = (Integer)contentVersionIdListIterator.next();
			templateController.getDeliveryContext().addUsedContentVersion("contentVersion_" + currentContentVersionId);
		}

		return property;
	}

	/**
	 * This method returns a url to the given page. The url is composed of siteNode, language and content
	 */

	public String getPageUrl(Integer siteNodeId, Integer languageId, Integer contentId)
	{
		String pageUrl = this.templateController.getPageUrl(siteNodeId, languageId, contentId);
		
		return pageUrl;
	}

		
	public String getPageNavTitle(Integer siteNodeId)
	{
		String navTitle = "";

		navTitle = templateController.getPageNavTitle(siteNodeId);
	
		return navTitle;
	}
	
	
	/**
	 * This method fetches the component named component property. If not available on the current page metainfo we go up recursive.
	 */
	
	private Map getComponentProperty(String propertyName, boolean useInheritance, boolean useStructureInheritance, Set contentVersionIdList, boolean useRepositoryInheritance) throws Exception
	{
		Map property = (Map)this.infoGlueComponent.getProperties().get(propertyName);
    	//Map property = getInheritedComponentProperty(this.templateController, templateController.getSiteNodeId(), this.templateController.getLanguageId(), this.templateController.getContentId(), this.infoGlueComponent.getId(), propertyName, contentVersionIdList);
		
		if(useInheritance)
		{
			try
			{
				if(property == null)
				{
				    NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(this.templateController.getSiteNodeId(), this.templateController.getLanguageId(), this.templateController.getContentId());
					
				    List usedRepositoryIds = new ArrayList();
					SiteNodeVO parentSiteNodeVO = nodeDeliveryController.getSiteNodeVO(templateController.getDatabase(), this.templateController.getSiteNodeId());
				    while(property == null && parentSiteNodeVO != null)
					{				    	
				    	usedRepositoryIds.add(parentSiteNodeVO.getRepositoryId());
				    	property = getInheritedComponentProperty(this.templateController, parentSiteNodeVO.getId(), this.templateController.getLanguageId(), this.templateController.getContentId(), this.infoGlueComponent.getId(), propertyName, contentVersionIdList);
				    	if(!useStructureInheritance)
				    		break;
				    	
					    SiteNodeVO newParentSiteNodeVO = nodeDeliveryController.getParentSiteNode(templateController.getDatabase(), parentSiteNodeVO.getId());
					
					    if(newParentSiteNodeVO == null && useRepositoryInheritance)
						{
						    Integer parentRepositoryId = this.templateController.getParentRepositoryId(parentSiteNodeVO.getRepositoryId());
						    if(logger.isInfoEnabled())
						    {
							    logger.info("parentRepositoryId:" + parentRepositoryId);
							    logger.info("allready used:" + usedRepositoryIds.contains(parentRepositoryId) + ":" + parentRepositoryId);
						    }
						    
						    if(parentRepositoryId != null && !usedRepositoryIds.contains(parentRepositoryId))
						    {
						        newParentSiteNodeVO = this.templateController.getRepositoryRootSiteNode(parentRepositoryId);
						        if(newParentSiteNodeVO != null)
						        	usedRepositoryIds.add(newParentSiteNodeVO.getRepositoryId());
							}
						}
						
						parentSiteNodeVO = newParentSiteNodeVO;
					}
				}
			}
			catch(Exception e)
			{
				logger.warn("Could not get componentProperty:" + propertyName + ":" + this.templateController.getSiteNodeId() + " / " + this.templateController.getLanguageId() + ":" + e.getMessage(), e);
			}
		}

        //logger.info("Done..." + propertyName);

		return property;
	}

	/**
	 * This method fetches the component named component property. If not available on the sent in page metainfo we go up recursive.
	 */
	
	private Map getComponentProperty(Integer siteNodeId, String propertyName, boolean useInheritance) throws Exception
	{
	    Set contentVersionIdList = new HashSet();

		//Map property = (Map)this.infoGlueComponent.getProperties().get(propertyName);
		//logger.info("property1:" + property);
		Map property = getInheritedComponentProperty(this.templateController, siteNodeId, this.templateController.getLanguageId(), this.templateController.getContentId(), this.infoGlueComponent.getId(), propertyName, contentVersionIdList);
		
		if(useInheritance && property == null)
		{
			try
			{
			    NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(siteNodeId, this.templateController.getLanguageId(), this.templateController.getContentId());
				SiteNodeVO parentSiteNodeVO = nodeDeliveryController.getSiteNodeVO(templateController.getDatabase(), siteNodeId);

				while(property == null && parentSiteNodeVO != null)
				{
				    property = getInheritedComponentProperty(this.templateController, parentSiteNodeVO.getId(), this.templateController.getLanguageId(), this.templateController.getContentId(), this.infoGlueComponent.getId(), propertyName, contentVersionIdList);
					
				    SiteNodeVO newParentSiteNodeVO = nodeDeliveryController.getParentSiteNode(templateController.getDatabase(), parentSiteNodeVO.getId());
				
				    if(newParentSiteNodeVO == null)
					{
					    Integer parentRepositoryId = this.templateController.getParentRepositoryId(parentSiteNodeVO.getRepositoryId());
					    logger.info("parentRepositoryId:" + parentRepositoryId);
					    if(parentRepositoryId != null)
					    {
					        newParentSiteNodeVO = this.templateController.getRepositoryRootSiteNode(parentRepositoryId);
						}
					}
					
					parentSiteNodeVO = newParentSiteNodeVO;
				}
			}
			catch(Exception e)
			{
				logger.warn("Error getting component property: " + propertyName + " on siteNode " + siteNodeId, e);
			}
		}

		Iterator contentVersionIdListIterator = contentVersionIdList.iterator();
		while(contentVersionIdListIterator.hasNext())
		{
			Integer currentContentVersionId = (Integer)contentVersionIdListIterator.next();
			templateController.getDeliveryContext().addUsedContentVersion("contentVersion_" + currentContentVersionId);
		}

		return property;
	}

	
	/**
	 * This method gets a component property from the parent to the current recursively until found.
	 */
	 
	private Map getInheritedComponentProperty(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId, Integer componentId, String propertyName, Set contentVersionIdList) throws Exception
	{
	    StringBuilder key = new StringBuilder("inherited_").append(siteNodeId).append("_").append(languageId).append("_").append(componentId).append("_").append(propertyName);
	    //StringBuilder key = new StringBuilder("inherited_").append(templateController.getSiteNodeId()).append("_").append(siteNodeId).append("_").append(languageId).append("_").append(componentId).append("_").append(propertyName);
	    String versionKey = key.toString() + "_contentVersionIds";
		Object propertyCandidate = CacheController.getCachedObjectFromAdvancedCache("componentPropertyCache", key.toString());
		Set propertyCandidateVersions = (Set)CacheController.getCachedObjectFromAdvancedCache("componentPropertyVersionIdCache", versionKey);
		Map property = null;
			
		if(propertyCandidate != null)
		{
			if(propertyCandidate instanceof NullObject)
				property = null;				
			else
				property = (Map)propertyCandidate;
				
			if(propertyCandidateVersions != null)
			{
				contentVersionIdList.addAll(propertyCandidateVersions);
				
				try
				{
					Iterator propertyCandidateVersionsIterator = propertyCandidateVersions.iterator();
					while(propertyCandidateVersionsIterator.hasNext())
					{
						Integer currentContentVersionId = (Integer)propertyCandidateVersionsIterator.next();
						templateController.getDeliveryContext().addUsedContentVersion("contentVersion_" + currentContentVersionId);
					}
				}
				catch(Exception e)
				{
					logger.warn("Got synchronize error getting inherited component property.");
					templateController.getDeliveryContext().addUsedContentVersion("selectiveCacheUpdateNonApplicable");
				}
			}
		}
		else
		{
			String inheritedPageComponentsXML = getPageComponentsString(templateController, siteNodeId, languageId, contentId, contentVersionIdList);
			if(logger.isDebugEnabled())
			{
				logger.info("Checking for property " + propertyName + " on siteNodeId " + siteNodeId);
				logger.info("Have to fetch property from XML...:" + contentVersionIdList.size());
			}			
			
			if(inheritedPageComponentsXML != null && inheritedPageComponentsXML.length() > 0)
			{
				property = parseProperties(inheritedPageComponentsXML, componentId, propertyName, siteNodeId, languageId);
			}
			
			if(property != null && contentVersionIdList.size() > 0)
	        {
			    Set groups = new HashSet();
			    Iterator contentVersionIdListIterator = contentVersionIdList.iterator();
			    while(contentVersionIdListIterator.hasNext())
			    {
					Integer contentVersionId = (Integer)contentVersionIdListIterator.next();
					try
					{
				        ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, this.templateController.getDatabase());
					    groups.add("contentVersion_" + contentVersionId);
					    groups.add("content_" + contentVersionVO.getContentId());
					}
					catch (Exception e) 
					{
					    if(contentVersionId != null)
					    	groups.add("contentVersion_" + contentVersionId);
						logger.warn("Could not fetch contentVersionVO to set correct groups:" + e.getMessage());
					}
				}
			    			    
			    groups.add("siteNode_" + templateController.getSiteNodeId());
			    groups.add("siteNode_" + siteNodeId);

			    //TODO - TEST - NOT SAFE
			    CacheController.cacheObjectInAdvancedCacheWithGroupsAsSet("componentPropertyCache", key, property, groups, true);
			    CacheController.cacheObjectInAdvancedCacheWithGroupsAsSet("componentPropertyVersionIdCache", versionKey, contentVersionIdList, groups, true);
		    }
			else
			{
				if(property == null && contentVersionIdList.size() > 0)
				{
				    Set groups = new HashSet();
					Iterator contentVersionIdListIterator = contentVersionIdList.iterator();
				    while(contentVersionIdListIterator.hasNext())
				    {
						Integer contentVersionId = (Integer)contentVersionIdListIterator.next();
						try
						{
							ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, this.templateController.getDatabase());
						    groups.add("contentVersion_" + contentVersionId);
						    groups.add("content_" + contentVersionVO.getContentId());
						}
						catch (Exception e) 
						{
						    if(contentVersionId != null)
						    	groups.add("contentVersion_" + contentVersionId);
							logger.warn("Could not fetch contentVersionVO to set correct groups:" + e.getMessage());
						}
					}
				    
				    groups.add("siteNode_" + templateController.getSiteNodeId());
				    groups.add("siteNode_" + siteNodeId);

//				  	TODO - TEST - NOT SAFE
				    CacheController.cacheObjectInAdvancedCacheWithGroupsAsSet("componentPropertyCache", key.toString(), new NullObject(), groups, true);
				    CacheController.cacheObjectInAdvancedCacheWithGroupsAsSet("componentPropertyVersionIdCache", versionKey, contentVersionIdList, groups, true);
				}
				else
				{
//					TODO - TEST - NOT SAFE
					CacheController.cacheObjectInAdvancedCache("componentPropertyCache", key.toString(), new NullObject(), new String[]{}, false);
				}
			}
		}
				
		return property;
	}

	private static String parser = null;
	
	private Map parseProperties(String inheritedPageComponentsXML, Integer componentId, String propertyName, Integer siteNodeId, Integer languageId) throws Exception
	{
		if(parser == null)
		{
			parser = CmsPropertyHandler.getPropertiesParser();
		}
		
		if(parser != null && parser.equalsIgnoreCase("xalan"))
			return parsePropertiesWithXalan(inheritedPageComponentsXML, componentId, propertyName, siteNodeId, languageId);
		else if(parser != null && parser.equalsIgnoreCase("dom4j"))
			return parsePropertiesWithDOM4J(inheritedPageComponentsXML, componentId, propertyName, siteNodeId, languageId);
		else
			return parsePropertiesWithXPP3(inheritedPageComponentsXML, componentId, propertyName, siteNodeId, languageId);
	}

	private Map parsePropertiesWithXalan(String inheritedPageComponentsXML, Integer componentId, String propertyName, Integer siteNodeId, Integer languageId) throws Exception
	{
		Map property = null;
		
		Document document = XMLHelper.readDocumentFromByteArray(inheritedPageComponentsXML.getBytes("UTF-8"));
		String propertyXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + propertyName + "']";
		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), propertyXPath);
		
		//If not found on the same component id - let's check them all and use the first we find.
		if(anl == null || anl.getLength() == 0)
		{
			String globalPropertyXPath = "(//component/properties/property[@name='" + propertyName + "'])[1]";
			anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), globalPropertyXPath);
		}			
		
		for(int i=0; i < anl.getLength(); i++)
		{
			Element propertyElement = (Element)anl.item(i);
			
			String name		= propertyElement.getAttribute("name");
			String type		= propertyElement.getAttribute("type");
			String entity 	= propertyElement.getAttribute("entity");
			boolean isMultipleBinding = new Boolean(propertyElement.getAttribute("multiple")).booleanValue();
			
			//logger.info("name:" + name);
			//logger.info("type:" + type);
			//logger.info("entity:" + entity);
			//logger.info("isMultipleBinding:" + isMultipleBinding);
			
			String value = null;
			
			if(type.equalsIgnoreCase("textfield") || type.equalsIgnoreCase("textarea") || type.equalsIgnoreCase("select"))
			{
			    value = propertyElement.getAttribute("path");

			    Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
				Locale masterLocale = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), siteNodeId).getLocale();
				
			    if(propertyElement.hasAttribute("path_" + locale.getLanguage()))
				    value = propertyElement.getAttribute("path_" + locale.getLanguage());

			    if((value == null || value.equals("")) && propertyElement.hasAttribute("path_" + masterLocale.getLanguage()))
			        value = propertyElement.getAttribute("path_" + masterLocale.getLanguage());
			    
				if(value != null)
					value = value.replaceAll("igbr", separator);
			}
			else
			{
			    value = getComponentPropertyValue(inheritedPageComponentsXML, componentId, languageId, name);
			}
			
			
			property = new HashMap();
			property.put("name", name);
			//property.put("path", "Inherited");
			property.put("path", value);
			property.put("type", type);
			
			NamedNodeMap attributes = propertyElement.getAttributes();
			for(int j=0; j<attributes.getLength(); j++)
			{
				Node attribute = attributes.item(j);
				if(attribute.getNodeName().startsWith("path_"))
					property.put(attribute.getNodeName(), attribute.getNodeValue());
			}

			List bindings = new ArrayList();
			NodeList bindingNodeList = propertyElement.getElementsByTagName("binding");
			//logger.info("bindingNodeList:" + bindingNodeList.getLength());
			for(int j=0; j < bindingNodeList.getLength(); j++)
			{
				Element bindingElement = (Element)bindingNodeList.item(j);
				String entityName = bindingElement.getAttribute("entity");
				String entityId = bindingElement.getAttribute("entityId");
				String assetKey = bindingElement.getAttribute("assetKey");
				//logger.info("Binding found:" + entityName + ":" + entityId);
				
				ComponentBinding componentBinding = new ComponentBinding();
				//componentBinding.setId(new Integer(id));
				//componentBinding.setComponentId(componentId);
				componentBinding.setEntityClass(entity);
				componentBinding.setEntityId(new Integer(entityId));
				componentBinding.setAssetKey(assetKey);
				//componentBinding.setBindingPath(path);
				
				bindings.add(componentBinding);
				/*
				if(entityName.equalsIgnoreCase("Content"))
				{
					//logger.info("Content added:" + entityName + ":" + entityId);
					bindings.add(entityId);
				}
				else
				{
					//logger.info("SiteNode added:" + entityName + ":" + entityId);
					bindings.add(entityId); 
				}
				*/ 
			}

			property.put("bindings", bindings);
		}
		
		return property;
	}


	private Map parsePropertiesWithDOM4J(String inheritedPageComponentsXML, Integer componentId, String propertyName, Integer siteNodeId, Integer languageId) throws Exception
	{
		Map property = null;
		
		org.dom4j.Document document = domBuilder.getDocument(inheritedPageComponentsXML);
		String propertyXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + propertyName + "']";
		
		List anl = document.getRootElement().selectNodes(propertyXPath);
		//If not found on the same component id - let's check them all and use the first we find.
		if(anl == null || anl.size() == 0)
		{
			String globalPropertyXPath = "(//component/properties/property[@name='" + propertyName + "'])[1]";
			anl = document.getRootElement().selectNodes(globalPropertyXPath);
		}			
		
		Iterator anlIterator = anl.iterator();
		while(anlIterator.hasNext())
		{
			org.dom4j.Element propertyElement = (org.dom4j.Element)anlIterator.next();
			
			String name		= propertyElement.attributeValue("name");
			String type		= propertyElement.attributeValue("type");
			String entity 	= propertyElement.attributeValue("entity");
			boolean isMultipleBinding = new Boolean(propertyElement.attributeValue("multiple")).booleanValue();
			
			String value = null;
			
			if(type.equalsIgnoreCase("textfield") || type.equalsIgnoreCase("textarea") || type.equalsIgnoreCase("select"))
			{
			    value = propertyElement.attributeValue("path");

			    Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
				Locale masterLocale = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), siteNodeId).getLocale();
				
			    if(propertyElement.attributeValue("path_" + locale.getLanguage()) != null && !propertyElement.attributeValue("path_" + locale.getLanguage()).equals(""))
				    value = propertyElement.attributeValue("path_" + locale.getLanguage());

			    if((value == null || value.equals("")) && (propertyElement.attributeValue("path_" + masterLocale.getLanguage()) != null && !propertyElement.attributeValue("path_" + masterLocale.getLanguage()).equals("")))
			        value = propertyElement.attributeValue("path_" + masterLocale.getLanguage());
			    
				if(value != null)
					value = value.replaceAll("igbr", separator);
			}
			else
			{
				Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);

				//Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
				String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']";
				//logger.info("componentXPath:" + componentXPath);
				List propertyNL = document.getRootElement().selectNodes(componentXPath);
				Iterator propertyNLIterator = propertyNL.iterator();
				while(propertyNLIterator.hasNext())
				{
					org.dom4j.Element propertyElement2 = (org.dom4j.Element)propertyNLIterator.next();
					
					String id 			= propertyElement2.attributeValue("type");
					String path 		= propertyElement2.attributeValue("path");
					
					if(propertyElement2.attributeValue("path_" + locale.getLanguage()) != null && !propertyElement2.attributeValue("path_" + locale.getLanguage()).equals(""))
						path = propertyElement2.attributeValue("path_" + locale.getLanguage());
					
					value = path;
					
					if(value != null)
						value = value.replaceAll("igbr", separator);
				}
				
			    //value = getComponentPropertyValue(inheritedPageComponentsXML, componentId, languageId, name);
			}
			
			
			property = new HashMap();
			property.put("name", name);
			//property.put("path", "Inherited");
			property.put("path", value);
			property.put("type", type);
			
			List attributes = propertyElement.attributes();
			Iterator attributesIterator = attributes.iterator();
			while(attributesIterator.hasNext())
			{
				Attribute attribute = (Attribute)attributesIterator.next();
				if(attribute.getName().startsWith("path_"))
					property.put(attribute.getName(), attribute.getValue());
			}

			List bindings = new ArrayList();
			List bindingNodeList = propertyElement.elements("binding");
			Iterator bindingNodeListIterator = bindingNodeList.iterator();
			while(bindingNodeListIterator.hasNext())
			{
				org.dom4j.Element bindingElement = (org.dom4j.Element)bindingNodeListIterator.next();
				String entityName = bindingElement.attributeValue("entity");
				String entityId = bindingElement.attributeValue("entityId");
				String assetKey = bindingElement.attributeValue("assetKey");
				//logger.info("Binding found:" + entityName + ":" + entityId);
				
				ComponentBinding componentBinding = new ComponentBinding();
				//componentBinding.setId(new Integer(id));
				//componentBinding.setComponentId(componentId);
				componentBinding.setEntityClass(entity);
				componentBinding.setEntityId(new Integer(entityId));
				componentBinding.setAssetKey(assetKey);
				//componentBinding.setBindingPath(path);
				
				bindings.add(componentBinding);
				/*
				if(entityName.equalsIgnoreCase("Content"))
				{
					//logger.info("Content added:" + entityName + ":" + entityId);
					bindings.add(entityId);
				}
				else
				{
					//logger.info("SiteNode added:" + entityName + ":" + entityId);
					bindings.add(entityId); 
				} 
				*/
			}

			property.put("bindings", bindings);
		}
		
		return property;
	}

	Map cachedXPathObjects = new HashMap();
	
	private Map parsePropertiesWithXPP3(String inheritedPageComponentsXML, Integer componentId, String propertyName, Integer siteNodeId, Integer languageId) throws Exception
	{
		Map property = null;
		
        XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance();
        XmlDocument doc = builder.parseReader(new StringReader( inheritedPageComponentsXML ) );
        
		String propertyXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + propertyName + "']";
		
		Xb1XPath xpathObject = (Xb1XPath)cachedXPathObjects.get(propertyXPath);
        if(xpathObject == null)
        {
        	xpathObject = new Xb1XPath( propertyXPath );
        	cachedXPathObjects.put(propertyXPath, xpathObject);
        }

        List anl = xpathObject.selectNodes( doc );

		//If not found on the same component id - let's check them all and use the first we find.
		if(anl == null || anl.size() == 0)
		{
			String globalPropertyXPath = "(//component/properties/property[@name='" + propertyName + "'])[1]";
			
			Xb1XPath globalXpathObject = new Xb1XPath( globalPropertyXPath );
	        anl = globalXpathObject.selectNodes( doc );
		}			

		Iterator anlIterator = anl.iterator();
		while(anlIterator.hasNext())
		{
			XmlElement infosetItem = (XmlElement)anlIterator.next();

			String name		= infosetItem.getAttributeValue(infosetItem.getNamespaceName(), "name");
			String type		= infosetItem.getAttributeValue(infosetItem.getNamespaceName(), "type");
			String entity 	= infosetItem.getAttributeValue(infosetItem.getNamespaceName(), "entity");
			boolean isMultipleBinding = new Boolean(infosetItem.getAttributeValue(infosetItem.getNamespaceName(), "multiple")).booleanValue();
			
			String value = null;
			
			if(type.equalsIgnoreCase("textfield") || type.equalsIgnoreCase("textarea") || type.equalsIgnoreCase("select"))
			{
			    value = infosetItem.getAttributeValue(infosetItem.getNamespaceName(), "path");

			    Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
				Locale masterLocale = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), siteNodeId).getLocale();
				
			    if(infosetItem.attribute("path_" + locale.getLanguage()) != null)
				    value = infosetItem.getAttributeValue(infosetItem.getNamespaceName(), "path_" + locale.getLanguage());

			    if((value == null || value.equals("")) && infosetItem.attribute("path_" + masterLocale.getLanguage()) != null)
			        value = infosetItem.getAttributeValue(infosetItem.getNamespaceName(), "path_" + masterLocale.getLanguage());
			    
				if(value != null)
					value = value.replaceAll("igbr", separator);
			}
			else
			{
				Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);

				String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']";

				Xb1XPath globalXpathObject = new Xb1XPath( componentXPath );
				List propertyNL = globalXpathObject.selectNodes( doc );

				Iterator propertyNLIterator = propertyNL.iterator();
				while(propertyNLIterator.hasNext())
				{
					XmlElement propertyElement2 = (XmlElement)propertyNLIterator.next();
					
					String id 			= propertyElement2.getAttributeValue(infosetItem.getNamespaceName(), "type");
					String path 		= propertyElement2.getAttributeValue(infosetItem.getNamespaceName(), "path");
					
					if(propertyElement2.attribute("path_" + locale.getLanguage()) != null)
						path = propertyElement2.getAttributeValue(infosetItem.getNamespaceName(), "path_" + locale.getLanguage());
					
					value = path;
					
					if(value != null)
						value = value.replaceAll("igbr", separator);
				}
			    //value = getComponentPropertyValue(inheritedPageComponentsXML, componentId, languageId, name);
			}
			
			property = new HashMap();
			property.put("name", name);
			//property.put("path", "Inherited");
			property.put("path", value);
			property.put("type", type);

			Iterator attributesIterator = infosetItem.attributes();
			while(attributesIterator.hasNext())
			{
				XmlAttribute attribute = (XmlAttribute)attributesIterator.next();
				if(attribute.getName().startsWith("path_"))
					property.put(attribute.getName(), attribute.getValue());
			}

			List bindings = new ArrayList();
			
			Iterator bindingNodeListIterator = infosetItem.elements(infosetItem.getNamespace(), "binding").iterator();
			while(bindingNodeListIterator.hasNext())
			{
				XmlElement bindingElement = (XmlElement)bindingNodeListIterator.next();
				String entityName = bindingElement.getAttributeValue(infosetItem.getNamespaceName(), "entity");
				String entityId = bindingElement.getAttributeValue(infosetItem.getNamespaceName(), "entityId");
				String assetKey = bindingElement.getAttributeValue(infosetItem.getNamespaceName(), "assetKey");
				//logger.info("Binding found:" + entityName + ":" + entityId);

				ComponentBinding componentBinding = new ComponentBinding();
				//componentBinding.setId(new Integer(id));
				//componentBinding.setComponentId(componentId);
				componentBinding.setEntityClass(entity);
				componentBinding.setEntityId(new Integer(entityId));
				componentBinding.setAssetKey(assetKey);
				//componentBinding.setBindingPath(path);
				
				bindings.add(componentBinding);
				
				/*
				if(entityName.equalsIgnoreCase("Content"))
				{
					//logger.info("Content added:" + entityName + ":" + entityId);
					bindings.add(entityId);
				}
				else
				{
					//logger.info("SiteNode added:" + entityName + ":" + entityId);
					bindings.add(entityId); 
				} 
				*/
			}
			
			property.put("bindings", bindings);
		}
		
		return property;
	}

	
	/**
	 * This method fetches the components named component property. If not available on the sent in page metainfo we go up recursive.
	 */
	
	private List getComponentProperties(Integer siteNodeId, String propertyName, boolean useInheritance, boolean skipRepositoryInheritance) throws Exception
	{
		List properties = getInheritedComponentProperties(this.templateController, siteNodeId, this.templateController.getLanguageId(), this.templateController.getContentId(), this.infoGlueComponent.getId(), propertyName);
		
		if(useInheritance)
		{
			try
			{
			    NodeDeliveryController nodeDeliveryController = NodeDeliveryController.getNodeDeliveryController(siteNodeId, this.templateController.getLanguageId(), this.templateController.getContentId());
			
				SiteNodeVO parentSiteNodeVO = nodeDeliveryController.getSiteNodeVO(templateController.getDatabase(), siteNodeId);
				while(properties == null || properties.size() == 0 && parentSiteNodeVO != null)
				{
					properties = getInheritedComponentProperties(this.templateController, parentSiteNodeVO.getId(), this.templateController.getLanguageId(), this.templateController.getContentId(), this.infoGlueComponent.getId(), propertyName);
					
				    SiteNodeVO newParentSiteNodeVO = nodeDeliveryController.getParentSiteNode(templateController.getDatabase(), parentSiteNodeVO.getId());
				
				    if(newParentSiteNodeVO == null && !skipRepositoryInheritance)
					{
					    Integer parentRepositoryId = this.templateController.getParentRepositoryId(parentSiteNodeVO.getRepositoryId());
					    if(parentRepositoryId != null)
					    {
					        newParentSiteNodeVO = this.templateController.getRepositoryRootSiteNode(parentRepositoryId);
						}
					}
					
					parentSiteNodeVO = newParentSiteNodeVO;
				}
			}
			catch(Exception e)
			{
				logger.warn("Error getting component properties: " + propertyName + " on siteNode " + siteNodeId, e);
			}
		}

		return properties;
	}

	
	/**
	 * This method gets a component property from the parent to the current recursively until found.
	 */
	 
	private List getInheritedComponentProperties(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId, Integer componentId, String propertyName) throws Exception
	{
		Set contentVersionIdList = new HashSet();
	    
	    //logger.info("Checking for property " + propertyName + " on siteNodeId " + siteNodeId);
		String inheritedPageComponentsXML = getPageComponentsString(templateController, siteNodeId, languageId, contentId, contentVersionIdList);

	    String key = "all_" + siteNodeId + "_" + languageId + "_" + propertyName;
	    String versionKey = key + "_contentVersionIds";
		List properties = (List)CacheController.getCachedObjectFromAdvancedCache("componentPropertyCache", key);
		Set propertyCandidateVersions = (Set)CacheController.getCachedObjectFromAdvancedCache("componentPropertyVersionIdCache", versionKey);
		
		if(properties != null)
		{
			//TODO - add used content version perhaps??
			if(propertyCandidateVersions != null)
				contentVersionIdList.addAll(propertyCandidateVersions);
		}
		else
		{
			properties = new ArrayList();
			
			if(inheritedPageComponentsXML != null && inheritedPageComponentsXML.length() > 0)
			{
				Document document = XMLHelper.readDocumentFromByteArray(inheritedPageComponentsXML.getBytes("UTF-8"));
				
				String globalPropertyXPath = "//component/properties/property[@name='" + propertyName + "']";
				NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), globalPropertyXPath);
				
				for(int i=0; i < anl.getLength(); i++)
				{
					Element propertyElement = (Element)anl.item(i);
					//logger.info(XMLHelper.serializeDom(propertyElement, new StringBuffer()));
					//logger.info("YES - we read the property...");		
					
					String name		= propertyElement.getAttribute("name");
					String type		= propertyElement.getAttribute("type");
					String entity 	= propertyElement.getAttribute("entity");
					boolean isMultipleBinding = new Boolean(propertyElement.getAttribute("multiple")).booleanValue();
					
					//logger.info("name:" + name);
					//logger.info("type:" + type);
					//logger.info("entity:" + entity);
					//logger.info("isMultipleBinding:" + isMultipleBinding);
					
					String value = null;
					
					if(type.equalsIgnoreCase("textfield") || type.equalsIgnoreCase("textarea") || type.equalsIgnoreCase("select"))
					{
					    value = propertyElement.getAttribute("path");
	
					    Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);
	
					    if(propertyElement.hasAttribute("path_" + locale.getLanguage()))
						    value = propertyElement.getAttribute("path_" + locale.getLanguage());
					    
						if(value != null)
							value = value.replaceAll("igbr", separator);
					}
					else
					{
						value = getComponentPropertyValue(propertyElement, languageId, name);
					}					

					Map property = new HashMap();
					property.put("name", name);
					property.put("path", value);
					property.put("type", type);
					
					NamedNodeMap attributes = propertyElement.getAttributes();
					for(int j=0; j<attributes.getLength(); j++)
					{
						Node attribute = attributes.item(j);
						if(attribute.getNodeName().startsWith("path_"))
							property.put(attribute.getNodeName(), attribute.getNodeValue());
					}

					List bindings = new ArrayList();
					NodeList bindingNodeList = propertyElement.getElementsByTagName("binding");
					for(int j=0; j < bindingNodeList.getLength(); j++)
					{
						Element bindingElement = (Element)bindingNodeList.item(j);
						String entityName = bindingElement.getAttribute("entity");
						String entityId = bindingElement.getAttribute("entityId");
						String assetKey = bindingElement.getAttribute("assetKey");
						
						ComponentBinding componentBinding = new ComponentBinding();
						//componentBinding.setId(new Integer(id));
						//componentBinding.setComponentId(componentId);
						componentBinding.setEntityClass(entity);
						componentBinding.setEntityId(new Integer(entityId));
						componentBinding.setAssetKey(assetKey);
						//componentBinding.setBindingPath(path);

						/*
						if(entityName.equalsIgnoreCase("Content"))
						{
							//logger.info("Content added:" + entityName + ":" + entityId);
							bindings.add(entityId);
						}
						else
						{
							//logger.info("SiteNode added:" + entityName + ":" + entityId);
							bindings.add(entityId); 
						} 
						*/
					}
	
					property.put("bindings", bindings);

					properties.add(property);
				}
			}
			
			if(properties != null && contentVersionIdList.size() > 0)
	        {
			    Set groups = new HashSet();
				Iterator contentVersionIdListIterator = contentVersionIdList.iterator();
			    while(contentVersionIdListIterator.hasNext())
			    {
					Integer contentVersionId = (Integer)contentVersionIdListIterator.next();
					ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, this.templateController.getDatabase());

				    groups.add("contentVersion_" + contentVersionId);
				    groups.add("content_" + contentVersionVO.getContentId());
				}
				
			    groups.add("siteNode_" + templateController.getSiteNodeId());

			    if(groups.size() < 20)
			    {
				    CacheController.cacheObjectInAdvancedCacheWithGroupsAsSet("componentPropertyCache", key, properties, groups, true);
				    CacheController.cacheObjectInAdvancedCacheWithGroupsAsSet("componentPropertyVersionIdCache", versionKey, contentVersionIdList, groups, true);
			    }
	        }
		}
		
		Iterator contentVersionIdListIterator = contentVersionIdList.iterator();
		while(contentVersionIdListIterator.hasNext())
		{
			Integer currentContentVersionId = (Integer)contentVersionIdListIterator.next();
			templateController.getDeliveryContext().addUsedContentVersion("contentVersion_" + currentContentVersionId);
		}

		return properties;
	}

	/**
	 * This method fetches the template-string.
	 */
    
	private String getComponentPropertiesString(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
	{
		String template = null;
    	
		try
		{
			template = templateController.getContentAttribute(contentId, "ComponentStructure", true);

			if(template == null)
				throw new SystemException("There was no component properties bound to this page which makes it impossible to render.");	
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}

		return template;
	}
	
	/**
	 * This method returns a value for a property if it's set. The value is collected in the
	 * properties for the page.
	 */
	
	public String getComponentPropertyValue(Integer siteNodeId, Integer componentId, Integer languageId, Integer contentId, String name) throws Exception
	{
        Set contentVersionIdList = new HashSet();

		String componentXML = getPageComponentsString(templateController, siteNodeId, languageId, contentId, contentVersionIdList);

		String value = "Undefined";
		
		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);

		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
		String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']";
		//logger.info("componentXPath:" + componentXPath);
		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
		for(int i=0; i < anl.getLength(); i++)
		{
			Element property = (Element)anl.item(i);
			
			String componentContentId 	= property.getAttribute("contentId");
			String id 					= property.getAttribute("type");
			String path 				= property.getAttribute("path");
			
			if(property.hasAttribute("path_" + locale.getLanguage()))
				path = property.getAttribute("path_" + locale.getLanguage());
			
			value = path;
			
			if(value == null)
			{
				ComponentPropertyDefinition propertyDefinition = getComponentPropertyDefinition(new Integer(componentContentId), name, siteNodeId, languageId, contentId, templateController.getDatabase(), templateController.getPrincipal());
				if(propertyDefinition != null)
					value = propertyDefinition.getDefaultValue();
			}
		}

		if(value != null)
			value = value.replaceAll("igbr", separator);

		return value;
	}

	
	/**
	 * This method returns a value for a property if it's set. The value is collected in the
	 * properties for the page.
	 */
	
	private String getComponentPropertyValue(String componentXML, Integer componentId, Integer languageId, String name) throws Exception
	{
		String value = "Undefined";
		
		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);

		Document document = XMLHelper.readDocumentFromByteArray(componentXML.getBytes("UTF-8"));
		String componentXPath = "//component[@id=" + componentId + "]/properties/property[@name='" + name + "']";
		//logger.info("componentXPath:" + componentXPath);
		NodeList anl = org.apache.xpath.XPathAPI.selectNodeList(document.getDocumentElement(), componentXPath);
		for(int i=0; i < anl.getLength(); i++)
		{
			Element property = (Element)anl.item(i);
			
			String componentContentId 	= property.getAttribute("contentId");
			String id 					= property.getAttribute("type");
			String path 				= property.getAttribute("path");
			
			if(property.hasAttribute("path_" + locale.getLanguage()))
				path = property.getAttribute("path_" + locale.getLanguage());
			
			value = path;
			
			if(value == null)
			{
				ComponentPropertyDefinition propertyDefinition = getComponentPropertyDefinition(new Integer(componentContentId), name, templateController.getSiteNodeId(), languageId, templateController.getContentId(), templateController.getDatabase(), templateController.getPrincipal());
				if(propertyDefinition != null)
					value = propertyDefinition.getDefaultValue();
			}
		}

		if(value != null)
			value = value.replaceAll("igbr", separator);

		return value;
	}

	/**
	 * This method returns a value for a property if it's set. The value is collected in the
	 * properties for the page.
	 */
	
	private String getComponentPropertyValue(Element property, Integer languageId, String name) throws Exception
	{
		String value = "Undefined";
		
		Locale locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(templateController.getDatabase(), languageId);

		String componentContentId 	= property.getAttribute("contentId");
		String id 					= property.getAttribute("type");
		String path					= property.getAttribute("path");
		
		if(property.hasAttribute("path_" + locale.getLanguage()))
			path = property.getAttribute("path_" + locale.getLanguage());
		
		value = path;

		if(value == null)
		{
			ComponentPropertyDefinition propertyDefinition = getComponentPropertyDefinition(new Integer(componentContentId), name, templateController.getSiteNodeId(), languageId, templateController.getContentId(), templateController.getDatabase(), templateController.getPrincipal());
			if(propertyDefinition != null)
				value = propertyDefinition.getDefaultValue();
		}

		if(value != null)
			value = value.replaceAll("igbr", separator);
		
		return value;
	}

	/**
	 * This method returns all components which are on slots under the current component.
	 */
	
	public List getChildComponents()
	{
	    return getChildComponents(this.getInfoGlueComponent(), null);
	}
	
	/**
	 * This method returns all components which are on a given slots under the current component.
	 */
	
	public List getChildComponents(String slotId)
	{
	    return getChildComponents(this.getInfoGlueComponent(), slotId);
	}

	/**
	 * This method returns all components which are on a given slots under the current component.
	 */
	
	public List getChildComponents(String slotId, boolean searchRecursive, String propertyFilterStrings)
	{
		List filters = null;
		if(propertyFilterStrings != null && !propertyFilterStrings.equals(""))
		{
			filters = new ArrayList();
	        String[] filtersArray = propertyFilterStrings.split(",");
	        for(int i=0; i<filtersArray.length; i++)
	        {
	        	String nameValuePair = filtersArray[i];
	        	String[] pair = nameValuePair.split("=");
	        	String name = pair[0];
	        	String value = pair[1];
	        	Map nameValues = new HashMap();
	        	nameValues.put("name", name);
	        	nameValues.put("value", value);
	        	filters.add(nameValues);
	        }
		}
        
	    return getChildComponents(this.getInfoGlueComponent(), slotId, searchRecursive, filters);
	}

	/**
	 * This method returns all components which are on slots under the current component.
	 */
	
	public List getChildComponents(InfoGlueComponent component, String slotId)
	{
        List childComponents = new ArrayList();
        
	    List slotList = component.getSlotList();
        
        Iterator slotListIterator = slotList.iterator();
        while(slotListIterator.hasNext())
        {
            Slot slot = (Slot)slotListIterator.next();
            if(slotId == null || slotId.equalsIgnoreCase(slot.getId()))
            {
                childComponents.addAll(slot.getComponents());
            }
        }
        
        return childComponents;
	}

	/**
	 * This method returns all components which are on slots under the current component - potentially recursive and filtered.
	 */
	
	public List getChildComponents(InfoGlueComponent component, String slotId, boolean searchRecursive, List filters)
	{
        List childComponents = new ArrayList();
        
	    List slotList = component.getSlotList();
        
        Iterator slotListIterator = slotList.iterator();
        while(slotListIterator.hasNext())
        {
            Slot slot = (Slot)slotListIterator.next();
            if(slotId == null || slotId.equalsIgnoreCase(slot.getId()))
            {
            	List slotChildComponents = slot.getComponents();
                childComponents.addAll(slotChildComponents);
            	if(searchRecursive)
            	{
	                Iterator slotChildComponentsIterator = slotChildComponents.iterator();
	            	while(slotChildComponentsIterator.hasNext())
	            	{
	            		InfoGlueComponent slotChildComponent = (InfoGlueComponent)slotChildComponentsIterator.next();
	            		List subChildComponents = getChildComponents(slotChildComponent, slotId, searchRecursive, filters);
	            		childComponents.addAll(subChildComponents);
	            	}
            	}
            }
        }
                
        if(filters != null && filters.size() > 0)
        {
	        Iterator childComponentsIterator = childComponents.iterator();
	        while(childComponentsIterator.hasNext())
	        {
	        	InfoGlueComponent childComponent = (InfoGlueComponent)childComponentsIterator.next();
	        	boolean deleteComponent = true;
	        	Iterator filtersIterator = filters.iterator();
	        	while(filtersIterator.hasNext())
	        	{
	        		Map filter = (Map)filtersIterator.next();
	        		String name = (String)filter.get("name");
	        		String value = (String)filter.get("value");
	        								
	        		try
	        		{
		            	String propertyValue = getComponentPropertyValue(templateController.getSiteNodeId(), childComponent.getId(), templateController.getLanguageId(), templateController.getContentId(), name);
		            	if(propertyValue.trim().equalsIgnoreCase(value.trim()))
		            	{
		            		deleteComponent = false;
		            		break;
		            	}
	        		}
	        		catch (Exception e) 
	        		{
	    				logger.error("Error filtering child components: " + e.getMessage(), e);
	        		}
	        	}
	        	
	        	if(deleteComponent)
	        		childComponentsIterator.remove();
	        }
        }
        
        return childComponents;
	}

	/**
	 * This method fetches the pageComponent structure from the metainfo content.
	 */
	    
	protected String getPageComponentsString(TemplateController templateController, Integer siteNodeId, Integer languageId, Integer contentId, Set usedContentVersionId) throws SystemException, Exception
	{ 
		if(siteNodeId == null || siteNodeId.intValue() <= 0)
			return null;
		
		String cacheName 	= "componentEditorCache";
		String cacheKey		= "pageComponentString_" + siteNodeId + "_" + languageId + "_" + contentId;
		String versionKey 	= cacheKey + "_contentVersionId";
		
		String cachedPageComponentsString = (String)CacheController.getCachedObjectFromAdvancedCache(cacheName, cacheKey);
		Set contentVersionIds = (Set)CacheController.getCachedObjectFromAdvancedCache("componentEditorVersionIdCache", versionKey);
		if(cachedPageComponentsString != null)
		{
		    if(usedContentVersionId != null && contentVersionIds != null)
		        usedContentVersionId.addAll(contentVersionIds);

			return cachedPageComponentsString;
		}
		
		String pageComponentsString = null;
    	
		NodeDeliveryController ndc = NodeDeliveryController.getNodeDeliveryController(siteNodeId, languageId, contentId);
		ContentVO contentVO = ndc.getBoundContent(templateController.getDatabase(), templateController.getPrincipal(), siteNodeId, languageId, true, "Meta information", templateController.getDeliveryContext());
		
		if(contentVO == null)
			throw new SystemException("There was no Meta Information bound to this page [" + siteNodeId + "] which makes it impossible to render.");	
		
		SiteNodeVO siteNodeVO = ndc.getSiteNodeVO(templateController.getDatabase(), siteNodeId);
		Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(templateController.getDatabase(), siteNodeVO.getRepositoryId()).getId();
		//Integer masterLanguageId = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(templateController.getDatabase(), siteNodeId).getId();
		pageComponentsString = templateController.getContentAttributeWithReturningId(contentVO.getContentId(), masterLanguageId, "ComponentStructure", true, usedContentVersionId);
		pageComponentsString = appendPagePartTemplates(pageComponentsString, templateController);

		if(pageComponentsString == null)
			throw new SystemException("There was no Meta Information bound to this page [" + siteNodeId + "] which makes it impossible to render.");	
	
		CacheController.cacheObjectInAdvancedCache(cacheName, cacheKey, pageComponentsString, null, false);
		
		if(usedContentVersionId != null && usedContentVersionId.size() > 0)
		{
		    Set groups = new HashSet();
			Iterator contentVersionIdListIterator = usedContentVersionId.iterator();
		    while(contentVersionIdListIterator.hasNext())
		    {
				Integer contentVersionId = (Integer)contentVersionIdListIterator.next();
			    ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, this.templateController.getDatabase());
			    groups.add("contentVersion_" + contentVersionId);
			    groups.add("content_" + contentVersionVO.getContentId());
			}

		    CacheController.cacheObjectInAdvancedCacheWithGroupsAsSet("componentEditorVersionIdCache", versionKey, usedContentVersionId, groups, true);
		}
		
		return pageComponentsString;
	}

	/**
	 * @return Returns the infoGlueComponent.
	 */
	
	public InfoGlueComponent getInfoGlueComponent()
	{
		return infoGlueComponent;
	}

	public Integer getIncludedComponentContentId()
	{
		return this.includedComponentContentId;
	}
	
    public boolean getUseInheritance()
    {
        return useInheritance;
    }
    
    public void setUseInheritance(boolean useInheritance)
    {
        this.useInheritance = useInheritance;
    }
    
    public boolean getUseEditOnSight()
    {
        return useEditOnSight;
    }
    
    public void setUseEditOnSight(boolean useEditOnSight)
    {
        this.useEditOnSight = useEditOnSight;
    }
    
    public boolean getThreatFoldersAsContents()
    {
        return threatFoldersAsContents;
    }
    
    public void setThreatFoldersAsContents(boolean threatFoldersAsContents)
    {
        this.threatFoldersAsContents = threatFoldersAsContents;
        this.templateController.setThreatFoldersAsContents(threatFoldersAsContents);
    }

    public void setIncludedComponentContentId(Integer includedComponentContentId)
    {
        this.includedComponentContentId = includedComponentContentId;
    }

    /**
     * Returns ComponentDeliveryContext
     * @return the ComponentDeliveryContext
     */
    public ComponentDeliveryContext getComponentDeliveryContext()
    {
        return componentDeliveryContext;
    }
    

    /**
     * Returns a map value from a content attribute bound by a propertie. This
     * method is good for global labels and translations, instead of having
     * property files on disk you have them in the cms.
     * @param propertyName a bound content
     * @param attributeName a attribute in the content
     * @param keyName keyname in the propertyu, the text should be formed as a
     *            standard java property file (key=val)
     * @return The value of the keyname in the content attribute or empty string
     *         if none found.
     * @author Per Jonsson - per.jonsson@it-huset.se
     */
    public String getContentAttributeMapValue( String propertyName, String attributeName, String keyName )
    {
        String mapValue = "";
        try
        {
            mapValue = (String)Support.convertTextToProperties(
                    getContentAttribute( propertyName, attributeName, true, true ) ).get( keyName );
        }
        catch ( Exception e )
        {
            logger.error( "An error occurred trying to get getContentAttributeMapValue ( " + propertyName +", " + attributeName + ", " + keyName + " ) ; "+ e.getMessage(), e );
        }
        if ( mapValue == null )
        {
            mapValue = "";
        }
        return mapValue;
    }

    /**
     * Renders a text to a PNG file, the preferences of the text rendering is
     * taken from the deliver.properties. ie. fontrender.fontName=Arial
     * @param text The text to render
     * @return an asseturl to the rendered text or an empty string if something
     *         went wrong
     * @author Per Jonsson - per.jonsson@it-huset.se
     */
    public String getRenderedTextUrl( String text )
    {
        return templateController.getRenderedTextUrl( text, null );
    }

    /**
     * Renders a text to a PNG file, the preferences of the text rendering is
     * taken from the bound component content of the property. The content is
     * iterated and extract all matching properties. ie. fontName, fontsize etc.
     * @param fontConfigPropertyName name of the bound component with font render
     *            properties. If null property is ignored.
     * @param text the text to render.
     * @return an asseturl to the rendered text or an empty string if something
     *         went wrong
     * @author Per Jonsson - per.jonsson@it-huset.se
     */
    public String getRenderedTextUrl( String fontConfigPropertyName, String text )
    {
        return getRenderedTextUrl( fontConfigPropertyName, text, (Map)null );
    }

    /**
     * Renders a text to a PNG file, the preferences of the text rendering is
     * taken from the bound component content of the property. The content is
     * iterated and extract all matching properties. ie. fontName, fontsize etc.
     * @param propertyName name of the bound component with font render
     *            properties. If null property is ignored.
     * @param text the text to render
     * @param renderAttributes render attributes in a commaseparated string ie.
     *            "fontname=Arial,fontsize=12" to override the bound content
     *            preferences
     * @return an asseturl to the rendered text or an empty string if something
     *         went wrong
     * @author Per Jonsson - per.jonsson@it-huset.se
     */
    public String getRenderedTextUrl( String fontConfigPropertyName, String text, String renderAttributes )
    {
        return getRenderedTextUrl( fontConfigPropertyName, text, Support.convertTextToMap( renderAttributes, "=", "," ) );
    }

    /**
     * Renders a text to a PNG file, the preferences of the text rendering is
     * taken from the bound component content of the property. The content is
     * iterated and extract all matching properties. ie. fontName, fontsize etc.
     * @param propertyName name of the bound component with font render
     *            properties. If null property is ignored.
     * @param text the text to render
     * @param renderAttributes render attributes in a commaseparated string ie.
     *            "fontname=Arial,fontsize=12" to override the bound content
     *            preferences
     * @return an asseturl to the rendered text or an empty string if something
     *         went wrong
     * @author Per Jonsson - per.jonsson@it-huset.se
     */
    public String getRenderedTextUrl( final String fontConfigPropertyName, final String text, final Map renderAttributes )
    {
        String assetUrl = "";
        try
        {
            final Map property = getInheritedComponentProperty( this.infoGlueComponent, fontConfigPropertyName, true, true, this.useRepositoryInheritance);
            if ( property != null )
            {
                final List<ComponentBinding> bindings = (List<ComponentBinding>)property.get("bindings");
    			Iterator<ComponentBinding> bindingsIterator = bindings.iterator();
    			if(bindingsIterator.hasNext())
    			{
    				Integer contentId = bindingsIterator.next().getEntityId();
                    assetUrl = templateController.getRenderedTextUrl( contentId, text, renderAttributes );
    			}                
            }
            else
            {
            	assetUrl = templateController.getRenderedTextUrl( text, renderAttributes );
            }
        }
        catch ( Exception e )
        {
            logger.error( "An error occurred trying to get getRenderedTextUrl As ImageUrl:" + e.getMessage(), e );
        }

        return assetUrl;
    }
    
    public Integer getContentId(Map property)
	{
		Integer boundContentId = null;
		
		if(property != null)
		{	
			List<ComponentBinding> bindings = (List<ComponentBinding>)property.get("bindings");
			if(bindings.size() > 0)
			{
				ComponentBinding componentBinding = bindings.get(0);
				boundContentId = componentBinding.getEntityId();
				//boundContentId = new Integer((String)bindings.get(0));
			}
		}
		
		return boundContentId;
	}

    public String getAssetKey(Map property)
	{
		String assetKey = null;
		
		if(property != null)
		{	
			List<ComponentBinding> bindings = (List<ComponentBinding>)property.get("bindings");
			if(bindings.size() > 0)
			{
				ComponentBinding componentBinding = bindings.get(0);
				assetKey = componentBinding.getAssetKey();
				//boundContentId = new Integer((String)bindings.get(0));
			}
		}
		
		return assetKey;
	}

    /*
	public Integer getContentId(Map property)
	{
	    Integer contentId = null;

	    if(property != null)
		{	
			List bindings = (List)property.get("bindings");
			if(bindings.size() > 0)
			{
				contentId = new Integer((String)bindings.get(0));
			}
		}

		return contentId;
	}
	*/
	
	private Integer getSingleBindingAsInteger(Map componentProperty)
	{
		List bindings = (List)componentProperty.get("bindings");
		return (bindings.size() > 0)
			   		? new Integer((String)bindings.get(0))
			   		: new Integer(0);
	}

	public List<ContentVO> getBoundContents(Map property)
	{
	    List<ContentVO> contents = new ArrayList<ContentVO>();

	    if(property != null)
		{	
	    	List<ComponentBinding> bindings = (List<ComponentBinding>)property.get("bindings");
			Iterator<ComponentBinding> bindingsIterator = bindings.iterator();
			while(bindingsIterator.hasNext())
			{
				ComponentBinding componentBinding = bindingsIterator.next();
				Integer contentId = componentBinding.getEntityId();
				contents.add(this.templateController.getContent(contentId));
			}
		}

		return contents;
	}
	/*
	public List getBoundContents(Map property)
	{
	    List contents = new ArrayList();

	    if(property != null)
		{	
			List bindings = (List)property.get("bindings");
			Iterator bindingsIterator = bindings.iterator();
			while(bindingsIterator.hasNext())
			{
				Integer contentId = new Integer((String)bindingsIterator.next());
				contents.add(this.templateController.getContent(contentId));
			}
		}

		return contents;
	}
	*/

	public Integer getSiteNodeId(Map property)
	{
	    Integer siteNodeId = null;

	    if(property != null)
		{	
	    	List<ComponentBinding> bindings = (List<ComponentBinding>)property.get("bindings");
			if(bindings.size() > 0)
			{
				ComponentBinding componentBinding = bindings.get(0);
			    siteNodeId = componentBinding.getEntityId();
			}
		}

		return siteNodeId;
	}

	/*
	public Integer getSiteNodeId(Map property)
	{
	    Integer siteNodeId = null;

	    if(property != null)
		{	
			List bindings = (List)property.get("bindings");
			if(bindings.size() > 0)
			{
			    siteNodeId = new Integer((String)bindings.get(0));
			}
		}

		return siteNodeId;
	}
	*/

	public List<SiteNodeVO> getBoundSiteNodes(Map property)
	{
	    List<SiteNodeVO> siteNodeVOList = new ArrayList<SiteNodeVO>();

	    if(property != null)
		{	
	    	List<ComponentBinding> bindings = (List<ComponentBinding>)property.get("bindings");
			Iterator<ComponentBinding> bindingsIterator = bindings.iterator();
	    	while(bindingsIterator.hasNext())
			{
				ComponentBinding componentBinding = bindingsIterator.next();
			    Integer siteNodeId = componentBinding.getEntityId();
			    SiteNodeVO siteNodeVO = templateController.getSiteNode(siteNodeId);
				if(siteNodeVO != null)
					siteNodeVOList.add(siteNodeVO);
			}
		}

		return siteNodeVOList;
	}

	public List getBoundPages(Map property)
	{
		List pages = new ArrayList();

		if(property != null)
		{	
	    	List<ComponentBinding> bindings = (List<ComponentBinding>)property.get("bindings");
			Iterator<ComponentBinding> bindingsIterator = bindings.iterator();
			while(bindingsIterator.hasNext())
			{
				ComponentBinding componentBinding = bindingsIterator.next();
				Integer siteNodeId = componentBinding.getEntityId();
				SiteNodeVO siteNode = templateController.getSiteNode(siteNodeId);
				if(siteNode != null)
				{
					WebPage webPage = new WebPage();					
					webPage.setSiteNodeId(siteNodeId);
					webPage.setLanguageId(templateController.getLanguageId());
					webPage.setContentId(null);
					webPage.setNavigationTitle(getPageNavTitle(siteNodeId));
					webPage.setMetaInfoContentId(siteNode.getMetaInfoContentId());
					webPage.setUrl(getPageUrl(siteNodeId));
					pages.add(webPage);
				}
			}
		}

		return pages;
	}

	public WebPage getBoundPage(Map property)
	{
		WebPage webPage = null;

		if(property != null)
		{	
	    	List<ComponentBinding> bindings = (List<ComponentBinding>)property.get("bindings");
			Iterator<ComponentBinding> bindingsIterator = bindings.iterator();
			if(bindingsIterator.hasNext())
			{
				webPage = new WebPage();
				ComponentBinding componentBinding = bindingsIterator.next();
				Integer siteNodeId = componentBinding.getEntityId();
				SiteNodeVO siteNode = templateController.getSiteNode(siteNodeId);
				if(siteNode != null)
				{
					webPage.setSiteNodeId(siteNodeId);
					webPage.setLanguageId(templateController.getLanguageId());
					webPage.setContentId(null);
					webPage.setNavigationTitle(getPageNavTitle(siteNodeId));
					webPage.setMetaInfoContentId(siteNode.getMetaInfoContentId());
					webPage.setUrl(getPageUrl(siteNodeId));
				}
			}
		}

		return webPage;
	}

	/*
	public List getBoundPages(Map property)
	{
		List pages = new ArrayList();

		if(property != null)
		{	
			List bindings = (List)property.get("bindings");
			Iterator bindingsIterator = bindings.iterator();
			while(bindingsIterator.hasNext())
			{
				Integer siteNodeId = new Integer((String)bindingsIterator.next());
				SiteNodeVO siteNode = templateController.getSiteNode(siteNodeId);
				if(siteNode != null)
				{
					WebPage webPage = new WebPage();						
					webPage.setSiteNodeId(siteNodeId);
					webPage.setLanguageId(templateController.getLanguageId());
					webPage.setContentId(null);
					webPage.setNavigationTitle(getPageNavTitle(siteNodeId));
					webPage.setMetaInfoContentId(siteNode.getMetaInfoContentId());
					webPage.setUrl(getPageUrl(siteNodeId));
					pages.add(webPage);
				}
			}
		}

		return pages;
	}
	*/
	
	/*
	 * This method returns a bean representing a list of ComponentProperties that the component has.
	 */
	private static PageEditorHelper pageEditorHelper = new PageEditorHelper();
	
	private ComponentPropertyDefinition getComponentPropertyDefinition(Integer componentContentId, String propertyName, Integer siteNodeId, Integer languageId, Integer contentId, Database db, InfoGluePrincipal principal) throws Exception
	{
		//TODO - här kan vi säkert cache:a.
		
		ComponentPropertyDefinition propertyDefinition = null;
		
		Timer timer = new Timer();
		timer.setActive(false);

		try
		{
			org.dom4j.Document document = pageEditorHelper.getComponentPropertiesDOM4JDocument(siteNodeId, languageId, componentContentId, db, principal);
			
			if(document != null)
			{
				timer.printElapsedTime("Read document");

				String propertyXPath = "//property[@name='" + propertyName + "']";
				org.dom4j.Node node = document.selectSingleNode(propertyXPath);
				timer.printElapsedTime("Set property xpath");
				if(node != null)
				{
					org.dom4j.Element element = (org.dom4j.Element)node;
											
					String name						= element.attributeValue("name");
					String displayName				= element.attributeValue("displayName");
					String type					 	= element.attributeValue("type");
					String entity					= element.attributeValue("entity");
					String multiple					= element.attributeValue("multiple");
					String assetBinding 			= element.attributeValue("assetBinding");
					String assetMask				= element.attributeValue("assetMask");
					String isPuffContentForPage 	= element.attributeValue("isPuffContentForPage");
					String allowedContTypeDefNames	= element.attributeValue("allowedContentTypeDefinitionNames");
					String description 				= element.attributeValue("description");
					String defaultValue 			= element.attributeValue("defaultValue");
					String allowLanguageVariations 	= element.attributeValue("allowLanguageVariations");
					String dataProvider 			= element.attributeValue("dataProvider");
					String dataProviderParameters 	= element.attributeValue("dataProviderParameters");
					String allowMultipleSelections 	= element.attributeValue("allowMultipleSelections");
					String WYSIWYGEnabled 			= element.attributeValue("WYSIWYGEnabled");
					String WYSIWYGToolbar 			= element.attributeValue("WYSIWYGToolbar");
					String autoCreatContent 		= element.attributeValue("autoCreatContent");
					String autoCreatContentMethod 	= element.attributeValue("autoCreatContentMethod");
					String autoCreatContentPath		= element.attributeValue("autoCreatContentPath");
					String customMarkup 			= element.attributeValue("customMarkup");
					if(allowLanguageVariations == null || allowLanguageVariations.equals(""))
						allowLanguageVariations = "true";
					
					propertyDefinition = new ComponentPropertyDefinition(name, displayName, type, entity, new Boolean(multiple), new Boolean(assetBinding), assetMask, new Boolean(isPuffContentForPage), allowedContTypeDefNames, description, defaultValue, new Boolean(allowLanguageVariations), new Boolean(WYSIWYGEnabled), WYSIWYGToolbar, dataProvider, dataProviderParameters, new Boolean(autoCreatContent), autoCreatContentMethod, autoCreatContentPath, customMarkup, new Boolean(allowMultipleSelections));
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("The property " + propertyName + " on component with contentId: " + componentContentId + " had a incorrect xml defining it's properties:" + e.getMessage(), e);
		}
							
		return propertyDefinition;
	}

	protected String appendPagePartTemplates(String componentXML, TemplateController templateController) throws Exception
    {
    	String resultComponentXML = componentXML;
    	
		List entries = new ArrayList();
		int isPagePartReferenceIndex = componentXML.indexOf("isPagePartReference");
		while(isPagePartReferenceIndex > -1)
		{
			int tagStartIndex = componentXML.lastIndexOf("<component ", isPagePartReferenceIndex);
			int tagEndIndex = componentXML.indexOf(">", isPagePartReferenceIndex);
			String componentString = componentXML.substring(tagStartIndex, tagEndIndex);
			
			int contentIdIndex = componentString.indexOf(" contentId=");
			String contentId = componentString.substring(contentIdIndex + 12, componentString.indexOf("\"", contentIdIndex + 12));
			
			int idIndex = componentString.indexOf(" id=");
			String id = componentString.substring(idIndex + 5, componentString.indexOf("\"", idIndex + 5));

			int nameIndex = componentString.indexOf(" name=");
			String name = componentString.substring(nameIndex + 7, componentString.indexOf("\"", nameIndex + 7));
			
			Map entry = new HashMap();
			entry.put("contentId", contentId);
			entry.put("id", id);
			entry.put("name", name);
			entries.add(entry);
			
			isPagePartReferenceIndex = componentXML.indexOf("isPagePartReference", isPagePartReferenceIndex + 20);
		}
		
		Iterator entriesIterator = entries.iterator();
		while(entriesIterator.hasNext())
		{
			Map entry = (Map)entriesIterator.next();
			
			String contentIdString = (String)entry.get("contentId");
			Integer contentId = new Integer(contentIdString);
			String id = (String)entry.get("id");
			String name = (String)entry.get("name");
			ContentTypeDefinitionVO contentTypeDefinitionVO = ContentDeliveryController.getContentDeliveryController().getContentTypeDefinitionVO(templateController.getDatabase(), contentId);
		    if(contentTypeDefinitionVO != null && contentTypeDefinitionVO.getName().equals("PagePartTemplate"))
		    {
		    	String pagePartString = templateController.getContentAttribute(contentId, "ComponentStructure", true);
		    	if(pagePartString == null || pagePartString.equals(""))
		    	{
		    		ContentVO contentVO = ContentDeliveryController.getContentDeliveryController().getContentVO(templateController.getDatabase(), contentId, templateController.getDeliveryContext());
		    		LanguageVO masterLanguageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId(), templateController.getDatabase());
		    		pagePartString = templateController.getContentAttribute(contentId, masterLanguageVO.getId(), "ComponentStructure", true);
		    	}
		    	
		    	pagePartString = pagePartString.replaceFirst(" id=\".*?\"", " id=\"" + id + "\"");
		    	pagePartString = pagePartString.replaceFirst(" name=\".*?\"", " name=\"" + name + "\"");
		    	pagePartString = pagePartString.replaceFirst(" pagePartTemplateContentId=\".*?\"", " pagePartTemplateContentId=\"" + contentId + "\"");
		    	
		    	pagePartString = pagePartString.substring(pagePartString.indexOf("<component "));
		    	pagePartString = pagePartString.substring(0, pagePartString.lastIndexOf("</components>"));
		    	
		    	
		    	String newComponentXML = componentXML.replaceAll("<component contentId=\"" + contentId + ".*?</component>", "" + pagePartString);
		    	resultComponentXML = newComponentXML;
		    }
		}

    	return resultComponentXML;
    }
}