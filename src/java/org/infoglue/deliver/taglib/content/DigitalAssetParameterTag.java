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

import java.io.File;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.commons.fileupload.FileItem;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.webservices.elements.RemoteAttachment;
import org.infoglue.deliver.taglib.AbstractTag;

/**
 * This class implements the &lt;content:digitalAssetParameter&gt; tag, which adds an asset
 * to the list of assets in the contentVersion.
 *
 *  Note! This tag must have a &lt;content:contentVersionParameter&gt; ancestor.
 */

public class DigitalAssetParameterTag extends AbstractTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 4482006814634520239L;

	/**
	 * The assetKey of the parameter.
	 */
	private String assetKey;

	/**
	 * The fileName of the parameter.
	 */
	private String fileName;

	/**
	 * The filePath of the parameter.
	 */
	private String filePath;

	/**
	 * The contentType of the parameter.
	 */
	private String contentType;

	/**
	 * The bytes of the parameter.
	 */
	private byte[] bytes;

	/**
	 * The file.
	 */
	private File file;
	
	/**
	 * The file item.
	 */
	private FileItem fileItem;
	
	/**
	 * Default constructor. 
	 */
	public DigitalAssetParameterTag()
	{
		super();
	}

	/**
	 * Adds a parameter with the specified name and value to the parameters
	 * of the parent tag.
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		addDigitalAsset();
		
		this.assetKey = null;
		this.bytes = null;
		this.contentType = null;
		this.file = null;
		this.fileItem = null;
		this.fileName = null;
		this.filePath = null;

		return EVAL_PAGE;
    }
	
	/**
	 * Adds the digital asset to the ancestor tag.
	 * 
	 * @throws JspException if the ancestor tag isn't a content version tag.
	 */
	protected void addDigitalAsset() throws JspException
	{
		final ContentVersionParameterInterface parent = (ContentVersionParameterInterface) findAncestorWithClass(this, ContentVersionParameterInterface.class);
		if(parent == null)
		{
			throw new JspTagException("DigitalAssetParameterTag must have a ContentVersionParameterInterface ancestor.");
		}
		
		if(bytes != null)
		{
		}
		else if(file != null)
		{
		    try
            {
                this.bytes = FileHelper.getFileBytes(file);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
		}
		else if(fileItem != null)
		{
		    try
            {
                this.bytes = fileItem.get();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
		}
		else
		{
		    throw new JspException("Must state either bytes, a file or a fileItem");
		}
		
		RemoteAttachment attachment = new RemoteAttachment(this.assetKey, this.fileName, this.filePath, this.contentType, this.bytes);
		
		((ContentVersionParameterInterface) parent).addDigitalAsset(attachment);
	}
	
	/**
	 * Sets the assetKey attribute.
	 * 
	 * @param assetKey the assetKey to use.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setAssetKey(final String assetKey) throws JspException
	{
		this.assetKey = evaluateString("parameter", "assetKey", assetKey);
	}

	/**
	 * Sets the fileName attribute.
	 * 
	 * @param fileName the fileName to use.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setFileName(final String fileName) throws JspException
	{
		this.fileName = evaluateString("parameter", "fileName", fileName);
	}

	/**
	 * Sets the filePath attribute.
	 * 
	 * @param filePath the filePath to use.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setFilePath(final String filePath) throws JspException
	{
		this.filePath = evaluateString("parameter", "filePath", filePath);
	}

	/**
	 * Sets the contentType attribute.
	 * 
	 * @param contentType the contentType to use.
	 * @throws JspException if an error occurs while evaluating name parameter.
	 */
	public void setContentType(final String contentType) throws JspException
	{
		this.contentType = evaluateString("parameter", "contentType", contentType);
	}

	/**
	 * Sets the bytes attribute to the specified evaluated bytes.
	 * 
	 * @param name the contents of the attachment.
	 * @throws JspException if an error occurs while evaluating the bytes.
	 */
	public void setBytes(final String bytes) throws JspException
	{
		this.bytes = (byte[]) evaluate("addAttachment", "bytes", bytes, Object.class);
	}

	/**
	 * Sets the file attribute to the specified evaluated file.
	 * 
	 * @param file the contents of the attachment.
	 * @throws JspException if an error occurs while evaluating the file.
	 */
	public void setFile(final String file) throws JspException
	{
		this.file = (File) evaluate("addAttachment", "file", file, Object.class);
	}

	/**
	 * Sets the FileItem attribute to the specified evaluated fileItem.
	 * 
	 * @param fileItem the contents of the attachment.
	 * @throws JspException if an error occurs while evaluating the fileItem.
	 */
	public void setFileItem(final String fileItem) throws JspException
	{
		this.fileItem = (FileItem) evaluate("addAttachment", "fileItem", fileItem, Object.class);
	}

}
