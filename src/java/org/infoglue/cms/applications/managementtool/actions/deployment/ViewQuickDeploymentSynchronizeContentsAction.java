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

package org.infoglue.cms.applications.managementtool.actions.deployment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.sorters.ComponentDeploymentComparator;
import org.infoglue.deliver.util.webservices.DynamicWebservice;

import webwork.action.Action;

public class ViewQuickDeploymentSynchronizeContentsAction extends InfoGlueAbstractAction
{
    public final static Logger logger = Logger.getLogger(ViewQuickDeploymentSynchronizeContentsAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Integer contentId;

	private String deploymentServerName = null;
	private String password = null;
	private String synchronizationMethod = "push";

	private List<DeploymentCompareBean> deviatingContents = new ArrayList<DeploymentCompareBean>();
	private List<DeploymentCompareBean> missingComponents = new ArrayList<DeploymentCompareBean>();
	//List missingComponents = new ArrayList();

	private List localRepositories = null;
	private List remoteRepositories = null;

	public String doInput() throws Exception
    {
    	try
    	{
    		Map<String, DeploymentServerBean> deploymentServers = CmsPropertyHandler.getDeploymentServers();
	    	logger.info("deploymentServers:" + deploymentServers.size());
	    	DeploymentServerBean deploymentServerBean = deploymentServers.get(deploymentServerName);
	    	String deploymentServerUrl = deploymentServerBean.getUrl();
	    	logger.info("deploymentServerUrl:" + deploymentServerUrl);
	    	
	    	String targetEndpointAddress = deploymentServerUrl + "/services/RemoteDeploymentService";
	    	logger.info("targetEndpointAddress:" + targetEndpointAddress);

	    	localRepositories = RepositoryController.getController().getRepositoryVOListNotMarkedForDeletion();
	    	
	    	Object[] repositoryVOArray = (Object[])invokeOperation(targetEndpointAddress, "getAllRepositories", "repository", null, new Class[]{RepositoryVO.class}, "infoglue", new Class[]{RepositoryVO.class}, deploymentServerBean.getUser());
	    	remoteRepositories = Arrays.asList(repositoryVOArray);
	    	
	    	//Getting deviatingComponents
	    	Object[] contentVOArray = (Object[])invokeOperation(targetEndpointAddress, "getComponents", "content", null, new Class[]{ContentVO.class, ContentVersionVO.class}, "infoglue", new Class[]{ContentVO.class, ContentVersionVO.class}, deploymentServerBean.getUser());
	    	List remoteContentVOList = Arrays.asList(contentVOArray);
	    	//List components = ContentController.getContentController().getContentVOWithContentTypeDefinition("HTMLTemplate");
	    	ContentVO localComponentContent = ContentController.getContentController().getContentVOWithId(this.contentId);
	    	logger.info("localComponentContent:" + localComponentContent.getName());
	    	logger.info("remoteContentVOList:" + remoteContentVOList.size());
			String localFullPath = ContentController.getContentController().getContentPath(localComponentContent.getId(), true, true);

	    	boolean match = false;
	    	Iterator remoteContentVOListIterator = remoteContentVOList.iterator();
	    	while(remoteContentVOListIterator.hasNext())
	    	{
	    		ContentVO remoteContentVO = (ContentVO)remoteContentVOListIterator.next();
	    		logger.info("remoteContentVO:" + remoteContentVO.getName() + " - " + remoteContentVO.getFullPath());
	    		//logger.info("Versions:" + remoteContentVO.getContentVersion());
	    		
    			if(localFullPath.equals(remoteContentVO.getFullPath()))
    			{
    				logger.info("---------------------------------------------------MATCH");
    				match = true;
    				
		    		DeploymentCompareBean bean = new DeploymentCompareBean();
		    		bean.setRemoteVersion(remoteContentVO);
		    		if(localComponentContent != null)
		    		{
		        		bean.setLocalVersion(localComponentContent);
						LanguageVO languageVO = LanguageController.getController().getMasterLanguage(localComponentContent.getRepositoryId());
						ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(localComponentContent.getId(), languageVO.getId());
						if(contentVersionVO != null)
						{
							localComponentContent.setVersions(new String[]{contentVersionVO.getVersionValue()});
							localComponentContent.setContentVersion(contentVersionVO);
						}
		    		}
		    		deviatingContents.add(bean);
    			}
	    	}
	    	
	    	if(!match)
	    	{
	    		DeploymentCompareBean bean = new DeploymentCompareBean();
	    		if(localComponentContent != null)
	    		{
	        		bean.setLocalVersion(localComponentContent);

					localComponentContent.setFullPath(localFullPath);
					
					LanguageVO languageVO = LanguageController.getController().getMasterLanguage(localComponentContent.getRepositoryId());
					ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(localComponentContent.getId(), languageVO.getId());
					if(contentVersionVO != null)
					{
						localComponentContent.setVersions(new String[]{contentVersionVO.getVersionValue()});
						localComponentContent.setContentVersion(contentVersionVO);
					}
	    		}
	    		
	    		deviatingContents.add(bean);
	    	}
    	}
    	catch (Exception e)
    	{
    		logger.error("Error in sync tool:" + e.getMessage(), e);
		}

		return Action.INPUT;
    }

	public String doExecute() throws Exception
    {
		logger.info("Inne i execute...");
		
		try
		{
			
		Map<String, DeploymentServerBean> deploymentServers = CmsPropertyHandler.getDeploymentServers();
		DeploymentServerBean deploymentServerBean = deploymentServers.get(deploymentServerName);
		String deploymentServerUrl = deploymentServerBean.getUrl();
    	
    	String targetEndpointAddress = deploymentServerUrl + "/services/RemoteDeploymentService";
    	//logger.info("targetEndpointAddress:" + targetEndpointAddress);

		logger.info("Updating components with push....");

    	Map input = new HashMap();

    	String[] missingLocalContentIdArray = this.getRequest().getParameterValues("missingContentId");
    	logger.info("missingLocalContentIdArray:" + missingLocalContentIdArray);
    	
    	List missingComponents = new ArrayList();
    	if(missingLocalContentIdArray != null)
    	{
        	logger.info("missingLocalContentIdArray.length:" + missingLocalContentIdArray.length);

	    	for(int i=0; i<missingLocalContentIdArray.length; i++)
	    	{
	    		String missingLocalContentId = missingLocalContentIdArray[i];
	        	logger.info("missingLocalContentId:" + missingLocalContentId);
	    	    	    
	        	ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(missingLocalContentId).intValue());
	    		if(contentVO != null)
	    		{
					LanguageVO languageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
					
					String fullPath = ContentController.getContentController().getContentPath(contentVO.getId(), true, true);
					
					ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageVO.getId());
					if(contentVersionVO != null)
						contentVO.setVersions(new String[]{contentVersionVO.getVersionValue()});
					
					contentVO.setFullPath(fullPath);

					missingComponents.add(contentVO);
	    		}
	    	}
    	}

    	String[] deviatingLocalContentIdArray = this.getRequest().getParameterValues("deviatingContentId");
    	logger.info("deviatingLocalContentIdArray:" + deviatingLocalContentIdArray);
    	
    	List deviatingComponents = new ArrayList();
    	if(deviatingLocalContentIdArray != null)
    	{
	    	for(int i=0; i<deviatingLocalContentIdArray.length; i++)
	    	{
	    		String deviatingLocalContentId = deviatingLocalContentIdArray[i];
	        	logger.info("deviatingLocalContentId:" + deviatingLocalContentId);
	    	
	        	String deviatingRemoteVersionId = this.getRequest().getParameter("deviatingRemoteVersionId_" + deviatingLocalContentId);
	        	logger.info("deviatingRemoteVersionId:" + deviatingRemoteVersionId);

	    		ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(deviatingLocalContentId).intValue());
	    		if(contentVO != null)
	    		{
					LanguageVO languageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
					
					//String fullPath = ContentController.getContentController().getContentPath(contentVO.getId(), true, true);
					
					ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageVO.getId());
					if(contentVersionVO != null)
						contentVO.setVersions(new String[]{contentVersionVO.getVersionValue()});
					
					//contentVO.setFullPath(fullPath);
					contentVO.setFullPath("deviatingRemoteVersionId=" + deviatingRemoteVersionId);
					
					deviatingComponents.add(contentVO);
	    		}
	    	}
    	}

    	logger.info("missingComponents:" + missingComponents.size());
    	logger.info("deviatingComponents:" + deviatingComponents.size());

    	input.put("missingComponents", missingComponents);
    	input.put("deviatingComponents", deviatingComponents);
    	//input.put("requestMap", requestMap);
    	
    	Boolean success = (Boolean)invokeOperation(targetEndpointAddress, "updateComponents", "content", input, new Class[]{Boolean.class}, "java", new Class[]{ContentVO.class, ContentVersionVO.class}, deploymentServerBean.getUser());
    	logger.info("success:" + success);
		}
		catch (Throwable e) 
		{
			e.printStackTrace();
		}
		
		return Action.SUCCESS;
    }


	public String getSynchronizationMethod()
	{
		return synchronizationMethod;
	}

	public void setSynchronizationMethod(String synchronizationMethod)
	{
		this.synchronizationMethod = synchronizationMethod;
	}

	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}

	public void setDeploymentServerName(String deploymentServerName)
	{
		this.deploymentServerName = deploymentServerName;
	}
	
	public String getDeploymentServerName()
	{
		return this.deploymentServerName;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public String getPassword()
	{
		return this.password;
	}

	public List<DeploymentCompareBean> getDeviatingContents()
	{
		return deviatingContents;
	}

	public List<DeploymentCompareBean> getMissingComponents()
	{
		return missingComponents;
	}

	protected Object invokeOperation(String endpointAddress, String operationName, String name, Object argument, Class returnType[], String nameSpace) throws JspException
    {
		return invokeOperation(endpointAddress, operationName, name, argument, returnType, nameSpace, null, null);
    }
	
	protected Object invokeOperation(String endpointAddress, String operationName, String name, Object argument, Class[] returnType, String nameSpace, Class[] extraClassInfo, String userName) throws JspException
    {
		Object result = null;
		
        try
        {
        	InfoGluePrincipal principal = this.getInfoGluePrincipal();
        	if(userName != null && !userName.equals(""))
        		principal = new InfoGluePrincipal(userName, userName, userName, userName, "", null, null, null, false, null);
            
        	final DynamicWebservice ws = new DynamicWebservice(principal);

            ws.setTargetEndpointAddress(endpointAddress);
            ws.setOperationName(operationName);
            //ws.setReturnType(ContentVersionVO.class, new QName(nameSpace, ws.getClassName(ContentVersionVO.class)));
            for(int i=0; i<returnType.length; i++)
            	ws.setReturnType(returnType[i], new QName(nameSpace, ws.getClassName(returnType[i])));
            
            if(argument != null)
            {
	            if(argument instanceof Map || argument instanceof HashMap)
	                ws.addArgument(name, (Map)argument, extraClassInfo);
	            else if(argument instanceof List || argument instanceof ArrayList)
	                ws.addArgument(name, (List)argument, extraClassInfo);
	            else
	                ws.addArgument(name, argument);
            }
            
            ws.callService();
            result = ws.getResult();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new JspTagException(e.getMessage());
        }
        
        return result;
    }

}
