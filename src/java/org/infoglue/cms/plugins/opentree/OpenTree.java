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

import java.awt.Color;
import java.awt.FlowLayout;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import netscape.javascript.JSObject;

/**
 * This is the applet-class. It is very simple and apart from initializing the tree
 * it consists mainly of methods for the applet to influence and be influenced by its surroundings.
 */
 
public class OpenTree extends JApplet
{
	
	private Icon customOpenIcon   = null;
	private Icon customClosedIcon = null;
  	private Icon customLeafIcon   = null;

	private TreeNode rootNode;
   	protected JTree nodeTree;
   	private JScrollPane sp;
   	private Controller controller;
   	private Integer repositoryId;
   	private String entityName;
   	private String hideLeafs;
   	
   	private Integer currentNodeId;

	public void init()
	{
        String serverAddress = null;
        Color bgColor = null;
        
        try
        { 
			int rColor = Integer.parseInt(this.getParameter("bgColorR"));
	        int gColor = Integer.parseInt(this.getParameter("bgColorG"));
	        int bColor = Integer.parseInt(this.getParameter("bgColorB"));
	        bgColor = new Color(rColor,gColor,bColor);
			
			this.entityName   = this.getParameter("entityName");
	        this.repositoryId = new Integer(this.getParameter("repositoryId"));
	        this.hideLeafs    = this.getParameter("hideLeafs");
	        
	        URL codeBase = this.getCodeBase();
			String applicationPath = this.getDocumentBase().getPath().substring(0, this.getDocumentBase().getPath().lastIndexOf("/"));
			System.out.println("applicationPath:" + applicationPath);
	        serverAddress = codeBase.getProtocol() + "://" + codeBase.getHost() + ":" + codeBase.getPort() + "" + applicationPath + "/" +  entityName + "TreeService"; 
	        String imageBaseURL = codeBase.getProtocol() + "://" + codeBase.getHost() + ":" + codeBase.getPort() + "" + applicationPath + "/"; 
	        
			String folderOpenImage   = imageBaseURL + "images/" + entityName + "folderOpen.gif";
			String folderClosedImage = imageBaseURL + "images/" + entityName + "folderClosed.gif";
			String documentImage     = imageBaseURL + "images/" + entityName + "document.gif";
			
			customOpenIcon   = new ImageIcon(new URL(folderOpenImage));
	        customClosedIcon = new ImageIcon(new URL(folderClosedImage));
	  		customLeafIcon   = new ImageIcon(new URL(documentImage));
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
        try
        {  	
	        controller	= new Controller(serverAddress, entityName, repositoryId, hideLeafs);
	        rootNode    = controller.getRootNode();
	   	    nodeTree    = new JTree(rootNode);
			controller.setNodeTree(this.nodeTree);
			
			try{ UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); SwingUtilities.updateComponentTreeUI(nodeTree); } catch (Exception e){ e.printStackTrace(); }
			
			DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		    renderer.setOpenIcon(customOpenIcon);
	    	renderer.setClosedIcon(customClosedIcon);
	    	renderer.setLeafIcon(customLeafIcon);
	    	renderer.setBackgroundNonSelectionColor(bgColor);
	    	nodeTree.setCellRenderer(renderer);
	   	
			FlowLayout flowManager = new FlowLayout(FlowLayout.LEFT);
			flowManager.setHgap(0);
			flowManager.setVgap(0);
			
			sp = new JScrollPane(nodeTree);
			sp.setBorder(null);
			getContentPane().setBackground(bgColor);
			getContentPane().add(sp, "Center");
			
			nodeTree.setBackground(bgColor);
			nodeTree.addTreeSelectionListener(new NodeTreeSelectionListener(this));
	        CMSTreeListener cmsTreeListener = new CMSTreeListener(nodeTree, controller);
	   	    nodeTree.addMouseListener(cmsTreeListener);
	   	    nodeTree.addTreeWillExpandListener(cmsTreeListener);
	   		
	   	}
        catch(Exception e)
        {
        	FlowLayout flowManager = new FlowLayout(FlowLayout.LEFT);
			flowManager.setHgap(0);
			flowManager.setVgap(0);
			getContentPane().setBounds(5, 5, 150, 300);
        	getContentPane().setLayout(flowManager);
			getContentPane().setBackground(bgColor);
			JTextPane errorMessage = new JTextPane();
			errorMessage.setBackground(bgColor);
			errorMessage.setText("Error:\r\nAn server-error made it\r\nimpossible to show the tree. \r\nPlease make sure that there are\r\nat least one repository available. \r\nIf the problem persists report it to \r\nyour technical straff.");
			
			getContentPane().add(errorMessage);
        	e.printStackTrace();
        }
        
        notifyLoaded();
	}

    /**
     * This method is used to call the external javascript that updates the
     * mainarea with a new screen. It's used to show the node you just clicked on
     * in the tree.
     */
    
    public void openMainArea(CMSNode node)
    {
        try
        {
            JSObject win = JSObject.getWindow(this);
            Object[] args = {node.getId().toString(), this.repositoryId.toString(), getPath(node, new StringBuffer())};
            String functionName = "loadMainArea";
            //System.out.println("Calling function " + functionName + " with arguments " + args[0] + ", " + args[1]);
            win.call(functionName, args);
            //System.out.println("Setting the content for easy access:" + node.getId());
            this.currentNodeId = node.getId();
        }
        catch(Exception e)
        {
            System.err.println("An error occurred while we tried to call a javascript: " + e);
        }
    }    


    /**
     * This method is used to call the external javascript that updates the
     * mainarea with a new screen. It's used to show the node you just clicked on
     * in the tree.
     */
    
    public void notifyLoaded()
    {
        System.out.println("Going to tell the world that I'm fully loaded...");
        try
        {
            JSObject win = JSObject.getWindow(this);
            Object[] args = {};
            String functionName = "notifyIsLoaded";
            //System.out.println("Calling function " + functionName);
            win.call(functionName, args);
        }
        catch(Exception e)
        {
            System.err.println("An error occurred while we tried to call a javascript: " + e);
			e.printStackTrace();
        }
    }    


	public void refreshTreeNode(Integer nodeId, Integer changeTypeId, Integer addedNodeId)
	{
		//System.out.println("RefreshNode was called with id:" + nodeId + " and changeTypeId:" + changeTypeId);
		controller.refreshNode(nodeId, changeTypeId, addedNodeId);		
	}
	
	public void refreshTreeNode(int nodeId, int changeTypeId, int addedNodeId)
	{
		//System.out.println("RefreshNode was called with id:" + nodeId + " and changeTypeId:" + changeTypeId);
		controller.refreshNode(new Integer(nodeId), new Integer(changeTypeId), new Integer(addedNodeId));		
	}
    
    private StringBuffer getPath(CMSNode node, StringBuffer path)
    {
		
    	path.insert(0, "/" + node.getName());
    	//System.out.println("path:" + path);
    	if(node.getParent() != null)
    	{
    		getPath((CMSNode)node.getParent(), path);
    	}
    		
    	return path;
    }

	/**
	 * This method is called to resize the applet when the frame it is in is changed.
	 */
	
	public void setSize(int width, int height)
	{
		//System.out.println("Resizing the entire applet to: " + width + ":" + height);
   		super.setSize(width,height);
   		//this.setSize(width,height);
   		//this.setBounds(0,0,width,height);
   		validate();
   		//this.getContentPane().setSize(width-2,height-2);
   		//this.getContentPane().setBounds(1,1,width-2,height-2);
   		//getContentPane().validate();
   		//getContentPane().repaint();
		//sp.setSize(width,height);
		//sp.validate();
		//sp.repaint();
		//validate();
	}


}
