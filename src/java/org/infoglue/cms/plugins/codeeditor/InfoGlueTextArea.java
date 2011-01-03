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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

/**
 * This class is the basic for the editor.
 */

public class InfoGlueTextArea extends JTextPane implements DocumentListener //JTextArea
{
	protected InfoGlueCodeEditorController controller = null;
	protected JScrollPane scrollPane = null; 
	private Font standardFont = new Font("Courier", Font.PLAIN,  12);
	private CodeHelperDialog methodsCodeHelperDialog = null;
	private CodeHelperDialog velocityCodeHelperDialog = null;
	private CodeHelperDialog velocityVariableCodeHelperDialog = null;
	
	/**
	 * The constructor for this editor.
	 */
	
	public InfoGlueTextArea(InfoGlueCodeEditorController infoGlueCodeEditorController)
	{	
		Style styDefault = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(styDefault,"Courier");
		StyleConstants.setFontSize(styDefault, 14);
		StyleConstants.setForeground(styDefault, Color.black);
		this.setLogicalStyle(styDefault);
		SimpleAttributeSet attribs = new SimpleAttributeSet(this.getParagraphAttributes());
		StyleConstants.setBackground(attribs, new Color(230, 230, 230));
		this.setCharacterAttributes(attribs, false);
		
		setTabs();
		this.addKeyListener(new TextAreaInputListener(this));
		this.controller = infoGlueCodeEditorController;
	}

	/**
	 * This method sets the tabs on the JTextPane
	 */
	
	private void setTabs()
	{
		List list = new ArrayList();
	    
	    float pos = 0;
	    for(int i=0; i<20; i++)
	    {
		    pos += 20;
		    int align 	= TabStop.ALIGN_LEFT;
		    int leader	= TabStop.LEAD_NONE;
		    TabStop tstop = new TabStop(pos, align, leader);
		    list.add(tstop);
	    }
	    	    	    
	    TabStop[] tstops = (TabStop[])list.toArray(new TabStop[0]);
	    TabSet tabs = new TabSet(tstops);
	    
	    Style style = this.getLogicalStyle();
	    StyleConstants.setTabSet(style, tabs);
	    this.setLogicalStyle(style);
	}
	
	/**
	 * This method fetches a list of methods available to the template. It does this
	 * by asking the delivery-engine (calling a service). This way the list is allways up2date.
	 */	
	private List getTemplateLogicMethodList()
	{
		List methods = new ArrayList();
		
		try
		{
			String xml = HttpUtilities.getUrlContent(this.controller.getDeliverySettingsUrl());
			//System.out.println("xml:" + xml);
			
			int offset = 0; 
			int methodIndex = xml.indexOf("<method>", offset);
			int methodEndIndex = xml.indexOf("</method>", offset);
			
			while(methodIndex > -1 && methodEndIndex > -1)
			{
				String methodDescription = xml.substring(methodIndex + 8, methodEndIndex);
				methods.add(methodDescription);
				
				offset = methodEndIndex + 9; 
				methodIndex = xml.indexOf("<method>", offset);
				methodEndIndex = xml.indexOf("</method>", offset);
			}		
		}
		catch(Exception e)
		{
			System.out.println("Exception:" + e);
			e.printStackTrace();
		}
		
		return methods;
	}
		
	public void openTemplateLogicCodeHelper(int xPosition, int yPosition)
	{
		if(this.methodsCodeHelperDialog == null)
		{
			List methods = getTemplateLogicMethodList();
			List listItems = new ArrayList();
			Iterator i = methods.iterator();
			while(i.hasNext())
			{
				String method = "" + (String)i.next();
				listItems.add(method);
			}	
	    	
	    	methodsCodeHelperDialog = new CodeHelperDialog(getFrame(this), this, listItems.toArray());
		}
		
		int xLocation = getFrame(this).getLocationOnScreen().x + xPosition + 10;
		int yLocation = getFrame(this).getLocationOnScreen().y + yPosition + 24;

		if(xLocation > this.scrollPane.getWidth())
			xLocation = this.scrollPane.getWidth();

		if(yLocation > this.scrollPane.getHeight())
			yLocation = this.scrollPane.getHeight();
		
		this.methodsCodeHelperDialog.setLocation(xLocation, yLocation);
		this.methodsCodeHelperDialog.setVisible(true);
	}
	

	public void openVelocityTemplateCodeHelper(int xPosition, int yPosition)
	{
		if(this.velocityCodeHelperDialog == null)
		{
			List listItems = new ArrayList();
			listItems.add("if()");
	        listItems.add("else()");
	        listItems.add("elseif()");
	        listItems.add("end()");
	        listItems.add("foreach()");
	        listItems.add("if()");
	        listItems.add("include()");
	        listItems.add("macro()");
	        listItems.add("parse()");
	        listItems.add("set()");
	        listItems.add("stop()");
			
			this.velocityCodeHelperDialog = new CodeHelperDialog(getFrame(this), this, listItems.toArray());
		}
		
		int xLocation = getFrame(this).getLocationOnScreen().x + xPosition + 10;
		int yLocation = getFrame(this).getLocationOnScreen().y + yPosition + 24;

		if(xLocation > this.scrollPane.getWidth())
			xLocation = this.scrollPane.getWidth();

		if(yLocation > this.scrollPane.getHeight())
			yLocation = this.scrollPane.getHeight();


		this.velocityCodeHelperDialog.setLocation(xLocation, yLocation);
		this.velocityCodeHelperDialog.setVisible(true);
	}

	public void openVelocityVariableTemplateCodeHelper(int xPosition, int yPosition)
	{
		if(this.velocityVariableCodeHelperDialog == null)
		{
			List listItems = new ArrayList();
			listItems.add("templateLogic");
			
			this.velocityVariableCodeHelperDialog = new CodeHelperDialog(getFrame(this), this, listItems.toArray());
		}
		
		int xLocation = getFrame(this).getLocationOnScreen().x + xPosition + 10;
		int yLocation = getFrame(this).getLocationOnScreen().y + yPosition + 24;

		if(xLocation > this.scrollPane.getWidth())
			xLocation = this.scrollPane.getWidth();

		if(yLocation > this.scrollPane.getHeight())
			yLocation = this.scrollPane.getHeight();

		this.velocityVariableCodeHelperDialog.setLocation(xLocation, yLocation);
		this.velocityVariableCodeHelperDialog.setVisible(true);
	}


	private Frame getFrame(Component parent)
	{
		while (!(parent instanceof Frame))
		{
			parent = ((Component)parent).getParent();
			//System.out.println("parent:" + parent.getClass().getName());
		}
		
		//System.out.println("returning :" + parent.getClass().getName());
		//System.out.println("Position:" + parent.getLocationOnScreen().x + ":" + parent.getLocationOnScreen().y);
		return (Frame)parent;
	}
	

	public void setScrollPane(JScrollPane scrollPane)
	{
		this.scrollPane = scrollPane;
		
		Rule columnView = new Rule(Rule.HORIZONTAL, true);
	    columnView.setPreferredWidth(500);
	    Rule rowView = new Rule(Rule.VERTICAL, true);
	    rowView.setPreferredHeight(20000);
	    scrollPane.setRowHeaderView(rowView);
	}


	// Handle insertions into the text field
	public void insertUpdate( DocumentEvent event )
	{
	}

	// Handle deletions	from the text field
	public void removeUpdate( DocumentEvent event )
	{
	}

	// Handle changes to the text field
	public void changedUpdate( DocumentEvent event )
	{
	}
	
	/**   
	 * overridden from JEditorPane   
	 * to suppress line wraps   
	 * @see setSize   
	 */  
	
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	} 
	
	/**   
	 * overridden from JEditorPane   
	 * to suppress line wraps   
	 *   
	 * @see getScrollableTracksViewportWidth   
	 */
	
	public void setSize(Dimension d)
	{
		if (d.width < getParent().getSize().width)
		{
			d.width = getParent().getSize().width;
		}
		super.setSize(d);
	}
	
}
