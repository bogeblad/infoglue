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

public class ViewLanguageAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;

    private LanguageVO languageVO;

    public ViewLanguageAction()
    {
        this(new LanguageVO());
    }
    
    public ViewLanguageAction(LanguageVO languageVO)
    {
        this.languageVO = languageVO;
    }
    
    protected void initialize(Integer languageId) throws Exception
    {
        languageVO = LanguageController.getController().getLanguageVOWithId(languageId);
    } 

    public String doExecute() throws Exception
    {
        this.initialize(getLanguageId());

        return "success";
    }
        
    public java.lang.Integer getLanguageId()
    {
        return this.languageVO.getLanguageId();
    }
        
    public void setLanguageId(java.lang.Integer languageId)
    {
        this.languageVO.setLanguageId(languageId);
    }
    
    public java.lang.String getName()
    {
        return this.languageVO.getName();
    }
        
	public java.lang.String getLanguageCode()
    {
        return this.languageVO.getLanguageCode();
    }
    
	public java.lang.String getCharset()
	{
		return this.languageVO.getCharset();
	}
    
}
