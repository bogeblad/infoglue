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
package org.infoglue.deliver.taglib.content.el;

import java.io.File;

import javax.servlet.jsp.JspException;

import org.apache.commons.fileupload.FileItem;

/**
 * This class implements the &lt;content:digitalAssetParameter&gt; tag, which adds an asset
 * to the list of assets in the contentVersion.
 *
 *  Note! This tag must have a &lt;content:contentVersionParameter&gt; ancestor.
 */

public class DigitalAssetParameterTag extends org.infoglue.deliver.taglib.content.DigitalAssetParameterTag 
{
	private static final long serialVersionUID = -4438665303175141058L;

	public void setBytes(final byte[] bytes) throws JspException
	{
		super.setBytesObject(bytes);
	}

	public void setFile(final File file) throws JspException
	{
		super.setFileObject(file);
	}

	public void setFileItem(final FileItem fileItem) throws JspException
	{
		super.setFileItemObject(fileItem);
	}

}
