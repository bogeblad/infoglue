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
import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.exception.SystemException;

/**
 * This action removes a language from the system.
 * 
 * @author Mattias Bogeblad
 */

public class DeleteLanguageAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
	private LanguageVO languageVO;
	private Integer languageId;
	
	public DeleteLanguageAction()
	{
		this(new LanguageVO());
	}

	public DeleteLanguageAction(LanguageVO languageVO) {
		this.languageVO = languageVO;
	}
	
	protected String doExecute() throws Exception 
	{
		this.languageVO.setLanguageId(this.getLanguageId());
		LanguageController.getController().delete(languageVO);
		
		return "success";
	}
	
	public void setLanguageId(Integer languageId) throws SystemException
	{
		this.languageVO.setLanguageId(languageId);	
	}

    public java.lang.Integer getLanguageId()
    {
        return this.languageVO.getLanguageId();
    }
        
	
}
