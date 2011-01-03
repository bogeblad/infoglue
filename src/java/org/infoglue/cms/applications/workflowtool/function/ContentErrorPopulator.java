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

import org.infoglue.cms.applications.workflowtool.util.ContentFactory;
import org.infoglue.cms.applications.workflowtool.util.ContentValues;
import org.infoglue.cms.applications.workflowtool.util.ContentVersionValues;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public class ContentErrorPopulator extends ErrorPopulator 
{

	/**
	 * 
	 */
	private static final String CONTENT_ERROR_PROPERTYSET_PREFIX = ERROR_PROPERTYSET_PREFIX + "content_";
	
	/**
	 * 
	 */
	private static final String CONTENT_VERSION_ERROR_PROPERTYSET_PREFIX = ERROR_PROPERTYSET_PREFIX + "contentversion_";
	
	/**
	 * 
	 */
	private LanguageVO language;

	/**
	 * 
	 */
	private ContentTypeDefinitionVO contentTypeDefinitionVO;

	/**
	 * 
	 */
	private ContentValues contentValues;

	/**
	 * 
	 */
	private ContentVersionValues contentVersionValues;
	
	/**
	 * 
	 */
	protected void clean() throws WorkflowException
	{
		clean(CONTENT_ERROR_PROPERTYSET_PREFIX);
		clean(CONTENT_VERSION_ERROR_PROPERTYSET_PREFIX);
		if(language != null)
			clean(language.getLanguageCode() + "_" + CONTENT_VERSION_ERROR_PROPERTYSET_PREFIX);
	}
	
	/**
	 * 
	 */
	protected void populate() throws WorkflowException
	{
		populate(new ContentFactory(contentTypeDefinitionVO, contentValues, contentVersionValues, getPrincipal(), language).validate(), language.getLanguageCode());
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
		language                = (LanguageVO)              getParameter(LanguageProvider.LANGUAGE_PARAMETER);
		contentTypeDefinitionVO = (ContentTypeDefinitionVO) getParameter(ContentTypeDefinitionProvider.CONTENT_TYPE_DEFINITION_PARAMETER);
		contentValues           = (ContentValues)           getParameter(ContentPopulator.CONTENT_VALUES_PARAMETER);
		contentVersionValues    = (ContentVersionValues)    getParameter(ContentPopulator.CONTENT_VERSION_VALUES_PARAMETER);
	}
}