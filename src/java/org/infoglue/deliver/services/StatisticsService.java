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

package org.infoglue.deliver.services;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.webloggers.CommonLogger;
import org.infoglue.deliver.util.webloggers.Logger;
import org.infoglue.deliver.util.webloggers.W3CExtendedLogger;


/**
 * This class is thought of as an service which logs statistics about a request so that log analyzers later on can
 * extract information from it. The thought is to have all delivery-engines report to a central service
 * about their requests so the logfiles get merged at once. The service should be very performance centered and
 * only write to the logfile when the system is low on use.
 *
 * Frank (03/04/2005): Only logs statistics if statistics.enabled=true in the deliver configuration file.
 */
public class StatisticsService
{
	private static StatisticsService statisticsService = null;
	private Logger logger;

	/**
	 * A private constructor
	 * Could later on be specialized to use other loggers as well.
	 */

	private StatisticsService()
	{
		this.logger = new CommonLogger();

		String statisticsLogger = CmsPropertyHandler.getStatisticsLogger();
		if(statisticsLogger != null && statisticsLogger.equalsIgnoreCase("W3CExtendedLogger"))
			this.logger = new W3CExtendedLogger();
	}

	/**
	 * A factory singleton implementation
	 */

	public static StatisticsService getStatisticsService()
	{
		if(statisticsService == null)
			statisticsService = new StatisticsService();

		return statisticsService;
	}

	/**
	 * This method registers a request to the distributed service.
	 * For now it only logs the request - bad boy!!!!
	 *
	 * It only logs the request if statistics were enabled in the configuration file.
	 */
	public void registerRequest(HttpServletRequest request, HttpServletResponse response, String pagePath, long elapsedTime)
	{
		String enabled = CmsPropertyHandler.getStatisticsEnabled();
		if(Boolean.valueOf(enabled).booleanValue())
			this.logger.logRequest(request, response, pagePath, elapsedTime);
	}
}