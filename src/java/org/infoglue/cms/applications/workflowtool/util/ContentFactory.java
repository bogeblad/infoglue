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
package org.infoglue.cms.applications.workflowtool.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.PersistenceException;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentCategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.dom.DOMBuilder;

/**
 * 
 */
public class ContentFactory 
{
	/**
	 * The class logger. 
	 */
    private final static Logger logger = Logger.getLogger(ContentFactory.class.getName());
	
	
	/**
	 * The content type.
	 */
	private final ContentTypeDefinitionVO contentTypeDefinitionVO;
	
	/**
	 * The content bean.
	 */
	private final ContentValues contentValues;
	
	/**
	 * The content version bean.
	 */
	private final ContentVersionValues contentVersionValues;
	
	/**
	 * The creator.
	 */
	private final InfoGluePrincipal principal;

	/**
	 * The language associated with the content version.
	 */
	private final LanguageVO language;

	/**
	 * The database to use.
	 */
	private Database db;
	
	/**
	 * 
	 * @param contentTypeDefinitionVO
	 * @param contentValues
	 * @param contentVersionValues
	 * @param principal
	 * @param language
	 */
	public ContentFactory(final ContentTypeDefinitionVO contentTypeDefinitionVO, final ContentValues contentValues, final ContentVersionValues contentVersionValues, final InfoGluePrincipal principal, final LanguageVO language) 
	{
		this.contentTypeDefinitionVO = contentTypeDefinitionVO;
		this.contentValues           = contentValues;
		this.contentVersionValues    = contentVersionValues;
		this.principal               = principal;
		this.language                = language;
		
		if(logger.isDebugEnabled())
		{
			logger.info("*********ContentFactory**********");
			logger.info("contentTypeDefinitionVO:" + contentTypeDefinitionVO);
			logger.info("contentValues:" + contentValues);
			logger.info("contentVersionValues:" + contentVersionValues);
			logger.info("principal:" + principal);
			logger.info("language:" + language);
			logger.info("*********END ContentFactory**********");
		}
	}

	/**
	 * 
	 * @param parentContent
	 * @param categories
	 * @param db
	 * @return
	 */
	public ContentVO create(final ContentVO parentContent, final Map categories, final Database db) throws PersistenceException, SystemException, ConstraintException, IOException  
	{
		if(logger.isDebugEnabled())
		{
			logger.info("*********ContentFactory.create**********");
			logger.info("contentTypeDefinitionVO:" + contentTypeDefinitionVO);
			logger.info("contentValues:" + contentValues);
			logger.info("contentVersionValues:" + contentVersionValues);
			logger.info("principal:" + principal);
			logger.info("language:" + language);
			logger.info("parentContent:" + parentContent);
			logger.info("categories:" + categories);
		}
		
		this.db = db;
		final ContentVO contentVO = createContentVO();
		final Document contentVersionDocument = buildContentVersionDocument();
		final ContentVersionVO contentVersionVO = createContentVersionVO(contentVersionDocument.asXML());
		
		ConstraintExceptionBuffer ceb = validate(contentVO, contentVersionVO);
		if(ceb.isEmpty())
		{
			return createContent(parentContent, contentVO, contentVersionVO, categories);
		}
		try
		{
			ceb.throwIfNotEmpty();
		}
		catch (ConstraintException e) 
		{
			logger.error("Problem creating content:" + e.getMessage());
		}
		catch (Exception e) 
		{
			logger.error("Problem creating content:" + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 
	 * @param contentVO
	 * @param categories
	 * @return
	 * @throws ConstraintException
	 */
	public ContentVO update(final ContentVO contentVO, final Map categories, final Database db)  
	{
		this.db = db;
		populateContentVO(contentVO);
		final ContentVersionVO contentVersionVO = createContentVersionVO(buildContentVersionDocument().asXML());
		
		ConstraintExceptionBuffer ceb = validate(contentVO, contentVersionVO);
		if(ceb.isEmpty())
		{
			return updateContent(contentVO, contentVersionVO, categories);
		}
		logger.error("Error: Not valid content or version - cancelling update.");
		return null;
	}

	/**
	 * 
	 * @return
	 */
	public ConstraintExceptionBuffer validate() 
	{
		final ContentVO contentVO               = createContentVO();
		final ContentVersionVO contentVersionVO = createContentVersionVO(buildContentVersionDocument().asXML());
		return validate(contentVO, contentVersionVO);
	}

	/**
	 * 
	 * @param contentVO
	 * @param contentVersionVO
	 * @return
	 */
	private ConstraintExceptionBuffer validate(final ContentVO contentVO, final ContentVersionVO contentVersionVO) 
	{
		final ConstraintExceptionBuffer ceb = contentVO.validate();
		ceb.add(contentVersionVO.validateAdvanced(contentTypeDefinitionVO));
		return ceb;
	}

	/**
	 * 
	 * @param parentContent
	 * @param contentVO
	 * @param contentVersionVO
	 * @param categories
	 * @return
	 */
	private ContentVO createContent(final ContentVO parentContent, final ContentVO contentVO, final ContentVersionVO contentVersionVO, final Map categories) throws SystemException, PersistenceException, ConstraintException, IOException 
	{
    	if(logger.isDebugEnabled())
    	{
			logger.info("************createContent**********");
			logger.info("parentContent:" + parentContent);
			logger.info("contentVO:" + contentVO);
			logger.info("contentVersionVO:" + contentVersionVO);
			logger.info("categories:" + categories);
    	}
    	
		final Content content = ContentControllerProxy.getContentController().create(db, parentContent.getId(), contentTypeDefinitionVO.getId(), parentContent.getRepositoryId(), contentVO);
		final ContentVersion newContentVersion = ContentVersionController.getContentVersionController().create(content.getId(), language.getId(), contentVersionVO, null, db);
		createCategories(newContentVersion, categories);
		
		if(logger.isDebugEnabled())
			logger.info("Returning:" + content + ":" + content.getValueObject());
		
		return content.getValueObject();
	}

	/**
	 * 
	 * @param contentVO
	 * @param contentVersionVO
	 * @param categories
	 * @return
	 */
	private ContentVO updateContent(final ContentVO contentVO, final ContentVersionVO contentVersionVO, final Map categories) 
	{
		try 
		{
			final Content content = ContentControllerProxy.getContentController().getContentWithId(contentVO.getId(), db);
			content.setValueObject(contentVO);
			final ContentVersion contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersion(content.getId(), language.getId(), db);
			if(contentVersion != null)
			{
				contentVersion.getValueObject().setVersionValue(contentVersionVO.getVersionValue());
				deleteCategories(contentVersion);
				createCategories(contentVersion, categories);
			}
			else
			{
				final ContentVersion newContentVersion = ContentVersionController.getContentVersionController().create(content.getId(), language.getId(), contentVersionVO, null, db);
				createCategories(newContentVersion, categories);
			}
			
			return content.getValueObject();
	    } 
		catch(Exception e) 
		{
			e.printStackTrace();
			logger.warn(e);
	    }
		return null;
	}

	/**
	 * Deletes all content categories associated with the specified content version.
	 * 
	 * @param contentVersion the content version.
	 */
	private void deleteCategories(final ContentVersion contentVersion) throws SystemException 
	{
		ContentCategoryController.getController().deleteByContentVersion(contentVersion.getId(), db);
	}

	/**
	 * 
	 * @param contentVersion
	 * @param categorieVOs
	 */
	private void createCategories(final ContentVersion contentVersion, final Map categorieVOs) throws SystemException, PersistenceException  
	{
		if(categorieVOs != null)
		{
			for(Iterator i=categorieVOs.keySet().iterator(); i.hasNext(); ) 
			{
				String attributeName = (String) i.next();
				List categoryVOs     = (List) categorieVOs.get(attributeName);
				createCategory(contentVersion, attributeName, categoryVOs);
			}
		}
	}

	/**
	 * 
	 * @param contentVersion
	 * @param attributeName
	 * @param categoryVOs
	 */
	private void createCategory(final ContentVersion contentVersion, final String attributeName, final List categoryVOs) throws SystemException, PersistenceException 
	{
		final List categories = categoryVOListToCategoryList(categoryVOs);
		ContentCategoryController.getController().create(categories, contentVersion, attributeName, db);
	}

	/**
	 * 
	 * @param db the database to use in the operation.
	 * @param categoryVOList
	 * @return
	 */
	private List categoryVOListToCategoryList(final List categoryVOList) throws SystemException 
	{
		final List result = new ArrayList();
		for(Iterator i=categoryVOList.iterator(); i.hasNext(); ) 
		{
			CategoryVO categoryVO = (CategoryVO) i.next();
			result.add(CategoryController.getController().findById(categoryVO.getCategoryId(), db));
		}
		return result;
	}
	

	/**
	 * Creates a new content value object and populates it using the content bean.
	 * 
	 * @return the content value object.
	 */
	private ContentVO createContentVO() 
	{
		final ContentVO contentVO = new ContentVO();
		populateContentVO(contentVO);
		return contentVO;
	}

	/**
	 * Populates the specified content value object using the content bean.
	 * 
	 * @param contentVO the content value object.
	 */
	private void populateContentVO(final ContentVO contentVO) 
	{
		contentVO.setName(contentValues.getName());
		if(contentValues.getPublishDateTime() != null)
			contentVO.setPublishDateTime(contentValues.getPublishDateTime());
		
		if(contentValues.getExpireDateTime() != null)
			contentVO.setExpireDateTime(contentValues.getExpireDateTime());
		
		contentVO.setIsBranch(Boolean.FALSE);
		contentVO.setCreatorName(principal.getName());
	}

	/**
	 * Creates a new content version value object with the specified version value.
	 * 
	 * @param versionValue the version value.
	 * @return the content version value object.
	 */
	private ContentVersionVO createContentVersionVO(final String versionValue) 
	{
		final ContentVersionVO contentVersion = new ContentVersionVO();
		contentVersion.setVersionComment("");
		contentVersion.setVersionModifier(principal.getName());
		contentVersion.setVersionValue(versionValue);
		return contentVersion;
	}

	/**
	 * Returns all attributes of the content type.
	 * 
	 * @return the attributes.
	 */
	private Collection getContentTypeAttributes() 
	{
		return ContentTypeDefinitionController.getController().getContentTypeAttributes(contentTypeDefinitionVO, true);
	}
	
	/**
	 * Builds the content version value document using the content version bean.
	 * 
	 * @return the document.
	 */
	private Document buildContentVersionDocument() 
	{
		final DOMBuilder builder  = new DOMBuilder();
		final Document document   = builder.createDocument();
		final Element rootElement = builder.addElement(document, "root");
		builder.addAttribute(rootElement, "xmlns", "x-schema:Schema.xml");
		final Element attributesRoot =  builder.addElement(rootElement, "attributes");
		
		buildAttributes(builder, attributesRoot);
		
		return document;
	}

	/**
	 * Builds the attributes part of the content version value document.
	 * 
	 * @param domBuilder the document builder. 
	 * @param parentElement the parent element of all the attributes.
	 */
	private void buildAttributes(final DOMBuilder domBuilder, final Element parentElement) 
	{	
		final Collection typeAttributes = getContentTypeAttributes();
		for(final Iterator i=typeAttributes.iterator(); i.hasNext(); ) 
		{
			final ContentTypeAttribute attribute = (ContentTypeAttribute) i.next();
			final Element element = domBuilder.addElement(parentElement, attribute.getName());
			final String value = contentVersionValues.get(attribute.getName());
			if(value != null)
			{
				domBuilder.addCDATAElement(element, value);
			}
		}		
	}
}
