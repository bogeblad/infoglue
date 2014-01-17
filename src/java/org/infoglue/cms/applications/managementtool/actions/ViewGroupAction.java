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

package org.infoglue.cms.applications.managementtool.actions;

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupPropertiesController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.util.CmsPropertyHandler;

public class ViewGroupAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewGroupAction.class.getName());

	private static final long serialVersionUID = 1L;

	private String groupName;
	private boolean supportsUpdate = true;
	private InfoGlueGroup infoGlueGroup;
	private List contentTypeDefinitionVOList;
	private List assignedContentTypeDefinitionVOList;    
	
	/**
	 * This method initializes the view by populating all the entities. 
	 * It fetches the group itself, the type of authorization update support and all the assigned principals.
	 * It then populates a list of unassigned principals.
	 */
    protected void initialize(String groupName) throws Exception
    {
		this.infoGlueGroup				= GroupControllerProxy.getController().getGroup(groupName);
		this.supportsUpdate 			= this.infoGlueGroup.getAutorizationModule().getSupportUpdate();
		
		this.contentTypeDefinitionVOList 			= ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.EXTRANET_GROUP_PROPERTIES);
		this.assignedContentTypeDefinitionVOList 	= GroupPropertiesController.getController().getContentTypeDefinitionVOList(groupName);  
    } 

    public String doExecute() throws Exception
    {
        this.initialize(getGroupName());
        
        return "success";
    }
     
    public String doV3() throws Exception
    {
        this.initialize(getGroupName());
        
        return "successV3";
    }

    public String getGroupName()
    {
        return groupName;
    }

	public void setGroupName(String groupName) throws Exception
	{
		if(groupName != null)
		{
			logger.info("groupName:" + groupName);
			byte[] bytes = Base64.decodeBase64(groupName);
			String decodedGroupName = new String(bytes, "utf-8");
			logger.info("decodedGroupName:" + decodedGroupName);
			try
			{
				if(GroupControllerProxy.getController().groupExists(decodedGroupName))
				{
					groupName = decodedGroupName;
				}
				else
				{
					logger.info("No match on base64-based groupName:" + groupName);
					
					String fromEncoding = CmsPropertyHandler.getURIEncoding();
					String toEncoding = "utf-8";
					
					logger.info("groupName:" + groupName);
					String testGroupName = new String(groupName.getBytes(fromEncoding), toEncoding);
					if(logger.isInfoEnabled())
					{
						for(int i=0; i<groupName.length(); i++)
							logger.info("c:" + groupName.charAt(i) + "=" + (int)groupName.charAt(i));
					}
					if(testGroupName.indexOf((char)65533) == -1)
						groupName = testGroupName;
					
					logger.info("groupName after:" + groupName);
				}
			}
			catch (Exception e) 
			{
				logger.error("Error getting group: " + e.getMessage());
				logger.warn("Error getting group: " + e.getMessage(), e);
				throw e;
			}
		}
		
		this.groupName = groupName;
	}
            
    public java.lang.String getDescription()
    {
        return this.infoGlueGroup.getDescription();
    }

    public java.lang.String getSource()
    {
        return this.infoGlueGroup.getSource();
    }

    public java.lang.String getGroupType()
    {
        return this.infoGlueGroup.getGroupType();
    }

    public Boolean getIsActive()
    {
    	return this.infoGlueGroup.getIsActive();
    }

    public java.util.Date getModifiedDateTime()
    {
        return this.infoGlueGroup.getModifiedDateTime();
    }
	
	public List getAssignedContentTypeDefinitionVOList()
	{
		return assignedContentTypeDefinitionVOList;
	}

	public List getContentTypeDefinitionVOList()
	{
		return contentTypeDefinitionVOList;
	}

	public void setAssignedContentTypeDefinitionVOList(List list)
	{
		assignedContentTypeDefinitionVOList = list;
	}

	public void setContentTypeDefinitionVOList(List list)
	{
		contentTypeDefinitionVOList = list;
	}

	public boolean getSupportsUpdate()
	{
		return this.supportsUpdate;
	}
}
