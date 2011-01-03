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

package org.infoglue.deliver.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is meant to be used for convering between different datastructures.
 * 
 * @author Mattias Bogeblad
 */

public class ObjectConverter
{
	/**
	 * This method returns the HttpServletRequest as a Map
	 */
	
	public Map requestToMap(HttpServletRequest request) 
	{	
		Map map = new HashMap();
		
		if(request != null)
		{
			for (Enumeration e = request.getParameterNames(); e.hasMoreElements() ;) 
			{		        
				String name  = (String)e.nextElement();
				String value = (String)request.getParameter(name);
				map.put(name, value);
			}        
		}
		        
		return map;	
		
	}
}