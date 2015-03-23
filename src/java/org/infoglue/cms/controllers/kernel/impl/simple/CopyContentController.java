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

package org.infoglue.cms.controllers.kernel.impl.simple;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.applications.managementtool.actions.ImportRepositoryAction;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;

/**
* This class handles Importing copying of a repository - by processing it as a thread in a process bean.
* 
* @author Mattias Bogeblad
*/

public class CopyContentController extends BaseController implements Runnable
{
    public final static Logger logger = Logger.getLogger(CopyContentController.class.getName());

    private String onlyLatestVersions;
    private Integer contentId;
    private Integer newParentContentId;
    private Integer maxAssetSize;
    private InfoGluePrincipal principal;
    private ProcessBean processBean;
    private String[] repositoryIds;
    
	private VisualFormatter visualFormatter = new VisualFormatter();
	
	/**
	 * Factory method to get object
	 */
	
	public static void copyContent(InfoGluePrincipal principal, Integer contentId, Integer newParentContentId, Integer maxAssetSize, String onlyLatestVersions, ProcessBean processBean) throws Exception
	{
		CopyContentController copyController = new CopyContentController(principal, contentId, newParentContentId, maxAssetSize, onlyLatestVersions, processBean);
		Thread thread = new Thread(copyController);
		thread.start();
	}
	
	private CopyContentController(InfoGluePrincipal principal, Integer contentId, Integer newParentContentId, Integer maxAssetSize, String onlyLatestVersions, ProcessBean processBean)
	{
		this.principal = principal;
		this.contentId = contentId;
		this.newParentContentId = newParentContentId;
		this.maxAssetSize = maxAssetSize;
		this.onlyLatestVersions = onlyLatestVersions;
		this.processBean = processBean;
	}

	   	
	public synchronized void run()
	{
		logger.info("Starting Copy Thread....");
		try
		{
			processBean.setStatus(processBean.RUNNING);
			ContentControllerProxy.getController().acCopyContent(this.principal, contentId, newParentContentId, maxAssetSize, onlyLatestVersions, this.processBean);
			processBean.setStatus(processBean.FINISHED);
		}
		catch (Exception e) 
		{
			//TODO: Fix this error message better. Support illegal xml-chars
			processBean.setError("Something went wrong with the import. Please consult the logfiles.");
			logger.error("Error in monitor:" + e.getMessage(), e);
		}
	}
	
    public BaseEntityVO getNewVO()
    {
        return null;
    }

}
