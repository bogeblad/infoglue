package org.infoglue.cms.entities.management;

import org.infoglue.cms.entities.ValidationTestCase;

/**
 * Test the CategoryVO validation stuff
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class CategoryTest extends ValidationTestCase
{

	public void testValidation() throws Exception
	{
		checkSuccess(create(generateString(10)));
		checkSuccess(create(generateString(10), generateString(0)));
		checkSuccess(create(generateString(10), generateString(10)));

		// Boundary Conditions
		checkSuccess(create(generateString(1)));
		checkSuccess(create(generateString(100)));
		checkSuccess(create(generateString(10), generateString(1)));
		checkSuccess(create(generateString(10), generateString(255)));

		checkFailure(create(null));
		checkFailure(create(generateString(0)));
		checkFailure(create(generateString(101)));
		checkFailure(create(generateString(500)));
		checkFailure(create(generateString(10), generateString(256)));
		checkFailure(create(generateString(10), generateString(500)));
	}

	private CategoryVO create(String name)
	{
		return create(name, null);
	}

	private CategoryVO create(String name, String desc)
	{
		CategoryVO c = new CategoryVO();
		c.setName(name);
		c.setDescription(desc);
		return c;
	}

}
