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

import java.awt.Font;

/**
 * This class is an attempt to give template-coders access to the Font-class in some ways.
 * Basically used to enable good control in the templates.
 */

public class FontHelper
{
	public FontHelper()
	{
	}
	
	/**
	 * Returns the plain-style
	 */
	
	public int getPlainStyle()
	{
		return Font.PLAIN;
	}

	/**
	 * Returns the bold-style
	 */
	
	public int getBoldStyle()
	{
		return Font.BOLD;
	}

	/**
	 * Returns the italic style
	 */

	public int getItalicStyle()
	{
		return Font.ITALIC;
	}

}
