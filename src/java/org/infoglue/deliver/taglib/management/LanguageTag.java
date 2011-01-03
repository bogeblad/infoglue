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

package org.infoglue.deliver.taglib.management;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.deliver.taglib.TemplateControllerTag;

public class LanguageTag extends TemplateControllerTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

	private Integer languageId;
	private String languageCode;
	
	public LanguageTag()
    {
        super();
    }

	public int doStartTag() throws JspException 
    {        
        return EVAL_BODY_INCLUDE;
    }
    
	public int doEndTag() throws JspException
    {
	    if(languageId != null)
	        setResultAttribute(getController().getLanguage(languageId));
	    else if(languageCode != null)
	    {
	        LanguageVO languageVO = getController().getLanguage(languageCode);
	        if(languageVO != null)
	            setResultAttribute(languageVO);
		    else
		        throw new JspException("There was no valid language with the languageCode:" + languageCode);
	    }
	    else
	        throw new JspException("Must state either languageId or languageCode");
	    
        languageId = null;
        languageCode = null;

	    return EVAL_PAGE;
    }

    public void setLanguageId(final String languageId) throws JspException
    {
        this.languageId = evaluateInteger("language", "languageId", languageId);
    }

    public void setLanguageCode(final String languageCode) throws JspException
    {
        this.languageCode = evaluateString("language", "languageCode", languageCode);
    }

}
