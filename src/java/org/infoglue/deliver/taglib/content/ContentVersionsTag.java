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

import java.util.List;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * Tag for org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController.getContentVersions(<String>, <Sring>, <boolean>);
 */

public class ContentVersionsTag extends TemplateControllerTag
{
	private static final long serialVersionUID = 3258135773294113587L;

	private ContentVO content;
	private Integer contentId;
	private Integer languageId;
	private boolean includeAllLanguages = false;
    
    public ContentVersionsTag()
    {
        super();
    }
    
    public int doStartTag() throws JspException 
    {        
        return EVAL_BODY_INCLUDE;
    }
    
    public int doEndTag() throws JspException
    {
		produceResult(getContentVersions());
        
		content = null;
		
		return EVAL_PAGE;
    }

	private List getContentVersions() throws JspException
	{	    
		if(content != null)
			contentId = content.getId();
		else if(contentId == null)
			throw new JspException("You must assign either content or contentId");
		
		if(!this.includeAllLanguages)
		{
		    if(this.languageId == null)
		        this.languageId = getController().getLanguageId();

		    return getController().getContentVersions(contentId, languageId);
		}
		else
		{
		    return getController().getContentVersions(contentId, null);			
		}
	}
	
    public void setContent(String content) throws JspException
    {
        this.content = (ContentVO)evaluate("contentVersions", "content", content, ContentVO.class);
    }

    public void setContentId(String contentId) throws JspException
    {
        this.contentId = evaluateInteger("contentVersions", "contentId", contentId);
    }

    public void setLanguageId(String languageId) throws JspException
    {
        this.languageId = evaluateInteger("contentVersions", "languageId", languageId);;
    }
    
    public void setIncludeAllLanguages(boolean includeAllLanguages)
    {
        this.includeAllLanguages = includeAllLanguages;
    }
    
}