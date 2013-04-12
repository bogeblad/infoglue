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
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.entities.management.AccessRight;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.util.Timer;


/**
 * @author Mattias Bogeblad
 */

public class SiteNodeVersionControllerProxy extends SiteNodeVersionController 
{
    private final static Logger logger = Logger.getLogger(SiteNodeVersionControllerProxy.class.getName());

	protected static final Integer NO 			= new Integer(0);
	protected static final Integer YES 			= new Integer(1);
	protected static final Integer INHERITED 	= new Integer(2);

	private static List interceptors = new ArrayList();

	public static SiteNodeVersionControllerProxy getSiteNodeVersionControllerProxy()
	{
		return new SiteNodeVersionControllerProxy();
	}
	
	
	private List getInterceptors(Integer interceptorPointId) throws SystemException, Bug
	{
		//if(interceptors == null)
			interceptors = InterceptionPointController.getController().getInterceptorsVOList(interceptorPointId);
		
		return interceptors;
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
	/*
	private void intercept(Map hashMap, String InterceptionPointName, InfoGluePrincipal infogluePrincipal, Database db) throws ConstraintException, SystemException, Bug, Exception
	{
		InterceptionPoint interceptionPoint = InterceptionPointController.getController().getInterceptionPointWithName(InterceptionPointName, db);
    	
		if(interceptionPoint == null)
			throw new SystemException("The InterceptionPoint " + InterceptionPointName + " was not found. The system will not work unless you restore it.");

		List interceptors = InterceptionPointController.getController().getInterceptorsVOList(interceptionPoint.getInterceptionPointId(), db);
		Iterator interceptorsIterator = interceptors.iterator();
		while(interceptorsIterator.hasNext())
		{
			InterceptorVO interceptorVO = (InterceptorVO)interceptorsIterator.next();
			logger.info("Adding interceptorVO:" + interceptorVO.getName());
			try
			{
				InfoGlueInterceptor infoGlueInterceptor = (InfoGlueInterceptor)Class.forName(interceptorVO.getClassName()).newInstance();
				infoGlueInterceptor.intercept(infogluePrincipal, interceptionPoint.getValueObject(), hashMap, db);
			}
			catch(ClassNotFoundException e)
			{
				logger.warn("The interceptor " + interceptorVO.getClassName() + "was not found: " + e.getMessage(), e);
			}
		}
	}
	*/
	
	/**
	 * This method returns a specific siteNodeVersion-object
	 */
	
    public SiteNodeVersionVO getACLatestActiveSiteNodeVersionVO(InfoGluePrincipal infogluePrincipal, Integer siteNodeId, Database db) throws ConstraintException, SystemException, Bug, Exception
    {
		SiteNodeVersionVO siteNodeVersionVO = getLatestActiveSiteNodeVersionVO(db, siteNodeId);
		
		if(siteNodeVersionVO != null)
		{
			Map hashMap = new HashMap();
			hashMap.put("siteNodeVersionId", siteNodeVersionVO.getId());
	
			intercept(hashMap, "SiteNodeVersion.Read", infogluePrincipal, db);
		}

		return getLatestActiveSiteNodeVersionVO(db, siteNodeId);
    } 

    
	/**
	 * This method returns a specific siteNodeVersion-object
	 */
	
    public SiteNodeVersionVO getACLatestActiveSiteNodeVersionVO(InfoGluePrincipal infogluePrincipal, Integer siteNodeId) throws ConstraintException, SystemException, Bug, Exception
    {
		SiteNodeVersionVO siteNodeVersionVO = getLatestActiveSiteNodeVersionVO(siteNodeId);
		
		Map hashMap = new HashMap();
		hashMap.put("siteNodeVersionId", siteNodeVersionVO.getId());

		intercept(hashMap, "SiteNodeVersion.Read", infogluePrincipal);
    	
		return getLatestActiveSiteNodeVersionVO(siteNodeId);
    } 

    
	/**
	 * This method creates a content after first checking that the user has rights to edit it.
	 */
/*
	public ContentVO acCreate(InfoGluePrincipal infogluePrincipal, Integer parentContentId, Integer contentTypeDefinitionId, Integer repositoryId, ContentVO contentVO) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("contentId", parentContentId);
    	
		intercept(hashMap, "Content.Create", infogluePrincipal);

		return ContentController.getContentController().create(parentContentId, contentTypeDefinitionId, repositoryId, contentVO);
	}   
*/
    
	/**
	 * This method updates a content after first checking that the user has rights to edit it.
	 */

	public SiteNodeVersionVO acUpdate(InfoGluePrincipal infogluePrincipal, SiteNodeVersionVO siteNodeVersionVO) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("siteNodeVersionId", siteNodeVersionVO.getId());
    	
		intercept(hashMap, "SiteNodeVersion.Write", infogluePrincipal);

		return update(siteNodeVersionVO);
	}   
	
	/**
	 * This method updates a content after first checking that the user has rights to edit it.
	 */

	public SiteNodeVersionVO acUpdate(InfoGluePrincipal infogluePrincipal, SiteNodeVersionVO siteNodeVersionVO, Database db) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("siteNodeVersionId", siteNodeVersionVO.getId());
    	
		intercept(hashMap, "SiteNodeVersion.Write", infogluePrincipal, db);
		
		return update(siteNodeVersionVO, db);
	}   
	
	/**
	 * This method deletes a content after first checking that the user has rights to edit it.
	 */
	/*
	public void acDelete(InfoGluePrincipal infogluePrincipal, ContentVO contentVO) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("contentId", contentVO.getId());
    	
		intercept(hashMap, "Content.Delete", infogluePrincipal);

		delete(contentVO);
	}   
	*/
	
	/**
	 * This method moves a content after first checking that the user has rights to edit it.
	 */
	/*
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
	*/

	/**
	 * This method returns true if the if the siteNode in question is protected.
	 */

	public Integer getProtectedSiteNodeVersionId(Integer siteNodeVersionId)
	{
		logger.info("siteNodeVersionId:" + siteNodeVersionId);
		Integer protectedSiteNodeVersionId = null;
		
		try
		{
			SiteNodeVersionVO siteNodeVersionVO = getSiteNodeVersionVOWithId(siteNodeVersionId);
			logger.info("Is Protected: " + siteNodeVersionVO.getIsProtected());
			if(siteNodeVersionVO != null)
			{	
				if(siteNodeVersionVO.getIsProtected() != null)
				{	
					if(siteNodeVersionVO.getIsProtected().intValue() == NO.intValue())
						protectedSiteNodeVersionId = null;
					else if(siteNodeVersionVO.getIsProtected().intValue() == YES.intValue())
						protectedSiteNodeVersionId = siteNodeVersionVO.getId();
					else if(siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK.intValue())
						protectedSiteNodeVersionId = siteNodeVersionVO.getId();
					else if(siteNodeVersionVO.getIsProtected().intValue() == INHERITED.intValue())
					{
						SiteNodeVO parentSiteNodeVO = SiteNodeController.getController().getParentSiteNode(siteNodeVersionVO.getSiteNodeId());
						if(parentSiteNodeVO != null)
						{
							siteNodeVersionVO = getLatestSiteNodeVersionVO(parentSiteNodeVO.getSiteNodeId());
							protectedSiteNodeVersionId = getProtectedSiteNodeVersionId(siteNodeVersionVO.getSiteNodeVersionId());
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion is protected:" + e.getMessage(), e);
		}
			
		return protectedSiteNodeVersionId;
	}
	
	/**
	 * This method returns true if the if the siteNode in question is protected.
	 */

	public Integer getProtectedSiteNodeVersionId(Integer siteNodeVersionId, Integer interceptionPointId)
	{
		logger.info("siteNodeVersionId:" + siteNodeVersionId);
		Integer protectedSiteNodeVersionId = null;
		
		try
		{
			SiteNodeVersionVO siteNodeVersionVO = getSiteNodeVersionVOWithId(siteNodeVersionId);
			logger.info("Is Protected: " + siteNodeVersionVO.getIsProtected());
			if(siteNodeVersionVO != null)
			{	
				if(siteNodeVersionVO.getIsProtected() != null)
				{	
					if(siteNodeVersionVO.getIsProtected().intValue() == NO.intValue())
						protectedSiteNodeVersionId = null;
					else if(siteNodeVersionVO.getIsProtected().intValue() == YES.intValue())
						protectedSiteNodeVersionId = siteNodeVersionVO.getId();
					else if(siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK.intValue())
						protectedSiteNodeVersionId = siteNodeVersionVO.getId();
					else if(siteNodeVersionVO.getIsProtected().intValue() == INHERITED.intValue())
					{
						SiteNodeVO parentSiteNodeVO = SiteNodeController.getController().getParentSiteNode(siteNodeVersionVO.getSiteNodeId());
						if(parentSiteNodeVO != null)
						{
							siteNodeVersionVO = getLatestSiteNodeVersionVO(parentSiteNodeVO.getSiteNodeId());
							protectedSiteNodeVersionId = getProtectedSiteNodeVersionId(siteNodeVersionVO.getSiteNodeVersionId());
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion is protected:" + e.getMessage(), e);
		}
			
		return protectedSiteNodeVersionId;
	}
	

	/**
	 * This method returns true if the if the siteNode in question is protected within a transaction.
	 */

	public Integer getProtectedSiteNodeVersionId(Integer siteNodeVersionId, Database db)
	{
		logger.info("siteNodeVersionId:" + siteNodeVersionId);
		Integer protectedSiteNodeVersionId = null;
		
		try
		{
			SiteNodeVersionVO siteNodeVersion = getSiteNodeVersionVOWithId(siteNodeVersionId, db);
			logger.info("Is Protected: " + siteNodeVersion.getIsProtected());
			if(siteNodeVersion != null)
			{	
				if(siteNodeVersion.getIsProtected() != null)
				{	
					if(siteNodeVersion.getIsProtected().intValue() == NO.intValue())
						protectedSiteNodeVersionId = null;
					else if(siteNodeVersion.getIsProtected().intValue() == YES.intValue())
						protectedSiteNodeVersionId = siteNodeVersion.getId();
					else if(siteNodeVersion.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK.intValue())
						protectedSiteNodeVersionId = siteNodeVersion.getId();
					else if(siteNodeVersion.getIsProtected().intValue() == INHERITED.intValue())
					{
						SiteNodeVO parentSiteNode = SiteNodeController.getController().getParentSiteNodeVO(siteNodeVersion.getSiteNodeId(), db);
						//SiteNode parentSiteNode = siteNodeVersion.getOwningSiteNode().getParentSiteNode();
						if(parentSiteNode != null)
						{
							siteNodeVersion = getLatestActiveSiteNodeVersionVO(db, parentSiteNode.getSiteNodeId());
							//siteNodeVersion = getLatestSiteNodeVersionVO(db, parentSiteNode.getSiteNodeId());
							protectedSiteNodeVersionId = getProtectedSiteNodeVersionId(siteNodeVersion.getSiteNodeVersionId(), db);
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion is protected:" + e.getMessage(), e);
		}
			
		return protectedSiteNodeVersionId;
	}


	/**
	 * This method returns true if the if the siteNode in question is protected within a transaction.
	 */

	public Integer getProtectedSiteNodeVersionId(Integer siteNodeVersionId, Integer interceptionPointId, Database db)
	{
		return getProtectedSiteNodeVersionId(siteNodeVersionId, interceptionPointId, true, db);
	}
	
	/**
	 * This method returns true if the if the siteNode in question is protected within a transaction.
	 */

	public Integer getProtectedSiteNodeVersionId(Integer siteNodeVersionId, Integer interceptionPointId, Boolean honourInheritanceFallback, Database db)
	{
		logger.info("siteNodeVersionId:" + siteNodeVersionId);
		Integer protectedSiteNodeVersionId = null;
		
		try
		{
			SiteNodeVersionVO siteNodeVersion = getSiteNodeVersionVOWithId(siteNodeVersionId, db);
			if(siteNodeVersion != null)
			{	
				if(siteNodeVersion.getIsProtected() != null)
				{	
					if(siteNodeVersion.getIsProtected().intValue() == NO.intValue())
						protectedSiteNodeVersionId = null;
					else if(siteNodeVersion.getIsProtected().intValue() == YES.intValue())
						protectedSiteNodeVersionId = siteNodeVersion.getId();
					else if(siteNodeVersion.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK.intValue())
					{
						logger.info(honourInheritanceFallback);
						if(honourInheritanceFallback)
						{
							List<AccessRight> accessRights = AccessRightController.getController().getAccessRightListOnlyReadOnly(interceptionPointId, siteNodeVersion.getId().toString(), db);
							if(accessRights == null || accessRights.size() == 0)
							{
								SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(siteNodeVersion.getSiteNodeId(), db);
								if(siteNodeVO != null && siteNodeVO.getParentSiteNodeId() != null)
								{
									SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(db, siteNodeVO.getParentSiteNodeId());
									protectedSiteNodeVersionId = getProtectedSiteNodeVersionId(siteNodeVersionVO.getSiteNodeVersionId(), interceptionPointId, honourInheritanceFallback, db);		
								}
							}
							else
								protectedSiteNodeVersionId = siteNodeVersion.getId();
						}
						else
							protectedSiteNodeVersionId = siteNodeVersion.getId();
					}
					else if(siteNodeVersion.getIsProtected().intValue() == INHERITED.intValue())
					{
						SiteNodeVO parentSiteNode = SiteNodeController.getController().getParentSiteNodeVO(siteNodeVersion.getSiteNodeId(), db);
						//SiteNode parentSiteNode = siteNodeVersion.getOwningSiteNode().getParentSiteNode();
						if(parentSiteNode != null)
						{
							siteNodeVersion = getLatestSiteNodeVersionVO(db, parentSiteNode.getSiteNodeId());
							protectedSiteNodeVersionId = getProtectedSiteNodeVersionId(siteNodeVersion.getSiteNodeVersionId(), db);
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion is protected:" + e.getMessage(), e);
		}

		return protectedSiteNodeVersionId;
	}
	/**
	 * This method returns true if the if the siteNode in question is protected.
	 */

	public boolean getIsSiteNodeVersionProtected(Integer siteNodeVersionId)
	{
		logger.info("siteNodeVersionId:" + siteNodeVersionId);
		boolean isSiteNodeVersionProtected = false;
	
		try
		{
			SiteNodeVersionVO siteNodeVersionVO = getSiteNodeVersionVOWithId(siteNodeVersionId);
			logger.info("Is Protected: " + siteNodeVersionVO.getIsProtected());
			if(siteNodeVersionVO != null)
			{	
				if(siteNodeVersionVO.getIsProtected() != null)
				{	
					if(siteNodeVersionVO.getIsProtected().intValue() == NO.intValue())
						isSiteNodeVersionProtected = false;
					else if(siteNodeVersionVO.getIsProtected().intValue() == YES.intValue())
						isSiteNodeVersionProtected = true;
					else if(siteNodeVersionVO.getIsProtected().intValue() == SiteNodeVersionVO.YES_WITH_INHERIT_FALLBACK.intValue())
						isSiteNodeVersionProtected = true;
					else if(siteNodeVersionVO.getIsProtected().intValue() == INHERITED.intValue())
					{
						SiteNodeVO parentSiteNodeVO = SiteNodeController.getController().getParentSiteNode(siteNodeVersionVO.getSiteNodeId());
						if(parentSiteNodeVO != null)
						{
							siteNodeVersionVO = getLatestSiteNodeVersionVO(parentSiteNodeVO.getSiteNodeId());
							isSiteNodeVersionProtected = getIsSiteNodeVersionProtected(siteNodeVersionVO.getSiteNodeVersionId());
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion is protected:" + e.getMessage(), e);
		}
			
		return isSiteNodeVersionProtected;
	}

	/**
	 * This method returns true if the if the siteNode in question is protected.
	 */

	public boolean getIsSiteNodeVersionProtected(Integer siteNodeVersionId, Database db)
	{
		logger.info("siteNodeVersionId:" + siteNodeVersionId);
		boolean isSiteNodeVersionProtected = false;
	
		try
		{
			SiteNodeVersion siteNodeVersion = getSiteNodeVersionWithId(siteNodeVersionId, db);
			logger.info("Is Protected: " + siteNodeVersion.getIsProtected());
			if(siteNodeVersion != null)
			{	
				if(siteNodeVersion.getIsProtected() != null)
				{	
					if(siteNodeVersion.getIsProtected().intValue() == NO.intValue())
						isSiteNodeVersionProtected = false;
					else if(siteNodeVersion.getIsProtected().intValue() == YES.intValue())
						isSiteNodeVersionProtected = true;
					else if(siteNodeVersion.getIsProtected().intValue() == INHERITED.intValue())
					{
						SiteNodeVO parentSiteNode = SiteNodeController.getController().getParentSiteNodeVO(siteNodeVersion.getValueObject().getSiteNodeId(), db);
						if(parentSiteNode != null)
						{
							siteNodeVersion = getLatestSiteNodeVersion(db, parentSiteNode.getSiteNodeId(), false);
							isSiteNodeVersionProtected = getIsSiteNodeVersionProtected(siteNodeVersion.getSiteNodeVersionId(), db);
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion is protected:" + e.getMessage(), e);
		}
			
		return isSiteNodeVersionProtected;
	}


}
