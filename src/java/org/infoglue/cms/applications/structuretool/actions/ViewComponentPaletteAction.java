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

package org.infoglue.cms.applications.structuretool.actions;

import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ComponentController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.exception.SystemException;

/**
 * This class implements the palette view.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewComponentPaletteAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;

	private List components = null;
	private LanguageVO masterLanguageVO;
	private Integer filterRepositoryId;
	private Integer siteNodeId;
	
	/**
	 * This method returns the contents that are of contentTypeDefinition "HTMLTemplate" sorted on the property given.
	 */
	
	public List getSortedComponents(String sortProperty)
	{
	    List componentVOList = null;
	    
	    try
	    {
	        String direction = "asc";
	        
	        componentVOList = ComponentController.getController().getComponentVOList(sortProperty, direction, null, null, new String[]{"favourites"}, this.getInfoGluePrincipal());
	    }
	    catch(Exception e)
	    {
	        e.printStackTrace();
	    }
		
	    return componentVOList;
	}
	
	
    public String doExecute() throws SystemException
    {
    	Integer currentRepositoryId = getRepositoryId();
		this.masterLanguageVO = LanguageController.getController().getMasterLanguage(currentRepositoryId);		
		this.components = getSortedComponents("name");
		
        return "success";
    }

	public List getComponents() 
	{
		return components;
	}

	public LanguageVO getMasterLanguageVO() 
	{
		return masterLanguageVO;
	}


}
