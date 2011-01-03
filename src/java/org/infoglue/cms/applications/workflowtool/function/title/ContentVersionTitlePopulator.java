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
package org.infoglue.cms.applications.workflowtool.function.title;

import org.infoglue.cms.applications.workflowtool.function.ContentPopulator;
import org.infoglue.cms.applications.workflowtool.util.ContentVersionValues;

import com.opensymphony.workflow.WorkflowException;

public class ContentVersionTitlePopulator extends Populator 
{
	/**
	 * 
	 */
    private static final String ATTRIBUTE_ARGUMENT = "attributeName";
	
	/**
	 * 
	 */
	private String title;
	
	
	
	/**
	 * Default constructor.
	 */
	public ContentVersionTitlePopulator() 
	{ 
		super(); 
	}
	
	/**
	 * 
	 */
	protected String getTitle() 
	{ 
		return title; 
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
		final String attributeName = getArgument(ATTRIBUTE_ARGUMENT);
		final ContentVersionValues contentVersionValues = (ContentVersionValues) getParameter(ContentPopulator.CONTENT_VERSION_VALUES_PARAMETER, null);
		if(contentVersionValues != null && contentVersionValues.contains(attributeName))
		{
			title = contentVersionValues.get(attributeName).toString();
		}
	}
}
