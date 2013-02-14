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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;

import org.apache.log4j.Logger;
import org.infoglue.cms.entities.management.GroupVO;
import org.infoglue.cms.entities.management.RoleVO;
import org.infoglue.cms.entities.management.SystemUserVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.PrincipalNotFoundException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.NullObject;
import org.infoglue.deliver.util.Timer;

/**
 * @author Mattias Bogeblad
 *
 * This authentication module authenticates an user against the ordinary infoglue database.
 */

public class LenientFallbackJNDIBasicAuthorizationModule extends Thread implements AuthorizationModule, Serializable
{
    private final static Logger logger = Logger.getLogger(LenientFallbackJNDIBasicAuthorizationModule.class.getName());

    private static Thread classThread = null;
    
	protected Properties extraProperties = null;
	
	public LenientFallbackJNDIBasicAuthorizationModule()
	{
		if(classThread == null)
		{
			classThread = this;
			classThread.start();
		}			
	}
	
	public void run()
	{
		try
		{
			Thread.sleep(3000);
		} 
		catch (InterruptedException e1)
		{
			e1.printStackTrace();
		}
		
		while(true)
		{
			logger.info("Running SimplifiedFallbackJNDIBasicAuthorizationModule thread which updates the auth cache:" + CmsPropertyHandler.getContextRootPath());
			Timer t = new Timer();
			if(!logger.isInfoEnabled())
				t.setActive(false);
			
			try
			{
				DirContext ctx = getContext();
				
				try
				{
					List roles = getRoles(ctx, true, null);
					t.printElapsedTime("Caching roles took");
					Iterator rolesIterator = roles.iterator();
					while(rolesIterator.hasNext())
					{
						InfoGlueRole infoGlueRole = (InfoGlueRole)rolesIterator.next();
						getAuthorizedInfoGlueRole(infoGlueRole.getName(), ctx, true);
					}
					t.printElapsedTime("Caching individual roles took");
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
					ctx.close();
				}
	
				try
				{
					List groups = getGroups(ctx, true, null);
					t.printElapsedTime("Caching groups took");
					Iterator groupsIterator = groups.iterator();
					while(groupsIterator.hasNext())
					{
						InfoGlueGroup infoGlueGroup = (InfoGlueGroup)groupsIterator.next();
						getAuthorizedInfoGlueGroup(infoGlueGroup.getName(), ctx, true);
					}
					t.printElapsedTime("Caching individual groups took");
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				finally
				{
					ctx.close();
				}
	
				/*
				List users = getUsers(true);
				t.printElapsedTime("Caching users took");
				
				String cacheIndividualUsers = this.extraProperties.getProperty("cacheIndividualUsers", "false");
				if(cacheIndividualUsers.equals("true"))
				{
					Iterator usersIterator = users.iterator();
					while(usersIterator.hasNext())
					{
						InfoGluePrincipal infoGluePrincipal = (InfoGluePrincipal)usersIterator.next();
						getAuthorizedInfoGluePrincipal(infoGluePrincipal.getName(), true);
					}
				}
				*/
				t.printElapsedTime("Caching individual users took");
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}

			try
			{
				String userCacheTimeout = this.extraProperties.getProperty("batchReadTimeout", "1800");
				int threadSleep = 1000 * 60 * 20;
				if(userCacheTimeout != null && !userCacheTimeout.equals(""))
					threadSleep = Integer.parseInt(userCacheTimeout) * 1000;
				
				logger.info("threadSleep:" + threadSleep);
				Thread.sleep(threadSleep);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
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
	 * This method gets a Context - either by an anonymous bind or a real bind
	 */
	
	public DirContext getContext() throws Exception
	{
		//logger.info("Creating JNDI-context...");
		
		String connectionURL 		= this.extraProperties.getProperty("connectionURL");
		String ldapVersion			= this.extraProperties.getProperty("ldapVersion");
		String socketFactory		= this.extraProperties.getProperty("socketFactory");
		String authenticationMethod	= this.extraProperties.getProperty("authenticationMethod");
		String connectionName		= this.extraProperties.getProperty("connectionName");
		String connectionPassword	= this.extraProperties.getProperty("connectionPassword");

		//logger.info("connectionURL:" + connectionURL);
		
		// Create a Hashtable object.
		Hashtable env = new Hashtable();
		
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, connectionURL);
		env.put("java.naming.batchsize", "100");
		
		if(ldapVersion != null && !ldapVersion.equals("3"))
			env.put("java.naming.ldap.version", ldapVersion); 		
		else
			env.put("java.naming.ldap.version", "3"); 
		
		if(socketFactory != null && !socketFactory.equals(""))
			env.put("java.naming.ldap.factory.socket", "org.infoglue.cms.security.DummySSLSocketFactory");
		
		if(authenticationMethod != null && authenticationMethod.equals("none"))
		{
			env.put(Context.SECURITY_AUTHENTICATION, "none");
		}
		else
		{
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, connectionName);
			env.put(Context.SECURITY_CREDENTIALS, connectionPassword);
		}
				
		env.put("com.sun.jndi.ldap.connect.pool", "true");

		DirContext ctx = new InitialDirContext(env); 
		
		return ctx;
	}

	/**
	 * This method gets a Context - either by an anonymous bind or a real bind
	 */
	
	public DirContext getContext(Control[] controls) throws Exception
	{
		String connectionURL 		= this.extraProperties.getProperty("connectionURL");
		String ldapVersion			= this.extraProperties.getProperty("ldapVersion");
		String socketFactory		= this.extraProperties.getProperty("socketFactory");
		String authenticationMethod	= this.extraProperties.getProperty("authenticationMethod");
		String connectionName		= this.extraProperties.getProperty("connectionName");
		String connectionPassword	= this.extraProperties.getProperty("connectionPassword");

		// Create a Hashtable object.
		Hashtable env = new Hashtable();
		
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

		env.put(Context.PROVIDER_URL, connectionURL);
		if(ldapVersion != null && !ldapVersion.equals("3"))
			env.put("java.naming.ldap.version", ldapVersion); 		
		else
			env.put("java.naming.ldap.version", "3"); 
		
		if(socketFactory != null && !socketFactory.equals(""))
			env.put("java.naming.ldap.factory.socket", "org.infoglue.cms.security.DummySSLSocketFactory");
		
		if(authenticationMethod != null && authenticationMethod.equals("none"))
		{
			env.put(Context.SECURITY_AUTHENTICATION, "none");
		}
		else
		{
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, connectionName);
			env.put(Context.SECURITY_CREDENTIALS, connectionPassword);
		}
			
		env.put("com.sun.jndi.ldap.connect.pool", "true");
		
		DirContext ctx = new InitialLdapContext(env, controls); 
		
		return ctx;
	}

	/**
	 * Gets an authorized InfoGluePrincipal 
	 */
	
	public InfoGluePrincipal getAuthorizedInfoGluePrincipal(String userName) throws Exception
	{
		return getAuthorizedInfoGluePrincipal(userName, false);
	}
	
	/**
	 * Gets an authorized InfoGluePrincipal 
	 */
	
	public InfoGluePrincipal getAuthorizedInfoGluePrincipal(String userName, boolean skipCaches) throws Exception
	{
		String userCacheTimeout = this.extraProperties.getProperty("userCacheTimeout", "1800");

	    String authorizerIndex = this.extraProperties.getProperty("authorizerIndex");
	    if(authorizerIndex == null)
	    	authorizerIndex = "";

	    logger.info("userName:" + userName);
		String userBase	= this.extraProperties.getProperty("userBase");
		
	    String key = "user_" + userName + authorizerIndex;
	    InfoGluePrincipal infogluePrincipal = null;
	    Object infogluePrincipalObject = CacheController.getCachedObjectFromAdvancedCache("JNDIAuthorizationCache", key, new Integer(userCacheTimeout).intValue());
		if(infogluePrincipalObject != null && !skipCaches)
		{
			if(infogluePrincipalObject instanceof NullObject)
			{
				return null;
			}
			else
			{
				infogluePrincipal = (InfoGluePrincipal)infogluePrincipalObject;
				//logger.info("Returning cached user:" + userName + ":" + infogluePrincipal);
				return infogluePrincipal;
			}
		}

		String administratorUserName = CmsPropertyHandler.getAdministratorUserName();
		String administratorEmail 	 = CmsPropertyHandler.getAdministratorEmail();
		//String administratorUserName = CmsPropertyHandler.getProperty("administratorUserName");
		//String administratorEmail 	 = CmsPropertyHandler.getProperty("administratorEmail");
		
		final boolean isAdministrator = userName.equalsIgnoreCase(administratorUserName) ? true : false;
		if(isAdministrator)
		{
			infogluePrincipal = new InfoGluePrincipal(userName, "System", "Administrator", administratorEmail, new ArrayList(), new ArrayList(), isAdministrator, this);
		}
		else
		{				
			DirContext ctx = getContext();
			
			try
			{
				String[] userBases = null;
				if(userBase != null)
					userBases = userBase.split(";");
				
				userName = getFullUserName(userName, ctx);
				
				if(logger.isInfoEnabled())
					logger.info("userAttributes:" + userName);
				
				List memberOfNames = new ArrayList();
				Map userAttributes = getUserAttributes(userName, memberOfNames, ctx);
				
				List roles = getRoles(userName, memberOfNames, ctx);
				List groups = getGroups(userName, memberOfNames, ctx);
				
				infogluePrincipal = new InfoGluePrincipal(userName, (String)userAttributes.get("displayName"), (String)userAttributes.get("firstName"), (String)userAttributes.get("lastName"), (String)userAttributes.get("mail"), roles, groups, isAdministrator, this);
				infogluePrincipal.getMetaInformation().putAll(userAttributes);
				if(logger.isInfoEnabled())
					logger.info("metaInformation 2:" + infogluePrincipal.hasCode() + ":" + infogluePrincipal.getMetaInformation() + ":" + userAttributes);
				
			    if(infogluePrincipal != null)
			    	CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, infogluePrincipal, null, false);
			}
			catch(PrincipalNotFoundException pnfe)
			{
				logger.warn("Warning:" + pnfe.getMessage());
			    CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, new NullObject(), null, false);
			}
			catch(Exception e)
			{
				logger.error("Error:" + e.getMessage(), e);
			    CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, new NullObject(), null, false);
			}
			finally
			{
				ctx.close();
			}
		}
	    	
		return infogluePrincipal;
	}

	/*
	private String getDistinguishedUserName(String userName, DirContext ctx) throws Exception
	{
		String distinguishedUserName = null;
		
		String userBase					= this.extraProperties.getProperty("userBase");				

		String[] userBases = null;
		if(userBase != null)
			userBases = userBase.split(";");

		String userSearch				= this.extraProperties.getProperty("userSearch");
		String userAttributesFilter		= this.extraProperties.getProperty("userAttributesFilter", "cn, distinguishedName");
		String userNameAttributeFilter	= this.extraProperties.getProperty("userNameAttributeFilter", "distinguishedName");
			
   		for(int userBaseIndex=0; userBaseIndex < userBases.length; userBaseIndex++)
		{
			String baseDN = userBases[userBaseIndex];
			
			if(logger.isInfoEnabled())
				logger.info("Searching for distinguished name in " + baseDN);
			//String baseDN = userBase;

			try
			{
		        String anonymousUserName = CmsPropertyHandler.getAnonymousUser();
		        if(userName.equals(anonymousUserName))
		        {
		            String anonymousUserBase = this.extraProperties.getProperty("anonymousUserBase");
		        	if(anonymousUserBase != null && !anonymousUserBase.equals(""))
		        		baseDN = anonymousUserBase;
		        }
		        
		        logger.info("userName:" + userName);
			        
		        int index = 0;
				String samAccountDomainName	= this.extraProperties.getProperty("samAccountDomainName." + index);
				while(samAccountDomainName != null)
				{
			        logger.info("samAccountDomainName:" + samAccountDomainName);
			        if(samAccountDomainName != null && !samAccountDomainName.equals(""))
			        {
			        	int startIndex = userName.indexOf(samAccountDomainName);
			        	if(startIndex > -1)
			        		userName = userName.substring(0, startIndex) + userName.substring(startIndex + samAccountDomainName.length());
			        	//userName.replaceAll(samAccountDomainName, "");
			        }
			        
			        index++;
					samAccountDomainName = this.extraProperties.getProperty("samAccountDomainName." + index);
				}
				
				if(logger.isInfoEnabled())
					logger.info("userName:" + userName);
		        
				String searchFilter = "(CN=" + userName + ")";
				if(userSearch != null && userSearch.length() > 0)
				{
					searchFilter = userSearch.replaceAll("\\{1\\}", userName);
				}
				
				if(logger.isInfoEnabled())
					logger.info("searchFilter:" + searchFilter);
				
				String attributesFilter = "cn, distinguishedName";
				if(userAttributesFilter != null && userAttributesFilter.length() > 0)
					attributesFilter = userAttributesFilter;
				
				String[] attrID = attributesFilter.split(",");

				if(logger.isInfoEnabled())
				{
					logger.info("baseDN:" + baseDN);
					logger.info("searchFilter:" + searchFilter);
					logger.info("attrID" + attrID);
				}
							
				SearchControls ctls = new SearchControls(); 
				ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				ctls.setReturningAttributes(attrID);
				
				NamingEnumeration answer = ctx.search(baseDN, searchFilter, ctls); 
				if(logger.isInfoEnabled())
					logger.info("baseDN:" + baseDN + " - " + searchFilter + "\n" + answer.hasMore());

				if(!answer.hasMore())
					throw new Exception("The user with userName=" + userName + " was not found in the JNDI Data Source.");
					
				while (answer.hasMore()) 
				{
					SearchResult sr = (SearchResult)answer.next();
					if(logger.isInfoEnabled())
						logger.info("Person:" + sr.toString() + "\n");
					
					Attributes attributes = sr.getAttributes();
					
					if(logger.isInfoEnabled())
						logger.info("attributes:" + attributes + "\n");
									
					Attribute userNameAttribute = attributes.get(userNameAttributeFilter);
					
					if(logger.isInfoEnabled())
						logger.info("userNameAttribute:" + userNameAttribute.toString());
					
					if(userNameAttribute != null)
					{
						NamingEnumeration allEnum = userNameAttribute.getAll();
						while(allEnum.hasMore())
						{
							String value = (String)allEnum.next();
							
							if(logger.isInfoEnabled())
								logger.info("value:" + value);
							
							distinguishedUserName = value;
							if(distinguishedUserName != null)
								return distinguishedUserName;
						}
					}
				} 
			}
			catch (Exception e) 
			{
				logger.warn(e);
			}
		}

		if(distinguishedUserName == null)
			throw new PrincipalNotFoundException("No user called " + userName + " was found in getDistinguishedUserName.");
			
		return distinguishedUserName;
	}
	*/

	private String getFullUserName(String userName, DirContext ctx) throws Exception
	{
		String distinguishedUserName = null;
		
		String userBase	= this.extraProperties.getProperty("userBase");				

		if(logger.isInfoEnabled())
		{
			logger.info("userBase:" + userBase);
			logger.info("userName:" + userName);
			logger.info("indexOf:" + userName.indexOf(userBase));
		}
		
		if(userName.toLowerCase().indexOf(userBase.toLowerCase()) > -1)
			return userName;
		
		String[] userBases = null;
		if(userBase != null)
			userBases = userBase.split(";");
		
		if(userBases.length > 0)
			userBase = userBases[0];

		String baseDN = userBase;
			
		distinguishedUserName = "cn=" + userName + "," + baseDN;
			
		return distinguishedUserName;
	}

	private String getFullRoleName(String roleName, DirContext ctx) throws Exception
	{
		String distinguishedRoleName = null;
		
		String roleBase	= this.extraProperties.getProperty("roleBase");				

		if(logger.isInfoEnabled())
		{
			logger.info("roleBase:" + roleBase);
			logger.info("roleName:" + roleName);
			logger.info("indexOf:" + roleName.indexOf(roleBase));
		}

		if(roleName.toLowerCase().indexOf(roleBase.toLowerCase()) > -1)
			return roleName;
		
		String[] roleBases = null;
		if(roleBase != null)
			roleBases = roleBase.split(";");
		
		if(roleBases.length > 0)
			roleBase = roleBases[0];

		String baseDN = roleBase;
			
		distinguishedRoleName = "cn=" + roleName + "," + baseDN;
			
		return distinguishedRoleName;
	}

	private String getFullGroupName(String groupName, DirContext ctx) throws Exception
	{
		String distinguishedGroupName = null;
		
		String groupBase = this.extraProperties.getProperty("groupBase");				

		if(groupName.toLowerCase().indexOf(groupBase.toLowerCase()) > -1)
			return groupName;
		
		String[] groupBases = null;
		if(groupBase != null)
			groupBases = groupBase.split(";");
		
		if(groupBases.length > 0)
			groupBase = groupBases[0];

		String baseDN = groupBase;
			
		distinguishedGroupName = "cn=" + groupName + "," + baseDN;
			
		return distinguishedGroupName;
	}

	/**
	 * Gets an authorized InfoGluePrincipal 
	 */
	
	public InfoGluePrincipal getAuthorizedInfoGluePrincipal(String userName, boolean attachRolesAndGroups, DirContext ctx, boolean skipCaches) throws Exception
	{
		String userCacheTimeout = this.extraProperties.getProperty("userCacheTimeout", "1800");

	    String authorizerIndex = this.extraProperties.getProperty("authorizerIndex");
	    if(authorizerIndex == null)
	    	authorizerIndex = "";

	    String key = "user_" + userName + authorizerIndex + "_" + attachRolesAndGroups;
	    InfoGluePrincipal infogluePrincipal = null;
	    Object infogluePrincipalObject = CacheController.getCachedObjectFromAdvancedCache("JNDIAuthorizationCache", key, new Integer(userCacheTimeout).intValue());
		if(infogluePrincipalObject != null && !skipCaches)
		{
			if(infogluePrincipalObject instanceof NullObject)
			{
				return null;
			}
			else
			{
				infogluePrincipal = (InfoGluePrincipal)infogluePrincipalObject;
				//logger.info("Returning cached user:" + userName + ":" + infogluePrincipal);
				return infogluePrincipal;
			}
		}

		String administratorUserName = CmsPropertyHandler.getAdministratorUserName();
		String administratorEmail 	 = CmsPropertyHandler.getAdministratorEmail();
		//String administratorUserName = CmsPropertyHandler.getProperty("administratorUserName");
		//String administratorEmail 	 = CmsPropertyHandler.getProperty("administratorEmail");
		
		final boolean isAdministrator = userName.equalsIgnoreCase(administratorUserName) ? true : false;
		if(isAdministrator)
		{
			infogluePrincipal = new InfoGluePrincipal(userName, "System", "Administrator", administratorEmail, new ArrayList(), new ArrayList(), isAdministrator, this);
		}
		else
		{				
			try
			{
				List memberOfNames = new ArrayList();
				Map userAttributes = getUserAttributes(userName, memberOfNames, ctx);
				List roles = new ArrayList();
				List groups = new ArrayList();
				if(attachRolesAndGroups)
				{
					roles = getRoles(userName, memberOfNames, ctx);
					groups = getGroups(userName, memberOfNames, ctx);
				}
				
				infogluePrincipal = new InfoGluePrincipal(userName, (String)userAttributes.get("displayName"), (String)userAttributes.get("firstName"), (String)userAttributes.get("lastName"), (String)userAttributes.get("mail"), roles, groups, isAdministrator, this);
				infogluePrincipal.getMetaInformation().putAll(userAttributes);
				if(logger.isInfoEnabled())
					logger.info("metaInformation 1:" + infogluePrincipal.hasCode() + ":" + infogluePrincipal.getMetaInformation() + ":" + userAttributes);
				
			    if(infogluePrincipal != null)
			    	CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, infogluePrincipal, null, false);
			}
			catch(Exception e)
			{
			    CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, new NullObject(), null, false);
			}
		}
	    	
		return infogluePrincipal;
	}

	/**
	 * Gets an authorized InfoGlueRole.
	 */
	
	public InfoGlueRole getAuthorizedInfoGlueRole(String roleName) throws Exception
	{
		InfoGlueRole role = null;
		
		DirContext ctx = getContext();
		
		try
		{
			role = getAuthorizedInfoGlueRole(roleName, ctx, false);
		}
		finally
		{
			ctx.close();
		}
		
		return role;
	}
	
	/**
	 * Gets an authorized InfoGlueRole.
	 */
	
	public InfoGlueRole getAuthorizedInfoGlueRole(String roleName, DirContext ctx, boolean skipCaches) throws Exception
	{
		roleName = getFullRoleName(roleName, ctx);
		
		String roleCacheTimeout = this.extraProperties.getProperty("roleCacheTimeout", "1800");
			
	    String authorizerIndex = this.extraProperties.getProperty("authorizerIndex");
	    if(authorizerIndex == null)
	    	authorizerIndex = "";

	    String key = "role_" + roleName.hashCode() + "_" + authorizerIndex;
	    InfoGlueRole infoglueRole = null;
	    Object infoglueRoleObject = CacheController.getCachedObjectFromAdvancedCache("JNDIAuthorizationCache", key, new Integer(roleCacheTimeout).intValue());
		
		if(infoglueRoleObject != null && !skipCaches)
		{
			if(infoglueRoleObject instanceof NullObject)
			{
				return null;
			}
			else
			{
				infoglueRole = (InfoGlueRole)infoglueRoleObject;
				return infoglueRole;
			}
		}

		logger.info("\n\n\n ---------- getAuthorizedInfoGlueRole starting ---------\n\n\n");

		String roleNameAttribute 		= this.extraProperties.getProperty("roleNameAttribute", "distinguishedName");
		String roleDisplayNameAttribute = this.extraProperties.getProperty("roleDisplayNameAttribute", "cn");
		String roleDescriptionAttribute = this.extraProperties.getProperty("roleDescriptionAttribute", "description");

		try 
		{
			logger.info("roleNameAttribute:" + roleNameAttribute);
			logger.info("roleDisplayNameAttribute:" + roleDisplayNameAttribute);
			logger.info("roleDescriptionAttribute:" + roleDescriptionAttribute);
			
			Attributes attributes = ctx.getAttributes(roleName);
			if(attributes == null)
				logger.info("Could not find " + roleName);
			
			if(logger.isInfoEnabled())
				logger.info("attributes:" + attributes);

			String name = null;
			Attribute nameAttribute = attributes.get(roleNameAttribute);
			logger.info("nameAttribute:" + nameAttribute);
			if(nameAttribute == null)
			{
				name = roleName;
			}
			else
			{
				NamingEnumeration nameAttributeAllEnum = nameAttribute.getAll();
				while(nameAttributeAllEnum.hasMore())
				{
					String roleNameCandidate = (String)nameAttributeAllEnum.next();
					logger.info("roleNameCandidate:" + roleNameCandidate);
					name = roleNameCandidate;
				}
			}
			
			String displayName = name;
			Attribute displayNameAttribute = attributes.get(roleDisplayNameAttribute);
			if(displayNameAttribute != null)
			{
				logger.info("attribute:" + displayNameAttribute.toString());
				NamingEnumeration displayNameAttributeAllEnum = displayNameAttribute.getAll();
				while(displayNameAttributeAllEnum.hasMore())
				{
					String displayNameCandidate = (String)displayNameAttributeAllEnum.next();
					logger.info("displayNameCandidate:" + displayNameCandidate);
					displayName = displayNameCandidate;
				}
			}
			
			String description = "Not available from JNDI-source";
			logger.info("descriptionAttribute:" + roleDescriptionAttribute);
			Attribute descriptionAttribute = attributes.get(roleDescriptionAttribute);
			if(descriptionAttribute != null)
			{
				logger.info("descriptionAttribute:" + descriptionAttribute.toString());
				NamingEnumeration descriptionAllEnum = descriptionAttribute.getAll();
				while(descriptionAllEnum.hasMore())
				{
					String descriptionCandidate = (String)descriptionAllEnum.next();
					logger.info("descriptionCandidate:" + descriptionCandidate);
					description = descriptionCandidate;
				}
			}
			
			infoglueRole = new InfoGlueRole(roleName, displayName, description, this);
			
		    if(infoglueRole != null)
		    {	
		    	CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, infoglueRole, null, false);
		    }

		    logger.info("-----------------------\n");
		}
		catch (Exception e) 
		{
		    CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, new NullObject(), null, false);
			logger.warn("Could not find Role: " + e.getMessage());
		}

		
		return infoglueRole;
	}

	/**
	 * Gets an authorized InfoGlueGroup.
	 */
	
	public InfoGlueGroup getAuthorizedInfoGlueGroup(String groupName) throws Exception
	{
		InfoGlueGroup group = null;
		
		DirContext ctx = getContext();
		
		try
		{
			group = getAuthorizedInfoGlueGroup(groupName, ctx, false);
		}
		finally
		{
			ctx.close();
		}
		
		return group;
	}
	
	/**
	 * Gets an authorized InfoGlueGroup.
	 */
	
	public InfoGlueGroup getAuthorizedInfoGlueGroup(String groupName, DirContext ctx, boolean skipCaches) throws Exception
	{
		groupName = getFullGroupName(groupName, ctx);

		String groupCacheTimeout = this.extraProperties.getProperty("groupCacheTimeout", "1800");

	    String authorizerIndex = this.extraProperties.getProperty("authorizerIndex");
	    if(authorizerIndex == null)
	    	authorizerIndex = "";
		
	    String key = "group_" + groupName + "_" + authorizerIndex;
	    InfoGlueGroup infoglueGroup = null;
	    Object infoglueGroupObject = CacheController.getCachedObjectFromAdvancedCache("JNDIAuthorizationCache", key, new Integer(groupCacheTimeout).intValue());
		if(infoglueGroupObject != null && !skipCaches)
		{
			if(infoglueGroupObject instanceof NullObject)
			{
				return null;
			}
			else
			{
				infoglueGroup = (InfoGlueGroup)infoglueGroupObject;
				return infoglueGroup;
			}
		}

		logger.info("\n\n\n ---------- getAuthorizedInfoGlueGroup starting ---------\n\n\n");

		String groupNameAttribute 	= this.extraProperties.getProperty("groupNameAttribute", "distinguishedName");
		String groupDisplayNameAttribute = this.extraProperties.getProperty("groupDisplayNameAttribute", "cn");
		String groupDescriptionAttribute = this.extraProperties.getProperty("groupDescriptionAttribute", "description");

		try 
		{				
			logger.info("groupNameAttribute:" + groupNameAttribute);
			logger.info("groupDisplayNameAttribute:" + groupDisplayNameAttribute);
			logger.info("groupDescriptionAttribute:" + groupDescriptionAttribute);
			
			Attributes attributes = ctx.getAttributes(groupName);
			logger.info("attributes:" + attributes.toString());

			String name = null;
			Attribute nameAttribute = attributes.get(groupNameAttribute);
			logger.info("nameAttribute:" + nameAttribute);
			
			if(nameAttribute == null)
			{
				name = groupName;
			}
			else
			{
				NamingEnumeration nameAttributeAllEnum = nameAttribute.getAll();
				while(nameAttributeAllEnum.hasMore())
				{
					String groupNameCandidate = (String)nameAttributeAllEnum.next();
					logger.info("groupNameCandidate:" + groupNameCandidate);
					name = groupNameCandidate;
				}
			}
			
			String displayName = name;
			Attribute displayNameAttribute = attributes.get(groupDisplayNameAttribute);
			logger.info("attribute:" + displayNameAttribute);
			if(displayNameAttribute != null)
			{
				NamingEnumeration displayNameAttributeAllEnum = displayNameAttribute.getAll();
				while(displayNameAttributeAllEnum.hasMore())
				{
					String displayNameCandidate = (String)displayNameAttributeAllEnum.next();
					logger.info("displayNameCandidate:" + displayNameCandidate);
					displayName = displayNameCandidate;
				}
			}
			
			String description = "Not available from JNDI-source";
			logger.info("descriptionAttribute:" + groupDescriptionAttribute);
			Attribute descriptionAttribute = attributes.get(groupDescriptionAttribute);
			logger.info("descriptionAttribute:" + descriptionAttribute);
			if(displayNameAttribute != null)
			{
				NamingEnumeration descriptionAllEnum = descriptionAttribute.getAll();
				while(descriptionAllEnum.hasMore())
				{
					String descriptionCandidate = (String)descriptionAllEnum.next();
					logger.info("descriptionCandidate:" + descriptionCandidate);
					description = descriptionCandidate;
				}
			}
			
			infoglueGroup = new InfoGlueGroup(name, displayName, description, this);

		    if(infoglueGroup != null)
		    	CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, infoglueGroup, null, false);

			logger.info("-----------------------\n");
		}
		catch (Exception e) 
		{
		    CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, new NullObject(), null, false);
			logger.info("Could not find Group: " + e.getMessage());
		}		
		return infoglueGroup;
	}

	/**
	 * This method gets a users groups
	 */
	
	public List authorizeUser(String userName) throws Exception
	{
		return getRoles(userName);
	}


	/**
	 * Returns an attribute set which this user has. 
	 *
	 * @param context The directory context we are searching
	 * @param user The User to be checked
	 *
	 * @exception NamingException if a directory server error occurs
	 */
	
	protected Map getUserAttributes(String userName, List memberOfNames) throws NamingException, Exception
	{
		Map attributes = null;
		
		DirContext ctx = getContext();
		
		try
		{
			attributes = getUserAttributes(userName, memberOfNames, ctx);
		}
		finally
		{
			ctx.close();
		}
		
		return attributes;
	}
	
	/**
	 * Returns an attribute set which this user has. 
	 *
	 * @param context The directory context we are searching
	 * @param user The User to be checked
	 *
	 * @exception NamingException if a directory server error occurs
	 */
	
	protected Map getUserAttributes(String userName, List memberOfNames, DirContext ctx) throws NamingException, Exception
	{
		logger.info("userName:" + userName);
		
		Map userAttributes = new HashMap();
		
		String userAttributesFilter			= this.extraProperties.getProperty("userAttributesFilter", "cn, distinguishedName");
		String userDisplayNameFilter		= this.extraProperties.getProperty("displayNameFilter", "displayName");
		String userNameAttributeFilter		= this.extraProperties.getProperty("userNameAttributeFilter", "distinguishedName");
		String userFirstNameAttributeFilter	= this.extraProperties.getProperty("userFirstNameAttributeFilter", "givenName");
		String userLastNameAttributeFilter	= this.extraProperties.getProperty("userLastNameAttributeFilter", "sn");
		String userMailAttributeFilter		= this.extraProperties.getProperty("userMailAttributeFilter", "mail");

		String memberOfAttributeFilter		= this.extraProperties.getProperty("memberOfFilter", "memberOf");
		
		memberOfAttributeFilter = memberOfAttributeFilter.toLowerCase().trim();
		
		String[] memberOfAttributes = memberOfAttributeFilter.split(",");
		
		try 
		{
			Attributes attributes = ctx.getAttributes(userName);
			if(logger.isInfoEnabled())
				logger.info("attributes:" + attributes);
			if(attributes == null)
				throw new SystemException("No user attributes found for user:" + userName);
				
			Attribute userNameAttribute 		= attributes.get(userNameAttributeFilter);
			Attribute userDisplayNameAttribute 	= attributes.get(userDisplayNameFilter);
			Attribute userFirstNameAttribute 	= attributes.get(userFirstNameAttributeFilter);
			Attribute userLastNameAttribute 	= attributes.get(userLastNameAttributeFilter);
			Attribute userMailAttribute	 		= attributes.get(userMailAttributeFilter);
		
			if(logger.isInfoEnabled())
			{
				logger.info("userNameAttribute:" + userNameAttribute);
				logger.info("userDisplayNameAttribute:" + userDisplayNameAttribute);
				logger.info("userFirstNameAttribute:" + userFirstNameAttribute);
				logger.info("userLastNameAttribute:" + userLastNameAttribute);
				logger.info("userMailAttribute:" + userMailAttribute);
			}
			
			userAttributes.put("displayName", (userDisplayNameAttribute == null ? userName : userDisplayNameAttribute.get().toString()));
			userAttributes.put("firstName", (userFirstNameAttribute == null ? "Unknown" : userFirstNameAttribute.get().toString()));
			userAttributes.put("lastName", (userLastNameAttribute == null ? "Unknown" : userLastNameAttribute.get().toString()));
			userAttributes.put("mail", (userMailAttribute == null ? "Unknown" : userMailAttribute.get().toString()));
		
			if(logger.isInfoEnabled())
				logger.info("userAttributesFilter:" + userAttributesFilter);

			String[] attrID = userAttributesFilter.split(",");
			for(int i=0; i<attrID.length; i++)
			{
				String attributeName = attrID[i];
				if(logger.isInfoEnabled())
					logger.info("attributeName:" + attributeName);

				if(!attributeName.equals(userNameAttributeFilter) && 
				   !attributeName.equals(userNameAttributeFilter) && 
				   !attributeName.equals(userNameAttributeFilter) && 
				   !attributeName.equals(userNameAttributeFilter) && 
				   !attributeName.equals(userNameAttributeFilter))
				{
					Attribute value = attributes.get(attributeName);
					if(logger.isInfoEnabled())
						logger.info("value:" + value);

					userAttributes.put(attributeName, (value == null ? "Unknown" : value.get().toString()));
				}
			}
						
			//logger.info("memberOfAttributes:" + memberOfAttributes.length);
			for(int i=0; i<memberOfAttributes.length; i++)
			{
				String memberOfAttributeName = memberOfAttributes[i];
				Attribute value = attributes.get(memberOfAttributeName);
				if(value != null)
				{
					NamingEnumeration e = value.getAll();
					while(e.hasMore()) 
					{
						Object memberOfNameObject = e.next();
						String memberOfName = memberOfNameObject.toString().trim();
						//logger.info("memberOfName:" + memberOfName);
									
						memberOfNames.add((memberOfNameObject == null ? "Unknown" : memberOfName));
					}
				}
				else
				{
					logger.warn("User " + userName + " had " + memberOfAttributeName + " of attributes..");
				}
			}
		}
		catch (NameNotFoundException nnfe) 
		{
			logger.warn("No user called " + userName + " was found in getUserAttributes.");
			throw new PrincipalNotFoundException("No user called " + userName + " was found in getUserAttributes..");
		}
		catch (Exception e) 
		{
			logger.warn(e);
			throw e;
		}

		return userAttributes;
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
	
	protected List getRoles(String userName) throws NamingException, Exception 
	{
		List roles = null;
		
		DirContext ctx = getContext();
		
		try
		{
			roles = getRoles(userName, ctx, false);
		}
		finally
		{
			ctx.close();
		}
		
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
	
	protected List getRoles(String userName, DirContext ctx, boolean skipCaches) throws NamingException, Exception 
	{
		logger.info("**************************************************");
		logger.info("*In JNDI version								 *");
		logger.info("**************************************************");
		logger.info("userName:" + userName);
		
		List roles = new ArrayList();
		Map allRoleNamesMap = new HashMap();
		List allRoles = getRoles(ctx, skipCaches, allRoleNamesMap);

		String memberOfAttribute	= this.extraProperties.getProperty("memberOfAttributeFilter");
		String roleFilter			= this.extraProperties.getProperty("roleFilter", "InfoGlue");
		
		try 
		{
			String memberOfAttributeFilter = "memberOf";
			if(memberOfAttribute != null && memberOfAttribute.length() > 0)
			    memberOfAttributeFilter = memberOfAttribute;
			memberOfAttributeFilter = memberOfAttributeFilter.toLowerCase().trim();
			
			String[] memberOfAttributes = memberOfAttributeFilter.split(",");
			
			Attributes attributes = ctx.getAttributes(userName, memberOfAttributes);
			if(attributes == null)
				throw new SystemException("No user attributes found for user:" + userName);

			NamingEnumeration allEnum = attributes.getAll();
			while(allEnum.hasMore())
			{
				Attribute attr = (Attribute)allEnum.next();
				//logger.info("roleNameObject:" + attr);

				NamingEnumeration e = attr.getAll();
				while(e.hasMore()) 
				{
					Object roleNameObject = e.next();
					logger.info("roleNameObject:" + roleNameObject);
					//LdapAttribute attribute = (LdapAttribute)roleNameObject;
					
					String fullRoleName = roleNameObject.toString().trim();
					String roleName = fullRoleName;
					//logger.info("roleName:" + roleName);
					
					logger.info("roleName:" + fullRoleName);
					
					if(roleFilter.equalsIgnoreCase("*") || roleName.indexOf(roleFilter) > -1)
					{
						InfoGlueRole cachedRole = (InfoGlueRole)allRoleNamesMap.get(roleName);
						if(cachedRole != null)
						{
							roles.add(cachedRole);
						}
						/*
						InfoGlueRole infoGlueRole = getAuthorizedInfoGlueRole(roleName, ctx, skipCaches);
						if(allRoles.contains(infoGlueRole))
					    {
							//InfoGlueRole infoGlueRole = new InfoGlueRole(roleName, "Not available from JNDI-source");
							logger.info("Adding role.................:" + fullRoleName);
							roles.add(infoGlueRole);
					    }
					    */
					}
				}
			}
		}
		catch (Exception e) 
		{
			logger.warn("Could not find Group for empID: " + userName + e);
			throw e;
		}

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
	
	protected List getRoles(String userName, List memberOfNames, DirContext ctx) throws NamingException, Exception 
	{
		logger.info("**************************************************");
		logger.info("*In JNDI version								 *");
		logger.info("**************************************************");
		logger.info("userName:" + userName);
		
		List roles = new ArrayList();
		Map allRoleNamesMap = new HashMap();
		List allRoles = getRoles(ctx, false, allRoleNamesMap);

		try 
		{
			if(memberOfNames == null)
				throw new SystemException("No memberOfNames found for user:" + userName);

			String roleFilter = this.extraProperties.getProperty("roleFilter", "InfoGlue");

			Iterator memberOfNamesIterator = memberOfNames.iterator();
			while(memberOfNamesIterator.hasNext())
			{
				String memberOfName = ((String)memberOfNamesIterator.next()).toLowerCase();
				//logger.info("roleNameObject:" + attr);

				logger.info("memberOfName:" + memberOfName);
				
				if(roleFilter.equalsIgnoreCase("*") || memberOfName.indexOf(roleFilter) > -1)
				{
					InfoGlueRole cachedRole = (InfoGlueRole)allRoleNamesMap.get(memberOfName);

					if(cachedRole != null)
					{
						//InfoGlueRole infoGlueRole = getAuthorizedInfoGlueRole(memberOfName, ctx, false);
						//logger.info("Adding role.................:" + memberOfName);
						roles.add(cachedRole);
					}
				}
			}
		}
		catch (Exception e) 
		{
			logger.warn("Could not find role for userName: " + userName + e);
			throw e;
		}

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
	
	protected List getGroups(String userName) throws NamingException, Exception 
	{
		List groups = null;
		
		DirContext ctx = getContext();
		
		try
		{
			groups = getGroups(userName, ctx, false);
		}
		finally
		{
			ctx.close();
		}

		return groups;
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
	
	protected List getGroups(String userName, DirContext ctx, boolean skipCaches) throws NamingException, Exception 
	{
		logger.info("**************************************************");
		logger.info("*In JNDI version								  *");
		logger.info("**************************************************");
		logger.info("userName:" + userName);
		
		List groups = new ArrayList();
		Map allGroupsMap = new HashMap();
		List allGroups = getGroups(ctx, skipCaches, allGroupsMap);
	    
		String memberOfAttribute	= this.extraProperties.getProperty("memberOfAttributeFilter");
		String groupFilter			= this.extraProperties.getProperty("groupFilter", "InfoGlue");
		
		try 
		{
			String memberOfAttributeFilter = "memberOf";
			if(memberOfAttribute != null && memberOfAttribute.length() > 0)
			    memberOfAttributeFilter = memberOfAttribute;
			memberOfAttributeFilter = memberOfAttributeFilter.toLowerCase().trim();
			
			String[] memberOfAttributes = memberOfAttributeFilter.split(",");
			
			Attributes attributes = ctx.getAttributes(userName, memberOfAttributes);
			if(attributes == null)
				throw new SystemException("No user attributes found for user:" + userName);

			NamingEnumeration allEnum = attributes.getAll();
			while(allEnum.hasMore())
			{
				Attribute attr = (Attribute)allEnum.next();
				//logger.info("groupNameObject:" + attr);

				NamingEnumeration e = attr.getAll();
				while(e.hasMore()) 
				{
					Object groupNameObject = e.next();
					logger.info("groupNameObject:" + groupNameObject);
					//LdapAttribute attribute = (LdapAttribute)groupNameObject;
					
					String fullGroupName = groupNameObject.toString().trim();
					String groupName = fullGroupName.toLowerCase();
					//logger.info("groupName:" + groupName);
					
					logger.info("groupName:" + fullGroupName);
					
					if(groupFilter.equalsIgnoreCase("*") || groupName.indexOf(groupFilter) > -1)
					{
						InfoGlueGroup cachedGroup = (InfoGlueGroup)allGroupsMap.get(groupName);
						if(cachedGroup != null)
						{
							groups.add(cachedGroup);							
						}
					}
				}
			}
		}
		catch (Exception e) 
		{
			logger.warn("Could not find Group for empID: " + userName + e);
			throw e;
		}

		return groups;
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
	
	protected List getGroups(String userName, List memberOfNames, DirContext ctx) throws NamingException, Exception 
	{
		logger.info("**************************************************");
		logger.info("*In JNDI version								  *");
		logger.info("**************************************************");
		logger.info("userName:" + userName);
		
		List groups = new ArrayList();
		Map allGroupsMap = new HashMap();
		List allGroups = getGroups(ctx, false, allGroupsMap);
	    
		String groupFilter = this.extraProperties.getProperty("groupFilter", "InfoGlue");
		
		try 
		{
			if(memberOfNames == null)
				throw new SystemException("No user memberOfNames for user:" + userName);

			Iterator memberOfNamesIterator = memberOfNames.iterator();
			while(memberOfNamesIterator.hasNext())
			{
				String memberOfName = ((String)memberOfNamesIterator.next()).toLowerCase();
				//logger.info("memberOfName:" + memberOfName);
				
				logger.info("memberOfName:" + memberOfName);
					
				if(groupFilter.equalsIgnoreCase("*") || memberOfName.indexOf(groupFilter) > -1)
				{
					InfoGlueGroup cachedGroup = (InfoGlueGroup)allGroupsMap.get(memberOfName);
					if(cachedGroup != null)
					{
						groups.add(cachedGroup);							
					}
				}
			}
		}
		catch (Exception e) 
		{
			logger.warn("Could not find Group for userName: " + userName + e);
			throw e;
		}

		return groups;
	}
	
    /**
     * This method returns a list of all roles available to InfoGlue.
     */
    public List getRoles() throws Exception
    {
    	List roles = null;
    	
		DirContext ctx = getContext();
		
		try
		{
			roles = getRoles(ctx, false, null);
		}
		finally
		{
			ctx.close();
		}

		return roles;
    }
    
	/**
	 * This method gets a list of roles
	 */
	
	public List getRoles(DirContext ctx, boolean skipCaches, Map allRolesMap) throws Exception
	{
	    logger.info("getRoles start....");
	    
		String roleCacheTimeout = this.extraProperties.getProperty("roleCacheTimeout", "1800");

	    String authorizerIndex = this.extraProperties.getProperty("authorizerIndex");
	    if(authorizerIndex == null)
	    	authorizerIndex = "";

	    if(allRolesMap == null)
	    	allRolesMap = new HashMap();
	    
	    String key = "allRoles" + authorizerIndex;
		List roles = (List)CacheController.getCachedObjectFromAdvancedCache("JNDIAuthorizationCache", key, new Integer(roleCacheTimeout).intValue());
		if(roles != null && !skipCaches)
		{
			if(allRolesMap != null)
			{
				Map cachedRolesMap = (Map)CacheController.getCachedObjectFromAdvancedCache("JNDIAuthorizationCache", key + "_namesMap");
				if(cachedRolesMap != null)
					allRolesMap.putAll(cachedRolesMap);
				else
					logger.error("Problem getting all roles - was not cached before - strange");
			}
			
			return roles;
		}
		
		roles = new ArrayList();

		String roleBase 				= this.extraProperties.getProperty("roleBase");

		String[] roleBases = null;
		if(roleBase != null)
			roleBases = roleBase.split(";");

		String rolesFilter 				= this.extraProperties.getProperty("rolesFilter");
		String rolesAttributeFilter 	= this.extraProperties.getProperty("rolesAttributesFilter");
		String roleNameAttribute 		= this.extraProperties.getProperty("roleNameAttribute");
		String roleDisplayNameAttribute = this.extraProperties.getProperty("roleDisplayNameAttribute", "cn");
		String roleSearchScope 			= this.extraProperties.getProperty("roleSearchScope");

		for(int roleBaseIndex=0; roleBaseIndex < roleBases.length; roleBaseIndex++)
		{
			String baseDN = roleBases[roleBaseIndex];
			
			if(logger.isInfoEnabled())
				logger.info("Searching for roles in " + baseDN + " - roles was " + roles.size());

			//String baseDN = roleBase;

			try 
			{
				String searchFilter = "(cn=InfoGlue*)";
				if(rolesFilter != null && rolesFilter.length() > 0)
					searchFilter = rolesFilter;
	
				if(logger.isInfoEnabled())
				{
					logger.info("baseDN:" + baseDN);
					logger.info("searchFilter:" + searchFilter);
					logger.info("roleSearchScope:" + roleSearchScope);
					logger.info("rolesAttributeFilter:" + rolesAttributeFilter);
				}

				String rolesAttribute = "distinguishedName";
				if(rolesAttributeFilter != null && rolesAttributeFilter.length() > 0)
					rolesAttribute = rolesAttributeFilter;
				
				if(logger.isInfoEnabled())
					logger.info("rolesAttribute:" + rolesAttribute);
				
				String[] attrID = rolesAttribute.split(",");
				
				if(logger.isInfoEnabled())
					logger.info("attrID:" + attrID);
				
				SearchControls ctls = new SearchControls(); 
	
				int roleSearchScopeInt = SearchControls.SUBTREE_SCOPE;
				if(roleSearchScope != null && roleSearchScope.equalsIgnoreCase("ONELEVEL_SCOPE"))
				    roleSearchScopeInt = SearchControls.ONELEVEL_SCOPE;
				else if(roleSearchScope != null && roleSearchScope.equalsIgnoreCase("OBJECT_SCOPE"))
				    roleSearchScopeInt = SearchControls.OBJECT_SCOPE;
				    
			    ctls.setSearchScope(roleSearchScopeInt);
				ctls.setReturningAttributes(attrID);
		
				NamingEnumeration answer = ctx.search(baseDN, searchFilter, ctls); 
				
				if(!answer.hasMore())
					throw new Exception("There was no roles found in the JNDI Data Source.");
			
				while (answer.hasMore()) 
				{
					SearchResult sr = (SearchResult)answer.next();
					
					if(logger.isInfoEnabled())
						logger.info("sr.getName():" + sr.getName() + "-" + sr.getNameInNamespace());
					
					Attributes attributes = sr.getAttributes();
					if(logger.isInfoEnabled())
					{
						logger.info("attributes:" + attributes.toString());
						logger.info("roleNameAttribute:" + roleNameAttribute);
					}
	
					Attribute attribute = attributes.get(roleNameAttribute);
					String roleName = "";
					if(attribute == null)
					{
						roleName = sr.getNameInNamespace();
					}
					else
					{						
						//String roleName = "";
						NamingEnumeration allEnum = attribute.getAll();
						while(allEnum.hasMore())
						{
							roleName = (String)allEnum.next();
							logger.info("roleName:" + roleName);
						}
					}
					logger.info("roleName:" + roleName);
					
					String displayName = roleName;
					Attribute displayNameAttribute = attributes.get(roleDisplayNameAttribute);
					if(displayNameAttribute != null)
					{
						NamingEnumeration allEnumDisplayName = displayNameAttribute.getAll();
						while(allEnumDisplayName.hasMore())
						{
							displayName = (String)allEnumDisplayName.next();
							logger.info("displayName:" + displayName);
						}
					}
					
					InfoGlueRole infoGlueRole = new InfoGlueRole(roleName, displayName, "Not available from JNDI-source", this);
					roles.add(infoGlueRole);
					if(allRolesMap != null)
						allRolesMap.put(infoGlueRole.getName().toLowerCase(), infoGlueRole);
				} 
				logger.info("-----------------------\n");
			}
			catch (Exception e) 
			{
				logger.info("Could not find Roles: " + e.getMessage());
			}
		}
		
		logger.info("getRoles end....");

	    if(roles != null)
	    {
	    	CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, roles, null, false);
		    if(allRolesMap != null)
		    	CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key + "_namesMap", allRolesMap, null, false);	
	    }
	    	
		return roles;
	}

	/**
     * This method gets a list of users
     */
	
	public List getUsers() throws Exception
    {
    	List users = getUsers(false);

		return users;
    }
    
	/**
	 * This method gets a list of users
	 */
	
	public List getUsers(boolean skipCaches) throws Exception
	{
		Timer t = new Timer();
		if(!logger.isInfoEnabled())
			t.setActive(false);
		
		logger.info("*******************");
	    logger.info("* getUsers start  *");
	    logger.info("*******************");
	    
		String userCacheTimeout = this.extraProperties.getProperty("userCacheTimeout", "1800");

	    String authorizerIndex = this.extraProperties.getProperty("authorizerIndex");
	    if(authorizerIndex == null)
	    	authorizerIndex = "";

		String key = "allUsers" + authorizerIndex;
		List users = (List)CacheController.getCachedObjectFromAdvancedCache("JNDIAuthorizationCache", key, new Integer(userCacheTimeout).intValue());
		if(users != null && !skipCaches)
			return users;
		
		users = new ArrayList();
		
		String userBase					= this.extraProperties.getProperty("userBase");
		
		String[] userBases = null;
		if(userBase != null)
			userBases = userBase.split(";");
			
		String userListSearch			= this.extraProperties.getProperty("userListSearch");
		String userAttributesFilter		= this.extraProperties.getProperty("userAttributesFilter");
		String userNameAttributeFilter		= this.extraProperties.getProperty("userNameAttributeFilter", "distinguishedName");
		String userDisplayNameAttributeFilter	= this.extraProperties.getProperty("userDisplayNameAttributeFilter", "cn");
		String userFirstNameAttributeFilter	= this.extraProperties.getProperty("userFirstNameAttributeFilter", "givenName");
		String userLastNameAttributeFilter	= this.extraProperties.getProperty("userLastNameAttributeFilter", "sn");
		String userMailAttributeFilter	= this.extraProperties.getProperty("userMailAttributeFilter", "mail");
		String memberOfAttributeFilter	= this.extraProperties.getProperty("memberOfAttributeFilter", "memberOf");
		String roleFilter				= this.extraProperties.getProperty("roleFilter", "InfoGlue");
		String roleNameAttribute 		= this.extraProperties.getProperty("roleNameAttribute");
		String userSearchScope 			= this.extraProperties.getProperty("userSearchScope");

		String userFirstNameDummy		= this.extraProperties.getProperty("userFirstNameDummy");
		String userLastNameDummy		= this.extraProperties.getProperty("userLastNameDummy");
		String userDisplayNameDummy		= this.extraProperties.getProperty("userDisplayNameDummy");
		String userMailDummy			= this.extraProperties.getProperty("userMailDummy");

		t.printElapsedTime("Got context took:");
		
		int index = 0;

		for(int userBaseIndex=0; userBaseIndex < userBases.length; userBaseIndex++)
		{
			String baseDN = userBases[userBaseIndex];
			
			if(logger.isInfoEnabled())
				logger.info("Searching for users in " + baseDN + " - users was " + users.size());

			DirContext ctx = getContext();

			Map roleNamesMap = new HashMap();
			List allRoles = getRoles(ctx, skipCaches, roleNamesMap);
			Map groupNamesMap = new HashMap();
			List allGroups = getGroups(ctx, skipCaches, groupNamesMap);

			try 
			{
				//String baseDN = userBase;
				String searchFilter = "(CN=*)";
				if(userListSearch != null && userListSearch.length() > 0)
					searchFilter = userListSearch;
				
				String attributesFilter = "name, displayName, givenName, sn, mail, memberOf";
				if(userAttributesFilter != null && userAttributesFilter.length() > 0)
					attributesFilter = userAttributesFilter;
							
				String[] attrID = attributesFilter.split(",");
				String[] userMailAttributeFilterAttributeId = userMailAttributeFilter.split(",");
				
				if(logger.isInfoEnabled())
				{
					logger.info("attributesFilter:" + attributesFilter);
					logger.info("userMailAttributeFilterAttributeId:" + userMailAttributeFilterAttributeId);
					logger.info("baseDN:" + baseDN);
					logger.info("searchFilter:" + searchFilter);
					//logger.info("attrID" + attrID);
				}
				
				SearchControls ctls = new SearchControls(); 
	
				int userSearchScopeInt = SearchControls.SUBTREE_SCOPE;
				if(userSearchScope != null && userSearchScope.equalsIgnoreCase("ONELEVEL_SCOPE"))
				    userSearchScopeInt = SearchControls.ONELEVEL_SCOPE;
				else if(userSearchScope != null && userSearchScope.equalsIgnoreCase("OBJECT_SCOPE"))
				    userSearchScopeInt = SearchControls.OBJECT_SCOPE;
				    
			    ctls.setSearchScope(userSearchScopeInt);
				ctls.setReturningAttributes(attrID);
	
				NamingEnumeration answer = ctx.search(baseDN, searchFilter, ctls); 
	
				t.printElapsedTime("Answer took:");
				
				if(!answer.hasMore())
					throw new Exception("The was no users found in the JNDI Data Source.");
			
				while (answer.hasMore()) 
				{
					try
					{
						SearchResult sr = (SearchResult)answer.next();
						if(logger.isInfoEnabled())
							logger.info("Person:" + sr.toString() + "\n");
						
						Attributes attributes = sr.getAttributes();
						if(logger.isInfoEnabled())
							logger.info("attributes:" + attributes.toString());
						Attribute userNameAttribute = attributes.get(userNameAttributeFilter);
						Attribute userDisplayNameAttribute = attributes.get(userDisplayNameAttributeFilter);
						Attribute userFirstNameAttribute = attributes.get(userFirstNameAttributeFilter);
						Attribute userLastNameAttribute = attributes.get(userLastNameAttributeFilter);
	
						Attribute userMailAttribute = null;
						for(int i=0; i<userMailAttributeFilterAttributeId.length; i++)
						{
							userMailAttribute = attributes.get(userMailAttributeFilterAttributeId[i]);
							if(userMailAttribute != null)
								break;
						}
	
						Attribute memberOfAttribute = attributes.get(memberOfAttributeFilter);
						Attribute memberOfGroupsAttribute = attributes.get(memberOfAttributeFilter);
	
						String userFirstName = null;
						if(userFirstNameAttribute != null)
							userFirstName = userFirstNameAttribute.get().toString();
						else if(userFirstNameDummy != null && !userFirstNameDummy.equals(""))
							userFirstName = userFirstNameDummy;
						
						String userLastName = null;
						if(userLastNameAttribute != null)
							userLastName = userLastNameAttribute.get().toString();
						else if(userLastNameDummy != null && !userLastNameDummy.equals(""))
							userLastName = userLastNameDummy;

						String userDisplayName = null;
						if(userDisplayNameAttribute != null)
							userDisplayName = userDisplayNameAttribute.get().toString();
						else if(userDisplayNameDummy != null && !userDisplayNameDummy.equals(""))
							userDisplayName = userDisplayNameDummy;

						String userMail = null;
						if(userMailAttribute != null)
							userMail = userMailAttribute.get().toString();
						else if(userMailDummy != null && !userMailDummy.equals(""))
							userMail = userMailDummy;

						if(userFirstName == null || userLastName == null || userDisplayName == null || userMail == null)
						{
							if(logger.isInfoEnabled())
								logger.info("User not valid " + userNameAttribute);
							throw new SystemException("The user " + userNameAttribute + " did not have firstName, lastName or email attribute which InfoGlue requires");
						}
						
						if(logger.isInfoEnabled())
						{
							logger.info("userNameAttribute:" + userNameAttribute);
							logger.info("userDisplayName:" + userDisplayName);
							logger.info("userFirstName:" + userFirstName);
							logger.info("userLastName:" + userLastName);
							logger.info("userMail:" + userMail);
						}
						
						List roles = new ArrayList();
						List groups = new ArrayList();
	
						if(memberOfAttribute != null)
						{
							if(logger.isInfoEnabled())
								logger.info("memberOfAttribute:" + memberOfAttribute);
						
							NamingEnumeration allEnum = memberOfAttribute.getAll();
							while(allEnum.hasMore())
							{
								String roleName = ((String)allEnum.next()).toLowerCase();
	
								if(logger.isInfoEnabled())
									logger.info("roleName:" + roleName);
								
								if(roleFilter.equalsIgnoreCase("*") || roleName.indexOf(roleFilter) > -1)
								{
									if(logger.isInfoEnabled())
									{
										logger.info("roleNameAttribute:" + roleNameAttribute);
										logger.info("groupName:" + roleName);
										logger.info("indexOf:" + roleName.indexOf(roleNameAttribute));
									}
									
									InfoGlueRole cachedRole = (InfoGlueRole)roleNamesMap.get(roleName);
									if(cachedRole != null)
									{
										//InfoGlueRole infoGlueRole = this.getAuthorizedInfoGlueRole(roleName, ctx, false);
									    //InfoGlueRole infoGlueRole = new InfoGlueRole(roleName, "Not available from JNDI-source", this);
										roles.add(cachedRole);
									}
								}
							}
						}
						else
						{
							if(logger.isInfoEnabled())
								logger.info("No memberOfAttribute named :" + memberOfAttributeFilter + " was found.");
						}
	
						if(memberOfGroupsAttribute != null)
						{
							NamingEnumeration allGroupsEnum = memberOfGroupsAttribute.getAll();
							while(allGroupsEnum.hasMore())
							{
								String groupName = ((String)allGroupsEnum.next()).toLowerCase();
								
								if(logger.isInfoEnabled())
									logger.info("groupName:" + groupName);
								
								if(roleFilter.equalsIgnoreCase("*") || groupName.indexOf(roleFilter) > -1)
								{
									if(logger.isInfoEnabled())
									{
										logger.info("roleNameAttribute:" + roleNameAttribute);
										logger.info("groupName:" + groupName);
										logger.info("indexOf:" + groupName.indexOf(roleNameAttribute));
									}
									
									InfoGlueGroup cachedGroup = (InfoGlueGroup)groupNamesMap.get(groupName);
									if(cachedGroup != null)
									{
										//InfoGlueGroup infoGlueGroup = this.getAuthorizedInfoGlueGroup(groupName, ctx, false);
										//InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupName, "Not available from JNDI-source", this);
									    groups.add(cachedGroup);
									}
								}
							}
						}
						else
						{
							if(logger.isInfoEnabled())
								logger.info("No memberOfGroupsAttribute named :" + memberOfAttributeFilter + " was found.");
						}
	
						String userName = "";
						if(userNameAttribute == null)
						{
							userName = sr.getNameInNamespace();
						}
						else
						{						
							NamingEnumeration allEnum = userNameAttribute.getAll();
							while(allEnum.hasMore())
							{
								userName = (String)allEnum.next();
								logger.info("userName:" + userName);
							}
						}
						logger.info("userName:" + userName);

						InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(userName, userDisplayName, userFirstName, userLastName, userMail, roles, groups, false, this);
						users.add(infoGluePrincipal);
					}
					catch(Exception e)
					{
						logger.warn("An error occurred when we tried to read user: " + e.getMessage(), e);
					}
				}
			}
			catch (Exception e) 
			{
				logger.warn("Could not find Users: " + e.getMessage(), e);
			}
			finally
			{
				ctx.close();
			}

			if(logger.isInfoEnabled())
				logger.info("After searching for users in " + baseDN + " - users was " + users.size());
		}

		t.printElapsedTime("all users took " + index + ":");
		
		logger.info("getUsers end...");

	    if(users != null)
	    {
	    	CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, users, null, false);
	    }
	   
		return users;
	}
	
	public List getFilteredUsers(String firstName, String lastName, String userName, String email, String[] roleIds) throws SystemException, Bug
	{
		List users = new ArrayList();
		//TODO		
		return users;
	}
	
	public List getFilteredUsers(Integer offset, Integer limit,	String sortProperty, String direction, String searchString, boolean populateRolesAndGroups) throws SystemException, Bug, Exception
	{
		//TODO
		return getUsers();
	}


    /* (non-Javadoc)
     * @see org.infoglue.cms.security.AuthorizationModule#getRoleUsers(java.lang.String)
     */
    public List getUsers(String roleName) throws Exception
    {
        return getRoleUsers(roleName);
    }

	
	public List getRoleUsers(String roleName) throws Exception
	{
		List users = null;
		
		DirContext ctx = getContext();
		
		try
		{
			users = getRoleUsers(roleName, ctx, false);
		}
		finally
		{
			ctx.close();
		}

		return users;
	}
	
	public List getRoleUsers(String roleName, DirContext ctx, boolean skipCaches) throws Exception
	{
		List users = new ArrayList();
		
		String roleNameAttribute		= this.extraProperties.getProperty("roleNameAttribute", "distinguishedName");
		String roleDisplayNameAttribute = this.extraProperties.getProperty("roleDisplayNameAttribute", "cn");
		String roleDescriptionAttribute = this.extraProperties.getProperty("roleDescriptionAttribute", "description");
		String usersAttributeFilter 	= this.extraProperties.getProperty("usersAttributesFilter");
		
		try 
		{
			logger.info("roleNameAttribute:" + roleNameAttribute);
			logger.info("roleDisplayNameAttribute:" + roleDisplayNameAttribute);
			logger.info("roleDescriptionAttribute:" + roleDescriptionAttribute);

			logger.info("Getting users with role:" + roleName);

			Attributes attributes = ctx.getAttributes(roleName);
			logger.info("attributes:" + attributes.toString());

			String name = null;
			Attribute nameAttribute = attributes.get(roleNameAttribute);
			if(logger.isInfoEnabled())
				logger.info("nameAttribute:" + nameAttribute);
			if(nameAttribute == null)
			{
				name = roleName;
			}
			else
			{						
				NamingEnumeration nameAttributeAllEnum = nameAttribute.getAll();
				while(nameAttributeAllEnum.hasMore())
				{
					String roleNameCandidate = (String)nameAttributeAllEnum.next();
					logger.info("roleNameCandidate:" + roleNameCandidate);
					name = roleNameCandidate;
				}
			}

			String displayName = name;
			logger.info("roleDisplayNameAttribute:" + roleDisplayNameAttribute);
			Attribute displayNameAttribute = attributes.get(roleDisplayNameAttribute);
			logger.info("attribute:" + displayNameAttribute);
			if(displayNameAttribute != null)
			{
				logger.info("attribute:" + displayNameAttribute.toString());
				NamingEnumeration displayNameAttributeAllEnum = displayNameAttribute.getAll();
				while(displayNameAttributeAllEnum.hasMore())
				{
					String displayNameCandidate = (String)displayNameAttributeAllEnum.next();
					logger.info("displayNameCandidate:" + displayNameCandidate);
					logger.info("displayNameCandidate:" + displayNameCandidate);
					displayName = displayNameCandidate;
				}
			}
			
			String description = "Not available from JNDI-source";
			logger.info("descriptionAttribute:" + roleDescriptionAttribute);
			Attribute descriptionAttribute = attributes.get(roleDescriptionAttribute);
			logger.info("descriptionAttribute:" + descriptionAttribute);
			if(displayNameAttribute != null)
			{
				NamingEnumeration descriptionAllEnum = descriptionAttribute.getAll();
				while(descriptionAllEnum.hasMore())
				{
					String descriptionCandidate = (String)descriptionAllEnum.next();
					logger.info("descriptionCandidate:" + descriptionCandidate);
					description = descriptionCandidate;
				}
			}
			
			logger.info("usersAttributeFilter:" + usersAttributeFilter);
		    Attribute usersAttribute = attributes.get(usersAttributeFilter);
			logger.info("usersAttribute:" + usersAttribute);
			
			NamingEnumeration allUsersEnum = usersAttribute.getAll();
			while(allUsersEnum.hasMore())
			{
				String userName = (String)allUsersEnum.next();
				logger.info("userName:" + userName);
				
				InfoGluePrincipal infoGluePrincipal = this.getAuthorizedInfoGluePrincipal(userName, false, ctx, skipCaches);
				logger.info("infoGluePrincipal:" + infoGluePrincipal);

				//InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(userName, "", "", "", new ArrayList(), new ArrayList(), false, this);
			    users.add(infoGluePrincipal);				
			} 
		}
		catch (Exception e) 
		{
			logger.info("Could not find users for role: " + e.getMessage());
		}
	    logger.info("--------------------END---------------------");

		return users;
	}

	
	public Properties getExtraProperties()
	{
		return this.extraProperties;
	}

	public void setExtraProperties(Properties properties)
	{
		this.extraProperties = properties;
	}

    public void setTransactionObject(Object transactionObject)
    {
    }

    public Object getTransactionObject()
    {
        return null;
    }

    
    /**
     * This method returns a list of all groups available to InfoGlue.
     */
    public List getGroups() throws Exception
    {
    	List groups = null;
    	
		DirContext ctx = getContext();
		
		try
		{
			groups = getGroups(ctx, false, null);
		}
		finally
		{
			ctx.close();
		}

		return groups;
    }
    
    /**
     * This method returns a list of all groups available to InfoGlue.
     */
    public List getGroups(DirContext ctx, boolean skipCaches, Map groupNamesMap) throws Exception
    {
	    logger.info("getGroups start....");

		String groupCacheTimeout = this.extraProperties.getProperty("groupCacheTimeout", "1800");

	    String authorizerIndex = this.extraProperties.getProperty("authorizerIndex");
	    if(authorizerIndex == null)
	    	authorizerIndex = "";

	    if(groupNamesMap == null)
	    	groupNamesMap = new HashMap();
	    
		String key = "allGroups" + authorizerIndex;
		List groups = (List)CacheController.getCachedObjectFromAdvancedCache("JNDIAuthorizationCache", key, new Integer(groupCacheTimeout).intValue());
		if(groups != null && !skipCaches)
		{
			if(groupNamesMap != null)
			{
				Map cachedGroupNamesMap = (Map)CacheController.getCachedObjectFromAdvancedCache("JNDIAuthorizationCache", key + "_namesMap");
				if(cachedGroupNamesMap != null)
					groupNamesMap.putAll(cachedGroupNamesMap);
				else
					logger.error("Error getting cached group names map - strange...");
			}
			
			return groups;
		}
		
		groups = new ArrayList();
		
		String groupBase 					= this.extraProperties.getProperty("groupBase");
		
		String[] groupBases = null;
		if(groupBase != null)
			groupBases = groupBase.split(";");

		String groupsFilter 				= this.extraProperties.getProperty("groupsFilter");
		String groupsAttributeFilter		= this.extraProperties.getProperty("groupsAttributesFilter");
		String groupNameAttribute 			= this.extraProperties.getProperty("groupNameAttribute");
		String groupDisplayNameAttribute 	= this.extraProperties.getProperty("groupDisplayNameAttribute", "cn");
		String groupSearchScope 			= this.extraProperties.getProperty("groupSearchScope");
		
		for(int groupBaseIndex=0; groupBaseIndex < groupBases.length; groupBaseIndex++)
		{
			String baseDN = groupBases[groupBaseIndex];
			
			if(logger.isInfoEnabled())
				logger.info("Searching for groups in " + baseDN + " - groups was " + groups.size());

			//String baseDN = groupBase;

			try 
			{
				String searchFilter = "(cn=InfoGlue*)";
				if(groupsFilter != null && groupsFilter.length() > 0)
					searchFilter = groupsFilter;
				
				if(logger.isInfoEnabled())
				{
					logger.info("searchFilter:" + searchFilter);
					logger.info("groupSearchScope:" + groupSearchScope);
				}
				
				String groupsAttribute = "distinguishedName";
				if(groupsAttributeFilter != null && groupsAttributeFilter.length() > 0)
					groupsAttribute = groupsAttributeFilter;
		
				String[] attrID = groupsAttribute.split(",");
				logger.info("attrID:" + attrID);
				
				SearchControls ctls = new SearchControls(); 
	
				int groupSearchScopeInt = SearchControls.SUBTREE_SCOPE;
				if(groupSearchScope != null && groupSearchScope.equalsIgnoreCase("ONELEVEL_SCOPE"))
				    groupSearchScopeInt = SearchControls.ONELEVEL_SCOPE;
				else if(groupSearchScope != null && groupSearchScope.equalsIgnoreCase("OBJECT_SCOPE"))
				    groupSearchScopeInt = SearchControls.OBJECT_SCOPE;
				    
			    ctls.setSearchScope(groupSearchScopeInt);
				ctls.setReturningAttributes(attrID);
		
				NamingEnumeration answer = ctx.search(baseDN, searchFilter, ctls); 
	
				if(!answer.hasMore())
					throw new Exception("The was no groups found in the JNDI Data Source.");
			
				logger.info("-----------------------\n");
				while (answer.hasMore()) 
				{
					SearchResult sr = (SearchResult)answer.next();
					
					Attributes attributes = sr.getAttributes();
					
					Attribute attribute = attributes.get(groupNameAttribute);
					String groupName = "";
					if(attribute == null)
					{
						groupName = sr.getNameInNamespace();
					}
					else
					{						
						NamingEnumeration allEnum = attribute.getAll();
						while(allEnum.hasMore())
						{
							groupName = (String)allEnum.next();
							logger.info("groupName:" + groupName);
						}
					}
					logger.info("groupName:" + groupName);
						
					Attribute displayNameAttribute = attributes.get(groupDisplayNameAttribute);
					String displayName = groupName;
					if(displayNameAttribute != null)
					{
						NamingEnumeration allEnumDisplayName = displayNameAttribute.getAll();
						while(allEnumDisplayName.hasMore())
						{
							displayName = (String)allEnumDisplayName.next();
						}
					}
					
					InfoGlueGroup infoGlueGroup = new InfoGlueGroup(groupName, displayName, "Not available from JNDI-source", this);
					groups.add(infoGlueGroup);	
					if(groupNamesMap != null)
						groupNamesMap.put(infoGlueGroup.getName().toLowerCase(), infoGlueGroup);
				} 
				
				logger.info("-----------------------\n");
			}
			catch (Exception e) 
			{
				logger.info("Could not find Groups: " + e.getMessage());
			}
		}
		
	    logger.info("getGroups end....");

	    if(groups != null)
	    {
	    	CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key, groups, null, false);
		    if(groupNamesMap != null)
		    	CacheController.cacheObjectInAdvancedCache("JNDIAuthorizationCache", key + "_namesMap", groupNamesMap, null, false);	
	    }
	    
		return groups;
    }


    /** 
     * Gets a list of users which is memebers of the given group
     */
    public List getGroupUsers(String groupName) throws Exception
    {
		List users = null;
		
		DirContext ctx = getContext();
		
		try
		{
			users = getGroupUsers(groupName, ctx, false);
		}
		finally
		{
			ctx.close();
		}

		return users;
    }

    /** 
     * Gets a list of users which is memebers of the given group
     */
    public List getGroupUsers(String groupName, DirContext ctx, boolean skipCaches) throws Exception
    {
	    logger.info("--------getGroupUsers(String groupName) start---------------");
		List users = new ArrayList();

		String groupNameAttribute			= this.extraProperties.getProperty("groupNameAttribute", "distinguishedName");
		String groupDisplayNameAttribute	= this.extraProperties.getProperty("groupDisplayNameAttribute", "cn");
		String groupDescriptionAttribute 	= this.extraProperties.getProperty("groupDescriptionAttribute", "description");
		String usersAttributeFilter 		= this.extraProperties.getProperty("usersAttributesFilter");
		
		try 
		{
			Attributes attributes = ctx.getAttributes(groupName);
			logger.info("attributes:" + attributes);

			String name = null;
			Attribute nameAttribute = attributes.get(groupNameAttribute);
			if(logger.isInfoEnabled())
				logger.info("nameAttribute:" + nameAttribute);
			if(nameAttribute == null)
			{
				name = groupName;
			}
			else
			{						
				NamingEnumeration nameAttributeAllEnum = nameAttribute.getAll();
				while(nameAttributeAllEnum.hasMore())
				{
					String groupNameCandidate = (String)nameAttributeAllEnum.next();
					logger.info("groupNameCandidate:" + groupNameCandidate);
					name = groupNameCandidate;
				}
			}

			String displayName = name;
			logger.info("groupDisplayNameAttribute:" + groupDisplayNameAttribute);
			Attribute displayNameAttribute = attributes.get(groupDisplayNameAttribute);
			logger.info("attribute:" + displayNameAttribute);
			if(displayNameAttribute != null)
			{
				NamingEnumeration displayNameAttributeAllEnum = displayNameAttribute.getAll();
				while(displayNameAttributeAllEnum.hasMore())
				{
					String displayNameCandidate = (String)displayNameAttributeAllEnum.next();
					logger.info("displayNameCandidate:" + displayNameCandidate);
					logger.info("displayNameCandidate:" + displayNameCandidate);
					displayName = displayNameCandidate;
				}
			}
			
			String description = "Not available from JNDI-source";
			logger.info("descriptionAttribute:" + groupDescriptionAttribute);
			Attribute descriptionAttribute = attributes.get(groupDescriptionAttribute);
			logger.info("descriptionAttribute:" + descriptionAttribute);
			if(displayNameAttribute != null)
			{
				NamingEnumeration descriptionAllEnum = descriptionAttribute.getAll();
				while(descriptionAllEnum.hasMore())
				{
					String descriptionCandidate = (String)descriptionAllEnum.next();
					logger.info("descriptionCandidate:" + descriptionCandidate);
					description = descriptionCandidate;
				}
			}
			
			logger.info("usersAttributeFilter:" + usersAttributeFilter);
		    Attribute usersAttribute = attributes.get(usersAttributeFilter);
			logger.info("usersAttribute:" + usersAttribute);
			
			NamingEnumeration allUsersEnum = usersAttribute.getAll();
			while(allUsersEnum.hasMore())
			{
				String userName = (String)allUsersEnum.next();
				logger.info("userName:" + userName);

				InfoGluePrincipal infoGluePrincipal = this.getAuthorizedInfoGluePrincipal(userName, false, ctx, skipCaches);
				logger.info("infoGluePrincipal:" + infoGluePrincipal);
				//InfoGluePrincipal infoGluePrincipal = new InfoGluePrincipal(userName, "", "", "", new ArrayList(), new ArrayList(), false, this);
			    users.add(infoGluePrincipal);				
			} 
		}
		catch (Exception e) 
		{
			logger.info("Could not find Groups: " + e.getMessage());
		}
	    logger.info("--------------------END---------------------");

		return users;
	}

    
	public void createInfoGluePrincipal(SystemUserVO systemUserVO) throws Exception
	{
		throw new SystemException("The JNDI BASIC Authorization module does not support creation of users yet...");
	}

	public void updateInfoGluePrincipalPassword(String userName) throws Exception 
	{
		throw new SystemException("The JNDI BASIC Authorization module does not support updates of users yet...");
	}

	public void updateInfoGlueAnonymousPrincipalPassword() throws Exception 
	{
		throw new SystemException("The JNDI BASIC Authorization module does not support updates of user password yet....");
	}

	public void updateInfoGluePrincipalPassword(String userName, String oldPassword, String newPassword) throws Exception
	{
		throw new SystemException("The JNDI BASIC Authorization module does not support updates of user password yet...");
	}
	
	public void changeInfoGluePrincipalUserName(String userName, String newUserName) throws Exception
	{
		throw new SystemException("This AuthorizationModule does not support changing user name of a principal");
	}
	
	public void deleteInfoGluePrincipal(String userName) throws Exception
	{
		throw new SystemException("The JNDI BASIC Authorization module does not support deletion of users yet...");
	}
	
	public void createInfoGlueRole(RoleVO roleVO) throws Exception
	{
		throw new SystemException("The JNDI BASIC Authorization module does not support creation of users yet...");
	}

	public void updateInfoGlueRole(RoleVO roleVO, String[] userNames) throws Exception
	{
	}

	public void deleteInfoGlueRole(String roleName) throws Exception
	{
		throw new SystemException("The JNDI BASIC Authorization module does not support deletion of roles yet...");
	}

    public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String[] roleNames, String[] groupNames) throws Exception
    {
    }

	public void updateInfoGluePrincipal(SystemUserVO systemUserVO, String oldPassword, String[] roleNames, String[] groupNames) throws Exception
	{
	}

    public void createInfoGlueGroup(GroupVO groupVO) throws Exception
    {
		throw new SystemException("The JNDI BASIC Authorization module does not support creation of groups yet...");        
    }

    public void updateInfoGlueGroup(GroupVO roleVO, String[] userNames) throws Exception
    {
    }

    public void deleteInfoGlueGroup(String groupName) throws Exception
    {
		throw new SystemException("The JNDI BASIC Authorization module does not support deletion of groups yet...");        
    }

	public void addUserToGroup(String groupName, String userName) throws Exception 
	{
		throw new SystemException("The JNDI BASIC Authorization module does not support adding of users to groups yet...");
	}

	public void addUserToRole(String roleName, String userName) throws Exception 
	{
		throw new SystemException("The JNDI BASIC Authorization module does not support adding of users to roles yet...");
	}

	public void removeUserFromGroup(String groupName, String userName) throws Exception 
	{
		throw new SystemException("The JNDI BASIC Authorization module does not support removing users from groups yet...");
	}

	public void removeUserFromRole(String roleName, String userName) throws Exception 
	{
		throw new SystemException("The JNDI BASIC Authorization module does not support removing users from roles yet...");
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


	public Integer getRoleCount(String searchString) throws Exception 
	{
		return getRoles().size();
	}

	public Integer getGroupCount(String searchString) throws Exception 
	{
		return getGroups().size();
	}

	public Integer getUserCount(String searchString) throws Exception 
	{
		return getUsers().size();
	}
	
	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public Integer getRoleUserCount(String roleName, String searchString) throws Exception 
	{
		return getRoleUsers(roleName, null, null, null, null, searchString).size();
	}

	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public Integer getRoleUserInvertedCount(String roleName, String searchString) throws Exception 
	{
		List<InfoGluePrincipal> allUsers = getFilteredUsers(null, null, null, null, searchString, false);
		List<InfoGluePrincipal> assignedUsers = getRoleUsers(roleName, null, null, null, null, searchString);
		
		List<InfoGluePrincipal> newAllUsers = new ArrayList<InfoGluePrincipal>();
		newAllUsers.addAll(allUsers);
		newAllUsers.removeAll(assignedUsers);
		
		return newAllUsers.size();
	}

	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public Integer getGroupUserCount(String groupName, String searchString) throws Exception 
	{
		return getGroupUsers(groupName, null, null, null, null, searchString).size();
	}

	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public Integer getGroupUserInvertedCount(String groupName, String searchString) throws Exception 
	{
		List<InfoGluePrincipal> allUsers = getFilteredUsers(null, null, null, null, searchString, false);
		List<InfoGluePrincipal> assignedUsers = getGroupUsers(groupName, null, null, null, null, searchString);
		
		List<InfoGluePrincipal> newAllUsers = new ArrayList<InfoGluePrincipal>();
		newAllUsers.addAll(allUsers);
		newAllUsers.removeAll(assignedUsers);
		
		return newAllUsers.size();
	}

	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public List<InfoGluePrincipal> getRoleUsers(String roleName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception 
	{
		return getRoleUsers(roleName);
	}

	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public List<InfoGluePrincipal> getRoleUsersInverted(String roleName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception 
	{
		List<InfoGluePrincipal> allUsers = getFilteredUsers(null, null, null, null, searchString, false);
		List<InfoGluePrincipal> assignedUsers = getRoleUsers(roleName);
		
		List<InfoGluePrincipal> newAllUsers = new ArrayList<InfoGluePrincipal>();
		newAllUsers.addAll(allUsers);
		newAllUsers.removeAll(assignedUsers);

		return newAllUsers;
	}

	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public List<InfoGluePrincipal> getGroupUsers(String groupName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception 
	{
		return getGroupUsers(groupName);
	}

	//Very bad basic implementation - should be overwritten by implementing class so it's effective.
	public List<InfoGluePrincipal> getGroupUsersInverted(String groupName, Integer offset, Integer limit, String sortProperty, String direction, String searchString) throws Exception 
	{
		List<InfoGluePrincipal> allUsers = getFilteredUsers(null, null, null, null, searchString, false);
		List<InfoGluePrincipal> assignedUsers = getGroupUsers(groupName);
		
		List<InfoGluePrincipal> newAllUsers = new ArrayList<InfoGluePrincipal>();
		newAllUsers.addAll(allUsers);
		newAllUsers.removeAll(assignedUsers);

		return newAllUsers;
	}
	

}
