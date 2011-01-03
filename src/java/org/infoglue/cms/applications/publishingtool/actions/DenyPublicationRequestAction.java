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

package org.infoglue.cms.applications.publishingtool.actions;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.mydesktoptool.actions.ViewMyDesktopToolToolBarAction;
import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.SystemException;

/**
 * @author Mattias Bogeblad
 *
 * This action denies a requested publishing of an entity or whatever is in the que. 
 * It deletes the old event and creates a new one with a reply to the requester.
 */

public class DenyPublicationRequestAction extends InfoGlueAbstractAction 
{
    private final static Logger logger = Logger.getLogger(DenyPublicationRequestAction.class.getName());

	private Integer eventId;
	private Integer repositoryId;
	private List events;
	private String comment;
	
	public Integer getEventId() 
	{
		return eventId;
	}
	
	public void setEventId(Integer eventId) 
	{
		this.eventId = eventId;
	}
		
	public Integer getRepositoryId() 
	{
		return repositoryId;
	}

	public void setRepositoryId(Integer repositoryId) 
	{
		this.repositoryId = repositoryId;
	}
	
	public String[] getSelList()
	{
		return getRequest().getParameterValues("sel");
	}
	
	protected String doExecute() throws Exception 
	{
		setEvents(getRequest().getParameterValues("sel"));
		
		PublicationController.denyPublicationRequest(this.events, this.getInfoGluePrincipal(), this.comment, getApplicationBaseUrl(getRequest()));
		return "success";
	}

	public String doV3() throws Exception 
	{
		setEvents(getRequest().getParameterValues("sel"));
		
		PublicationController.denyPublicationRequest(this.events, this.getInfoGluePrincipal(), this.comment, getApplicationBaseUrl(getRequest()));
		return "successV3";
	}

	public String doComment() throws Exception 
	{		
		return "comment";
	}

	public String doCommentV3() throws Exception 
	{		
		return "commentV3";
	}

	private String getApplicationBaseUrl(HttpServletRequest request)
	{
		return request.getRequestURL().toString().substring(0, request.getRequestURL().lastIndexOf("/") + 1) + "ViewCMSTool.action";
	}
	
	private void setEvents(String[] eventArguments) throws SystemException, Exception
	{
		List events = new ArrayList();
	
		for(int i=0; i < eventArguments.length; i++)
		{
			logger.info("EventId:" + eventArguments[i]);
			EventVO eventVO = EventController.getEventVOWithId(new Integer(eventArguments[i]));
			events.add(eventVO);
		}		
	
		this.events = events;
	}

	public String getComment() 
	{
		return comment;
	}

	public void setComment(String comment) 
	{
		this.comment = comment;
	}

}
