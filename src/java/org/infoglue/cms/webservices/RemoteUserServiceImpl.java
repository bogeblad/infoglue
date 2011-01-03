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

package org.infoglue.cms.webservices;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.webservices.elements.StatusBean;
import org.infoglue.common.security.beans.InfoGlueGroupBean;
import org.infoglue.common.security.beans.InfoGluePrincipalBean;
import org.infoglue.common.security.beans.InfoGlueRoleBean;
import org.infoglue.deliver.util.webservices.DynamicWebserviceSerializer;


/**
 * This class is responsible for letting an external application call InfoGlue
 * API:s remotely. It handles api:s to manage user properties.
 * 
 * @author Mattias Bogeblad
 */

public class RemoteUserServiceImpl extends RemoteInfoGlueService
{
    private final static Logger logger = Logger.getLogger(RemoteUserServiceImpl.class.getName());

	/**
	 * The principal executing the workflow.
	 */
	private InfoGluePrincipal principal;

    private static UserControllerProxy userControllerProxy = UserControllerProxy.getController();
    
    /**
     * Registers a new system user.
     */
    
    public Boolean createUser(final String principalName, String firstName, String lastName, String email, String userName, String password, List roleNames, List groupNames) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new Boolean(false);
        }

        Boolean status = new Boolean(true);
        
        logger.info("***************************************");
        logger.info("Creating user through webservice.......");
        logger.info("***************************************");
        
        try
        {
            initializePrincipal(principalName);
            
            SystemUserVO systemUserVO = new SystemUserVO();
            systemUserVO.setFirstName(firstName);
            systemUserVO.setLastName(lastName);
            systemUserVO.setEmail(email);
            systemUserVO.setUserName(userName);
            systemUserVO.setPassword(password);
            
            Object[] roleNamesArray = roleNames.toArray();
            Object[] groupNamesArray = groupNames.toArray();
            
            String[] roles = new String[roleNamesArray.length];
            String[] groups = new String[groupNamesArray.length];
            
            for(int i=0; i<roleNamesArray.length; i++)
            	roles[i] = "" + roleNamesArray[i];

            for(int i=0; i<groupNamesArray.length; i++)
            	groups[i] = "" + groupNamesArray[i];

            userControllerProxy.createUser(systemUserVO);
            userControllerProxy.updateUser(systemUserVO, roles, groups);
        }
        catch(Exception e)
        {
        	status = new Boolean(false);
            logger.error("En error occurred when we tried to create a new contentVersion:" + e.getMessage(), e);
        }
        
        updateCaches();

        return status;    
    }

    /**
     * Updates a system user.
     */
    
    public Boolean updateUser(final String principalName, String firstName, String lastName, String userName, String password, String email, String[] roleNames, String[] groupNames) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new Boolean(false);
        }

        Boolean status = new Boolean(true);
        
        logger.info("***************************************");
        logger.info("Updating user through webservice.......");
        logger.info("***************************************");
        
        try
        {
            initializePrincipal(principalName);
            
            SystemUserVO systemUserVO = new SystemUserVO();
            systemUserVO.setUserName(userName);
            systemUserVO.setEmail(email);
            systemUserVO.setFirstName(firstName);
            systemUserVO.setLastName(lastName);
            systemUserVO.setPassword(password);
            
            if(roleNames != null && roleNames.length > 0)
            	roleNames = null;
            if(groupNames != null && groupNames.length > 0)
            	groupNames = null;

            userControllerProxy.updateUser(systemUserVO, roleNames, groupNames);
        }
        catch(Exception e)
        {
        	status = new Boolean(false);
            logger.error("En error occurred when we tried to create a new contentVersion:" + e.getMessage(), e);
        }
        
        updateCaches();

        return status;    
    }

    /**
     * Updates a system user.
     */
    
    public StatusBean updateUser(final String principalName, final Object[] inputsArray, String[] roleNames, String[] groupNames) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new StatusBean(false, "You are not allowed to talk to this service");
        }

        StatusBean statusBean = new StatusBean(true, "ok");
    	
        logger.info("***************************************");
        logger.info("Updating user through webservice.......");
        logger.info("***************************************");
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
            List users = (List) serializer.deserialize(inputsArray);
	        logger.info("users:" + users);

            initializePrincipal(principalName);
            
	        Iterator usersIterator = users.iterator();
	        while(usersIterator.hasNext())
	        {
	            Map userMap = (Map)usersIterator.next();
	            
	            Boolean isPasswordChangeOperation 	= (Boolean)userMap.get("isPasswordChangeOperation");
	            Boolean isPasswordResetOperation	= (Boolean)userMap.get("isPasswordResetOperation");
	            
	            String firstName 					= (String)userMap.get("firstName");
	            String lastName 					= (String)userMap.get("lastName");
	            String email 						= (String)userMap.get("email");
	            String userName 					= (String)userMap.get("userName");
	            String password 					= (String)userMap.get("password");
	            String oldPassword 					= (String)userMap.get("oldPassword");

	            if(isPasswordChangeOperation)
	            {
	            	logger.info("isPasswordChangeOperation");
	            	logger.info("userName:" + userName);
	            	logger.info("oldPassword:" + oldPassword);
	            	logger.info("password:" + password);
	            	userControllerProxy.updateUserPassword(userName, oldPassword, password);
	            }
	            else if(isPasswordResetOperation)
	            {
	            	logger.info("isPasswordResetOperation");
	            	userControllerProxy.updateUserPassword(userName);
	            }
	            else
	            {
	            	logger.info("isUserUpdateOperation");
		            SystemUserVO systemUserVO = new SystemUserVO();
		            systemUserVO.setEmail(email);
		            systemUserVO.setFirstName(firstName);
		            systemUserVO.setLastName(lastName);
		            systemUserVO.setPassword(password);
		            systemUserVO.setUserName(userName);
		            
		            if(roleNames != null && roleNames.length == 0)
		            	roleNames = null;
		            if(groupNames != null && groupNames.length == 0)
		            	groupNames = null;
		            
		            userControllerProxy.updateUser(systemUserVO, oldPassword, roleNames, groupNames);
	            }
	        }
        }
        catch(Throwable e)
        {
        	statusBean.setStatus(false);
        	statusBean.setMessage("En error occurred when we tried to update one or more users:" + e.getMessage());
            logger.error("En error occurred when we tried to update one or more users:" + e.getMessage(), e);
        }
        
        updateCaches();

        return statusBean;    
    }

    /**
     * Deletes a system user.
     */
    
    public Boolean deleteUser(final String principalName, SystemUserVO systemUserVO) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new Boolean(false);
        }

        Boolean status = new Boolean(true);
        
        logger.info("***************************************");
        logger.info("Delete user through webservice.........");
        logger.info("***************************************");
        
        try
        {
            initializePrincipal(principalName);
            
            userControllerProxy.deleteUser(systemUserVO.getUserName());
        }
        catch(Exception e)
        {
        	status = new Boolean(false);
            logger.error("En error occurred when we tried to create a new contentVersion:" + e.getMessage(), e);
        }
        
        updateCaches();

        return status;    
    }

    /**
     * Gets all roles available.
     */
    
    public List<InfoGlueRoleBean> getRoles() 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return null;
        }

        List<InfoGlueRoleBean> roles = new ArrayList<InfoGlueRoleBean>();
        
        logger.info("***************************************");
        logger.info("Getting all roles through webservice...");
        logger.info("***************************************");
        
        try
        {
            List rolesList = RoleControllerProxy.getController().getAllRoles();
            
            Iterator rolesListIterator = rolesList.iterator();
            while(rolesListIterator.hasNext())
            {
            	InfoGlueRole role = (InfoGlueRole)rolesListIterator.next();
            	InfoGlueRoleBean bean = new InfoGlueRoleBean();
            	bean.setName(role.getName());
            	bean.setDisplayName(role.getDisplayName());
            	bean.setDescription(role.getDescription());
            	roles.add(bean);	
            }
        }
        catch(Exception e)
        {
        	logger.error("En error occurred when we tried to create a new contentVersion:" + e.getMessage(), e);
        }
        
        return roles;    
    }

    /**
     * Gets all roles available.
     */
    
    public List<InfoGlueGroupBean> getGroups() 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return null;
        }

        List<InfoGlueGroupBean> groups = new ArrayList<InfoGlueGroupBean>();
        
        logger.info("***************************************");
        logger.info("Getting all groups through webservice..");
        logger.info("***************************************");
        
        try
        {
            List groupsList = GroupControllerProxy.getController().getAllGroups();

            Iterator groupsListIterator = groupsList.iterator();
            while(groupsListIterator.hasNext())
            {
            	InfoGlueGroup group = (InfoGlueGroup)groupsListIterator.next();
            	InfoGlueGroupBean bean = new InfoGlueGroupBean();
            	bean.setName(group.getName());
            	bean.setDisplayName(group.getDisplayName());
            	bean.setDescription(group.getDescription());
            	groups.add(bean);	
            }
        }
        catch(Exception e)
        {
        	logger.error("En error occurred when we tried to create a new contentVersion:" + e.getMessage(), e);
        }
        
        return groups;    
    }


    /**
     * Gets a principal.
     */
    
    public InfoGluePrincipalBean getPrincipal(String userName) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return null;
        }

        InfoGluePrincipalBean bean = null;
        
        logger.info("***************************************");
        logger.info("Getting all principals through webservice..");
        logger.info("***************************************");
        
        try
        {
            InfoGluePrincipal principal = UserControllerProxy.getController().getUser(userName);
            
            if(principal != null)
            {
	        	bean = new InfoGluePrincipalBean();
	        	bean.setName(principal.getName());
	        	bean.setDisplayName(principal.getDisplayName());
	        	bean.setEmail(principal.getEmail());
	        	bean.setFirstName(principal.getFirstName());
	        	bean.setLastName(principal.getLastName());
	        	bean.setAdministrator(false);
	        	bean.setMetaInformation(principal.getMetaInformation());
	        	
	        	List groups = new ArrayList();
	        	Iterator groupsListIterator = principal.getGroups().iterator();
	            while(groupsListIterator.hasNext())
	            {
	            	InfoGlueGroup group = (InfoGlueGroup)groupsListIterator.next();
	            	InfoGlueGroupBean groupBean = new InfoGlueGroupBean();
	            	groupBean.setName(group.getName());
	            	groupBean.setDisplayName(group.getDisplayName());
	            	groupBean.setDescription(group.getDescription());
	            	groups.add(groupBean);	
	            }
	            bean.setGroups(groups);
	
	        	List roles = new ArrayList();
	        	Iterator rolesListIterator = principal.getRoles().iterator();
	            while(rolesListIterator.hasNext())
	            {
	            	InfoGlueRole role = (InfoGlueRole)rolesListIterator.next();
	            	InfoGlueRoleBean roleBean = new InfoGlueRoleBean();
	            	roleBean.setName(role.getName());
	            	roleBean.setDisplayName(role.getDisplayName());
	            	roleBean.setDescription(role.getDescription());
	            	roles.add(roleBean);	
	            }
	            bean.setRoles(roles);
            }
            else
            {
            	logger.error("User asked for was not in the system:" + userName);
            	bean = new InfoGluePrincipalBean();
	        	bean.setName(userName);
	        	bean.setDisplayName(userName);
	        	bean.setEmail("mattias.bogeblad@modul1.se");
	        	bean.setFirstName("Not valid user");
	        	bean.setLastName("");
	        	bean.setAdministrator(false);
	        	
	        	List groups = new ArrayList();
	            bean.setGroups(groups);
	
	        	List roles = new ArrayList();
	            bean.setRoles(roles);
            }
        }
        catch(Exception e)
        {
        	logger.error("En error occurred when we tried to create a new contentVersion:" + e.getMessage(), e);
        }
        
        return bean;    
    }

    /**
     * Gets all roles available.
     */
    
    public List<InfoGluePrincipalBean> getPrincipals() 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return null;
        }

        List<InfoGluePrincipalBean> users = new ArrayList<InfoGluePrincipalBean>();
        
        logger.info("***************************************");
        logger.info("Getting all principals through webservice..");
        logger.info("***************************************");
        
        try
        {
            List principalList = UserControllerProxy.getController().getAllUsers();

            Iterator principalListIterator = principalList.iterator();
            while(principalListIterator.hasNext())
            {
            	InfoGluePrincipal principal = (InfoGluePrincipal)principalListIterator.next();
            	InfoGluePrincipalBean bean = new InfoGluePrincipalBean();
            	bean.setName(principal.getName());
            	bean.setDisplayName(principal.getDisplayName());
            	bean.setEmail(principal.getEmail());
            	bean.setFirstName(principal.getFirstName());
            	bean.setLastName(principal.getLastName());
            	bean.setAdministrator(false);
            	bean.setMetaInformation(principal.getMetaInformation());
            	
            	List groups = new ArrayList();
            	Iterator groupsListIterator = principal.getGroups().iterator();
                while(groupsListIterator.hasNext())
                {
                	InfoGlueGroup group = (InfoGlueGroup)groupsListIterator.next();
                	InfoGlueGroupBean groupBean = new InfoGlueGroupBean();
                	groupBean.setName(group.getName());
                	groupBean.setDisplayName(group.getDisplayName());
                	groupBean.setDescription(group.getDescription());
                	groups.add(groupBean);	
                }
                bean.setGroups(groups);

            	List roles = new ArrayList();
            	Iterator rolesListIterator = principal.getRoles().iterator();
                while(rolesListIterator.hasNext())
                {
                	InfoGlueRole role = (InfoGlueRole)rolesListIterator.next();
                	InfoGlueRoleBean roleBean = new InfoGlueRoleBean();
                	roleBean.setName(role.getName());
                	roleBean.setDisplayName(role.getDisplayName());
                	roleBean.setDescription(role.getDescription());
                	roles.add(roleBean);	
                }
                bean.setRoles(roles);

            	users.add(bean);	
            }
        }
        catch(Exception e)
        {
        	logger.error("En error occurred when we tried to create a new contentVersion:" + e.getMessage(), e);
        }
        
        return users;    
    }

    /**
     * Gets all roles available.
     */
    
    public List<InfoGluePrincipalBean> getPrincipalsWithRole(String roleName) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return null;
        }

        List<InfoGluePrincipalBean> users = new ArrayList<InfoGluePrincipalBean>();
        
        logger.info("***************************************");
        logger.info("Getting all principals through webservice..");
        logger.info("***************************************");
        
        try
        {
            List principalList = RoleControllerProxy.getController().getInfoGluePrincipals(roleName);

            Iterator principalListIterator = principalList.iterator();
            while(principalListIterator.hasNext())
            {
            	InfoGluePrincipal principal = (InfoGluePrincipal)principalListIterator.next();
            	InfoGluePrincipalBean bean = new InfoGluePrincipalBean();
            	bean.setName(principal.getName());
            	bean.setDisplayName(principal.getDisplayName());
            	bean.setEmail(principal.getEmail());
            	bean.setFirstName(principal.getFirstName());
            	bean.setLastName(principal.getLastName());
            	bean.setAdministrator(false);
            	bean.setMetaInformation(principal.getMetaInformation());
            	
            	List groups = new ArrayList();
            	Iterator groupsListIterator = principal.getGroups().iterator();
                while(groupsListIterator.hasNext())
                {
                	InfoGlueGroup group = (InfoGlueGroup)groupsListIterator.next();
                	InfoGlueGroupBean groupBean = new InfoGlueGroupBean();
                	groupBean.setName(group.getName());
                	groupBean.setDisplayName(group.getDisplayName());
                	groupBean.setDescription(group.getDescription());
                	groups.add(groupBean);	
                }
                bean.setGroups(groups);

            	List roles = new ArrayList();
            	Iterator rolesListIterator = principal.getRoles().iterator();
                while(rolesListIterator.hasNext())
                {
                	InfoGlueRole role = (InfoGlueRole)rolesListIterator.next();
                	InfoGlueRoleBean roleBean = new InfoGlueRoleBean();
                	roleBean.setName(role.getName());
                	roleBean.setDisplayName(role.getDisplayName());
                	roleBean.setDescription(role.getDescription());
                	roles.add(roleBean);	
                }
                bean.setRoles(roles);

            	users.add(bean);	
            }
        }
        catch(Exception e)
        {
        	logger.error("En error occurred when we tried to create a new contentVersion:" + e.getMessage(), e);
        }
        
        return users;    
    }

    /**
     * Gets all roles available.
     */
    
    public List<InfoGluePrincipalBean> getPrincipalsWithGroup(String groupName) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return null;
        }

        List<InfoGluePrincipalBean> users = new ArrayList<InfoGluePrincipalBean>();
        
        logger.info("***************************************");
        logger.info("Getting all principals through webservice..");
        logger.info("***************************************");
        
        try
        {
            List principalList = GroupControllerProxy.getController().getInfoGluePrincipals(groupName);

            Iterator principalListIterator = principalList.iterator();
            while(principalListIterator.hasNext())
            {
            	InfoGluePrincipal principal = (InfoGluePrincipal)principalListIterator.next();
            	InfoGluePrincipalBean bean = new InfoGluePrincipalBean();
            	bean.setName(principal.getName());
            	bean.setDisplayName(principal.getDisplayName());
            	bean.setEmail(principal.getEmail());
            	bean.setFirstName(principal.getFirstName());
            	bean.setLastName(principal.getLastName());
            	bean.setAdministrator(false);
            	bean.setMetaInformation(principal.getMetaInformation());
            	
            	List groups = new ArrayList();
            	Iterator groupsListIterator = principal.getGroups().iterator();
                while(groupsListIterator.hasNext())
                {
                	InfoGlueGroup group = (InfoGlueGroup)groupsListIterator.next();
                	InfoGlueGroupBean groupBean = new InfoGlueGroupBean();
                	groupBean.setName(group.getName());
                	groupBean.setDisplayName(group.getDisplayName());
                	groupBean.setDescription(group.getDescription());
                	groups.add(groupBean);	
                }
                bean.setGroups(groups);

            	List roles = new ArrayList();
            	Iterator rolesListIterator = principal.getRoles().iterator();
                while(rolesListIterator.hasNext())
                {
                	InfoGlueRole role = (InfoGlueRole)rolesListIterator.next();
                	InfoGlueRoleBean roleBean = new InfoGlueRoleBean();
                	roleBean.setName(role.getName());
                	roleBean.setDisplayName(role.getDisplayName());
                	roleBean.setDescription(role.getDescription());
                	roles.add(roleBean);	
                }
                bean.setRoles(roles);

            	users.add(bean);	
            }
        }
        catch(Exception e)
        {
        	logger.error("En error occurred when we tried to create a new contentVersion:" + e.getMessage(), e);
        }
        
        return users;    
    }

	/**
	 * Checks if the principal exists and if the principal is allowed to create the workflow.
	 * 
	 * @param userName the name of the user.
	 * @param workflowName the name of the workflow to create.
	 * @throws SystemException if the principal doesn't exists or doesn't have permission to create the workflow.
	 */
	private void initializePrincipal(final String userName) throws SystemException 
	{
		try 
		{
			principal = UserControllerProxy.getController().getUser(userName);
		}
		catch(SystemException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new SystemException(e);
		}
		if(principal == null) 
		{
			throw new SystemException("No such principal [" + userName + "].");
		}
	}

}
