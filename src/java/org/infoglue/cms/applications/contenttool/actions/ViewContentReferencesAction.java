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

package org.infoglue.cms.applications.contenttool.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RegistryController;

import webwork.action.Action;

public class ViewContentReferencesAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewContentReferencesAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Integer contentId		= null;
	private List referenceBeanList 	= new ArrayList();
    
    public String doExecute() throws Exception
    {
		this.referenceBeanList = RegistryController.getController().getReferencingObjectsForContent(contentId, 100, true, true);
        
        return Action.SUCCESS;
    }

    public Integer getContentId()
    {
        return this.contentId;
    }
        
    public void setContentId(Integer contentId)
    {
	    this.contentId =  contentId;
    }
	
    public List getReferenceBeanList()
    {
        return referenceBeanList;
    }
}
