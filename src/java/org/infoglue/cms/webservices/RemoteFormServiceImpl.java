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

package org.infoglue.cms.webservices;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.FormEntryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.entities.management.FormEntry;
import org.infoglue.cms.entities.management.FormEntryAssetVO;
import org.infoglue.cms.entities.management.FormEntryVO;
import org.infoglue.cms.entities.management.FormEntryValueVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.webservices.elements.RemoteAttachment;
import org.infoglue.deliver.util.webservices.DynamicWebserviceSerializer;


/**
 * This class is responsible for letting an external application call InfoGlue
 * API:s remotely. It handles api:s to manage form entries and their values.
 * 
 * @author Mattias Bogeblad
 */

public class RemoteFormServiceImpl extends RemoteInfoGlueService
{
    private final static Logger logger = Logger.getLogger(RemoteFormServiceImpl.class.getName());

	/**
	 * The principal executing the workflow.
	 */
	private InfoGluePrincipal principal;
	
	private static FormEntryController formEntryController = FormEntryController.getController();
    
 
    /**
     * Inserts one or many new SiteNode including versions etc.
     */
    
    public Boolean createFormEntry(final String principalName, final Object[] inputsArray) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new Boolean(false);
        }

    	Boolean status = new Boolean(true);

        List newSiteNodeIdList = new ArrayList();
        
        logger.info("****************************************");
        logger.info("Creating gorm entry through webservice....");
        logger.info("****************************************");
        
        logger.info("principalName:" + principalName);
        logger.info("inputsArray:" + inputsArray);
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
            List formEntries = (List) serializer.deserialize(inputsArray);
	        logger.info("formEntries:" + formEntries);

            initializePrincipal(principalName);
	        Iterator formEntriesIterator = formEntries.iterator();
	        while(formEntriesIterator.hasNext())
	        {
	            Map formEntry = (Map)formEntriesIterator.next();
	            
	            String formName 		= (String)formEntry.get("formName");
	            Integer formContentId 	= (Integer)formEntry.get("formContentId");
	            String originAddress 	= (String)formEntry.get("originAddress");
	            String userIP 			= (String)formEntry.get("userIP");
	            String userAgent 		= (String)formEntry.get("userAgent");
	            String userName 		= principalName;
	            Date registrationDate	= new Date();
	            
	            Map formEntryValues 	= (Map)formEntry.get("formEntryValues");
	            
	            logger.info("formName:" + formName);
	            logger.info("formContentId:" + formContentId);
	            logger.info("originAddress:" + originAddress);
	            logger.info("userIP:" + userIP);
	            logger.info("userAgent:" + userAgent);

	            logger.info("formEntryValues:" + formEntryValues);

	            List formEntryValueVOList = new ArrayList();
	            Iterator formEntryValuesIterator = formEntryValues.keySet().iterator();
	            while(formEntryValuesIterator.hasNext())
	            {
	            	String name = (String)formEntryValuesIterator.next();
	            	if(name != null && !name.equals(""))
	            	{
		            	String value = (String)formEntryValues.get(name);
		            	FormEntryValueVO formEntryValueVO = new FormEntryValueVO();
		            	formEntryValueVO.setName(name);
		            	formEntryValueVO.setValue(value);
		            	formEntryValueVOList.add(formEntryValueVO);
	            	}
	            	else
	            	{
	            		logger.warn("No name in variable - skipping");
	            	}
	            }
	            
	            FormEntryVO formEntryVO = new FormEntryVO();
	            formEntryVO.setFormContentId(formContentId);
	            formEntryVO.setFormName(formName);
	            formEntryVO.setOriginAddress(originAddress);
	            formEntryVO.setUserAgent(userAgent);
	            formEntryVO.setUserIP(userIP);
	            formEntryVO.setUserName(userName);
	            formEntryVO.setRegistrationDateTime(registrationDate);

	            FormEntry newFormEntry = null;
	            
	            Database db = CastorDatabaseService.getDatabase();
	            ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

	            beginTransaction(db);
	            try
	            {
	            	newFormEntry = formEntryController.create(formEntryVO, formEntryValueVOList, db);
	            			            
	                commitTransaction(db);
	            }
	            catch(Exception e)
	            {
	                logger.error("An error occurred so we should not completes the transaction:" + e, e);
	                rollbackTransaction(db);
	                throw new SystemException(e.getMessage());
	            }
	            
	            
	            Database db2 = CastorDatabaseService.getDatabase();

	            beginTransaction(db2);
	            try
	            {
	            	newFormEntry = formEntryController.getFormEntryWithId(newFormEntry.getFormEntryId(), db2);

	    	        List digitalAssets = (List)formEntry.get("digitalAssets");
	    	        logger.info("digitalAssets:" + digitalAssets);
	    	        if(digitalAssets != null)
	    	        {
		    	        Iterator digitalAssetIterator = digitalAssets.iterator();
		    	        while(digitalAssetIterator.hasNext())
		    	        {
		    	            RemoteAttachment remoteAttachment = (RemoteAttachment)digitalAssetIterator.next();
			    	        logger.info("digitalAssets in ws:" + remoteAttachment);
			    	        
			    	        FormEntryAssetVO newAsset = new FormEntryAssetVO();
							newAsset.setContentType(remoteAttachment.getContentType());
							newAsset.setAssetKey(remoteAttachment.getName());
							newAsset.setFileName(remoteAttachment.getFileName());
							newAsset.setFileSize(new Integer(new Long(remoteAttachment.getBytes().length).intValue()));
							byte[] bytes = remoteAttachment.getBytes();
							InputStream is = new ByteArrayInputStream(bytes);
		
							formEntryController.createAsset(newAsset, newFormEntry, is, newFormEntry.getId(), principal, db2);
			    	    }	 
	    	        }

	                commitTransaction(db2);
	            }
	            catch(Exception e)
	            {
	                logger.error("An error occurred so we should not completes the transaction:" + e, e);
	                rollbackTransaction(db2);
	                throw new SystemException(e.getMessage());
	            }

	        }
	        logger.info("Done with site nodes..");
        }
        catch(Throwable e)
        {
        	status = new Boolean(false);
            logger.error("En error occurred when we tried to create a new siteNode:" + e.getMessage(), e);
        }
        
        updateCaches();

        return status;
    }

    /**
     * Deletes a form entry.
     */
    
    public Boolean deleteFormEntry(final String principalName, final Object[] inputsArray) 
    {
        if(!ServerNodeController.getController().getIsIPAllowed(getRequest()))
        {
            logger.error("A client with IP " + getRequest().getRemoteAddr() + " was denied access to the webservice. Could be a hack attempt or you have just not configured the allowed IP-addresses correct.");
            return new Boolean(false);
        }

    	Boolean status = new Boolean(true);
    	
        logger.info("******************************************");
        logger.info("Deleting form entry through webservice....");
        logger.info("******************************************");
        
        logger.info("principalName:" + principalName);
        logger.info("inputsArray:" + inputsArray);
        //logger.warn("contents:" + contents);
        
        try
        {
			final DynamicWebserviceSerializer serializer = new DynamicWebserviceSerializer();
            Map content = (Map) serializer.deserialize(inputsArray);
	        logger.info("content:" + content);

            initializePrincipal(principalName);
            
            Integer formEntryId = (Integer)content.get("formEntryId");
            Boolean forceDelete = (Boolean)content.get("forceDelete");
            if(forceDelete == null)
            	forceDelete = new Boolean(false);
                        
            logger.info("formEntryId:" + formEntryId);
            
            FormEntryVO formEntryVO = new FormEntryVO();
            formEntryVO.setFormEntryId(formEntryId);

            FormEntryController.getController().delete(formEntryVO);
                           
	        logger.info("Done with contents..");

        }
        catch(Throwable e)
        {
        	status = new Boolean(false);
            logger.error("En error occurred when we tried to delete a digitalAsset:" + e.getMessage(), e);
        }
        
        updateCaches();

        return status;
    }

    
	/**
	 * Checks if the principal exists and if the principal is allowed to create the workflow.
	 * 
	 * @param userName the name of the user.
	 * @param workflowName the name of the workflow to create.
	 * @throws SystemException if the principal doesn't exists or doesn't have permission to create the workflow.
	 */
	private void initializePrincipal(final String userName) throws SystemException 
	{
		try 
		{
			principal = UserControllerProxy.getController().getUser(userName);
		}
		catch(SystemException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new SystemException(e);
		}
		if(principal == null) 
		{
			throw new SystemException("No such principal [" + userName + "].");
		}
	}


}
