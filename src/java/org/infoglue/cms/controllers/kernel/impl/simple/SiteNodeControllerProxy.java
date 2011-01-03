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

import org.exolab.castor.jdo.Database;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;


/**
 * @author Mattias Bogeblad
 */

public class SiteNodeControllerProxy extends SiteNodeController 
{
	protected static final Integer NO 			= new Integer(0);
	protected static final Integer YES 			= new Integer(1);
	protected static final Integer INHERITED 	= new Integer(2);

	private static List interceptors = new ArrayList();

	public static SiteNodeControllerProxy getSiteNodeControllerProxy()
	{
		return new SiteNodeControllerProxy();
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
	 * This method creates a siteNode after first checking that the user has rights to create it.
	 */

	public SiteNodeVO acCreate(InfoGluePrincipal infogluePrincipal, Integer parentSiteNodeId, Integer siteNodeTypeDefinitionId, Integer repositoryId, SiteNodeVO siteNodeVO) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("siteNodeId", parentSiteNodeId);
    	
		intercept(hashMap, "SiteNodeVersion.CreateSiteNode", infogluePrincipal);

		return SiteNodeController.getController().create(parentSiteNodeId, siteNodeTypeDefinitionId, infogluePrincipal, repositoryId, siteNodeVO);
	}   

	/**
	 * This method creates a siteNode after first checking that the user has rights to create it.
	 */

	public SiteNode acCreate(InfoGluePrincipal infogluePrincipal, Integer parentSiteNodeId, Integer siteNodeTypeDefinitionId, Integer repositoryId, SiteNodeVO siteNodeVO, Database db) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("siteNodeId", parentSiteNodeId);
    	
		intercept(hashMap, "SiteNodeVersion.CreateSiteNode", infogluePrincipal, db);

		return SiteNodeController.getController().create(db, parentSiteNodeId, siteNodeTypeDefinitionId, infogluePrincipal, repositoryId, siteNodeVO);
	}   
    
	/**
	 * This method updates a content after first checking that the user has rights to edit it.
	 */
	/*
	public SiteNodeVersionVO acUpdate(InfoGluePrincipal infogluePrincipal, SiteNodeVersionVO siteNodeVersionVO) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("siteNodeVersionId", siteNodeVersionVO.getId());
    	
		intercept(hashMap, "SiteNodeVersion.Write", infogluePrincipal);

		return update(siteNodeVersionVO);
	}   
	*/
	
	/**
	 * This method updates a content after first checking that the user has rights to edit it.
	 */
	/*
	public SiteNodeVersionVO acUpdate(InfoGluePrincipal infogluePrincipal, SiteNodeVersionVO siteNodeVersionVO, Database db) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("siteNodeVersionId", siteNodeVersionVO.getId());
    	
		intercept(hashMap, "SiteNodeVersion.Write", infogluePrincipal, db);

		return update(siteNodeVersionVO, db);
	}   
	*/
	/**
	 * This method deletes a sitenode after first checking that the user has rights to delete it.
	 */
	
	public void acDelete(InfoGluePrincipal infogluePrincipal, SiteNodeVO siteNodeVO) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("siteNodeId", siteNodeVO.getId());
    	
		intercept(hashMap, "SiteNodeVersion.DeleteSiteNode", infogluePrincipal);

		delete(siteNodeVO, infogluePrincipal);
	}   

	/**
	 * This method deletes a sitenode after first checking that the user has rights to delete it.
	 */
	
	public void acMarkForDelete(InfoGluePrincipal infogluePrincipal, SiteNodeVO siteNodeVO) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("siteNodeId", siteNodeVO.getId());
    	
		intercept(hashMap, "SiteNodeVersion.DeleteSiteNode", infogluePrincipal);

		markForDeletion(siteNodeVO, infogluePrincipal);
	}   

	
	/**
	 * This method moves a content after first checking that the user has rights to edit it.
	 */

	public void acMoveSiteNode(InfoGluePrincipal infogluePrincipal, SiteNodeVO siteNodeVO, Integer newParentSiteNodeId) throws ConstraintException, SystemException, Bug, Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("siteNodeId", siteNodeVO.getId());
    	
		intercept(hashMap, "SiteNodeVersion.MoveSiteNode", infogluePrincipal);
		
		hashMap = new HashMap();
		hashMap.put("siteNodeId", newParentSiteNodeId);

		intercept(hashMap, "SiteNodeVersion.CreateSiteNode", infogluePrincipal);

		moveSiteNode(siteNodeVO, newParentSiteNodeId, infogluePrincipal);
	}

	public void acChangeSiteNodeSortOrder(InfoGluePrincipal infoGluePrincipal, Integer siteNodeId, Integer beforeSiteNodeId, String direction) throws Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("siteNodeId", siteNodeId);
    	
		intercept(hashMap, "SiteNodeVersion.MoveSiteNode", infoGluePrincipal);
		
		changeSiteNodeSortOrder(siteNodeId, beforeSiteNodeId, direction, infoGluePrincipal);
	}   
	
	public void acToggleHidden(InfoGluePrincipal infoGluePrincipal, Integer siteNodeId) throws Exception
	{
		Map hashMap = new HashMap();
		hashMap.put("siteNodeId", siteNodeId);
    	
		intercept(hashMap, "SiteNodeVersion.MoveSiteNode", infoGluePrincipal);
		
		toggleSiteNodeHidden(siteNodeId, infoGluePrincipal);
	}   

}
