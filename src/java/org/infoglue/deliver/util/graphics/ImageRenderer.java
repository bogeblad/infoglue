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

package org.infoglue.deliver.util.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.ImageIO;


/**
 * This class demonstrates how to line-break and draw a paragraph 
 * of text using LineBreakMeasurer and TextLayout.
 *
 * This class constructs a LineBreakMeasurer from an
 * AttributedCharacterIterator.  It uses the LineBreakMeasurer
 * to create and draw TextLayouts (lines of text) which fit within 
 * the Component's width.
 */

public class ImageRenderer //extends JFrame
{
	public static final int ALIGN_LEFT 		= 0;
	public static final int ALIGN_CENTER 	= 1;
	public static final int ALIGN_RIGHT 	= 2;

	private int canvasWidth  	= 300;
    private int canvasHeight 	= 100;
    private int textStartPosX	= 5;
    private int textStartPosY	= 25;
    private int textWidth		= 300;
    private int textHeight		= 100;
    
    private String fontName 	= "Dialog";
    private int fontStyle 		= Font.PLAIN;
    private int fontSize 		= 12;
    
    private int alignment       = ALIGN_LEFT;
        
    private Color backgroundColor = null;
    private Color foreGroundColor = null;
    
    private String backgroundImageUrl = null;
    
    /*
    Frame frame = null;
	
	public static void main(String[] args)
	{
		ImageRenderer ir = new ImageRenderer();
		
		ir.setCanvasWidth(300);
    	ir.setCanvasHeight(100);
    	ir.setTextStartPosX(5);
    	ir.setTextStartPosY(25);
    	ir.setTextWidth(300);
    	ir.setTextHeight(100);
		ir.setAlignment(ALIGN_RIGHT);
		
		ir.setFontName("Verdana");
		ir.setFontStyle(Font.BOLD);
		ir.setFontSize(20);
		
		ir.setForeGroundColor(Color.BLACK);
		ir.setBackgroundColor(Color.WHITE);
		//ir.setBackgroundImageUrl("http://localhost:8080/infoglueDeliverDev/digitalAssets/94_1080909656343_gradient.jpg");
		
		//ir.listAvailableFonts();
		ir.setSize(400, 400);
		ir.setVisible(true);
	}

	private void listAvailableFonts()
	{
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    String[] fontNames = env.getAvailableFontFamilyNames();
	    //logger.info("Available Fonts:");
	    for(int i=0; i<fontNames.length; i++)
	    {
	    	//logger.info("  " + fontNames[i]);
	    	System.out.println("  " + fontNames[i]);
	    }
	}

	
	public void paint(Graphics g) 
	{
	    Graphics2D g2d = (Graphics2D)g;
	    
	    try
	    {
	    	drawText(g2d, "Detta är ett test som bör brytas");

	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
  	}
	

	public ImageRenderer()
	{
		//frame = new Frame();
		//frame.addNotify();
	}
	*/
	
	/**
	 * This method generates a gif-image from the send in string with the given width/height.
	 */

	public void generateGifImageFromText(String file, String text, String encoding) throws Exception
	{
		BufferedImage image = new BufferedImage(this.canvasWidth, this.canvasHeight, BufferedImage.TYPE_INT_ARGB);
		
		//if(!encoding.equalsIgnoreCase("utf-8"))
		//	text = new String(text.getBytes(encoding), "UTF-8");
		
		drawText((Graphics2D)image.getGraphics(), text); 
		Hashtable arguments = new Hashtable();
		arguments.put("encoding", "websafe"); 
		//logger.info("Going to generate gif to disc..."); 
		//new GifEncoder().encode(image, new DataOutputStream(new FileOutputStream(file)), arguments);
		File outputFile = new File(file);
		javax.imageio.ImageIO.write(image, "PNG", outputFile);
	}


	private void drawText(Graphics2D g2d, String text) throws Exception
	{
		Font font = FontSaver.create(this.fontName, this.fontStyle, this.fontSize);

		if(this.backgroundImageUrl != null)
		{	
			URL url = new URL(this.backgroundImageUrl);       
			BufferedImage bufferedImage = ImageIO.read(url);
			g2d.drawImage(bufferedImage,0,0, null);
		}
		else
		{	
			g2d.setBackground(this.backgroundColor);
			g2d.setPaint(this.backgroundColor); 
			g2d.fillRect(0, 0, this.canvasWidth, this.canvasHeight);
		}
		
		g2d.setPaint(this.foreGroundColor);
    	g2d.setFont(font);
		
    	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY); 
    	
		AttributedString as = new AttributedString(text);
		as.addAttribute(TextAttribute.FONT, font);
		as.addAttribute(TextAttribute.JUSTIFICATION, font);
		AttributedCharacterIterator paragraph = as.getIterator();
		int paragraphStart = paragraph.getBeginIndex();
		int paragraphEnd = paragraph.getEndIndex();
		        
 		float drawPosY = (float)textStartPosY;

    	StringTokenizer st = new StringTokenizer(text);
    	Vector v = new Vector();
    	while(st.hasMoreElements())
    	{
    		String word = (String)st.nextElement();
    		v.addElement(word);
    	}
		
    	String testString = "";
    	String realString = "";
    	java.util.List lines = new ArrayList();
    	int offset = 0;
    	for(int i=0; i < v.size(); i++)
    	{
    		testString = realString + (String)v.get(i) + " ";
    		TextLayout testLay = new TextLayout(testString, font, g2d.getFontRenderContext());
			 
			if(testLay.getBounds().getWidth() > textWidth || i == v.size()-2)
			{
				String remainingString = testString;
				if(v.size() > i + 1)
					remainingString += (String)v.get(i + 1);
				if(v.size() > i + 2)
					remainingString += " " + (String)v.get(i + 2);
				
				TextLayout fullyFilledLay = new TextLayout(remainingString, font, g2d.getFontRenderContext());
				if(fullyFilledLay.getBounds().getWidth() < textWidth)
				{
					realString = testString;
					testString = "";
				}
				else
				{
					String row = "";
					for(int j=offset; j < i; j++)
						row = row + (String)v.get(j) + " ";
						
					lines.add(row);
					realString = "";
					testString = "";
					offset = i;
					realString = (String)v.get(i) + " "; 
				}
			}
			else
			{
				realString = testString;
				testString = "";
			}	
    	}

    	if(!realString.equalsIgnoreCase(""))
	    	lines.add(realString);
		
			
	    Iterator i = lines.iterator();   
		while (i.hasNext()) 
		{   
			String word = (String)i.next();
			if(word != null && word.length() > 0)
			{
			   	TextLayout layout = new TextLayout(word, font, g2d.getFontRenderContext());
			   	
			   	int centerX = this.textWidth / 2;
				int centeredTextStartX = centerX - ((int)layout.getVisibleAdvance() / 2);
				int rightTextStartX = this.textWidth - (int)layout.getVisibleAdvance();
				
			   	// Move y-coordinate by the ascent of the layout.
			   	drawPosY += layout.getAscent();
			        
			   	float drawPosX;
			   	if (layout.isLeftToRight()) 
			   	{
			   		if(this.alignment == ALIGN_CENTER)
			   			drawPosX = centeredTextStartX; 
					if(this.alignment == ALIGN_RIGHT)
						drawPosX = rightTextStartX - textStartPosX; 
			   		else
			   			drawPosX = textStartPosX;
			   	}
			   	else  
			   	{
					drawPosX = textWidth - layout.getAdvance();
			   	}
			            
			   	// Draw the TextLayout at (drawPosX, drawPosY). 
			   	layout.draw(g2d, drawPosX, drawPosY);
			           
			   	// Move y-coordinate in preparation for next layout.
			   	drawPosY += layout.getDescent() + layout.getLeading();
			}
		}

  	}


	public void setCanvasHeight(int canvasHeight)
	{
		this.canvasHeight = canvasHeight;
	}

	public void setCanvasWidth(int canvasWidth)
	{
		this.canvasWidth = canvasWidth;
	}

	public void setTextHeight(int textHeight)
	{
		this.textHeight = textHeight;
	}

	public void setTextStartPosX(int textStartPosX)
	{
		this.textStartPosX = textStartPosX;
	}

	public void setTextStartPosY(int textStartPosY)
	{
		this.textStartPosY = textStartPosY;
	}

	public void setTextWidth(int textWidth)
	{
		this.textWidth = textWidth;
	}

	public void setBackgroundColor(Color backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}

	public void setForeGroundColor(Color foreGroundColor)
	{
		this.foreGroundColor = foreGroundColor;
	}

	public void setFontName(String fontName)
	{
		this.fontName = fontName;
	}

	public void setFontSize(int fontSize)
	{
		this.fontSize = fontSize;
	}

	public void setFontStyle(int fontStyle)
	{
		this.fontStyle = fontStyle;
	}

	public void setBackgroundImageUrl(String backgroundImageUrl)
	{
		this.backgroundImageUrl = backgroundImageUrl;
	}

	public int getAlignment()
	{
		return alignment;
	}

	public void setAlignment(int alignment)
	{
		this.alignment = alignment;
	}

}




