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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.controllers.kernel.impl.simple.LabelController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeControllerProxy;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.dom.DOMBuilder;

/**
 * This action represents the CreateSiteNode Usecase.
 */

public class CopyMultipleSiteNodeAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 955902951432881226L;

	private final static Logger logger = Logger.getLogger(CopyMultipleSiteNodeAction.class.getName());

   	//  Initial params
    private Integer originalSiteNodeId;
    private Integer repositoryId;
    private Integer siteNodeId;
    private Integer parentSiteNodeId;
    private List qualifyers = new ArrayList();
    private boolean errorsOccurred = false;
	protected List repositories = null;
	private Integer sortLanguageId;
    
    //Move params
    protected String qualifyerXML = null;
    private Integer newParentSiteNodeId;
    
    //Tree params
    private Integer changeTypeId;
    private Integer topSiteNodeId;

    private ConstraintExceptionBuffer ceb;
   	private SiteNodeVO siteNodeVO;
   	
   	
   	private String userSessionKey;
    private String originalAddress;
    private String returnAddress;

	private String processId = null;
	private int processStatus = -1;
  
  	public CopyMultipleSiteNodeAction()
	{
		this(new SiteNodeVO());
	}
	
	public CopyMultipleSiteNodeAction(SiteNodeVO siteNodeVO)
	{
		this.siteNodeVO = siteNodeVO;
		this.ceb = new ConstraintExceptionBuffer();			
	}	

	public void setSiteNodeId(Integer siteNodeId)
	{
		this.siteNodeVO.setSiteNodeId(siteNodeId);
	}

	public Integer getSiteNodeId()
	{
		return siteNodeVO.getSiteNodeId();
	}

	@SuppressWarnings("unchecked")
	public String doInput() throws Exception
	{
		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
		this.returnAddress = "ViewInlineOperationMessages.action"; //ViewContent!V3.action?contentId=" + contentId + "&repositoryId=" + this.repositoryId;

		if(this.qualifyerXML != null && !this.qualifyerXML.equals(""))
		{
			this.qualifyers = parseSiteNodesFromXML(this.qualifyerXML);
		}
		else
		{
			SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(getSiteNodeId());
			siteNodeVO.getExtraProperties().put("displayName", getLocalizedNameForSiteNode(siteNodeVO, sortLanguageId));
			this.qualifyers.add(siteNodeVO);
		}

		return "input";
	}

   public String doCopyDone() throws Exception
   {
       return "success";
   }
    
    public String doExecute() throws Exception
    {
    	if(this.newParentSiteNodeId == null)
        {
    		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), false);
            return "chooseDestination";
        }
        
        ceb.throwIfNotEmpty();
    	
        String errorMessage = "";
        
		ProcessBean processBean = ProcessBean.createProcessBean(this.getClass().getName(), "" + getInfoGluePrincipal().getName());
		processBean.setStatus(ProcessBean.RUNNING);

		try
		{
            if(this.qualifyerXML != null && this.qualifyerXML.length() != 0)
		    {
		        Document document = new DOMBuilder().getDocument(this.qualifyerXML);
				List siteNodes = parseSiteNodesFromXML(this.qualifyerXML);
				Iterator iterator = siteNodes.iterator();
				int i=0;
				while(iterator.hasNext())
				{
				    SiteNodeVO siteNodeVO = (SiteNodeVO)iterator.next();
				    try
					{		
				    	SiteNodeControllerProxy.getSiteNodeControllerProxy().acCopySiteNode(this.getInfoGluePrincipal(), siteNodeVO, this.newParentSiteNodeId, processBean);
					}
					catch(ConstraintException ce)
					{
						logger.warn("Limitation in copy site nodes:" + ce.getMessage());
						errorMessage = LabelController.getController(getLocale()).getLocalizedString(getLocale(), ce.getErrorCode(), getSiteNodeVO(newParentSiteNodeId).getName());
					    this.errorsOccurred = true;
					}
					catch(Exception e)
					{
						logger.error("Error in copy site nodes:" + e.getMessage(), e);
						errorMessage = e.getMessage();
					    this.errorsOccurred = true;
					}
					i++;
		    	}
		    }

            processBean.updateProcess("Finished - cleaning up");
            Thread.sleep(1000);
		}
		catch(Exception e)
		{
			logger.error("Error in copy site nodes:" + e.getMessage(), e);
	        setActionExtraData(userSessionKey, "confirmationMessage", e.getMessage() /*getLocalizedString(getLocale(), "tool.contenttool.siteNodeCopied.confirmation", getSiteNodeVO(newParentSiteNodeId).getName())*/);
		}
		finally
		{
			processBean.setStatus(ProcessBean.FINISHED);
			processBean.removeProcess();
		}
		
        setActionExtraData(userSessionKey, "refreshToolbarAndMenu", "" + true);
        setActionExtraData(userSessionKey, "repositoryId", "" + this.repositoryId);
        setActionExtraData(userSessionKey, "siteNodeId", "" + newParentSiteNodeId);
        setActionExtraData(userSessionKey, "unrefreshedSiteNodeId", "" + newParentSiteNodeId);
        setActionExtraData(userSessionKey, "unrefreshedNodeId", "" + newParentSiteNodeId);
        setActionExtraData(userSessionKey, "changeTypeId", "" + this.changeTypeId);

        if(errorsOccurred)
        	setActionExtraData(userSessionKey, "confirmationMessage", errorMessage);        	
        else
        	setActionExtraData(userSessionKey, "confirmationMessage", getLocalizedString(getLocale(), "tool.contenttool.siteNodeCopied.confirmation", getSiteNodeVO(newParentSiteNodeId).getName()));

		this.topSiteNodeId = SiteNodeController.getController().getRootSiteNodeVO(this.repositoryId).getId();
		    
        return "success";
    }

	@SuppressWarnings("unchecked")
	private List<SiteNodeVO> parseSiteNodesFromXML(String qualifyerXML)
	{
		List<SiteNodeVO> siteNodes = new ArrayList<SiteNodeVO>();

		try
		{
			Document document = new DOMBuilder().getDocument(qualifyerXML);
			String entity = document.getRootElement().attributeValue("entity");
			Map<String, SiteNodeVO> addedSiteNodes = new HashMap<String, SiteNodeVO>();

			@SuppressWarnings("unchecked")
			List<Element> children = document.getRootElement().elements();
			Iterator<Element> i = children.iterator();
			while(i.hasNext())
			{
				Element child = i.next();
				String id = child.getStringValue();
				String path = child.attributeValue("path");

				if(!addedSiteNodes.containsKey(id))
				{
					SiteNodeVO siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(new Integer(id));
					siteNodeVO.getExtraProperties().put("displayName", getLocalizedNameForSiteNode(siteNodeVO, sortLanguageId));
					siteNodes.add(siteNodeVO);
					addedSiteNodes.put(id, siteNodeVO);
				}
			}
		}
		catch(Exception ex)
		{
			logger.error("Error when parsing XML for SiteNodes. Message: " + ex.getMessage());
			logger.warn("Error when parsing XML for SiteNodes.", ex);
		}

		return siteNodes;
	}

    public Integer getChangeTypeId()
    {
        return changeTypeId;
    }
    
    public void setChangeTypeId(Integer changeTypeId)
    {
        this.changeTypeId = changeTypeId;
    }
    
    public Integer getNewParentSiteNodeId()
    {
        return newParentSiteNodeId;
    }
    
    public void setNewParentSiteNodeId(Integer newParentSiteNodeId)
    {
        this.newParentSiteNodeId = newParentSiteNodeId;
    }
    
    public Integer getOriginalSiteNodeId()
    {
        return originalSiteNodeId;
    }
    
    public void setOriginalSiteNodeId(Integer originalSiteNodeId)
    {
        this.originalSiteNodeId = originalSiteNodeId;
    }
    
    public Integer getParentSiteNodeId()
    {
        return parentSiteNodeId;
    }
    
    public void setParentSiteNodeId(Integer parentSiteNodeId)
    {
        this.parentSiteNodeId = parentSiteNodeId;
    }
    
    public String getQualifyerXML()
    {
        return qualifyerXML;
    }
    
    public void setQualifyerXML(String qualifyerXML)
    {
        this.qualifyerXML = qualifyerXML;
    }
    
    public Integer getRepositoryId()
    {
        return repositoryId;
    }
    
    public void setRepositoryId(Integer repositoryId)
    {
        this.repositoryId = repositoryId;
    }
    
    public Integer getTopSiteNodeId()
    {
        return topSiteNodeId;
    }
    
    public void setTopSiteNodeId(Integer topSiteNodeId)
    {
        this.topSiteNodeId = topSiteNodeId;
    }
    
    public boolean isErrorsOccurred()
    {
        return errorsOccurred;
    }
    
    public List getQualifyers()
    {
        return qualifyers;
    }
    
    public List getRepositories()
    {
        return repositories;
    }
    
    public SiteNodeVO getSiteNodeVO()
    {
        return siteNodeVO;
    }
    
	public String getUserSessionKey()
	{
		return userSessionKey;
	}

	public void setUserSessionKey(String userSessionKey)
	{
		this.userSessionKey = userSessionKey;
	}

	public String getReturnAddress()
	{
		return this.returnAddress;
	}    

    public String getOriginalAddress()
	{
		return originalAddress;
	}

	public void setOriginalAddress(String originalAddress)
	{
		this.originalAddress = originalAddress;
	}

	public void setReturnAddress(String returnAddress)
	{
		this.returnAddress = returnAddress;
	}

	public Integer getSortLanguageId()
	{
		return sortLanguageId;
	}

	public void setSortLanguageId(Integer languageId)
	{
		this.sortLanguageId = languageId;
	}

}
