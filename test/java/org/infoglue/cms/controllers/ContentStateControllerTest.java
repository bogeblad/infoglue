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
 * $Id: ContentStateControllerTest.java,v 1.6 2008/06/04 07:19:51 mattias Exp $
 */
package org.infoglue.cms.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentCategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentStateController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.EventController;
import org.infoglue.cms.controllers.kernel.impl.simple.InfoGluePrincipalControllerProxy;
import org.infoglue.cms.entities.content.ContentCategoryVO;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersion;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.entities.workflow.EventVO;
import org.infoglue.cms.util.InfoGlueTestCase;

/**
 * This test exercises the publishing/unpublishing of content.
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class ContentStateControllerTest extends InfoGlueTestCase
{
	private static final Integer REPO = getRepoId();
	private static final Integer LANGUAGE = getLanguageId(REPO);

	private ContentVO testContent;
	private ContentVersionVO testContentVersion;
	private ContentVersionVO testEventContentVersion;
	private ContentController contentStore = ContentController.getContentController();
	private ContentVersionController contentVersionStore = ContentVersionController.getContentVersionController();

	private CategoryVO testCategory;
	private ContentCategoryVO testContentCategory;
	private CategoryController categoryStore = CategoryController.getController();
	private ContentCategoryController contentCategoryStore = ContentCategoryController.getController();

	private List allContent = new ArrayList();

	protected void setUp() throws Exception
	{
		super.setUp();

		testContent = new ContentVO();
		testContent.setName(getName());
		testContent.setCreatorName("junit");
		testContent.setIsBranch(Boolean.FALSE);
		testContent.setPublishDateTime(changeDate(Calendar.YEAR, -1));
		testContent.setExpireDateTime(changeDate(Calendar.YEAR, 1));
		testContent = contentStore.create(null, null, REPO, testContent);
		allContent.add(testContent);

		testContentVersion = new ContentVersionVO();
		testContentVersion.setVersionModifier("junit");
		testContentVersion.setVersionValue(getName());
		testContentVersion = contentVersionStore.create(testContent.getId(), LANGUAGE, testContentVersion, null);

		testCategory = new CategoryVO();
		testCategory.setName(getName());
		testCategory.setDescription(getName() + " description");
		testCategory = categoryStore.save(testCategory);

		testContentCategory = new ContentCategoryVO();
		testContentCategory.setAttributeName(getName());
		testContentCategory.setContentVersionId(testContentVersion.getId());
		testContentCategory.setCategory(testCategory);
		testContentCategory = contentCategoryStore.save(testContentCategory, InfoGluePrincipalControllerProxy.getController().getTestPrincipal());
	}


	protected void tearDown() throws Exception
	{
		for (Iterator i = allContent.iterator(); i.hasNext();)
			contentStore.delete((ContentVO)i.next(), getCmsUserPrincipal());
		assertRemoved();

		if(testCategory != null)
			categoryStore.delete(testCategory.getId());

		removeAnyEvents();
	}

	public void testCategoryDuplicationOnPublishing() throws Exception
	{
		performStateChangeTest(ContentVersionVO.PUBLISH_STATE);
	}

	public void testCategoryDuplicationOnWorking() throws Exception
	{
		performStateChangeTest(ContentVersionVO.WORKING_STATE);
	}

	public void performStateChangeTest(Integer stateId) throws Exception
	{
		// First make sure a certain count exists
		List found = contentCategoryStore.findByContentVersion(testContentVersion.getId());
		assertEquals("Wrong number of ContentCategories returned", 1, found.size());
		assertTrue("testContentCategory data not returned", found.contains(testContentCategory));

		List events = new ArrayList();
		ContentVersion newVersion = ContentStateController.changeState(testContentVersion.getId(), stateId, getName(), false, getAdminPrincipal(), null, events);
		testEventContentVersion = newVersion.getValueObject();

		List newFound = contentCategoryStore.findByContentVersion(newVersion.getId());
		assertEquals("Wrong number of new ContentCategories returned", found.size(), newFound.size());

		ContentCategoryVO newContentCategory = (ContentCategoryVO)newFound.get(0);
		assertEquals("Wrong contentVersionId", newVersion.getContentVersionId(), newContentCategory.getContentVersionId());
		assertEquals("Wrong category", testContentCategory.getCategory(), newContentCategory.getCategory());
		assertEquals("Wrong attribute", testContentCategory.getAttributeName(), newContentCategory.getAttributeName());
	}

	//---------------------------------------------------------------------------------------
	/**
	 * As a result of some of these processes, events occasionally get created,
	 * lets be good boys and clean them up.
	 */
	private void removeAnyEvents() throws Exception
	{
		List events = EventController.getEventVOListForEntity(ContentVersion.class.getName(), testEventContentVersion.getId());
		for (Iterator iter = events.iterator(); iter.hasNext();)
			EventController.delete((EventVO) iter.next());
	}

	// Make sure it was removed from the DB
	private void assertRemoved() throws Exception
	{
		try
		{
			contentStore.getContentVOWithId(testContent.getId());
			fail("The Content was not deleted");
		}
		catch(Exception e)
		{ /* expected */ }

		try
		{
			contentVersionStore.getContentVersionVOWithId(testContentVersion.getId());
			fail("The ContentVersion was not deleted");
		}
		catch(Exception e)
		{ /* expected */ }

		assertTrue("The ContentCategories were not deleted",
				   contentCategoryStore.findByContentVersion(testContentVersion.getId()).isEmpty());
	}
}
