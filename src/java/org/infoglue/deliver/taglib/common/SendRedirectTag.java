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

public class SendRedirectTag extends TemplateControllerTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 8603406098980150888L;
	
	/**
	 * The cookie name.
	 */
	private String url;
	
	/**
	 * Process the end tag. Sends the redirect.  
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		try
		{
			this.getController().getDeliveryContext().getHttpServletResponse().sendRedirect(url);
		}
		catch (Exception e) 
		{
			throw new JspException("Error when redirecting: " + e.getMessage());
		}
        
		return EVAL_PAGE;
    }

    public void setUrl(String url) throws JspException
    {
        this.url = evaluateString("SendRedirect", "url", url);
    }
}
