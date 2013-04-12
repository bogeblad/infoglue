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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public class ContentCleaner extends ContentFunction 
{
	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		try 
		{
			if(getContentVO() != null)
			{
				//Content content = ContentController.getContentController().getMediumContentWithId(getContentVO().getId(), getDatabase());
				//Collection versions = content.getContentVersions();
				
				List<ContentVersionVO> versions = ContentVersionController.getContentVersionController().getContentVersionVOList(getContentVO().getId(), getDatabase());
				boolean hasPublishedVersion = false;
				Iterator<ContentVersionVO> versionsIterator = versions.iterator();
				while(versionsIterator.hasNext())
				{
					ContentVersionVO cv = versionsIterator.next();
					if(cv.getStateId().intValue() == ContentVersionVO.PUBLISHED_STATE.intValue())
						hasPublishedVersion = true;
				}
			
				if(!hasPublishedVersion)
					ContentController.getContentController().delete(getContentVO(), getPrincipal(), getDatabase());
			}
		} 
		catch(Exception e) 
		{
			throwException(e);
		}
	}
}