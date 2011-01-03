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


package org.infoglue.deliver.applications.actions;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.applications.databeans.CacheEvictionBean;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;
import org.infoglue.deliver.controllers.kernel.impl.simple.DigitalAssetDeliveryController;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.ThreadMonitor;


/**
 * This is the action that takes care of all incoming update-calls. This action is
 * called by either the system or by replication-program and the class the distibutes the 
 * update-call to all the listeners which have registered earlier.
 *
 * @author Mattias Bogeblad
 */

public class DownloadAssetAction extends InfoGlueAbstractAction 
{
    private final static Logger logger = Logger.getLogger(DownloadAssetAction.class.getName());

    private Integer digitalAssetId;

    private Integer siteNodeId;
	private Integer contentId;
	private Integer languageId;
	private String assetKey;
	
	private Principal principal = null;
	
	private DigitalAssetVO digitalAssetVO;
	private String assetFilePath = "";
    
    /**
     * This method is the application entry-point. The parameters has been set through the setters
     * and now we just have to render the appropriate output. 
     */
         
    public String doExecute() throws Exception
    {
    	HttpServletResponse response = this.getResponse();
    	
        if(digitalAssetId != null && !ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the download action. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return null;
        }

        principal = (Principal)this.getHttpSession().getAttribute("infogluePrincipal");

    	getAssetInformation();
    	//logger.info("assetFilePath:" + assetFilePath);
    	//logger.info("digitalAssetVO:" + digitalAssetVO);
    	
    	if(assetFilePath != null && digitalAssetVO != null)
    	{
    		response.setContentType(digitalAssetVO.getAssetContentType());

	        // print some html
	        ServletOutputStream out = response.getOutputStream();
	        
	        // print the file
	        InputStream in = null;
	        try 
	        {
	            in = new BufferedInputStream(new FileInputStream(assetFilePath));
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
    	
    	response.sendError(302);
    	
    	return NONE;
    }

	private void getAssetInformation() throws Exception
	{
		if(logger.isDebugEnabled())
			logger.debug("Getting asset information....");
		
		if(digitalAssetId != null)
		{
			this.digitalAssetVO = DigitalAssetController.getDigitalAssetVOWithId(digitalAssetId);
			if(logger.isDebugEnabled())
				logger.debug("this.digitalAssetVO:" + this.digitalAssetVO);
			
			if(this.digitalAssetVO != null)
				this.assetFilePath = DigitalAssetController.getDigitalAssetFilePath(this.digitalAssetVO.getId());
		}
		else
		{
			if(CmsPropertyHandler.getApplicationName().equalsIgnoreCase("cms"))
			{
				if(logger.isDebugEnabled())
				{
					logger.info("contentId:" + contentId);
					logger.info("languageId:" + languageId);
					logger.info("assetKey:" + assetKey);
				}
				
				this.digitalAssetVO = DigitalAssetController.getDigitalAssetVO(contentId, languageId, assetKey, true);
				if(this.digitalAssetVO != null)
					this.assetFilePath = DigitalAssetController.getDigitalAssetProtectedFilePath(this.digitalAssetVO.getId());
			}
			else
			{
		    	ContentDeliveryController cdc = ContentDeliveryController.getContentDeliveryController();
		    	
		    	Database db = CastorDatabaseService.getDatabase();
		    	
		    	try
		    	{
		    		db.begin();
		    		DeliveryContext deliveryContext = DeliveryContext.getDeliveryContext();
		        	//assetUrl = cdc.getAssetUrl(db, contentId, languageId, assetKey, siteNodeId, true, deliveryContext, this.getInfoGluePrincipal());
		    		logger.info("principal:" + this.principal);
		    		if(this.principal == null)
		    		{
			        	this.principal = this.getInfoGluePrincipal();
			        	if(this.principal == null)
			        		this.principal = getAnonymousPrincipal();
		    		}
			        //System.out.println("principal:" + principal);
		    		Integer digitalAssetId = cdc.getDigitalAssetId(db, contentId, languageId, assetKey, siteNodeId, true, deliveryContext, (InfoGluePrincipal)principal);
		        	//System.out.println("digitalAssetId:" + digitalAssetId);
		        	if(digitalAssetId != null)
		        	{
		        		this.digitalAssetVO = DigitalAssetController.getSmallDigitalAssetVOWithId(digitalAssetId, db);
		        		logger.info("digitalAssetVO:" + digitalAssetVO);

		        		if(this.digitalAssetVO != null)
		        		{
		        			this.assetFilePath = DigitalAssetController.getDigitalAssetProtectedFilePath(digitalAssetId);
		        			logger.info("assetFilePath:" + assetFilePath);
		        		}
		        	}
		        	commitTransaction(db);
		    	}
		    	catch (Exception e) 
		    	{
		    		logger.error("Problem getting asset url:" + e.getMessage(), e);
		    		rollbackTransaction(db);
		    	}
			}
		}
	}

	public String getAssetKey()
	{
		return assetKey;
	}

	public void setAssetKey(String assetKey)
	{
		this.assetKey = assetKey;
	}

	public Integer getContentId()
	{
		return contentId;
	}

	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}

	public Integer getLanguageId()
	{
		return languageId;
	}

	public void setLanguageId(Integer languageId)
	{
		this.languageId = languageId;
	}

	public Integer getSiteNodeId()
	{
		return siteNodeId;
	}

	public void setSiteNodeId(Integer siteNodeId)
	{
		this.siteNodeId = siteNodeId;
	}

	public Integer getDigitalAssetId()
	{
		return digitalAssetId;
	}

	public void setDigitalAssetId(Integer digitalAssetId)
	{
		this.digitalAssetId = digitalAssetId;
	}
    
}
