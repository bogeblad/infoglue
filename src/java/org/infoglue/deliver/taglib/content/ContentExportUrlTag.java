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
import java.util.ArrayList;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * This tag exports one or many contents into a xml-file.
 * 
 * <%@ taglib uri="infoglue" prefix="infoglue" %>
 * 
 * <content:contentExportUrl id="myExportUrl" contentId="2423" fileNamePrefix="KomponentX"/>
 *
 * @author Mattias Bogeblad
 */

public class ContentExportUrlTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3546080250652931383L;

	private List contentIdList = new ArrayList();
	private String fileNamePrefix = "ContentExport";
	private boolean includeContentTypeDefinitions = true;
	private boolean includeCategories = true;
	
	public int doEndTag() throws JspException
    {
    	if(contentIdList == null || contentIdList.size() == 0)
    		throw new JspTagException("You must state either contentId or contentIdList");

    	try
        {
            produceResult(getController().getExportedContentsUrl(contentIdList, fileNamePrefix, includeContentTypeDefinitions, includeCategories));
        }
        catch(Exception e)
        {
            throw new JspTagException("ContentExportUrlTag Error: " + e.getMessage());
        }
        
        contentIdList.clear();
        
        return EVAL_PAGE;
    }

    public void setContentId(String contentIdString) throws JspException
    {
        Integer contentId = evaluateInteger("ContentExportUrlTag", "contentId", contentIdString);
        if(contentId != null)
        	contentIdList.add(contentId);
        else
        	throw new JspException("Wrong argument contentId:" + contentIdString);
    }

    public void setContentIdList(String contentIdList) throws JspException
    {
    	this.contentIdList = evaluateList("ContentExportUrlTag", "contentIdList", contentIdList);
    }

    public void setFileNamePrefix(String fileNamePrefix) throws JspException
    {
        this.fileNamePrefix = evaluateString("assetUrlFromString", "fileNamePrefix", fileNamePrefix);
    }

	public void setIncludeContentTypeDefinitions(boolean includeContentTypeDefinitions)
	{
		this.includeContentTypeDefinitions = includeContentTypeDefinitions;
	}

	public void setIncludeCategories(boolean includeCategories)
	{
		this.includeCategories = includeCategories;
	}


}