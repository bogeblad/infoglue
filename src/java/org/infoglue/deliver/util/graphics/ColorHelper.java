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
import java.util.StringTokenizer;

import org.infoglue.deliver.util.MathHelper;

/**
 * This class is an attempt to give template-coders access to the Color-class in some ways.
 * Basically used to enable good control in the templates.
 */

public class ColorHelper
{
	public ColorHelper()
	{
	}
	
	/**
	 * Used to get a color-object
	 */
	
	public Color getColor(int r, int g, int b)
	{
		return new Color(r, g, b);
	}


	/**
	 * Used to create a new color-object by hex-values.
	 */
	
	public Color getHexColor(String hexadecimalValue)
	{
		return new Color(new MathHelper().hexToDecimal(hexadecimalValue));
	}

	/**
	 * Creates a Color object from a comma separated string like "12:34:6:154"
	 * values 0-255 and in order R,G,B,A
	 * @param colonSeparatedRGBA a string
	 * @return a Color object
	 */
	public static Color getColor( String colonSeparatedRGBA )
	{
	    return ColorHelper.getColor( colonSeparatedRGBA, ":" );
	}
	
	/**
	 * Creates a Color object from a separated string like "12-34-6-154" values
	 * 0-255 and in order R,G,B,A
	 * @param separatedRGBA a string
	 * @param delimiter a delimiter
	 * @return a Color object
	 * @author Per Jonsson - per.jonsson@it-huset.se
	 */
	
	public static Color getColor( String separatedRGBA, String delimiter )
	{
	    StringTokenizer st = new StringTokenizer( separatedRGBA, delimiter, false );
	    int r = 0, g = 0, b = 0, a = 0;
	    if ( st.hasMoreTokens() )
	    {
	        r = Integer.parseInt( st.nextToken().trim() );
	    }
	    if ( st.hasMoreTokens() )
	    {
	        g = Integer.parseInt( st.nextToken().trim() );
	    }
	    if ( st.hasMoreTokens() )
	    {
	        b = Integer.parseInt( st.nextToken().trim() );
	    }
	    if ( st.hasMoreTokens() )
	    {
	        a = Integer.parseInt( st.nextToken().trim() );
	    }
	    return new Color( r, g, b, a );
	}

}
