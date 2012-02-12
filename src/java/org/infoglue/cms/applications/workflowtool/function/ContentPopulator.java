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
package org.infoglue.cms.applications.workflowtool.function;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.workflowtool.util.ContentValues;
import org.infoglue.cms.applications.workflowtool.util.ContentVersionValues;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public class ContentPopulator extends InfoglueFunction 
{
    private final static Logger logger = Logger.getLogger(ContentPopulator.class.getName());

	/**
	 * 
	 */
	public static final String CONTENT_PROPERTYSET_PREFIX = "content_";

	/**
	 * 
	 */
	public static final String CONTENT_VERSION_PROPERTYSET_PREFIX = "contentversion_";
	
	/**
	 * 
	 */
	public static final String CONTENT_VERSION_PROPERTYSET_LANGUAGE_PREFIX = "languageId_";

	/**
	 * 
	 */
	public static final String CONTENT_VALUES_PARAMETER = "contentValues";

	/**
	 * 
	 */
	public static final String CONTENT_VERSION_VALUES_PARAMETER = "contentVersionValues";
	
	/**
	 * 
	 */
	private ContentTypeDefinitionVO contentTypeDefinitionVO;

	/**
	 * 
	 */
	private LanguageVO languageVO;
	
	
	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		populateContentValues();
		populateContentVersionValues();
	}

	/**
	 * 
	 */
	protected void initialize() throws WorkflowException 
	{
		super.initialize();
		contentTypeDefinitionVO = (ContentTypeDefinitionVO) getParameter(ContentTypeDefinitionProvider.CONTENT_TYPE_DEFINITION_PARAMETER);
		languageVO = (LanguageVO) getParameter(LanguageProvider.LANGUAGE_PARAMETER);
	}
	
	/**
	 * 
	 */
	protected void populateContentValues() 
	{
		final ContentValues result = new ContentValues();
		
		result.setName(populate(CONTENT_PROPERTYSET_PREFIX + ContentValues.NAME));
		result.setPublishDateTime(populate(CONTENT_PROPERTYSET_PREFIX + ContentValues.PUBLISH_DATE_TIME));
		result.setExpireDateTime(populate(CONTENT_PROPERTYSET_PREFIX + ContentValues.EXPIRE_DATE_TIME));

		setParameter(CONTENT_VALUES_PARAMETER, result);
	}
	
	/**
	 * 
	 */
	protected void populateContentVersionValues() 
	{
		final ContentVersionValues result = new ContentVersionValues();
		final List contentTypeAttributes = getContentTypeAttributes();
		for(Iterator i=contentTypeAttributes.iterator(); i.hasNext(); ) 
		{
			final ContentTypeAttribute attribute = (ContentTypeAttribute) i.next();
			result.set(attribute.getName(), populate(CONTENT_VERSION_PROPERTYSET_PREFIX + attribute.getName()));
		}
		setParameter(CONTENT_VERSION_VALUES_PARAMETER, result);
	}
	
	/**
	 * 
	 */
	private String populate(final String name) 
	{
		if(parameterExists(name)) 
		{
			if(languageVO == null)
				setPropertySetDataString(name, getRequestParameter(name));
			else
			{
				setPropertySetDataString(name, getRequestParameter(name));
				setPropertySetDataString(languageVO.getLanguageCode() + "_" + name, getRequestParameter(name));
			}
			
			logger.debug(name + " is found in the request; propertyset updated.");
		} 
		else
		{
			logger.debug(name + " is not found in the request; propertyset not updated.");
		}
		if(languageVO == null)
			return propertySetContains(name) ? getPropertySetDataString(name) : "";
		return propertySetContains(languageVO.getLanguageCode() + "_" + name) ? getPropertySetDataString(languageVO.getLanguageCode() + "_" + name) : "";
	}
	/**
	 * 
	 */
	private List getContentTypeAttributes() 
	{
		return ContentTypeDefinitionController.getController().getContentTypeAttributes(contentTypeDefinitionVO, true);
	}
}
