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
package org.infoglue.cms.applications.workflowtool.function.email;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.infoglue.cms.applications.workflowtool.function.ContentFunction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.DigitalAsset;

import com.opensymphony.workflow.WorkflowException;

public class AttachmentsProvider extends ContentFunction 
{
	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		setParameter(EmailFunction.ATTACHMENTS_PARAMETER, getAttachments());
	}
	
	/**
	 * 
	 */
	private Collection getAttachments() throws WorkflowException
	{
		final Collection digitalAssets = getDigitalAssets();
		final Collection attachments   = new ArrayList();
		for(final Iterator i = digitalAssets.iterator(); i.hasNext(); )
		{
			final DigitalAsset digitalAsset = (DigitalAsset) i.next();
			attachments.add(new DigitalAssetAttachment(digitalAsset.getAssetFileName(), digitalAsset.getAssetContentType(), digitalAsset.getAssetBytes()));
		}
		return attachments;
	}
	
	/**
	 * 
	 */
	private Collection getDigitalAssets() throws WorkflowException
	{
		if(getContentVersionVO() != null)
		{
			try
			{
				final ContentVersion contentVersion = ContentVersionController.getContentVersionController().getReadOnlyContentVersionWithId(getContentVersionVO().getContentVersionId(), getDatabase()); 
				if(contentVersion != null)
				{
					return contentVersion.getDigitalAssets();
				}
			}
			catch(Exception e)
			{
				throwException(e);
			}
		}
		return new ArrayList();
	}
}
