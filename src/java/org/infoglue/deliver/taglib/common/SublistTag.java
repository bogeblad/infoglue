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
package org.infoglue.deliver.taglib.common;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.deliver.taglib.AbstractTag;

/**
 * This class implements the &lt;common:sublist&gt; tag, which stores the sublist of a specified list
 * in the page context variable specified by the <code>id</code> attribute.  
 */
public class SublistTag extends AbstractTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = -8053523983744317359L;

	/**
	 * The list.
	 */
	private List list;
	
	/**
	 * The start index of the sublist operation.
	 */
	private int startIndex;
	
	/**
	 * The number of element to include in the sublist operation.
	 * A zero value indicates all elements.
	 * Depending on the size of the list, a different count may be used.
	 */
	private Integer count;
	
	
	/**
	 * Default constructor.
	 */
	public SublistTag() 
	{
		super();
	}
	
	/**
	 * Process the end tag. Stores the sublist in the specified page context variable.  
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		checkAttributes();
	    setResultAttribute(getSublist());
	    
	    list = null;
	    startIndex = 0;
	    count = 0;

	    return EVAL_PAGE;
    }

	/**
	 * Checks if all attributes has legal values. 
	 * 
	 * @throws JspException if any attribute has an illegal value.
	 */
	private void checkAttributes() throws JspException
	{
		if(list == null)
		{
			throw new JspTagException("List is null.");
		}
		if(startIndex < 0 || (!list.isEmpty() && startIndex >= list.size()))
		{
			throw new JspTagException("Illegal startIndex [0<=" + startIndex + "<" + list.size() + "].");
		}
		if(count.intValue() < 0)
		{
			throw new JspTagException("Illegal count; must be a non-negative integer.");
		}
	}
	
	/**
	 * Returns the sublist using the specified attributes.
	 */
	private List getSublist() 
	{
		// Don't use List.sublist()  
		final List result = new ArrayList();
		int endIndex = startIndex + count.intValue();
		if(endIndex > list.size())
			endIndex = list.size();
		
		for(int i=startIndex; i<endIndex; i++)
		{
			result.add(list.get(i));
		}
		
		return result;
	}
	
	/**
	 * Returns the count to use which might be different from the specified count depending
	 * on the size of the list. 
	 * 
	 * @return the count to use.
	 */
	private int getRealCount() 
	{
		return (count.intValue() == 0 || count.intValue() > list.size() - startIndex) ? list.size() - startIndex + 1 : count.intValue();
		//return (count.intValue() == 0 || count.intValue() > list.size() - startIndex) ? list.size() - startIndex : count.intValue();
	}
	
	/**
	 * Sets the list attribute to the specified list.
	 * 
	 * @param list the list to use.
	 * @throws JspException if an error occurs while evaluating the list.
	 */
    public void setList(final String list) throws JspException
    {
        this.list = evaluateList("sublist", "list", list);
    }

	/**
	 * Sets the start index attribute to the specified index.
	 * 
	 * @param index the index to use.
	 */
    public void setStartIndex(final int startIndex)
    {
        this.startIndex = startIndex;
    }

	/**
	 * Sets the count attribute to the specified count. A count of zero
	 * indicates all remaining elements.
	 * 
	 * @param count the count to use.
	 * @throws JspException if an error occurs while evaluating the count.
	 */
    public void setCount(final String count) throws JspException
    {
        this.count = evaluateInteger("sublist", "count", count);
    }
}
