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

package org.infoglue.cms.controllers.kernel.impl.simple;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.kernel.IBaseEntity;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;


/**
 * @author Stefan Sik, ss@frovi.com
 *
 * ValidationController
 * Provides validation functionality for valueobjects
 * 
 * 
 */
public class ValidationController extends BaseController 
{

    private final static Logger logger = Logger.getLogger(ValidationController.class.getName());

	private static final String NOTUNIQUE_FIELD_ERROR_CODE = "302";

	protected static void validateUniqueness(String value, String fieldName, Class objectClass, Integer excludeId, Object excludedObject) throws ConstraintException, SystemException 
	{
		Pattern p = Pattern.compile("[.\\s]+");
		String[] arrString = p.split(fieldName);
		String cleanField = arrString[arrString.length-1];
		
		if(fieldValueExists(objectClass, cleanField, value, excludeId, excludedObject)) 
		{
		  throw createConstraintException(fieldName, NOTUNIQUE_FIELD_ERROR_CODE);
		}
	}

	private static final ConstraintException createConstraintException(String fieldName, String errorCode) 
	{
		return new ConstraintException(fieldName, errorCode);
	}


	public static boolean fieldValueExists(Class objectClass, String fieldName, String checkValue, Integer excludeId, Object excludeObject) throws SystemException
	{
		boolean valueExist = false;
		Database db = CastorDatabaseService.getDatabase();
        OQLQuery oql;

		try 
		{
			beginTransaction(db);

    		oql = db.getOQLQuery( "SELECT u FROM " +objectClass.getName() + " u WHERE u." + fieldName + " = $1");
			oql.bind(checkValue);

			QueryResults results = oql.execute();
			logger.info("Fetching entity in read/write mode");

			if (excludeId == null && excludeObject == null)
				valueExist = results.hasMore();
			else
			{
				// Check for excluded object
				while (results.hasMore()) 
	            {
	            	IBaseEntity o = (IBaseEntity) results.next();
	            	logger.info("Validating...." + o.getIdAsObject() + ":" + excludeObject + ":" + o.getIdAsObject().equals(excludeObject));
	            	if(excludeObject != null)
	            	{
						if (!o.getIdAsObject().equals(excludeObject))
							valueExist = true;
	            	}
					else
					{
		            	if (o.getId().compareTo(excludeId) != 0)
		            		valueExist = true;
					}
	            }
			}
			
			commitTransaction(db);
		} 
		catch (Exception e) 
		{
			logger.error("An error occurred so we should not complete the transaction:" + e, e);
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}				
		return valueExist;		
	}

	/**
	 * This is a method that never should be called.
	 */

	public BaseEntityVO getNewVO()
	{
		return null;
	}

}
