package com.frovi.ss.Tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * PureHtmlTree.java
 * Created on 2002-sep-27 
 * @author Stefan Sik, ss@frovi.com 
 * ss
 * 
 */
public class MakeTree
{
	private boolean showTreeLines = true;
	private ArrayList ret = new ArrayList();
	private INodeSupplier nodeSupplier;
	private boolean showRoot = true;
	private int startLevel = -1;
	
	private Vector intTreeLines = new Vector(50,10);

	// Constructor, we need a nodeSupplier.
	public MakeTree(INodeSupplier nodeSupplier)
	{
		setNodeSupplier(nodeSupplier);
	}

	// Creates a nodelist with the default parent node, and no expansion string
	public Collection makeNodeList() {
		return makeNodeList("");
	}

	// Creates a nodelist with the default parent node
	public Collection makeNodeList(String expString) 
	{
		BaseNode root = nodeSupplier.getRootNode();
		root.setIsRoot(true);
		
		if (showRoot)
		{
			addNodeToList(root, startLevel, makeKey(root.getId()), true, true);		
		}
		
		return makeNodeList(root.getId(), expString);
	}
	
	public Collection makeNodeList(Integer parentNode, String expString)
	{
		intTreeLines.setSize(50);
		FillTree(parentNode, startLevel, expString, new String(""));
		return ret;
	}
	public Collection makeNodeList(BaseNode parentNode, String expString)
	{
		intTreeLines.setSize(50);
		FillTree(parentNode.getId(), startLevel, expString, new String(""));
		return ret;
	}

	public void setNodeSupplier(INodeSupplier nodeSupplier)
	{
		this.nodeSupplier = nodeSupplier;
	}

	private String makeKey(Integer id)
	{
		if(id == null)
			return "";
		
		return "/" + String.valueOf(id.intValue()).trim() + "/";
	}

	private void FillTree(Integer parent, int level, String expkey, String makekey)
	{
		Collection leafs = new ArrayList();
		Collection roots = new ArrayList();;

		// Every time this method is called, we are one step
		// deeper in the tree.
		level++;

		// Get all the children to this node
		try
		{
			roots = nodeSupplier.getChildContainerNodes(parent);
			leafs = nodeSupplier.getChildLeafNodes(parent);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		boolean blnHasDocs = leafs.size() > 0;

		// Create expansion key to this node
		String parentKey = makeKey(parent);
		makekey += parentKey;

		// loop through all container nodes.  	
		Iterator rIter = roots.iterator();
		while (rIter.hasNext())
		{
			BaseNode thisNode = (BaseNode) rIter.next();
			String nodeKey = makeKey(thisNode.getId());

			// TreeLines check  		
			if (showTreeLines)
			{
				intTreeLines.add(level, new Integer(3));
				if (!rIter.hasNext())
				{
					if (blnHasDocs == false)
					{
						intTreeLines.remove(level);
						intTreeLines.add(level, new Integer(2));
					}
				}
			}

			// Kolla om denna folder finns med i expansionsnyckeln. I så
			// fall låter vi rekursionen göra sitt.
			if ( expkey.indexOf(nodeKey) != -1)
			{
				addNodeToList(thisNode, level, makekey, true, true);
				// Open folder       

				// TreeLines check  		
				if (showTreeLines)
				{
					if (!rIter.hasNext())
					{
						if (blnHasDocs == false)
						{
							intTreeLines.remove(level);
							intTreeLines.add(level, new Integer(0));
						}
					}
					if (((Integer) intTreeLines.get(level)).intValue() == 3)
					{
						intTreeLines.remove(level);
						intTreeLines.add(level, new Integer(1));
					}
				}

				// older is open, recursion    		
				FillTree(thisNode.getId(), level, expkey, makekey);
			}
			else
			{
				//  Folder är stängd
				addNodeToList(thisNode, level, makekey, false, true);
			}

		}

		// Documents
		if (blnHasDocs)
		{
			rIter = leafs.iterator();
			while (rIter.hasNext())
			{
				BaseNode thisNode = (BaseNode) rIter.next();
				if (showTreeLines)
				{
					intTreeLines.remove(level);
					intTreeLines.add(level, new Integer(3));
					if (!rIter.hasNext())
					{
						intTreeLines.remove(level);
						intTreeLines.add(level, new Integer(2));
					}
				}
				addNodeToList(thisNode, level, "", false, false);
			}		
		}


		intTreeLines.remove(level);
		intTreeLines.add(level, new Integer(0));

	}


	/**
	 * Method addNodeToList
	 * @param thisNode
	 * @param level
	 * @param makekey
	 * @param b
	 */
	private void addNodeToList(BaseNode thisNode, int level,	String makekey, boolean isOpen, boolean isContainer) 
	{
			String newKey = makekey;
			String thisKey =  makeKey(thisNode.getId());
			int cnt = 0;
			Vector tree = null;
			if (level > -1) tree = new Vector(level);
			Iterator i  = intTreeLines.iterator();
			while (i.hasNext() && cnt <= level)
				tree.add(cnt++, i.next());
			
			if (!isOpen)
				newKey += thisKey;
			
			thisNode.setTreeStuff(tree);
			thisNode.setOpen(isOpen);
			thisNode.setThisKey(thisKey);
			thisNode.setOpenCloseKey(newKey);
			thisNode.setLevel(level);
			thisNode.setContainer(isContainer);
			
			try
			{
				// has children check (may belong here??)
				if (isContainer && nodeSupplier.hasChildren())
				{
					thisNode.setChildren(nodeSupplier.hasChildren(thisNode.getId()));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
				
			ret.add(thisNode);
	}

	/**
	 * Returns the showRoot.
	 * @return boolean
	 */
	public boolean getShowRoot()
	{
		return showRoot;
	}

	/**
	 * Sets the showRoot.
	 * @param showRoot The showRoot to set
	 */
	public void setShowRoot(boolean showRoot)
	{
		this.showRoot = showRoot;
	}

}
