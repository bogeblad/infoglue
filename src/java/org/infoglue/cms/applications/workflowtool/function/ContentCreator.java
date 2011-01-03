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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.workflowtool.util.ContentFactory;
import org.infoglue.cms.applications.workflowtool.util.ContentValues;
import org.infoglue.cms.applications.workflowtool.util.ContentVersionValues;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.exception.ConstraintException;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public class ContentCreator extends ContentFunction 
{
    private final static Logger logger = Logger.getLogger(ContentCreator.class.getName());

	/**
	 * 
	 */
	public static final String FOLDER_PARAMETER = "create_folder";
	
	/**
	 * 
	 */
	private static final String STATUS_OK = "status.content.ok";
	
	/**
	 * 
	 */
	private static final String STATUS_NOK = "status.content.nok";
	
	/**
	 * 
	 */
	private LanguageVO languageVO;
	
	/**
	 * 
	 */
	private ContentTypeDefinitionVO contentTypeDefinitionVO;
	
	/**
	 * 
	 */
	private Map categories;
	
	/**
	 * The content bean.
	 */
	private ContentValues contentValues;
	
	/**
	 * The content version bean.
	 */
	private ContentVersionValues contentVersionValues;
	
	/**
	 * The parent content to use when creating new content.
	 */
	private ContentVO parentContentVO;
	
	
	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		setFunctionStatus(STATUS_NOK);
		try 
		{
			final ContentFactory factory = new ContentFactory(contentTypeDefinitionVO, contentValues, contentVersionValues, getPrincipal(), languageVO);
			ContentVO newContentVO = null;
			if(getContentVO() == null)
			{
				parentContentVO = (ContentVO) getParameter(FOLDER_PARAMETER);
				newContentVO = factory.create(parentContentVO, categories, getDatabase());
			}
			else
			{
				newContentVO = factory.update(getContentVO(), categories, getDatabase());
			}
			if(newContentVO != null)
			{
				setPropertySetString(ContentProvider.CONTENT_ID_PROPERTYSET_KEY, newContentVO.getContentId().toString());
			}
			setFunctionStatus((newContentVO != null) ? STATUS_OK : STATUS_NOK);
		} 
		catch(ConstraintException e) 
		{
			logger.debug(e.toString());
		} 
		catch(Exception e) 
		{
			throwException(e);
		}
	}

	/**
	 * Method used for initializing the function; will be called before <code>execute</code> is called.
	 * <p><strong>Note</strong>! You must call <code>super.initialize()</code> first.</p>
	 * 
	 * @throws WorkflowException if an error occurs during the initialization.
	 */
	protected void initialize() throws WorkflowException 
	{
		super.initialize();
		contentTypeDefinitionVO = (ContentTypeDefinitionVO) getParameter(ContentTypeDefinitionProvider.CONTENT_TYPE_DEFINITION_PARAMETER);
		languageVO              = (LanguageVO)              getParameter(LanguageProvider.LANGUAGE_PARAMETER);
		categories              = (Map)                     getParameter(CategoryProvider.CATEGORIES_PARAMETER, new HashMap());
		contentValues           = (ContentValues)           getParameter(ContentPopulator.CONTENT_VALUES_PARAMETER);
		contentVersionValues    = (ContentVersionValues)    getParameter(ContentPopulator.CONTENT_VERSION_VALUES_PARAMETER);
	}
}
