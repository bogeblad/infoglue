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

package org.infoglue.cms.applications.workflowtool.actions;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;


/**
 * This action deletes a given event.
 */

public class DeleteEventAction extends InfoGlueAbstractAction
{

   	private ConstraintExceptionBuffer ceb;
	private EventVO eventVO;  
	private String url = "";
		  
  	public DeleteEventAction()
	{
		this(new EventVO());
	}
	
	public DeleteEventAction(EventVO eventVO)
	{
		this.eventVO = eventVO;
		this.ceb = new ConstraintExceptionBuffer();			
	}	


	public void setEventId(Integer eventId)
	{
		this.eventVO.setEventId(eventId);
	}

	public void setUrl(String url)
	{
		this.url = url;
	}
      
    public String doExecute() throws Exception
    {
    	ceb.throwIfNotEmpty();
    	
		EventController.delete(this.eventVO);  	
    	getResponse().sendRedirect(this.url);
		
        return NONE;
    }
        
}
