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

import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.deliver.taglib.AbstractTag;

/**
 * 
 */
public class DigitalAssetUrlTag extends AbstractTag 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3648763492131170473L;
	
	/**
	 * 
	 */
	private Integer digitalAssetId;


	/**
	 * 
	 */
	public DigitalAssetUrlTag() 
	{
		super();
	}

	/**
	 * 
	 */
	public int doEndTag() throws JspException 
	{
		setResultAttribute(getDigitalAssetUrl());
		return super.doEndTag();
	}

	/**
	 * 
	 */
	private String getDigitalAssetUrl() throws JspException 
	{
		try
		{
			return DigitalAssetController.getDigitalAssetUrl(digitalAssetId);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new JspTagException(e.getMessage());
		}
	}
	
	/**
	 * 
	 */
	public void setDigitalAssetId(final String digitalAssetId) throws JspException
	{
		this.digitalAssetId = evaluateInteger("digitalAssetUrl", "digitalAssetId", digitalAssetId);
	}

}
