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
import javax.servlet.jsp.JspTagException;

import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * This tag is made to support that a user uploads a javascript plugin to a component as a zip-archive which shall be 
 * unpacked in the digitalAssets/extensions-directory. Another possible feature is to enable bundling, uniqueness and minification of javascript.
 * 
 * @author Mattias Bogeblad
 */

public class ScriptExtensionUrlTag extends ComponentLogicTag
{
	private static final long serialVersionUID = -1L;

	private Integer contentId;
    private String assetKey;
    private String fileNames;
    private Boolean autoCreateMarkup = true;
    private Boolean addToHTMLHeader = false;
    private Boolean addToHTMLBody = false;
    private Boolean addToBundledIncludes = false;
    private String bundleName;
    
    public ScriptExtensionUrlTag()
    {
        super();
    }
    
    public int doEndTag() throws JspException
    {
        try
        {
        	String scriptBaseUrl = "";
        	if(contentId == null)
        		this.contentId = getController().getComponentContentId();
        		
            produceResult(getController().getScriptExtensionUrls(this.contentId, this.assetKey, this.fileNames, this.autoCreateMarkup, this.addToHTMLHeader, this.addToHTMLBody, this.addToBundledIncludes, this.bundleName));
        }
        catch(Exception e)
        {
            throw new JspTagException("getScriptExtensionBaseUrl Error: " + e.getMessage());
        }
        
        this.contentId = null;
        this.assetKey = null;
        this.fileNames = null;
        this.autoCreateMarkup = true;
        this.addToHTMLHeader = false;
        this.addToHTMLBody = false;
        this.addToBundledIncludes = false;
        this.assetKey = null;
        
        return EVAL_PAGE;
    }

    public void setAssetKey(String assetKey) throws JspException
    {
        this.assetKey = evaluateString("scriptExtensionBaseUrl", "assetKey", assetKey);
    }

    public void setFileNames(String fileNames) throws JspException
    {
        this.fileNames = evaluateString("scriptExtensionBaseUrl", "fileNames", fileNames);
    }
    
    public void setAutoCreateMarkup(String autoCreateMarkup) throws JspException
    {
        this.autoCreateMarkup = (Boolean)evaluate("scriptExtensionBaseUrl", "autoCreateMarkup", autoCreateMarkup, Boolean.class);
    }
        
    public void setContentId(String contentId) throws JspException
    {
        this.contentId = evaluateInteger("assetUrl", "contentId", contentId);
    }

    public void setAddToHTMLHeader(String addToHTMLHeader) throws JspException
    {
        this.addToHTMLHeader = (Boolean)evaluate("scriptExtensionBaseUrl", "addToHTMLHeader", addToHTMLHeader, Boolean.class);
    }

    public void setAddToHTMLBody(String addToHTMLBody) throws JspException
    {
        this.addToHTMLBody = (Boolean)evaluate("scriptExtensionBaseUrl", "addToHTMLBody", addToHTMLBody, Boolean.class);
    }

    public void setAddToBundledIncludes(String addToBundledIncludes) throws JspException
    {
        this.addToBundledIncludes = (Boolean)evaluate("scriptExtensionBaseUrl", "addToBundledIncludes", addToBundledIncludes, Boolean.class);
    }

    public void setBundleName(String bundleName) throws JspException
    {
        this.bundleName = evaluateString("scriptExtensionBaseUrl", "bundleName", bundleName);
    }

}