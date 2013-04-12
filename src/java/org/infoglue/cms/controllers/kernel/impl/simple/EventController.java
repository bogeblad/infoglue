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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.Repository;
import org.infoglue.cms.entities.management.impl.simple.RepositoryImpl;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.entities.workflow.Event;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.entities.workflow.impl.simple.EventImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.util.Timer;

/**
 * @author Mattias Bogeblad
 *
 * This class implements all operations we can do on the cmEvent-entity.
 */

public class EventController extends BaseController 
{
    private final static Logger logger = Logger.getLogger(EventController.class.getName());

    /**
     * Gets the eventVO in a readonly transaction.
     */
    
    public static EventVO getEventVOWithId(Integer eventId) throws SystemException, Bug
    {
		return (EventVO) getVOWithId(EventImpl.class, eventId);
    }
   	
    /**
     * Gets the event in the given transaction.
     */
	
    public static Event getEventWithId(Integer eventId, Database db) throws SystemException, Bug
    {
		return (Event) getObjectWithId(EventImpl.class, eventId, db);
    }

    /**
     * Gets all events in a read only transaction.
     */

    public List getEventVOList() throws SystemException, Bug
    {
        return getAllVOObjects(EventImpl.class, "eventId");
    }

	/**
	 * Creates a new Event with the values in the eventVO sent in. 
	 */
	
	public static EventVO create(EventVO eventVO, Integer repositoryId, InfoGluePrincipal infoGluePrincipal, Database db) throws SystemException
    {
        Event event = new EventImpl();
        event.setValueObject(eventVO);				
        event.setRepositoryId(repositoryId);
        event.setCreator(infoGluePrincipal.getName());
        
        try
        {
            db.create(event);
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            throw new SystemException(e.getMessage());
        }
        
        return event.getValueObject();
    }  


	/**
	 * Creates a new Event with the values in the eventVO sent in in a new transaction. 
	 */
	
	public static EventVO create(EventVO eventVO, Integer repositoryId, InfoGluePrincipal infoGluePrincipal) throws SystemException
    {
        Event event = null;
		
        Database db = CastorDatabaseService.getDatabase();
		beginTransaction(db);
		try
        {
	        event = new EventImpl();
	        event.setValueObject(eventVO);				
	        event.setRepositoryId(repositoryId);
            event.setCreator(infoGluePrincipal.getName());
            db.create(event);
    
            commitTransaction(db);
        }
        catch(Exception e)
        {
        	logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
        return event.getValueObject();
    }  
	

    
    /**
     * This method removes an event from the database.
     */
                       
	public static void delete(EventVO eventVO) throws SystemException
    {
	    deleteEntity(EventImpl.class, eventVO.getEventId());
    }        


    /**
     * This method removes an event from the database.
     */
                       
	public static void delete(Event event, Database db) throws SystemException
	{
		try
		{
			db.remove(event);
		}
		catch (Exception e)
		{
			throw new SystemException(e);
		}
	}

	/**
	 * This method updates an event.
	 */
	
	public static EventVO update(EventVO eventVO) throws SystemException
    {
    	return (EventVO) updateEntity(EventImpl.class, eventVO);
    }        





	/**
	 * Returns a list of events currently available for the certain entity.
	 */
	
	public static List getEventVOListForEntity(String entityClass, Integer entityId) throws SystemException, Bug
	{
		List events = new ArrayList();
		Database db = CastorDatabaseService.getDatabase();
		beginTransaction(db);

		try
		{
			OQLQuery oql = db.getOQLQuery("SELECT e FROM org.infoglue.cms.entities.workflow.impl.simple.EventImpl e WHERE e.entityClass = $1 AND e.entityId = $2");
			oql.bind(entityClass);
			oql.bind(entityId);

			QueryResults results = oql.execute(Database.ReadOnly);

			while (results.hasMore())
			{
				Event event = (Event)results.next();
				events.add(event.getValueObject());
			}

			results.close();
			oql.close();

			commitTransaction(db);
		}
		catch (Exception e)
		{
			logger.error("An error occurred so we should not completes the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}

		return events;
	}

	/**
	 * Returns a list of events with either publish or unpublish-state currently available for the repository stated.
	 */
	
	public static List getPublicationEventVOListForRepository(Integer repositoryId) throws SystemException, Bug
	{
		return getPublicationEventVOListForRepository(repositoryId, null, null, false);
	}
	
	/**
	 * Returns a list of events with either publish or unpublish-state currently available for the repository stated.
	 */
	
	public static Map<Integer,List<EventVO>> getPublicationRepositoryEvents() throws SystemException, Bug
	{
		Map<Integer,List<EventVO>> repoEvents = new HashMap<Integer,List<EventVO>>();
		
		boolean hasBrokenItems = false;
		
		Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);
		try
        {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MONTH, -12);
			
            OQLQuery oql = db.getOQLQuery( "SELECT e FROM org.infoglue.cms.entities.workflow.impl.simple.EventImpl e WHERE (e.typeId = $1 OR e.typeId = $2) AND e.creationDateTime > $3 ORDER BY e.eventId desc LIMIT $4");
        	oql.bind(EventVO.PUBLISH);
        	oql.bind(EventVO.UNPUBLISH_LATEST);
        	oql.bind(cal.getTime());
        	oql.bind(1000);
        	
        	//logger.info("Fetching entity in read/write mode" + repositoryId);
        	QueryResults results = oql.execute(Database.ReadOnly);
        	
			while (results.hasMore()) 
            {
            	Event event = (Event)results.next();
            	if(event.getRepositoryId() != null)
            	{
	             	List<EventVO> events = repoEvents.get(event.getRepositoryId());
	             	if(events == null)
	             	{
	             		events = new ArrayList<EventVO>();
	             		repoEvents.put(event.getRepositoryId(), events);
	             	}
	             	events.add(event.getValueObject());
            	}
            	else
            		System.out.println("Skipping event as it does not belong to a repo...:" + event.getId());
            }
            
			results.close();
			oql.close();

            commitTransaction(db);
        }
        catch(Exception e)
        {
        	logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        return repoEvents;	
	}

	
	/**
	 * Returns a list of events with either publish or unpublish-state currently available for the repository stated.
	 */
	
	public static List getPublicationEventVOListForRepository(Integer repositoryId, InfoGluePrincipal principal, String filter, boolean validate) throws SystemException, Bug
	{
		List events = new ArrayList();
		boolean hasBrokenItems = false;
		
		Database db = CastorDatabaseService.getDatabase();

		beginTransaction(db);
		
		try
        {
            OQLQuery oql = db.getOQLQuery( "SELECT e FROM org.infoglue.cms.entities.workflow.impl.simple.EventImpl e WHERE (e.typeId = $1 OR e.typeId = $2) AND e.repositoryId = $3 ORDER BY e.eventId desc");
        	oql.bind(EventVO.PUBLISH);
        	oql.bind(EventVO.UNPUBLISH_LATEST);
        	oql.bind(repositoryId);
        	
        	//logger.info("Fetching entity in read/write mode" + repositoryId);
        	QueryResults results = oql.execute(Database.READONLY);
        	
			while (results.hasMore()) 
            {
            	Event event = (Event)results.next();
    		    //logger.warn("event:" + event.getId());
            	//logger.warn("entityClass:" + event.getEntityClass());
            	//logger.warn("entityId:" + event.getEntityId());

            	boolean isValid = true;
            	
            	if(validate)
            	{
	            	try
	            	{
		            	if(event.getEntityClass().equalsIgnoreCase(ContentVersion.class.getName()))
		            	{
		            		//ContentVersion contentVersion = null;
		            		ContentVersionVO contentVersionVO = null;
		            		ContentVO contentVO = null;
		            		try
		            		{
		            			contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(event.getEntityId(), db);
		            			//contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(event.getEntityId(), db);
		            			if(contentVersionVO != null && contentVersionVO.getContentId() != null)
		            				contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId(), db);
		            		}
		            		catch(SystemException e)
		            		{
		            			hasBrokenItems = true;
		            			throw e;
		            		}
		            		
		            		if(contentVersionVO == null || contentVO == null)
		    	            {
								isValid = false;
		            			hasBrokenItems = true;
							}
		            		else
		            		{
		            			if(principal != null && filter != null && filter.equalsIgnoreCase("groupBased"))
			        		    {
		            				String versionModifier = contentVersionVO.getVersionModifier();
		            				if(versionModifier != null)
		            				{
		            					InfoGluePrincipal versionModifierPrincipal = UserControllerProxy.getController(db).getUser(versionModifier);
		            					if(versionModifierPrincipal != null)
		            					{
		            						boolean hasGroup = false;
		            						
		            						List groups = versionModifierPrincipal.getGroups();
		            						Iterator groupsIterator = groups.iterator();
		            						while(groupsIterator.hasNext())
		            						{
		            							InfoGlueGroup group = (InfoGlueGroup)groupsIterator.next();
		            							if(principal.getGroups().contains(group))
		            								hasGroup = true;
		            						}
		            						
		            						if(!hasGroup)
		            							isValid = false;
		            					}
		            				}
			        		    }
			            		else if(principal != null && filter != null && filter.indexOf("groupNameBased_") > -1)
			            		{
		            				String versionModifier = contentVersionVO.getVersionModifier();
		            				if(versionModifier != null)
		            				{
		            					InfoGluePrincipal versionModifierPrincipal = UserControllerProxy.getController(db).getUser(versionModifier);
		            					if(versionModifierPrincipal != null)
		            					{
		            						boolean hasGroup = false;
		            						String groupName = filter.substring(filter.indexOf("_") + 1);
		            						
		            						List groups = versionModifierPrincipal.getGroups();
		            						Iterator groupsIterator = groups.iterator();
		            						while(groupsIterator.hasNext())
		            						{
		            							InfoGlueGroup group = (InfoGlueGroup)groupsIterator.next();
		            							if(groupName.equalsIgnoreCase(group.getName()))
		            								hasGroup = true;
		            						}
		            						
		            						if(!hasGroup)
		            							isValid = false;
		            					}
		            				}	            			
			            		}
		            		}
		            	}
						else if(event.getEntityClass().equalsIgnoreCase(SiteNodeVersion.class.getName()))
						{
							SiteNodeVersionVO siteNodeVersion = null;
							SiteNodeVO siteNode = null;
							try
		            		{
								siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(event.getEntityId(), db);
								//siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithIdAsReadOnly(event.getEntityId(), db);
								if(siteNodeVersion.getSiteNodeId() != null)
									siteNode = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVersion.getSiteNodeId(), db);
		            		}
		            		catch(SystemException e)
		            		{
		            			hasBrokenItems = true;
		            			throw e;
		            		}
	
							if(siteNodeVersion == null || siteNode == null)
							{
		            			hasBrokenItems = true;
							    isValid = false;
							}
							else
		            		{
		            			if(principal != null && filter != null && filter.equalsIgnoreCase("groupBased"))
			        		    {
		            				String versionModifier = siteNodeVersion.getVersionModifier();
		            				if(versionModifier != null)
		            				{
		            					InfoGluePrincipal versionModifierPrincipal = UserControllerProxy.getController(db).getUser(versionModifier);
		            					if(versionModifierPrincipal != null)
		            					{
		            						boolean hasGroup = false;
		            						
		            						List groups = versionModifierPrincipal.getGroups();
		            						Iterator groupsIterator = groups.iterator();
		            						while(groupsIterator.hasNext())
		            						{
		            							InfoGlueGroup group = (InfoGlueGroup)groupsIterator.next();
		            							if(principal.getGroups().contains(group))
		            								hasGroup = true;
		            						}
		            						
		            						if(!hasGroup)
		            							isValid = false;
		            					}
		            				}
								}
			            		else if(principal != null && filter != null && filter.indexOf("groupNameBased_") > -1)
			            		{
		            				String versionModifier = siteNodeVersion.getVersionModifier();
		            				if(versionModifier != null)
		            				{
		            					InfoGluePrincipal versionModifierPrincipal = UserControllerProxy.getController(db).getUser(versionModifier);
		            					if(versionModifierPrincipal != null)
		            					{
		            						boolean hasGroup = false;
		            						String groupName = filter.substring(filter.indexOf("_") + 1);
		            						
		            						List groups = versionModifierPrincipal.getGroups();
		            						Iterator groupsIterator = groups.iterator();
		            						while(groupsIterator.hasNext())
		            						{
		            							InfoGlueGroup group = (InfoGlueGroup)groupsIterator.next();
		            							if(groupName.equalsIgnoreCase(group.getName()))
		            								hasGroup = true;
		            						}
		            						
		            						if(!hasGroup)
		            							isValid = false;
		            					}
		            				}	            		
								}
		            		}
						}
					}
					catch(Exception e)
					{
						isValid = false;
	        			hasBrokenItems = true;
					}
            	}
            	
				if(isValid && !hasBrokenItems)
	            	events.add(event.getValueObject());
            }
            
			results.close();
			oql.close();

            commitTransaction(db);
        }
        catch(Exception e)
        {
        	logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }

        if(hasBrokenItems)
        {
        	cleanPublicationEventVOListForRepository(repositoryId, principal, filter);
        	events = getPublicationEventVOListForRepository(repositoryId, principal, filter, validate);
        }

        return events;	
	}

	/**
	 * Returns a list of events with either publish or unpublish-state currently available for the repository stated.
	 */
	
	public static void cleanPublicationEventVOListForRepository(Integer repositoryId, InfoGluePrincipal principal, String filter) throws SystemException, Bug
	{
		Database db = CastorDatabaseService.getDatabase();
        beginTransaction(db);

        try
        {
            OQLQuery oql = db.getOQLQuery( "SELECT e FROM org.infoglue.cms.entities.workflow.impl.simple.EventImpl e WHERE (e.typeId = $1 OR e.typeId = $2) AND e.repositoryId = $3 ORDER BY e.eventId desc");
        	oql.bind(EventVO.PUBLISH);
        	oql.bind(EventVO.UNPUBLISH_LATEST);
        	oql.bind(repositoryId);
        	
        	//logger.info("Fetching entity in read/write mode" + repositoryId);
        	QueryResults results = oql.execute();
        	
			while (results.hasMore()) 
            {
            	Event event = (Event)results.next();
    	
            	boolean isBroken = false;
            	boolean isValid = true;
            	try
            	{
	            	if(event.getEntityClass().equalsIgnoreCase(ContentVersion.class.getName()))
	            	{
	            		ContentVersionVO contentVersionVO = null;
	            		ContentVO contentVO = null;
	            		try
	            		{
	            			contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(event.getEntityId(), db);
	            			//contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(event.getEntityId(), db);
	            			if(contentVersionVO != null && contentVersionVO.getContentId() != null)
	            				contentVO = ContentController.getContentController().getContentVOWithId(contentVersionVO.getContentId(), db);
	            		}
	            		catch(SystemException e)
	            		{
	            			isBroken = true;
	            			throw e;
	            		}
	            		
	            		if(contentVersionVO == null || contentVO == null)
	    	            {
							isBroken = true;
							isValid = false;
							try
							{
								ContentVersion contentVersion = ContentVersionController.getContentVersionController().getContentVersionWithId(event.getEntityId(), db);
								ContentVersionController.getContentVersionController().delete(contentVersion, db);
							}
							catch (Exception e) 
							{
								logger.error("Error deleting contentVersion which lacked content:" + e.getMessage(), e);
							}
						}
	            		else
	            		{
	            			if(principal != null && filter != null && filter.equalsIgnoreCase("groupBased"))
		        		    {
	            				String versionModifier = contentVersionVO.getVersionModifier();
	            				if(versionModifier != null)
	            				{
	            					InfoGluePrincipal versionModifierPrincipal = UserControllerProxy.getController(db).getUser(versionModifier);
	            					if(versionModifierPrincipal != null)
	            					{
	            						boolean hasGroup = false;
	            						
	            						List groups = versionModifierPrincipal.getGroups();
	            						Iterator groupsIterator = groups.iterator();
	            						while(groupsIterator.hasNext())
	            						{
	            							InfoGlueGroup group = (InfoGlueGroup)groupsIterator.next();
	            							if(principal.getGroups().contains(group))
	            								hasGroup = true;
	            						}
	            						
	            						if(!hasGroup)
	            							isValid = false;
	            					}
	            				}
		        		    }
		            		else if(principal != null && filter != null && filter.indexOf("groupNameBased_") > -1)
		            		{
	            				String versionModifier = contentVersionVO.getVersionModifier();
	            				if(versionModifier != null)
	            				{
	            					InfoGluePrincipal versionModifierPrincipal = UserControllerProxy.getController(db).getUser(versionModifier);
	            					if(versionModifierPrincipal != null)
	            					{
	            						boolean hasGroup = false;
	            						String groupName = filter.substring(filter.indexOf("_") + 1);
	            						
	            						List groups = versionModifierPrincipal.getGroups();
	            						Iterator groupsIterator = groups.iterator();
	            						while(groupsIterator.hasNext())
	            						{
	            							InfoGlueGroup group = (InfoGlueGroup)groupsIterator.next();
	            							if(groupName.equalsIgnoreCase(group.getName()))
	            								hasGroup = true;
	            						}
	            						
	            						if(!hasGroup)
	            							isValid = false;
	            					}
	            				}	            			
		            		}
	            		}
	            	}
					else if(event.getEntityClass().equalsIgnoreCase(SiteNodeVersion.class.getName()))
					{
						SiteNodeVersionVO siteNodeVersion = null;
						SiteNodeVO siteNode = null;
						
						try
	            		{
							siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(event.getEntityId(), db);
							if(siteNodeVersion != null && siteNodeVersion.getSiteNodeId() != null)
								siteNode = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVersion.getSiteNodeId(), db);
	            		}
	            		catch(SystemException e)
	            		{
	            			isBroken = true;
	            			throw e;
	            		}

						if(siteNodeVersion == null || siteNode == null)
						{
						    isBroken = true;
						    isValid = false;
						    SiteNodeVersionController.getController().delete(siteNodeVersion.getId(), db);
						}
						else
	            		{
	            			if(principal != null && filter != null && filter.equalsIgnoreCase("groupBased"))
		        		    {
	            				String versionModifier = siteNodeVersion.getVersionModifier();
	            				if(versionModifier != null)
	            				{
	            					InfoGluePrincipal versionModifierPrincipal = UserControllerProxy.getController(db).getUser(versionModifier);
	            					if(versionModifierPrincipal != null)
	            					{
	            						boolean hasGroup = false;
	            						
	            						List groups = versionModifierPrincipal.getGroups();
	            						Iterator groupsIterator = groups.iterator();
	            						while(groupsIterator.hasNext())
	            						{
	            							InfoGlueGroup group = (InfoGlueGroup)groupsIterator.next();
	            							if(principal.getGroups().contains(group))
	            								hasGroup = true;
	            						}
	            						
	            						if(!hasGroup)
	            							isValid = false;
	            					}
	            				}
							}
		            		else if(principal != null && filter != null && filter.indexOf("groupNameBased_") > -1)
		            		{
	            				String versionModifier = siteNodeVersion.getVersionModifier();
	            				if(versionModifier != null)
	            				{
	            					InfoGluePrincipal versionModifierPrincipal = UserControllerProxy.getController(db).getUser(versionModifier);
	            					if(versionModifierPrincipal != null)
	            					{
	            						boolean hasGroup = false;
	            						String groupName = filter.substring(filter.indexOf("_") + 1);
	            						
	            						List groups = versionModifierPrincipal.getGroups();
	            						Iterator groupsIterator = groups.iterator();
	            						while(groupsIterator.hasNext())
	            						{
	            							InfoGlueGroup group = (InfoGlueGroup)groupsIterator.next();
	            							if(groupName.equalsIgnoreCase(group.getName()))
	            								hasGroup = true;
	            						}
	            						
	            						if(!hasGroup)
	            							isValid = false;
	            					}
	            				}	            		
							}
	            		}
					}
				}
				catch(Exception e)
				{
					isValid = false;
				}
				    
				if(isBroken)
				    delete(event, db);
            }
            
			results.close();
			oql.close();

            commitTransaction(db);
        }
        catch(Exception e)
        {
        	logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
	}

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new EventVO();
	}

}
