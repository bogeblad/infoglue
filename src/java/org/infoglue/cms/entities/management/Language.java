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

public interface Language extends IBaseEntity
{
 	public Integer getId();
        
    public LanguageVO getValueObject();
    
    public void setValueObject(LanguageVO valueObject);

    
    public java.lang.Integer getLanguageId();
    
    public void setLanguageId(java.lang.Integer languageId);
    
    public java.lang.String getName();
    
    public void setName(java.lang.String name) throws ConstraintException;
    
    public java.lang.String getLanguageCode();
    
    public void setLanguageCode(java.lang.String languageCode) throws ConstraintException;

	public java.lang.String getCharset();
    
	public void setCharset(java.lang.String charset) throws ConstraintException;
    
    public java.util.Collection getRepositoryLanguages();
    
    public void setRepositoryLanguages(java.util.Collection repositoryLanguages);
        
}
