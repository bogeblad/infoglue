package com.frovi.ss.Tree;

import java.util.Collection;

import org.infoglue.cms.exception.SystemException;

/**
 * INodeSupplier.java
 * Created on 2002-sep-27 
 * @author Stefan Sik, ss@frovi.com 
 * ss
 */
public interface INodeSupplier
{
	/**
	 * return true if this node (nodeId) has childnodes
	 */
	public boolean hasChildren(Integer nodeId) throws SystemException, Exception;
	
	/** 
	 * return true if you will answer the question above
	 */
	public boolean hasChildren();
		
	/** 	
	 * ContainerNodes (folderNodes)
	 * Create a list of nodes that is children to the node with
	 *	the supplied id "parentNode".
	 *	Set the attributes on each node, and if possible
	 *	also determine if each node itself has children.
	 */
	public Collection getChildContainerNodes(Integer parentNode) throws SystemException, Exception;
	
	/** 	
	 * LeafNodes (documentNodes)
	 * Create a list of nodes that is children to the node with
	 *	the supplied id "parentNode".
	 *	Set the attributes on each node, in this case 
	 *	node.setChildren(boolean) has no effect
	 */
	public Collection getChildLeafNodes(Integer parentNode);
	
	// Optional
	// public Integer getRootNodeId();
	// public void setRootNodeId(Integer rootNodeId);
	public BaseNode getRootNode();

}
