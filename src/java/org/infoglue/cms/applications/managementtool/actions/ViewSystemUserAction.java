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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.UserPropertiesController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;

public class ViewSystemUserAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewRoleAction.class.getName());

	private static final long serialVersionUID = 1L;

	private String userName;
	private boolean supportsUpdate = true;
	private InfoGluePrincipal infoGluePrincipal;
	private List roles = new ArrayList();
	private List assignedRoleVOList;
	private List groups = new ArrayList();
	private List assignedGroupVOList;
	private List contentTypeDefinitionVOList;   
	private List assignedContentTypeDefinitionVOList; 	
	
    protected void initialize(String userName) throws Exception
    {
		//this.supportsUpdate					= UserControllerProxy.getController().getSupportUpdate();
		this.infoGluePrincipal				= UserControllerProxy.getController().getUser(userName);
		
		if(infoGluePrincipal == null)
			throw new SystemException("No user found called '" + userName + "'. This could be an encoding issue if you gave your user a login name with non ascii chars in it. Look in the administrative manual on how to solve it.");

		this.supportsUpdate					= this.infoGluePrincipal.getAutorizationModule().getSupportUpdate();
					
		this.assignedRoleVOList 			= infoGluePrincipal.getRoles();
		if(this.supportsUpdate) //Only fetch if the user can edit.
			this.roles 						= this.infoGluePrincipal.getAutorizationModule().getRoles();
		
		this.assignedGroupVOList 			= infoGluePrincipal.getGroups();
		if(this.supportsUpdate) //Only fetch if the user can edit.
			this.groups 					= this.infoGluePrincipal.getAutorizationModule().getGroups();
		
		this.contentTypeDefinitionVOList 			= ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.EXTRANET_USER_PROPERTIES);
		this.assignedContentTypeDefinitionVOList 	= UserPropertiesController.getController().getContentTypeDefinitionVOList(userName);  
    } 

    public String doExecute() throws Exception
    {
        this.initialize(getUserName());
        
        return "success";
    }

    public String doV3() throws Exception
    {
        this.initialize(getUserName());
        
        return "successV3";
    }

	public List getAssignedRoles() throws Exception
	{
		return this.assignedRoleVOList;
	}        

	public List getAllRoles() throws Exception
	{
		return this.roles;
	}        

	public List getAssignedGroups() throws Exception
	{
		return this.assignedGroupVOList;
	}        

	public List getAllGroups() throws Exception
	{
		return this.groups;
	}        
    
	public List getContentTypeDefinitionVOList()
	{
		return contentTypeDefinitionVOList;
	}

	public String getUserName() 
	{
		return this.userName;
	}

	public void setUserName(String userName) throws Exception
	{
		if(userName != null)
		{
			logger.info("userName:" + userName);
			byte[] bytes = Base64.decodeBase64(userName);
			String decodedUserName = new String(bytes, "utf-8");
			logger.info("decodedUserName:" + decodedUserName);
			boolean userExists = false;
			try
			{
				userExists = UserControllerProxy.getController().userExists(decodedUserName);
			}
			catch (Exception e) 
			{
				logger.error("Error looking up user [" + decodedUserName + "]:" + e.getMessage());
			}
			
			if(userExists)
			{
				userName = decodedUserName;
			}
			else
			{
				logger.info("No match on base64-based userName:" + userName);
				if(!UserControllerProxy.getController().userExists(userName))
				{
					String fromEncoding = CmsPropertyHandler.getURIEncoding();
					String toEncoding = "utf-8";
					
					logger.info("userName:" + userName);
					String testUserName = new String(userName.getBytes(fromEncoding), toEncoding);
					if(logger.isInfoEnabled())
					{
						for(int i=0; i<userName.length(); i++)
							logger.info("c:" + userName.charAt(i) + "=" + (int)userName.charAt(i));
					}
					if(testUserName.indexOf((char)65533) == -1)
						userName = testUserName;
					
					logger.info("userName after:" + userName);
				}
			}
		}
		
		this.userName = userName;
	}

	public String getFirstName() 
	{
		return infoGluePrincipal.getFirstName();
	}
	
	public String getLastName() 
	{
		return infoGluePrincipal.getLastName();
	}

	public String getEmail() 
	{
		return infoGluePrincipal.getEmail();
	}
	
    public String getSource()
    {
        return infoGluePrincipal.getSource();
    }

    public Boolean getIsActive()
    {
        return infoGluePrincipal.getIsActive();
    }

    public Date getModifiedDateTime()
    {
        return infoGluePrincipal.getModifiedDateTime();
    }

	public boolean getSupportsUpdate()
	{
		return this.supportsUpdate;
	}
		
	public List getAssignedContentTypeDefinitionVOList()
	{
		return assignedContentTypeDefinitionVOList;
	}

}
