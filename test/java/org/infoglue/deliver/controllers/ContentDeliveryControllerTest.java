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
 * $Id: ContentDeliveryControllerTest.java,v 1.6 2008/06/04 07:19:51 mattias Exp $
 */
package org.infoglue.deliver.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.exolab.castor.jdo.Database;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentCategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.InfoGluePrincipalControllerProxy;
import org.infoglue.cms.entities.content.ContentCategoryVO;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.InfoGlueTestCase;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.controllers.kernel.impl.simple.ContentDeliveryController;

/**
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class ContentDeliveryControllerTest extends InfoGlueTestCase
{
	// use 1 until we determine a smarter way to get a repo/language id.
	private static final Integer REPO = getRepoId();
	private static final Integer LANGUAGE = getLanguageId(REPO);
	private static final Integer SITENODE = getSiteNodeId(REPO);
	private static final String ATTRIBUTE = "ContentDeliveryControllerTest.attributeName";

	private static final InfoGluePrincipal ADMIN = getAdminPrincipal();
	private static final InfoGluePrincipal ANON = getAnonPrincipal();

	private ContentDeliveryController testController;

	private ContentVO testContent;
	private ContentVersionVO testContentVersion;
	private ContentController contentStore = ContentController.getContentController();
	private ContentVersionController contentVersionStore = ContentVersionController.getContentVersionController();

	private CategoryVO testCategory;
	private CategoryController categoryStore = CategoryController.getController();
	private ContentCategoryController contentCategoryStore = ContentCategoryController.getController();

	private List allContent = new ArrayList();
	private List allContentCategories = new ArrayList();
	private List allCategories = new ArrayList();

	private Database testDatabase;

	protected void setUp() throws Exception
	{
		super.setUp();

		testContent = new ContentVO();
		testContent.setName(getName());
		testContent.setCreatorName("frank");
		testContent.setIsBranch(Boolean.FALSE);
		testContent.setPublishDateTime(changeDate(Calendar.YEAR, -1));
		testContent.setExpireDateTime(changeDate(Calendar.YEAR, 1));
		testContent = contentStore.create(null, null, REPO, testContent);
		allContent.add(testContent);

		testContentVersion = new ContentVersionVO();
		testContentVersion.setVersionModifier("frank");
		testContentVersion.setVersionValue(getName());
		testContentVersion = contentVersionStore.create(testContent.getId(), LANGUAGE, testContentVersion, null);

		// This is done here because of the static initializer tried to load properties
		// and they have not been initialized yet by InfoGlueTestCase.setUp
		testController = ContentDeliveryController.getContentDeliveryController();

		testDatabase = CastorDatabaseService.getDatabase();
		testDatabase.begin();
	}

	protected void tearDown() throws Exception
	{
		testDatabase.commit();
		testDatabase.close();

		for (Iterator i = allContent.iterator(); i.hasNext();)
			contentStore.delete((ContentVO)i.next(), getCmsUserPrincipal());
		assertRemoved();

		for (Iterator i = allCategories.iterator(); i.hasNext();)
			categoryStore.delete(((CategoryVO)i.next()).getId());
	}

	public void testFindByCategory() throws Exception
	{
		testCategory = createContentCategory(testContentVersion.getId(), ATTRIBUTE);

		List found = testController.findContentVersionVOsForCategory(testDatabase, testCategory.getId(), ATTRIBUTE, ANON, SITENODE, LANGUAGE, true, DeliveryContext.getDeliveryContext());
		assertEquals("Wrong number of Content Versions found", 1, found.size());

		ContentVersionVO foundVersion = (ContentVersionVO)found.get(0);
		assertEquals("Wrong Content Version id", testContentVersion.getId(), foundVersion.getId());
	}

	public void testFindBadByPublishDate() throws Exception
	{
		testCategory = createContentCategory(testContentVersion.getId(), ATTRIBUTE);

		testContent.setPublishDateTime(changeDate(Calendar.YEAR, 1));
		contentStore.update(testContent);

		List found = testController.findContentVersionVOsForCategory(testDatabase, testCategory.getId(), ATTRIBUTE, ANON, SITENODE, LANGUAGE, true, DeliveryContext.getDeliveryContext());
		assertEquals("Wrong number of Content Versions found", 0, found.size());
	}

	public void testFindBadByExpireDate() throws Exception
	{
		testCategory = createContentCategory(testContentVersion.getId(), ATTRIBUTE);

		testContent.setExpireDateTime(changeDate(Calendar.YEAR, -1));
		contentStore.update(testContent);

		List found = testController.findContentVersionVOsForCategory(testDatabase, testCategory.getId(), ATTRIBUTE, ANON, SITENODE, LANGUAGE, true, DeliveryContext.getDeliveryContext());
		assertEquals("Wrong number of Content Versions found", 0, found.size());
	}

	public void testFindWithAdminPermissions() throws Exception
	{
		testCategory = createContentCategory(testContentVersion.getId(), ATTRIBUTE);

		testContent.setIsProtected(ContentVO.YES);
		contentStore.update(testContent);

		List found = testController.findContentVersionVOsForCategory(testDatabase, testCategory.getId(), ATTRIBUTE, ADMIN, SITENODE, LANGUAGE, true, DeliveryContext.getDeliveryContext());
		assertEquals("Wrong number of Content Versions found", 1, found.size());
	}

	public void testFindWithAnonPermissions() throws Exception
	{
		testCategory = createContentCategory(testContentVersion.getId(), ATTRIBUTE);

		testContent.setIsProtected(ContentVO.YES);
		contentStore.update(testContent);

		List found = testController.findContentVersionVOsForCategory(testDatabase, testCategory.getId(), ATTRIBUTE, ANON, SITENODE, LANGUAGE, true, DeliveryContext.getDeliveryContext());
		assertEquals("Wrong number of Content Versions found", 0, found.size());
	}

	public void testFindMostRecent() throws Exception
	{
		testCategory = createContentCategory(testContentVersion.getId(), ATTRIBUTE);

		ContentVersionVO another = addMoreContent();
		CategoryVO anotherCategory = createContentCategory(another.getId(), getName());

		List found = testController.findContentVersionVOsForCategory(testDatabase, anotherCategory.getId(), getName(), ANON, SITENODE, LANGUAGE, true, DeliveryContext.getDeliveryContext());
		assertEquals("Wrong number of Content Versions found", 1, found.size());
		assertEquals("Wrong Content Versions found", another.getId(), ((ContentVersionVO)found.get(0)).getId());
	}

	//-------------------------------------------------------------------------
	// Helpers
	//-------------------------------------------------------------------------
	private CategoryVO createContentCategory(Integer contentVersionId, String attributeName) throws SystemException
	{
		CategoryVO c = new CategoryVO();
		c.setName(getName());
		c.setDescription(getName() + " description");
		c = categoryStore.save(c);
		allCategories.add(c);
		createContentCategory(contentVersionId, attributeName, c);
		return c;
	}

	private ContentCategoryVO createContentCategory(Integer contentVersionId, String attributeName, CategoryVO category) throws SystemException
	{
		ContentCategoryVO cc = new ContentCategoryVO();
		cc.setAttributeName(attributeName);
		cc.setContentVersionId(contentVersionId);
		cc.setCategory(category);
		cc = contentCategoryStore.save(cc, InfoGluePrincipalControllerProxy.getController().getTestPrincipal());
		allContentCategories.add(cc);
		return cc;
	}

	private ContentVersionVO addMoreContent() throws Exception
	{
		ContentVO content = new ContentVO();
		content.setName(getName() + System.currentTimeMillis());
		content.setCreatorName("frank");
		content.setIsBranch(Boolean.FALSE);
		content.setExpireDateTime(changeDate(Calendar.YEAR, 1));
		content = contentStore.create(null, null, REPO, content);
		allContent.add(content);

		ContentVersionVO contentVersion = new ContentVersionVO();
		contentVersion.setVersionModifier("frank");
		contentVersion.setVersionValue(getName());
		contentVersion = contentVersionStore.create(content.getId(), LANGUAGE, contentVersion, null);
		return contentVersion;
	}

	// Make sure it was removed from the DB
	private void assertRemoved() throws Exception
	{
		try
		{
			contentStore.getContentVOWithId(testContent.getId());
			fail("The ContentVersion was not deleted");
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
