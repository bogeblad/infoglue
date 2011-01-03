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

package org.infoglue.deliver.taglib.structure;

import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.StringManager;
import org.infoglue.cms.util.StringManagerFactory;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * This taglib creates the needed html so a user can click on a link and get the save page part dialog.
 * 
 * @author Mattias Bogeblad
 */

public class SavePagePartTemplateTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3257850991142318897L;
	
	private String html;
    private boolean showInPublishedMode = false;
    private boolean showInDecoratedMode = true;
    
    public int doEndTag() throws JspException
    {
    	
        produceResult(getSavePagePartTemplateHTML());
        
        html = null;
        showInPublishedMode = false;
        showInDecoratedMode = false;
        
        return EVAL_PAGE;
    }

    private String getSavePagePartTemplateHTML()
    {
    	String result = "";
    	
    	try
    	{
	    	String componentEditorUrl 	= "" + CmsPropertyHandler.getComponentEditorUrl();
			
			if(html == null)
			{
				InfoGluePrincipal principal = getController().getPrincipal();
			    String cmsUserName = (String)getController().getHttpServletRequest().getSession().getAttribute("cmsUserName");
			    if(cmsUserName != null && !CmsPropertyHandler.getAnonymousUser().equalsIgnoreCase(cmsUserName))
				    principal = getController().getPrincipal(cmsUserName);

				Locale locale = getController().getLocaleAvailableInTool(principal);
				String savePagePartTemplateHTML = getLocalizedString(locale, "deliver.editOnSight.savePagePartTemplateHTML");
				html = "<a href=\"javascript:saveComponentStructure('$saveUrl');\">" + savePagePartTemplateHTML + "</a>";
			}
			
			String url = "" + componentEditorUrl + "CreatePageTemplate!input.action?contentId=" + this.getController().getSiteNode().getMetaInfoContentId() + "&componentId=" + this.getComponentLogic().getInfoGlueComponent().getId();
			result = html.replaceAll("\\$saveUrl", url);
    	}
    	catch (Exception e) 
    	{
    		e.printStackTrace();
    	}
    	
		return result;
    }
    
    public String getLocalizedString(Locale locale, String key) 
  	{
    	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);
    	return stringManager.getString(key);
  	}
    
    public void setHtml(final String html) throws JspException
    {
        this.html = evaluateString("EditOnSightTag", "html", html);
    }

    public void setShowInPublishedMode(boolean showInPublishedMode)
    {
        this.showInPublishedMode = showInPublishedMode;
    }
    
	public void setShowInDecoratedMode(boolean showInDecoratedMode) throws JspException
	{
		this.showInDecoratedMode = showInDecoratedMode;
	}
}