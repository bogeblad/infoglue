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

package org.infoglue.deliver.taglib.structure;

import javax.servlet.jsp.JspException;

import org.infoglue.deliver.taglib.component.ComponentLogicTag;

public class RelatedPagesTag extends ComponentLogicTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

	private Integer contentId;
	private String propertyName;
	private String attributeName;
	private boolean useInheritance = true;
	private boolean useRepositoryInheritance = true;
		
    public RelatedPagesTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
	    if(contentId != null)
	    {
	        setResultAttribute(getController().getRelatedPages(contentId, attributeName));
	    }
	    else if(propertyName != null)
	    {
	        setResultAttribute(getComponentLogic().getRelatedPages(propertyName, attributeName, useRepositoryInheritance));
	    }
	    else
	    {
	        throw new JspException("You must specify either contentId or attributeName");
	    }
	    
	    return EVAL_PAGE;
    }

    public void setPropertyName(String propertyName) throws JspException
    {
        this.propertyName = evaluateString("relatedPages", "propertyName", propertyName);
    }
		
    public void setAttributeName(String attributeName)
    {
        this.attributeName = attributeName;
    }
    
    public void setContentId(String contentId) throws JspException
    {
        this.contentId = evaluateInteger("RelatedPagesTag", "contentId", contentId);
    }
    
    public void setUseInheritance(boolean useInheritance)
    {
        this.useInheritance = useInheritance;
    }
    
    public void setUseRepositoryInheritance(boolean useRepositoryInheritance)
    {
        this.useRepositoryInheritance = useRepositoryInheritance;
    }

}
