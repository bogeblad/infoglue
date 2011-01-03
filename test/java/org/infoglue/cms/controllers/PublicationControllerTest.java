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
 * $Id: PublicationControllerTest.java,v 1.3 2006/03/06 16:54:01 mattias Exp $
 */
package org.infoglue.cms.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.infoglue.cms.controllers.kernel.impl.simple.PublicationController;
import org.infoglue.cms.entities.publishing.EditionBrowser;
import org.infoglue.cms.entities.publishing.PublicationDetailVO;
import org.infoglue.cms.entities.publishing.PublicationVO;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.InfoGlueTestCase;

/**
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class PublicationControllerTest extends InfoGlueTestCase
{
	public static final Integer TEST_REPO = new Integer(9999999);
	public static final Integer PAGE_SIZE = new Integer(5);

	ArrayList testEditions = new ArrayList();

	protected void setUp() throws Exception
	{
		super.setUp();
		CmsPropertyHandler.setProperty("edition.pageSize", PAGE_SIZE.toString());
	}

	protected void tearDown() throws Exception
	{
		for (Iterator iter = testEditions.iterator(); iter.hasNext();)
		{
			PublicationVO publicationVO = (PublicationVO)iter.next();
			PublicationController.deleteEntity(PublicationImpl.class, publicationVO.getId());
			assertRemoved(publicationVO.getId());
		}
	}

	public void testGetEditions() throws Exception
	{
		int numEditions = 11;
		for (int i = 0; i < numEditions; i++)
			createEdition("TestEdition #" + i, changeDate(Calendar.DAY_OF_YEAR, (0-i)), i);

		int startIndex = 4;
		EditionBrowser browser = PublicationController.getEditionPage(TEST_REPO, startIndex);

		assertEquals("Wrong total number of editions", numEditions, browser.getTotalEditions());
		assertFalse("No editions returned", browser.getEditions().isEmpty());
		assertEquals("Wrong number of editions on page", PAGE_SIZE.intValue(), browser.getEditions().size());
		assertEquals("Wrong start index", startIndex, browser.getStartIndex());
		assertEquals("Wrong total pages", 3, browser.getTotalPages());
		assertEquals("Wrong current page", 1, browser.getCurrentPage());
		assertEquals("Wrong previous page size", 4, browser.getPreviousPageSize());
		assertEquals("Wrong next page size", 2, browser.getNextPageSize());

		int index = 0;
		for (Iterator iter = browser.getEditions().iterator(); iter.hasNext(); index++)
		{
			PublicationVO found = (PublicationVO) iter.next();
			PublicationVO expected = (PublicationVO) testEditions.get(startIndex + index);
			assertEquals("Wrong previous page size", expected.getId(), found.getId());
		}
	}

	private PublicationVO createEdition(String name, Date publicationDate, int detailCount) throws SystemException
	{
		PublicationVO edition = new PublicationVO();
		edition.setRepositoryId(TEST_REPO);
		edition.setName(name);
		edition.setDescription(getName() + " description");
		edition.setPublicationDateTime(publicationDate);
		edition.setPublisher(getName());

		for (int i = 0; i < detailCount; i++)
		{
			PublicationDetailVO detail = new PublicationDetailVO();
			detail.setName("TestPublicationDetail");
			detail.setEntityClass("TEST-CLASS");
			detail.setEntityId(new Integer(-99));
			detail.setCreationDateTime(new Date());
			detail.setCreator(getName());
			detail.setTypeId(PublicationDetailVO.PUBLISH);
			edition.getPublicationDetails().add(detail);
		}

		PublicationVO savedEdition = PublicationController.create(edition);
		testEditions.add(savedEdition);
		return savedEdition;
	}

	// Make sure it was removed from the DB
	private void assertRemoved(Integer id) throws Exception
	{
		try
		{
			PublicationController.getVOWithId(PublicationImpl.class, id);
			fail("The Publication was not deleted");
		}
		catch(Exception e)
		{
			// Exception is expected
		}
	}
}
