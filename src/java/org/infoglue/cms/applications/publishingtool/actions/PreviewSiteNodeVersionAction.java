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

package org.infoglue.cms.applications.publishingtool.actions;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;

/**
 * @author ss
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */


public class PreviewSiteNodeVersionAction  extends InfoGlueAbstractAction 
{
	private Integer siteNodeVersionId;
	private SiteNodeVersionVO siteNodeVersionVO;

	/**
	 * @see org.infoglue.cms.applications.common.actions.WebworkAbstractAction#doExecute()
	 */
	protected String doExecute() throws Exception 
	{
		siteNodeVersionVO = SiteNodeVersionController.getController().getSiteNodeVersionVOWithId(siteNodeVersionId);
		return "success";
	}



	/**
	 * Returns the siteNodeVersionId.
	 * @return Integer
	 */
	public Integer getSiteNodeVersionId() {
		return siteNodeVersionId;
	}

	/**
	 * Returns the siteNodeVersionVO.
	 * @return SiteNodeVersionVO
	 */
	public SiteNodeVersionVO getSiteNodeVersionVO() {
		return siteNodeVersionVO;
	}

	/**
	 * Sets the siteNodeVersionId.
	 * @param siteNodeVersionId The siteNodeVersionId to set
	 */
	public void setSiteNodeVersionId(Integer siteNodeVersionId) {
		this.siteNodeVersionId = siteNodeVersionId;
	}

	/**
	 * Sets the siteNodeVersionVO.
	 * @param siteNodeVersionVO The siteNodeVersionVO to set
	 */
	public void setSiteNodeVersionVO(SiteNodeVersionVO siteNodeVersionVO) {
		this.siteNodeVersionVO = siteNodeVersionVO;
	}

}
