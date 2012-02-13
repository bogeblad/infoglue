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

package org.infoglue.cms.applications.contenttool.actions;

import java.util.List;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.exception.SystemException;

/**
 *
 * @author Mattias Bogeblad
 * 
 * Present a list of contentVersion under a given content, showing off what has happened.
 */

public class ViewContentVersionHistoryAction extends InfoGlueAbstractAction 
{
	private static final long serialVersionUID = 1L;
	
	private Integer contentId;
	private List contentVersionVOList;
	private Boolean inline					= false;

	protected String doExecute() throws SystemException 
	{
	    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(this.contentId);
	    contentVersionVOList = ContentVersionController.getContentVersionController().getContentVersionVOWithParent(contentId);

	    return "success";
	}

	public String doV3() throws SystemException 
	{
		doExecute();

	    return "successV3";
	}

	/**
	 * @return
	 */
	
	public Integer getContentId() 
	{
		return contentId;
	}

	/**
	 * @param integer
	 */
	
	public void setContentId(Integer integer) 
	{
		contentId = integer;
	}

    public List getContentVersionVOList()
    {
        return contentVersionVOList;
    }
    
    public void setInline(Boolean inline)
    {
        this.inline = inline;
    }

    public Boolean getInline()
    {
        return inline;
    }
}
