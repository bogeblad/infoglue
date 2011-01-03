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
 * $Id: ContentCategoryControllerTest.java,v 1.4 2008/06/04 07:19:51 mattias Exp $
 */
package org.infoglue.cms.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentCategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.InfoGluePrincipalControllerProxy;
import org.infoglue.cms.entities.content.ContentCategoryVO;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.util.InfoGlueTestCase;

/**
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class ContentCategoryControllerTest extends InfoGlueTestCase
{
	private static final Integer VERSION_ID = new Integer("1234321");

	private ContentCategoryVO testContentCategory;
	private CategoryVO testCategory;
	private List extraContentCategories = new ArrayList();
	private ContentCategoryController testController = ContentCategoryController.getController();
	private CategoryController testCategoryController = CategoryController.getController();

	private boolean deleted;

	protected void setUp() throws Exception
	{
		super.setUp();
		testCategory = new CategoryVO();
		testCategory.setName(getName());
		testCategory.setDescription(getName() + " description");
		testCategory = testCategoryController.save(testCategory);

		testContentCategory = new ContentCategoryVO();
		testContentCategory.setAttributeName(getName());
		testContentCategory.setContentVersionId(VERSION_ID);
		testContentCategory.setCategory(testCategory);
		testContentCategory = testController.save(testContentCategory, InfoGluePrincipalControllerProxy.getController().getTestPrincipal());
	}

	protected void tearDown() throws Exception
	{
		if (!deleted)
		{
			testController.delete(testContentCategory.getId(), InfoGluePrincipalControllerProxy.getController().getTestPrincipal());
			assertRemoved();
		}

		for (Iterator i = extraContentCategories.iterator(); i.hasNext();)
			testController.delete(((ContentCategoryVO)i.next()).getId(), InfoGluePrincipalControllerProxy.getController().getTestPrincipal());

		// If you delete the category first, then try to delete the ContentCategory
		// it will barf because it cannot pull up the Category relationship
		testCategoryController.delete(testCategory.getId());
	}

	public void testFindById() throws Exception
	{
		ContentCategoryVO found = testController.findById(testContentCategory.getId());
		assertEquals("ContentCategory data found does not match the created data", testContentCategory, found);
		assertEquals("Wrong Category relationship", testCategory, found.getCategory());
	}

	public void testFindByContentVersionAttribute() throws Exception
	{
		ContentCategoryVO sameAttribute = new ContentCategoryVO();
		sameAttribute.setAttributeName(getName());
		sameAttribute.setContentVersionId(VERSION_ID);
		sameAttribute.setCategory(testCategory);
		sameAttribute = testController.save(sameAttribute, InfoGluePrincipalControllerProxy.getController().getTestPrincipal());
		extraContentCategories.add(sameAttribute);

		ContentCategoryVO differentAttribute = new ContentCategoryVO();
		differentAttribute.setAttributeName("randomAttribute");
		differentAttribute.setContentVersionId(VERSION_ID);
		differentAttribute.setCategory(testCategory);
		differentAttribute = testController.save(differentAttribute, InfoGluePrincipalControllerProxy.getController().getTestPrincipal());
		extraContentCategories.add(differentAttribute);

		List found = testController.findByContentVersionAttribute(getName(), VERSION_ID);
		assertEquals("Wrong number of ContentCategories returned", 2, found.size());
		assertTrue("testContentCategory data not returned", found.contains(testContentCategory));
		assertTrue("sameAttribute data not returned", found.contains(sameAttribute));
		assertFalse("differentAttribute data returned", found.contains(differentAttribute));
	}

	public void testFindByContentVersionAttributeBadId() throws Exception
	{
		List found = testController.findByContentVersionAttribute(getName(), new Integer(-9999999));
		assertEquals("Wrong number of ContentCategories returned", 0, found.size());
		assertFalse("testContentCategory data not returned", found.contains(testContentCategory));
	}

	public void testFindByContentVersionAttributeBadAttribute() throws Exception
	{
		List found = testController.findByContentVersionAttribute("xxxxxxxxxxx", VERSION_ID);
		assertEquals("Wrong number of ContentCategories returned", 0, found.size());
		assertFalse("testContentCategory data not returned", found.contains(testContentCategory));
	}

	public void testFindByContentVersion() throws Exception
	{
		ContentCategoryVO sameAttribute = new ContentCategoryVO();
		sameAttribute.setAttributeName(getName());
		sameAttribute.setContentVersionId(VERSION_ID);
		sameAttribute.setCategory(testCategory);
		sameAttribute = testController.save(sameAttribute, InfoGluePrincipalControllerProxy.getController().getTestPrincipal());
		extraContentCategories.add(sameAttribute);

		ContentCategoryVO differentAttribute = new ContentCategoryVO();
		differentAttribute.setAttributeName("randomAttribute");
		differentAttribute.setContentVersionId(VERSION_ID);
		differentAttribute.setCategory(testCategory);
		differentAttribute = testController.save(differentAttribute, InfoGluePrincipalControllerProxy.getController().getTestPrincipal());
		extraContentCategories.add(differentAttribute);

		List found = testController.findByContentVersion(VERSION_ID);
		assertEquals("Wrong number of ContentCategories returned", 3, found.size());
		assertTrue("testContentCategory data not returned", found.contains(testContentCategory));
		assertTrue("sameAttribute data not returned", found.contains(sameAttribute));
		assertTrue("differentAttribute data not returned", found.contains(differentAttribute));
	}

	public void testFindByContentVersionBadId() throws Exception
	{
		List found = testController.findByContentVersion(new Integer(-9999999));
		assertEquals("Wrong number of ContentCategories returned", 0, found.size());
		assertFalse("testContentCategory data returned", found.contains(testContentCategory));
	}

	public void testFindByCategory() throws Exception
	{
		ContentCategoryVO differentAttribute = new ContentCategoryVO();
		differentAttribute.setAttributeName("randomAttribute");
		differentAttribute.setContentVersionId(VERSION_ID);
		differentAttribute.setCategory(testCategory);
		differentAttribute = testController.save(differentAttribute, InfoGluePrincipalControllerProxy.getController().getTestPrincipal());
		extraContentCategories.add(differentAttribute);

		List found = testController.findByCategory(testCategory.getId());
		assertEquals("Wrong number of ContentCategories returned", 2, found.size());
		assertTrue("testContentCategory data not returned", found.contains(testContentCategory));
		assertTrue("differentAttribute data not returned", found.contains(differentAttribute));
	}

	public void testFindByBadCategory() throws Exception
	{
		ContentCategoryVO differentAttribute = new ContentCategoryVO();
		differentAttribute.setAttributeName("randomAttribute");
		differentAttribute.setContentVersionId(VERSION_ID);
		differentAttribute.setCategory(testCategory);
		differentAttribute = testController.save(differentAttribute, InfoGluePrincipalControllerProxy.getController().getTestPrincipal());
		extraContentCategories.add(differentAttribute);

		List found = testController.findByCategory(new Integer(-999));
		assertEquals("Wrong number of ContentCategories returned", 0, found.size());
	}

	public void testDeleteByContentVersion() throws Exception
	{
		createWithRandomAttribute();
		testController.deleteByContentVersion(VERSION_ID);
		List found = testController.findByContentVersion(VERSION_ID);
		assertEquals("Wrong number of ContentCategories returned", 0, found.size());
		deleted = true;
	}

	public void testDeleteByCategory() throws Exception
	{
		createWithRandomAttribute();
		testController.deleteByCategory(testCategory.getId());
		List found = testController.findByCategory(testCategory.getId());
		assertEquals("Wrong number of ContentCategories returned", 0, found.size());
		deleted = true;
	}

	public void testDeleteByContentVersionAttribute() throws Exception
	{
		ContentCategoryVO random = createWithRandomAttribute();
		testController.deleteByContentVersionAttribute(random.getAttributeName(), random.getContentVersionId());
		List found = testController.findByContentVersionAttribute(random.getAttributeName(), random.getContentVersionId());
		assertEquals("Wrong number of ContentCategories returned", 0, found.size());
		deleted = true;
	}

	private ContentCategoryVO createWithRandomAttribute() throws Exception
	{
		return testController.save(new ContentCategoryVO("randomAttribute", VERSION_ID, testCategory), InfoGluePrincipalControllerProxy.getController().getTestPrincipal());
	}

	/**
	 * Verifies that the test content category was removed from the database
	 */
	private void assertRemoved()
	{
		try
		{
			testController.findById(testContentCategory.getId());
			fail("The ContentCategory was not deleted");
		}
		catch (Exception e)
		{ /* expected */ }
	}
}
