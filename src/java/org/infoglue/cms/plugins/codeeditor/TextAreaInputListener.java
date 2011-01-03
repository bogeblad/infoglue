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

import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;

public class TextAreaInputListener extends KeyAdapter
{
	private InfoGlueTextArea infoGlueTextArea = null;
	private boolean isControlKeyActive = false;
	private boolean isShiftKeyActive = false;
	
	public TextAreaInputListener(InfoGlueTextArea infoGlueTextArea)
	{
		this.infoGlueTextArea = infoGlueTextArea;
	}
	
	public void keyPressed(KeyEvent e)
	{
		//System.out.println("Event:" + e.getKeyChar() + ":" + e.getKeyCode());
		//System.out.println("this.isControlKeyActive" + this.isControlKeyActive);
		
		char key = e.getKeyChar();
		
		if(key == '.')
		{
			try
			{
				int endPosition = this.infoGlueTextArea.getCaretPosition() - 1;
				//String text = this.infoGlueTextArea.getText();
				String text = this.infoGlueTextArea.getDocument().getText(this.infoGlueTextArea.getDocument().getStartPosition().getOffset(), this.infoGlueTextArea.getDocument().getEndPosition().getOffset());
				//System.out.println("Text1:" + this.infoGlueTextArea.getText().length());
				//System.out.println("Text2:" + this.infoGlueTextArea.getDocument().getText(0, this.infoGlueTextArea.getDocument().getLength()).length());
				
				StringBuffer sb = new StringBuffer();
				int position = endPosition;
				while(true && position > -1)
				{
					char c = text.charAt(position);
					if(c == '$' || c == ' ')
					{
						sb.insert(0, c);
						break;
					}
					else
					{
						sb.insert(0, c);
					}
					position--;
				}
				
				String keyword = sb.toString();
				//System.out.println("keyword:" + keyword + ": Length=" + keyword.length());	
				if(keyword.equals("$templateLogic") || keyword.equals("$!templateLogic") || keyword.equals("${templateLogic") || keyword.equals("$!{templateLogic"))
				{
					//System.out.println("Trigger codeHelper...");
					int x = 0;
					int y = 0;
					try
					{
						Point caretPosition = this.infoGlueTextArea.getCaret().getMagicCaretPosition();
						//System.out.println("caretPosition:" + caretPosition);
						
						x = caretPosition.x;
						y = caretPosition.y;
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
					
					this.infoGlueTextArea.openTemplateLogicCodeHelper(x, y);
					this.isControlKeyActive = false;
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			//System.out.println("The component with focus is:" + KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
		}
		else if(e.getKeyCode() == 52 && this.isControlKeyActive)  //$-sign
		{
			int x = 0;
			int y = 0;
			try
			{
				Point caretPosition = this.infoGlueTextArea.getCaret().getMagicCaretPosition();
				//System.out.println("caretPosition:" + caretPosition);
				
				x = caretPosition.x;
				y = caretPosition.y;
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			
			this.infoGlueTextArea.openVelocityVariableTemplateCodeHelper(x, y);
			this.isControlKeyActive = false;

		}
		else if(key == '#')
		{
			int x = 0;
			int y = 0;
			try
			{
				Point caretPosition = this.infoGlueTextArea.getCaret().getMagicCaretPosition();
				//System.out.println("caretPosition:" + caretPosition);
				
				x = caretPosition.x;
				y = caretPosition.y;
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			
			this.infoGlueTextArea.openVelocityTemplateCodeHelper(x, y);
		}
		else if(key == '\t')
		{
			String selectedText = this.infoGlueTextArea.getSelectedText();
			//int startIndex = this.infoGlueTextArea.getSelectionStart();
			//int endIndex   = this.infoGlueTextArea.getSelectionEnd();
			
			//System.out.println("selectedText:" + selectedText);
			if(selectedText != null)
			{
				String linebreak = System.getProperty("line.separator"); 
				//System.out.println("linebreak:" + linebreak);
       			StringBuffer sb = new StringBuffer();
				StringTokenizer st = new StringTokenizer(selectedText, linebreak, true);
				while (st.hasMoreTokens()) 
				{
					String row = st.nextToken();
         			//System.out.println("Row:" + row);
         			if(this.isShiftKeyActive)
         			{
         				row = row.replaceFirst("" + '\t', "");
         				sb.append(row);
         			}
         			else
         			{
         				sb.append('\t' + row);
         			}
     			}
     			
     			this.infoGlueTextArea.replaceSelection(sb.toString());
			}
		}
		else if(e.getKeyCode() == 83)
		{
			//System.out.println("this.isControlKeyActive" + this.isControlKeyActive);
			if(this.isControlKeyActive)
			{
				//System.out.println("Save text");
				this.infoGlueTextArea.controller.executeSave(this.infoGlueTextArea.getText());
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_SHIFT)
		{
			//System.out.println("Shift is pressed");
			this.isShiftKeyActive = true;
		}
		else if(e.getKeyCode() == KeyEvent.VK_CONTROL)
		{
			//System.out.println("Control is pressed");
			this.isControlKeyActive = true;
		}
		
		//this.infoGlueTextArea.setCharacterAttributes(this.infoGlueTextArea.getParagraphAttributes(), false);
	}
	
	
	public void keyReleased(KeyEvent e)
	{
		//System.out.println("Event:" + e.getKeyChar() + ":" + e.getKeyCode());
		
		char key = e.getKeyChar();
		
		if(e.getKeyCode() == KeyEvent.VK_SHIFT)
		{
			//System.out.println("Shift is released");
			this.isShiftKeyActive = false;
		}
		else if(e.getKeyCode() == KeyEvent.VK_CONTROL)
		{
			//System.out.println("Control is released");
			this.isControlKeyActive = false;
		}
	}
	
	
}