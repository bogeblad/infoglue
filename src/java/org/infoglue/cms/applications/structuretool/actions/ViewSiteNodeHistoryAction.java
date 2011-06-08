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

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;

/**
 *
 * @author Mattias Bogeblad
 * 
 * Present a list of contentVersion under a given content, showing off what has happened.
 */

public class ViewSiteNodeHistoryAction extends InfoGlueAbstractAction 
{
    private final static Logger logger = Logger.getLogger(ViewSiteNodeHistoryAction.class.getName());

	private static final long serialVersionUID = 1L;
	
	private Integer siteNodeId;
	private SiteNodeVO siteNodeVO = null;
	private Boolean inline = false;
	
	private List<SiteNodeVersionVO> siteNodeVersionVOList;

	protected String doExecute() throws Exception 
	{
	    this.siteNodeVO = SiteNodeController.getController().getSiteNodeVOWithId(this.siteNodeId);
	    this.siteNodeVersionVOList = SiteNodeVersionController.getController().getSiteNodeVersionVOList(this.siteNodeId);

	    return "success";
	}

	public ContentVersionVO getMatchingContentVersion(Date modifiedDateTime) throws Exception
	{
		ContentVersionVO metaInfoContentVersionVO = null;
		long previousDiff = -1;
		
		List<ContentVersionVO> metaInfoContentVersionVOList = ContentVersionController.getContentVersionController().getContentVersionVOWithParent(this.siteNodeVO.getMetaInfoContentId());
		Iterator<ContentVersionVO> metaInfoContentVersionVOListIterator = metaInfoContentVersionVOList.iterator();
		while(metaInfoContentVersionVOListIterator.hasNext())
		{
			ContentVersionVO metaInfoContentVersionVOCandidate = metaInfoContentVersionVOListIterator.next();
			Date metaInfoContentVersionModifiedDateTime = metaInfoContentVersionVOCandidate.getModifiedDateTime();
			logger.info("metaInfoContentVersionModifiedDateTime: " + metaInfoContentVersionModifiedDateTime.getTime());
			if(metaInfoContentVersionVO == null)
			{
				metaInfoContentVersionVO = metaInfoContentVersionVOCandidate;
				previousDiff = Math.abs(metaInfoContentVersionModifiedDateTime.getTime() - modifiedDateTime.getTime());
			}
			else
			{
				long diff = Math.abs(metaInfoContentVersionModifiedDateTime.getTime() - modifiedDateTime.getTime());
				logger.info("diff:" + diff + " : " + previousDiff);
				if(previousDiff < diff)
					metaInfoContentVersionVO = metaInfoContentVersionVOCandidate;					
			}
		}
		
		return metaInfoContentVersionVO;
	}
	
	public Integer getSiteNodeId() 
	{
		return this.siteNodeId;
	}

	public void setSiteNodeId(Integer siteNodeId) 
	{
		this.siteNodeId = siteNodeId;
	}

	public SiteNodeVO getSiteNode() 
	{
		return this.siteNodeVO;
	}

    public List<SiteNodeVersionVO> getSiteNodeVersionVOList()
    {
        return this.siteNodeVersionVOList;
    }
    
    public void setInline(Boolean inline)
    {
        this.inline = inline;
    }

    public Boolean getInline()
    {
        return inline;
    }

}
