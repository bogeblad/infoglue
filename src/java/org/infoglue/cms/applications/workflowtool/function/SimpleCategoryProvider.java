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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.entities.management.CategoryVO;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public class SimpleCategoryProvider extends CategoryProvider 
{
	/**
	 * 
	 */
	private static final String CATEGORY_PROPERTYSET_PREFIX = "category_";

	/**
	 * 
	 */
	private static final String NAME_ARGUMENT = "attributeName";

	/**
	 * 
	 */
	private static final String	ROOT_ARGUMENT = "rootCategory";

	/**
	 * 
	 */
	private CategoryVO rootCategory;

	/**
	 * 
	 */
	private String attributeName;
		
	
	
	/**
	 * 
	 */
	public SimpleCategoryProvider() 
	{ 
		super();	
	}

	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		cleanPropertySet();
		populate();	
	}
	
	/**
	 * 
	 */
	private void populate() throws WorkflowException 
	{
		List result = new ArrayList();
		for(Iterator i = rootCategory.getChildren().iterator(); i.hasNext();) 
		{
			final CategoryVO categoryVO = (CategoryVO) i.next();
			final String key = getCategoryKey(categoryVO);
			final String value = getRequestParameter(key);
			if(parameterExists(key) && value != null && value.equals("1")) 
			{
				setPropertySetDataString(getCategoryKey(categoryVO), "1");
				result.add(categoryVO);
			}
		}
		getCategories().put(attributeName, result);
	}
	
	/**
	 * 
	 */
	private void cleanPropertySet() throws WorkflowException
	{
		removeFromPropertySet(getBaseKey(), true);
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
		attributeName = getArgument(NAME_ARGUMENT);
		rootCategory  = getRootCategory(getArgument(ROOT_ARGUMENT));
	}
	
	/**
	 * 
	 */
	private CategoryVO getRootCategory(final String path) throws WorkflowException 
	{
		try 
		{
			final CategoryVO categoryVO = CategoryController.getController().findByPath(path, getDatabase());
			return CategoryController.getController().findWithChildren(categoryVO.getId(), getDatabase());
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
			throw new WorkflowException("SimpleCategoryProvider.getRootCategory() : " + e);
		}
	}

	/**
	 * 
	 */
	private String getBaseKey() 
	{ 
		return CATEGORY_PROPERTYSET_PREFIX + attributeName + "_";	
	}
	
	/**
	 * 
	 */
	private String getCategoryKey(final CategoryVO categoryVO) 
	{ 
		return getBaseKey() + categoryVO.getName(); 
	}
}
