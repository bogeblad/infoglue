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

package org.infoglue.cms.applications.common.actions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.SubscriptionController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.management.SubscriptionFilterVO;
import org.infoglue.cms.entities.management.SubscriptionVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.util.DesEncryptionHelper;

import webwork.action.Action;


/** 
 * This class contains methods to handle the trashcan and the item's in it.
 */

public class ViewFileBrowserAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewFileBrowserAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private String path = "c:/";	
	private File file = null;
	private File[] drives = null;
	
	public String doExecute() throws Exception
    {
		drives = File.listRoots();
		
		file = new File(path);

		return Action.SUCCESS;
    }

	public String doViewFile() throws Exception
    {
		try
		{
			if(path != null && !path.equals(""))
			{
				System.out.println("path:" + path);
				DesEncryptionHelper desEncryptionHelper = new DesEncryptionHelper();
				String decryptedPath = desEncryptionHelper.decrypt(path);
				System.out.println("decryptedPath:" + decryptedPath);
				
				File file = new File(decryptedPath);
				if(file.exists() && file.isFile())
				{
					String contentType = "";
					if(file.getName().endsWith("pdf"))
						contentType = "application/pdf";
					else if(file.getName().endsWith("ppt") || file.getName().endsWith("pptx"))
						contentType = "application/vnd.ms-powerpoint";
					else if(file.getName().endsWith("xls") || file.getName().endsWith("xlsx"))
						contentType = "application/vnd.ms-excel";
					else if(file.getName().endsWith("doc") || file.getName().endsWith("docx"))
						contentType = "application/msword";
					else if(file.getName().endsWith("png"))
						contentType = "image/png";
					else if(file.getName().endsWith("jpg"))
						contentType = "image/jpg";
					else if(file.getName().endsWith("gif"))
						contentType = "image/gif";
					else if(file.getName().endsWith("tiff"))
						contentType = "image/tiff";
					else if(file.getName().endsWith("psd"))
						contentType = "image/psd";
					
					if(contentType.equals(""))
					{
						System.out.println("Not allowed file type");
						throw new Exception("Not allowed file type");
					}
					
					HttpServletResponse response = getResponse();
					response.setContentType(contentType);
					
					// print some html
			        ServletOutputStream out = response.getOutputStream();
			        
			        // print the file
			        InputStream in = null;
			        try 
			        {
			            in = new BufferedInputStream(new FileInputStream(file));
			            int ch;
			            while ((ch = in.read()) !=-1) 
			            {
			                out.print((char)ch);
			            }
			        }
			        finally 
			        {
			            if (in != null) in.close();  // very important
			        }
			
			        return NONE;
				}
				else
					System.out.println("File not found...:" + path);
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return Action.ERROR;
    }

	public File[] getDrives()
	{
		return drives;
	}

	public File getFile()
	{
		return file;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getPath()
	{
		return this.path;
	}

	public String getPortablePath(File file)
	{
		if(file == null)
			return "";
		
		String path = file.getPath();
		path = path.replaceAll("\\\\", "/");
		return path;
	}
}