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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DeploymentController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryLanguageController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.Language;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.CmsPropertyHandler;

public class ViewVCDeploymentAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewVCDeploymentAction.class.getName());

	private static final long serialVersionUID = 1L;
		
	private Map<String,VersionControlServerBean> vcServers = new HashMap<String,VersionControlServerBean>();
	private String vcServerName = null;
	private String vcPassword = null;
	private List<String> tags = new ArrayList<String>();
	private String tagName = null;
	private List<DeploymentCompareBean> deviatingContents = new ArrayList<DeploymentCompareBean>();
	private boolean repositoryCreated = false;
	
	public String doInput() throws Exception
    {
    	this.vcServers = CmsPropertyHandler.getVCServers();
    	
    	return "input";
    }

    public String doInputChooseTag() throws Exception
    {
    	this.vcServers = CmsPropertyHandler.getVCServers();
    	
    	if(vcServerName != null && !vcServerName.equals(""))
    	{
	    	VersionControlServerBean serverBean = this.vcServers.get(vcServerName);
	    	if(serverBean != null)
	    	{
	    		if(this.vcPassword != null)
	    			serverBean.setPassword(this.vcPassword);
	    		this.tags = DeploymentController.getAvailableTags(serverBean);
	    		
	    	}
    	}

    	return "inputChooseTag";
    }

    public String doInputVerifyCheckout() throws Exception
    {
    	this.vcServers = CmsPropertyHandler.getVCServers();
    	
    	if(vcServerName != null && !vcServerName.equals(""))
    	{
    		logger.info("vcServerName:" + vcServerName);
	    	VersionControlServerBean serverBean = this.vcServers.get(vcServerName);
	    	if(serverBean != null)
	    	{
	    		logger.info("tagName:" + tagName);
	    		if(this.vcPassword != null)
	    			serverBean.setPassword(this.vcPassword);
	    		
    			this.deviatingContents = DeploymentController.getDeploymentComparisonBeans(serverBean, tagName, getInfoGluePrincipal());
	    	}
	    }
    	
    	return "inputVerifyCheckout";
    }
    
    
    public String doExecute() throws Exception
    {
    	ContentTypeDefinitionVO ctd = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("HTMLTemplate");
    	ContentTypeDefinitionVO ctdFolder = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("Folder");
    	
    	String[] missingRemoteContentPathArray = this.getRequest().getParameterValues("missingRemoteContentPath");
    	System.out.println("missingRemoteContentPathArray:" + missingRemoteContentPathArray);
    	
    	//List deviatingComponents = new ArrayList();
    	if(missingRemoteContentPathArray != null)
    	{
	    	for(int i=0; i<missingRemoteContentPathArray.length; i++)
	    	{
	    		String missingRemoteContentPath = missingRemoteContentPathArray[i];
	    		logger.info("missingRemoteContentPath:" + missingRemoteContentPath);
	    	
	    		File missingRemoteContentFile = new File(missingRemoteContentPath);

	    		String templateLanguageCode = null;
    			String plainName = missingRemoteContentFile.getName();
    			if(plainName.indexOf(".xml") > -1)
    			{
    				plainName = plainName.substring(0, plainName.indexOf(".xml"));
    				logger.info("Plain name:" + plainName);
    				if(plainName.lastIndexOf("_") > -1)
	    			{
    					templateLanguageCode = plainName.substring(plainName.lastIndexOf("_") + 1);
    					plainName = plainName.substring(0, plainName.lastIndexOf("_"));
    					logger.info("templateLanguageCode:" + templateLanguageCode);
    					logger.info("Plain name:" + plainName);
	    			}
    			}

	    		String repositoryName = getRepositoryNameFromCheckoutFile(missingRemoteContentFile);
	    		logger.info("repositoryName:" + repositoryName);
	    		logger.info("templateLanguageCode:" + templateLanguageCode);
				RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithName(repositoryName);
				LanguageVO masterLanguageVO = LanguageController.getController().getLanguageVOWithCode(templateLanguageCode);
				
				
				ContentVO localParentContentVO = null;
				if(repositoryVO == null)
				{
					RepositoryVO newRepositoryVO = new RepositoryVO();
					newRepositoryVO.setName(repositoryName);
					newRepositoryVO.setDnsName("undefined");
					newRepositoryVO.setDescription("Autogenerated during cvs sync");

					repositoryVO = RepositoryController.getController().create(newRepositoryVO);
					if(masterLanguageVO == null)
						masterLanguageVO = (LanguageVO)LanguageController.getController().getLanguageVOList().get(0);
					
					RepositoryLanguageController.getController().createRepositoryLanguage(repositoryVO.getId(), masterLanguageVO.getId(), 0);
					localParentContentVO = ContentController.getContentController().getRootContentVO(repositoryVO.getId(), getInfoGluePrincipal().getName(), true);
					this.repositoryCreated = true;
				}
				else
				{
					logger.info("Found repository:" + repositoryVO.getName());
					localParentContentVO = ContentController.getContentController().getRootContentVO(repositoryVO.getId(), getInfoGluePrincipal().getName(), true);
					masterLanguageVO = LanguageController.getController().getMasterLanguage(repositoryVO.getId());
				}
				
				String contentPath = getContentPathAsListFromCheckoutFile(missingRemoteContentFile);
				logger.info("contentPath:" + contentPath);
				
				ContentVO contentVO = ContentController.getContentController().getContentVOWithPath(repositoryVO.getId(), contentPath, true, getInfoGluePrincipal());
	    		if(contentVO != null)
	    		{
	    			if(missingRemoteContentFile.isFile())
	    			{
		    			ContentVO newContentVO = new ContentVO();
		    			newContentVO.setCreatorName(getInfoGluePrincipal().getName());
		    			newContentVO.setIsBranch(false);
		    			newContentVO.setName(plainName);
		    			ContentVO newlyCreatedContentVO = ContentController.getContentController().create(contentVO.getId(), ctd.getContentTypeDefinitionId(), contentVO.getRepositoryId(), newContentVO);
		    			
		    			logger.info("Created content:" + newlyCreatedContentVO.getName());
	
			    		String fileContent = "";
			    		if(missingRemoteContentFile.exists())
			    			fileContent = FileHelper.getFileAsString(missingRemoteContentFile, "iso-8859-1");
						
		    			ContentVersionVO newContentVersionVO = new ContentVersionVO();
		    			newContentVersionVO.setVersionComment("Checked out from version control system (tag: " + tagName + ")");
		    			newContentVersionVO.setVersionModifier(getInfoGluePrincipal().getName());
		    			newContentVersionVO.setVersionValue(fileContent);
		    			ContentVersionController.getContentVersionController().create(newlyCreatedContentVO.getId(), masterLanguageVO.getId(), newContentVersionVO, null);
	    			}
	    			else
	    			{
		    			ContentVO newContentVO = new ContentVO();
		    			newContentVO.setCreatorName(getInfoGluePrincipal().getName());
		    			newContentVO.setIsBranch(true);
		    			newContentVO.setName(plainName);
		    			ContentVO newlyCreatedContentVO = ContentController.getContentController().create(contentVO.getId(), ctdFolder.getContentTypeDefinitionId(), contentVO.getRepositoryId(), newContentVO);
		    			
		    			logger.info("Created content:" + newlyCreatedContentVO.getName());
	    			}
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
	    	
	        	String deviatingFilePath = this.getRequest().getParameter("deviatingRemoteVersionId_" + deviatingLocalContentId);
	        	logger.info("deviatingFilePath:" + deviatingFilePath);
	        	
	        	ContentVO contentVO = ContentController.getContentController().getContentVOWithId(new Integer(deviatingLocalContentId).intValue());
	    		if(contentVO != null)
	    		{
					LanguageVO languageVO = LanguageController.getController().getMasterLanguage(contentVO.getRepositoryId());
					ContentVersionVO contentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(contentVO.getId(), languageVO.getId());
					String fileContent = FileHelper.getFileAsString(new File(deviatingFilePath), "iso-8859-1");
					
					contentVersionVO.setVersionValue(fileContent);
					contentVersionVO.setVersionComment("Checked out from version control system (tag: " + tagName + ")");
					
					logger.info("We are going to replace local content: " + contentVO.getName() + " with contents in " + deviatingFilePath);
					ContentVersionController.getContentVersionController().update(contentVersionVO.getContentId(), contentVersionVO.getLanguageId(), contentVersionVO, getInfoGluePrincipal());
				}
	    	}
    	}
    	
       	return "success";
    }

	private String getRepositoryNameFromCheckoutFile(File missingRemoteContentFile)
	{
		String name = missingRemoteContentFile.getName();
		String parentParentName = missingRemoteContentFile.getParentFile().getParentFile().getName();
		logger.info("parentParentName1:" + parentParentName);
		String previousMissingRemoteContentParentParentName = parentParentName;
		while(parentParentName != null && !parentParentName.equalsIgnoreCase("checkout"))
		{
			previousMissingRemoteContentParentParentName = parentParentName;
			logger.info("parentParentName2:" + parentParentName);
			missingRemoteContentFile = missingRemoteContentFile.getParentFile();
			parentParentName = missingRemoteContentFile.getParentFile().getParentFile().getName();
			logger.info("parentParentName3:" + parentParentName);
		}
		
		logger.info("Found repositoryName:" + previousMissingRemoteContentParentParentName);
		
		return previousMissingRemoteContentParentParentName;
	}

	private String getContentPathAsListFromCheckoutFile(File missingRemoteContentFile)
	{
		StringBuffer contentPath = new StringBuffer();
		
		String name = missingRemoteContentFile.getName();
		//contentPath.append(name);
		
		String parentParentParentName = missingRemoteContentFile.getParentFile().getParentFile().getParentFile().getName();
		while(!parentParentParentName.equalsIgnoreCase("checkout"))
		{
			if(!missingRemoteContentFile.getName().equals(name))
			{
				logger.info("missingRemoteContentFile:" + missingRemoteContentFile.getName());
				contentPath.insert(0, missingRemoteContentFile.getName() + "/");
			}

			missingRemoteContentFile = missingRemoteContentFile.getParentFile();
			parentParentParentName = missingRemoteContentFile.getParentFile().getParentFile().getParentFile().getName();
		}
		
		logger.info("Found contentPath:" + contentPath);
		
		return contentPath.toString();
	}

	public Map<String,VersionControlServerBean> getVcServers() 
	{
		return vcServers;
	}

	public String getVcServerName() 
	{
		return vcServerName;
	}

	public void setVcServerName(String vcServerName) 
	{
		this.vcServerName = vcServerName;
	}

	public List getTags() 
	{
		return tags;
	}

	public void setTags(List tags) 
	{
		this.tags = tags;
	}

	public String getTagName() 
	{
		return tagName;
	}

	public void setTagName(String tagName) 
	{
		this.tagName = tagName;
	}

	public List<DeploymentCompareBean> getDeviatingContents() 
	{
		return deviatingContents;
	}

	public void setVcPassword(String vcPassword)
	{
		this.vcPassword = vcPassword;
	}

	public String getVcPassword()
	{
		return this.vcPassword;
	}
	
	public boolean getRepositoryCreated()
	{
		return this.repositoryCreated;
	}
}
