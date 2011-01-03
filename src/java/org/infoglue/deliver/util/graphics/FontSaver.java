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

// FontSaver.java
// conserves RAM and time by reusing Font and Font peer objects.
// Instead of saying:
//    Font f = new Font("Dialog", 12, Font.PLAIN);
// say:
//    Font f = FontSaver.create("Dialog", 12, Font.PLAIN);
// Version 1.2 1998 November 10 - new address and phone.
// Version 1.1 1998 January 15

/** copyright (c) 1998-2002 Roedy Green, Canadian Mind Products
  * #327 - 964 Heywood Avenue
  * Victoria, BC Canada V8V 2Y5
  * mailto:roedy@mindprod.com
  * http://mindprod.com
  */

// May be distributed freely and used for any purpose but military.


import java.awt.Font;
import java.util.Hashtable;

public class FontSaver
{
	
	private static Hashtable h;
   
   	/**
	 * Works just like the Font Constructor:
 	 * Creates a new font with the specified name, style and point size.
 	 * @param name the font name
 	 * @param style the constant style used
 	 * @param size the point size of the font 
 	 */
   
	public static Font create (String name, int style, int size)
   	{
   		if (h == null) 
   			h = new Hashtable(101,.75f);
      
		FontKey fontKey = new FontKey(name, style, size);
      	Font prevFont = (Font)h.get(fontKey);
      	
      	if (prevFont != null) 
      		return prevFont;
      
		Font newFont = new Font(name, style, size);
      
		h.put(fontKey, newFont);
      	
      	return newFont;
	} 

}


/*
 * FontKey is just the name, style and size fields of a font,
 * enough to identify it, a key to find it in a Hashtable.
 */

class FontKey
{

   /**
     * constructor
     * Creates a new FontKey id object with the specified name, style and point size.
     * @param name the font name
     * @param style the constant style used
     * @param size the point size of the font
     */

	public FontKey (String name, int style, int size)
    {
      	this.name = name;
      	this.style = style;
      	this.size = size;
    }

   	public boolean equals(Object obj)
    {
      	if ( obj instanceof FontKey )
        {
        	FontKey fontKey = (FontKey)obj;
         	return(size == fontKey.size) && (style == fontKey.style) && name.equals(fontKey.name);
       	}
      	return false;
    }

   /**
    * Returns a hashcode for this font.
    */
    
   	public int hashCode()
    {
      	return name.hashCode() ^ style ^ size;
    }

   /**
    * The logical name of this font.
    */
   	
   	protected String name;

   /**
    * The style of the font. This is the sum of the
    * constants PLAIN, BOLD, or ITALIC.
    */
   	
   	protected int style;

   /**
    * The point size of this font.
    */
   
   	protected int size;

}