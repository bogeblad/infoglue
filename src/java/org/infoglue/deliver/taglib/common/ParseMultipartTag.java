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
package org.infoglue.deliver.taglib.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * This tag will get a cookie value  
 */

public class ParseMultipartTag extends TemplateControllerTag 
{
    public final static Logger logger = Logger.getLogger(ParseMultipartTag.class.getName());

	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 8603406098980150888L;
	
	private Integer maxSize = new Integer(100000); 
	private String allowedContentTypes;
	private String[] allowedContentTypeArray;
	private boolean ignoreEmpty = false;
	
	/**
	 * Default constructor.
	 */
	public ParseMultipartTag() 
	{
		super();
	}
	
	/**
	 * Process the end tag. Sets a cookie.  
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
	    Map parameters = new HashMap();
	    
	    try
	    {
			//Create a factory for disk-based file items
		    DiskFileItemFactory factory = new DiskFileItemFactory();
			
			//Set factory constraints
			factory.setSizeThreshold(maxSize.intValue());
			//factory.setRepository(new File(CmsPropertyHandler.getDigitalAssetPath()));
			
			//Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);
			
			//Set overall request size constraint
			upload.setSizeMax(this.maxSize.intValue());
	
			if(upload.isMultipartContent(this.getController().getHttpServletRequest()))
			{
				//Parse the request
				List items = upload.parseRequest(this.getController().getHttpServletRequest());
					
				List files = new ArrayList();
				
				//Process the uploaded items
				Iterator iter = items.iterator();
				while (iter.hasNext()) 
				{
				    FileItem item = (FileItem) iter.next();
		
				    if (!item.isFormField()) 
				    {
				        String fieldName = item.getFieldName();
				        String fileName = item.getName();
				        String contentType = item.getContentType();
				        boolean isInMemory = item.isInMemory();
				        long sizeInBytes = item.getSize();

				        if(isValidContentType(contentType))
				        {
				            files.add(item);
				        }
				        else
				        {
				            if((item.getName() == null || item.getName().equals("")) && this.ignoreEmpty)
				            {
				                logger.warn("Empty file but that was ok..");
				            }
				            else
				            {
						        pageContext.setAttribute("status", "nok");
						        pageContext.setAttribute("upload_error", "A field did not have a valid content type");
						        pageContext.setAttribute(fieldName + "_error", "Not a valid content type");
					            //throw new JspException("Not a valid content type");
				            }
				        }
				    }
				    else
				    {
				        String name = item.getFieldName();
				        String value = item.getString();
				        String oldValue = (String)parameters.get(name);
				        if(oldValue != null)
				            value = oldValue + "," + value; 
				        
				        if(value != null)
						{
							try
							{
								String fromEncoding = "iso-8859-1";
								String toEncoding = "utf-8";
						
								String testValue = new String(value.getBytes(fromEncoding), toEncoding);
								
								if(testValue.indexOf((char)65533) == -1)
									value = testValue;
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
						}
				        parameters.put(name, value);
				    }
				        
				}
				
				parameters.put("files", files);
				
			    setResultAttribute(parameters);
			}
			else
			{
			    setResultAttribute(null);
			}
	    }
	    catch(Exception e)
	    {
	        logger.warn("Error doing an upload" + e.getMessage(), e);
            //pageContext.setAttribute("fieldName_exception", "contentType_MAX");
	        //throw new JspException("File upload failed: " + e.getMessage());
	        pageContext.setAttribute("status", "nok");
	        pageContext.setAttribute("upload_error", "" + e.getMessage());
	    }
	    
        return EVAL_PAGE;
    }

	private boolean isValidContentType(String contentType)
	{
	    boolean valid = false;
	    for(int i=0; i<this.allowedContentTypeArray.length; i++)
	    {
	        if(this.allowedContentTypeArray[i].equalsIgnoreCase(contentType))
	            valid = true;
	    }
	    return valid;
	}
	
    public void setMaxSize(String maxSize) throws JspException
    {
        this.maxSize = evaluateInteger("FileUploadTag", "maxSize", maxSize);
    }

    public void setAllowedContentTypes(String allowedContentTypes) throws JspException
    {
        this.allowedContentTypes = evaluateString("FileUploadTag", "allowedContentTypes", allowedContentTypes);
        this.allowedContentTypeArray = this.allowedContentTypes.split(",");
    }
    
    public void setIgnoreEmpty(boolean ignoreEmpty)
    {
        this.ignoreEmpty = ignoreEmpty;
    }
}
