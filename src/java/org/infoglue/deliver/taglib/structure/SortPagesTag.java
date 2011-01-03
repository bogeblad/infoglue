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

package org.infoglue.deliver.taglib.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.util.sorters.HardcodedPageComparator;
import org.infoglue.cms.util.sorters.PageComparator;
import org.infoglue.deliver.taglib.TemplateControllerTag;

public class SortPagesTag extends TemplateControllerTag 
{
	private static final long serialVersionUID = 3257003254859576632L;

	private Comparator comparator;
	
	private List input = new ArrayList();
	
	private String sortProperty = "NavigationTitle";
	private String nameProperty = "NavigationTitle";
	private String sortOrder 	= "asc";
	private boolean numberOrder = false; 
	private String type			= "PageComparator";
	private String namesInOrderString = null;
	
	/**
	 *
	 */
    public SortPagesTag()
    {
        super();
    }
	
	/**
	 * 
	 */
	public int doEndTag() throws JspException
    {
	    if(this.type.equalsIgnoreCase("HardcodedPageComparator") || namesInOrderString != null)
	        this.comparator = new HardcodedPageComparator(sortProperty, sortOrder, numberOrder, nameProperty, namesInOrderString, getController());
	    else
	        this.comparator = new PageComparator(sortProperty, sortOrder, numberOrder, getController());
	        
	    Collections.sort(input, comparator);
		produceResult(input);
		
		comparator = null;
		input = new ArrayList();
		
        return EVAL_PAGE;
    }
	
	/**
	 * 
	 */
	public void setInput(final String input) throws JspException
	{
		this.input = evaluateList("contentSort", "input", input);
	}

	/**
	 * 
	 */
	public void setSortProperty(final String sortProperty) throws JspException
	{
		this.sortProperty = evaluateString("contentSort", "sortProperty", sortProperty);
	}


	/**
	 * 
	 */
	public void setSortOrder(final String sortOrder) throws JspException
	{
		this.sortOrder = evaluateString("contentSort", "sortOrder", sortOrder);
	}

	/**
	 * 
	 */
	public void setType(final String type) throws JspException
	{
	    this.type = type;
	}

	public void setNamesInOrderString(final String namesInOrderString) throws JspException
	{
	    this.namesInOrderString = namesInOrderString;
	}
	
    public void setNumberOrder(boolean numberOrder)
    {
        this.numberOrder = numberOrder;
    }
}
