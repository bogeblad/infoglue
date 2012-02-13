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

package org.infoglue.cms.applications.managementtool.actions;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.sorters.ReflectionComparator;

/**
 * 	Action class for usecase ViewListContentTypeDefinitionUCC 
 *
 *  @author Mattias Bogeblad
 */

public class ViewListContentTypeDefinitionAction extends InfoGlueAbstractAction 
{
    private final static Logger logger = Logger.getLogger(ViewListContentTypeDefinitionAction.class.getName());

	private static final long serialVersionUID = 1L;

	private List<ContentTypeDefinitionVO> contentTypeDefinitions;
	

	protected String doExecute() throws SystemException 
	{
		//this.contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList();
		this.contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOListWithParentId(null);
    	logger.info("contentTypeDefinitions:" + contentTypeDefinitions.size());
		Collections.sort(contentTypeDefinitions, new ReflectionComparator("name"));
    	return "success";
	}
	

	public List<ContentTypeDefinitionVO> getContentTypeDefinitions()
	{
		return this.contentTypeDefinitions;		
	}
	
	public List<ContentTypeDefinitionVO> getContentTypeDefinitions(Integer parentId) throws SystemException
	{
		return ContentTypeDefinitionController.getController().getContentTypeDefinitionVOListWithParentId(parentId);
	}
} 
