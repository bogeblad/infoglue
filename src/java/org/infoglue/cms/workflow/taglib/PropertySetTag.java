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

/**
 * This class implements the &lt;iw:property&gt; tag, which fetches a value from the propertyset 
 * associated with the workflow and
 * 
 * - stores it as a string in a page context variable (if the <code>id</code> attribute is set).
 * - writes the value to the output stream.
 */
public class PropertySetTag extends WorkflowTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = -5671251095976313163L;

	/**
	 * The lookup key.
	 */
	private String key;
	
	/**
	 * Default constructor.
	 */
    public PropertySetTag() 
    {
        super();
    }

	/**
	 * Process the end tag. Fetches the value from the propertyset using the specified key and
	 * either stores the result in a context variable or writes it to the output stream.
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurs when producing the result.
	 */
	public int doEndTag() throws JspException {
		produceResult(getPropertySet().getAsString(key));
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
}
