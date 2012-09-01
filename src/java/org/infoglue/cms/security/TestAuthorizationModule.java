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

package org.infoglue.cms.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.infoglue.cms.entities.management.GroupVO;
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * @author Mattias Bogeblad
 *
 * This authentication module is a simple dummy example on how to write a AuthorizationModule which uses the new base classes for simplifiation.
 */

public class TestAuthorizationModule extends BasicIndexedAuthorizationModule implements AuthorizationModule, Serializable, Runnable
{
	private static final long serialVersionUID = 1L;

	private final static Logger logger = Logger.getLogger(TestAuthorizationModule.class.getName());
   
	private boolean keepRunning = true; //Change to false
	private static boolean runNow = true;
	
	private static TestAuthorizationModule singleton = null;

	protected Properties extraProperties = null;
		
	public static void setRunNow(boolean runNow)
	{
		TestAuthorizationModule.runNow = runNow;
	}
	
	public TestAuthorizationModule()
	{
		if(singleton == null && CmsPropertyHandler.getApplicationName().equalsIgnoreCase("cms"))
		{
			singleton = this;
			Thread thread = new Thread(this);
			thread.setDaemon(true);
			logger.warn("Creating singleton....");
			thread.start();
		}
	}
	
	public synchronized void run()
	{
		logger.warn("Starting TestAuthorizationModule thread...");
			
		while(keepRunning)
		{
			String runHourMinuteString = this.extraProperties.getProperty("syncTime", "2:1");
			logger.info("runHourMinuteString:" + runHourMinuteString);
			String[] runHourMinuteArray = runHourMinuteString.split(":");
			int runHour = new Integer(runHourMinuteArray[0]);
			int runMinute = new Integer(runHourMinuteArray[1]);
			
			Calendar calendar = Calendar.getInstance();
			if(runNow || (calendar.get(Calendar.HOUR_OF_DAY) == runHour && calendar.get(Calendar.MINUTE) == runMinute))
			{
				runNow = false;
				try 
				{
					Thread.sleep(10000);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				
				logger.warn("Starting synchronization process:" + calendar.getTime());
				try
				{
					synchronize();
					
					logger.warn("Ending synchronization process:" + new Date());
				}
				catch (Exception e) 
				{
					logger.error("Error running synchronize: " + e.getMessage(), e);
				}
			}
			
			try 
			{
				Thread.sleep(30000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}

	private void synchronize() throws Exception
	{
		//Here we could put any cool sync logic we want if we want to use cached data after initial read.
		
		//Important to reindex the users afterwards 
		reIndex();
	}
	
	public Properties getExtraProperties()
	{
		return this.extraProperties;
	}

	public void setExtraProperties(Properties properties)
	{
		this.extraProperties = properties;
		if(singleton != null && singleton != this)
		{
			singleton.setExtraProperties(properties);
		}
	}
	
	/**
	 * Gets is the implementing class can update as well as read 
	 */
	
	public boolean getSupportUpdate() 
	{
		return false;
	}
	
	/**
	 * Gets is the implementing class can delete as well as read 
	 */
	
	public boolean getSupportDelete()
	{
		return false;
	}
	
	/**
	 * Gets is the implementing class can create as well as read 
	 */
	
	public boolean getSupportCreate()
	{
		return false;
	}
	
	/**
	 * Gets an authorized InfoGluePrincipal 
	 */
	
	public InfoGluePrincipal getAuthorizedInfoGluePrincipal(String userName) throws Exception
	{
		logger.info("Trying to get users......:" + userName);
		
		List<InfoGlueRole> roles = new ArrayList<InfoGlueRole>();
		List<InfoGlueGroup> groups = new ArrayList<InfoGlueGroup>();

		if(userName.equals("dummyUser0") || userName.equals("dummyUser2"))
		{
			roles.add(new InfoGlueRole("dummyRole0", "dummyRole0", "dummyDesc0", "dummysource0", true, new Date(), this));
			roles.add(new InfoGlueRole("dummyRole1", "dummyRole1", "dummyDesc1", "dummysource1", true, new Date(), this));
			roles.add(new InfoGlueRole("dummyRole5", "dummyRole5", "dummyDesc5", "dummysource5", true, new Date(), this));
			roles.add(new InfoGlueRole("dummyRole7", "dummyRole7", "dummyDesc7", "dummysource7", true, new Date(), this));

			groups.add(new InfoGlueGroup("dummyGroup0", "dummyGroup0", "dummyDesc0", "dummyType", "dummysource0", true, new Date(), this));
			groups.add(new InfoGlueGroup("dummyGroup1", "dummyGroup1", "dummyDesc1", "dummyType", "dummysource1", true, new Date(), this));
		}
		
		InfoGluePrincipal infogluePrincipal = new InfoGluePrincipal(userName, userName, "Anders", "anders@test.com", "infoglueTest", roles, groups, new HashMap(), true, this);
		
		return infogluePrincipal;
	}

	
	/**
	 * Gets an authorized InfoGlueRole.
	 */
	
	public InfoGlueRole getAuthorizedInfoGlueRole(String roleName) throws Exception
	{
		InfoGlueRole infoglueRole = null;

		List<InfoGlueRole> roles = getRoles();
		for(InfoGlueRole infoglueRoleCandidate : roles)
		{
			if(infoglueRoleCandidate.getName().equals(roleName))
			{
				infoglueRole = infoglueRoleCandidate;
				break;
			}
		}
		
		return infoglueRole;
	}

	
	/**
	 * Gets an authorized InfoGlueGroup.
	 */
	
	public InfoGlueGroup getAuthorizedInfoGlueGroup(String groupName) throws Exception
	{
		InfoGlueGroup infoglueGroup = null;

		List<InfoGlueGroup> groups = getGroups();
		for(InfoGlueGroup infoglueGroupCandidate : groups)
		{
			if(infoglueGroupCandidate.getName().equals(groupName))
			{
				infoglueGroup = infoglueGroupCandidate;
				break;
			}
		}
		
		return infoglueGroup;
	}

	/**
	 * This method gets a users roles
	 */
	
	public List authorizeUser(String userName) throws Exception
	{
		return getRoles(userName);
	}
	


	
	/**
	 * Return a List of roles associated with the given User. Any
	 * roles present in the user's directory entry are supplemented by
	 * a directory search. If no roles are associated with this user,
	 * a zero-length List is returned.
	 *
	 * @param context The directory context we are searching
	 * @param user The User to be checked
	 *
	 * @exception NamingException if a directory server error occurs
	 */
	
	protected List<InfoGlueRole> getRoles(String userName) throws Exception 
	{
		List<InfoGlueRole> roles = new ArrayList<InfoGlueRole>();
		InfoGluePrincipal principal = getAuthorizedInfoGluePrincipal(userName);
		if(principal != null && principal.getRoles() != null)
			roles = principal.getRoles();
		
		return roles;
	}
	
	
	
	/**
	 * Return a List of roles associated with the given User. Any
	 * roles present in the user's directory entry are supplemented by
	 * a directory search. If no roles are associated with this user,
	 * a zero-length List is returned.
	 *
	 * @param context The directory context we are searching
	 * @param user The User to be checked
	 *
	 * @exception NamingException if a directory server error occurs
	 */
	
	protected List<InfoGlueGroup> getGroups(String userName) throws Exception 
	{
		List<InfoGlueGroup> groups = new ArrayList<InfoGlueGroup>();
		
		InfoGluePrincipal principal = getAuthorizedInfoGluePrincipal(userName);
		if(principal != null && principal.getRoles() != null)
			groups = principal.getGroups();

		return groups;
	}
	
	
	/**
	 * This method gets a list of groups
	 */
	
	public List<InfoGlueGroup> getGroups() throws Exception
	{
		List<InfoGlueGroup> groups = new ArrayList<InfoGlueGroup>();
		
		for(int i=0; i<1000; i++)
		{
			InfoGlueGroup group = new InfoGlueGroup("dummyGroup" + i, "dummyGroup" + i, "dummyDesc" + i, "dummysource" + i, "dummyType", true, new Date(), this);
			groups.add(group);
		}

		return groups;
	}
	
	/**
	 * This method gets a list of roles
	 */
	
	public List<InfoGlueRole> getRoles() throws Exception
	{
		List<InfoGlueRole> roles = new ArrayList<InfoGlueRole>();
		
		for(int i=0; i<1000; i++)
		{
			InfoGlueRole role = new InfoGlueRole("dummyRole" + i, "dummyRole" + i, "dummyDesc" + i, "dummysource" + i, true, new Date(), this);
			roles.add(role);
		}

		return roles;
	}

	/**
	 * This method gets a list of users
	 */
	
	public List<InfoGluePrincipal> getUsers() throws Exception
	{
		List<InfoGluePrincipal> users = new ArrayList<InfoGluePrincipal>();
		
		for(int i=0; i<10000; i++)
		{
			List<InfoGlueRole> roles = new ArrayList<InfoGlueRole>();
			List<InfoGlueGroup> groups = new ArrayList<InfoGlueGroup>();
			
			//Test user
			if(i == 0 || i == 2 || (i > 200 && i < 229))
			{
				roles.add(new InfoGlueRole("dummyRole0", "dummyRole0", "dummyDesc0", "dummysource0", true, new Date(), this));
				roles.add(new InfoGlueRole("dummyRole1", "dummyRole1", "dummyDesc1", "dummysource1", true, new Date(), this));
				roles.add(new InfoGlueRole("dummyRole5", "dummyRole5", "dummyDesc5", "dummysource5", true, new Date(), this));
				roles.add(new InfoGlueRole("dummyRole7", "dummyRole7", "dummyDesc7", "dummysource7", true, new Date(), this));

				groups.add(new InfoGlueGroup("dummyGroup0", "dummyGroup0", "dummyDesc0", "dummyType", "dummysource0", true, new Date(), this));
				groups.add(new InfoGlueGroup("dummyGroup1", "dummyGroup1", "dummyDesc1", "dummyType", "dummysource1", true, new Date(), this));
			}

			InfoGluePrincipal user = new InfoGluePrincipal("dummyUser" + i, "dummyUser" + i, "Mr Dummy" + i, "Dummysson" + i, "mattias.bogeblad@gmail.com", "dummysource" + i, true, new Date(), roles, groups, new HashMap(), false, this);
			users.add(user);
		}
		return users;
	}
	
	/**
	 * This method gets a list of users part of a given role
	 */
	
    public List<InfoGluePrincipal> getUsers(String roleName) throws Exception
    {
        return getRoleUsers(roleName);
    }

    public void setTransactionObject(Object transactionObject)
    {
    }

    public Object getTransactionObject()
    {
        return null;
    }
    
	public void createInfoGluePrincipal(SystemUserVO systemUserVO) throws Exception
	{
		throw new SystemException("The Test Authorization module does not support creation of users yet...");
	}

	public void updateInfoGluePrincipalPassword(String userName) throws Exception 
	{
		throw new SystemException("The Test Authorization module does not support updates of users yet...");
	}

	public void updateInfoGlueAnonymousPrincipalPassword() throws Exception 
	{
		throw new SystemException("The Test Authorization module does not support updates of user password yet....");
	}

	public void updateInfoGluePrincipalPassword(String userName, String oldPassword, String newPassword) throws Exception
	{
		throw new SystemException("The Test Authorization module does not support updates of user password yet...");
	}
	
	public void deleteInfoGluePrincipal(String userName) throws Exception
	{
		throw new SystemException("The Test Authorization module does not support deletion of users yet...");
	}
	
	public void createInfoGlueRole(RoleVO roleVO) throws Exception
	{
		throw new SystemException("The Test Authorization module does not support creation of users yet...");
	}

	public void updateInfoGlueRole(RoleVO roleVO, String[] userNames) throws Exception
	{
		throw new SystemException("The Test Authorization module does not support updates of user password yet...");
	}

	public void deleteInfoGlueRole(String roleName) throws Exception
	{
		throw new SystemException("The Test Authorization module does not support deletion of roles yet...");
	}

    public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String[] roleNames, String[] groupNames) throws Exception
    {
		throw new SystemException("The Test Authorization module does not support updates of user password yet...");
    }

	public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String oldPassword, String[] roleNames, String[] groupNames) throws Exception
	{
		throw new SystemException("The Test Authorization module does not support updates of user password yet...");
	}

    public void createInfoGlueGroup(GroupVO groupVO) throws Exception
    {
		throw new SystemException("The Test Authorization module does not support creation of groups yet...");        
    }

    public void updateInfoGlueGroup(GroupVO roleVO, String[] userNames) throws Exception
    {
		throw new SystemException("The Test Authorization module does not support updates of user password yet...");
    }

    public void deleteInfoGlueGroup(String groupName) throws Exception
    {
		throw new SystemException("The Test Authorization module does not support deletion of groups yet...");        
    }

	public void addUserToGroup(String groupName, String userName) throws Exception 
	{
		throw new SystemException("The Test Authorization module does not support adding of users to groups yet...");
	}

	public void addUserToRole(String roleName, String userName) throws Exception 
	{
		throw new SystemException("The Test Authorization module does not support adding of users to roles yet...");
	}

	public void removeUserFromGroup(String groupName, String userName) throws Exception 
	{
		throw new SystemException("The Test Authorization module does not support removing users from groups yet...");
	}

	public void removeUserFromRole(String roleName, String userName) throws Exception 
	{
		throw new SystemException("The Test Authorization module does not support removing users from roles yet...");
	}

	/**
	 * This method is used find out if a user exists. Much quicker than getAuthorizedPrincipal 
	 */
	
    public boolean userExists(String userName) throws Exception
    {
    	return (getAuthorizedInfoGluePrincipal(userName) == null ? false : true);
    }

	/**
	 * This method is used find out if a role exists. Much quicker than getRole 
	 */
    public boolean roleExists(String roleName) throws Exception
    {
    	return (getAuthorizedInfoGlueRole(roleName) == null ? false : true);
    }
    
	/**
	 * This method is used find out if a group exists. Much quicker than getGroup 
	 */
    public boolean groupExists(String groupName) throws Exception
    {
    	return (getAuthorizedInfoGlueGroup(groupName) == null ? false : true);
    }


	@Override
	public List<InfoGlueRole> getAllRoles() throws Exception 
	{
		return getRoles();
	}

	@Override
	public List<InfoGlueGroup> getAllGroups() throws Exception 
	{
		return getGroups();
	}

	@Override
	public List<InfoGluePrincipal> getAllUsers(boolean populateRolesAndGroups) throws Exception 
	{
		List<InfoGluePrincipal> users = new ArrayList<InfoGluePrincipal>();
		
		for(int i=0; i<10000; i++)
		{
			List<InfoGlueRole> roles = new ArrayList<InfoGlueRole>();
			List<InfoGlueGroup> groups = new ArrayList<InfoGlueGroup>();
			
			if(populateRolesAndGroups)
			{
				//Test user
				if(i == 0 || i == 2 || (i > 200 && i < 229))
				{
					roles.add(new InfoGlueRole("dummyRole0", "dummyRole0", "dummyDesc0", "dummysource0", true, new Date(), this));
					roles.add(new InfoGlueRole("dummyRole1", "dummyRole1", "dummyDesc1", "dummysource1", true, new Date(), this));
					roles.add(new InfoGlueRole("dummyRole5", "dummyRole5", "dummyDesc5", "dummysource5", true, new Date(), this));
					roles.add(new InfoGlueRole("dummyRole7", "dummyRole7", "dummyDesc7", "dummysource7", true, new Date(), this));
	
					groups.add(new InfoGlueGroup("dummyGroup0", "dummyGroup0", "dummyDesc0", "dummyType", "dummysource0", true, new Date(), this));
					groups.add(new InfoGlueGroup("dummyGroup1", "dummyGroup1", "dummyDesc1", "dummyType", "dummysource1", true, new Date(), this));
				}
			}
			
			InfoGluePrincipal user = new InfoGluePrincipal("dummyUser" + i, "dummyUser" + i, "Mr Dummy" + i, "Dummysson" + i, "dummy" + i + "@gmail.com", "dummysource" + i, true, new Date(), roles, groups, new HashMap(), false, this);
			users.add(user);
		}
		return users;
	}

}
