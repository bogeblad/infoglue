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

package org.infoglue.cms.security.interceptors;

import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.structure.SiteNodeVersion;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.AccessConstraintExceptionBuffer;


/**
 * @author Mattias Bogeblad
 *
 * This interceptor is used to handle all built in access control InfoGlue offers. Additional interceptors can be 
 * registered by users of course.
 */

public class InfoGlueCommonAccessRightsInterceptor implements InfoGlueInterceptor
{
    private final static Logger logger = Logger.getLogger(InfoGlueCommonAccessRightsInterceptor.class.getName());

	public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata) throws ConstraintException, SystemException, Exception
	{
		intercept(infoGluePrincipal, interceptionPointVO, extradata, true);
	}
	
	/**
	 * This method will be called when a interceptionPoint is reached.
	 * 
	 * @param interceptionPoint
	 * @param extradata
	 * @throws ConstraintException
	 * @throws SystemException
	 */

	public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata, boolean allowCreatorAccess) throws ConstraintException, SystemException, Exception
	{
		logger.info("interceptionPointVO:" + interceptionPointVO.getName());
		
		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
		if(interceptionPointVO.getName().equalsIgnoreCase("Content.Read"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
			if(!allowCreatorAccess || !contentVO.getCreatorName().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
				if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Read", protectedContentId.toString()))
					ceb.add(new AccessConstraintException("Content.contentId", "1000"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Component.Select"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
			if(!allowCreatorAccess || !contentVO.getCreatorName().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedContentId = contentId; //ContentControllerProxy.getController().getProtectedContentId(contentId);
				if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Component.Select", protectedContentId.toString()))
					ceb.add(new AccessConstraintException("Content.contentId", "1000"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Write"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
			if(!allowCreatorAccess || !contentVO.getCreatorName().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
				if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Write", protectedContentId.toString()))
					ceb.add(new AccessConstraintException("Content.contentId", "1001"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Create"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
			if(!allowCreatorAccess || !contentVO.getCreatorName().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
				if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Create", protectedContentId.toString()))
					ceb.add(new AccessConstraintException("Content.contentId", "1002"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Delete"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
			if(!allowCreatorAccess || !contentVO.getCreatorName().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
				if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Delete", protectedContentId.toString()))
					ceb.add(new AccessConstraintException("Content.contentId", "1003"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Move"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
			if(!allowCreatorAccess || !contentVO.getCreatorName().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
				if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Move", protectedContentId.toString()))
					ceb.add(new AccessConstraintException("Content.contentId", "1004"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.CreateVersion"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
			if(!allowCreatorAccess || !contentVO.getCreatorName().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
				if(ContentVersionControllerProxy.getController().getIsContentProtected(contentId, true) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.CreateVersion", protectedContentId.toString()))
					ceb.add(new AccessConstraintException("Content.contentId", "1002"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.SubmitToPublish"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
			if(!allowCreatorAccess || !contentVO.getCreatorName().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
				if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.SubmitToPublish", protectedContentId.toString()))
					ceb.add(new AccessConstraintException("Content.contentId", "1005"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.ChangeAccessRights"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);
			if(!allowCreatorAccess || !contentVO.getCreatorName().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
				if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.ChangeAccessRights", protectedContentId.toString()))
					ceb.add(new AccessConstraintException("Content.contentId", "1006"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("ContentVersion.Read"))
		{
			Integer contentVersionId = (Integer)extradata.get("contentVersionId");
			ContentVersionVO contentVersionVO = ContentVersionControllerProxy.getController().getContentVersionVOWithId(contentVersionId);
			if(!allowCreatorAccess || !contentVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{	
				if(ContentVersionControllerProxy.getController().getIsContentProtected(contentVersionVO.getContentId(), false) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "ContentVersion.Read", contentVersionId.toString()))
				{
					ceb.add(new AccessConstraintException("ContentVersion.contentVersionId", "1000"));
				}
				else
				{
					Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentVersionVO.getContentId());
					if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Read", protectedContentId.toString()))
						ceb.add(new AccessConstraintException("Content.contentId", "1000"));
				}
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("ContentVersion.Write"))
		{
			Integer contentVersionId = (Integer)extradata.get("contentVersionId");
			ContentVersionVO contentVersionVO = ContentVersionControllerProxy.getController().getContentVersionVOWithId(contentVersionId);
			if(!allowCreatorAccess || !contentVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{	
				if(ContentVersionControllerProxy.getController().getIsContentProtected(contentVersionVO.getContentId(), false) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "ContentVersion.Write", contentVersionId.toString()))
				{
					ceb.add(new AccessConstraintException("ContentVersion.contentVersionId", "1001"));
				}
				else
				{
					Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentVersionVO.getContentId());
					if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Write", protectedContentId.toString()))
						ceb.add(new AccessConstraintException("Content.contentId", "1001"));
				}
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("ContentVersion.Delete"))
		{
			Integer contentVersionId = (Integer)extradata.get("contentVersionId");
			ContentVersionVO contentVersionVO = ContentVersionControllerProxy.getController().getContentVersionVOWithId(contentVersionId);
			if(!allowCreatorAccess || !contentVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{	
				if(ContentVersionControllerProxy.getController().getIsContentProtected(contentVersionVO.getContentId(), false) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "ContentVersion.Delete", contentVersionId.toString()))
					ceb.add(new AccessConstraintException("ContentVersion.contentVersionId", "1003"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.Read"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
			if(!allowCreatorAccess || !siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId);
				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()))
					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeVersionId", "1000"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.Write"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
			if(!allowCreatorAccess || !siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId);
				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "SiteNodeVersion.Write", protectedSiteNodeVersionId.toString()))
					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeVersionId", "1001"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.CreateSiteNode"))
		{
			Integer parentSiteNodeId = (Integer)extradata.get("siteNodeId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(parentSiteNodeId);
			if(!allowCreatorAccess || !siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionVO.getId());
				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "SiteNodeVersion.CreateSiteNode", protectedSiteNodeVersionId.toString()))
					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1002"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.DeleteSiteNode"))
		{
			Integer siteNodeId = (Integer)extradata.get("siteNodeId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(siteNodeId);
			if(!allowCreatorAccess || !siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionVO.getId());
				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "SiteNodeVersion.DeleteSiteNode", protectedSiteNodeVersionId.toString()))
					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1003"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.MoveSiteNode"))
		{
			Integer siteNodeId = (Integer)extradata.get("siteNodeId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(siteNodeId);
			if(!allowCreatorAccess || !siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionVO.getId());
				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "SiteNodeVersion.MoveSiteNode", protectedSiteNodeVersionId.toString()))
					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1004"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.SubmitToPublish"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
			if(!allowCreatorAccess || !siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId);
				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "SiteNodeVersion.SubmitToPublish", protectedSiteNodeVersionId.toString()))
					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1005"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.ChangeAccessRights"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
			if(!allowCreatorAccess || !siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId);
				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "SiteNodeVersion.ChangeAccessRights", protectedSiteNodeVersionId.toString()))
					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1006"));
			}
		}
		
		ceb.throwIfNotEmpty();
	}

	
	/**
	 * This method will be called when a interceptionPoint is reached and it handle it within a transaction.
	 * 
	 * @param interceptionPoint
	 * @param extradata
	 * @throws ConstraintException
	 * @throws SystemException
	 */

	public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata, Database db) throws ConstraintException, SystemException, Exception
	{
		intercept(infoGluePrincipal, interceptionPointVO, extradata, true, db);
	}
	
	/**
	 * This method will be called when a interceptionPoint is reached and it handle it within a transaction.
	 * 
	 * @param interceptionPoint
	 * @param extradata
	 * @throws ConstraintException
	 * @throws SystemException
	 */

	public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata, boolean allowCreatorAccess, Database db) throws ConstraintException, SystemException, Exception
	{
		logger.info("interceptionPointVO:" + interceptionPointVO.getName());
		
		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
		if(interceptionPointVO.getName().equalsIgnoreCase("Content.Read"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId, db);
			if(!allowCreatorAccess || !contentVO.getCreatorName().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId, db);
				if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Content.Read", protectedContentId.toString()))
					ceb.add(new AccessConstraintException("Content.contentId", "1000"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Component.Select"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId, db);
			if(!allowCreatorAccess || !contentVO.getCreatorName().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedContentId = contentId; //ContentControllerProxy.getController().getProtectedContentId(contentId, db);
				if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "Component.Select", protectedContentId.toString()))
					ceb.add(new AccessConstraintException("Content.contentId", "1000"));
			}
		}
		
		/*
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Write"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			if(ContentControllerProxy.getController().getIsContentProtected(contentId) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Write", contentId.toString()))
				ceb.add(new AccessConstraintException("Content.contentId", "1001"));
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Create"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			if(ContentControllerProxy.getController().getIsContentProtected(contentId) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Create", contentId.toString()))
				ceb.add(new AccessConstraintException("Content.contentId", "1002"));
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Delete"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			if(ContentControllerProxy.getController().getIsContentProtected(contentId) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Delete", contentId.toString()))
				ceb.add(new AccessConstraintException("Content.contentId", "1003"));
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Move"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			if(ContentControllerProxy.getController().getIsContentProtected(contentId) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Move", contentId.toString()))
				ceb.add(new AccessConstraintException("Content.contentId", "1004"));
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.SubmitToPublish"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			if(ContentControllerProxy.getController().getIsContentProtected(contentId) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.SubmitToPublish", contentId.toString()))
				ceb.add(new AccessConstraintException("Content.contentId", "1005"));
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.ChangeAccessRights"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			if(ContentControllerProxy.getController().getIsContentProtected(contentId) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.ChangeAccessRights", contentId.toString()))
				ceb.add(new AccessConstraintException("Content.contentId", "1006"));
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("ContentVersion.Read"))
		{
			Integer contentVersionId = (Integer)extradata.get("contentVersionId");
			ContentVersionVO contentVersionVO = ContentVersionControllerProxy.getController().getContentVersionVOWithId(contentVersionId);
			if(!allowCreatorAccess || !contentVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{	
				if(ContentVersionControllerProxy.getController().getIsContentProtected(contentVersionVO.getContentId()) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "ContentVersion.Read", contentVersionId.toString()))
					ceb.add(new AccessConstraintException("ContentVersion.contentVersionId", "1000"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("ContentVersion.Write"))
		{
			Integer contentVersionId = (Integer)extradata.get("contentVersionId");
			ContentVersionVO contentVersionVO = ContentVersionControllerProxy.getController().getContentVersionVOWithId(contentVersionId);
			if(!allowCreatorAccess || !contentVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{	
				if(ContentVersionControllerProxy.getController().getIsContentProtected(contentVersionVO.getContentId()) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "ContentVersion.Write", contentVersionId.toString()))
					ceb.add(new AccessConstraintException("ContentVersion.contentVersionId", "1001"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("ContentVersion.Delete"))
		{
			Integer contentVersionId = (Integer)extradata.get("contentVersionId");
			ContentVersionVO contentVersionVO = ContentVersionControllerProxy.getController().getContentVersionVOWithId(contentVersionId);
			if(!allowCreatorAccess || !contentVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{	
				if(ContentVersionControllerProxy.getController().getIsContentProtected(contentVersionVO.getContentId()) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "ContentVersion.Delete", contentVersionId.toString()))
					ceb.add(new AccessConstraintException("ContentVersion.contentVersionId", "1003"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.CreateVersion"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			if(ContentVersionControllerProxy.getController().getIsContentProtected(contentId) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.CreateVersion", contentId.toString()))
				ceb.add(new AccessConstraintException("Content.contentId", "1002"));
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.Read"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
			if(!allowCreatorAccess || !siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				if(SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getIsSiteNodeVersionProtected(siteNodeVersionId) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "SiteNodeVersion.Read", siteNodeVersionId.toString()))
					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeVersionId", "1000"));
			}
		}
		else*/ if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.CreateSiteNode"))
		{
			Integer parentSiteNodeId = (Integer)extradata.get("siteNodeId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(db, parentSiteNodeId);
			if(!allowCreatorAccess || !siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionVO.getId(), db);
				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "SiteNodeVersion.CreateSiteNode", protectedSiteNodeVersionId.toString()))
					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeId", "1002"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.Read"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId, db);
			//SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionId, db);
			if(!allowCreatorAccess || !siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId, db);
				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString()))
					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeVersionId", "1000"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.Write"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId, db);
			//SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionId, db);
			if(!allowCreatorAccess || !siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId, db);
				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "SiteNodeVersion.Write", protectedSiteNodeVersionId.toString()))
					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeVersionId", "1001"));
			}
		}

		ceb.throwIfNotEmpty();
	}
}
