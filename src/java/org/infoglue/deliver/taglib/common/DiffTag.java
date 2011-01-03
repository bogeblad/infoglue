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

import org.infoglue.deliver.taglib.AbstractTag;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * This tag display a diff view of two texts  
 */

public class DiffTag extends ComponentLogicTag 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The original text.
	 */
	private String originalText;

	/**
	 * The text indicating that there is more text
	 */
	private String modifiedText;

	/**
	 * Process the end tag. Crops the text and adds the suffix.  
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		String contextPath = getController().getHttpServletRequest().getContextPath();
		StringBuffer sb = new StringBuffer("<script type=\"text/javascript\" src=\"" + contextPath + "/script/jsdiff.js\"></script>");

		sb.append("\n<script type=\"text/javascript\">\n");
		sb.append("<!--\n");
		sb.append("var value1 = \"" + originalText.replaceAll("\"", "'") + "\";\n");
		sb.append("var value2 = \"" + modifiedText.replaceAll("\"", "'") + "\";\n");
		sb.append("document.write(\"\" + diffString(value1, value2) + \"\");\n");
		sb.append("-->\n");
		sb.append("</script>\n");

	    setResultAttribute(sb.toString());
	    
        return EVAL_PAGE;
    }

    public void setOriginalText(String originalText) throws JspException
    {
        this.originalText = evaluateString("diffTag", "originalText", originalText);
    }
    
    public void setModifiedText(String modifiedText) throws JspException
    {
        this.modifiedText = evaluateString("diffTag", "modifiedText", modifiedText);
    }
    
}
