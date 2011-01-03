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

package org.infoglue.cms.applications.cmstool.actions;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * This class implements the base class for a tool.
 * 
 * @author Mattias Bogeblad  
 */

public abstract class ViewCMSAbstractToolAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewCMSAbstractToolAction.class.getName());

    public String doV3() throws Exception
    {
        return "successV3";
    }


    private Integer repositoryId = null;
    
    public void setRepositoryId(Integer repositoryId)
    {
    	this.repositoryId = repositoryId;
    }

	/**
	 * This method gets the repositoryId and if it'n not available we check first id it's located in the
	 * Session. If not we take the master repositoryId and also defaults it to that in the session.
	 */
	
    public Integer getRepositoryId()
    {
    	try
    	{
	    	if(this.repositoryId == null)
	    	{	
	    		logger.info("The repositoryId was null in ViewContentToolAction so we fetch it from the session");
	    		this.repositoryId = (Integer)getHttpSession().getAttribute("repositoryId");
	    		
	    		if(this.repositoryId == null)
	    		{
					List authorizedRepositoryVOList = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
					if(authorizedRepositoryVOList.size() > 0)
					{
			    		String prefferedRepositoryId = CmsPropertyHandler.getPreferredRepositoryId(this.getInfoGluePrincipal().getName());
			    		if(prefferedRepositoryId != null && prefferedRepositoryId.length() > 0)
			    		{
			    			Iterator authorizedRepositoryVOListIterator = authorizedRepositoryVOList.iterator();
			    			while(authorizedRepositoryVOListIterator.hasNext())
			    			{
			    				RepositoryVO repositoryVO = (RepositoryVO)authorizedRepositoryVOListIterator.next();
			    				if(repositoryVO.getId().toString().equals(prefferedRepositoryId))
			    				{
			    					this.repositoryId = repositoryVO.getId();
			    					break;
			    				}
			    			}
			    		}
			    		else
			    		{
							RepositoryVO repositoryVO = (RepositoryVO)authorizedRepositoryVOList.get(0);
							this.repositoryId = repositoryVO.getId();
				    	}
			    		getHttpSession().setAttribute("repositoryId", this.repositoryId);		
			    		logger.info("We set the defaultRepositoryId in the users session to " + this.repositoryId);
		    		}
		    		else
		    		{
		    		    this.repositoryId = new Integer(-1);
			    		//getHttpSession().setAttribute("repositoryId", this.repositoryId);		
		    		    logger.info("We set the defaultRepositoryId in the users session to " + this.repositoryId);
		    		}
		    	}
	    	}
    	}
    	catch(Exception e)
    	{
    	    logger.error("The master repository could not be fetched due to an error:" + e.getMessage(), e);
    	}
    	    		
    	return this.repositoryId;
    }
 
               
}
