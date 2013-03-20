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

package org.infoglue.cms.entities.content;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.kernel.IBaseEntity;

public interface ContentVersionAttribute extends IBaseEntity
{

	public BaseEntityVO getVO();

	public void setVO(BaseEntityVO valueObject);
 
	public Integer getId();
	
	public Object getIdAsObject();

    public ContentVersionAttributeVO getValueObject();
        
    public void setValueObject(ContentVersionAttributeVO valueObject);
	
    public java.lang.Integer getContentVersionAttributeId();
            
    public void setContentVersionAttributeId(java.lang.Integer contentVersionAttributeId);
      
    public java.lang.Integer getContentVersionId();
            
    public void setContentVersionId(java.lang.Integer contentVersionId);
      
    public java.lang.String getAttributeName();
            
    public void setAttributeName(java.lang.String attributeName);
      
    public java.lang.String getAttributeValue();
            
    public void setAttributeValue(java.lang.String attributeValue);

}
