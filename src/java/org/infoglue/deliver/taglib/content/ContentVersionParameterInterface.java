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
package org.infoglue.deliver.taglib.content;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.webservices.elements.RemoteAttachment;

/**
 * @author mattias
 */

public interface ContentVersionParameterInterface
{
	/**
	 * Adds the content version attribute to the contentVersion Value.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	public void addContentVersionAttribute(String name, String value) throws JspException;

	/**
	 * Adds the content version attribute to the contentVersion Value.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	public void addDigitalAsset(RemoteAttachment remoteAttachment) throws JspException;

	/**
	 * Adds the content category to the contentVersion Value.
	 * 
	 * @throws JspException if the ancestor tag isn't a url tag.
	 */
	public void addContentCategory(String contentCategory) throws JspException;

}