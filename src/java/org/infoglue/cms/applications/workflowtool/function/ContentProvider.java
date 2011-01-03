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

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public class ContentProvider extends InfoglueFunction 
{
    private final static Logger logger = Logger.getLogger(ContentProvider.class.getName());

	/**
	 * 
	 */
	public static final String CONTENT_ID_PROPERTYSET_KEY = ContentPopulator.CONTENT_PROPERTYSET_PREFIX + "contentID";
	
	/**
	 * 
	 */
	private LanguageVO languageVO;
	
	/**
	 * 
	 */
	private ContentVO contentVO;
	
	/**
	 * 
	 */
	private ContentVersionVO contentVersionVO;
	
	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		initializeContentVO();
		initializeContentVersionVO();
		if(getContentParameterName() != null && getContentParameterName().length() > 0)
		{
			setParameter(getContentParameterName(), contentVO);
			
		}
		if(getContentVersionParameterName() != null && getContentVersionParameterName().length() > 0)
		{
			setParameter(getContentVersionParameterName(), contentVersionVO);
		}
	}

	/**
	 * 
	 */
	protected void setContentVO(final ContentVO contentVO)
	{
		this.contentVO = contentVO;
	}
	
	/**
	 * 
	 */
	protected void setContentVersionVO(final ContentVersionVO contentVersionVO)
	{
		this.contentVersionVO = contentVersionVO;
	}
	
	/**
	 * 
	 */
	protected final LanguageVO getLanguageVO()
	{
		return this.languageVO;
	}

	/**
	 * 
	 */
	protected String getContentParameterName()
	{
		return ContentFunction.CONTENT_PARAMETER;
	}
	
	/**
	 * 
	 */
	protected String getContentVersionParameterName()
	{
		return ContentFunction.CONTENT_VERSION_PARAMETER;
	}
	
	/**
	 * 
	 */
	protected void initializeContentVO() throws WorkflowException
	{
		if(!propertySetContains(CONTENT_ID_PROPERTYSET_KEY))
		{
			return;
		}
		try 
		{
			final Integer contentID = new Integer(getPropertySetString(CONTENT_ID_PROPERTYSET_KEY));
			setContentVO(ContentController.getContentController().getContentVOWithId(contentID, getDatabase()));
		} 
		catch(Exception e) 
		{
			logger.warn("Non-existing contentId found; removing from the resultset.");
			removeFromPropertySet(CONTENT_ID_PROPERTYSET_KEY);
		}
	}
	
	/**
	 * 
	 */
	protected void initializeContentVersionVO() throws WorkflowException
	{
		if(contentVO != null)
		{
			try 
			{
				final ContentVersionController controller = ContentVersionController.getContentVersionController();
				setContentVersionVO(controller.getLatestActiveContentVersionVO(contentVO.getId(), languageVO.getId(), getDatabase()));
			}
			catch(Exception e)
			{
				throwException(e);
			}
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
		languageVO = (LanguageVO) getParameter(LanguageProvider.LANGUAGE_PARAMETER);
	}
}
