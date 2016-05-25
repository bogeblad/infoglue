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

package org.infoglue.cms.applications.contenttool.actions;

import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionControllerProxy;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;

/**
 *
 * @author Mattias Bogeblad
 * 
 * Present a comparison between two content versions.
 */

public class ViewContentVersionDifferenceAction extends InfoGlueAbstractAction 
{
	private static final long serialVersionUID = 1L;
	
	private Integer contentVersionId1;
	private Integer contentVersionId2;

	private ContentVersionVO contentVersionVO1;
	private ContentVersionVO contentVersionVO2;
	
	public ContentTypeDefinitionVO contentTypeDefinitionVO;

	public List attributes = null;

	
	protected String doExecute() throws Exception 
	{
		logUserActionInfo(getClass(), "doExecute");
        this.contentVersionVO1 = ContentVersionControllerProxy.getController().getACContentVersionVOWithId(this.getInfoGluePrincipal(), contentVersionId1);    		 	
        this.contentVersionVO2 = ContentVersionControllerProxy.getController().getACContentVersionVOWithId(this.getInfoGluePrincipal(), contentVersionId2);    		 	

        this.contentTypeDefinitionVO = ContentController.getContentController().getContentTypeDefinition(contentVersionVO2.getContentId());

        if(this.contentTypeDefinitionVO != null)
        {
            this.attributes = ContentTypeDefinitionController.getController().getContentTypeAttributes(this.contentTypeDefinitionVO, true);
        }

	    return "success";
	}
		
    
	/**
	 * This method fetches a value from the xml that is the contentVersions Value. If the 
	 * contentVersioVO is null the contentVersion has not been created yet and no values are present.
	 */
	 
	public String getUnescapedAttributeValue(ContentVersionVO contentVersionVO, String key)
	{
		String value = "";
		
		if(contentVersionVO != null)
		{
			try
			{
				String xml = contentVersionVO.getVersionValue();
				
				int startTagIndex = xml.indexOf("<" + key + ">");
				int endTagIndex   = xml.indexOf("]]></" + key + ">");

				if(startTagIndex > 0 && startTagIndex < xml.length() && endTagIndex > startTagIndex && endTagIndex <  xml.length())
				{
					value = xml.substring(startTagIndex + key.length() + 11, endTagIndex);
				}					
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		//logger.info("value:" + value);	
		return value;
	}

	
    public Integer getContentVersionId1()
    {
        return contentVersionId1;
    }
    
    public void setContentVersionId1(Integer contentVersionId1)
    {
        this.contentVersionId1 = contentVersionId1;
    }
    
    public Integer getContentVersionId2()
    {
        return contentVersionId2;
    }
    
    public void setContentVersionId2(Integer contentVersionId2)
    {
        this.contentVersionId2 = contentVersionId2;
    }
    
    public List getAttributes()
    {
        return attributes;
    }
    
    public ContentTypeDefinitionVO getContentTypeDefinitionVO()
    {
        return contentTypeDefinitionVO;
    }
    
    public ContentVersionVO getContentVersionVO1()
    {
        return contentVersionVO1;
    }
    
    public ContentVersionVO getContentVersionVO2()
    {
        return contentVersionVO2;
    }
}
