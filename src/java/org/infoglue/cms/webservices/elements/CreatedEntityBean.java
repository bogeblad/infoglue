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

package org.infoglue.cms.webservices.elements;

/**
 * This bean can contain information about new items created during the webservice. Remember that it's up to the webservice
 * to decide what entities to expose here.
 */

public class CreatedEntityBean
{
	private String className = "";
	private Long entityId = 0L;
		
	public CreatedEntityBean()
	{
	}
	
	public CreatedEntityBean(String className, Long entityId)
	{
		this.className = className;
		this.entityId = entityId;
	}
	
	public String getClassName()
	{
		return className;
	}
	
	public void setClassName(String className)
	{
		this.className = className;
	}
	
	public Long getEntityId()
	{
		return entityId;
	}
	
	public void setEntityId(Long entityId)
	{
		this.entityId = entityId;
	}
}
