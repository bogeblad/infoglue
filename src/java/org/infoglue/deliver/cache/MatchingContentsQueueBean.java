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

package org.infoglue.deliver.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.jsp.JspException;

/**
 * This bean represents all the data needed in the publication queue.
 * Mainly it contains the url to the deliver instance (the bean is unique to each instance), the request parameters
 * and also the serialized parameters. 
 */
public class MatchingContentsQueueBean
{
	private String contentTypeDefinitionNames;
	private String categoryCondition;
	private String freeText;
	private String freeTextAttributeNames;
	private Date fromDate = null;
	private Date toDate = null;
	private String versionModifier;
	private Integer maximumNumberOfItems;
	private Date expireFromDate = null;
	private Date expireToDate = null;
	private String repositoryIds = null;
	private Integer languageId = null;
	private Boolean skipLanguageCheck = false;
	private Integer startNodeId;
	private String sortColumn = null;
	private String sortOrder = null;
	private String userName = null;

	private boolean cacheResult = true;
	private int cacheInterval = 1800;
	private String cacheName = null;
	private String cacheKey = null;
	private long lastFetched = -1;
	private Boolean scheduleFetch = false;
	private Integer scheduleInterval = 900;
	private Boolean validateAccessRightsAsAnonymous = false;

	/**
	 * @return the contentTypeDefinitionNames
	 */
	public String getContentTypeDefinitionNames() {
		return contentTypeDefinitionNames;
	}

	/**
	 * @param contentTypeDefinitionNames the contentTypeDefinitionNames to set
	 */
	public void setContentTypeDefinitionNames(String contentTypeDefinitionNames) {
		this.contentTypeDefinitionNames = contentTypeDefinitionNames;
	}

	/**
	 * @return the categoryCondition
	 */
	public String getCategoryCondition() {
		return categoryCondition;
	}

	/**
	 * @param categoryCondition the categoryCondition to set
	 */
	public void setCategoryCondition(String categoryCondition) {
		this.categoryCondition = categoryCondition;
	}

	/**
	 * @return the freeText
	 */
	public String getFreeText() {
		return freeText;
	}

	/**
	 * @param freeText the freeText to set
	 */
	public void setFreeText(String freeText) {
		this.freeText = freeText;
	}

	/**
	 * @return the freeTextAttributeNames
	 */
	public String getFreeTextAttributeNames() {
		return freeTextAttributeNames;
	}

	/**
	 * @param freeTextAttributeNames the freeTextAttributeNames to set
	 */
	public void setFreeTextAttributeNames(String freeTextAttributeNames) {
		this.freeTextAttributeNames = freeTextAttributeNames;
	}
	
	/**
	 * @param freeTextAttributeNames the freeTextAttributeNames to set
	 */
	public List getFreeTextAttributeNamesList() {
		List freeTextAttributeNamesList = null;
		if(freeTextAttributeNames != null && !freeTextAttributeNames.equals(""))
		{
			String[] freeTextAttributeNamesArray = freeTextAttributeNames.split(",");
			if(freeTextAttributeNamesArray.length > 0)
				freeTextAttributeNamesList = Arrays.asList(freeTextAttributeNamesArray);
		}
		
		return freeTextAttributeNamesList;
	}

	
	/**
	 * @return the fromDate
	 */
	public Date getFromDate() {
		return fromDate;
	}

	/**
	 * @param fromDate the fromDate to set
	 */
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	/**
	 * @return the toDate
	 */
	public Date getToDate() {
		return toDate;
	}

	/**
	 * @param toDate the toDate to set
	 */
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	/**
	 * @return the versionModifier
	 */
	public String getVersionModifier() {
		return versionModifier;
	}

	/**
	 * @param versionModifier the versionModifier to set
	 */
	public void setVersionModifier(String versionModifier) {
		this.versionModifier = versionModifier;
	}

	/**
	 * @return the maximumNumberOfItems
	 */
	public Integer getMaximumNumberOfItems() {
		return maximumNumberOfItems;
	}

	/**
	 * @param maximumNumberOfItems the maximumNumberOfItems to set
	 */
	public void setMaximumNumberOfItems(Integer maximumNumberOfItems) {
		this.maximumNumberOfItems = maximumNumberOfItems;
	}

	/**
	 * @return the expireFromDate
	 */
	public Date getExpireFromDate() {
		return expireFromDate;
	}

	/**
	 * @param expireFromDate the expireFromDate to set
	 */
	public void setExpireFromDate(Date expireFromDate) {
		this.expireFromDate = expireFromDate;
	}

	/**
	 * @return the expireToDate
	 */
	public Date getExpireToDate() {
		return expireToDate;
	}

	/**
	 * @param expireToDate the expireToDate to set
	 */
	public void setExpireToDate(Date expireToDate) {
		this.expireToDate = expireToDate;
	}

	/**
	 * @return the cacheResult
	 */
	public boolean isCacheResult() {
		return cacheResult;
	}

	/**
	 * @param cacheResult the cacheResult to set
	 */
	public void setCacheResult(boolean cacheResult) {
		this.cacheResult = cacheResult;
	}

	/**
	 * @return the cacheInterval
	 */
	public int getCacheInterval() {
		return cacheInterval;
	}

	/**
	 * @param cacheInterval the cacheInterval to set
	 */
	public void setCacheInterval(int cacheInterval) {
		this.cacheInterval = cacheInterval;
	}

	/**
	 * @return the cacheName
	 */
	public String getCacheName() {
		return cacheName;
	}

	/**
	 * @param cacheName the cacheName to set
	 */
	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	/**
	 * @return the cacheKey
	 */
	public String getCacheKey() {
		return cacheKey;
	}

	/**
	 * @param cacheKey the cacheKey to set
	 */
	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}

	/**
	 * @return the scheduleFetch
	 */
	public Boolean getScheduleFetch() {
		return scheduleFetch;
	}

	/**
	 * @param scheduleFetch the scheduleFetch to set
	 */
	public void setScheduleFetch(Boolean scheduleFetch) {
		this.scheduleFetch = scheduleFetch;
	}

	/**
	 * @return the scheduleInterval
	 */
	public Integer getScheduleInterval() {
		return scheduleInterval;
	}

	/**
	 * @param scheduleInterval the scheduleInterval to set
	 */
	public void setScheduleInterval(Integer scheduleInterval) {
		this.scheduleInterval = scheduleInterval;
	}

	/**
	 * @return the repositoryIds
	 */
	public String getRepositoryIds() {
		return repositoryIds;
	}

	/**
	 * @return the repositoryIds
	 */
	public List<Integer> getRepositoryIdsList() {
		List<Integer> repositoryIdList = null;
		if(repositoryIds != null && !repositoryIds.equals(""))
		{
			String[] repositoryIdsArray = repositoryIds.split(",");
			if(repositoryIdsArray.length > 0)
			{
				repositoryIdList = new ArrayList<Integer>();
				for(int i=0; i<repositoryIdsArray.length; i++)
				{
					repositoryIdList.add(new Integer(repositoryIdsArray[i]));
				}
			}
		}
		return repositoryIdList;
	}
	
	/**
	 * @param repositoryIds the repositoryIds to set
	 */
	public void setRepositoryIds(String repositoryIds) {
		this.repositoryIds = repositoryIds;
	}

	/**
	 * @return the languageId
	 */
	public Integer getLanguageId() {
		return languageId;
	}

	/**
	 * @param languageId the languageId to set
	 */
	public void setLanguageId(Integer languageId) {
		this.languageId = languageId;
	}

	/**
	 * @return the skipLanguageCheck
	 */
	public Boolean getSkipLanguageCheck() {
		return skipLanguageCheck;
	}

	/**
	 * @param skipLanguageCheck the skipLanguageCheck to set
	 */
	public void setSkipLanguageCheck(Boolean skipLanguageCheck) {
		this.skipLanguageCheck = skipLanguageCheck;
	}

	/**
	 * @return the startNodeId
	 */
	public Integer getStartNodeId() {
		return startNodeId;
	}

	/**
	 * @param startNodeId the startNodeId to set
	 */
	public void setStartNodeId(Integer startNodeId) {
		this.startNodeId = startNodeId;
	}
	
	/**
	 * @return the lastFetched
	 */
	public long getLastFetched() {
		return lastFetched;
	}

	/**
	 * @param lastFetched the lastFetched to set
	 */
	public void setLastFetched(long lastFetched) {
		this.lastFetched = lastFetched;
	}

	/**
	 * @return the sortColumn
	 */
	public String getSortColumn() {
		return sortColumn;
	}

	/**
	 * @return the sortOrder
	 */
	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortColumn(String sortColumn)
	{
		this.sortColumn = sortColumn;
	}

	public void setSortOrder(String sortOrder)
	{
		this.sortOrder = sortOrder;
	}

	/**
	 * @return the sortColumn
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @return the sortOrder
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the considerAccessRights
	 */
	public Boolean getValidateAccessRightsAsAnonymous() {
		return validateAccessRightsAsAnonymous;
	}

	/**
	 * @param considerAccessRights the considerAccessRights to set
	 */
	public void setValidateAccessRightsAsAnonymous(Boolean validateAccessRightsAsAnonymous) {
		this.validateAccessRightsAsAnonymous = validateAccessRightsAsAnonymous;
	}

}
