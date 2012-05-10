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

import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.RedirectController;
import org.infoglue.cms.entities.management.RedirectVO;
import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

/**
 * This class implements the action class for viewRedirect.
 * The use-case lets the user see all information about a specific site/redirect.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewRedirectAction extends InfoGlueAbstractAction
{ 
	private static final long serialVersionUID = 1L;

    protected RedirectVO redirectVO;


    public ViewRedirectAction()
    {
        this(new RedirectVO());
    }
    
    public ViewRedirectAction(RedirectVO redirectVO)
    {
        this.redirectVO = redirectVO;
    }
    
    protected void initialize(Integer redirectId) throws SystemException
    {
        redirectVO = RedirectController.getController().getRedirectVOWithId(redirectId);
    } 

    /**
     * The main method that fetches the Value-object for this use-case
     */
    
    public String doExecute() throws AccessConstraintException, ConstraintException, SystemException
    {
        this.initialize(getRedirectId());

        return "success";
    }

    /**
     * The main method that fetches the Value-object for this use-case
     */
    
    public String doLocalView() throws SystemException
    {
        this.initialize(getRedirectId());

        return "successLocal";
    }
          
    public java.lang.Integer getRedirectId()
    {
        return this.redirectVO.getRedirectId();
    }
        
    public void setRedirectId(java.lang.Integer redirectId)
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

    public String getPublishDateTime()
    {
    	return new VisualFormatter().formatDate(this.redirectVO.getPublishDateTime(), "yyyy-MM-dd HH:mm");
    }
    
    public void setPublishDateTime(String publishDateTime)
    {
    	this.redirectVO.setPublishDateTime(new VisualFormatter().parseDate(publishDateTime, "yyyy-MM-dd HH:mm"));
    }
    
    public String getExpireDateTime()
    {
    	return new VisualFormatter().formatDate(this.redirectVO.getExpireDateTime(), "yyyy-MM-dd HH:mm");
    }
    
    public void setExpireDateTime(String expireDateTime)
    {
    	this.redirectVO.setExpireDateTime(new VisualFormatter().parseDate(expireDateTime, "yyyy-MM-dd HH:mm"));
    }

    public java.lang.String getModifier()
    {
        return this.redirectVO.getModifier();
    }

    public String getCreatedDateTime()
    {
    	return new VisualFormatter().formatDate(this.redirectVO.getCreatedDateTime(), "yyyy-MM-dd HH:mm");
    }

}
