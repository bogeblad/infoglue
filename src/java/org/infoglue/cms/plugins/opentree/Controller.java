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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.infoglue.cms.net.Node;

public class Controller
{
	public static final int NODE_CHANGED = 1;
	public static final int NODE_CHILDREN_CHANGED = 2;
	public static final int NODE_MOVED = 3;
	public static final int NODE_DELETED = 4;
	
    private JTree nodeTree;
    private Communicator communicator;
    private Hashtable nodes = new Hashtable();
    
    private String hideLeafs;
    
    public Controller(String serverAddress, String entityName, Integer repositoryId, String hideLeafs)
    {
        this.communicator = new Communicator(serverAddress, entityName, repositoryId);
    	this.hideLeafs    = hideLeafs;
    }

	public void setNodeTree(JTree nodeTree)
	{
		this.nodeTree = nodeTree;
	}
	
	/**
	 * This method refreshes a node and emties it's children so they can be reinitialized.
	 */
	
	public void refreshNode(Integer nodeId, Integer changeTypeId, Integer addedNodeId)
	{
		DefaultTreeModel model = (DefaultTreeModel)nodeTree.getModel();
		CMSNode node = (CMSNode)nodes.get(nodeId);
		//System.out.println("Node found:" + node);
		
		if(changeTypeId.intValue() == NODE_CHANGED)
		{
			CMSNode newNode = getNode(nodeId);
			//System.out.println("Updated Node found:" + newNode);
		
			node.setName(newNode.getName());
			model.nodeChanged(node);
		}
		else if(changeTypeId.intValue() == NODE_CHILDREN_CHANGED)
		{
			node.setAreChildrenDefined(false);
			
			//model.nodeChanged(node);
			model.nodeStructureChanged(node);	
			//model.reload(node);
	
			CMSNode addedNode = (CMSNode)nodes.get(addedNodeId);
			TreePath currentPath = nodeTree.getSelectionPath();
			if(currentPath == null)
				 currentPath = new TreePath(node);
				 
			//System.out.println("Current path:" + currentPath);
			//System.out.println("Added Node:" + addedNode);
			nodeTree.setSelectionPath(currentPath.pathByAddingChild(addedNode));
		}
		else if(changeTypeId.intValue() == NODE_MOVED)
		{
			CMSNode currentParent = (CMSNode)((CMSNode)nodeTree.getLastSelectedPathComponent()).getParent();			
			//System.out.println("node:" + node.getName());
			//System.out.println("currentParent:" + currentParent.getName());
			currentParent.setAreChildrenDefined(false);
			model.nodeStructureChanged(currentParent);	
			
			node.setAreChildrenDefined(false);
			model.nodeStructureChanged(node);	

			CMSNode addedNode = (CMSNode)nodes.get(addedNodeId);
		}
		else if(changeTypeId.intValue() == NODE_DELETED)
		{
			nodeTree.setSelectionPath(nodeTree.getSelectionPath().getParentPath());
			node.setAreChildrenDefined(false);
			model.nodeStructureChanged(node);	
			//System.out.println("Hoping this is the parent:" + node.getName());
		}

	}
    
    
    public CMSNode getRootNode() throws Exception
    {
        Node rootNode = communicator.getRootNode();
        CMSNode rootCMSNode = generateVisualTreeModel(rootNode);
        
    	updateHash(rootCMSNode);
    	
        //This should be there later when I know how to fire of events
        //addContentChildrenToParent(rootCMSContent);
        
        return rootCMSNode;
    }
    
    public CMSNode getNode(Integer nodeId)
    {
        Node node = communicator.getNode(nodeId);
        CMSNode cmsNode = generateVisualTreeModel(node);
        
        updateHash(cmsNode);
        
        return cmsNode;
    }

	public void updateHash(CMSNode node)
	{
		if(!nodes.containsKey(node.getId()))
	        nodes.put(node.getId(), node);
	}

    public List getChildNodes(Integer parentId)
    {
        List childNodeVOList = communicator.getChildNodeList(parentId);
        List childNodes = generateVisualTreeModel(childNodeVOList);
        
        return childNodes;
    }


    private CMSNode generateVisualTreeModel(Node node)
    {
    	
        CMSNode rootNode = new CMSNode(this, node.getIsBranch().booleanValue());
        rootNode.setId(node.getId());
        rootNode.setName(node.getName());
               
        return rootNode;
    }

    private List generateVisualTreeModel(List nodeList)
    {
    	ArrayList children = new ArrayList();
    	if(nodeList != null)
    	{
	    	Iterator iterator = nodeList.iterator();
	    	while(iterator.hasNext())
	    	{
	    		Node node = (Node)iterator.next();
	    		if(this.hideLeafs == null || this.hideLeafs.equals("false") || node.getIsBranch().booleanValue() == true)
		        {
			        CMSNode cmsNode = new CMSNode(this, node.getIsBranch().booleanValue());
			        cmsNode.setId(node.getId());
			        cmsNode.setName(node.getName());
			        children.add(cmsNode);
	
			        updateHash(cmsNode);
		        }
	    	}
    	}
    	        
        return children;
    }

   
}