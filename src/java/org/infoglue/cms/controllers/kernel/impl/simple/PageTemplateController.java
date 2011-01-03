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
import java.util.Iterator;
import java.util.List;

import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.security.InfoGluePrincipal;

public class PageTemplateController extends BaseController 
{
	
	/**
	 * Factory method
	 */

	public static PageTemplateController getController()
	{
		return new PageTemplateController();
	}

	/**
	 * This method returns the contents that are of contentTypeDefinition "PageTemplate"
	 */
	
	public List getPageTemplates(InfoGluePrincipal infoGluePrincipal, Integer languageId) throws Exception
	{
	    List pageTemplates = getPageTemplates(infoGluePrincipal);
	    Iterator i = pageTemplates.iterator();
	    while(i.hasNext())
	    {
	        ContentVO contentVO = (ContentVO)i.next();
	        ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageId);
		    if(contentVersionVO == null)
		    {
		        i.remove();
		    }
		}
	    
	    return pageTemplates;
	}

	/**
	 * This method returns the contents that are of contentTypeDefinition "PageTemplate"
	 */
	
	public List getPageTemplates(InfoGluePrincipal infoGluePrincipal) throws Exception
	{
		HashMap arguments = new HashMap();
		arguments.put("method", "selectListOnContentTypeName");
		
		List argumentList = new ArrayList();
		HashMap argument = new HashMap();
		argument.put("contentTypeDefinitionName", "PageTemplate");
		argumentList.add(argument);
		arguments.put("arguments", argumentList);
		
		return ContentControllerProxy.getController().getACContentVOList(infoGluePrincipal, arguments);
		//return ContentController.getContentController().getContentVOList(arguments);
	}

	
	

	
	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new SiteNodeVO();
	}


}
 
