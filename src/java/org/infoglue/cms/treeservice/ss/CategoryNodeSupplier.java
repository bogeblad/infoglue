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
 * $Id: CategoryNodeSupplier.java,v 1.3 2006/03/06 16:52:20 mattias Exp $
 */
package org.infoglue.cms.treeservice.ss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.exception.SystemException;

import com.frovi.ss.Tree.BaseNode;
import com.frovi.ss.Tree.BaseNodeSupplier;

/**
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class CategoryNodeSupplier extends BaseNodeSupplier
{
    private final static Logger logger = Logger.getLogger(CategoryNodeSupplier.class.getName());

	public static final Integer ROOT = new Integer(-1);

	private CategoryController controller = CategoryController.getController();

	private ArrayList cacheLeafs;

	public CategoryNodeSupplier()
	{
		BaseNode rootNode =  new ContentNodeImpl();
		rootNode.setChildren(true);
		rootNode.setId(ROOT); // There is no BASE category so make it up
		rootNode.setTitle("Categories");
		rootNode.setContainer(true);
		setRootNode(rootNode);
	}

	public boolean hasChildren()
	{
		return true;
	}

	public Collection getChildContainerNodes(Integer parentNode)
	{
		ArrayList ret = new ArrayList();
		cacheLeafs = new ArrayList();

		List children = null;
		try
		{
			children = (ROOT.equals(parentNode))
							? controller.findRootCategories()
							: controller.findActiveByParent(parentNode);
		}
		catch (SystemException e)
		{
			logger.warn("Error getting Category Children", e);
		}

		for (Iterator i = children.iterator(); i.hasNext();)
		{
			CategoryVO vo = (CategoryVO) i.next();

			List grandkids = getGrandKids(vo.getId());

			BaseNode node =  new CategoryNodeImpl();
			node.setId(vo.getId());
			node.setTitle(vo.getName());
			node.setContainer(true);
			node.setChildren(!grandkids.isEmpty());
			ret.add(node);
		}

		return ret;
	}

	private List getGrandKids(Integer childId)
	{
		try
		{
			return controller.findActiveByParent(childId);
		}
		catch (SystemException e)
		{
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * @see com.frovi.ss.Tree.INodeSupplier#getChildLeafNodes(Integer)
	 */
	public Collection getChildLeafNodes(Integer parentNode)
	{
		return Collections.EMPTY_LIST;
	}

}
