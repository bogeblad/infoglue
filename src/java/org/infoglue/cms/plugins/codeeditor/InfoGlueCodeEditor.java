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
 
package org.infoglue.cms.plugins.codeeditor;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class InfoGlueCodeEditor extends JFrame implements InfoGlueCodeEditorController
{
	
	public static void main(String[] args)
	{
		new InfoGlueCodeEditor().setVisible(true);
	}	
	
	public InfoGlueCodeEditor()
	{
		//KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(new FocusChangeListener());
    
		this.setSize(500, 400);
		setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		setTitle("InfoGlue Template Editor");
		this.getContentPane().setBackground(Color.WHITE);
		
		InfoGlueTextArea textArea = new InfoGlueTextArea(this);
		textArea.setSize(500, 400);
		textArea.setBounds(5, 5, 500, 400);
		
		JScrollPane areaScrollPane = new JScrollPane(textArea);
		areaScrollPane.setPreferredSize(new Dimension(500, 400));
		areaScrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
		
		textArea.setScrollPane(areaScrollPane);

		this.getContentPane().add(areaScrollPane);
	}

	/**
     * This method does nothing right now
     */
    
    public void executeSave(String text)
    {
    	System.out.println("Going to tell the world that the user wants to save...");
    }
	
	/**
     * This method does nothing right now
     */
    
    public String getDeliverySettingsUrl()
    {
    	System.out.println("Going to return a url");
    	return "http://localhost:8080/infoglueDeliverDev/ViewApplicationSettings!getTemplateLogicMethods.action";
    }
}