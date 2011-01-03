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

/**
 * CmsSecurityManagerImpl.java
 * Created on 2002-sep-06 
 * @author Stefan Sik, ss@frovi.com 
 *
 */
public class CmsSecurityManagerImpl implements CmsSecurityManager
{
	/**
	 * @see org.infoglue.cms.util.CmsSecurityManager#getRemoteHost()
	 */
	public String getRemoteHost()
	{
		return "dummy.frovi.com";
	}

	/**
	 * @see org.infoglue.cms.util.CmsSecurityManager#getUserName()
	 */
	public String getUserName()
	{
		return "cmsUser";
	}


	/**
	 * This method implements the autorization of users trying to login.
	 */
	
	public boolean authorizeSystemUser(String userName, String password)
	{
		boolean isAuthorized = false;
		
			
		
		return isAuthorized;	
	}
}
