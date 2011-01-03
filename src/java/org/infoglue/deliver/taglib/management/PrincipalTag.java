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

package org.infoglue.deliver.taglib.management;

import javax.servlet.jsp.JspException;

import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.deliver.taglib.TemplateControllerTag;

public class PrincipalTag extends TemplateControllerTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

	private String userName;
	private ContentVersionVO contentVersion;
	
    public PrincipalTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
		try
		{
		    if(userName != null && !userName.equals(""))
		        setResultAttribute(getController().getPrincipal(userName));
		    else if(contentVersion != null)
		    {
		        String versionUserName = contentVersion.getVersionModifier();
		        if(versionUserName != null && !versionUserName.equals(""))
		            setResultAttribute(getController().getPrincipal(versionUserName));
			    else
			        throw new JspException("ContentVersion had no valid user:" + versionUserName);
		    }
		    else
		        throw new JspException("Must state either userName or contentVersion");
		}
		finally
		{
			this.userName = null;
			this.contentVersion = null;
		}
	    
	    return EVAL_PAGE;
    }

    public void setUserName(final String userName) throws JspException
    {
        this.userName = evaluateString("principal", "userName", userName);
    }
    
    public void setContentVersion(String contentVersionExp) throws JspException
    {
        this.contentVersion = (ContentVersionVO)evaluate("principal", "contentVersion", contentVersionExp, ContentVersionVO.class);
    }
}
