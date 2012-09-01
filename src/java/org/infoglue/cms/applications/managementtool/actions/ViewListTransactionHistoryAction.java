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

package org.infoglue.cms.applications.managementtool.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.TransactionHistoryController;


/**
 * 	Action class for usecase ViewListTransactionHistoryUCC 
 *
 *  @author Mattias Bogeblad
 */

public class ViewListTransactionHistoryAction extends InfoGlueAbstractAction 
{
	private static final long serialVersionUID = 1L;

	private String typeFilter = "";
	private Integer numberOfRows = new Integer(250);
	private Date filterStartDateTime = null;
	private Date filterEndDateTime = null;
	private String filterStartDateTimeString = null;
	private String filterEndDateTimeString = null;
	
	private List transactionHistoryVOList;
	
	/**
	 * This is the main execution method. Allows for a few filtering options to show only events / transactions of a certain kind.
	 */
	protected String doExecute() throws Exception 
	{
		/*
		String typeFilterFull = null;
		if(typeFilter != null && typeFilter.equals("auth"))
			typeFilterFull = "200, 201, 202";
		else if(typeFilter != null && typeFilter.equals("crud"))
			typeFilterFull = "0, 1, 2";
		*/
		List typeFilterFull = null;
		if(typeFilter != null && typeFilter.equals("auth"))
		{
			typeFilterFull = new ArrayList();
			typeFilterFull.add("200");
			typeFilterFull.add("201");
			typeFilterFull.add("202");
		}
		else if(typeFilter != null && typeFilter.equals("crud"))
		{
			typeFilterFull = new ArrayList();
			typeFilterFull.add("0");
			typeFilterFull.add("1");
			typeFilterFull.add("2");
		}
		else if(typeFilter != null && typeFilter.equals("publ"))
		{
			typeFilterFull = new ArrayList();
			typeFilterFull.add("300");
			typeFilterFull.add("301");
			typeFilterFull.add("302");
			typeFilterFull.add("303");
			typeFilterFull.add("305");
		}
		else if(typeFilter != null && typeFilter.equals("surv"))
		{
			typeFilterFull = new ArrayList();
			typeFilterFull.add("304");
			typeFilterFull.add("306");
		}
		
		
		//this.transactionHistoryVOList = TransactionHistoryController.getController().getTransactionHistoryVOList();
		this.transactionHistoryVOList = TransactionHistoryController.getController().getLatestTransactionHistoryVOListForEntity(null, null, typeFilterFull, filterStartDateTime, filterEndDateTime, numberOfRows.intValue());
		
	    //Collections.sort(this.transactionHistoryVOList, Collections.reverseOrder(new ReflectionComparator("id")));

	    return "success";
	}
	
	public List getTransactionHistories()
	{
		return this.transactionHistoryVOList;		
	}

	public String getTypeFilter()
	{
		return typeFilter;
	}

	public void setTypeFilter(String typeFilter)
	{
		this.typeFilter = typeFilter;
	}

	public Integer getNumberOfRows()
	{
		return numberOfRows;
	}

	public void setNumberOfRows(Integer numberOfRows)
	{
		if(numberOfRows != null)
			this.numberOfRows = numberOfRows;
	}

    public void setFilterStartDateTime(String filterStartDateTime)
    {
    	this.filterStartDateTimeString = filterStartDateTime;
    	if(filterStartDateTime != null && !filterStartDateTime.equals(""))
    		this.filterStartDateTime = new VisualFormatter().parseDate(filterStartDateTime, "yyyy-MM-dd HH:mm");
	}

    public void setFilterEndDateTime(String filterEndDateTime)
    {
    	this.filterEndDateTimeString = filterEndDateTime;
    	if(filterEndDateTime != null && !filterEndDateTime.equals(""))
    		this.filterEndDateTime = new VisualFormatter().parseDate(filterEndDateTime, "yyyy-MM-dd HH:mm");
	}

	public String getFilterStartDateTimeString()
	{
		return filterStartDateTimeString;
	}

	public String getFilterEndDateTimeString()
	{
		return filterEndDateTimeString;
	}
}
