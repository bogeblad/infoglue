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

package org.infoglue.cms.util;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * This class just formats the log-message the way we like it....
 */

public class CmsSimpleFormatter extends SimpleFormatter
{
	public final String format(LogRecord record)
	{
		StringBuffer sb = new StringBuffer();
		String addition= "";
		if(record.getLevel() == Level.INFO)
			addition = "    ";
		else if(record.getLevel() == Level.WARNING)
			addition = " ";
		else if(record.getLevel() == Level.SEVERE)
			addition = "  ";
			
		sb.append(record.getLevel()).append(addition).append(new Date(record.getMillis()).toString()).append(" ").append(record.getMessage()).append('\n');
		return sb.toString();
	}
	
}
