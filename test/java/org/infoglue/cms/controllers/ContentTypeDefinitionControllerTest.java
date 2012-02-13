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
 * $Id: ContentTypeDefinitionControllerTest.java,v 1.2 2006/03/06 16:54:01 mattias Exp $
 */
package org.infoglue.cms.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.entities.management.CategoryAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.util.InfoGlueTestCase;
import org.infoglue.cms.util.ResourceHelper;

/**
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class ContentTypeDefinitionControllerTest extends InfoGlueTestCase
{
	private ContentTypeDefinitionVO testDefinition;
	private ContentTypeDefinitionController testController = ContentTypeDefinitionController.getController();

	protected void setUp() throws Exception
	{
		super.setUp();

		testDefinition = new ContentTypeDefinitionVO();
		testDefinition.setName(getName());
		testDefinition.setSchemaValue(getSampleDefintion());
		testDefinition = testController.create(testDefinition);
	}

	protected void tearDown() throws Exception
	{
		testController.delete(testDefinition);
		assertRemoved();
	}

	public void testGetDefinedAssetKeys() 
	{
		List keys = testController.getDefinedAssetKeys(testDefinition, true);
		assertEquals("Wrong number of keys were found", 3, keys.size());
		assertTrue("image key not found", keys.contains("image"));
		assertTrue("second-image key not found", keys.contains("second-image"));
		assertTrue("other-image key not found", keys.contains("other-image"));
		assertFalse("xxxxxx key found", keys.contains("xxxxxx"));
	}

	public void testGetDefinedCategoryKeys() 
	{
		List keys = testController.getDefinedCategoryKeys(testDefinition, true);
		assertEquals("Wrong number of keys were found", 3, keys.size());
		assertTrue("first key not found", keys.contains(new CategoryAttribute("first", "100", "First Title", "First Description")));
		assertTrue("second key not found", keys.contains(new CategoryAttribute("second", "200", "Second Title", "Second Description")));
		assertTrue("third key not found", keys.contains(new CategoryAttribute("third", "300", "Third Title", "Third Description")));
		assertFalse("xxxxxx key found", keys.contains(new CategoryAttribute("xxxxx", "0")));
	}

	/**
	 * For easy of testing and writing I defined a test Schema on the filesystem in the same
	 * package as this class. We can put every singe type of variation in here and use it
	 * for testing, or do something slicker, but this will work for testing keys, and can grow
	 * in the future.
	 *
	 * This is also used by ViewContentTypeDefinitionActionTest
	 */
	public static String getSampleDefintion() throws IOException
	{
		InputStream is = ResourceHelper.getResourceAsStream("org/infoglue/cms/controllers/TestContentDefinition.xml", ContentTypeDefinitionControllerTest.class);

		if(is == null)
			fail("Unable to read test document, make sure it got copied to the test classpath");

		String line = null;
		StringBuffer sb = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		while((line = reader.readLine()) != null)
			sb.append(line);

		reader.close();
		return sb.toString();
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
