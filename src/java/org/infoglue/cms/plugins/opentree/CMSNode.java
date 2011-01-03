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
 
package org.infoglue.cms.plugins.opentree;

import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

public class CMSNode extends DefaultMutableTreeNode
{
	private Controller controller = null;
    private Integer id            = null;
    private String name           = null;
    private boolean isBranch     = false;

	private boolean areChildrenDefined = false;

    
    public CMSNode(Controller controller, boolean isBranch)
    {
    	this.controller  = controller;
		this.isBranch    = isBranch;
    }

    public Integer getId()
    {
        return this.id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public String toString()
    {
        return this.name;
    }
    
	public int getChildCount()
	{
		//System.out.println("getChildCount called:" + areChildrenDefined);
		if (!areChildrenDefined)
			defineChildNodes();
		return (super.getChildCount());
	}
	
	private void defineChildNodes()
	{
		areChildrenDefined = true;
		//System.out.println("defineChildNodes was called");
		this.removeAllChildren();
		//System.out.println("removed all old children");
		List childNodes = controller.getChildNodes(this.id);
		Iterator iterator = childNodes.iterator();
		while(iterator.hasNext())
		{
			add((CMSNode)iterator.next());
		}
	}
	
	public void setAreChildrenDefined(boolean areChildrenDefined)
	{
		this.areChildrenDefined = areChildrenDefined;
		getChildCount();
	}
	
	public boolean isLeaf() 
	{
		return !this.isBranch;
	}
}