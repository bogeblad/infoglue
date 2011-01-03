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

import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.validators.ConstraintRule;

public class CreateEditionAction extends ViewPublicationsAction
{

	private PublicationVO publicationVO;
	private ConstraintExceptionBuffer ceb;
    private String name;
    private String description;
    private List events;

	public CreateEditionAction()
	{
		this(new PublicationVO());
	}
	
	public CreateEditionAction(PublicationVO publicationVO)
	{
		this.publicationVO = publicationVO;
		this.ceb = new ConstraintExceptionBuffer();
			
	}	

	public ConstraintRule getRule(String fieldName)
	{
		return publicationVO.getRule(fieldName);
	}
		    
    public java.lang.String getName()
    {
    	if(this.name != null)
    		return this.name;
    		
        return this.publicationVO.getName();
    }
        
    public void setName(java.lang.String name)
    {
    	this.publicationVO.setName(name);
    }
      

    public String getDescription()
    {
    	if(this.description != null)
    		return this.description;
    		
        return this.publicationVO.getDescription();
    }
        
    public void setDescription(String description)
    {
        	this.publicationVO.setDescription(description);
    }

	public List getEvents()
	{
		return this.events;
	}

    public String doExecute() throws Exception
    {
    	this.publicationVO.setRepositoryId(getRepositoryId());
    	
		ceb = this.publicationVO.validate();

		// Content versions to publish
    	setEvents(getRequest().getParameterValues("sel"));
    	
    	//String[] contents = getRequest().getParameterValues("sel");
    	//String[] siteNodes = getRequest().getParameterValues("sit");
    	//setContentToPublish(PublicationController.getContentVersionVOToPublish(contents));
    	//setSiteNodeToPublish(PublicationController.getSiteNodeVersionVOToPublish(siteNodes));

    	ceb.throwIfNotEmpty();		

    	this.publicationVO = PublicationController.getController().createAndPublish(this.publicationVO, events, false, this.getInfoGluePrincipal());
    	
        return "success";
    }

    public String doV3() throws Exception
    {
    	doExecute();
    	
        return "successV3";
    }
    
    public String doInput() throws Exception
    {
    	this.publicationVO.PrepareValidation();
    	setEvents(getRequest().getParameterValues("sel"));
    	return "input";
    }    

    public String doInputV3() throws Exception
    {
    	this.publicationVO.PrepareValidation();
    	setEvents(getRequest().getParameterValues("sel"));
    	return "inputV3";
    }    

	private void setEvents(String[] eventArguments) throws SystemException, Exception
	{
		List events = new ArrayList();
		
		for(int i=0; i < eventArguments.length; i++)
		{
			EventVO eventVO = EventController.getEventVOWithId(new Integer(eventArguments[i]));
			events.add(eventVO);
		}		
		
		this.events = events;
	}

}
