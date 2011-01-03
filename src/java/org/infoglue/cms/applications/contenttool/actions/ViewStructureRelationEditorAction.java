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

import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;


/**
 * This action handles all interaction when a user wishes to relate pages to a contentversion.
 * The xml representing the relations is stored in the attribute given in the contentversion.
 */ 

public class ViewStructureRelationEditorAction extends ViewRelationEditorAction
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * The default constructor. It sets the parameters needed for the interaction.
	 */

    public ViewStructureRelationEditorAction()
    {
    	this.currentAction 				= "ViewStructureRelationEditor!V3.action";
    	this.changeRepositoryAction 	= "ViewStructureRelationEditor!changeRepositoryV3.action";
    	this.currentEntity 				= "SiteNode";
    	this.currentEntityIdentifyer 	= "siteNodeId";
    }
    
	/**
	 * A method that gets the name for the qualifyer for representation purposes.
	 */

	public String getQualifyerPath(String entityId)
	{	
		try
		{	
			return SiteNodeController.getController().getSiteNodeVOWithId(new Integer(entityId)).getName();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return "";
	}
	 
}
