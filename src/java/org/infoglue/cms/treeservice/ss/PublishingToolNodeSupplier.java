/* ===============================================================================
 *
 * Part of the InfoGlue Content Publishing Platform (www.infoglue.org)
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

package org.infoglue.cms.treeservice.ss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.LabelController;
import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.deliver.util.Timer;

import com.frovi.ss.Tree.BaseNodeSupplier;

/**
 * Provides tree menu for the publishing tool
 * 
 * @author Mattias Bogeblad 
 * 
 */
public class PublishingToolNodeSupplier extends BaseNodeSupplier
{

	private boolean showLeafs = true;
	private InfoGluePrincipal infogluePrincipal = null;
	private Locale locale = null;

	public PublishingToolNodeSupplier(InfoGluePrincipal infogluePrincipal, Locale locale) throws SystemException
	{
		this.infogluePrincipal = infogluePrincipal;
		this.locale = locale;
		
		setRootNode(new PublishingNodeImpl(0, "root", "ViewPublishingToolStartPage!V3.action", Collections.EMPTY_MAP));
	}
	
	/**
	 * @see com.frovi.ss.Tree.BaseNodeSupplier#hasChildren()
	 */
	public boolean hasChildren()
	{
		if (showLeafs)
			return false;
		else
			return true;
	}
	
	/**
	 * @see com.frovi.ss.Tree.INodeSupplier#getChildContainerNodes(Integer)
	 */
	public Collection getChildContainerNodes(Integer parentNode) throws Exception
	{
		ArrayList r = new ArrayList();
		PublishingNodeImpl node;
				
		Map parameters = new HashMap();

		List repositoryVOList = RepositoryController.getController().getAuthorizedRepositoryVOList(infogluePrincipal, false);
		Iterator repositoryVOListIterator = repositoryVOList.iterator();
		while(repositoryVOListIterator.hasNext())
		{
			RepositoryVO repositoryVO = (RepositoryVO)repositoryVOListIterator.next();
			
			Timer t = new Timer();
			List events = PublicationController.getPublicationEvents(repositoryVO.getId(), infogluePrincipal, "all");
			List groupEvents = PublicationController.getPublicationEvents(repositoryVO.getId(), infogluePrincipal, "groupBased");
			//t.printElapsedTime("Events took...");
			
			r.add(new PublishingNodeImpl(repositoryVO.getId(), "" + repositoryVO.getName() + " (" + (events.size() > 0 ? "<strong>" : "") + events.size() + (events.size() > 0 ? "</strong>" : "") + "/" + (groupEvents.size() > 0 ? "<strong>" : "") + groupEvents.size() + (groupEvents.size() > 0 ? "</strong>" : "") + ")", "ViewPublications!V3.action?repositoryId=" + repositoryVO.getId(), parameters));
		}

		Map parameterMap = new HashMap();
		parameterMap.put("extraMarkup", "System wide");
		
		r.add(new PublishingNodeImpl(-1, LabelController.getController(locale).getLocalizedString(locale, "tool.publishingtool.globalSettings.label"), "ViewPublications!system.action", parameterMap));

		return r;
	}

	/**
	 * @see com.frovi.ss.Tree.INodeSupplier#getChildLeafNodes(Integer)
	 */
	public Collection getChildLeafNodes(Integer parentNode)
	{
		ArrayList ret = new ArrayList();
		return ret;
	}

}
