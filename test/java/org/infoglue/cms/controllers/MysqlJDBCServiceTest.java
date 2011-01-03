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
 * $Id: MysqlJDBCServiceTest.java,v 1.3 2006/03/12 10:53:40 mattias Exp $
 */
package org.infoglue.cms.controllers;

import org.infoglue.cms.controllers.kernel.impl.simple.MysqlJDBCService;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.InfoGlueTestCase;

/**
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class MysqlJDBCServiceTest extends InfoGlueTestCase
{
	public void testEnabled()
	{
		setProperties("master", "slave");
		assertTrue("MysqlJDBCService is not enabled", new MysqlJDBCService().isEnabled());
	}

	public void testDisabledMasterBlank()
	{
		assertNotEnabled("", "slave");
	}

	public void testDisabledMasterNull()
	{
		assertNotEnabled(null, "slave");
	}

	public void testDisabledSlaveBlank()
	{
		assertNotEnabled("master", "");
	}

	public void testDisabledSlaveNull()
	{
		assertNotEnabled("master", null);
	}

	public void testBothBlank()
	{
		assertNotEnabled("", "");
	}

	public void testBothNull()
	{
		assertNotEnabled(null, null);
	}

	private void setProperties(String master, String slave)
	{
		setProperty("masterServer", master);
		setProperty("slaveServer", slave);
	}

	private void setProperty(String key, String value)
	{
		if(value == null)
			CmsPropertyHandler.getProperties().remove(key);
		else
			CmsPropertyHandler.setProperty(key, value);
	}

	private void assertNotEnabled(String master, String slave)
	{
		setProperties(master, slave);
		assertFalse("MysqlJDBCService should not be enabled", new MysqlJDBCService().isEnabled());
	}
}
