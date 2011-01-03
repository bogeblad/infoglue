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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.cms.entities.management.FormEntryValueVO;
import org.infoglue.deliver.taglib.management.FormStatisticsTag;

/**
 * This class represents a databean for statistics around form entries
 */

public class FormStatisticsBean
{
    private final static Logger logger = Logger.getLogger(FormStatisticsBean.class.getName());

	//private Integer numberOfEntries;
	//private Integer 
	private List<FormStatisticsOptionBean> formStatisticsOptionBeanList = new ArrayList<FormStatisticsOptionBean>();
	private List<FormEntryValueVO> formEntryValueVOList = new ArrayList<FormEntryValueVO>();

	public Integer getTotalNumberOfEntries()
	{
		return formEntryValueVOList.size();
	}
	
	public Float getSum()
	{
		Float sum = 0F;
		Iterator formEntryValueVOListIterator = formEntryValueVOList.iterator();
		while(formEntryValueVOListIterator.hasNext())
		{
			FormEntryValueVO formEntryValueVO = (FormEntryValueVO)formEntryValueVOListIterator.next();
			
			try
			{
				String value = formEntryValueVO.getValue();
				sum = sum + Float.valueOf(value);
			}
			catch (Exception e) 
			{
				logger.warn("Not a valid number:" + e.getMessage());
			}
		}
		
		return sum;
	}

	public Float getAverage()
	{
		Float sum = getSum();
		
		return sum / getTotalNumberOfEntries();
	}

	public List<FormStatisticsOptionBean> getFormStatisticsOptionBeanList()
	{
		return formStatisticsOptionBeanList;
	}
	
	public List<FormEntryValueVO> getFormEntryValueVOList()
	{
		return formEntryValueVOList;
	}

}