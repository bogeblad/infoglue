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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class only contains some simple methods for date handling.
 * 
 * @author Mattias Bogeblad
 */

public class DateHelper
{
    /**
     * This method returns a Date initialized without milliseconds. 
     * This is very important when saving items i oracle as dates.
     * 
     * @return
     */
    
    public static Date getSecondPreciseDate()
    {
        return new Date();
        /*
        Calendar now = Calendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);
		now.set(Calendar.SECOND, 0);

		return now.getTime();
		*/
    }
    
    /**
     * This method returns a Date initialized without milliseconds. 
     * This is very important when saving items i oracle as dates.
     * 
     * @return
     */
    
    public static String getFormattedCurrentDateTime(String pattern)
    {
    	Date date = new Date();
            
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String dateString = formatter.format(date);

        return dateString;
    }
}
