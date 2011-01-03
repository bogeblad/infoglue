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

import java.util.Map;

/**
 * This is the interface for all controllers that wants to listen to new 
 * notificationMessages.
 */

public interface NotificationListener
{
	/** 
	 * This method sets the context/arguments thelistener should operate with. 
	 * Could be debuglevels and stuff like that.
	 */
	
	public void setContextParameters(Map map);
	
	/**
	 * This method gets called when a new NotificationMessage is available.
	 */
	
	public void notify(NotificationMessage message);	
}