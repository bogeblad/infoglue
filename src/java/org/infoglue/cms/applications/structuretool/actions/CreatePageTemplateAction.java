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

package org.infoglue.cms.applications.structuretool.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.contenttool.actions.ViewContentTreeActionInterface;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import webwork.action.Action;
import webwork.action.ActionContext;
import webwork.multipart.MultiPartRequestWrapper;

/**
 * This action represents the CreatePageTemplate Usecase.
 */

public class CreatePageTemplateAction extends InfoGlueAbstractAction implements ViewContentTreeActionInterface
{
    private final static Logger logger = Logger.getLogger(CreatePageTemplateAction.class.getName());

	//Used by the tree only
	private List repositories;
	private Integer contentId;
	private String tree;
	private String hideLeafs;
	
	private Integer parentContentId;
	private Integer repositoryId;
	private Integer componentId;
	private Integer pagePartContentId;
	private Boolean attemptDirectPublication;
	private ConstraintExceptionBuffer ceb;

	private Integer siteNodeId;
	private String name;
	private String groupName = "";
	
	private String returnAddress;

	
    public String doInput() throws Exception
    {
		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);

        return Action.INPUT;
    }

    public String doExecute() throws Exception
    {
        logger.info("contentId:" + contentId);
        logger.info("parentContentId:" + parentContentId);
        logger.info("repositoryId:" + repositoryId);
        logger.info("siteNodeId:" + siteNodeId);
        logger.info("name:" + name);
        
        ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("PageTemplate");
        if(componentId != null)
            contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName("PagePartTemplate");

        if(contentTypeDefinitionVO == null)
        {
        	if(componentId != null)
        	{
        		String xml = FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "cms/defaults/contenttypes/PagePartTemplate.xml"));
        		ContentTypeDefinitionVO newContentTypeDefinitionVO = new ContentTypeDefinitionVO();
        		newContentTypeDefinitionVO.setName("PagePartTemplate");
        		newContentTypeDefinitionVO.setSchemaValue(xml);
        		newContentTypeDefinitionVO.setType(ContentTypeDefinitionVO.CONTENT);
        		
        		contentTypeDefinitionVO = ContentTypeDefinitionController.getController().create(newContentTypeDefinitionVO);
        	}
        	else
        	{
        		String xml = FileHelper.getFileAsString(new File(CmsPropertyHandler.getContextRootPath() + "cms/defaults/contenttypes/PageTemplate.xml"));
        		ContentTypeDefinitionVO newContentTypeDefinitionVO = new ContentTypeDefinitionVO();
        		newContentTypeDefinitionVO.setName("PageTemplate");
        		newContentTypeDefinitionVO.setSchemaValue(xml);
        		newContentTypeDefinitionVO.setType(ContentTypeDefinitionVO.CONTENT);
        		
        		contentTypeDefinitionVO = ContentTypeDefinitionController.getController().create(newContentTypeDefinitionVO);
        	}
        	//throw new SystemException("The system does not have the content type named 'PageTemplate' which is required for this operation.");
        }
                
        ContentVO contentVO = new ContentVO();
        
		contentVO.setCreatorName(this.getInfoGluePrincipal().getName());
		contentVO.setIsBranch(new Boolean(false));
		contentVO.setName(name);
		contentVO.setRepositoryId(this.repositoryId);

		contentVO = ContentControllerProxy.getController().create(parentContentId, contentTypeDefinitionVO.getId(), this.repositoryId, contentVO);
		
		String componentStructure = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><components></components>";
		
		ContentVO metaInfoContentVO = ContentController.getContentController().getContentVOWithId(this.contentId);
		Integer originalMetaInfoMasterLanguageId = LanguageController.getController().getMasterLanguage(metaInfoContentVO.getRepositoryId()).getId();
		Integer destinationMasterLanguageId = LanguageController.getController().getMasterLanguage(this.repositoryId).getId();
		ContentVersionVO originalContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(this.contentId, originalMetaInfoMasterLanguageId);
		logger.info("originalMetaInfoMasterLanguageId:" + originalMetaInfoMasterLanguageId);
		logger.info("contentId:" + contentId);
		logger.info("originalContentVersionVO:" + originalContentVersionVO);
		
	    componentStructure = ContentVersionController.getContentVersionController().getAttributeValue(originalContentVersionVO.getId(), "ComponentStructure", false);
	    logger.info("componentStructure:" + componentStructure);
		if(componentId != null)
		{
			logger.info("We should strip all but componentId:" + componentId);
			Document document = XMLHelper.readDocumentFromByteArray(componentStructure.getBytes("UTF-8"));
			String componentXPath = "//component[@id=" + componentId + "]";

			Node node = org.apache.xpath.XPathAPI.selectSingleNode(document.getDocumentElement(), componentXPath);
			if(node != null)
			{
				Element component = (Element)node;
				component.setAttribute("pagePartTemplateContentId", "-1");
				component.setAttribute("isInherited", "true");
					
				String modifiedXML = XMLHelper.serializeDom(component, new StringBuffer()).toString(); 
				logger.info("modifiedXML:" + modifiedXML);
				componentStructure = "<?xml version='1.0' encoding='UTF-8'?><components>" + modifiedXML + "</components>";
			}
		}
	    
		//Create initial content version also... in masterlanguage
		String versionValue = "<?xml version='1.0' encoding='UTF-8'?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes><Name><![CDATA[" + this.name + "]]></Name><GroupName><![CDATA[" + this.groupName + "]]></GroupName><ComponentStructure><![CDATA[" + componentStructure + "]]></ComponentStructure></attributes></article>";
	
		ContentVersionVO contentVersionVO = new ContentVersionVO();
		contentVersionVO.setVersionComment("Saved page template");
		contentVersionVO.setVersionModifier(this.getInfoGluePrincipal().getName());
		contentVersionVO.setVersionValue(versionValue);
		ContentVersionVO newContentVersion = ContentVersionController.getContentVersionController().create(contentVO.getId(), destinationMasterLanguageId, contentVersionVO, null);
        
		
    	InputStream is = null;
		File file = null;
		
    	try 
    	{
    		MultiPartRequestWrapper mpr = ActionContext.getContext().getMultiPartRequest();
    		logger.info("mpr:" + mpr);
    		if(mpr != null)
    		{ 
	    		Enumeration names = mpr.getFileNames();
	         	while (names.hasMoreElements()) 
	         	{
	            	String name 		  = (String)names.nextElement();
					String contentType    = mpr.getContentType(name);
					String fileSystemName = mpr.getFilesystemName(name);
					
					logger.info("name:" + name);
					logger.info("contentType:" + contentType);
					logger.info("fileSystemName:" + fileSystemName);
	            	
	            	file = mpr.getFile(name);
	            	if(file != null)
	            	{
						String fileName = fileSystemName;
						//fileName = new VisualFormatter().replaceNonAscii(fileName, '_');
		            	fileName = new VisualFormatter().replaceNiceURINonAsciiWithSpecifiedChars(fileName, CmsPropertyHandler.getNiceURIDefaultReplacementCharacter());

						String tempFileName = "tmp_" + System.currentTimeMillis() + "_" + fileName;
		            	String filePath = CmsPropertyHandler.getDigitalAssetPath();
		            	fileSystemName = filePath + File.separator + tempFileName;
		            	
		            	DigitalAssetVO newAsset = new DigitalAssetVO();
						newAsset.setAssetContentType(contentType);
						newAsset.setAssetKey("thumbnail");
						newAsset.setAssetFileName(fileName);
						newAsset.setAssetFilePath(filePath);
						newAsset.setAssetFileSize(new Integer(new Long(file.length()).intValue()));
						is = new FileInputStream(file);
						
					    DigitalAssetController.create(newAsset, is, newContentVersion.getContentVersionId(), this.getInfoGluePrincipal());	         		    
	            	}
	            }
    		}
    		else
    		{
    		    logger.error("File upload failed for some reason.");
    		}
      	} 
      	catch (Exception e) 
      	{
      		logger.error("An error occurred when we tried to upload a new asset:" + e.getMessage(), e);
      	}
		finally
		{
			try
			{
				is.close();
				file.delete();
			}
			catch(Exception e){}
		}

		
		
        return Action.SUCCESS;
    }
    
    public String doUpdate() throws Exception
    {
        logger.info("pagePartContentId:" + pagePartContentId);
        logger.info("contentId:" + contentId);
        logger.info("componentId:" + componentId);
        
        ContentVO pagePartContentVO = ContentController.getContentController().getContentVOWithId(pagePartContentId);
		Integer pagePartMasterLanguageId = LanguageController.getController().getMasterLanguage(pagePartContentVO.getRepositoryId()).getId();
		ContentVersionVO pagePartContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(pagePartContentId, pagePartMasterLanguageId);
		Locale pagePartMasterLocale = LanguageController.getController().getLocaleWithId(pagePartMasterLanguageId);
		
		ContentVO metaInfoContentVO = ContentController.getContentController().getContentVOWithId(this.contentId);
		Integer originalMetaInfoMasterLanguageId = LanguageController.getController().getMasterLanguage(metaInfoContentVO.getRepositoryId()).getId();
		ContentVersionVO originalContentVersionVO = ContentVersionController.getContentVersionController().getLatestActiveContentVersionVO(this.contentId, originalMetaInfoMasterLanguageId);
		logger.info("originalMetaInfoMasterLanguageId:" + originalMetaInfoMasterLanguageId);
		logger.info("contentId:" + contentId);
		logger.info("originalContentVersionVO:" + originalContentVersionVO);
		
	    String componentStructure = ContentVersionController.getContentVersionController().getAttributeValue(originalContentVersionVO.getId(), "ComponentStructure", false);
	    logger.info("componentStructure:" + componentStructure);
		if(componentId != null)
		{
			logger.info("We should strip all but componentId:" + componentId);
			Document document = XMLHelper.readDocumentFromByteArray(componentStructure.getBytes("UTF-8"));
			String componentXPath = "//component[@id=" + componentId + "]";

			Node node = org.apache.xpath.XPathAPI.selectSingleNode(document.getDocumentElement(), componentXPath);
			if(node != null)
			{
				Element component = (Element)node;
				component.setAttribute("pagePartTemplateContentId", "-1");
				component.setAttribute("isInherited", "true");
				/*
				NodeList propertiesNL = component.getElementsByTagName("properties");
				if(propertiesNL != null && propertiesNL.getLength() > 0)
				{
					Node propertiesNode = propertiesNL.item(0);
					addPropertyElement((Element)propertiesNode, "pagePartContentId", "" + pagePartContentId, "textfield", pagePartMasterLocale);
				}
				*/
				String modifiedXML = XMLHelper.serializeDom(component, new StringBuffer()).toString(); 
				logger.info("modifiedXML:" + modifiedXML);
				componentStructure = "<?xml version='1.0' encoding='UTF-8'?><components>" + modifiedXML + "</components>";
			}
		}
	    
		String versionValue = "<?xml version='1.0' encoding='UTF-8'?><article xmlns=\"x-schema:ArticleSchema.xml\"><attributes><Name><![CDATA[" + this.name + "]]></Name><GroupName><![CDATA[" + this.groupName + "]]></GroupName><ComponentStructure><![CDATA[" + componentStructure + "]]></ComponentStructure></attributes></article>";
	
		ContentVersionVO contentVersionVO = pagePartContentVersionVO;
		contentVersionVO.setVersionComment("Saved page template");
		contentVersionVO.setVersionModifier(this.getInfoGluePrincipal().getName());
		contentVersionVO.setVersionValue(versionValue);
		ContentVersionVO updateContentVersionVO = ContentVersionController.getContentVersionController().update(pagePartContentVO.getId(), pagePartMasterLanguageId, contentVersionVO, this.getInfoGluePrincipal());

		if(this.attemptDirectPublication)
		{
			List events = new ArrayList();

			ContentVersion contentVersion = ContentStateController.changeState(updateContentVersionVO.getId(), ContentVersionVO.PUBLISH_STATE, "Auto publish", false, null, this.getInfoGluePrincipal(), null, events);

		    PublicationVO publicationVO = new PublicationVO();
		    publicationVO.setName("Direct publication by " + this.getInfoGluePrincipal().getName());
		    publicationVO.setDescription("Direct publication");
		    publicationVO.setRepositoryId(pagePartContentVO.getRepositoryId());
		    publicationVO = PublicationController.getController().createAndPublish(publicationVO, events, false, this.getInfoGluePrincipal());
		}
		
        return Action.SUCCESS;
    }
    
	public Integer getTopRepositoryId() throws ConstraintException, SystemException, Bug
	{
		List repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
		
		Integer topRepositoryId = null;

		if (repositoryId != null)
			topRepositoryId = repositoryId;

		if(repositories.size() > 0)
		{
			topRepositoryId = ((RepositoryVO)repositories.get(0)).getRepositoryId();
		}
  	
		return topRepositoryId;
	}
  
	public void setHideLeafs(String hideLeafs)
	{
		this.hideLeafs = hideLeafs;
	}

	public String getHideLeafs()
	{
		return this.hideLeafs;
	}    

	public String getTree()
	{
		return tree;
	}

	public void setTree(String tree)
	{
		this.tree = tree;
	}

	public void setParentContentId(Integer parentContentId)
	{
		this.parentContentId = parentContentId;
	}

	public Integer getParentContentId()
	{
		return this.parentContentId;
	}
	
	public List getRepositories()
	{
		return this.repositories;
	}  

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public Integer getRepositoryId() 
	{
		try
		{
			if(this.repositoryId == null)
			{	
				this.repositoryId = (Integer)getHttpSession().getAttribute("repositoryId");
					
				if(this.repositoryId == null)
				{
					this.repositoryId = getTopRepositoryId();
					getHttpSession().setAttribute("repositoryId", this.repositoryId);		
				}
			}
		}
		catch(Exception e)
		{
		}
	    	
		return repositoryId;
	}

	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}

	public Integer getContentId()
	{
		return this.contentId;
	}    
	
	public String getReturnAddress()
	{
		return returnAddress;
	}

	public void setReturnAddress(String string)
	{
		returnAddress = string;
	}
    
	public Integer getSiteNodeId()
    {
        return siteNodeId;
    }

    public void setSiteNodeId(Integer siteNodeId)
    {
        this.siteNodeId = siteNodeId;
    }
    
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public String getGroupName()
    {
        return groupName;
    }
    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

	public Integer getComponentId()
	{
		return componentId;
	}

	public void setComponentId(Integer componentId)
	{
		this.componentId = componentId;
	}

	public Integer getPagePartContentId()
	{
		return pagePartContentId;
	}

	public void setPagePartContentId(Integer pagePartContentId)
	{
		this.pagePartContentId = pagePartContentId;
	}

	public void setAttemptDirectPublication(Boolean attemptDirectPublication)
	{
		this.attemptDirectPublication = attemptDirectPublication;
	}
	
	
	/**
	 * This method creates a parameter for the given input type.
	 * This is to support form steering information later.
	 */
	
	private Element addPropertyElement(Element parent, String name, String path, String type, Locale locale)
	{
		Element element = parent.getOwnerDocument().createElement("property");
		element.setAttribute("name", name);
		
		if(type.equalsIgnoreCase("siteNodeBinding") || type.equalsIgnoreCase("contentBinding"))
		{
			element.setAttribute("path", path);
			element.setAttribute("path_" + locale.getLanguage(), path);
		}
		else
		{
			element.setAttribute("path_" + locale.getLanguage(), path);
		}
		
		element.setAttribute("type", type);
		parent.appendChild(element);
		return element;
	}
}
