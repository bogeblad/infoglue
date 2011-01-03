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
package org.infoglue.cms.taglib.content;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.deliver.taglib.AbstractTag;

/**
 * 
 */
public class ContentVersionTag extends AbstractTag 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5895792525780372296L;

	/**
	 * 
	 */
	private Integer contentId;

	/**
	 * 
	 */
	private Integer languageId;
	
	/**
	 * 
	 */
	public ContentVersionTag() 
	{
		super();
	}
	
	/**
	 * 
	 */
	public int doEndTag() throws JspException 
	{		
		setResultAttribute(getContentVersion());
		return super.doEndTag();
	}

	private ContentVersionVO getContentVersion() throws JspTagException
	{
		ContentVersionVO contentVersion;
		try 
		{
			contentVersion = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentId, languageId);
		} 
		catch(Exception e)
		{
			e.printStackTrace();
			throw new JspTagException(e.getMessage());
		}
		return contentVersion;
	}
	
	/**
	 * 
	 */
	public void setContentId(final String contentId) throws JspException
	{
		this.contentId = evaluateInteger("digitalAssets", "contentId", contentId);
	}

	/**
	 * 
	 */
	public void setLanguageId(final String languageId) throws JspException
	{
		this.languageId = evaluateInteger("digitalAssets", "languageId", languageId);
	}
}
