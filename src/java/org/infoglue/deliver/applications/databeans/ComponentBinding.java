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

package org.infoglue.deliver.applications.databeans;

/**
 * 
 */

public class ComponentBinding
{
	private Integer id;
	private Integer componentId;
	private String entityClass;
	private String entityId;
	private String assetKey;
	private String bindingPath;

	public Integer getId()
	{
		return this.id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public Integer getComponentId()
	{
		return componentId;
	}

	public String getEntityClass()
	{
		return entityClass;
	}

	public String getEntityId()
	{
		return entityId;
	}

	public void setComponentId(Integer integer)
	{
		componentId = integer;
	}

	public void setEntityClass(String string)
	{
		entityClass = string;
	}

	public void setEntityId(String integer)
	{
		entityId = integer;
	}

	public String getBindingPath()
	{
		return bindingPath;
	}

	public void setBindingPath(String string)
	{
		bindingPath = string;
	}

	public String getAssetKey()
	{
		return assetKey;
	}

	public void setAssetKey(String assetKey)
	{
		this.assetKey = assetKey;
	}
}