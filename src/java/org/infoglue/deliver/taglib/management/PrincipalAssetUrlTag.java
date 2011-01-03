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

import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.taglib.component.ComponentLogicTag;

/**
 * Asset url for principal asset
 *
 * @author Mattias Bogeblad
 */

public class PrincipalAssetUrlTag extends ComponentLogicTag
{
	private static final long serialVersionUID = 3546080250652931383L;

	private Integer digitalAssetId;
	private String userName;
	private InfoGluePrincipal principal;
    private String assetKey;
	private Integer languageId = null;

    public PrincipalAssetUrlTag()
    {
        super();
    }
    
    public int doEndTag() throws JspException
    {
        try
        {
			if(digitalAssetId != null)
			{
                produceResult(getController().getAssetUrlForAssetWithId(digitalAssetId));
			}
			else
			{
				if(languageId == null)
	        		languageId = getController().getLanguageId();
	        	
				DigitalAssetVO assetVO = null;
				if(principal != null)
	            {
					assetVO = getController().getPrincipalAsset(principal, assetKey, languageId);    
	            }
	            else if(userName != null)
	            {
	            	assetVO = getController().getPrincipalAsset(getController().getPrincipal(userName), assetKey, languageId);
	            }
	            else
	            {
	            	assetVO = getController().getPrincipalAsset(getController().getPrincipal(), assetKey, languageId);
	            }
				
				//System.out.println("assetVO:" + assetVO);
				if(assetVO != null)
					produceResult(getController().getAssetUrlForAssetWithId(assetVO.getId()));
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

    public void setDigitalAssetId(String digitalAssetId) throws JspException
    {
        this.digitalAssetId = evaluateInteger("principalAssetUrl", "digitalAssetId", digitalAssetId);
    }

    public void setUserName(String userName) throws JspException
    {
        this.userName = evaluateString("principalAssetUrl", "userName", userName);
    }

    public void setAssetKey(String assetKey) throws JspException
    {
        this.assetKey = evaluateString("principalAssetUrl", "assetKey", assetKey);
    }
    
    public void setLanguageId(final String languageIdString) throws JspException
    {
 	   this.languageId = this.evaluateInteger("principal", "languageId", languageIdString);
    }
}