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

package org.infoglue.cms.treeservice.ss;

import java.util.Collections;
import java.util.Map;

import org.apache.velocity.runtime.parser.node.SetExecutor;

/**
 * @author ss
 *
 */
public class ManagementNodeImpl extends com.frovi.ss.Tree.BaseNode 
{
	// This is a test, rely heavy on base for now
	
	public ManagementNodeImpl(Integer id, String title, String action, Map extraData)
	{
		setId(id);
		setTitle(title);
		setAction(action);
		setChildren(false);
		getParameters().putAll(extraData);
	}

	
	public ManagementNodeImpl(Integer id, String title, String action)
	{
		this(id, title, action, Collections.EMPTY_MAP);
	}
	
	public ManagementNodeImpl(int id, String title, String action, Map extraData)
	{
		this(new Integer(id), title, action, extraData);
	}
	
	private String action;

	/**
	 * Returns the action.
	 * @return String
	 */
	public String getAction()
	{
		return action;
	}

	/**
	 * Sets the action.
	 * @param action The action to set
	 */
	public void setAction(String action)
	{
		this.action = action;
	}

}
