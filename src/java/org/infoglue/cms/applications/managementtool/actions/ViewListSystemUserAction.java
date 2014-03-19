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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.LuceneUsersController;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.util.Timer;
import org.jfree.util.Log;


/**
 * @author Magnus Güvenal
 * @author Mattias Bogeblad
 *
 *	Action class for usecase ViewListSystemUserUCC 
 * 
 */

public class ViewListSystemUserAction extends InfoGlueAbstractAction 
{
	private static final long serialVersionUID = 1L;

	private List infogluePrincipals;
	
	private String mode 				= null;
	private String filterUserName 		= null;
	private String filterFirstName 		= null; 
	private String filterLastName 		= null; 
	private String filterEmail 			= null; 
	private String[] filterRoleNames	= null;
	private int slotId					= 0;
	private int numberOfSlots			= 0;
	
	private String filterChar = null;
	
	//DataTable parameters for dynamic filtering
	private String sEcho = null;
	private int iTotalRecords = 0;
	private int iTotalDisplayRecords = 0;
	private String sSearch = null;
	private String format = "normal";
	
	private String roleName;
	private String groupName;
	private List assignedInfoGluePrincipals;

	
	protected String doExecute() throws Exception 
	{
		return doV3();
	}

	public String doV3() throws Exception 
	{
	    return "successV3";
	}

	/**
	 * 
	 */

	public String doPopupProcessAndFilter() throws Exception 
	{
		doProcessAndFilter();
		
		return "successPopupFiltered"; 
	}
	
	public String doProcessAndFilter() throws Exception 
	{
		String sortColNumber = getRequest().getParameter("iSortCol_0");
		String sortDirection = getRequest().getParameter("sSortDir_0");
		if(sortDirection == null || sortDirection.equals(""))
			sortDirection = "asc";
		
		String sortProperty = "userName";
		if(sortColNumber != null && sortColNumber.equals("2"))
			sortProperty = "firstName";
		else if(sortColNumber != null && sortColNumber.equals("3"))
			sortProperty = "lastName";
		else if(sortColNumber != null && sortColNumber.equals("4"))
			sortProperty = "source";
		else if(sortColNumber != null && sortColNumber.equals("5"))
			sortProperty = "isActive";
		
		String iDisplayStartString = getRequest().getParameter("iDisplayStart");
		String iDisplayLengthString = getRequest().getParameter("iDisplayLength");
		if(iDisplayStartString == null)
			iDisplayStartString = "0";
		if(iDisplayLengthString == null)
			iDisplayLengthString = "1000000";

		int start = new Integer(iDisplayStartString);
		int end = start + new Integer(iDisplayLengthString);

		if(sSearch == null || sSearch.equals(""))
		{																
			String filterAssignedRoleUsers = getRequest().getParameter("filterAssignedRoleUsers");
			String filterAssignedGroupUsers = getRequest().getParameter("filterAssignedGroupUsers");
			if(filterAssignedRoleUsers != null && filterAssignedRoleUsers.equalsIgnoreCase("true"))
			{
				this.infogluePrincipals = RoleControllerProxy.getController().getInfoGluePrincipalsNotInRole(roleName, new Integer(iDisplayStartString), new Integer(iDisplayLengthString), sortProperty, sortDirection, this.sSearch);
				Integer unassignedInfogluePrincipalsCount = RoleControllerProxy.getController().getInfoGluePrincipalsNotInRoleCount(roleName, this.sSearch);
			
				this.iTotalRecords = unassignedInfogluePrincipalsCount;
				this.iTotalDisplayRecords = unassignedInfogluePrincipalsCount;
			}
			else if(filterAssignedGroupUsers != null && filterAssignedGroupUsers.equalsIgnoreCase("true"))
			{
				this.infogluePrincipals = GroupControllerProxy.getController().getInfoGluePrincipalsNotInGroup(groupName, new Integer(iDisplayStartString), new Integer(iDisplayLengthString), sortProperty, sortDirection, this.sSearch);
				Integer unassignedInfogluePrincipalsCount = GroupControllerProxy.getController().getInfoGluePrincipalsNotInGroupCount(groupName, this.sSearch);

				this.iTotalRecords = unassignedInfogluePrincipalsCount;
				this.iTotalDisplayRecords = unassignedInfogluePrincipalsCount;
			}
			else
			{
				//this.infogluePrincipals = UserControllerProxy.getController().getFilteredUsers(start, new Integer(iDisplayLengthString), sortProperty, sortDirection, null, false);
				this.infogluePrincipals = LuceneUsersController.getController().getFilteredUsers(start, new Integer(iDisplayLengthString), sortProperty, sortDirection, null, false);
				Integer totalRecords = UserControllerProxy.getController().getUserCount(this.sSearch);
				this.iTotalRecords = totalRecords;
				this.iTotalDisplayRecords = totalRecords;
			}
		}
		else
		{
			String filterAssignedRoleUsers = getRequest().getParameter("filterAssignedRoleUsers");
			String filterAssignedGroupUsers = getRequest().getParameter("filterAssignedGroupUsers");
			if(filterAssignedRoleUsers != null && filterAssignedRoleUsers.equalsIgnoreCase("true"))
			{
				this.infogluePrincipals = RoleControllerProxy.getController().getInfoGluePrincipalsNotInRole(roleName, new Integer(iDisplayStartString), new Integer(iDisplayLengthString), sortProperty, sortDirection, this.sSearch);
				Integer unassignedInfogluePrincipalsCount = RoleControllerProxy.getController().getInfoGluePrincipalsNotInRoleCount(roleName, this.sSearch);

				this.iTotalRecords = unassignedInfogluePrincipalsCount;
				this.iTotalDisplayRecords = unassignedInfogluePrincipalsCount;
			}
			else if(filterAssignedGroupUsers != null && filterAssignedGroupUsers.equalsIgnoreCase("true"))
			{
				this.infogluePrincipals = GroupControllerProxy.getController().getInfoGluePrincipalsNotInGroup(groupName, new Integer(iDisplayStartString), new Integer(iDisplayLengthString), sortProperty, sortDirection, this.sSearch);
				Integer unassignedInfogluePrincipalsCount = GroupControllerProxy.getController().getInfoGluePrincipalsNotInGroupCount(groupName, this.sSearch);

				this.iTotalRecords = unassignedInfogluePrincipalsCount;
				this.iTotalDisplayRecords = unassignedInfogluePrincipalsCount;
			}
			else
			{
				this.infogluePrincipals = LuceneUsersController.getController().getFilteredUsers(new Integer(iDisplayStartString), new Integer(iDisplayLengthString), sortProperty, sortDirection, this.sSearch, false);
				//this.infogluePrincipals = UserControllerProxy.getController().getFilteredUsers(new Integer(iDisplayStartString), new Integer(iDisplayLengthString), sortProperty, sortDirection, this.sSearch, false);
	
				this.iTotalRecords = UserControllerProxy.getController().getUserCount(this.sSearch);
				this.iTotalDisplayRecords = UserControllerProxy.getController().getUserCount(this.sSearch);
	
				try
				{
					InfoGlueRole infoGlueRole = RoleControllerProxy.getController().getRole(this.sSearch);
					List rolePrincipals	= infoGlueRole.getAutorizationModule().getRoleUsers(this.sSearch);
	
					this.iTotalRecords = rolePrincipals.size();
					this.iTotalDisplayRecords = rolePrincipals.size();
	
					if(rolePrincipals.size() > end)
						rolePrincipals = rolePrincipals.subList(start, end);
					else
						rolePrincipals = rolePrincipals.subList(start, rolePrincipals.size());
					
					List newInfogluePrincipals = new ArrayList();
					newInfogluePrincipals.addAll(this.infogluePrincipals);
					newInfogluePrincipals.removeAll(rolePrincipals);
					newInfogluePrincipals.addAll(rolePrincipals);
		
					this.infogluePrincipals = newInfogluePrincipals;
				}
				catch (Exception e) 
				{
					Log.warn("Could not find a role by that name:" + e.getMessage());
				}
				
				try
				{
					InfoGlueGroup infoGlueGroup = GroupControllerProxy.getController().getGroup(this.sSearch);
					List groupPrincipals	= infoGlueGroup.getAutorizationModule().getGroupUsers(this.sSearch);
	
					this.iTotalRecords = groupPrincipals.size();
					this.iTotalDisplayRecords = groupPrincipals.size();
	
					if(groupPrincipals.size() > end)
						groupPrincipals = groupPrincipals.subList(start, end);
					else
						groupPrincipals = groupPrincipals.subList(start, groupPrincipals.size());
	
					List newInfogluePrincipals = new ArrayList();
					newInfogluePrincipals.addAll(this.infogluePrincipals);
					newInfogluePrincipals.removeAll(groupPrincipals);
					newInfogluePrincipals.addAll(groupPrincipals);
		
					this.infogluePrincipals = newInfogluePrincipals;
				}
				catch (Exception e) 
				{
					Log.warn("Could not find a group by that name:" + e.getMessage());
				}
			}			
		}
		
		return "successFiltered";
	}

	
	public String doPopupProcessAndFilterAssignedForRole() throws Exception 
	{
		doProcessAndFilterAssignedForRole();
		
		return "successPopupFiltered"; 
	}
	
	public String doProcessAndFilterAssignedForRole() throws Exception 
	{
		String sortColNumber = getRequest().getParameter("iSortCol_0");
		String sortDirection = getRequest().getParameter("sSortDir_0");
		if(sortDirection == null || sortDirection.equals(""))
			sortDirection = "asc";
		
		String sortProperty = "userName";
		if(sortColNumber != null && sortColNumber.equals("2"))
			sortProperty = "firstName";
		else if(sortColNumber != null && sortColNumber.equals("3"))
			sortProperty = "lastName";
		else if(sortColNumber != null && sortColNumber.equals("4"))
			sortProperty = "source";
		else if(sortColNumber != null && sortColNumber.equals("5"))
			sortProperty = "isActive";

		String iDisplayStartString = getRequest().getParameter("iDisplayStart");
		String iDisplayLengthString = getRequest().getParameter("iDisplayLength");
		if(iDisplayStartString == null)
			iDisplayStartString = "0";
		if(iDisplayLengthString == null)
			iDisplayLengthString = "1000000";

		int start = new Integer(iDisplayStartString);
		int end = start + new Integer(iDisplayLengthString);
		
		if(sSearch == null || sSearch.equals(""))
		{
			String filterAssignedRoleUsers = getRequest().getParameter("filterAssignedRoleUsers");
			if(filterAssignedRoleUsers != null && filterAssignedRoleUsers.equalsIgnoreCase("true"))
			{
				InfoGlueRole infoGlueRole = RoleControllerProxy.getController().getRole(roleName);
				
				List allInfogluePrincipals = UserControllerProxy.getController().getAllUsers();
				List assignedInfogluePrincipals	= infoGlueRole.getAutorizationModule().getRoleUsers(roleName);
	
				List unassignedInfogluePrincipals = new ArrayList();
				unassignedInfogluePrincipals.addAll(allInfogluePrincipals);
				unassignedInfogluePrincipals.removeAll(assignedInfogluePrincipals);
	
				this.infogluePrincipals	= unassignedInfogluePrincipals;
			}
			else
			{
				this.infogluePrincipals	= RoleControllerProxy.getController().getInfoGluePrincipals(roleName, new Integer(iDisplayStartString), new Integer(iDisplayLengthString), sortProperty, sortDirection, this.sSearch);
				Integer principalsCount	= RoleControllerProxy.getController().getInfoGluePrincipalsCount(roleName, this.sSearch);

				this.iTotalRecords = principalsCount;
				this.iTotalDisplayRecords = principalsCount;
			}
		}
		else
		{
			List assignedInfogluePrincipals	= RoleControllerProxy.getController().getInfoGluePrincipals(roleName, new Integer(iDisplayStartString), new Integer(iDisplayLengthString), sortProperty, sortDirection, this.sSearch);
			Integer assignedInfogluePrincipalsCount	= RoleControllerProxy.getController().getInfoGluePrincipalsCount(roleName, this.sSearch);

			this.iTotalRecords = assignedInfogluePrincipalsCount;
			this.iTotalDisplayRecords = assignedInfogluePrincipalsCount;
			
			this.infogluePrincipals = assignedInfogluePrincipals;
		}
				
	    return "successFilteredAssignedForRole";
	}

	public String doPopupProcessAndFilterAssignedForGroup() throws Exception 
	{
		doProcessAndFilterAssignedForGroup();
		
		return "successPopupFiltered"; 
	}
	
	public String doProcessAndFilterAssignedForGroup() throws Exception 
	{
		String sortColNumber = getRequest().getParameter("iSortCol_0");
		String sortDirection = getRequest().getParameter("sSortDir_0");
		if(sortDirection == null || sortDirection.equals(""))
			sortDirection = "asc";
		
		String sortProperty = "userName";
		if(sortColNumber != null && sortColNumber.equals("2"))
			sortProperty = "firstName";
		else if(sortColNumber != null && sortColNumber.equals("3"))
			sortProperty = "lastName";
		else if(sortColNumber != null && sortColNumber.equals("4"))
			sortProperty = "source";
		else if(sortColNumber != null && sortColNumber.equals("5"))
			sortProperty = "isActive";

		String iDisplayStartString = getRequest().getParameter("iDisplayStart");
		String iDisplayLengthString = getRequest().getParameter("iDisplayLength");
		if(iDisplayStartString == null)
			iDisplayStartString = "0";
		if(iDisplayLengthString == null)
			iDisplayLengthString = "1000000";

		int start = new Integer(iDisplayStartString);
		int end = start + new Integer(iDisplayLengthString);
		
		//InfoGlueGroup infoGlueGroup = GroupControllerProxy.getController().getGroup(groupName);
		
		if(sSearch == null || sSearch.equals(""))
		{
			String filterAssignedGroupUsers = getRequest().getParameter("filterAssignedGroupUsers");
			if(filterAssignedGroupUsers != null && filterAssignedGroupUsers.equalsIgnoreCase("true"))
			{
				List unassignedInfogluePrincipals = GroupControllerProxy.getController().getInfoGluePrincipalsNotInGroup(groupName, new Integer(iDisplayStartString), new Integer(iDisplayLengthString), sortProperty, sortDirection, this.sSearch);
				Integer unassignedInfogluePrincipalsCount = GroupControllerProxy.getController().getInfoGluePrincipalsNotInGroupCount(groupName, this.sSearch);

				this.iTotalRecords = unassignedInfogluePrincipalsCount;
				this.iTotalDisplayRecords = unassignedInfogluePrincipalsCount;
				
				this.infogluePrincipals = unassignedInfogluePrincipals;
			}
			else
			{
				this.infogluePrincipals	= GroupControllerProxy.getController().getInfoGluePrincipals(groupName, new Integer(iDisplayStartString), new Integer(iDisplayLengthString), sortProperty, sortDirection, this.sSearch);
				Integer principalsCount	= GroupControllerProxy.getController().getInfoGluePrincipalsCount(groupName, this.sSearch);

				this.iTotalRecords = principalsCount;
				this.iTotalDisplayRecords = principalsCount;
			}
		}
		else
		{
			List assignedInfogluePrincipals	= GroupControllerProxy.getController().getInfoGluePrincipals(groupName, new Integer(iDisplayStartString), new Integer(iDisplayLengthString), sortProperty, sortDirection, this.sSearch);
			Integer assignedInfogluePrincipalsCount	= GroupControllerProxy.getController().getInfoGluePrincipalsCount(groupName, this.sSearch);

			this.iTotalRecords = assignedInfogluePrincipalsCount;
			this.iTotalDisplayRecords = assignedInfogluePrincipalsCount;
			
			this.infogluePrincipals = assignedInfogluePrincipals;
		}
		
	    return "successFilteredAssignedForGroup";
	}
	
	public String doUserListForPopup() throws Exception 
	{
		this.infogluePrincipals = UserControllerProxy.getController().getAllUsers();
		Collections.sort(this.infogluePrincipals, new ReflectionComparator("firstName"));
		
	    return "successPopup";
	}

	public String doUserListForPopupV3() throws Exception 
	{
		/*
		this.infogluePrincipals = UserControllerProxy.getController().getAllUsers();
		Collections.sort(this.infogluePrincipals, new ReflectionComparator("firstName"));
		*/
	    return "successPopupV3";
	}

	public List getUsersFirstNameChars()
	{
		List usersFirstNameChars = new ArrayList();
		Iterator principalIterator = this.infogluePrincipals.iterator();
		while(principalIterator.hasNext())
		{
			InfoGluePrincipal infogluePrincipal = (InfoGluePrincipal)principalIterator.next();
			if(!usersFirstNameChars.contains(infogluePrincipal.getName().charAt(0)))
				usersFirstNameChars.add(infogluePrincipal.getName().charAt(0));
			//else
			//	logger.info("Exists:" + infogluePrincipal.getName().charAt(0));
		}
		
		Collections.sort(usersFirstNameChars);
		
		return usersFirstNameChars;
	}
	
	public List getFilteredInfogluePrincipals()
	{
		List subList = new ArrayList();
		
		char filterChar = ((InfoGluePrincipal)this.infogluePrincipals.get(0)).getFirstName().charAt(0);
		if(this.filterChar != null && this.filterChar.length() == 1)
			filterChar = this.filterChar.charAt(0);
			
		Iterator infogluePrincipalsIterator = this.infogluePrincipals.iterator();
		boolean foundSection = false;
		while(infogluePrincipalsIterator.hasNext())
		{
			InfoGluePrincipal infogluePrincipal = (InfoGluePrincipal)infogluePrincipalsIterator.next();
			if(infogluePrincipal.getName().charAt(0) == filterChar)
			{
				subList.add(infogluePrincipal);
				foundSection = true;
			}
			else if(foundSection)
				break;
		}
		
		return subList;
	}
	
	/*
	public String doUserListSearch() throws Exception
	{
		String searchString 					= this.getRequest().getParameter("searchString");		
		List<InfoGluePrincipal> searchResult 	= UserControllerProxy.getController().getFilteredUsers(searchString, null, null, null, null);
		ServletOutputStream myOut 				= getResponse().getOutputStream();
		
		myOut.print("<select name=\"searchResult\" size=\"10\" class=\"userSelectBox\" multiple=\"true\">");
		
		for (InfoGluePrincipal igp : searchResult)
		{
			myOut.print("<option value=\"" + igp.getName() + "\">" + igp.getFirstName() + " " + igp.getLastName() + "</option>");
		}
		
		myOut.print("</select>");
		
		return "none";
	}
	*/
	
	public List getRoles() throws Exception
	{
		List roles = RoleControllerProxy.getController().getAllRoles();
		
		return roles; 
	}	
	
	public List getInfogluePrincipals()
	{
		return this.infogluePrincipals;		
	}
	
	public String getFilterEmail()
	{
		return filterEmail;
	}

	public void setFilterEmail(String email)
	{
		if(email != null && !email.equals(""))
			this.filterEmail = email;
	}

	public String getFilterFirstName()
	{
		return filterFirstName;
	}

	public void setFilterFirstName(String firstName)
	{
		if(firstName != null && !firstName.equals(""))
			this.filterFirstName = firstName;
	}

	public String getFilterLastName()
	{
		return filterLastName;
	}

	public void setFilterLastName(String lastName)
	{
		if(lastName != null && !lastName.equals(""))
			this.filterLastName = lastName;
	}

	public String getFilterUserName()
	{
		return filterUserName;
	}

	public void setFilterUserName(String userName)
	{
		if(userName != null && !userName.equals(""))
			this.filterUserName = userName;
	}

	public String getFilterChar()
	{
		return filterChar;
	}
	
	public void setFilterChar(String filterChar)
	{
		if(filterChar != null && !filterChar.equals(""))
			this.filterChar = filterChar;
	}

	public String getMode()
	{
		return mode;
	}

	public void setMode(String mode)
	{
		this.mode = mode;
	}

	public String[] getFilterRoleNames()
	{
		return filterRoleNames;
	}

	public int getSlotId() 
	{
		return slotId;
	}

	public void setSlotId(int slotId) 
	{
		this.slotId = slotId;
	}

	public int getNumberOfSlots() 
	{
		return numberOfSlots;
	}

	public void setNumberOfSlots(int numberOfSlots) 
	{
		this.numberOfSlots = numberOfSlots;
	}
	
	public int getTotalRecords() 
	{
		return iTotalRecords;
	}

	public int getTotalDisplayRecords() 
	{
		return iTotalDisplayRecords;
	}

	public String getsEcho() 
	{
		return sEcho;
	}

	public void setsEcho(String sEcho) 
	{
		this.sEcho = sEcho;
	}
	
	public String getsSearch() 
	{
		return sSearch;
	}

	public void setsSearch(String sSearch) 
	{
		this.sSearch = sSearch;
	}

	public void setFormat(String format) 
	{
		this.format = format;
	}

	public String getFormat() 
	{
		return this.format;
	}

	public String getRoleName() 
	{
		return this.roleName;
	}
	
	public void setRoleName(String roleName) throws Exception
	{	
		if(roleName != null)
		{
			byte[] bytes = Base64.decodeBase64(roleName);
			String decodedRoleName = new String(bytes, "utf-8");
			if(RoleControllerProxy.getController().roleExists(decodedRoleName))
			{
				roleName = decodedRoleName;
			}
			else
			{
				String fromEncoding = CmsPropertyHandler.getURIEncoding();
				String toEncoding = "utf-8";
				
				String testRoleName = new String(roleName.getBytes(fromEncoding), toEncoding);
				if(testRoleName.indexOf((char)65533) == -1)
					roleName = testRoleName;
			}
		}
		
		this.roleName = roleName;
	}
	
	
	public String getGroupName() 
	{
		return this.groupName;
	}
	
	public void setGroupName(String groupName) throws Exception
	{	
		if(groupName != null)
		{
			byte[] bytes = Base64.decodeBase64(groupName);
			String decodedGroupName = new String(bytes, "utf-8");
			if(GroupControllerProxy.getController().groupExists(decodedGroupName))
			{
				groupName = decodedGroupName;
			}
			else
			{
				String fromEncoding = CmsPropertyHandler.getURIEncoding();
				String toEncoding = "utf-8";
				
				String testGroupName = new String(groupName.getBytes(fromEncoding), toEncoding);
				if(testGroupName.indexOf((char)65533) == -1)
					groupName = testGroupName;
			}
		}
		
		this.groupName = groupName;
	}
	
	public String getRolesAndGroups(InfoGluePrincipal principal)
	{
		if((principal.getRoles() == null || principal.getRoles().size() == 1) && (principal.getGroups() == null || principal.getGroups().size() == 0))
		{
			try 
			{
				principal = UserControllerProxy.getController((Database)principal.getAutorizationModule().getTransactionObject()).getUser(principal.getName());
			} 
			catch (Exception e) 
			{
				//logger.error();
			}
		}
		
		StringBuilder sb = new StringBuilder("");
		
		sb.append("<b>Roles:</b> ");
		int i = 0;
		for(InfoGlueRole role : (List<InfoGlueRole>)principal.getRoles())
		{
			if(!role.getName().equals("anonymous") || sb.indexOf("anonymous") == -1)
			{
				sb.append((i > 0 ? ", " : "") + role.getDisplayName());
				i++;
			}
		}
		
		sb.append("<br/><b>Groups:</b> ");
		i = 0;
		for(InfoGlueGroup group : (List<InfoGlueGroup>)principal.getGroups())
		{
			sb.append((i > 0 ? ", " : "") + group.getDisplayName());
			i++;
		}
		
		return sb.toString();
	}
}
