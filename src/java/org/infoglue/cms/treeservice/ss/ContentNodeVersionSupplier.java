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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

import com.frovi.ss.Tree.BaseNode;
import com.frovi.ss.Tree.BaseNodeSupplier;

/**
 * ContentNodeSupplier.java
 * Created on 2002-sep-30 
 * @author Stefan Sik, ss@frovi.com 
 * ss
 */
public class ContentNodeVersionSupplier extends BaseNodeSupplier
{

	// private BaseNode rootNode;
	private ArrayList cacheLeafs;

	
	public ContentNodeVersionSupplier(Integer repositoryId, String userName) throws SystemException
	{
		ContentVO vo =null;
		try
		{
			vo = ContentControllerProxy.getController().getRootContentVO(repositoryId, userName);
			BaseNode rootNode =  new ContentNodeImpl();
			rootNode.setChildren(true);
			rootNode.setId(vo.getId());
			rootNode.setTitle(vo.getName());
			rootNode.setContainer(vo.getIsBranch().booleanValue());	
			setRootNode(rootNode);
		}
		catch (ConstraintException e)
		{
		}
		catch (SystemException e)
		{
		}
			
	}
	/**
	 * @see com.frovi.ss.Tree.BaseNodeSupplier#hasChildren()
	 */
	public boolean hasChildren()
	{
		return false;
	}

	/**
	 * @see com.frovi.ss.Tree.INodeSupplier#getChildContainerNodes(Integer)
	 */
	public Collection getChildContainerNodes(Integer parentNode)
	{
		ArrayList ret = new ArrayList();
		cacheLeafs = new ArrayList();
		
		if (parentNode.intValue() < 0 )
		{
			return ret;
		}
		
		List l = null;
		try
		{
			l = ContentController.getContentController().getContentChildrenVOList(parentNode, null, false);
		}
		catch (ConstraintException e)
		{
		}
		catch (SystemException e)
		{
		}
		
		// Do it in two loops to get sorting right
		Iterator i = l.iterator();
		while(i.hasNext())
		{
			ContentVO vo = (ContentVO) i.next();
			if (vo.getIsBranch().booleanValue())
			{
				BaseNode node =  new ContentNodeImpl();
				node.setId(vo.getId());
				node.setContainer(true);
				
				node.setChildren((vo.getChildCount().intValue() > 0)); // 
				
				node.setTitle(vo.getName());
				
				ret.add(node);
			}
		}
		i = l.iterator();
		while(i.hasNext())
		{
			ContentVO vo = (ContentVO) i.next();
			if (!vo.getIsBranch().booleanValue())
			{
				// Betrakta dessa som container nodes
				
				BaseNode node =  new ContentNodeImpl();
				node.setId( new Integer(- vo.getId().intValue()) );
				node.setContainer(true);
				node.setTitle(vo.getName());
				node.setChildren((vo.getChildCount().intValue() > 0));
				ret.add(node);				
			}
		}
		
		return ret;
	}

	/**
	 * @see com.frovi.ss.Tree.INodeSupplier#getChildLeafNodes(Integer)
	 */
	public Collection getChildLeafNodes(Integer parentNode)
	{
		ArrayList ret = new ArrayList();
		if (parentNode.intValue() >= 0)
			return ret;
		
		List l = null;
		try
		{
			l = ContentVersionController.getContentVersionController().getContentVersionVOWithParent(new Integer(- parentNode.intValue()));
		}
		catch (SystemException e)
		{
		}
		
		Iterator i = l.iterator();
		while(i.hasNext())
		{
			ContentVersionVO vo = (ContentVersionVO) i.next();
			ContentNodeImpl node =  new ContentNodeImpl();
			node.setId(vo.getId());
			node.setState(vo.getStateId());
			node.setContainer(false);
			node.setTitle(vo.getVersionComment() + " (" + vo.getModifiedDateTime() + ")");
			node.getParameters().put("languageId", vo.getLanguageId());
			node.getParameters().put("languageName", vo.getLanguageName());

			ret.add(node);
			
		}
		
		return ret;
	}

}
