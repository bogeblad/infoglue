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

import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * This taglib creates the needed html so a user can klick on a item and come to the edit view
 * of a content from the site. The same as rightclicking on a text in edit on sight but easier.
 * 
 * @author Mattias Bogeblad
 */

public class EditOnSightTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3257850991142318897L;
	
	private Integer contentId;
	private Integer languageId;
    private String attributeName;
    private String html;
    private boolean showInPublishedMode = false;
    
    public EditOnSightTag()
    {
        super();
    }
    
    public int doEndTag() throws JspException
    {
        if(languageId == null)
            this.languageId = getController().getLanguageId();
        
        produceResult(this.getController().getEditOnSightTag(contentId, languageId, attributeName, html, showInPublishedMode));
        
        this.contentId = null;
        this.languageId = null;
        this.attributeName = null;
        this.html = null;
        this.showInPublishedMode = false;
        
        return EVAL_PAGE;
    }    
   
    public void setContentId(final String contentId) throws JspException
    {
        this.contentId = evaluateInteger("EditOnSightTag", "contentId", contentId);
    }

    public void setLanguageId(final String languageId) throws JspException
    {
        this.languageId = evaluateInteger("EditOnSightTag", "languageId", languageId);
    }

    public void setAttributeName(final String attributeName) throws JspException
    {
        this.attributeName = evaluateString("EditOnSightTag", "attributeName", attributeName);
    }

    public void setHtml(final String html) throws JspException
    {
        this.html = evaluateString("EditOnSightTag", "html", html);
    }

    public void setShowInPublishedMode(boolean showInPublishedMode)
    {
        this.showInPublishedMode = showInPublishedMode;
    }
}