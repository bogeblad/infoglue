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

import java.io.File;
import java.util.List;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.taglib.TemplateControllerTag;

public class DocumentConverterTag extends TemplateControllerTag 
{
	private static final long serialVersionUID = 4050206323348354355L;
	
	private String docUrl;
	private String docFilePath;
	private String title;
	private String menuTextLength;
	private String keepMenuExpanded;
	private List cssList;
	private String rewrite;
	
    public DocumentConverterTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
		try
        {			
			File docFile = createFileObject();
			
            setResultAttribute(this.getController().getDocumentTransformerHelper().convert(this.getController().getHttpServletRequest(), docFile, title, menuTextLength, cssList, rewrite, keepMenuExpanded));
        } 
		catch (Exception e)
        {
            e.printStackTrace();
        }
		
        return EVAL_PAGE;
    }

    private File createFileObject()
	{
    	File returnFile = null;
    	
		if (docFilePath != null && !docFilePath.equals(""))
		{
			returnFile = new File(docFilePath);
		}
		else if (docUrl != null && !docUrl.equals(""))
		{
			String fileName = docUrl.substring(docUrl.lastIndexOf("/") + 1);
			String filePath = CmsPropertyHandler.getDigitalAssetPath();
			int idIndex = fileName.indexOf("_");
			if(idIndex > -1)
			{
				String fileIdString = fileName.substring(0, idIndex);
				int fileId = Integer.parseInt(fileIdString);
				String folderName = "" + (fileId / 1000);
				filePath = CmsPropertyHandler.getDigitalAssetPath() + File.separator + folderName;
			}
			
			String assetPath = filePath + File.separator + fileName;
			returnFile = new File(assetPath);
		}
		
		return returnFile;
	}

	public void setDocUrl(String docUrl) throws JspException
    {
        this.docUrl = evaluateString("DocumentConverterTag", "docUrl", docUrl);
    }
    
    public void setDocFilePath(String docFilePath) throws JspException
    {
        this.docFilePath = evaluateString("DocumentConverterTag", "docFilePath", docFilePath);
    }
    
    public void setTitle(String title) throws JspException
    {
        this.title = evaluateString("DocumentConverterTag", "title", title);
    }
    
    public void setKeepMenuExpanded(String keepMenuExpanded) throws JspException
    {
        this.keepMenuExpanded = evaluateString("DocumentConverterTag", "keepMenuExpanded", keepMenuExpanded);
    }
    
    public void setMenuTextLength(String menuTextLength) throws JspException
    {
        this.menuTextLength = evaluateString("DocumentConverterTag", "menuTextLength", menuTextLength);
    }
    
    public void setCssList(String cssList) throws JspException
    {
        this.cssList = (List)evaluate("DocumentConverterTag", "cssList", cssList, List.class);
    }
    
    public void setRewrite(String rewrite) throws JspException
    {
        this.rewrite = evaluateString("DocumentConverterTag", "rewrite", rewrite);
    }
}
