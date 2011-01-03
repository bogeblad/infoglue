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

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
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
public class ContentNodeSupplierJDBC extends BaseNodeSupplier
{

	private ArrayList cacheLeafs;
	private java.sql.Connection conn;
	
	
	public ContentNodeSupplierJDBC(Integer repositoryId, String userName) throws Exception, SystemException
	{
		Class.forName("com.mysql.jdbc.Driver").newInstance();	
		conn = DriverManager.getConnection("jdbc:mysql://localhost/frovi_cms_dev?user=frovi_cms&password=pass123");
	
        Statement stmt = null;
        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select * from cmContent where parentContentId is null and repositoryId=" + repositoryId);		
		BaseNode rootNode =  new ContentNodeImpl();		
		if (rs.next())
		{
			rootNode.setChildren(true);
			rootNode.setId(new Integer(rs.getInt("contentId")));
			rootNode.setTitle(rs.getString("name"));
			rootNode.setContainer(rs.getBoolean("isBranch"));	
			setRootNode(rootNode);
		}
		else
		{
			ContentVO vo =null;
			try
			{
				vo = ContentControllerProxy.getController().getRootContentVO(repositoryId, userName);
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
			
	}
	/**
	 * @see com.frovi.ss.Tree.BaseNodeSupplier#hasChildren()
	 */
	public boolean hasChildren()
	{
		return true;
	}
	
	/**
	 * @see com.frovi.ss.Tree.BaseNodeSupplier#hasChildren(Integer)
	 */
	public boolean hasChildren(Integer nodeId)
	{
		boolean res = false;
		ArrayList ret = new ArrayList();
		List l = null;
        Statement stmt = null;
        
        
        try {
			stmt = conn.createStatement();
	        ResultSet rs;
			rs = stmt.executeQuery("select count(contentId) as cnt from cmContent where parentContentId="+ nodeId);
			if(rs.next())
				res = rs.getInt("cnt") > 0;
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }		
		return res;
	}


	/**
	 * @see com.frovi.ss.Tree.INodeSupplier#getChildContainerNodes(Integer)
	 */
	public Collection getChildContainerNodes(Integer parentNode)
	{
		ArrayList ret = new ArrayList();
		cacheLeafs = new ArrayList();
		
		List l = null;
		ResultSet rs = null;
        Statement stmt = null;

        
        try {
	        stmt = conn.createStatement();
	        rs = stmt.executeQuery("select * from cmContent where parentContentId=" + parentNode);
			while (rs.next())
			{
				BaseNode node =  new ContentNodeImpl();			
				node.setId(new Integer(rs.getInt("contentId")));
				node.setTitle(rs.getString("name"));
				node.setContainer(rs.getBoolean("isBranch"));	
				
				if (node.isContainer())
				{
					ret.add(node);
				}
				else
				{
					cacheLeafs.add(node);
				}
				
			}
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
		return ret;
	}

	/**
	 * @see com.frovi.ss.Tree.INodeSupplier#getChildLeafNodes(Integer)
	 */
	public Collection getChildLeafNodes(Integer parentNode)
	{
		if (cacheLeafs == null)
			getChildContainerNodes(parentNode);
		return cacheLeafs;
	}

}
