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
import org.infoglue.cms.entities.management.InterceptionPointVO;

public class ViewInterceptionPointAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;

	private Integer interceptionPointId;
    private InterceptionPointVO interceptionPointVO;
    private List allInterceptors;
    private List assignedInterceptors;

    public ViewInterceptionPointAction()
    {
        this(new InterceptionPointVO());
    }
    
    public ViewInterceptionPointAction(InterceptionPointVO interceptionPointVO)
    {
        this.interceptionPointVO = interceptionPointVO;
    }
       
    protected void initialize(Integer interceptionPointId) throws Exception
    {
		interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithId(interceptionPointId);
		allInterceptors = InterceptorController.getController().getInterceptorVOList();
		assignedInterceptors = InterceptorController.getController().getInterceptorsVOList(interceptionPointId);
    }   
    
    public String doExecute() throws Exception
    {
        initialize(interceptionPointId);

        return "success";
    }
        
        
	public Integer getInterceptionPointId()
	{
		return interceptionPointId;
	}

	public void setInterceptionPointId(Integer interceptionPointId)
	{
		this.interceptionPointId = interceptionPointId;
	}

	public List getAllInterceptors()
	{
		return allInterceptors;
	}

	public List getAssignedInterceptors()
	{
		return assignedInterceptors;
	}

	public InterceptionPointVO getInterceptionPointVO()
	{
		return interceptionPointVO;
	}

}
