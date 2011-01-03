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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.sorters.ReflectionComparator;
import org.infoglue.deliver.util.Timer;


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
	
	protected String doExecute() throws Exception 
	{
		this.filterRoleNames = this.getRequest().getParameterValues("filterRoleName");
		if(filterFirstName == null && filterLastName == null && filterUserName == null && filterEmail == null && (filterRoleNames == null || filterRoleNames.length == 0 || (filterRoleNames.length == 1 && filterRoleNames[0].equals(""))))
		{
			this.infogluePrincipals = UserControllerProxy.getController().getAllUsers();
			this.numberOfSlots = this.infogluePrincipals.size() / 100;
			int startIndex = 0 + (slotId * 100);
			int endIndex = 0 + (slotId * 100) + 100;
			if(endIndex > this.infogluePrincipals.size())
				endIndex = this.infogluePrincipals.size();
			
			this.infogluePrincipals = this.infogluePrincipals.subList(startIndex, endIndex);
		}
		else
		{
			this.infogluePrincipals = UserControllerProxy.getController().getFilteredUsers(this.filterFirstName, this.filterLastName, this.filterUserName, this.filterEmail, filterRoleNames);
		}

	    return "success";
	}

	public String doV3() throws Exception 
	{
		this.infogluePrincipals = UserControllerProxy.getController().getAllUsers();

	    return "successV3";
	}

	public String doUserListForPopup() throws Exception 
	{
		this.infogluePrincipals = UserControllerProxy.getController().getAllUsers();
		Collections.sort(this.infogluePrincipals, new ReflectionComparator("firstName"));
		
	    return "successPopup";
	}

	public String doUserListForPopupV3() throws Exception 
	{
		this.infogluePrincipals = UserControllerProxy.getController().getAllUsers();
		Collections.sort(this.infogluePrincipals, new ReflectionComparator("firstName"));
		
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
			//	System.out.println("Exists:" + infogluePrincipal.getName().charAt(0));
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
	
}
