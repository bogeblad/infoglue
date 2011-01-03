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
 * This tag takes a string and writes it to file and returns it as an assetUrl instead.
 * 
 * <%@ taglib uri="infoglue" prefix="infoglue" %>
 * 
 * <content:assetUrlFromString id="myAssetUrl" data="I test this" filePrefix="MyTest" fileSuffix="txt"/>
 *
 * @author Mattias Bogeblad
 */

public class AssetUrlFromStringTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3546080250652931383L;

	private String data;
	private String fileNamePrefix;
	private String fileNameSuffix;
	    
    public int doEndTag() throws JspException
    {
        try
        {
            produceResult(getController().getStringAsDigitalAssetUrl(data, fileNamePrefix, fileNameSuffix));
        }
        catch(Exception e)
        {
            throw new JspTagException("getStringAsDigitalAssetUrl Error: " + e.getMessage());
        }
        
        return EVAL_PAGE;
    }

    public void setData(String data) throws JspException
    {
        this.data = evaluateString("assetUrlFromString", "data", data);
    }

    public void setFileNamePrefix(String fileNamePrefix) throws JspException
    {
        this.fileNamePrefix = evaluateString("assetUrlFromString", "fileNamePrefix", fileNamePrefix);
    }

    public void setFileNameSuffix(String fileNameSuffix) throws JspException
    {
        this.fileNameSuffix = evaluateString("assetUrlFromString", "fileNameSuffix", fileNameSuffix);
    }

}