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

/**
 * This class implements the action class for viewRedirect.
 * The use-case lets the user see all information about a specific site/redirect.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewRedirectAction extends InfoGlueAbstractAction
{ 
	private static final long serialVersionUID = 1L;

    private RedirectVO redirectVO;


    public ViewRedirectAction()
    {
        this(new RedirectVO());
    }
    
    public ViewRedirectAction(RedirectVO redirectVO)
    {
        this.redirectVO = redirectVO;
    }
    
    protected void initialize(Integer redirectId) throws Exception
    {
        redirectVO = RedirectController.getController().getRedirectVOWithId(redirectId);
    } 

    /**
     * The main method that fetches the Value-object for this use-case
     */
    
    public String doExecute() throws Exception
    {
        this.initialize(getRedirectId());

        return "success";
    }

    /**
     * The main method that fetches the Value-object for this use-case
     */
    
    public String doLocalView() throws Exception
    {
        this.initialize(getRedirectId());

        return "successLocal";
    }
          
    public java.lang.Integer getRedirectId()
    {
        return this.redirectVO.getRedirectId();
    }
        
    public void setRedirectId(java.lang.Integer redirectId) throws Exception
    {
        this.redirectVO.setRedirectId(redirectId);
    }

    public java.lang.String getUrl()
    {
        return this.redirectVO.getUrl();
    }

    public java.lang.String getRedirectUrl()
    {
        return this.redirectVO.getRedirectUrl();
    }

}
