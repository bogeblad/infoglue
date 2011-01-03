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

/**
 * This class represents a Property entry which has a name and a value.
 * All applications that wants to have properties in their systems can inherit this class
 * and solve the persistence handling there.
 * 
 * @author Mattias Bogeblad
 */

public interface CastorProperty extends IBaseEntity
{
    public Integer getId(); 
    
    public void setId(Integer id); 
    
    public String getNameSpace();

    public void setNameSpace(String nameSpace);

    public String getName();

    public void setName(String name);

    public String getValue();

	public void setValue(String value);

    public String getComment();

	public void setComment(String comment);
}