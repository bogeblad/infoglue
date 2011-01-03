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

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.taglib.TemplateControllerTag;

public class CategoryDisplayNameTag extends TemplateControllerTag 
{
	private static final long serialVersionUID = 4050206323348354355L;

    private final static Logger logger = Logger.getLogger(CategoryDisplayNameTag.class.getName());

	private Integer categoryId;
	private CategoryVO categoryVO;
	
	public int doStartTag() throws JspException 
    {        
        return EVAL_BODY_INCLUDE;
    }
    
	public int doEndTag() throws JspException
    {
		try
		{
			if(categoryId != null && categoryVO == null)
				categoryVO = CategoryController.getController().findById(categoryId, getController().getDatabase()).getValueObject();
				
			if(categoryVO != null)
				setResultAttribute(categoryVO.getLocalizedDisplayName(getController().getLocale().getLanguage(), LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(getController().getDatabase(), getController().getRepositoryId()).getLanguageCode()));
			else
				throw new JspException("Must state categoryId or categoryVO for CategoryDisplayNameTag");
		}
		catch (Exception e) 
		{
			logger.error("Error in CategoryDisplayNameTag:" + e.getMessage(), e);
		}

		categoryId = null;
        categoryVO = null;
        
	    return EVAL_PAGE;
    }

    public void setCategoryId(final String categoryId) throws JspException
    {
        this.categoryId = evaluateInteger("categoryDisplayName", "categoryId", categoryId);
    }

    public void setCategoryVO(final String categoryVO) throws JspException
    {
        this.categoryVO = (CategoryVO)evaluate("categoryDisplayName", "categoryVO", categoryVO, CategoryVO.class);
    }

}
