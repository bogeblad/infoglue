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
 * $Id: CategoryControllerTest.java,v 1.3 2008/06/04 07:19:51 mattias Exp $
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
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.InfoGlueTestCase;

/**
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class CategoryControllerTest extends InfoGlueTestCase
{
	private CategoryVO testCategory;
	private ArrayList extraCategories = new ArrayList();
	private CategoryController testController = CategoryController.getController();

	private ContentCategoryController contentCategoryStore = ContentCategoryController.getController();
	
	protected void setUp() throws Exception
	{
		super.setUp();
		testCategory = new CategoryVO();
		testCategory.setName(getName());
		testCategory.setDescription(getName() + " description");

		testCategory = testController.save(testCategory);
	}

	protected void tearDown() throws Exception
	{
		testController.delete(testCategory.getId());
		assertRemoved();

		for (Iterator i = extraCategories.iterator(); i.hasNext();)
			testController.delete(((CategoryVO)i.next()).getId());

		assertTrue("ContentCateogries were not removed as part of Category delete",
				   contentCategoryStore.findByCategory(testCategory.getId()).isEmpty());
	}

	public void testFindById() throws Exception
	{
		CategoryVO found = testController.findById(testCategory.getId());
		assertEquals("Category data found does not match the created data", testCategory, found);
	}

	public void testUpdate() throws Exception
	{
		CategoryVO updater = testController.findById(testCategory.getId());
		updater.setName("Updated Name");
		testController.save(updater);

		CategoryVO found = testController.findById(updater.getId());
		assertEquals("Category data found does not match the updated data", updater, found);
	}

	public void testFindByParent() throws Exception
	{
		CategoryVO one = new CategoryVO();
		one.setName("FindByParentExtraOne");
		one.setDescription("Extra one description");
		one.setParentId(testCategory.getId());
		one = testController.save(one);
		testCategory.getChildren().add(one);

		CategoryVO two = new CategoryVO();
		two.setName("FindByParentExtraTwo");
		two.setDescription("Extra two description");
		two.setParentId(testCategory.getId());
		two = testController.save(two);
		testCategory.getChildren().add(two);

		List categories = testController.findByParent(testCategory.getId());
		assertTrue("Extra Category One was not found", categories.contains(one));
		assertTrue("Extra Category Two was not found", categories.contains(two));
	}


	public void testFindActiveByParent() throws Exception
	{
		CategoryVO one = new CategoryVO();
		one.setName("FindByParentExtraOne");
		one.setDescription("Extra one description");
		one.setParentId(testCategory.getId());
		one = testController.save(one);
		testCategory.getChildren().add(one);

		CategoryVO two = new CategoryVO();
		two.setName("FindByParentExtraTwo");
		two.setDescription("Extra two description");
		two.setParentId(testCategory.getId());
		two.setActive(false);
		two = testController.save(two);
		testCategory.getChildren().add(two);

		List categories = testController.findActiveByParent(testCategory.getId());
		assertTrue("Extra Category One was not found", categories.contains(one));
		assertFalse("Extra Inactive Category Two was found", categories.contains(two));
	}


	public void testFindRootCategories() throws Exception
	{
		CategoryVO nonRoot = new CategoryVO();
		nonRoot.setName("NonRoot");
		nonRoot.setDescription("NonRoot description");
		nonRoot.setParentId(testCategory.getId());
		nonRoot = testController.save(nonRoot);

		CategoryVO otherRoot = new CategoryVO();
		otherRoot.setName("FindByParentExtraTwo");
		otherRoot.setDescription("Extra two description");
		otherRoot = testController.save(otherRoot);
		extraCategories.add(otherRoot);

		List categories = testController.findRootCategories();
		assertTrue("Test Category was not found", categories.contains(testCategory));
		assertTrue("Other Root Category was not found", categories.contains(otherRoot));
		assertFalse("NonRoot Category was found", categories.contains(nonRoot));

		// For cleanup tests
		testCategory.getChildren().add(nonRoot);
	}

	public void testFindWithChildren() throws Exception
	{
		CategoryVO one = new CategoryVO();
		one.setName("FindWithChildrenExtraOne");
		one.setDescription("Extra one description");
		one.setParentId(testCategory.getId());
		one = testController.save(one);
		testCategory.getChildren().add(one);

		CategoryVO two = new CategoryVO();
		two.setName("FindWithChildrenExtraTwo");
		two.setDescription("Extra two description");
		two.setParentId(testCategory.getId());
		two = testController.save(two);
		testCategory.getChildren().add(two);

		CategoryVO found = testController.findWithChildren(testCategory.getId());
		assertEquals("Category data found does not match the created data", testCategory, found);
		assertTrue("Extra Category One was not a child", found.getChildren().contains(one));
		assertTrue("Extra Category Two was not a child", found.getChildren().contains(two));
	}

	public void testDeleteContentCategories() throws Exception
	{
		ContentCategoryVO cc = new ContentCategoryVO();
		cc.setContentVersionId(new Integer(-999));
		cc.setAttributeName(getName());
		cc.setCategory(testCategory);
		cc = contentCategoryStore.save(cc, InfoGluePrincipalControllerProxy.getController().getTestPrincipal());

		List found = contentCategoryStore.findByCategory(testCategory.getId());
		assertEquals("Wrong number of ContentCategories found", 1, found.size());
		assertTrue("ContentCategory not found by Category", found.contains(cc));
	}

	public void testMoveCategory() throws Exception
	{
		CategoryVO one = new CategoryVO();
		one.setName("MoveCategoryExtraOne");
		one.setDescription("Extra one description");
		one.setParentId(testCategory.getId());
		one = testController.save(one);

		CategoryVO otherRoot = new CategoryVO();
		otherRoot.setName("MoveCategoryParentExtraTwo");
		otherRoot.setDescription("Extra two description");
		otherRoot = testController.save(otherRoot);
		extraCategories.add(otherRoot);

		// test original setup
		List categories = testController.findByParent(testCategory.getId());
		assertTrue("Before Move - Child Category was not found by original parent", categories.contains(one));
		categories = testController.findByParent(otherRoot.getId());
		assertFalse("Before Move - Child Category was found by new parent", categories.contains(one));

		one = testController.moveCategory(one.getId(), otherRoot.getId());

		// test new setup
		categories = testController.findByParent(testCategory.getId());
		assertFalse("After Move - Child Category was found by old parent", categories.contains(one));
		categories = testController.findByParent(otherRoot.getId());
		assertTrue("After Move - Child Category was not found by new parent", categories.contains(one));
	}

	// Make sure it was removed from the DB
	private void assertRemoved()
	{
		try
		{
			testController.findById(testCategory.getId());
			fail("The Category was not deleted");
		}
		catch(Exception e)
		{ /* expected */ }

		for (Iterator iter = testCategory.getChildren().iterator(); iter.hasNext();)
		{
			try
			{
				testController.findById(((CategoryVO) iter.next()).getId());
				fail("A Category child was not deleted");
			}
			catch(Exception e)
			{ /* expected */ }
		}
	}
}
