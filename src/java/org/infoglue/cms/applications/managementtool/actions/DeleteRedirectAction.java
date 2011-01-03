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

package org.infoglue.cms.applications.managementtool.actions;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RedirectController;
import org.infoglue.cms.entities.management.RedirectVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

/**
 * This action removes a redirect from the system.
 * 
 * @author Mattias Bogeblad
 */

public class DeleteRedirectAction extends InfoGlueAbstractAction
{
	private RedirectVO redirectVO;
	private Integer redirectId;
	
	public DeleteRedirectAction()
	{
		this(new RedirectVO());
	}

	public DeleteRedirectAction(RedirectVO redirectVO) 
	{
		this.redirectVO = redirectVO;
	}
	
	protected String doExecute() throws ConstraintException, Exception 
	{
	    this.redirectVO.setRedirectId(this.getRedirectId());
		RedirectController.getController().delete(this.redirectVO);
		return "success";
	}
	
	public void setRedirectId(Integer redirectId) throws SystemException
	{
		this.redirectVO.setRedirectId(redirectId);	
	}

    public java.lang.Integer getRedirectId()
    {
        return this.redirectVO.getRedirectId();
    }
        
	
}
