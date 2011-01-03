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

import java.util.Collection;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.infoglue.deliver.taglib.AbstractTag;

/**
 * This class implements the &lt;common:size&gt; tag, which stores the size of a collection
 * in the page context variable specified by the <code>id</code> attribute.  
 */
public class SizeTag extends AbstractTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 8603406098980150888L;
	
	/**
	 * The collection.
	 */
	private Collection collection;
	private Map map;
	
	/**
	 * Default constructor.
	 */
	public SizeTag() 
	{
		super();
	}
	
	/**
	 * Process the end tag. Stores the size of the collection in the specified page context variable.  
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		if(map != null)
		    setResultAttribute(new Integer(map == null ? 0 : map.size()));
		else if(collection != null)
			setResultAttribute(new Integer(collection == null ? 0 : collection.size()));
		else
			throw new JspException("You must set either map or collection attribute on common:size-tag");
		
		this.collection = null;
		this.map = null;

        return EVAL_PAGE;
    }

	/**
	 * TODO: This class operates on collection; change name to setCollection.
	 * 
	 * Sets the collection attribute.
	 * 
	 * @param collection the collection to use.
	 */
    public void setList(final String collection) throws JspException
    {
        this.collection = evaluateCollection("size", "list", collection);
    }
    
    public void setMap(final String map) throws JspException
    {
        this.map = (Map)evaluate("size", "map", map, Map.class);
    }
}
