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

/*
 * Created on 2003-apr-06
 *
 */
package org.infoglue.cms.controllers.kernel.impl.simple;

import java.util.Date;
import java.util.Vector;

/**
 * This class holds the list of available updates. To expire this
 * list and force a refresh from the server, set availableUpdates to null 
 * setAvalilableUpdates(null);
 * 
 * To allways get a list from the server at first call, initialize availableUpdates
 * with null, otherwise to start with an empty list initialize with new Vector();
 * 
 * @author Stefan Sik
 * 
 *
 */
public class UpdateListHandler {
	
	// Start with empty vector so that we must do a refresh to get
	// the updates from the updateserver
	private static Vector availableUpdates=new Vector();
	private static Vector installedUpdates=null;
	private static Date latestRefresh = null;

	/**
	 * @return
	 */
	public static Vector getAvailableUpdates() {
		return availableUpdates;
	}

	/**
	 * @return
	 */
	public static Vector getInstalledUpdates() {
		return installedUpdates;
	}

	/**
	 * @param vector
	 */
	public static void setAvailableUpdates(Vector vector) {

		if (vector != null)
		{
			setLatestRefresh(new Date());
		}
		
		availableUpdates = vector;
	}

	/**
	 * @param vector
	 */
	public static void setInstalledUpdates(Vector vector) {
		installedUpdates = vector;
	}


	/**
	 * @return
	 */
	public static Date getLatestRefresh() {
		return latestRefresh;
	}

	/**
	 * @param date
	 */
	public static void setLatestRefresh(Date date) {
		latestRefresh = date;
	}

}
