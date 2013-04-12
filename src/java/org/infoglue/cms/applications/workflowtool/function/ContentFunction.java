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

import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.impl.simple.MediumContentVersionImpl;

import com.opensymphony.workflow.WorkflowException;

/**
 * Base class for all functions operating on <code>ContentVO</code> objects.
 */
public abstract class ContentFunction extends InfoglueFunction 
{
	/**
	 * 
	 */
	public static final String CONTENT_PARAMETER = "content";
	
	/**
	 * 
	 */
	public static final String CONTENT_VERSION_PARAMETER = "contentVersion";
	
	/**
	 * 
	 */
	private static final String CONTENT_PARAMETER_NAME_ARGUMENT = "contentParameterName";
	
	/**
	 * 
	 */
	private static final String CONTENT_VERSION_PARAMETER_NAME_ARGUMENT = "contentVersionParameterName";
	
	/**
	 *
	 */
	private Content content;
	
	/**
	 *
	 */
	private ContentVO contentVO;

	/**
	 *
	 */
	private MediumContentVersionImpl contentVersion;
	
	/**
	 *
	 */
	private ContentVersionVO contentVersionVO;
	
	/**
	 * Default constructor.
	 */
	protected ContentFunction() 
	{ 
		super(); 
	}
	
	/**
	 * 
	 */
	protected ContentVO getContentVO() 
	{ 
		return contentVO; 
	}
	
	/**
	 * 
	 */
	protected ContentVersionVO getContentVersionVO() 
	{ 
		return contentVersionVO; 
	}
	
	/**
	 * 
	 */
	protected Content getContent() 
	{
		if(contentVO != null && content == null)
		{
			try 
			{
				content = ContentController.getContentController().getContentWithId(contentVO.getContentId(), getDatabase());
			}
			catch(Exception e)
			{
				// shouldn't happen; just return null...
			}
		}
		return content;
	}
	
	/**
	 * 
	 */
	protected MediumContentVersionImpl getContentVersion() 
	{
		if(contentVersionVO != null && contentVersion == null)
		{
			try 
			{
				contentVersion = ContentVersionController.getContentVersionController().getMediumContentVersionWithId(contentVersionVO.getContentVersionId(), getDatabase());
			}
			catch(Exception e)
			{
				// shouldn't happen; just return null...
			}
		}
		return contentVersion;
	}
	
	/**
	 * 
	 */
	protected String getAttribute(final String name, final boolean escapeHTML) throws WorkflowException
	{
		if(contentVersionVO == null)
		{
			throwException("No content version.");
		}
		String value = "";
		try 
		{
			value = ContentVersionController.getContentVersionController().getAttributeValue(contentVersionVO, name, escapeHTML);
		}
		catch(Exception e)
		{
			throwException(e);
		}
		return value;
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
		contentVO        = (ContentVO)        getParameter(getArgument(CONTENT_PARAMETER_NAME_ARGUMENT, CONTENT_PARAMETER), false);
		contentVersionVO = (ContentVersionVO) getParameter(getArgument(CONTENT_VERSION_PARAMETER_NAME_ARGUMENT, CONTENT_VERSION_PARAMETER), false);
	}
}
