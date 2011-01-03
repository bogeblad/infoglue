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

public class AssignPropertyBindingTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3257850991142318897L;
	
	private String propertyName;
	private boolean createNew = false;
	private String html;
    private boolean showInPublishedMode = false;
    private String showDecoratedString = null;
    private boolean showDecorated = true;
    private String extraParameters = null;
    private boolean hideComponentPropertiesOnLoad = true;
    
    public AssignPropertyBindingTag()
    {
        super();
    }
    
    public int doEndTag() throws JspException
    {
    	if(showDecoratedString == null)
    		showDecoratedString = "" + this.getController().getIsDecorated();
    	showDecorated = new Boolean(showDecoratedString).booleanValue();
    	
        produceResult(this.getController().getAssignPropertyBindingTag(propertyName, createNew, html, showInPublishedMode, showDecorated, extraParameters, hideComponentPropertiesOnLoad));
        
        showDecoratedString = null;
        
        return EVAL_PAGE;
    }
   
    public void setPropertyName(final String propertyName) throws JspException
    {
        this.propertyName = evaluateString("AssignPropertyBindingTag", "propertyName", propertyName);
    }

    public void setCreateNew(boolean createNew)
    {
        this.createNew = createNew;
    }

    public void setHtml(final String html) throws JspException
    {
        this.html = evaluateString("AssignPropertyBindingTag", "html", html);
    }

    public void setShowInPublishedMode(boolean showInPublishedMode)
    {
        this.showInPublishedMode = showInPublishedMode;
    }
    
	public void setShowDecorated(boolean showDecorated) throws JspException
	{
		this.showDecoratedString = evaluateString("AssignPropertyBindingTag", "showDecoratedString", showDecoratedString);;
	}

    public void setExtraParameters(final String extraParameters) throws JspException
    {
        this.extraParameters = evaluateString("AssignPropertyBindingTag", "extraParameters", extraParameters);
    }
    
    public void setHideComponentPropertiesOnLoad(final boolean hideComponentPropertiesOnLoad) throws JspException
    {
        this.hideComponentPropertiesOnLoad = hideComponentPropertiesOnLoad;
    }
}