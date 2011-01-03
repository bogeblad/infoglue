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

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.deliver.controllers.kernel.impl.simple.ComponentLogic;
import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * Tag for org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController.getContentAttribute(<String>, <Sring>, <boolean>);
 */
public class ContentVersionTag extends TemplateControllerTag
{
    private final static Logger logger = Logger.getLogger(ContentVersionTag.class.getName());

    private static final long serialVersionUID = 3258135773294113587L;

	private ContentVO content;
    private Integer languageId;
	private Integer contentVersionId;
	private boolean useLanguageFallback = true;
    
    public ContentVersionTag()
    {
        super();
    }
    
    public int doStartTag() throws JspException 
    {        
        return EVAL_BODY_INCLUDE;
    }
    
    public int doEndTag() throws JspException
    {
		produceResult(getContentVersion());
        
        this.languageId = null;
        this.useLanguageFallback = true;
        this.content = null;

		return EVAL_PAGE;
    }

	private ContentVersionVO getContentVersion() throws JspException
	{	    
	    if(this.languageId == null)
	        this.languageId = getController().getLanguageId();
	    
	    if(this.contentVersionId != null)
	    	return getController().getContentVersionById(contentVersionId);
	    else if(this.content != null)
	    	return getController().getContentVersion(content.getContentId(), languageId, useLanguageFallback);
	    else
	    	throw new JspException("Must state either content or contentVersionId");
	}
	
    public void setContentVersionId(String contentVersionId) throws JspException
    {
        this.contentVersionId = evaluateInteger("contentVersion", "contentVersionId", contentVersionId);;
    }

    public void setContent(String contentExp) throws JspException
    {
    	try
    	{
    		this.content = (ContentVO)evaluate("contentVersion", "content", contentExp, ContentVO.class);
    	}
    	catch (Throwable t) 
    	{
    		logger.warn("Content evaluated wrong:" + t.getMessage());
		}
    }
    
    public void setLanguageId(String languageId) throws JspException
    {
        this.languageId = evaluateInteger("contentVersion", "languageId", languageId);;
    }
    
    public void setUseLanguageFallback(boolean useLanguageFallback)
    {
        this.useLanguageFallback = useLanguageFallback;
    }
}