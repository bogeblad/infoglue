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

package org.infoglue.cms.applications.publishingtool.actions;

import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.publishing.EditionBrowser;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.ChangeNotificationController;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.NotificationMessage;
import org.infoglue.cms.util.RemoteCacheUpdater;

/**
 * This returns a list of all events to be published or denied,
 * and a page of previously published editions.
 *
 * @author Stefan Sik, ss@frovi.com
 * @author Frank Febbraro, frank@phase2technology.com
 */
public class ViewPublicationsAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;

	private int startIndex = 0;
	private Integer repositoryId;
	private Integer publicationId;
	private RepositoryVO repositoryVO;
	private List publicationEvents;
	private List publicationDetailVOList;
	private EditionBrowser editionBrowser;
	private String filter = null;

	public int getStartIndex()			{ return startIndex; }
	public void setStartIndex(int i)	{ startIndex = i; }

	public Integer getRepositoryId()				{ return repositoryId; }
	public void setRepositoryId(Integer i)			{ repositoryId = i; }

	public RepositoryVO getRepositoryVO()			{ return repositoryVO; }
	public List getPublicationEvents()				{ return publicationEvents; }

	public EditionBrowser getEditionBrowser()		{ return editionBrowser; }
	public void setEditionBrowser(EditionBrowser b)	{ editionBrowser = b; }

	public String getFilter() 						{ return filter; }
	public void setFilter(String filter) 			
	{ 
		if(CmsPropertyHandler.getAllowPublicationEventFilter().equalsIgnoreCase("true"))
		{
			this.filter = filter; 
			if(this.filter != null)
			{
				this.getHttpSession().setAttribute("publishEventFilter", this.filter);
			}
		}
	}
	
    public String doV3() throws Exception
    {
    	doExecute();
    	
        return "successV3";
    }

	public String doExecute() throws Exception
	{
		if(filter == null)
		{
			if(CmsPropertyHandler.getAllowPublicationEventFilter().equalsIgnoreCase("true"))
			{
				String storedFilter = (String)this.getHttpSession().getAttribute("publishEventFilter");
				if(storedFilter != null)
					filter = storedFilter;
			}
			else
				filter = CmsPropertyHandler.getDefaultPublicationEventFilter();

		}

		repositoryVO		= RepositoryController.getController().getRepositoryVOWithId(repositoryId);
		publicationEvents	= PublicationController.getPublicationEvents(repositoryId, getInfoGluePrincipal(), filter);
		editionBrowser		= PublicationController.getEditionPage(repositoryId, startIndex);
					
		return SUCCESS;
	}
	
	public String doShowPublicationDetails() throws Exception
	{
		this.publicationDetailVOList = PublicationController.getPublicationDetailVOList(publicationId);
		
		return "showPublicationDetails";
	}

    public String doSystem() throws Exception
    {
        return "successSystem";
    }

    public String doPushSystemNotificationMessages() throws Exception
    {
        NotificationMessage notificationMessage = null;
        List messages = RemoteCacheUpdater.getSystemNotificationMessages();
        synchronized(messages)
        {
            if(messages.size() > 0)
                notificationMessage = (NotificationMessage)messages.get(0);
        }
        
        if(notificationMessage != null)
        {
    		notificationMessage = new NotificationMessage("ViewPublishingToolStartPageAction.doPushSystemNotificationMessages():", "" + notificationMessage.getClassName(), this.getInfoGluePrincipal().getName(), NotificationMessage.SYSTEM, notificationMessage.getObjectId(), notificationMessage.getObjectName());
            ChangeNotificationController.getInstance().addNotificationMessage(notificationMessage);
            RemoteCacheUpdater.clearSystemNotificationMessages();
        }
        
        return doSystem();
    }
    
    public List getSystemNotificationMessages()
    {
        return RemoteCacheUpdater.getSystemNotificationMessages();
    }

	public static List getPublicationDetails(Integer publicationId) throws SystemException
	{
		return PublicationController.getPublicationDetailVOList(publicationId);
	}

	public ContentVO getOwningContent(Integer id) throws SystemException
	{
		return PublicationController.getOwningContentVO(id);
	}

	public SiteNodeVO getOwningSiteNode(Integer id) throws SystemException
	{
		return PublicationController.getOwningSiteNodeVO(id);
	}
	
	/**
	 * Escapes the string
	 */
	public String escape(String string)
	{
		return string.replace('\'', '´');
	}
	
    public void setPublicationId(Integer publicationId)
    {
        this.publicationId = publicationId;
    }
    
    public List getPublicationDetailVOList()
    {
        return publicationDetailVOList;
    }
    
}
