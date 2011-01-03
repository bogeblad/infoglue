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
import javax.swing.JApplet;
import javax.swing.JScrollPane;

import netscape.javascript.JSObject;


public class InfoGlueCodeEditorApplet extends JApplet implements InfoGlueCodeEditorController
{
	private InfoGlueTextArea textArea = null;
	
	private String attributeId         = "";
	private String deliverySettingsUrl = "";
	

	public void init()
	{
		try
        { 
			attributeId         = this.getParameter("attributeId");
			deliverySettingsUrl = this.getParameter("deliverySettingsUrl");
			System.out.println("attributeId:" + attributeId);
			System.out.println("deliverySettingsUrl:" + deliverySettingsUrl);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
                
		this.setSize(500, 400);
		this.getContentPane().setBackground(Color.WHITE);
		
		textArea = new InfoGlueTextArea(this);
		textArea.setSize(700, 600);
		textArea.setBounds(0, 0, 700, 600);
		
		JScrollPane areaScrollPane = new JScrollPane(textArea);
		areaScrollPane.setPreferredSize(new Dimension(500, 400));
		areaScrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
		
		textArea.setScrollPane(areaScrollPane);
		
		this.getContentPane().add(areaScrollPane);
		
		Object[] args = {this.attributeId};
		String text = (String)callJavascript("getValue", args);
		this.setText(text);

		callJavascript("setAppletIsActive", args);
	}
	
	/**
     * This method is used by external javascripts to set the initial value
     */
    
    public void setText(String text)
    {
        //System.out.println("Trying to set the initial text through javascript:" + text);
        this.textArea.setText(text);
    }  
    
    /**
     * This method is used by external javascripts to get the text
     */
    
    public String getText()
    {
        return this.textArea.getText();
    }    
    
    
    /**
     * This method returns a string with the url to the delivery-engine settings service.
     */
    
    public String getDeliverySettingsUrl()
    {
    	return this.deliverySettingsUrl;
    }
    
    /**
     * This method calls external javascript to set value and submit the form
     */
    
    public void executeSave(String text)
    {
    	//System.out.println("Going to tell the world that the user wants to save..." + text);
        try
        {
            JSObject win = JSObject.getWindow(this);
            Object[] args = {this.attributeId, text};
            String functionName = "saveValue";
            //System.out.println("Calling function " + functionName);
            win.call(functionName, args);
        }
        catch(Exception e)
        {
            System.out.println("An error occurred while we tried to call a javascript: " + e);
        }
    }

	/**
	 * This method calls external javascript to interact with the webpage
	 */
    
	public Object callJavascript(String functionName, Object[] args)
	{
		Object returnValue = null;
		try
		{
			JSObject win = JSObject.getWindow(this);
			//System.out.println("Calling function " + functionName);
			returnValue = win.call(functionName, args);
		}
		catch(Exception e)
		{
			System.out.println("An error occurred while we tried to call a javascript: " + e);
			e.printStackTrace();
		}
		return returnValue;
	}

}