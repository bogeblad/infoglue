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
 *
 * $Id: InfoGlueTestCase.java,v 1.9 2006/09/06 14:37:39 mattias Exp $
 */
package org.infoglue.cms.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;

/**
 * The base class of all InfoGlue tests cases will setup the things that need to be setup
 * in order to fake out certain services and facilities that may not be running in the
 * environment that they expect.
 *
 * Things like this FakeServletContext can generally be gotten rid of with a few key
 * refactorings of the system to not rely on being run as a web app, etc.
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public abstract class InfoGlueTestCase extends TestCase
{
	private static final InfoGluePrincipal adminPrincipal = new InfoGluePrincipal("test-admin", "first", "last", "email", createRole("administrators"), new ArrayList(), true, null);
	private static final InfoGluePrincipal cmsUserPrincipal = new InfoGluePrincipal("test-user", "first", "last", "email", createRole("cmsUser"), new ArrayList(), false, null);
	private static final InfoGluePrincipal anonPrincipal = new InfoGluePrincipal(CmsPropertyHandler.getAnonymousUser(), "first", "last", "email", createRole("anonymous"), new ArrayList(), false, null);

	private static boolean initialized = false;

	protected void setUp() throws Exception
	{
		initializeInfoGlue();
	}

	/**
	 * For testing we dont care about the nofitications that take place,
	 * especially notifications to remote servers and such that will fail under
	 * a normal slimmed down test environment.
	 * We can test the Notifiers individually, so lets make our lives easier now.
	 */
	public static void initializeInfoGlue() throws SystemException
	{
		if (initialized)
			return;

		FakeServletContext.getContext().init();
		CastorDatabaseService.getJDO().setCallbackInterceptor(null);
		initialized = true;
	}

	/**
	 * Changes from todays date the supplied unit by the provided amount
	 * @param unit A value from Calendar
	 * @param amount the amount to change the unit, can be positive or negative
	 * @return The newly modified date
	 */
	protected Date changeDate(int unit, int amount)
	{
		return changeDate(new Date(), unit, amount);
	}

	/**
	 * Changes from the given date the supplied unit by the provided amount
	 * @param current The date to change
	 * @param unit A value from Calendar
	 * @param amount the amount to change the unit, can be positive or negative
	 * @return THe newly modified date
	 */
	protected Date changeDate(Date current, int unit, int amount)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(current);
		calendar.add(unit, amount);
		return calendar.getTime();
	}


	/**
	 * Creates a role with the given name to use for testing
	 * @param name the name of the role
	 * @return a list containing only the desired role.
	 */
	protected static List createRole(String name)
	{
		return Collections.singletonList(new InfoGlueRole(name, "test description", null));
	}

	/**
	 * Returns the administrator principal
	 * @return an InfoGluePrincipal representing an administrator
	 */
	public static InfoGluePrincipal getAdminPrincipal()
	{
		return adminPrincipal;
	}

	/**
	 * Returns the cmsUser principal
	 * @return an InfoGluePrincipal representing a cmsUser
	 */
	public static InfoGluePrincipal getCmsUserPrincipal()
	{
		return cmsUserPrincipal;
	}

	/**
	 * Returns the anonymous principal
	 * @return an InfoGluePrincipal representing an anonymous user
	 */
	public static InfoGluePrincipal getAnonPrincipal()
	{
		return anonPrincipal;
	}

	/**
	 * Returns a Repository Id to use for testing. It will get the first Repository
	 * and return it's id, or 1 if there is an error finding any repositories.
	 * @return An Integer Repository Id
	 */
	public static Integer getRepoId()
	{
		try
		{
			RepositoryVO repo = RepositoryController.getController().getFirstRepositoryVO();
			return repo.getId();
		}
		catch (SystemException e)
		{
			e.printStackTrace();
			return new Integer(1);
		}
	}

	/**
	 * Returns a Language Id. It will use the master language for the first repository,
	 * or 1 if there is some kind of error.
	 * @return An Integer Language Id, or 1 is there is some sort of error
	 */
	public static Integer getLanguageId()
	{
		return getLanguageId(getRepoId());
	}

	/**
	 * Returns a Language Id for the given Repository. It will use the master language for
	 * the given repository, or 1 if there is some kind of error.
	 * @param repoId The id of the Repository to get the Master Language.
	 * @return An Integer Language Id, or 1 is there is some sort of error
	 */
	public static Integer getLanguageId(Integer repoId)
	{
		try
		{
			return LanguageController.getController().getMasterLanguage(repoId).getId();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new Integer(1);
		}
	}

	/**
	 * Returns a SiteNode id. It will use the first repository, or return 1
	 * if there is some kind of error.
	 * @return An Integer SiteNode Id, or 1 is there is some sort of error
	 */
	public static Integer getSiteNodeId()
	{
		return getSiteNodeId(getRepoId());
	}

	/**
	 * Returns a SiteNode id for the given Repository. It will use the root site node for
	 * the given repository, or 1 if there is some kind of error.
	 * @param repoId The id of the Repository to get the Master Language.
	 * @return An Integer SiteNode Id, or 1 is there is some sort of error
	 */
	public static Integer getSiteNodeId(Integer repoId)
	{
		try
		{
			return SiteNodeController.getController().getRootSiteNodeVO(repoId).getId();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new Integer(1);
		}
	}
}
