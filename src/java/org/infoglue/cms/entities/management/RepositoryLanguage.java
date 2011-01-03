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

package org.infoglue.cms.entities.management;

import org.infoglue.cms.entities.kernel.IBaseEntity;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;

public interface RepositoryLanguage extends IBaseEntity
{
        
    public RepositoryLanguageVO getValueObject();
    
    public void setValueObject(RepositoryLanguageVO valueObject);

    public java.lang.Integer getRepositoryLanguageId();
    
    public void setRepositoryLanguageId(java.lang.Integer RepositoryLanguageId) throws SystemException;
    
    public java.lang.Boolean getIsPublished();
    
    public void setIsPublished(java.lang.Boolean isPublished) throws ConstraintException;

    public java.lang.Integer getSortOrder();
    
    public void setSortOrder(java.lang.Integer sortOrder) throws ConstraintException;

    public org.infoglue.cms.entities.management.Language getLanguage();
    
    public void setLanguage(org.infoglue.cms.entities.management.Language language);
    
    public org.infoglue.cms.entities.management.Repository getRepository();
    
    public void setRepository(org.infoglue.cms.entities.management.Repository repository);
        
}
