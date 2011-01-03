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
package org.infoglue.cms.applications.managementtool.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.common.Preference;
import org.apache.pluto.om.common.PreferenceSet;
import org.apache.pluto.om.entity.PortletApplicationEntityList;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.portlet.PortletDefinitionList;
import org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistry;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.PortletAssetController;
import org.infoglue.cms.entities.content.DigitalAsset;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.cms.util.RemoteCacheUpdater;
import org.infoglue.deliver.portal.deploy.Deploy;
import org.infoglue.deliver.portal.om.PortletApplicationEntityImpl;
import org.infoglue.deliver.portal.om.PortletApplicationEntityListImpl;
import org.infoglue.deliver.portal.om.PortletEntityImpl;
import org.infoglue.deliver.portal.om.PreferenceImpl;
import org.infoglue.deliver.portal.om.PreferenceSetImpl;

import webwork.action.ActionContext;
import webwork.multipart.MultiPartRequestWrapper;

/**
 * Upload a portlet-war. The war-file is stored as a digital asset.
 * 
 * @author jand
 *  
 */
public class UploadPortletAction extends InfoGlueAbstractAction 
{
    private static final Log log = LogFactory.getLog(UploadPortletAction.class);

    private static final String PORTLET_DEPLOY_PREFIX = "portlet.deploy";

    private DigitalAsset digitalAsset;

    public String doExecute() //throws Exception
    {
        try
		{
			MultiPartRequestWrapper mpr = ActionContext.getMultiPartRequest();
			if (mpr == null)
			{
				return "input";
			}

			log.debug("Handling upload...");
			Enumeration names = mpr.getFileNames();
			if (names.hasMoreElements())
			{
				String name = (String) names.nextElement();
				log.debug("name:" + name);
				File uploadedFile = mpr.getFile(name);
				if (uploadedFile == null || uploadedFile.length() == 0)
				{
					log.error("No file found in multipart request");
					return "input";
				}

				String contentType = mpr.getContentType(name);
				String fileName = mpr.getFilesystemName(name);
				String filePath = CmsPropertyHandler.getDigitalAssetPath();
				log.debug("fileName:" + fileName);
				
				// Pluto prepare portlet-war
				String appName = fileName;
				int dot = appName.lastIndexOf(".");
				if (dot > 0)
				{
					appName = appName.substring(0, dot);
				}

				log.info("appName:" + appName);
				
				// Create file where Deployer will write updated
				// (pluto-prepared) .war
				File file = new File(uploadedFile.getParentFile(), "tmp" + System.currentTimeMillis());
				log.info("file:" + file.getAbsolutePath());
				PortletApplicationDefinition pad = Deploy.prepareArchive(uploadedFile, file, appName);

				// Extract portlet application information to be added to
				// portlet entity registry
				log.info("Adding portlet application to registry: " + appName);
				PortletApplicationEntityImpl pae = new PortletApplicationEntityImpl();
				pae.setId(appName);

				PortletDefinitionList pdl = pad.getPortletDefinitionList();
				for (Iterator it = pdl.iterator(); it.hasNext();)
				{
					PortletDefinition pd = (PortletDefinition) it.next();
					log.debug("Adding portlet: " + pd.getName());
					PortletEntityImpl pe = new PortletEntityImpl();
					pe.setId(pd.getName());

					// Copy preferences
					ArrayList destPrefs = new ArrayList();
					PreferenceSet prefSet = pd.getPreferenceSet();
					for (Iterator prefs = prefSet.iterator(); prefs.hasNext();)
					{
						Preference src = (Preference) prefs.next();
						ArrayList destValues = new ArrayList();
						for (Iterator values = src.getValues(); values.hasNext();)
						{
							destValues.add(values.next());
						}
						destPrefs.add(new PreferenceImpl(src.getName(), destValues));
					}
					pe.setPreferenceSet(new PreferenceSetImpl(destPrefs));
					pae.addPortletEntity(pe);
				}

				// Create Digital Asset
				log.debug("Creating Digital Asset...");
				DigitalAssetVO newAsset = new DigitalAssetVO();
				newAsset.setAssetContentType(contentType);
				newAsset.setAssetKey(fileName);
				newAsset.setAssetFileName(fileName);
				newAsset.setAssetFilePath(filePath);
				newAsset.setAssetFileSize(new Integer(new Long(file.length()).intValue()));

				// Check existance of portlet and remove old ones
				List assets = PortletAssetController.getDigitalAssetByName(fileName);
				if (assets != null && assets.size() > 0)
				{
					log.info("Removing old instance of " + fileName);
					for (Iterator it = assets.iterator(); it.hasNext();)
					{
						DigitalAsset oldAsset = (DigitalAsset) it.next();
						PortletAssetController.delete(oldAsset.getId());
					}
				}

				log.info("Storing Digital Asset (portlet) " + fileName);
				InputStream is = new FileInputStream(file);
				digitalAsset = PortletAssetController.create(newAsset, is);
				is.close();
				log.debug("Digital Asset stored as id=" + digitalAsset.getId());

				// Cleanup
				uploadedFile.delete();
				file.delete();

				// Add the new application to portlet registry
				// Update persisted portlet registry
				// TODO check existance first?
				PortletApplicationEntityList pael = PortletEntityRegistry.getPortletApplicationEntityList();
				if (pael instanceof PortletApplicationEntityListImpl)
				{
					((PortletApplicationEntityListImpl) pael).add(pae);
					log.debug("Portlet application successfully added to registry");
				} else
				{
					log.error("Unknown implementation of PortletApplicationEntityList, "
							+ "cannot add portlet application!");
					return "error";
				}
				PortletEntityRegistry.store();

				// Refresh deliver-engines
				updateDeliverEngines(digitalAsset.getId());
			}
			else
			{
				throw new SystemException("No file was uploaded...");
			}
		} 
        catch (Throwable e)
		{
			log.error("ERROR", e);
			return "error";
		}

        return "success";
    }

    /**
	 * Report to deliver engines that a portlet has been uploaded
	 * 
	 * @param contentId
	 *            contentId of portlet
	 */
    private void updateDeliverEngines(Integer digitalAssetId) 
    {
    	List allUrls = CmsPropertyHandler.getInternalDeliveryUrls();
    	allUrls.addAll(CmsPropertyHandler.getPublicDeliveryUrls());
    	
    	Iterator urlIterator = allUrls.iterator();
    	while(urlIterator.hasNext())
    	{
    		String url = (String)urlIterator.next() + "/DeployPortlet.action";
    		
	        try 
	        {
	            HttpClient client = new HttpClient();
	
	            // establish a connection within 5 seconds
	            client.setConnectionTimeout(5000);
	
	            // set the default credentials
	            HttpMethod method = new GetMethod(url);
	            method.setQueryString("digitalAssetId=" + digitalAssetId);
	            method.setFollowRedirects(true);
	
	            // execute the method
	            client.executeMethod(method);
	            StatusLine status = method.getStatusLine();
	            if (status != null && status.getStatusCode() == 200) {
	                log.info("Successfully deployed portlet at " + url);
	            } else {
	                log.warn("Failed to deploy portlet at " + url + ": " + status);
	            }
	
	            //clean up the connection resources
	            method.releaseConnection();
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
    	}
    	
    	/*
        Properties props = CmsPropertyHandler.getProperties();
        for (Enumeration keys = props.keys(); keys.hasMoreElements();) {
            String key = (String) keys.nextElement();
            if (key.startsWith(PORTLET_DEPLOY_PREFIX)) {
                String url = props.getProperty(key);
                try {
                    HttpClient client = new HttpClient();

                    //establish a connection within 5 seconds
                    client.setConnectionTimeout(5000);

                    //set the default credentials
                    HttpMethod method = new GetMethod(url);
                    method.setQueryString("digitalAssetId=" + digitalAssetId);
                    method.setFollowRedirects(true);

                    //execute the method
                    client.executeMethod(method);
                    StatusLine status = method.getStatusLine();
                    if (status != null && status.getStatusCode() == 200) {
                        log.info("Successfully deployed portlet at " + url);
                    } else {
                        log.warn("Failed to deploy portlet at " + url + ": " + status);
                    }

                    //clean up the connection resources
                    method.releaseConnection();
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        */

    }

    /**
     * @return
     */
    public DigitalAsset getDigitalAsset() {
        return digitalAsset;
    }

}