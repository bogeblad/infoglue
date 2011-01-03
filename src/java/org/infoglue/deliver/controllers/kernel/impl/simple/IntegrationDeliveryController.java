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

package org.infoglue.deliver.controllers.kernel.impl.simple;

import javax.servlet.http.HttpServletRequest;

import org.infoglue.cms.exception.SystemException;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.util.HttpUtilities;


public class IntegrationDeliveryController
{
	
	/**
	 * Private constructor to enforce factory-use
	 */
	
	private IntegrationDeliveryController(Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
	{
	}
	
	
	/**
	 * Factory method
	 */
	
	public static IntegrationDeliveryController getIntegrationDeliveryController(Integer siteNodeId, Integer languageId, Integer contentId) throws SystemException, Exception
	{
		return new IntegrationDeliveryController(siteNodeId, languageId, contentId);
	}
	
	/**
	 * Factory method
	 */
	
	public static IntegrationDeliveryController getIntegrationDeliveryController(DeliveryContext deliveryContext) throws SystemException, Exception
	{
		return new IntegrationDeliveryController(deliveryContext.getSiteNodeId(), deliveryContext.getLanguageId(), deliveryContext.getContentId());
	}
	
	/**
	 * This method gets an remote URL's content.
	 */
	
	public String getUrlContent(String url, HttpServletRequest request, boolean includeRequest) throws SystemException, Exception
	{
		String response = "";
		
		response = HttpUtilities.getUrlContent(url, request, includeRequest);		
		
		return response;	
	}

	/**
	 * This method gets an remote URL's content.
	 */
	
	public String getUrlContent(String url, HttpServletRequest request, boolean includeRequest, String encoding) throws SystemException, Exception
	{
		String response = "";
		
		response = HttpUtilities.getUrlContent(url, request, includeRequest, encoding);		
		
		return response;	
	}


	/**
	 * @param classname
	 * @param request
	 * @return
	 */
	public Object getObjectWithName(String className, HttpServletRequest request) throws InstantiationException, IllegalAccessException, ClassNotFoundException 
	{
		Class theClass = null;
		
		try 
		{
			theClass = Thread.currentThread().getContextClassLoader().loadClass( className );
		}
		catch (ClassNotFoundException e) 
		{
			theClass = getClass().getClassLoader().loadClass( className );
		}
		
		return theClass.newInstance();
	}


}