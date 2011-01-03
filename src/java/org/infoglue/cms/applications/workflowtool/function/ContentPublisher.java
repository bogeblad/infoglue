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
package org.infoglue.cms.applications.workflowtool.function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.publishing.PublicationVO;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public class ContentPublisher extends ContentFunction 
{
	/**
	 * 
	 */
	private static final String STATUS_OK = "status.publish.ok";
	
	/**
	 * 
	 */
	private static final String STATUS_NOK = "status.publish.nok";

	/**
	 * 
	 */
	private static final String STATUS_MORE_VERSIONS = "status.publish.okwithmoreworkingversions";

	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		setFunctionStatus(STATUS_NOK);
		if(getContentVO() != null && getContentVersionVO() != null)
		{
			publish();
		}
	}
	
	/**
	 * 
	 */
	private void publish() throws WorkflowException 
	{
		try 
		{
			if(getContentVersionVO().getStateId().equals(ContentVersionVO.WORKING_STATE)) 
			{
				final List events = new ArrayList();
				ContentStateController.changeState(getContentVersionVO().getContentVersionId(), ContentVersionVO.PUBLISH_STATE, "Auto", false, getPrincipal(), getContentVO().getId(), getDatabase(), events);
				PublicationController.getController().createAndPublish(createPublicationVO(), events, true, getPrincipal(), getDatabase());
				
				boolean ok = true;
				
				List languages = LanguageController.getController().getLanguageVOList(getContentVO().getRepositoryId(), getDatabase());
				Iterator languageIterator = languages.iterator();
				while(languageIterator.hasNext())
				{
					LanguageVO languageVO = (LanguageVO)languageIterator.next();
					if(languageVO.getId().intValue() != getContentVersionVO().getLanguageId().intValue())
					{
						ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(getContentVO().getId(), languageVO.getId(), getDatabase());
						if(contentVersionVO != null && contentVersionVO.getStateId().intValue() == ContentVersionVO.WORKING_STATE.intValue())
						{
							if(languageVO == null && propertySetContains(LanguageProvider.LANGUAGE_PROPERTYSET_KEY))
							{
								removeFromPropertySet(LanguageProvider.LANGUAGE_PROPERTYSET_KEY);
							}
							if(languageVO != null) 
							{
								setParameter(LanguageProvider.LANGUAGE_PARAMETER, languageVO);
								setPropertySetString(LanguageProvider.LANGUAGE_PROPERTYSET_KEY, languageVO.getId().toString());
							}
							
							setFunctionStatus(STATUS_MORE_VERSIONS);
							ok = false;
							
							break;
						}
					}
				}
				
				if(ok)
					setFunctionStatus(STATUS_OK);
			} 
		} 
		catch(Exception e) 
		{
			throwException(e);
		}
	}
	
	/**
	 * 
	 */
	private PublicationVO createPublicationVO() 
	{
	    final PublicationVO publicationVO = new PublicationVO();
	    publicationVO.setName("Workflow publication by " + getPrincipal().getName());
	    publicationVO.setDescription("Workflow publication by " + getPrincipal().getName());
	    publicationVO.setRepositoryId(getContentVO().getRepositoryId());
		return publicationVO;
	}
}