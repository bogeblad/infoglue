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

import org.infoglue.deliver.taglib.TemplateControllerTag;

public class ContentTypeDefinitionAssetsTag extends TemplateControllerTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

	//private Integer contentTypeDefinitionId;
	private String schemaValue;
	
	public ContentTypeDefinitionAssetsTag()
    {
        super();
    }

	public int doStartTag() throws JspException 
    {        
        return EVAL_BODY_INCLUDE;
    }
    
	public int doEndTag() throws JspException
    {
	    //if(languageId != null)
	        setResultAttribute(getController().getContentTypeDefinitionAssetKeys(this.schemaValue));
	    /*
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
	    */
	        
        schemaValue = null;
        //languageCode = null;

	    return EVAL_PAGE;
    }

    public void setSchemaValue(final String schemaValue) throws JspException
    {
        this.schemaValue = evaluateString("contentTypeDefinitionAttributes", "schemaValue", schemaValue);
    }

}
