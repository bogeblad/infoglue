package org.infoglue.cms.entities;

import junit.framework.TestCase;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * Base class to help with validation test cases
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class ValidationTestCase extends TestCase
{
	private static final char CHAR = 'X';

	/**
	 * Use this to assert that there are NO validation errors
	 */
	protected void checkFailure(BaseEntityVO vo) throws Exception
	{
		ConstraintExceptionBuffer buffer = vo.validate();
		assertFalse("No validation errors were found for " + vo, buffer.isEmpty());
	}

	/**
	 * Use this to assert that there ARE validation errors
	 */
	protected void checkSuccess(BaseEntityVO vo) throws Exception
	{
		ConstraintExceptionBuffer buffer = vo.validate();
		assertTrue(buffer.toString(), buffer.isEmpty());
	}

	/**
	 * Generates a String of the supplied length
	 */
	protected String generateString(int length)
	{
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < length; i++)
			sb.append(CHAR);
		return sb.toString();
	}
}
