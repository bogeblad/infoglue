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

import java.util.HashMap;
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

import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.basic.BasicWorkflow;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

/**
 * This interceptor is used to handle integration of the external workflow engine OSWorkflow.
 *
 *  @author Mattias Bogeblad
 */

public class InfoGlueOSWorkflowInterceptor implements InfoGlueInterceptor
{
    private final static Logger logger = Logger.getLogger(InfoGlueOSWorkflowInterceptor.class.getName());

	/**
	 * This method will be called when a interceptionPoint is reached.
	 * 
	 * @param interceptionPoint
	 * @param extradata
	 * @throws ConstraintException
	 * @throws SystemException
	 */
	public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata) throws ConstraintException, SystemException, Exception
	{
		intercept(infoGluePrincipal, interceptionPointVO, extradata, true);
	}
	
	public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata, boolean allowCreatorAccess) throws ConstraintException, SystemException, Exception
	{
		logger.info("interceptionPointVO:" + interceptionPointVO.getName());
		
		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
		try
		{
			Workflow workflowInit = new BasicWorkflow(infoGluePrincipal.getName());
			
			long id = workflowInit.initialize("MattiasWF", 1, new HashMap());
			logger.info("Workflow initialized....");
			logger.info("id:" + id);
			logger.info("name:" + workflowInit.getWorkflowName(id));
			
			
			Workflow workflow = workflowInit; //new BasicWorkflow(infoGluePrincipal.getName());
			
			int[] actions = workflow.getAvailableActions(id, null);
			logger.info("actions:" + actions.length);
			WorkflowDescriptor wd = workflow.getWorkflowDescriptor(workflow.getWorkflowName(id));

			for (int i = 0; i < actions.length; i++) 
			{
				int availableActionId = actions[i];
				String name = wd.getAction(availableActionId).getName();
				logger.info("Action:" + availableActionId + ":" + name);
				
				//workflow.doAction(id, availableActionId, Collections.EMPTY_MAP);
			}
 
			Map map = new HashMap();
			map.put("userName", "Mattias");
			workflow.doAction(id, 1, map);

			actions = workflow.getAvailableActions(id, null);
			logger.info("actions:" + actions.length);
			wd = workflow.getWorkflowDescriptor(workflow.getWorkflowName(id));

			for (int i = 0; i < actions.length; i++) 
			{
				int availableActionId = actions[i];
				String name = wd.getAction(availableActionId).getName();
				logger.info("Action:" + availableActionId + ":" + name);
				
				//workflow.doAction(id, availableActionId, Collections.EMPTY_MAP);
			}

			//workflow.doAction(id, 2, Collections.EMPTY_MAP);
			//workflow.doAction(id, 3, Collections.EMPTY_MAP);
			//workflow.doAction(id, 1, Collections.EMPTY_MAP);	
			//workflow.doAction(id, 2, Collections.EMPTY_MAP);	
		
			/*
			WorkflowQuery queryLeft = new WorkflowQuery(WorkflowQuery.OWNER, WorkflowQuery.CURRENT, WorkflowQuery.EQUALS, infoGluePrincipal.getName());
			WorkflowQuery queryRight = new WorkflowQuery(WorkflowQuery.STATUS, WorkflowQuery.CURRENT, WorkflowQuery.EQUALS, "Underway");
			WorkflowQuery query = new WorkflowQuery(queryLeft, WorkflowQuery.AND, queryRight);
			List workflows = workflow.query(query);
			for (Iterator iterator = workflows.iterator(); iterator.hasNext();) {
				Long wfId = (Long) iterator.next();
				logger.info(wfId);
				} 
			*/		
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
				
		if(interceptionPointVO.getName().equalsIgnoreCase("Content.Read"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Write"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Create"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Delete"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.Move"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.CreateVersion"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.SubmitToPublish"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("Content.ChangeAccessRights"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			ContentVO contentVO = ContentControllerProxy.getController().getContentVOWithId(contentId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("ContentVersion.Read"))
		{
			Integer contentVersionId = (Integer)extradata.get("contentVersionId");
			ContentVersionVO contentVersionVO = ContentVersionControllerProxy.getController().getContentVersionVOWithId(contentVersionId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("ContentVersion.Write"))
		{
			Integer contentVersionId = (Integer)extradata.get("contentVersionId");
			ContentVersionVO contentVersionVO = ContentVersionControllerProxy.getController().getContentVersionVOWithId(contentVersionId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("ContentVersion.Delete"))
		{
			Integer contentVersionId = (Integer)extradata.get("contentVersionId");
			ContentVersionVO contentVersionVO = ContentVersionControllerProxy.getController().getContentVersionVOWithId(contentVersionId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.Read"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.Write"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.CreateSiteNode"))
		{
			Integer parentSiteNodeId = (Integer)extradata.get("siteNodeId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(parentSiteNodeId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.DeleteSiteNode"))
		{
			Integer siteNodeId = (Integer)extradata.get("siteNodeId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(siteNodeId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.MoveSiteNode"))
		{
			Integer siteNodeId = (Integer)extradata.get("siteNodeId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getLatestSiteNodeVersionVO(siteNodeId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.SubmitToPublish"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);

		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.ChangeAccessRights"))
		{
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			SiteNodeVersionVO siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);

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
	
	public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata, boolean allowCreatorAccess, Database db) throws ConstraintException, SystemException, Exception
	{
		logger.info("interceptionPointVO:" + interceptionPointVO.getName());
		
		AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
		
		/*
		if(interceptionPointVO.getName().equalsIgnoreCase("Content.Read"))
		{
			Integer contentId = (Integer)extradata.get("contentId");
			if(ContentControllerProxy.getController().getIsContentProtected(contentId) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Read", contentId.toString()))
				ceb.add(new AccessConstraintException("Content.contentId", "1000"));
		}
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
			if(!contentVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{	
				if(ContentVersionControllerProxy.getController().getIsContentProtected(contentVersionVO.getContentId()) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "ContentVersion.Read", contentVersionId.toString()))
					ceb.add(new AccessConstraintException("ContentVersion.contentVersionId", "1000"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("ContentVersion.Write"))
		{
			Integer contentVersionId = (Integer)extradata.get("contentVersionId");
			ContentVersionVO contentVersionVO = ContentVersionControllerProxy.getController().getContentVersionVOWithId(contentVersionId);
			if(!contentVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{	
				if(ContentVersionControllerProxy.getController().getIsContentProtected(contentVersionVO.getContentId()) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "ContentVersion.Write", contentVersionId.toString()))
					ceb.add(new AccessConstraintException("ContentVersion.contentVersionId", "1001"));
			}
		}
		else if(interceptionPointVO.getName().equalsIgnoreCase("ContentVersion.Delete"))
		{
			Integer contentVersionId = (Integer)extradata.get("contentVersionId");
			ContentVersionVO contentVersionVO = ContentVersionControllerProxy.getController().getContentVersionVOWithId(contentVersionId);
			if(!contentVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
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
			if(!siteNodeVersionVO.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				if(SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getIsSiteNodeVersionProtected(siteNodeVersionId) && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "SiteNodeVersion.Read", siteNodeVersionId.toString()))
					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeVersionId", "1000"));
			}
		}
		else*/ if(interceptionPointVO.getName().equalsIgnoreCase("SiteNodeVersion.Write"))
		{
			logger.info("******************************************************");
			logger.info("SiteNodeVersion.ChangeAccessRights");
			Integer siteNodeVersionId = (Integer)extradata.get("siteNodeVersionId");
			SiteNodeVersion siteNodeVersion = SiteNodeVersionController.getController().getSiteNodeVersionWithId(siteNodeVersionId, db);
			logger.info("VersionModifier:" + siteNodeVersion.getVersionModifier());
			logger.info("infoGluePrincipal:" + infoGluePrincipal.getName());
			if(!siteNodeVersion.getVersionModifier().equalsIgnoreCase(infoGluePrincipal.getName()))
			{
				Integer protectedSiteNodeVersionId = SiteNodeVersionControllerProxy.getSiteNodeVersionControllerProxy().getProtectedSiteNodeVersionId(siteNodeVersionId, db);
				if(protectedSiteNodeVersionId != null && !AccessRightController.getController().getIsPrincipalAuthorized(db, infoGluePrincipal, "SiteNodeVersion.Write", siteNodeVersionId.toString()))
					ceb.add(new AccessConstraintException("SiteNodeVersion.siteNodeVersionId", "1001"));
			}
		}

		ceb.throwIfNotEmpty();
	}
}
