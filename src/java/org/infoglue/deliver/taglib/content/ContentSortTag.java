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

package org.infoglue.deliver.taglib.content;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.util.sorters.ContentSort;
import org.infoglue.deliver.taglib.TemplateControllerTag;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;

public class ContentSortTag extends TemplateControllerTag {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3257003254859576632L;

	private ContentSort sorter;
	private Collection input = new ArrayList();
	private String comparatorClass;

	/**
	 *
	 */
    public ContentSortTag()
    {
        super();
    }

	/**
	 * 
	 */
	public int doStartTag() throws JspException 
	{
		sorter = new ContentSort(getController(), input);
		return EVAL_BODY_INCLUDE;
	}
	
	/**
	 * 
	 */
	public void addContentProperty(final String name, final boolean ascending) 
	{
		sorter.addContentProperty(name, ascending);
	}
	
	/**
	 * 
	 */
	public void addContentVersionProperty(final String name, final boolean ascending) 
	{
		sorter.addContentVersionProperty(name, ascending);
	}
	
	/**
	 * 
	 */
	public void addContentVersionAttribute(final String name, final String className, final boolean ascending, final boolean caseSensitive) 
	{
		sorter.addContentVersionAttribute(name, className, ascending, caseSensitive);
	}
	
	/**
	 * 
	 */
	public int doEndTag() throws JspException
    {
		if(comparatorClass!=null && !comparatorClass.equals("")) 
		{
			produceResult(sorter.getContentResult(comparatorClass));
		}
		else
		{
			produceResult(sorter.getContentResult());	
		}

		this.sorter.clear();
		this.sorter = null;
		this.input = new ArrayList();
		this.comparatorClass = null;
		
        return EVAL_PAGE;
    }
	
	/**
	 * 
	 */
	public void setInput(final String input) throws JspException
	{
		this.input = evaluateCollection("contentSort", "input", input);
	}
	
	/**
	 * @param comparatorClass the comparatorClass to set
	 */
	
	public void setComparatorClass(String comparatorClass) 
	{
		this.comparatorClass = comparatorClass;
	}

}
