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
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * This class is the dialog showing available methods in the templateLogic-object.
 */

public class CodeHelperDialog extends JDialog implements MouseListener, KeyListener
{
	private Frame parentFrame = null;
    private InfoGlueTextArea infoGlueTextArea = null;
    private JList list = new JList();
    
    
    public CodeHelperDialog(Frame parentFrame, InfoGlueTextArea infoGlueTextArea, Object[] items)
    {
        super(parentFrame, true);
        this.parentFrame = parentFrame;
        this.infoGlueTextArea = infoGlueTextArea;
        
        DefaultListModel model = new DefaultListModel();
		for (int i=0; i<items.length; i++)
		{	
			model.addElement(items[i]);
		}
		this.list.setModel(model);
		JScrollPane scroller = new JScrollPane();
		scroller.getViewport().add(list);
		this.getContentPane().add(scroller);
		this.list.setFont(new Font("Courier", Font.PLAIN, 12));
		this.list.addKeyListener(this);
		this.list.addMouseListener(this);
		this.setSize(500, 200);
		this.list.requestFocus();
    }
    
    public void mousePressed(MouseEvent event)
	{
		//this.infoGlueTextArea.insert(this.list.getSelectedValue().toString(), this.infoGlueTextArea.getCaretPosition());
		insertSpecialText(this.infoGlueTextArea.getCaretPosition(), this.list.getSelectedValue().toString());
			
		this.list.setSelectedIndex(0);
		this.setVisible(false);
		this.infoGlueTextArea.requestFocus();
	}
	
	public void mouseReleased(MouseEvent event)
	{
		//System.out.println("mouseReleased:" + event);
	}

	public void mouseClicked(MouseEvent event)
	{
		//System.out.println("mouseClicked:" + event);
	}

	public void mouseEntered(MouseEvent event)
	{
		//System.out.println("mouseEntered:" + event);
	}

	public void mouseExited(MouseEvent event)
	{
		//System.out.println("mouseExited:" + event);
	}
	
	public void keyTyped(KeyEvent e)
	{
		//System.out.println("Hepp:" + e.getKeyChar());
		if(e.getKeyChar() == KeyEvent.VK_ENTER)
		{
			//this.infoGlueTextArea.insert(this.list.getSelectedValue().toString(), this.infoGlueTextArea.getCaretPosition());
			insertSpecialText(this.infoGlueTextArea.getCaretPosition(), this.list.getSelectedValue().toString());
			
			this.setVisible(false);
			this.list.setSelectedIndex(0);
			this.infoGlueTextArea.requestFocus();
		}
		else
		{
			//this.infoGlueTextArea.insert("" + e.getKeyChar(), this.infoGlueTextArea.getCaretPosition());
			insertSpecialText(this.infoGlueTextArea.getCaretPosition(), "" + e.getKeyChar());
			
			this.setVisible(false);
			this.list.setSelectedIndex(0);
			this.infoGlueTextArea.requestFocus();
		}
	}
	
	public void keyPressed(KeyEvent e)
	{
		//System.out.println("Hopp:" + e.getKeyChar());
	}

	public void keyReleased(KeyEvent e)
	{
		//System.out.println("Happ:" + e.getKeyChar());
	}
	
	public void insertSpecialText(int position, String text)
	{
		//SimpleAttributeSet s = new SimpleAttributeSet();
		/*StyleConstants.setFontFamily(s, "Courier");
		StyleConstants.setFontSize(s, 12);
		StyleConstants.setForeground(s, Color.red);
		StyleConstants.setBackground(s, new Color(240, 240, 240));
		*/
		
		SimpleAttributeSet attribs = new SimpleAttributeSet(this.infoGlueTextArea.getParagraphAttributes());
		StyleConstants.setBackground(attribs, new Color(230, 230, 230));
				
		try
		{
			this.infoGlueTextArea.getDocument().insertString(position, text, attribs);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		this.infoGlueTextArea.setCharacterAttributes(this.infoGlueTextArea.getParagraphAttributes(), false);
	}


}

