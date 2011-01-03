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
package org.infoglue.cms.workflow.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * This class implements the &lt;iw:setproperty&gt; tag, which stores a value as a
 * data field in the propertyset associated with the workflow.
 */
public class SetPropertySetTag extends WorkflowTag
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = -4937344683246274243L;

	/**
	 * The propertyset key.
	 */
	private String key;
	
	/**
	 * The propertyset value.
	 */
	private String value;
	
	/**
	 * Default constructor.
	 */
    public SetPropertySetTag() 
	{
        super();
    }

	/**
	 * Process the end tag. Stores the specified value as a data field in the propertyset.
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurs when storing the the value in the propertyset.
	 */
	public int doEndTag() throws JspException 
	{
		try 
		{
			getPropertySet().setDataString(key, value);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new JspTagException(e.getMessage());
		}
        return EVAL_PAGE;
    }
	
	/**
	 * Sets the key attribute to the specified key.
	 * 
	 * @param key the key to use.
	 */
    public void setKey(final String key) 
	{
        this.key = key;
    }

	/**
	 * Sets the value attribute to the specified value.
	 * 
	 * @param key the value to use.
	 */
    public void setValue(final String value) throws JspException
	{
        this.value = evaluateString("SetPropertySetTag", "value", value);
    }
}
