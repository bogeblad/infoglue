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

import org.infoglue.cms.entities.kernel.BaseEntityVO;

/**
 * This class represents an implementation of a property.
 * 
 * @author Mattias Bogeblad
 * 
 * @hibernate.class table="Property"
 */

public class InfoGlueProperty implements CastorProperty
{
    private Integer id;
    private String nameSpace;
    private String name;
    private String value;
    private String comment = "No comment";
    
    /**
     * @hibernate.id generator-class="native" type="long" column="id" unsaved-value="null"
     * 
     * @return long
     */    
    public Integer getId() 
    {
        return this.id;
    }
    
    public void setId(Integer id) 
    {
        this.id = id;
    }
    
    /**
     * @hibernate.property name="getNameSpace" column="nameSpace" type="string" not-null="false" unique="false"
     * 
     * @return String
     */
    public String getNameSpace()
    {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace)
    {
        this.nameSpace = nameSpace;
    }

    /**
     * @hibernate.property name="getName" column="name" type="string" not-null="false" unique="false"
     * 
     * @return String
     */
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @hibernate.property name="getValue" column="value" type="clob" not-null="false" unique="false"
     * 
     * @return String
     */
	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public Object getIdAsObject()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public BaseEntityVO getVO()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setVO(BaseEntityVO valueObject)
	{
		// TODO Auto-generated method stub
		
	}
}