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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

/**
 * This is an interface dictating what a custom workflow action should look like.
 * That is - a POJO which can be called by the workflow engine. A good complement to scripts or
 * webwork actions.
 * 
 * @author Mattias Bogeblad
 */

public interface CustomWorkflowAction
{
    //public abstract void setContext();
    
    public abstract void invokeAction(String userName, HttpServletRequest request, Map args, PropertySet ps) throws WorkflowException; 
}
