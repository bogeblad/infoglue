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

import org.infoglue.cms.entities.management.LanguageVO;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public class PropertysetPopulator extends InfoglueFunction 
{
	/**
	 * 
	 */
	private static final String PREFIX_ARGUMENT = "prefix";

	/**
	 * 
	 */
	private String prefix;

	/**
	 * 
	 */
	private LanguageVO languageVO;

	
	
	/**
	 * 
	 */
	public PropertysetPopulator() 
	{ 
		super(); 
	}

	/**
	 * 
	 */
	protected void execute() throws WorkflowException {
		cleanPropertySet();
		populate();
	}
	
	/**
	 * 
	 */
	private void populate() throws WorkflowException 
	{
		for(final Iterator i = getParameters().keySet().iterator(); i.hasNext(); ) 
		{
			String key = i.next().toString();
			if(key.startsWith(prefix))
			{
				setPropertySetDataString(key, getRequestParameter(key));
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
		prefix = getArgument(PREFIX_ARGUMENT);
		try
		{
			languageVO = (LanguageVO) getParameter(LanguageProvider.LANGUAGE_PARAMETER);
		}
		catch (Exception e) 
		{
			System.out.println("Error getting languageVO:" + e.getMessage());
			//languageVO = (LanguageVO) getParameter(LanguageProvider.LANGUAGE_PARAMETER);
		}
	}
	
	/**
	 * 
	 */
	private void cleanPropertySet() throws WorkflowException
	{
		removeFromPropertySet(prefix, true);
	}
}
