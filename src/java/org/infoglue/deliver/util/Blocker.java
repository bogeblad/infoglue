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

/**
 * @author mattias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Blocker
{
    private static boolean isBlocking = false;
    private static long blockingStartTime = 0;
    
    private Blocker(){}
   
    static boolean getIsBlocking()
    {
        return isBlocking;
    }
    
    synchronized static void setBlocking(boolean isBlocking)
    {
    	Blocker.isBlocking = isBlocking;
    	Blocker.blockingStartTime = System.currentTimeMillis();
    }
    
    static long getBlockRequestTime()
    {
        return System.currentTimeMillis() - blockingStartTime;
    }
    
}
