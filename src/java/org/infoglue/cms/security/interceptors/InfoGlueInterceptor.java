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

package org.infoglue.cms.security.interceptors;

import java.util.Map;

import org.exolab.castor.jdo.Database;
import org.infoglue.cms.entities.management.InterceptionPointVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;

/**
 * @author Mattias Bogeblad
 *
 * This interface is for all Interceptors in play
 */

public interface InfoGlueInterceptor
{
	/**
	 * This method will be called when a interceptionPoint is reached.
	 * 
	 * @param interceptionPoint
	 * @param extradata
	 * @throws ConstraintException
	 * @throws SystemException
	 */
	
	public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata) throws ConstraintException, SystemException, Exception;

	public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata, boolean allowCreatorAccess) throws ConstraintException, SystemException, Exception;

	/**
	 * This method will be called when a interceptionPoint is reached and handle it withing a transaction.
	 * 
	 * @param interceptionPoint
	 * @param extradata
	 * @throws ConstraintException
	 * @throws SystemException
	 */
	
	public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata, Database db) throws ConstraintException, SystemException, Exception;

	public void intercept(InfoGluePrincipal infoGluePrincipal, InterceptionPointVO interceptionPointVO, Map extradata, boolean allowCreatorAccess, Database db) throws ConstraintException, SystemException, Exception;

}
