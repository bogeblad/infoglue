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
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.BaseController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupController;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleController;
import org.infoglue.cms.controllers.kernel.impl.simple.SystemUserController;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Group;
import org.infoglue.cms.entities.management.GroupVO;
import org.infoglue.cms.entities.management.Role;
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.entities.management.SystemUser;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.Timer;

/**
 * @author Mattias Bogeblad
 *
 * This authentication module authenticates an user against the ordinary infoglue database.
 */

public class InfoGlueJDBCAuthorizationModule extends BaseController implements AuthorizationModule, Serializable
{
    private final static Logger logger = Logger.getLogger(InfoGlueJDBCAuthorizationModule.class.getName());

	private Properties extraProperties = null;
	private transient Database transactionObject 	= null;

    protected String connectionName = null;
    protected String connectionPassword = null;
    protected String connectionURL = null;
    protected Driver driver = null;
    protected String driverName = null;

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
     * Open (if necessary) and return a database connection for use by
     * this class.
     *
     * @exception SQLException if a database error occurs
     */
    protected Connection getConnection() throws SQLException 
    {
		if(connectionURL == null)
			connectionURL = this.extraProperties.getProperty("jdbc.connectionURL");
		
		if(connectionName == null)
			connectionName = this.extraProperties.getProperty("jdbc.connectionName");
		
		if(connectionPassword == null)
			connectionPassword = this.extraProperties.getProperty("jdbc.connectionPassword");

		if(driverName == null)
			driverName = this.extraProperties.getProperty("jdbc.driverName");
    	
    	Connection conn = null;
    	
        // Instantiate our database driver if necessary
        if (driver == null) 
        {
            try 
            {
                Class clazz = Class.forName(driverName);
                driver = (Driver) clazz.newInstance();
            } 
            catch (Throwable e) 
            {
                throw new SQLException(e.getMessage());
            }
        }

        // Open a new connection
        Properties props = new Properties();
        if (connectionName != null)
            props.put("user", connectionName);
        
        if (connectionPassword != null)
            props.put("password", connectionPassword);
        
        conn = driver.connect(connectionURL, props);
        conn.setAutoCommit(false);
        
        return (conn);

    }

	/**
	 * Gets an authorized InfoGluePrincipal. If the user has logged in with the root-account
	 * we immediately return - otherwise we populate it.
	 */
	
	public InfoGluePrincipal getAuthorizedInfoGluePrincipal(String userName) throws Exception
	{
		logger.info("getAuthorizedInfoGluePrincipal with userName:" + userName);
		
	    if(userName == null || userName.equals(""))
	    {
	        logger.warn("userName was null or empty - fix your templates:" + userName);
	        return null;
	    }
	    
		InfoGluePrincipal infogluePrincipal = null;
		
		String administratorUserName = CmsPropertyHandler.getAdministratorUserName();
		String administratorEmail 	 = CmsPropertyHandler.getAdministratorEmail();
		
		final boolean isAdministrator = (userName != null && userName.equalsIgnoreCase(administratorUserName)) ? true : false;
		if(isAdministrator)
		{
			infogluePrincipal = new InfoGluePrincipal(userName, "System", "Administrator", administratorEmail, new ArrayList(), new ArrayList(), isAdministrator, this);
		}
		else
		{	
			List roles = new ArrayList();
			List groups = new ArrayList();
			
			ResultSet rs = null;
			Connection conn = null;
			PreparedStatement ps = null;
			
			try 
			{
				String userFirstNameColumn = this.extraProperties.getProperty("jdbc.userFirstNameColumn");
				if(userFirstNameColumn == null || userFirstNameColumn.equals(""))
					userFirstNameColumn = "USER_FIRSTNAME";

				String userLastNameColumn = this.extraProperties.getProperty("jdbc.userLastNameColumn");
				if(userLastNameColumn == null || userLastNameColumn.equals(""))
					userLastNameColumn = "USER_LASTNAME";

				String userEmailColumn = this.extraProperties.getProperty("jdbc.userEmailColumn");
				if(userEmailColumn == null || userEmailColumn.equals(""))
					userEmailColumn = "USER_EMAIL";

				String roleNameColumn = this.extraProperties.getProperty("jdbc.roleNameColumn");
				if(roleNameColumn == null || roleNameColumn.equals(""))
					roleNameColumn = "ROLE_NAME";
				
				String roleDescriptionColumn = this.extraProperties.getProperty("jdbc.roleDescriptionColumn");
				if(roleDescriptionColumn == null || roleDescriptionColumn.equals(""))
					roleDescriptionColumn = "ROLE_DESCRIPTION";
				
				String sql = this.extraProperties.getProperty("jdbc.userRolesSQL");
				if(sql == null || sql.equals(""))
					sql = "SELECT * from USER, ROLE_USER, ROLE where ROLE_USER.USER = USER.ID AND ROLE_USER.ROLE = ROLE.ID AND USER.USER_NAME = ?";
				
				conn = getConnection();
				
				ps = conn.prepareStatement(sql);
				ps.setString(1, userName);
				
				rs = ps.executeQuery();
				while(rs.next())
				{
					logger.info("infoGluePrincipal:" + infogluePrincipal);
					if(infogluePrincipal != null)
					{
						String roleName = rs.getString(roleNameColumn);
						String description = rs.getString(roleDescriptionColumn);
					
						InfoGlueRole infoGlueRole = new InfoGlueRole(roleName, description, this);
						infogluePrincipal.getRoles().add(infoGlueRole);
						logger.info("Added role:" + infoGlueRole.getName());
					}
					else
					{
						String userFirstName = rs.getString(userFirstNameColumn);
						String userLastName = rs.getString(userLastNameColumn);
						String userEmail = rs.getString(userEmailColumn);

						if(userFirstName == null)
							userFirstName = userName;

						if(userLastName == null)
							userLastName = userName;

						if(userEmail == null)
							userEmail = userName;
						
						String roleName = rs.getString(roleNameColumn);
						String description = rs.getString(roleDescriptionColumn);

						InfoGlueRole infoGlueRole = new InfoGlueRole(roleName, description, this);
						
						infogluePrincipal = new InfoGluePrincipal(userName, userFirstName, userLastName, userEmail, new ArrayList(), groups, false, this);
						infogluePrincipal.getRoles().add(infoGlueRole);
											
						logger.info("User read:" + infogluePrincipal.getName());
					}
					
				}

			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				logger.info("An error occurred trying to get jdbc user for " + userName + ":" + e);
				throw new SystemException(e.getMessage());
			}
			finally
			{
				if (rs != null) 
				{
					try 
					{
						rs.close();
					} 
					catch (SQLException e) {}
				}
				if (ps != null) 
				{
					try 
					{
						ps.close();
					} 
					catch (SQLException e) {}
				}
				if (conn != null) 
				{
					try 
					{
						conn.close();
					} 
					catch (Exception ex) {}
				}
			}

			logger.info("returning from getAuthorizedInfoGluePrincipal with userName:" + userName);
		}

		return infogluePrincipal;
	}

	/**
	 * Gets an authorized InfoGlueRole.
	 */
	
	public InfoGlueRole getAuthorizedInfoGlueRole(String roleName) throws Exception
	{
		InfoGlueRole infoglueRole = null;

		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement ps = null;
		
		try 
		{
			String roleDescriptionColumn = this.extraProperties.getProperty("jdbc.roleDescriptionColumn");
			if(roleDescriptionColumn == null || roleDescriptionColumn.equals(""))
				roleDescriptionColumn = "ROLE_DESCRIPTION";

			String sql = this.extraProperties.getProperty("jdbc.roleSQL");
			if(sql == null || sql.equals(""))
				sql = "SELECT * from ROLE where ROLE.ROLE_NAME = ?";

			conn = getConnection();
			
			ps = conn.prepareStatement(sql);
			ps.setString(1, roleName);
			
			rs = ps.executeQuery();
			while(rs.next())
			{
				String description = rs.getString(roleDescriptionColumn);
				
				infoglueRole = new InfoGlueRole(roleName, description, this);
			}
			
			logger.info("Role created:" + infoglueRole.getName());
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred trying to get jdbc user for " + roleName + ":" + e);
			throw new SystemException(e.getMessage());
		}
		finally
		{
			if (rs != null) 
			{
				try 
				{
					rs.close();
				} 
				catch (SQLException e) {}
			}
			if (ps != null) 
			{
				try 
				{
					ps.close();
				} 
				catch (SQLException e) {}
			}
			if (conn != null) 
			{
				try 
				{
					conn.close();
				} 
				catch (Exception ex) {}
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
				
		return infoglueGroup;
	}

	
	/**
	 * This method gets a users roles
	 */
	
	public List authorizeUser(String userName) throws Exception
	{
		List roles = new ArrayList();
		List groups = new ArrayList();
		
		String administratorUserName = CmsPropertyHandler.getAdministratorUserName();
		
		boolean isAdministrator = userName.equalsIgnoreCase(administratorUserName) ? true : false;
		if(isAdministrator)
			return roles;
		
		if(transactionObject == null)
		{
			List roleVOList = RoleController.getController().getRoleVOList(userName);
			Iterator roleVOListIterator = roleVOList.iterator();
			while(roleVOListIterator.hasNext())
			{
				RoleVO roleVO = (RoleVO)roleVOListIterator.next();
				InfoGlueRole infoGlueRole = new InfoGlueRole(roleVO.getRoleName(), roleVO.getDescription(), this);
				roles.add(infoGlueRole);
			}
	
			List groupVOList = GroupController.getController().getGroupVOList(userName);
			Iterator groupVOListIterator = groupVOList.iterator();
			while(groupVOListIterator.hasNext())
			{
			    GroupVO groupVO = (GroupVO)groupVOListIterator.next();
				InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupVO.getGroupName(), groupVO.getDescription(), this);
				groups.add(infoGlueGroup);
			}
		}
		else
		{
			Collection roleList = RoleController.getController().getRoleList(userName, transactionObject);
			Iterator roleListIterator = roleList.iterator();
			while(roleListIterator.hasNext())
			{
				Role role = (Role)roleListIterator.next();
				InfoGlueRole infoGlueRole = new InfoGlueRole(role.getRoleName(), role.getDescription(), this);
				roles.add(infoGlueRole);
			}
	
			Collection groupList = GroupController.getController().getGroupList(userName, transactionObject);
			Iterator groupListIterator = groupList.iterator();
			while(groupListIterator.hasNext())
			{
			    Group group = (Group)groupListIterator.next();
				InfoGlueGroup infoGlueGroup = new InfoGlueGroup(group.getGroupName(), group.getDescription(), this);
				groups.add(infoGlueGroup);
			}
		}
		
		return groups;
	}

	/**
	 * This method gets a list of roles
	 */
	
	public List getRoles() throws Exception
	{
		List roles = new ArrayList();
		
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement ps = null;
		
		try 
		{
			String roleNameColumn = this.extraProperties.getProperty("jdbc.roleNameColumn");
			if(roleNameColumn == null || roleNameColumn.equals(""))
				roleNameColumn = "ROLE_NAME";

			String roleDescriptionColumn = this.extraProperties.getProperty("jdbc.roleDescriptionColumn");
			if(roleDescriptionColumn == null || roleDescriptionColumn.equals(""))
				roleDescriptionColumn = "ROLE_DESCRIPTION";

			String sql = this.extraProperties.getProperty("jdbc.rolesSQL");
			if(sql == null || sql.equals(""))
				sql = "SELECT * from ROLE ORDER BY ROLE_NAME";

			conn = getConnection();
			
			ps = conn.prepareStatement(sql);
			
			rs = ps.executeQuery();
			while(rs.next())
			{
				String roleName = rs.getString(roleNameColumn);
				String description = rs.getString(roleDescriptionColumn);
				
				InfoGlueRole infoGlueRole = new InfoGlueRole(roleName, description, this);
				roles.add(infoGlueRole);
				
				logger.info("Role created:" + infoGlueRole.getName());
			}
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred trying to get all roles:" + e);
			throw new SystemException(e.getMessage());
		}
		finally
		{
			if (rs != null) 
			{
				try 
				{
					rs.close();
				} 
				catch (SQLException e) {}
			}
			if (ps != null) 
			{
				try 
				{
					ps.close();
				} 
				catch (SQLException e) {}
			}
			if (conn != null) 
			{
				try 
				{
					conn.close();
				} 
				catch (Exception ex) {}
			}
		}
		
		return roles;
	}

    public List getGroups() throws Exception
    {
        List groups = new ArrayList();
					
		return groups;
    }

    
	/**
	 * This method gets a list of users
	 */
	
	public List getUsers() throws Exception
	{
		List users = new ArrayList();
		
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement ps = null;
		
		try 
		{
			String userNameColumn = this.extraProperties.getProperty("jdbc.userNameColumn");
			if(userNameColumn == null || userNameColumn.equals(""))
				userNameColumn = "USER_NAME";

			String userFirstNameColumn = this.extraProperties.getProperty("jdbc.userFirstNameColumn");
			if(userFirstNameColumn == null || userFirstNameColumn.equals(""))
				userFirstNameColumn = "USER_FIRSTNAME";

			String userLastNameColumn = this.extraProperties.getProperty("jdbc.userLastNameColumn");
			if(userLastNameColumn == null || userLastNameColumn.equals(""))
				userLastNameColumn = "USER_LASTNAME";

			String userEmailColumn = this.extraProperties.getProperty("jdbc.userEmailColumn");
			if(userEmailColumn == null || userEmailColumn.equals(""))
				userEmailColumn = "USER_EMAIL";

			String roleNameColumn = this.extraProperties.getProperty("jdbc.roleNameColumn");
			if(roleNameColumn == null || roleNameColumn.equals(""))
				roleNameColumn = "ROLE_NAME";

			String roleDescriptionColumn = this.extraProperties.getProperty("jdbc.roleDescriptionColumn");
			if(roleDescriptionColumn == null || roleDescriptionColumn.equals(""))
				roleDescriptionColumn = "ROLE_DESCRIPTION";

			String sql = this.extraProperties.getProperty("jdbc.usersRolesSQL");
			if(sql == null || sql.equals(""))
				sql = "SELECT * from USER, ROLE_USER, ROLE where ROLE_USER.USER = USER.ID AND ROLE_USER.ROLE = ROLE.ID ORDER BY USER.USER_NAME";

			conn = getConnection();
			
			ps = conn.prepareStatement(sql);
			
			String oldUserName = "";
			
			List roles = new ArrayList();
			List groups = new ArrayList();
			
			String userFirstName = null;
			String userLastName = null;
			String userEmail = null;

			InfoGluePrincipal infoGluePrincipal = null;
			
			rs = ps.executeQuery();
			while(rs.next())
			{
				String userName = rs.getString(userNameColumn);

				logger.info("userName:" + userName);
				logger.info("oldUserName:" + oldUserName);
				if(userName.equals(oldUserName))
				{
					String roleName = rs.getString(roleNameColumn);
					String description = rs.getString(roleDescriptionColumn);
				
					InfoGlueRole infoGlueRole = new InfoGlueRole(roleName, description, this);
					infoGluePrincipal.getRoles().add(infoGlueRole);
				}
				else
				{
					userFirstName = rs.getString(userFirstNameColumn);
					userLastName = rs.getString(userLastNameColumn);
					userEmail = rs.getString(userEmailColumn);

					//if(oldUserName == null)
					//	oldUserName = userName;

					if(userFirstName == null)
						userFirstName = userName;

					if(userLastName == null)
						userLastName = userName;

					if(userEmail == null)
						userEmail = userName;
					
					String roleName = rs.getString(roleNameColumn);
					String description = rs.getString(roleDescriptionColumn);

					InfoGlueRole infoGlueRole = new InfoGlueRole(roleName, description, this);
					
					infoGluePrincipal = new InfoGluePrincipal(userName, userFirstName, userLastName, userEmail, new ArrayList(), groups, false, this);
					infoGluePrincipal.getRoles().add(infoGlueRole);
					users.add(infoGluePrincipal);
										
					logger.info("User read:" + infoGluePrincipal.getName());
				}
				
				oldUserName = userName;				
			}

		} 
		catch (Exception e) 
		{
			logger.info("An error occurred trying to get all roles:" + e);
			throw new SystemException(e.getMessage());
		}
		finally
		{
			if (rs != null) 
			{
				try 
				{
					rs.close();
				} 
				catch (SQLException e) {}
			}
			if (ps != null) 
			{
				try 
				{
					ps.close();
				} 
				catch (SQLException e) {}
			}
			if (conn != null) 
			{
				try 
				{
					conn.close();
				} 
				catch (Exception ex) {}
			}
		}
		
		return users;
	}

	public List getFilteredUsers(String firstName, String lastName, String userName, String email, String[] roleIds) throws Exception
	{
		return getUsers();
	}
	
	public List getUsers(String roleName) throws Exception
	{
		return getRoleUsers(roleName);
	}

    public List getRoleUsers(String roleName) throws Exception
    {
        logger.info("roleName:" + roleName);
		List users = new ArrayList();
				
		return users;
	}

    public List getGroupUsers(String groupName) throws Exception
    {
        logger.info("groupName:" + groupName);
		List users = new ArrayList();
		
		return users;
    }

	public void createInfoGluePrincipal(SystemUserVO systemUserVO) throws Exception
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support creation of users yet...");
	}

	public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String[] roleNames) throws Exception
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support updating of users yet...");
	}

	public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String oldPassword, String[] roleNames, String[] groupNames) throws Exception
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support updating of users yet...");
	}

	public void updateInfoGluePrincipalPassword(String userName) throws Exception 
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support updates of users yet...");
	}

	public void updateInfoGlueAnonymousPrincipalPassword() throws Exception 
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support updates of users yet...");
	}

	public void updateInfoGluePrincipalPassword(String userName, String oldPassword, String newPassword) throws Exception
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support updates of user password yet...");
	}
	
	public void deleteInfoGluePrincipal(String userName) throws Exception
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support deletion of users yet...");
	}
	
	public void createInfoGlueRole(RoleVO roleVO) throws Exception
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support creation of users yet...");
	}

	public void updateInfoGlueRole(RoleVO roleVO, String[] userNames) throws Exception
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support updates of users yet...");
	}

	public void deleteInfoGlueRole(String roleName) throws Exception
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support deletion of roles yet...");
	}

	public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String[] roleNames, String[] groupNames) throws Exception 
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support deletion of roles yet...");
	}

	public void createInfoGlueGroup(GroupVO groupVO) throws Exception 
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support deletion of roles yet...");
	}

	public void updateInfoGlueGroup(GroupVO roleVO, String[] userNames) throws Exception 
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support deletion of roles yet...");
	}

	public void deleteInfoGlueGroup(String groupName) throws Exception 
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support deletion of roles yet...");
	}

	public void addUserToGroup(String groupName, String userName) throws Exception 
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support adding of users to groups yet...");
	}

	public void addUserToRole(String roleName, String userName) throws Exception 
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support adding of users to roles yet...");
	}

	public void removeUserFromGroup(String groupName, String userName) throws Exception 
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support removing of users from groups yet...");
	}

	public void removeUserFromRole(String roleName, String userName) throws Exception 
	{
		throw new SystemException("The JDBC BASIC Authorization module does not support removing of users from roles yet...");
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

	public Properties getExtraProperties()
	{
		return extraProperties;
	}

	public void setExtraProperties(Properties extraProperties)
	{
		this.extraProperties = extraProperties;
	}
	
    public Object getTransactionObject()
    {
        return this.transactionObject;
    }

    public void setTransactionObject(Object transactionObject)
    {
        this.transactionObject = (Database)transactionObject; 
    }

	public BaseEntityVO getNewVO()
	{
		return null;
	}


}
