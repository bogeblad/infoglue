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

import java.util.Calendar;

import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.entities.content.ContentVO;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 *
 */
public class ContentMover extends ContentFunction 
{
	/**
	 * 
	 */
	public static final String DESTINATION_PARAMETER = "move_newParentFolder";
	
	/**
	 * 
	 */
	public static final String DESTINATION_PATH_ALGORITHM = "pathAlgorithm";
	
	/**
	 * 
	 */
	private ContentVO destinationContentVO;
	
	/**
	 *
	 */
	public ContentMover() 
	{ 
		super(); 
	}

	/**
	 * 
	 */
	protected void execute() throws WorkflowException 
	{
		if(getContentVO() != null)
		{
			try 
			{
				if(!getContentVO().getParentContentId().equals(destinationContentVO.getContentId()))
				{
					ContentController.getContentController().moveContent(getContentVO(), destinationContentVO.getId(), getDatabase());
				}
			} 
			catch(Exception e) 
			{
				throwException(e);
			}
		}
	}

	/**
	 * Method used for initializing the function; will be called before <code>execute</code> is called.
	 * <p><strong>Note</strong>! You must call <code>super.initialize()</code> first.</p>
	 * 
	 * 2007-11-05
	 * Content can now be stored i subfolders of year and month under the designated destination folder.
	 * 
	 * @throws WorkflowException if an error occurs during the initialization.
	 */
	protected void initialize() throws WorkflowException 
	{
		super.initialize();
		
		this.destinationContentVO = (ContentVO) getParameter(DESTINATION_PARAMETER, (getContentVO() != null));
		
		if(argumentExists(DESTINATION_PATH_ALGORITHM) && getArgument(DESTINATION_PATH_ALGORITHM).equalsIgnoreCase("YEAR_MONTH"))
		{
			try
			{
				String contentPath ="";
				Calendar aCal = Calendar.getInstance();
				int aYear = aCal.get(Calendar.YEAR);
				int aMonth = aCal.get(Calendar.MONTH) + 1;
				
				contentPath = ContentController.getContentController().getContentPath(destinationContentVO.getContentId());
				contentPath += "/" + aYear +"/" + aMonth;
				
				ContentVO newParentContentVO = ContentController.getContentController().getContentVOWithPath(destinationContentVO.getRepositoryId(), contentPath, true, getPrincipal(), getDatabase());
				this.destinationContentVO = newParentContentVO;
			}
			catch(Exception e) 
			{
				throwException(e);
			}
		}
	}

}
