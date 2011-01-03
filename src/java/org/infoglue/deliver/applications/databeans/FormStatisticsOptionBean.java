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

import java.util.ArrayList;
import java.util.List;

import org.infoglue.cms.entities.management.FormEntryValueVO;

/**
 * This class represents a databean for statistics around form entries
 */

public class FormStatisticsOptionBean
{
	private String name = null;
	private String value = null;
	private Integer numberOfEntries = null;
	private Float percentage = null;
	
	public FormStatisticsOptionBean(String name, String value, Integer numberOfEntries, Float percentage)
	{
		this.name = name;
		this.value = value;
		this.numberOfEntries = numberOfEntries;
		this.percentage = percentage;
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public Integer getNumberOfEntries()
	{
		return numberOfEntries;
	}

	public void setNumberOfEntries(Integer numberOfEntries)
	{
		this.numberOfEntries = numberOfEntries;
	}

	public Float getPercentage()
	{
		return percentage;
	}

	public void setPercentage(Float percentage)
	{
		this.percentage = percentage;
	}

	
}