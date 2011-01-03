/**
 * $Id: OwnerStepFilterTest.java,v 1.2 2006/03/06 16:54:41 mattias Exp $
 * Created by jed on Dec 28, 2004
 */
package org.infoglue.cms.util.workflow;

import org.infoglue.cms.entities.mydesktop.WorkflowStepVO;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.InfoGlueTestCase;

/**
 * @author jed
 * @version $Revision: 1.2 $ $Date: 2006/03/06 16:54:41 $
 */
public class OwnerStepFilterTest extends InfoGlueTestCase
{
	private static final WorkflowStepVO adminStep = createStep(getAdminPrincipal());
	private static final WorkflowStepVO userStep = createStep(getCmsUserPrincipal());
	private static final WorkflowStepVO unownedStep = new WorkflowStepVO();

	protected void setUp() throws Exception
	{
		// Don't initialize InfoGlue; we don't need it for this
	}

	public void testIsAllowedAdministrator() throws Exception
	{
		OwnerStepFilter filter = new OwnerStepFilter(getAdminPrincipal());
		assertTrue("admin should be allowed if admin owns it", filter.isAllowed(adminStep));
		assertTrue("admin should be allowed if user owns it", filter.isAllowed(userStep));
		assertTrue("admin should be allowed if nobody owns it", filter.isAllowed(unownedStep));
	}

	public void testIsAllowedUser() throws Exception
	{
		OwnerStepFilter filter = new OwnerStepFilter(getCmsUserPrincipal());
		assertFalse("user should not be allowed if admin owns it", filter.isAllowed(adminStep));
		assertTrue("user should be allowed if user owns it", filter.isAllowed(userStep));
		assertTrue("user should be allowed if nobody owns it", filter.isAllowed(unownedStep));
	}

	private static WorkflowStepVO createStep(InfoGluePrincipal owner)
	{
		WorkflowStepVO step = new WorkflowStepVO();
		step.setOwner(owner.getName());
		return step;
	}
}
