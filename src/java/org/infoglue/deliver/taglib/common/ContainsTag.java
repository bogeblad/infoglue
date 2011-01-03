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

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.infoglue.deliver.taglib.AbstractTag;

/**
 * This class checks if an object exists in another object. It tries to be a bit smart and should handle
 * arrays, collections and simple strings etc.  
 */
public class ContainsTag extends AbstractTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 8603406098980150888L;
	
	/**
	 * The collection.
	 */
	private Object object;
	private Object value;
	
	/**
	 * Default constructor.
	 */
	public ContainsTag() 
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
		if(object == null || value == null)
			setResultAttribute(false);
		
		if(object instanceof String[])
		{
			List list = Arrays.asList((String[])object);
			setResultAttribute(list.contains(value));
		}
		else if(object instanceof Collection)
		{
			Collection list = (Collection)object;
			setResultAttribute(list.contains(value));
		}
		else if(object instanceof Map)
		{
			Map map = (Map)object;
			setResultAttribute(map.containsKey(value));
		}
		else
		{
			setResultAttribute(object.equals(value));
		}
		
        return EVAL_PAGE;
    }

    public void setObject(final String object) throws JspException
    {
        this.object = evaluate("contains", "object", object, Object.class);
    }
    
    public void setValue(final String value) throws JspException
    {
        this.value = evaluate("contains", "value", value, Object.class);
    }
}
