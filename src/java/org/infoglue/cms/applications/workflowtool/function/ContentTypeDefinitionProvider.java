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

import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;

import com.opensymphony.workflow.WorkflowException;

public class ContentTypeDefinitionProvider extends InfoglueFunction 
{
	
	/**
	 * 
	 */
	public static final String CONTENT_TYPE_DEFINITION_PARAMETER = "contentTypeDefinition";

	/**
	 * 
	 */
	private static final String NAME_ARGUMENT = "contentTypeDefinitionName";

	/**
	 * 
	 */
	private String name;
	
	/**
	 * 
	 */
	public ContentTypeDefinitionProvider()
	{ 
		super(); 
	}

	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		try 
		{
		    final ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName(name, getDatabase());
		    if(contentTypeDefinitionVO == null)
		    {
				throwException(name + " is not a definied content type.");
		    }
			setParameter(CONTENT_TYPE_DEFINITION_PARAMETER, contentTypeDefinitionVO);
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
		this.name = getArgument(NAME_ARGUMENT);
	}
}
