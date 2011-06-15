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

package org.infoglue.cms.controllers.kernel.impl.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.management.AccessRightGroup;
import org.infoglue.cms.entities.management.AccessRightRole;
import org.infoglue.cms.entities.management.AccessRightUser;
import org.infoglue.cms.entities.management.InterceptionPoint;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.Interceptor;
import org.infoglue.cms.entities.management.impl.simple.InterceptionPointImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.NullObject;

/**
 * This class is a helper class for the use case handle InterceptionPoint
 *
 * @author Mattias Bogeblad
 */

public class InterceptionPointController extends BaseController
{
    private final static Logger logger = Logger.getLogger(InterceptionPointController.class.getName());

	public final static Map systemInterceptionPoints = new HashMap();
    
	static
	{
		systemInterceptionPoints.put("ContentTool.Read", new InterceptionPointVO("ContentTool", "ContentTool.Read", "Gives a user access to the content tool", false));
	    systemInterceptionPoints.put("ContentTool.ImportExport", new InterceptionPointVO("ContentTool", "ContentTool.ImportExport", "Intercepts exporting and importing of contents", false));
	    systemInterceptionPoints.put("ContentTool.ShowMetaInfoFolders", new InterceptionPointVO("ContentTool", "ContentTool.ShowMetaInfoFolders", "Allows the user to see the meta info folders in the content tree", false));
	    systemInterceptionPoints.put("ManagementTool.Read", new InterceptionPointVO("ManagementTool", "ManagementTool.Read", "Gives a user access to the management tool", false));
	    systemInterceptionPoints.put("MyDesktopTool.Read", new InterceptionPointVO("MyDesktopTool", "MyDesktopTool.Read", "Gives the user access to the mydesktop tool", false));
	    systemInterceptionPoints.put("PublishingTool.Read", new InterceptionPointVO("PublishingTool", "PublishingTool.Read", "Gives the user access to the publishing tool", false));
	    systemInterceptionPoints.put("StructureTool.Read", new InterceptionPointVO("StructureTool", "StructureTool.Read", "Gives a user access to the structure tool", false));
	    systemInterceptionPoints.put("FormsTool.Read", new InterceptionPointVO("FormsTool", "FormsTool.Read", "Gives a user access to the form tool", false));
	    systemInterceptionPoints.put("CalendarTool.Read", new InterceptionPointVO("CalendarTool", "CalendarTool.Read", "Gives a user access to the calendar system", false));
	    systemInterceptionPoints.put("SearchTool.Read", new InterceptionPointVO("SearchTool", "SearchTool.Read", "Gives a user access to the search tool", false));
	    
	    systemInterceptionPoints.put("Category.Read", new InterceptionPointVO("Category", "Category.Read", "This intercepts any read towards a category", true));
	    
	    systemInterceptionPoints.put("Content.Read", new InterceptionPointVO("Content", "Content.Read", "Intercepts the read of a content", true));
	    systemInterceptionPoints.put("Content.Write", new InterceptionPointVO("Content", "Content.Write", "Intercepts the write of a content", true));
	    systemInterceptionPoints.put("Content.Create", new InterceptionPointVO("Content", "Content.Create", "Intercepts the creation of a new content or folder", true));
	    systemInterceptionPoints.put("Content.Delete", new InterceptionPointVO("Content", "Content.Delete", "Intercepts the deletion of a content", true));
	    systemInterceptionPoints.put("Content.Move", new InterceptionPointVO("Content", "Content.Move", "Intercepts the movement of a content", true));
	    systemInterceptionPoints.put("Content.SubmitToPublish", new InterceptionPointVO("Content", "Content.SubmitToPublish", "Intercepts the submittance to publish of all content versions", true));
	    systemInterceptionPoints.put("Content.ChangeAccessRights", new InterceptionPointVO("Content", "Content.ChangeAccessRights", "Intercepts the attempt to change access rights", true));
	    systemInterceptionPoints.put("Content.CreateVersion", new InterceptionPointVO("Content", "Content.CreateVersion", "Intercepts the creation of a new contentversion", true));
	    systemInterceptionPoints.put("Content.ExpireDateComingUp", new InterceptionPointVO("Content", "Content.ExpireDateComingUp", "Intercepts the event of a content coming close to it's expire date", true));
	    
	    systemInterceptionPoints.put("Component.Select", new InterceptionPointVO("Component", "Component.Select", "Intercepts the read of a content when user want to list components to add in a slot", true));

	    systemInterceptionPoints.put("ContentTypeDefinition.Read", new InterceptionPointVO("ContentTypeDefinition", "ContentTypeDefinition.Read", "This point checks access to read/use a content type definition", true));
	    systemInterceptionPoints.put("ContentVersion.Delete", new InterceptionPointVO("ContentVersion", "ContentVersion.Delete", "Intercepts the deletion of a contentversion", true));
	    systemInterceptionPoints.put("ContentVersion.Write", new InterceptionPointVO("ContentVersion", "ContentVersion.Write", "Intercepts the editing of a contentversion", true));
	    systemInterceptionPoints.put("ContentVersion.Read", new InterceptionPointVO("ContentVersion", "ContentVersion.Read", "Intercepts the read of a contentversion", true));
	    systemInterceptionPoints.put("ContentVersion.Publish", new InterceptionPointVO("ContentVersion", "ContentVersion.Publish", "Intercepts the direct publishing of a content version", true));
	    
	    systemInterceptionPoints.put("Repository.Read", new InterceptionPointVO("Repository", "Repository.Read", "Gives a user access to look at a repository", true));
	    systemInterceptionPoints.put("Repository.ReadForBinding", new InterceptionPointVO("Repository", "Repository.ReadForBinding", "This point intercepts when a user tries to read the repository in a binding dialog", true));
	    
	    systemInterceptionPoints.put("SiteNodeVersion.Read", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.Read", "Intercepts the read of a SiteNodeVersion", true));
	    systemInterceptionPoints.put("SiteNodeVersion.Write", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.Write", "Intercepts the write of a SiteNodeVersion", true));
	    systemInterceptionPoints.put("SiteNodeVersion.CreateSiteNode", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.CreateSiteNode", "Intercepts the creation of a new sitenode", true));
	    systemInterceptionPoints.put("SiteNodeVersion.DeleteSiteNode", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.DeleteSiteNode", "Intercepts the deletion of a sitenode", true));
	    systemInterceptionPoints.put("SiteNodeVersion.MoveSiteNode", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.MoveSiteNode", "Intercepts the movement of a sitenode", true));
	    systemInterceptionPoints.put("SiteNodeVersion.SubmitToPublish", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.SubmitToPublish", "Intercepts the submittance to publish of all content versions", true));
	    systemInterceptionPoints.put("SiteNodeVersion.ChangeAccessRights", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.ChangeAccessRights", "Intercepts the attempt to change access rights", true));
	    systemInterceptionPoints.put("SiteNodeVersion.Publish", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.Publish", "Intercepts the direct publishing of a siteNode version", true));
	    systemInterceptionPoints.put("SiteNode.ExpireDateComingUp", new InterceptionPointVO("SiteNode", "SiteNode.ExpireDateComingUp", "Intercepts the event of a site node coming close to it's expire date", true));
	    
	    systemInterceptionPoints.put("StructureTool.PageTemplateIsOptional", new InterceptionPointVO("StructureTool", "StructureTool.PageTemplateIsOptional", "This interception point limits who has to supply a page template when creating a page", false));
	    systemInterceptionPoints.put("StructureTool.SaveTemplate", new InterceptionPointVO("StructureTool", "StructureTool.SaveTemplate", "This interception point limits who get the save-button in the toolbar", false));
	    systemInterceptionPoints.put("StructureTool.Palette", new InterceptionPointVO("StructureTool", "StructureTool.Palette", "This interception point limits who sees the toolbar", false));
	    
	    systemInterceptionPoints.put("ComponentEditor.ChangeSlotAccess", new InterceptionPointVO("ComponentEditor", "ComponentEditor.ChangeSlotAccess", "This interception point limits who can set access rights to a slot", false));
	    systemInterceptionPoints.put("ComponentEditor.AddComponent", new InterceptionPointVO("ComponentEditor", "ComponentEditor.AddComponent", "This interception point limits who can add a component to a specific slot", true));
	    systemInterceptionPoints.put("ComponentEditor.DeleteComponent", new InterceptionPointVO("ComponentEditor", "ComponentEditor.DeleteComponent", "This interception point limits who can delete a component in a specific slot", true));
	    systemInterceptionPoints.put("ComponentEditor.hasMoveComponentUpAccess", new InterceptionPointVO("ComponentEditor", "ComponentEditor.hasMoveComponentUpAccess", "This interception point limits who can move a component up in a specific slot", true));
	    systemInterceptionPoints.put("ComponentEditor.hasMoveComponentDownAccess", new InterceptionPointVO("ComponentEditor", "ComponentEditor.hasMoveComponentDownAccess", "This interception point limits who can move a component down in a specific slot", true));
	    systemInterceptionPoints.put("ComponentEditor.ChangeComponent", new InterceptionPointVO("ComponentEditor", "ComponentEditor.ChangeComponent", "This interception point limits who can change a component to another in a specific slot", true));
	    systemInterceptionPoints.put("ComponentEditor.SubmitToPublish", new InterceptionPointVO("ComponentEditor", "ComponentEditor.SubmitToPublish", "This interception point limits who have access to the Submit to publish choice in edit on sight", false));
		systemInterceptionPoints.put("ComponentEditor.PageStructure", new InterceptionPointVO("ComponentEditor", "ComponentEditor.PageStructure", "This interception point limits who can see the page structure menu option in edit on sight", false));
		systemInterceptionPoints.put("ComponentEditor.OpenInNewWindow", new InterceptionPointVO("ComponentEditor", "ComponentEditor.OpenInNewWindow", "This interception point limits who can see the Open in new window in edit on sight", false));
		systemInterceptionPoints.put("ComponentEditor.ViewSource", new InterceptionPointVO("ComponentEditor", "ComponentEditor.ViewSource", "This interception point limits who can see the view source menu in edit on sight", false));
		systemInterceptionPoints.put("ComponentEditor.CreateSubpage", new InterceptionPointVO("ComponentEditor", "ComponentEditor.CreateSubpage", "This interception point limits who can see the create subpage menu in edit on sight", false));
		systemInterceptionPoints.put("ComponentEditor.EditPageMetadata", new InterceptionPointVO("ComponentEditor", "ComponentEditor.EditPageMetadata", "This interception point limits who can see the edit page meta info menu in edit on sight", false));
		
		systemInterceptionPoints.put("ComponentEditor.NotifyUserOfPage", new InterceptionPointVO("ComponentEditor", "ComponentEditor.NotifyUserOfPage", "This interception point limits who can see the notify user menu in edit on sight", false));
		systemInterceptionPoints.put("ComponentEditor.ContentNotifications", new InterceptionPointVO("ComponentEditor", "ComponentEditor.ContentNotifications", "This interception point limits who can see the content notification info menu in edit on sight", false));
		systemInterceptionPoints.put("ComponentEditor.PageNotifications", new InterceptionPointVO("ComponentEditor", "ComponentEditor.PageNotifications", "This interception point limits who can see the page notification menu in edit on sight", false));
				
	    systemInterceptionPoints.put("ComponentPropertyEditor.EditProperty", new InterceptionPointVO("ComponentPropertyEditor", "ComponentPropertyEditor.EditProperty", "This interception point limits who can edit a specific component property", true));
	    systemInterceptionPoints.put("ComponentPropertyEditor.EditAdvancedProperties", new InterceptionPointVO("ComponentPropertyEditor", "ComponentPropertyEditor.EditAdvancedProperties", "This interception point limits who can edit advanced properties on a component", false));
	    
	    systemInterceptionPoints.put("Publication.Write", new InterceptionPointVO("Publication", "Publication.Write", "This interception point intercepts publications", true));
	    systemInterceptionPoints.put("Common.SubmitToPublishButton", new InterceptionPointVO("Common", "Common.SubmitToPublishButton", "Intercepts the submit to publish button", false));
	    systemInterceptionPoints.put("Common.PublishButton", new InterceptionPointVO("Common", "Common.PublishButton", "Intercepts the publish button", false));
	    
	    systemInterceptionPoints.put("ManagementToolMenu.Repositories", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.Repositories", "Intercepts access to the menu item 'Repositories' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.SystemUsers", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.SystemUsers", "Intercepts access to the menu item 'SystemUsers' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.Roles", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.Roles", "Intercepts access to the menu item 'Roles' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.Groups", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.Groups", "Intercepts access to the menu item 'Groups' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.Languages", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.Languages", "Intercepts access to the menu item 'Languages' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.InterceptionPoints", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.InterceptionPoints", "Intercepts access to the menu item 'InterceptionPoints' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.Interceptors", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.Interceptors", "Intercepts access to the menu item 'Interceptors' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.ServiceDefinitions", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.ServiceDefinitions", "Intercepts access to the menu item 'ServiceDefinitions' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.AvailableServiceBindings", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.AvailableServiceBindings", "Intercepts access to the menu item 'AvailableServiceBindings' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.SiteNodeTypeDefinitions", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.SiteNodeTypeDefinitions", "Intercepts access to the menu item 'SiteNodeTypeDefinitions' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.Categories", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.Categories", "Intercepts access to the menu item 'Categories' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.ContentTypeDefinitions", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.ContentTypeDefinitions", "Intercepts access to the menu item 'ContentTypeDefinitions' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.Up2Date", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.Up2Date", "Intercepts access to the menu item 'Up2Date' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.Workflows", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.Workflows", "Intercepts access to the menu item 'Workflows' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.Portlets", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.Portlets", "Intercepts access to the menu item 'Portlets' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.Redirects", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.Redirects", "Intercepts access to the menu item 'Redirects' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.ApplicationSettings", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.ApplicationSettings", "Intercepts access to the menu item 'Application settings' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.MessageCenter", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.MessageCenter", "Intercepts access to the menu item 'Message center' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.Diagnostics", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.Diagnostics", "Intercepts access to the menu item 'Diagnostics' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.SystemTools", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.SystemTools", "Intercepts access to the menu item 'SystemTools' in management tool", false));
	    systemInterceptionPoints.put("ManagementToolMenu.TransactionHistory", new InterceptionPointVO("ManagementToolMenu", "ManagementToolMenu.TransactionHistory", "Intercepts access to the menu item 'TransactionHistory' in management tool", false));

	    systemInterceptionPoints.put("Role.Read", new InterceptionPointVO("Role", "Role.Read", "Intercepts the read of a role", true));
	    systemInterceptionPoints.put("Role.ReadForAssignment", new InterceptionPointVO("Role", "Role.ReadForAssignment", "Intercepts the read of a role when assigning them to a user", true));
	    systemInterceptionPoints.put("Role.Write", new InterceptionPointVO("Role", "Role.Write", "Intercepts the write of a role", true));
	    systemInterceptionPoints.put("Role.Create", new InterceptionPointVO("Role", "Role.Create", "Intercepts the creation of a new role", true));
	    systemInterceptionPoints.put("Role.Delete", new InterceptionPointVO("Role", "Role.Delete", "Intercepts the deletion of a role", true));
	    systemInterceptionPoints.put("Role.ManageUsers", new InterceptionPointVO("Role", "Role.ManageUsers", "Intercepts the management of users", true));
	    systemInterceptionPoints.put("Role.ManageAccessRights", new InterceptionPointVO("Role", "Role.ManageAccessRights", "Intercepts the management of access rights", true));
	    systemInterceptionPoints.put("Role.ManageAllAccessRights", new InterceptionPointVO("Role", "Role.ManageAllAccessRights", "Intercepts the management of access rights", false));

	    systemInterceptionPoints.put("Group.Read", new InterceptionPointVO("Group", "Group.Read", "Intercepts the read of a group", true));
	    systemInterceptionPoints.put("Group.ReadForAssignment", new InterceptionPointVO("Group", "Group.ReadForAssignment", "Intercepts the read of a group when assigning them to a user", true));
	    systemInterceptionPoints.put("Group.Write", new InterceptionPointVO("Group", "Group.Write", "Intercepts the write of a group", true));
	    systemInterceptionPoints.put("Group.Create", new InterceptionPointVO("Group", "Group.Create", "Intercepts the creation of a new group", true));
	    systemInterceptionPoints.put("Group.Delete", new InterceptionPointVO("Group", "Group.Delete", "Intercepts the deletion of a group", true));
	    systemInterceptionPoints.put("Group.ManageUsers", new InterceptionPointVO("Group", "Group.ManageUsers", "Intercepts the management of users", true));
	    systemInterceptionPoints.put("Group.ManageAccessRights", new InterceptionPointVO("Group", "Group.ManageAccessRights", "Intercepts the management of access rights", true));
	    systemInterceptionPoints.put("Group.ManageAllAccessRights", new InterceptionPointVO("Group", "Group.ManageAllAccessRights", "Intercepts the management of access rights", false));

	    systemInterceptionPoints.put("WebDAV.Read", new InterceptionPointVO("WebDav", "WebDAV.Read", "Intercepts the WebDAV feature", false));
	    systemInterceptionPoints.put("ViewApplicationState.Read", new InterceptionPointVO("ViewApplicationState", "ViewApplicationState.Read", "Intercepts access to the View application state screens", false));
	}
    
	/**
	 * Factory method
	 */

	public static InterceptionPointController getController()
	{
		return new InterceptionPointController();
	}
	
	public InterceptionPoint getInterceptionPointWithId(Integer interceptionPointId, Database db) throws SystemException, Bug
	{
		return (InterceptionPoint) getObjectWithId(InterceptionPointImpl.class, interceptionPointId, db);
	}

	public InterceptionPoint getReadOnlyInterceptionPointWithId(Integer interceptionPointId, Database db) throws SystemException, Bug
	{
		return (InterceptionPoint) getObjectWithIdAsReadOnly(InterceptionPointImpl.class, interceptionPointId, db);
	}

	public InterceptionPointVO getInterceptionPointVOWithId(Integer interceptionPointId) throws SystemException, Bug
	{
		return (InterceptionPointVO) getVOWithId(InterceptionPointImpl.class, interceptionPointId);
	}
  
	public List getInterceptionPointVOList() throws SystemException, Bug
	{
		return getAllVOObjects(InterceptionPointImpl.class, "interceptionPointId");
	}

	/**
	 * This method returns Interception points which do stuff in the system but which are not yet activated.
	 * @return List of InterceptionPointVO:s
	 * @throws SystemException
	 * @throws Bug
	 */
	public List<InterceptionPointVO> getInactiveInterceptionPointVOList() throws SystemException, Bug
	{
		List<InterceptionPointVO> inactiveInterceptionPointVOList = new ArrayList();
		
		List interceptionPointVOList = getInterceptionPointVOList();

		Collection allInterceptionPoints = systemInterceptionPoints.values();
		Iterator allInterceptionPointsIterator = allInterceptionPoints.iterator();
		while(allInterceptionPointsIterator.hasNext())
		{
			InterceptionPointVO possibleInterceptionPoint = (InterceptionPointVO)allInterceptionPointsIterator.next();
			
			boolean exists = false;
			Iterator interceptionPointVOListIterator = interceptionPointVOList.iterator();
			while(interceptionPointVOListIterator.hasNext())
			{
				InterceptionPointVO existingInterceptionPointVO = (InterceptionPointVO)interceptionPointVOListIterator.next();
				if(existingInterceptionPointVO.getName().equals(possibleInterceptionPoint.getName()))
					exists = true;
			}
			
			//if(exists)
			//	allInterceptionPointsIterator.remove();
			if(!exists)
				inactiveInterceptionPointVOList.add(possibleInterceptionPoint);
		}
		
		return inactiveInterceptionPointVOList;
	}

	public List getSortedInterceptionPointVOList() throws SystemException, Bug
	{
		return getAllVOObjects(InterceptionPointImpl.class, "category", "asc");
	}	

	public InterceptionPointVO getInterceptionPointVOWithName(String interceptorPointName)  throws SystemException, Bug
	{
		InterceptionPointVO interceptionPointVO = null;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			interceptionPointVO = getInterceptionPointVOWithName(interceptorPointName, db);
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return interceptionPointVO;		
	}	

	public InterceptionPointVO getInterceptionPointVOWithName(String interceptorPointName, Database db)  throws SystemException, Bug
	{
	    String key = "" + interceptorPointName;
		logger.info("key:" + key);
		
		InterceptionPointVO interceptionPointVO = null;
		
	    Object object = CacheController.getCachedObject("interceptionPointCache", key);
		
	    if(object instanceof NullObject)
		{
			return null;
		}
		else if(object != null)
		{
		    interceptionPointVO = (InterceptionPointVO)object;
		}
		else
		{

		/*
		InterceptionPointVO interceptionPointVO = (InterceptionPointVO)CacheController.getCachedObject("interceptionPointCache", key);
		if(interceptionPointVO != null)
		{
			logger.info("There was an cached interceptionPointVO:" + interceptionPointVO);
		}
		else
		{
		*/	
			InterceptionPoint interceptorPoint = null;
			
			try
			{
				OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.InterceptionPointImpl f WHERE f.name = $1");
				oql.bind(interceptorPointName);
				
				QueryResults results = oql.execute(Database.ReadOnly);
				if(results.hasMore()) 
				{
					interceptorPoint = (InterceptionPoint)results.next();
					interceptionPointVO = interceptorPoint.getValueObject();
												 
					CacheController.cacheObject("interceptionPointCache", key, interceptionPointVO);				
				}
				else
				{	
				    CacheController.cacheObject("interceptionPointCache", key, new NullObject());
				}
				
				results.close();
				oql.close();
			}
			catch(Exception e)
			{
				throw new SystemException("An error occurred when we tried to fetch an InterceptionPointVO. Reason:" + e.getMessage(), e);    
			}
		}
		
		return interceptionPointVO;		
	}	


	public InterceptionPoint getInterceptionPointWithName(String interceptorPointName, Database db)  throws SystemException, Bug
	{
		InterceptionPoint interceptorPoint = null;
		
		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.InterceptionPointImpl f WHERE f.name = $1");
			oql.bind(interceptorPointName);
			
			QueryResults results = oql.execute();
			this.logger.info("Fetching entity in read/write mode:" + interceptorPointName);
			if(results.hasMore()) 
			{
				interceptorPoint = (InterceptionPoint)results.next();
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch an InterceptionPointVO. Reason:" + e.getMessage(), e);    
		}
		
		return interceptorPoint;		
	}
	
	
	public List getInterceptionPointVOList(String category) throws SystemException, Bug
	{
		List interceptionPointVOList = null;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			interceptionPointVOList = toVOList(getInterceptionPointList(category, db));

			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return interceptionPointVOList;	
	}
	
	
	public List getInterceptionPointList(String category, Database db)  throws SystemException, Bug
	{
		List interceptionPoints = new ArrayList();
		
		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.InterceptionPointImpl f WHERE f.category = $1");
			oql.bind(category);
			
			QueryResults results = oql.execute();
			this.logger.info("Fetching entity in read/write mode:" + category);

			while(results.hasMore()) 
			{
				InterceptionPoint interceptionPoint = (InterceptionPoint)results.next();
				interceptionPoints.add(interceptionPoint);
			}
			
			results.close();
			oql.close();
		}
		catch(Exception e)
		{
			throw new SystemException("An error occurred when we tried to fetch an InterceptionPointVO. Reason:" + e.getMessage(), e);    
		}
		
		return interceptionPoints;		
	}

	/**
	 * Creates a new Interception point
	 * 
	 * @param interceptionPointVO
	 * @return
	 * @throws ConstraintException
	 * @throws SystemException
	 */
	
	public InterceptionPointVO create(InterceptionPointVO interceptionPointVO) throws ConstraintException, SystemException
	{
		InterceptionPointVO newInterceptionPointVO = null;
		
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);

			newInterceptionPointVO = create(interceptionPointVO, db);
				
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
		
		return newInterceptionPointVO;
	}		
	
	/**
	 * Creates a new Interception point within a transaction
	 * 
	 * @param interceptionPointVO
	 * @return
	 * @throws ConstraintException
	 * @throws SystemException
	 */
	
	public InterceptionPointVO create(InterceptionPointVO interceptionPointVO, Database db) throws SystemException, Exception
	{
		InterceptionPoint interceptionPoint = new InterceptionPointImpl();
		interceptionPoint.setValueObject(interceptionPointVO);
		
		db.create(interceptionPoint);
					
		return interceptionPoint.getValueObject();
	}     

	
	public InterceptionPointVO update(InterceptionPointVO interceptionPointVO) throws ConstraintException, SystemException
	{
		return (InterceptionPointVO) updateEntity(InterceptionPointImpl.class, interceptionPointVO);
	}        

	
	public void update(InterceptionPointVO interceptionPointVO, String[] values) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();

		try 
		{
			beginTransaction(db);
			
			ConstraintExceptionBuffer ceb = interceptionPointVO.validate();
			ceb.throwIfNotEmpty();
			
			InterceptionPoint interceptionPoint = this.getInterceptionPointWithId(interceptionPointVO.getInterceptionPointId(), db);

			interceptionPoint.setValueObject(interceptionPointVO);
			
			Collection interceptors = interceptionPoint.getInterceptors();
			Iterator interceptorsIterator = interceptors.iterator();
			while(interceptorsIterator.hasNext())
			{
				Interceptor interceptor = (Interceptor)interceptorsIterator.next();
				interceptor.getInterceptionPoints().remove(interceptionPoint);
			}
			
			interceptionPoint.getInterceptors().clear();
	    	
	    	if(values != null)
	    	{
				for(int i=0; i<values.length; i++)
				{
					String interceptorId = values[i];
					Interceptor interceptor = InterceptorController.getController().getInterceptorWithId(new Integer(interceptorId), db);
					interceptionPoint.getInterceptors().add(interceptor);
					interceptor.getInterceptionPoints().add(interceptionPoint);
				}
			}
			
	    	logger.info("Interceptors:" + interceptionPoint.getInterceptors().size());
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			logger.info("An error occurred so we should not complete the transaction:" + e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}			
	
	
	public void delete(InterceptionPointVO interceptionPointVO) throws ConstraintException, SystemException
	{
		Database db = CastorDatabaseService.getDatabase();
		
		beginTransaction(db);

		try
		{
			InterceptionPoint interceptionPoint = this.getInterceptionPointWithId(interceptionPointVO.getInterceptionPointId(), db);
			
			List accessRights = AccessRightController.getController().getAccessRightList(interceptionPoint.getInterceptionPointId(), db);
			Iterator accessRightsIterator = accessRights.iterator();
			while(accessRightsIterator.hasNext())
			{
				AccessRight accessRight = (AccessRight)accessRightsIterator.next();
				
				Iterator groupIterator = accessRight.getGroups().iterator();
				while(groupIterator.hasNext())
				{
					AccessRightGroup group = (AccessRightGroup)groupIterator.next();
					groupIterator.remove();
					db.remove(group);
				}
				
				Iterator roleIterator = accessRight.getRoles().iterator();
				while(roleIterator.hasNext())
				{
					AccessRightRole role = (AccessRightRole)roleIterator.next();
					roleIterator.remove();
					db.remove(role);
				}

				Iterator userIterator = accessRight.getUsers().iterator();
				while(userIterator.hasNext())
				{
					AccessRightUser user = (AccessRightUser)userIterator.next();
					userIterator.remove();
					db.remove(user);
				}

				db.remove(accessRight);
				accessRightsIterator.remove();
			}
			
			db.remove(interceptionPoint);
	
			commitTransaction(db);
		}
		catch(Exception e)
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	
	}        

	/*
	public void delete(String name, String value, Database db) throws SystemException, Exception
	{
		List AccessList = getAccessList(name, value, db);
		Iterator i = AccessList.iterator();
		while(i.hasNext())
		{
			Access Access = (Access)i.next();
			db.remove(Access);
		}
		
	}        
	*/

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new InterceptionPointVO();
	}

}
 
