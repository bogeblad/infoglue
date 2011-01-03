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
 * $Id: CategoryActionTest.java,v 1.2 2006/03/06 16:54:01 mattias Exp $
 */
package org.infoglue.cms.applications.managementtool;

import org.infoglue.cms.applications.managementtool.actions.CategoryAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.WebWorkTestCase;

/**
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class CategoryActionTest extends WebWorkTestCase
{
	private CategoryAction testAction = new CategoryAction();
	private CategoryVO testCategory;
	private CategoryController testController = CategoryController.getController();

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
		if(testCategory != null)
			testController.delete(testCategory.getId());
	}

	public void testNew() throws Exception
	{
		assertSuccess(testAction, "new");
		assertTrue("Model was not new", testAction.getModel().isUnsaved());
	}

	public void testList() throws Exception
	{
		assertSuccess(testAction, "list");
		assertNotNull("Categories should not be null", testAction.getModels());
	}

	public void testEdit() throws Exception
	{
		testAction.setCategoryId(testCategory.getCategoryId());
		assertSuccess(testAction, "edit");
		assertFalse("Model was new", testAction.getModel().isUnsaved());
		assertEquals("Test Category was not returned", testCategory, testAction.getModel());
	}

	public void testBadEdit() throws Exception
	{
		testAction.setCategoryId(new Integer(Integer.MIN_VALUE));
		assertError(testAction, "edit");
	}

	public void testRootSave() throws Exception
	{
		testAction.getCategory().setName(testCategory.getName());
		testAction.getCategory().setDescription(testCategory.getDescription());

		try
		{
			testAction.setCommand("save");
			assertResult(CategoryAction.MAIN, testAction.execute());
			CategoryVO newCategory = testAction.getCategory();
			assertEquals("Category name is wrong", testCategory.getName(), newCategory.getName());
			assertEquals("Category name is wrong", testCategory.getDescription(), newCategory.getDescription());
		}
		finally
		{
			testController.delete(testAction.getCategoryId());
		}
	}

	public void testChildSave() throws Exception
	{
		testAction.getCategory().setName(testCategory.getName());
		testAction.getCategory().setDescription(testCategory.getDescription());
		testAction.getCategory().setParentId(new Integer(0));

		try
		{
			assertSuccess(testAction, "save");
			CategoryVO newCategory = testAction.getCategory();
			assertEquals("Category name is wrong", testCategory.getName(), newCategory.getName());
			assertEquals("Category description is wrong", testCategory.getDescription(), newCategory.getDescription());
		}
		finally
		{
			testController.delete(testAction.getCategoryId());
		}
	}

	public void testBadSave() throws Exception
	{
		testAction.getCategory().setName(null);
		assertInput(testAction, "save");
		assertNotNull("Error do not exist", testAction.getErrors());
		assertTrue("Contraint Errors do not exist", testAction.getErrors().hasErrors());
	}

	public void testDeleteRoot() throws Exception
	{
		testAction.setCategoryId(testCategory.getCategoryId());
		testAction.setCommand("delete");
		assertResult(CategoryAction.MAIN, testAction.execute());
		assertDeleteWorked();
	}

	public void testDeleteOfChild() throws Exception
	{
		testCategory.setParentId(new Integer(Integer.MAX_VALUE));
		testCategory = testController.save(testCategory);

		testAction.setCategoryId(testCategory.getCategoryId());
		assertSuccess(testAction, "delete");
		assertDeleteWorked();
	}

	public void testBadDelete() throws Exception
	{
		testAction.setCategoryId(new Integer(Integer.MIN_VALUE));
		assertError(testAction, "delete");
	}

	public void testMove() throws Exception
	{
		Integer parentId = new Integer(-1);

		testAction.setCategoryId(testCategory.getCategoryId());
		testAction.getCategory().setParentId(parentId);
		assertSuccess(testAction, "move");

		CategoryVO updatedCategory = testAction.getCategory();
		assertEquals("Category parent is wrong", parentId, updatedCategory.getParentId());

		testCategory = testController.findById(testCategory.getId());
		assertEquals("Category parent is wrong", parentId, testCategory.getParentId());
	}



	private void assertDeleteWorked()
	{
		try
		{
			testController.findById(testCategory.getCategoryId());
			fail("Test Category was not removed");
		}
		catch (SystemException e)
		{
			// Expected exception, set to null so tearDown wont try to remove it again
			testCategory = null;
		}
	}
}
