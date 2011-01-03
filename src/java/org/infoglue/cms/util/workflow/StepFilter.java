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
*/
package org.infoglue.cms.util.workflow;

import org.infoglue.cms.entities.mydesktop.WorkflowStepVO;

/**
 * Provides a mechanism for filtering steps in a workflow based on the owner of the step and the current user
 * @author <a href="mailto:jedprentice@gmail.com">Jed Prentice</a>
 * @version $Revision: 1.3 $ $Date: 2005/01/07 14:15:14 $
 */
public interface StepFilter
{
	/**
	 * Indicates whether the given step is allowed by the filter
	 * @param step a workflow step
	 * @return true if the step is allowed, otherwise returns false
	 */
	boolean isAllowed(WorkflowStepVO step);
}
