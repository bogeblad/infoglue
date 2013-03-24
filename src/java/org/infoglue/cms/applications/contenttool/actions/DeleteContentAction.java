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

package org.infoglue.cms.applications.contenttool.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.InconsistenciesController;
import org.infoglue.cms.controllers.kernel.impl.simple.RegistryController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.RegistryVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

/**
 * This action removes a content from the system.
 * 
 * @author Mattias Bogeblad
 */

public class DeleteContentAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(DeleteContentAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private ContentVO contentVO;
	private Integer parentContentId;
	private Integer changeTypeId;
	private String[] registryId;
	
	//Used for the relatedPages control
	private Integer siteNodeId;
	
	private String returnAddress = null;
	private String originalAddress = null;
	private List referenceBeanList = new ArrayList();
	
	private String userSessionKey = null;
	
	public String getUserSessionKey() {
		return userSessionKey;
	}

	public void setUserSessionKey(String userSessionKey) {
		this.userSessionKey = userSessionKey;
	}

	public DeleteContentAction()
	{
		this(new ContentVO());
	}

	public DeleteContentAction(ContentVO contentVO) 
	{
		this.contentVO = contentVO;
	}
	
	public String doExecute() throws Exception 
	{
		this.referenceBeanList = RegistryController.getController().getReferencingObjectsForContent(this.contentVO.getContentId());

		if(this.referenceBeanList != null && this.referenceBeanList.size() > 0)
		{
		    return "showRelations";
		}
	    else
	    {
	    	try
			{
				this.parentContentId = ContentController.getParentContent(this.contentVO.getContentId()).getContentId();
			}
			catch(Exception e)
			{
				logger.info("The content must have been a root-content because we could not find a parent.");
			}

	    	//ContentControllerProxy.getController().acDelete(this.getInfoGluePrincipal(), this.contentVO);	    
    		ContentControllerProxy.getController().acMarkForDelete(this.getInfoGluePrincipal(), this.contentVO);	    

	    	return "success";
	    }
	}	
	
	public String doV3() throws Exception 
	{
		String result = NONE;
		
		if(this.userSessionKey == null)
			userSessionKey = "" + System.currentTimeMillis();

        try
        {
        	ContentVO contentVO = ContentController.getContentController().getContentVOWithId(this.contentVO.getContentId());
        	if(ContentController.getContentController().hasPublishedVersion(this.contentVO.getContentId()))
        	{
        		throw new ConstraintException("ContentVersion.stateId", "3300", contentVO.getName());
        	}
        	
        	String contentName = contentVO.getName();
        	parentContentId = new Integer(contentVO.getId());
        	
        	result = doExecute();
            
    		String deleteContentInlineOperationDoneHeader = getLocalizedString(getLocale(), "tool.contenttool.deleteContentInlineOperationDoneHeader", contentName);
    		String deleteContentInlineOperationViewDeletedContentParentLinkText = getLocalizedString(getLocale(), "tool.contenttool.deleteContentInlineOperationViewDeletedContentParentLinkText");
    		String deleteContentInlineOperationViewDeletedContentParentTitleText = getLocalizedString(getLocale(), "tool.contenttool.deleteContentInlineOperationViewDeletedContentParentTitleText");
    	
    	    setActionMessage(userSessionKey, deleteContentInlineOperationDoneHeader);
    										  																	
    	    logger.debug("originalAddress:" + originalAddress);
    	    addActionLink(userSessionKey, new LinkBean("parentContentUrl", deleteContentInlineOperationViewDeletedContentParentLinkText, deleteContentInlineOperationViewDeletedContentParentTitleText, deleteContentInlineOperationViewDeletedContentParentTitleText, this.originalAddress, false, "", "", "content"));
            setActionExtraData(userSessionKey, "refreshToolbarAndMenu", "" + true);
            setActionExtraData(userSessionKey, "repositoryId", "" + this.contentVO.getRepositoryId());
            setActionExtraData(userSessionKey, "contentId", "" + this.contentVO.getId());
            setActionExtraData(userSessionKey, "unrefreshedContentId", "" + parentContentId);
            setActionExtraData(userSessionKey, "unrefreshedNodeId", "" + parentContentId);
            setActionExtraData(userSessionKey, "changeTypeId", "4");
            setActionExtraData(userSessionKey, "disableCloseLink", "true");
        }
        catch(ConstraintException ce)
        {
        	logger.warn("An error occurred so we should not complete the transaction:" + ce);
			ce.setResult(INPUT + "V3");
			throw ce;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e, e);
            throw new SystemException(e.getMessage());
        }

        logger.debug("result:" + result);
        if(!result.startsWith("success"))
        	return result;
        	
        logger.debug("returnAddress:" + returnAddress);
        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
	        String arguments 	= "userSessionKey=" + userSessionKey + "&isAutomaticRedirect=false";
	        String messageUrl 	= returnAddress + (returnAddress.indexOf("?") > -1 ? "&" : "?") + arguments;
	        logger.debug("messageUrl:" + messageUrl);
	        this.getResponse().sendRedirect(messageUrl);
	        return NONE;
        }
        else
        {
        	return "successV3";
        }
    }

	public String doStandalone() throws Exception 
	{
		this.referenceBeanList = RegistryController.getController().getReferencingObjectsForContent(this.contentVO.getContentId());
		if(this.referenceBeanList != null && this.referenceBeanList.size() > 0)
		{
		    return "showRelations";
		}
	    else
	    {
	    	try
			{
				this.parentContentId = ContentController.getParentContent(this.contentVO.getContentId()).getContentId();
			}
			catch(Exception e)
			{
			    logger.info("The content must have been a root-content because we could not find a parent.");
			}

	    	//ContentControllerProxy.getController().acDelete(this.getInfoGluePrincipal(), this.contentVO);	    
	    	ContentControllerProxy.getController().acMarkForDelete(this.getInfoGluePrincipal(), this.contentVO);	    
			
	    	return "successStandalone";
	    }
	}	

	public String doDeleteReference() throws Exception 
	{
	    for(int i=0; i<registryId.length; i++)
	    {
	    	try
	    	{
	    		InconsistenciesController.getController().removeReferences(new Integer(registryId[i]), this.getInfoGluePrincipal());
	    	}
	    	catch(Exception e)
	    	{
	    		logger.error("An error occurred when we tried to delete references: " + e.getMessage());
	    	}
	    	
	    	try
	    	{
	    		RegistryVO registryVO = RegistryController.getController().getRegistryVOWithId(new Integer(registryId[i]));
	    		if(registryVO != null)
	    			RegistryController.getController().delete(new Integer(registryId[i]));
	    	}
	    	catch(Exception e)
	    	{
	    		logger.error("An error occurred when we tried to delete references: " + e.getMessage());
	    	}
	    }
	    
	    return doV3();
	}	
	
	public String doFixPage() throws Exception 
	{
	    return "fixPage";
	}

	public String doFixPageHeader() throws Exception 
	{
	    return "fixPageHeader";
	}

	public void setContentId(Integer contentId)
	{
		this.contentVO.setContentId(contentId);
	}

	public void setParentContentId(Integer parentContentId)
	{
		this.parentContentId = parentContentId;
	}

	public void setChangeTypeId(Integer changeTypeId)
	{
		this.changeTypeId = changeTypeId;
	}

	public Integer getContentId()
	{
		return this.parentContentId;
	}
	
	public Integer getOriginalContentId()
	{
		return this.contentVO.getContentId();
	}
	
	public Integer getUnrefreshedContentId()
	{
		return this.parentContentId;
	}
	
	public Integer getChangeTypeId()
	{
		return this.changeTypeId;
	}
        
    public String getErrorKey()
	{
		return "ContentVersion.stateId";
	}
	
	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

	public void setOriginalAddress(String originalAddress)
	{
		this.originalAddress = originalAddress;
	}

	public String getOriginalAddress()
	{
		return this.originalAddress;
	}

	public String getReturnAddress()
	{
		if(this.returnAddress != null && !this.returnAddress.equals(""))
			return this.returnAddress;
		else
			return "ViewContent.action?contentId=" + this.contentVO.getId() + "&repositoryId=" + this.contentVO.getRepositoryId();
	}

    public List getReferenceBeanList()
    {
        return referenceBeanList;
    }
    
    public Integer getSiteNodeId()
    {
        return siteNodeId;
    }
    
    public void setSiteNodeId(Integer siteNodeId)
    {
        this.siteNodeId = siteNodeId;
    }
    
    public String[] getRegistryId()
    {
        return registryId;
    }
    
    public void setRegistryId(String[] registryId)
    {
        this.registryId = registryId;
    }
    
}
