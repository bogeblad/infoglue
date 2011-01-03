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
 * $Id: ViewContentTypeDefinitionActionTest.java,v 1.2 2006/03/06 16:54:01 mattias Exp $
 */
package org.infoglue.cms.applications.managementtool;

import java.util.HashMap;
import java.util.List;

import org.infoglue.cms.applications.managementtool.actions.ViewContentTypeDefinitionAction;
import org.infoglue.cms.controllers.ContentTypeDefinitionControllerTest;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.entities.management.CategoryAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.util.WebWorkTestCase;

/**
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class ViewContentTypeDefinitionActionTest extends WebWorkTestCase
{
	private ViewContentTypeDefinitionAction testAction = new ViewContentTypeDefinitionAction();
	private ContentTypeDefinitionVO testDefinition;
	private ContentTypeDefinitionController testController = ContentTypeDefinitionController.getController();

	protected void setUp() throws Exception
	{
		super.setUp();

		testDefinition = new ContentTypeDefinitionVO();
		testDefinition.setName(getName());
		testDefinition.setSchemaValue(ContentTypeDefinitionControllerTest.getSampleDefintion());
		testDefinition = testController.create(testDefinition);

		testAction.setContentTypeDefinitionId(testDefinition.getContentTypeDefinitionId());
	}

	protected void tearDown() throws Exception
	{
		testController.delete(testDefinition);
		assertRemoved();
	}


	public void testInsertAssetKeys() throws Exception
	{
		List keys = testController.getDefinedAssetKeys(testDefinition, true);
		assertEquals("Wrong number of keys found", 3, keys.size());

		assertResult(ViewContentTypeDefinitionAction.USE_EDITOR, testAction.doInsertAssetKey());
		refreshTestDefinition();

		keys = testController.getDefinedAssetKeys(testDefinition, true);
		assertEquals("New key was not added", 4, keys.size());
	}

	public void testInsertCategoryKeys() throws Exception
	{
		List keys = testController.getDefinedCategoryKeys(testDefinition, true);
		assertEquals("Wrong number of keys found", 3, keys.size());

		assertResult(ViewContentTypeDefinitionAction.USE_EDITOR, testAction.doInsertCategoryKey());
		refreshTestDefinition();

		keys = testController.getDefinedCategoryKeys(testDefinition, true);
		assertEquals("New key was not added", 4, keys.size());
	}

	public void testUpdateAssetKeys() throws Exception
	{
		testAction.setAssetKey("other-image");
		testAction.setNewAssetKey(getName());
		assertResult(ViewContentTypeDefinitionAction.USE_EDITOR, testAction.doUpdateAssetKey());
		refreshTestDefinition();

		List keys = testController.getDefinedAssetKeys(testDefinition, true);
		assertEquals("Wrong number of keys found", 3, keys.size());
		assertTrue("updated key not found", keys.contains(getName()));
		assertFalse("old key found", keys.contains("other-image"));
	}

	public void testUpdateCategoryKeys() throws Exception
	{
		testAction.setCategoryKey("third");
		testAction.setNewCategoryKey(getName());

		HashMap params = new HashMap();
		params.put("title", "New Title");
		params.put("description", "New Description");
		params.put("categoryId", "999");
		setSingleValueParameters(params);

		assertResult(ViewContentTypeDefinitionAction.USE_EDITOR, testAction.doUpdateCategoryKey());
		refreshTestDefinition();

		List keys = testController.getDefinedCategoryKeys(testDefinition, true);
		assertEquals("Wrong number of keys found", 3, keys.size());
		assertTrue("updated key not found", keys.contains(new CategoryAttribute(getName(), "999", "New Title", "New Description")));
		assertFalse("old key found", keys.contains(new CategoryAttribute("third", "300", "Third Title", "Third Description")));
	}

	public void testDeleteAssetKeys() throws Exception
	{
		testAction.setAssetKey("other-image");
		assertResult(ViewContentTypeDefinitionAction.USE_EDITOR, testAction.doDeleteAssetKey());
		refreshTestDefinition();

		List keys = testController.getDefinedAssetKeys(testDefinition, true);
		assertEquals("Wrong number of keys found", 2, keys.size());
		assertFalse("old key found", keys.contains("other-image"));
	}

	public void testDeleteCategoryKeys() throws Exception
	{
		testAction.setCategoryKey("third");
		assertResult(ViewContentTypeDefinitionAction.USE_EDITOR, testAction.doDeleteCategoryKey());
		refreshTestDefinition();

		List keys = testController.getDefinedCategoryKeys(testDefinition, true);
		assertEquals("Wrong number of keys found", 2, keys.size());
		assertFalse("old key found", keys.contains(new CategoryAttribute("third", "300")));
	}

	private void refreshTestDefinition() throws Exception
	{
		testDefinition = testController.getContentTypeDefinitionVOWithId(testDefinition.getId());
	}

	// Make sure it was removed from the DB
	private void assertRemoved()
	{
		try
		{
			testController.getContentTypeDefinitionVOWithId(testDefinition.getId());
			fail("The ContentTypeDefinition was not deleted");
		}
		catch(Exception e)
		{ /* expected */ }
	}
}
