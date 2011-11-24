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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.contenttool.wizards.actions.CreateContentWizardInfoBean;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;


/**
 * @author Mattias Bogeblad
 */

public class ContentControllerProxy extends ContentController 
{
    private final static Logger logger = Logger.getLogger(ContentControllerProxy.class.getName());

	protected static final Integer NO 			= new Integer(0);
	protected static final Integer YES 			= new Integer(1);
	protected static final Integer INHERITED 	= new Integer(2);

	private static List interceptors = new ArrayList();

	public static ContentControllerProxy getController()
	{
		return new ContentControllerProxy();
	}
	

	/**
	 * This method returns a specific content-object after checking that it is accessable by the given user
	 */
	
    public ContentVO getACContentVOWithId(InfoGluePrincipal infogluePrincipal, Integer contentId, Database db) throws ConstraintException, SystemException, Bug, Exception
    {
    	Map hashMap = new HashMap();
    	hashMap.put("contentId", contentId);
    	
		intercept(hashMap, "Content.Read", infogluePrincipal);
				
		return getSmallContentVOWithId(contentId, db);
    } 

	/**
	 * This method returns a specific content-object after checking that it is accessable by the given user
	 */
	
    public ContentVO getACContentVOWithId(InfoGluePrincipal infogluePrincipal, Integer contentId) throws ConstraintException, SystemException, Bug, Exception
    {
    	Map hashMap = new HashMap();
    	hashMap.put("contentId", contentId);
    	
		intercept(hashMap, "Content.Read", infogluePrincipal);
				
		return getContentVOWithId(contentId);
    } 

	/**
	 * This method returns a list of content-objects after checking that it is accessable by the given user
	 */
		
    public List getACContentVOList(InfoGluePrincipal infoGluePrincipal, HashMap argumentHashMap, Database db) throws SystemException, Bug, Exception
    {
    	return getACContentVOList(infoGluePrincipal, argumentHashMap, "Content.Read", db);
    }
   
	/**
	 * This method returns a list of content-objects after checking that it is accessable by the given user
	 */
		
    public List getACContentVOList(InfoGluePrincipal infoGluePrincipal, HashMap argumentHashMap, String interceptionPointName, Database db) throws SystemException, Bug, Exception
    {
    	List contents = null;
    	
    	String method = (String)argumentHashMap.get("method");
    	logger.info("method:" + method);
    	
    	if(method.equalsIgnoreCase("selectContentListOnIdList"))
    	{
			contents = new ArrayList();
			List arguments = (List)argumentHashMap.get("arguments");
			logger.info("Arguments:" + arguments.size());  
			Iterator argumentIterator = arguments.iterator();
			while(argumentIterator.hasNext())
			{ 		
				HashMap argument = (HashMap)argumentIterator.next(); 
				Integer contentId = new Integer((String)argument.get("contentId"));
				logger.info("Getting the content with Id:" + contentId);
				try
				{
				    contents.add(this.getACContentVOWithId(infoGluePrincipal, contentId, db));
				}
				catch(Exception e)
				{
				    //TODO - remove later
				    e.printStackTrace();
				}
			}
    	}
        else if(method.equalsIgnoreCase("selectListOnContentTypeName"))
    	{
			List arguments = (List)argumentHashMap.get("arguments");
			logger.info("Arguments:" + arguments.size());   		
			contents = getContentVOListByContentTypeNames(arguments, db);
			Iterator contentIterator = contents.iterator();
			while(contentIterator.hasNext())
			{
			    ContentVO candidateContentVO = (ContentVO)contentIterator.next();
			    
		    	Map hashMap = new HashMap();
		    	hashMap.put("contentId", candidateContentVO.getContentId());
		    	
		    	try
		    	{
		    		if(interceptionPointName.equals("Component.Select"))
		    		{
		    			InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName(interceptionPointName, db);
		    			if(interceptionPointVO != null)
		    				intercept(hashMap, interceptionPointName, infoGluePrincipal, false, db);
		    		}
		    		else
		    			intercept(hashMap, interceptionPointName, infoGluePrincipal, false, db);
		    	}
		    	catch(Exception e)
		    	{
		    		logger.info("Was not authorized to look at task...");
		    	    contentIterator.remove();
		    	}
			}
    	}
        return contents;
    }

    
	/**
	 * This method returns a list of content-objects after checking that it is accessable by the given user
	 */
		
    public List getACContentVOList(InfoGluePrincipal infoGluePrincipal, HashMap argumentHashMap) throws SystemException, Bug
    {
    	List contents = null;
    	
    	String method = (String)argumentHashMap.get("method");
    	logger.info("method:" + method);
    	
    	if(method.equalsIgnoreCase("selectContentListOnIdList"))
    	{
			contents = new ArrayList();
			List arguments = (List)argumentHashMap.get("arguments");
			logger.info("Arguments:" + arguments.size());  
			Iterator argumentIterator = arguments.iterator();
			while(argumentIterator.hasNext())
			{ 		
				HashMap argument = (HashMap)argumentIterator.next(); 
				Integer contentId = new Integer((String)argument.get("contentId"));
				//logger.info("Getting the content with Id:" + contentId);
				try
				{
				    contents.add(this.getACContentVOWithId(infoGluePrincipal, contentId));
				}
				catch(Exception e)
				{
				    //TODO - remove later
				    e.printStackTrace();
				}
			}
    	}
        else if(method.equalsIgnoreCase("selectListOnContentTypeName"))
    	{
			List arguments = (List)argumentHashMap.get("arguments");
			logger.info("Arguments:" + arguments.size());   		
			contents = getContentVOListByContentTypeNames(arguments);
			Iterator contentIterator = contents.iterator();
			while(contentIterator.hasNext())
			{
			    ContentVO candidateContentVO = (ContentVO)contentIterator.next();
			    
		    	Map hashMap = new HashMap();
		    	hashMap.put("contentId", candidateContentVO.getContentId());
		    	
		    	try
		    	{
		    	    intercept(hashMap, "Content.Read", infoGluePrincipal, false);
		    	}
		    	catch(Exception e)
		    	{
		    		logger.info("Was not authorized to look at task...");
		    	    contentIterator.remove();
		    	}
			}
    	}
        return contents;
    }
    
	/**
	 * This method finishes up what the create content wizard has resulted after first checking that the user has rights to complete the action.
	 */

	public ContentVO acCreate(InfoGluePrincipal infogluePrincipal, CreateContentWizardInfoBean createContentWizardInfoBean) throws ConstraintException, SystemException, Bug, Exception
	{
		//Map hashMap = new HashMap();
		//hashMap.put("contentId", parentContentId);
    	
		//intercept(hashMap, "Content.Create", infogluePrincipal);

		return ContentController.getContentController().create(createContentWizardInfoBean);
	}   

	/**
	 * This method creates a content after first checking that the user has rights to edit it.
	 */

	public ContentVO acCreate(InfoGluePrincipal infogluePrincipal, Integer parentContentId, Integer contentTypeDefinitionId, Integer repositoryId, ContentVO contentVO) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("contentId", parentContentId);
    	
		intercept(hashMap, "Content.Create", infogluePrincipal);

		return ContentController.getContentController().create(parentContentId, contentTypeDefinitionId, repositoryId, contentVO);
	}   
	
	/**
	 * This method updates a content after first checking that the user has rights to edit it.
	 */

	public ContentVO acUpdate(InfoGluePrincipal infogluePrincipal, ContentVO contentVO, Integer contentTypeDefinitionId) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("contentId", contentVO.getId());
    	
		intercept(hashMap, "Content.Write", infogluePrincipal);

		return update(contentVO, contentTypeDefinitionId);
	}   
	
	/**
	 * This method deletes a content after first checking that the user has rights to edit it.
	 */

	public void acDelete(InfoGluePrincipal infogluePrincipal, ContentVO contentVO) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("contentId", contentVO.getId());
    	
		intercept(hashMap, "Content.Delete", infogluePrincipal);

	    delete(contentVO, infogluePrincipal);
	}   

	/**
	 * This method deletes a content after first checking that the user has rights to edit it.
	 */

	public void acMarkForDelete(InfoGluePrincipal infogluePrincipal, ContentVO contentVO) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("contentId", contentVO.getId());
    	
		intercept(hashMap, "Content.Delete", infogluePrincipal);

		markForDeletion(contentVO, infogluePrincipal);
	}   

	
	/**
	 * This method moves a content after first checking that the user has rights to edit it.
	 */

	public void acMoveContent(InfoGluePrincipal infogluePrincipal, ContentVO contentVO, Integer newParentContentId) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("contentId", contentVO.getId());
    	
		intercept(hashMap, "Content.Move", infogluePrincipal);
		
		hashMap = new HashMap();
		hashMap.put("contentId", newParentContentId);

		intercept(hashMap, "Content.Create", infogluePrincipal);

		moveContent(contentVO, newParentContentId);
	}   

	/**
	 * This method moves a content after first checking that the user has rights to within a transaction.
	 */

	public void acMoveContent(InfoGluePrincipal infogluePrincipal, ContentVO contentVO, Integer newParentContentId, Database db) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("contentId", contentVO.getId());
    	
		intercept(hashMap, "Content.Move", infogluePrincipal);
		
		hashMap = new HashMap();
		hashMap.put("contentId", newParentContentId);

		intercept(hashMap, "Content.Create", infogluePrincipal);

		moveContent(contentVO, newParentContentId, db);
	}   

	/**
	 * This method returns true if the if the content in question is protected.
	 */

	public Integer getProtectedContentId(Integer contentId)
	{
		Integer protectedContentId = null;
	
		try
		{
			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
			if(contentVO.getIsProtected() != null)
			{	
				if(contentVO.getIsProtected().intValue() == NO.intValue())
					protectedContentId = null;
				else if(contentVO.getIsProtected().intValue() == YES.intValue())
					protectedContentId = contentVO.getId();
				else if(contentVO.getIsProtected().intValue() == INHERITED.intValue())
				{
					if(contentVO.getParentContentId() != null)
					{
						ContentVO parentContentVO = ContentController.getContentController().getContentVOWithId(contentVO.getParentContentId());
						if(parentContentVO != null)
							protectedContentId = getProtectedContentId(parentContentVO.getId()); 
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
			
		return protectedContentId;
	}	

	/**
	 * This method returns true if the if the content in question is protected.
	 */

	public Integer getProtectedContentId(Integer contentId, Database db)
	{
		Integer protectedContentId = null;
		boolean isContentProtected = false;
	
		try
		{
			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId, db);
			if(contentVO.getIsProtected() != null)
			{	
				if(contentVO.getIsProtected().intValue() == NO.intValue())
					protectedContentId = null;
				else if(contentVO.getIsProtected().intValue() == YES.intValue())
					protectedContentId = contentVO.getId();
				else if(contentVO.getIsProtected().intValue() == INHERITED.intValue())
				{
					ContentVO parentContentVO = ContentController.getParentContent(contentId, db);
					if(parentContentVO != null)
						protectedContentId = getProtectedContentId(parentContentVO.getId(), db); 
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
			
		return protectedContentId;
	}	

	/**
	 * This method returns true if the if the content in question is protected.
	 */

	public boolean getIsContentProtected(Integer contentId)
	{
		boolean isContentProtected = false;
	
		try
		{
			ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
			if(contentVO.getIsProtected() != null)
			{	
				if(contentVO.getIsProtected().intValue() == NO.intValue())
					isContentProtected = false;
				else if(contentVO.getIsProtected().intValue() == YES.intValue())
					isContentProtected = true;
				else if(contentVO.getIsProtected().intValue() == INHERITED.intValue())
				{
					ContentVO parentContentVO = ContentController.getParentContent(contentId);
					if(parentContentVO != null)
						isContentProtected = getIsContentProtected(parentContentVO.getId()); 
				}
			}

		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
			
		return isContentProtected;
	}
	
	/*
    public static Content getContentWithId(Integer contentId, Database db) throws SystemException, Bug
    {
		return (Content) getObjectWithId(ContentImpl.class, contentId, db);
    }
    */
/*
    public static List getContentVOList() throws SystemException, Bug
    {
        return getAllVOObjects(ContentImpl.class);
    }
*/
 
}
