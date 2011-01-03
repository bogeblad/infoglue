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

package org.infoglue.deliver.integration.dataproviders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.databeans.GenericOptionDefinition;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.applications.databeans.ComponentPropertyOption;

public class ContentTypeDefinitionsPropertyOptionsDataProvider implements PropertyOptionsDataProvider
{
	public List<GenericOptionDefinition> getOptions(Map parameters, String languageCode, InfoGluePrincipal principal, Database db) throws Exception
	{
		List<GenericOptionDefinition> options = new ArrayList<GenericOptionDefinition>();
		
		List authorizedContentTypeDefinitionVOList = ContentTypeDefinitionController.getController().getSortedAuthorizedContentTypeDefinitionVOList(principal, db);
		Iterator authorizedContentTypeDefinitionVOListIterator = authorizedContentTypeDefinitionVOList.iterator();
		while(authorizedContentTypeDefinitionVOListIterator.hasNext())
		{
			ContentTypeDefinitionVO contentTypeDefinitionVO = (ContentTypeDefinitionVO)authorizedContentTypeDefinitionVOListIterator.next();
			ComponentPropertyOption option = new ComponentPropertyOption();
			option.setName(contentTypeDefinitionVO.getName());
			option.setValue(contentTypeDefinitionVO.getId().toString());
			options.add(option);
		}
		
		return options;
	}
}
