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

import javax.servlet.jsp.JspException;

import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * This tag will set a cookie value 
 */

public class SetCookieTag extends TemplateControllerTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 8603406098980150888L;
	
	/**
	 * The cookie name.
	 */
	private String name;

	/**
	 * The cookie value.
	 */
	private String value;
	
	/**
	 * The domain.
	 */
	private String domain;
	
	/**
	 * The path.
	 */
	private String path;
	
	/**
	 * The maxAge.
	 */
	private Integer maxAge;
	
	/**
	 * Default constructor.
	 */
	public SetCookieTag() 
	{
		super();
	}
	
	/**
	 * Process the end tag. Sets a cookie.  
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
	    this.getController().setCookie(name, value, domain, path, maxAge);
	    
        return EVAL_PAGE;
    }

    public void setDomain(String domain) throws JspException
    {
        this.domain = evaluateString("SetCookie", "domain", domain);
    }
    
    public void setMaxAge(String maxAge) throws JspException
    {
        this.maxAge = evaluateInteger("SetCookie", "maxAge", maxAge);
    }
    
    public void setName(String name) throws JspException
    {
        this.name = evaluateString("SetCookie", "name", name);
    }
    
    public void setPath(String path) throws JspException
    {
        this.path = evaluateString("SetCookie", "path", path);
    }
       
    public void setValue(String value) throws JspException
    {
        this.value = evaluateString("SetCookie", "value", value);
    }
}
