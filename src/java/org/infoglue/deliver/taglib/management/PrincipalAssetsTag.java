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
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * Returns a list of assets connected to a user
 * 
 * @author Mattias Bogeblad
 */

public class PrincipalAssetsTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3546080250652931383L;

	private String userName;
	private InfoGluePrincipal principal;
	private Integer languageId = null;

    public PrincipalAssetsTag()
    {
        super();
    }
    
    public int doEndTag() throws JspException
    {
        try
        {
        	if(languageId == null)
        		languageId = getController().getLanguageId();
        	
			if(principal != null)
            {
	            produceResult(getController().getPrincipalAssets(principal, languageId));    
            }
            else if(userName != null)
            {
            	produceResult(getController().getPrincipalAssets(getController().getPrincipal(userName), languageId));
            }
            else
            {
            	produceResult(getController().getPrincipalAssets(getController().getPrincipal(), languageId));
            }
        }
        catch(Exception e)
        {
            throw new JspTagException("ComponentLogic.getAssetUrl Error: " + e.getMessage());
        }
        
        languageId = null;
        userName = null;
        principal = null;
        
        return EVAL_PAGE;
    }

    public void setUserName(String userName) throws JspException
    {
        this.userName = evaluateString("principalAssets", "userName", userName);
    }

    public void setLanguageId(final String languageIdString) throws JspException
    {
 	   this.languageId = this.evaluateInteger("principal", "languageId", languageIdString);
    }
}