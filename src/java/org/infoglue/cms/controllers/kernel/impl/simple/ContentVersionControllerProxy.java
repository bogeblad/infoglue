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
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;


/**
 * @author Mattias Bogeblad
 */

public class ContentVersionControllerProxy extends ContentVersionController 
{
    private final static Logger logger = Logger.getLogger(ContentVersionControllerProxy.class.getName());

	protected static final Integer NO 			= new Integer(0);
	protected static final Integer YES 			= new Integer(1);
	protected static final Integer INHERITED 	= new Integer(2);

	private static List interceptors = new ArrayList();

	public static ContentVersionControllerProxy getController()
	{
		return new ContentVersionControllerProxy();
	}
	
	/*
	private void intercept(Map hashMap, String InterceptionPointName, InfoGluePrincipal infogluePrincipal) throws ConstraintException, SystemException, Bug, Exception
	{
		InterceptionPointVO interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithName(InterceptionPointName);
    	
		if(interceptionPointVO == null)
			throw new SystemException("The InterceptionPoint " + InterceptionPointName + " was not found. The system will not work unless you restore it.");

		List interceptors = InterceptionPointController.getController().getInterceptorsVOList(interceptionPointVO.getInterceptionPointId());
		Iterator interceptorsIterator = interceptors.iterator();
		while(interceptorsIterator.hasNext())
		{
			InterceptorVO interceptorVO = (InterceptorVO)interceptorsIterator.next();
			logger.info("Adding interceptorVO:" + interceptorVO.getName());
			try
			{
				InfoGlueInterceptor infoGlueInterceptor = (InfoGlueInterceptor)Class.forName(interceptorVO.getClassName()).newInstance();
				infoGlueInterceptor.intercept(infogluePrincipal, interceptionPointVO, hashMap);
			}
			catch(ClassNotFoundException e)
			{
				logger.warn("The interceptor " + interceptorVO.getClassName() + "was not found: " + e.getMessage(), e);
			}
		}

	}
	*/

	/**
	 * This method returns a specific content-object after checking that it is accessable by the given user
	 */
	
    public ContentVersionVO getACContentVersionVOWithId(InfoGluePrincipal infogluePrincipal, Integer contentVersionId) throws ConstraintException, SystemException, Bug, Exception
    {
    	Map hashMap = new HashMap();
    	hashMap.put("contentVersionId", contentVersionId);
    	
		intercept(hashMap, "ContentVersion.Read", infogluePrincipal);
    	
		return getContentVersionVOWithId(contentVersionId);
    } 

	/**
	 * This method returns a specific content-object after checking that it is accessable by the given user
	 */

	public ContentVersionVO getACLatestActiveContentVersionVO(InfoGluePrincipal infogluePrincipal, Integer contentId, Integer languageId) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("contentId", contentId);
	
		intercept(hashMap, "Content.Read", infogluePrincipal);

		ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, languageId);
		
		hashMap = new HashMap();
		hashMap.put("contentVersionId", contentVersionVO.getId());
	
		intercept(hashMap, "ContentVersion.Read", infogluePrincipal);

		return contentVersionVO;
	} 
	
	
	
	/**
	 * This method creates a contentVersion after first checking that the user has rights to edit it.
	 */

	public ContentVersionVO acCreate(InfoGluePrincipal infogluePrincipal, Integer contentId, Integer languageId, ContentVersionVO contentVersionVO) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("contentId", contentId);
    	
		intercept(hashMap, "Content.CreateVersion", infogluePrincipal);

		return ContentVersionController.getContentVersionController().create(contentId, languageId, contentVersionVO, null);
	}   

	/**
	 * This method updates a content after first checking that the user has rights to edit it.
	 */

	public ContentVersionVO acUpdate(InfoGluePrincipal infogluePrincipal, Integer contentId, Integer languageId, ContentVersionVO contentVersionVO) throws ConstraintException, SystemException, Bug, Exception
	{
		logger.info("contentId:" + contentId);
		logger.info("languageId:" + languageId);
		logger.info("contentVersionId:" + contentVersionVO.getId());
		
		if(contentVersionVO.getId() != null)
		{
			Map hashMap = new HashMap();
			hashMap.put("contentVersionId", contentVersionVO.getId());
			hashMap.put("contentVersionVO", contentVersionVO);
    	
			intercept(hashMap, "ContentVersion.Write", infogluePrincipal);
		}
		else
		{
			Map hashMap = new HashMap();
			hashMap.put("contentId", contentId);
			hashMap.put("contentVersionVO", contentVersionVO);
			
			intercept(hashMap, "Content.CreateVersion", infogluePrincipal);
		}
		
		return ContentVersionController.getContentVersionController().update(contentId, languageId, contentVersionVO, infogluePrincipal);
	}   
	
	/**
	 * This method deletes a content after first checking that the user has rights to edit it.
	 */

	public void acDelete(InfoGluePrincipal infogluePrincipal, ContentVersionVO contentVersionVO) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("contentVersionId", contentVersionVO.getId());
    	
		intercept(hashMap, "ContentVersion.Delete", infogluePrincipal);

		ContentVersionController.getContentVersionController().delete(contentVersionVO);
	}   
	
	
	/**
	 * This method returns true if the if the content in question is protected.
	 */

	public boolean getIsContentProtected(Integer contentId, boolean inherit)
	{
		boolean isContentProtected = false;
		
		logger.info("getIsContentProtected contentId:" + contentId);
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
					if(inherit)
					{
						ContentVO parentContentVO = ContentController.getParentContent(contentId);
						if(parentContentVO != null)
							isContentProtected = getIsContentProtected(parentContentVO.getId(), inherit); 
					}
				}
			}

		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the content was protected:" + e.getMessage(), e);
		}
		
		logger.info("isContentProtected:" + isContentProtected);
		
		return isContentProtected;
	}
 
}
