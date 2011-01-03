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
 * $Id: ContentCategoryActionTest.java,v 1.3 2008/06/04 07:19:51 mattias Exp $
 */
package org.infoglue.cms.applications.contenttool;

import java.util.ArrayList;
import java.util.Iterator;

import org.infoglue.cms.applications.contenttool.actions.ContentCategoryAction;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentCategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.InfoGluePrincipalControllerProxy;
import org.infoglue.cms.entities.content.ContentCategoryVO;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.WebWorkTestCase;

/**
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class ContentCategoryActionTest extends WebWorkTestCase
{
	private static final Integer VERSION_ID = new Integer("1234321");

	private ContentCategoryAction testAction = new ContentCategoryAction();
	private ArrayList extraContentCategories = new ArrayList();
	private ContentCategoryVO testContentCategory;
	private ContentCategoryController testController = ContentCategoryController.getController();
	private CategoryVO testCategory;
	private CategoryController testCategoryController = CategoryController.getController();

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
		if (testContentCategory != null)
			testController.delete(testContentCategory.getId(), InfoGluePrincipalControllerProxy.getController().getTestPrincipal());

		for (Iterator i = extraContentCategories.iterator(); i.hasNext();)
			testController.delete(((ContentCategoryVO)i.next()).getId(), InfoGluePrincipalControllerProxy.getController().getTestPrincipal());

		// If you delete the category first, then try to delete the ContentCategory
		// it will barf because it cannot pull up the Category relationship
		testCategoryController.delete(testCategory.getId());
	}


	public void testAdd() throws Exception
	{
		testAction.getContentCategory().setAttributeName("testAddAttributeName");
		testAction.getContentCategory().setContentVersionId(VERSION_ID);
		testAction.getContentCategory().getCategory().setCategoryId(testCategory.getId());

		assertSuccess(testAction, "add");

		ContentCategoryVO found = testController.findById(testAction.getContentCategoryId());
		assertEquals("ContentCategoy found is not the same as the one saved", testAction.getContentCategory(), found);
		extraContentCategories.add(found); // for cleanup
	}

	public void testDelete() throws Exception
	{
		testAction.setContentCategoryId(testContentCategory.getContentCategoryId());

		assertSuccess(testAction, "delete");

		try
		{
			testController.findById(testContentCategory.getContentCategoryId());
			fail("ContentCategory was not deleted");
		}
		catch (SystemException e)
		{
			// expected exception, set to null for tearDown
			testContentCategory = null;
		}
	}
}
