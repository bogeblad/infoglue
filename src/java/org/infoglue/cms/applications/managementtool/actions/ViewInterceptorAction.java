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

import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
import org.infoglue.cms.controllers.kernel.impl.simple.InterceptorController;
import org.infoglue.cms.entities.management.InterceptorVO;

public class ViewInterceptorAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;

    private InterceptorVO interceptorVO;
    private List allInterceptionPoints;
    private List assignedInterceptionPoints;

    public ViewInterceptorAction()
    {
        this(new InterceptorVO());
    }
    
    public ViewInterceptorAction(InterceptorVO interceptorVO)
    {
        this.interceptorVO = interceptorVO;
    }
       
    protected void initialize(Integer interceptorId) throws Exception
    {
		interceptorVO = InterceptorController.getController().getInterceptorVOWithId(getInterceptorId());
		allInterceptionPoints = InterceptionPointController.getController().getInterceptionPointVOList();
		assignedInterceptionPoints = InterceptorController.getController().getInterceptionPointVOList(getInterceptorId());
    }   
    
    public String doExecute() throws Exception
    {
        initialize(getInterceptorId());

        return "success";
    }
        
    public java.lang.Integer getInterceptorId()
    {
        return this.interceptorVO.getInterceptorId();
    }
        
    public void setInterceptorId(java.lang.Integer interceptorId)
    {
        this.interceptorVO.setInterceptorId(interceptorId);
    }
        
    public java.lang.String getName()
    {
        return this.interceptorVO.getName();
    }

    public java.lang.String getDescription()
    {
        return this.interceptorVO.getDescription();
    }

    public java.lang.String getClassName()
    {
        return this.interceptorVO.getClassName();
    }
    
        
	public List getAllInterceptionPoints()
	{
		return this.allInterceptionPoints;
	}

	public List getAssignedInterceptionPoints()
	{
		return this.assignedInterceptionPoints;
	}

	public InterceptorVO getInterceptorVO()
	{
		return this.interceptorVO;
	}

}
