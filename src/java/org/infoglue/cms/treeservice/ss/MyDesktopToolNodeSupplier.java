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
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.StringManager;
import org.infoglue.cms.util.StringManagerFactory;

import com.frovi.ss.Tree.BaseNodeSupplier;

/**
 * Provides tree menu for the publishing tool
 * 
 * @author Mattias Bogeblad 
 * 
 */
public class MyDesktopToolNodeSupplier extends BaseNodeSupplier
{

	private boolean showLeafs = true;
	private InfoGluePrincipal infogluePrincipal = null;
	private Locale locale = null;
	
	public MyDesktopToolNodeSupplier(InfoGluePrincipal infogluePrincipal, Locale locale) throws SystemException
	{
		this.infogluePrincipal = infogluePrincipal;
		this.locale = locale;
		
		setRootNode(new MyDesktopNodeImpl(0, "root", "ViewMyDesktop.action", Collections.EMPTY_MAP));
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
	public Collection getChildContainerNodes(Integer parentNode)
	{
		int cnt = 1;
		ArrayList r = new ArrayList();
		ManagementNodeImpl node;
				
		Map parameters = new HashMap();
		//parameters.put("extraMarkup", "style='padding-top: 10px; border-top: 1px solid #999;'");

		//parameters.put("extraMarkup", "Basic");

		r.add(new MyDesktopNodeImpl(cnt++, getLocalizedString(locale, "tool.mydesktoptool.myPages"), "ViewMyDesktop!latestPages.action"));
		
		r.add(new MyDesktopNodeImpl(cnt++, getLocalizedString(locale, "tool.mydesktoptool.myContents"), "ViewMyDesktop!latestContents.action"));

		r.add(new MyDesktopNodeImpl(cnt++, getLocalizedString(locale, "tool.mydesktoptool.ongoingWorkflows"), "ViewMyDesktop!ongoing.action", parameters));

		r.add(new MyDesktopNodeImpl(cnt++, getLocalizedString(locale, "tool.mydesktoptool.availableWorkflows"), "ViewMyDesktop!available.action"));

		//r.add(new MyDesktopNodeImpl(cnt++, getLocalizedString(locale, "tool.mydesktoptool.availableShortcuts"), "ViewMyDesktop!shortcuts.action"));
	
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

	public String getLocalizedString(Locale locale, String key) 
  	{
    	StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.applications", locale);

    	return stringManager.getString(key);
  	}

}
