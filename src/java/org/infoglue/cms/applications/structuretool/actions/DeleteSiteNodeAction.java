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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.LinkBean;
import org.infoglue.cms.applications.databeans.ReferenceBean;
import org.infoglue.cms.controllers.kernel.impl.simple.RegistryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeControllerProxy;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;


/**
 * This action removes a siteNode from the system.
 * 
 * @author Mattias Bogeblad
 */

public class DeleteSiteNodeAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 9082753568968315848L;

	private final static Logger logger = Logger.getLogger(DeleteSiteNodeAction.class.getName());

	private SiteNodeVO siteNodeVO;
	private SiteNodeVO parentSiteNodeVO;
	private Integer parentSiteNodeId;
	private Integer changeTypeId;
	private Integer repositoryId;
	private String[] registryId;

	//Used for the relatedPages control
	private Integer contentId;

	private List<ReferenceBean> referenceBeanList = new ArrayList<ReferenceBean>();
	private String returnAddress = null;
	private String originalAddress = null;

	private String userSessionKey = null;

	public String getUserSessionKey() {
		return userSessionKey;
	}

	public void setUserSessionKey(String userSessionKey) {
		this.userSessionKey = userSessionKey;
	}

	public DeleteSiteNodeAction()
	{
		this(new SiteNodeVO());
	}

	public DeleteSiteNodeAction(SiteNodeVO siteNodeVO) 
	{
		this.siteNodeVO = siteNodeVO;
	}

	protected String executeAction(boolean forceDelete) throws Exception
	{
		if (!forceDelete)
		{
			this.referenceBeanList = RegistryController.getController().getReferencingObjectsForSiteNode(this.siteNodeVO.getSiteNodeId(), CmsPropertyHandler.getOnlyShowReferenceIfLatestVersion());
		}
		if(!forceDelete && this.referenceBeanList != null && this.referenceBeanList.size() > 0)
		{
		    return "showRelations";
		}
	    else
	    {
			try
			{
				this.parentSiteNodeVO = SiteNodeController.getParentSiteNode(this.siteNodeVO.getSiteNodeId());
				this.parentSiteNodeId = this.parentSiteNodeVO.getSiteNodeId();
			}
			catch(Exception e)
			{
				logger.info("The siteNode must have been a root-siteNode because we could not find a parent.");
			}

//			if (!forceDelete && CmsPropertyHandler.getOnlyShowReferenceIfLatestVersion())
//			{
//				logger.info("Looking for and removing registry entries that has not been removed because we are in show-only-latest-version-mode.");
//				RegistryController.getController().deleteAllForSiteNode(this.siteNodeVO.getSiteNodeId(), getInfoGluePrincipal());
//			}

			SiteNodeControllerProxy.getSiteNodeControllerProxy().acMarkForDelete(this.getInfoGluePrincipal(), this.siteNodeVO, forceDelete);

			return "success";
	    }
	}

	protected String executeV3(boolean forceDelete) throws Exception
	{
		String result = NONE;

		if(this.userSessionKey == null)
			userSessionKey = "" + System.currentTimeMillis();

        try
        {
    		SiteNodeVO siteNodeVO = SiteNodeControllerProxy.getController().getSiteNodeVOWithId(this.siteNodeVO.getSiteNodeId());
    		String siteNodeName = siteNodeVO.getName();
    		logger.info("siteNodeName:" + siteNodeName + " for " + this.siteNodeVO.getSiteNodeId());
    		parentSiteNodeId = new Integer(siteNodeVO.getId());

    		result = executeAction(forceDelete);

    		String deleteSiteNodeInlineOperationDoneHeader = getLocalizedString(getLocale(), "tool.structuretool.deleteSiteNodeInlineOperationDoneHeader", new String[]{siteNodeVO.getName()});
    		String deleteSiteNodeInlineOperationViewDeletedPageParentLinkText = getLocalizedString(getLocale(), "tool.structuretool.deleteSiteNodeInlineOperationViewDeletedPageParentLinkText");
    		String deleteSiteNodeInlineOperationViewCreatedPageParentTitleText = getLocalizedString(getLocale(), "tool.structuretool.deleteSiteNodeInlineOperationViewDeletedPageParentTitleText");

    		logger.info("userSessionKey:" + userSessionKey);
    	    setActionMessage(userSessionKey, deleteSiteNodeInlineOperationDoneHeader);

    	    logger.info("originalAddress:" + originalAddress);
    	    addActionLink(userSessionKey, new LinkBean("parentPageUrl", deleteSiteNodeInlineOperationViewDeletedPageParentLinkText, deleteSiteNodeInlineOperationViewCreatedPageParentTitleText, deleteSiteNodeInlineOperationViewCreatedPageParentTitleText, this.originalAddress, false, "", "", "structure"));
            setActionExtraData(userSessionKey, "refreshToolbarAndMenu", "" + true);
            setActionExtraData(userSessionKey, "repositoryId", "" + this.siteNodeVO.getRepositoryId());
            setActionExtraData(userSessionKey, "siteNodeId", "" + this.siteNodeVO.getId());
            setActionExtraData(userSessionKey, "siteNodeName", "" + siteNodeName);
            setActionExtraData(userSessionKey, "unrefreshedSiteNodeId", "" + parentSiteNodeId);
            setActionExtraData(userSessionKey, "unrefreshedNodeId", "" + parentSiteNodeId);
            setActionExtraData(userSessionKey, "changeTypeId", "4");
            setActionExtraData(userSessionKey, "disableCloseLink", "true");
        }
        catch(ConstraintException ce)
        {
        	logger.warn("An error occurred so we should not complete the transaction:" + ce.getMessage());

        	parentSiteNodeVO = SiteNodeControllerProxy.getController().getSiteNodeVOWithId(parentSiteNodeId);

        	if(ce.getErrorCode().equalsIgnoreCase("3400"))
        	{
        		ce.setResult("showRelations");
        		//String unpublishSiteNodesInlineOperationLinkText = getLocalizedString(getLocale(), "tool.structuretool.unpublishSiteNodesInlineOperationLinkText");
        		//String unpublishSiteNodesInlineOperationTitleText = getLocalizedString(getLocale(), "tool.structuretool.unpublishSiteNodesInlineOperationTitleText");

        		//ce.getLinkBeans().add(new LinkBean("unpublishPageUrl", unpublishSiteNodesInlineOperationLinkText, unpublishSiteNodesInlineOperationTitleText, unpublishSiteNodesInlineOperationTitleText, StructureToolbarController.getUnpublishButtonLink(this.siteNodeVO.getSiteNodeId(), true), false, "", "", "inline"));
        	}

			ce.setResult(INPUT + "V3");
			throw ce;
        }
        catch(Exception e)
        {
            logger.error("An error occurred so we should not complete the transaction:" + e.getMessage(), e);
            throw new SystemException(e.getMessage());
        }

        logger.info("result:" + result);
        if(!result.startsWith("success"))
        	return result;

        if(this.returnAddress != null && !this.returnAddress.equals(""))
        {
	        String arguments 	= "userSessionKey=" + userSessionKey + "&isAutomaticRedirect=false";
	        String messageUrl 	= returnAddress + (returnAddress.indexOf("?") > -1 ? "&" : "?") + arguments;

	        this.getResponse().sendRedirect(messageUrl);
	        return NONE;
        }
        else
        {
        	return "successV3";
        }
	}

	protected String doExecute() throws Exception
	{
		return executeAction(false);
	}

	public String doV3() throws Exception 
	{
		return executeV3(false);
    }

	public String doDeleteReference() throws Exception 
	{
		RegistryController.getController().delete(registryId, this.getInfoGluePrincipal(), true, getOnlyShowLatestReferenceIfLatestVersion());

	    return executeV3(false);
	}

	public String doDeleteAllReferences() throws Exception
	{
		return executeV3(true);
	}

	public String doFixPage() throws Exception 
	{
	    return "fixPage";
	}

	public String doFixPageHeader() throws Exception 
	{
	    return "fixPageHeader";
	}

	public boolean getOnlyShowLatestReferenceIfLatestVersion()
	{
		return CmsPropertyHandler.getOnlyShowReferenceIfLatestVersion();
	}

	public void setSiteNodeId(Integer siteNodeId)
	{
		this.siteNodeVO.setSiteNodeId(siteNodeId);
	}

	public Integer getOriginalSiteNodeId()
	{
		return this.siteNodeVO.getSiteNodeId();
	}

	public void setParentSiteNodeId(Integer parentSiteNodeId)
	{
		this.parentSiteNodeId = parentSiteNodeId;
	}

	public void setChangeTypeId(Integer changeTypeId)
	{
		this.changeTypeId = changeTypeId;
	}

	public Integer getSiteNodeId()
	{
		return this.parentSiteNodeId;
	}

	public Integer getUnrefreshedSiteNodeId()
	{
		return this.parentSiteNodeId;
	}

	public Integer getChangeTypeId()
	{
		return this.changeTypeId;
	}

	public String getErrorKey()
	{
		return "SiteNodeVersion.stateId";
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

	public String getReturnAddress()
	{
		if(this.returnAddress != null && !this.returnAddress.equals(""))
			return this.returnAddress;
		else
			return "ViewSiteNode.action?siteNodeId=" + this.siteNodeVO.getId() + "&repositoryId=" + this.siteNodeVO.getRepositoryId();
	}

	public void setOriginalAddress(String originalAddress)
	{
		this.originalAddress = originalAddress;
	}

	public String getOriginalAddress()
	{
		return this.originalAddress;
	}

    public Integer getRepositoryId()
    {
        return repositoryId;
    }

    public void setRepositoryId(Integer repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    public Integer getContentId()
    {
        return contentId;
    }

    public void setContentId(Integer contentId)
    {
        this.contentId = contentId;
    }

    public List<ReferenceBean> getReferenceBeanList()
    {
        return referenceBeanList;
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
