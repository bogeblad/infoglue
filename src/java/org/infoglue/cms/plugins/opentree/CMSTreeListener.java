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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;


//import se.sprawl.cms.entities.content.ContentVO;

public class CMSTreeListener extends MouseAdapter implements TreeWillExpandListener
{        
    private JTree tree = null;
    private Controller controller = null;
    
    public CMSTreeListener(JTree tree, Controller controller)
    {
        this.tree = tree;
        this.controller = controller;
    }
    
    public void mouseClicked(MouseEvent e) 
    {
        int selRow = tree.getRowForLocation(e.getX(), e.getY());
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        if(selRow != -1) 
        {
            if(e.getClickCount() == 1) 
            {
                //System.out.println("There was no doubleclick so we don't do anything... for now..");
                //Add later so that this triggers the call to the javascript
                //mySingleClick(selRow, selPath);
            }
			/*
            else if(e.getClickCount() == 2) 
            {
                actOnDoubleClick(selRow, selPath);
            }
            */
        }
    }
    
    /*
    private void actOnDoubleClick(int selRow, TreePath selPath)
    {
        CMSContent cmsContent = (CMSContent)selPath.getLastPathComponent();
                    
        contentController.addContentChildrenToParent(cmsContent);
    }
    */

    public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException 
    {
        //System.out.println("The treeWillExpand fired:" + tree.getSize() + ":" + tree.getPreferredSize() + ":" +  tree.getPreferredScrollableViewportSize());
     /*
        CMSContent cmsContent = (CMSContent)e.getPath().getLastPathComponent();        
        cmsContent.removeChildren();
        contentController.addContentChildrenToParent(cmsContent);
    */
    }

    public void treeWillCollapse(TreeExpansionEvent e) 
    {
        //System.out.println("The treeWillCollapse fired..");
    }
 }
