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

package org.infoglue.deliver.taglib.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.taglib.TemplateControllerTag;
import org.infoglue.deliver.util.SelectiveLivePublicationThread;
import org.infoglue.deliver.util.Timer;

public class MatchingContentsTag extends TemplateControllerTag 
{

    public final static Logger logger = Logger.getLogger(MatchingContentsTag.class.getName());

    private static final long serialVersionUID = 3833470599837135666L;
	
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
	
	private boolean cacheResult = true;
	private int cacheInterval = 1800;
	private String cacheName = null;
	private String cacheKey = null;
	private String repositoryIds = null;
	private Integer languageId = null;
	private Boolean skipLanguageCheck = false;
	
    public MatchingContentsTag()
    {
        super();
    }

	public int doEndTag() throws JspException
    {
		Timer t = new Timer();
		
		List freeTextAttributeNamesList = null;
		if(freeTextAttributeNames != null && !freeTextAttributeNames.equals(""))
		{
			String[] freeTextAttributeNamesArray = freeTextAttributeNames.split(",");
			if(freeTextAttributeNamesArray.length > 0)
				freeTextAttributeNamesList = Arrays.asList(freeTextAttributeNamesArray);
		}

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

		if(languageId == null)
			this.languageId = getController().getLanguageId();

		try
		{
			String maximumNumberOfItemsInMatchingContentsSearch = CmsPropertyHandler.getServerNodeProperty("maximumNumberOfItemsInMatchingContentsSearch", true, null);
			if(maximumNumberOfItemsInMatchingContentsSearch != null && !maximumNumberOfItemsInMatchingContentsSearch.equals("") && !maximumNumberOfItemsInMatchingContentsSearch.equals("-1"))
				this.maximumNumberOfItems = new Integer(maximumNumberOfItemsInMatchingContentsSearch);
		}
		catch (Exception e) 
		{
			logger.warn("Problem setting maximumNumberOfItemsInMatchingContentsSearch:" + e.getMessage());
		}
		
	    setResultAttribute(getController().getMatchingContents(contentTypeDefinitionNames, categoryCondition, freeText, freeTextAttributeNamesList, fromDate, toDate, expireFromDate, expireToDate, versionModifier, maximumNumberOfItems, true, cacheResult, cacheInterval, cacheName, cacheKey, repositoryIdList, this.languageId, skipLanguageCheck));
	    
	    this.contentTypeDefinitionNames = null;
	    this.categoryCondition = null;
	    this.freeText = null;
		this.freeTextAttributeNames = null;
		this.fromDate = null;
		this.toDate = null;
		this.versionModifier = null;
		this.maximumNumberOfItems = null;
		this.expireFromDate = null;
		this.expireToDate = null;
		
		this.cacheResult = true;
		this.cacheInterval = 1800;
		this.cacheName = null;
		this.cacheKey = null;
		this.repositoryIds = null;
		this.languageId = null;
		this.skipLanguageCheck = false;
		
	    long runningTime = t.getElapsedTime();
	    if(runningTime > 500)
	    	logger.warn("Running matching contents took:" + runningTime + " ms");
	    
	    return EVAL_PAGE;
    }

    public void setLanguageId(String languageId) throws JspException
    {
        this.languageId = evaluateInteger("matchingContentsTag", "languageId", languageId);
    }

    public void setSkipLanguageCheck(String skipLanguageCheck) throws JspException
    {
        this.skipLanguageCheck = (Boolean)evaluate("matchingContentsTag", "skipLanguageCheck", skipLanguageCheck, Boolean.class);
    }

    public void setContentTypeDefinitionNames(String contentTypeDefinitionNames) throws JspException
    {
        this.contentTypeDefinitionNames = evaluateString("matchingContentsTag", "contentTypeDefinitionNames", contentTypeDefinitionNames);
    }

    public void setCategoryCondition(String categoryCondition) throws JspException
    {
        this.categoryCondition = evaluateString("matchingContentsTag", "categoryCondition", categoryCondition);
    }

	public void setFreeText(String freeText) throws JspException
	{
		this.freeText = evaluateString("matchingContentsTag", "freeText", freeText);
	}

	public void setFreeTextAttributeNames(String freeTextAttributeNames) throws JspException
	{
		this.freeTextAttributeNames = evaluateString("matchingContentsTag", "freeTextAttributeNames", freeTextAttributeNames);
	}

	public void setFromDate(String fromDate) throws JspException
	{
		this.fromDate = (Date)evaluate("matchingContentsTag", "fromDate", fromDate, Date.class);
	}

	public void setToDate(String toDate) throws JspException
	{
		this.toDate = (Date)evaluate("matchingContentsTag", "toDate", toDate, Date.class);
	}

	public void setExpireFromDate(String expireFromDate) throws JspException
	{
		this.expireFromDate = (Date)evaluate("matchingContentsTag", "expireFromDate", expireFromDate, Date.class);
	}

	public void setExpireToDate(String expireToDate) throws JspException
	{
		this.expireToDate = (Date)evaluate("matchingContentsTag", "expireToDate", expireToDate, Date.class);
	}

	public void setVersionModifier(String versionModifier) throws JspException
	{
		this.versionModifier = evaluateString("matchingContentsTag", "versionModifier", versionModifier);
	}

	public void setMaximumNumberOfItems(String maximumNumberOfItems) throws JspException
	{
		this.maximumNumberOfItems = evaluateInteger("matchingContentsTag", "maximumNumberOfItems", maximumNumberOfItems);
	}

	public void setRepositoryIds(String repositoryIds) throws JspException
	{
		this.repositoryIds = evaluateString("matchingContentsTag", "repositoryIds", repositoryIds);;
	}

	public void setCacheInterval(int cacheInterval)
	{
		this.cacheInterval = cacheInterval;
	}

	public void setCacheKey(String cacheKey) throws JspException
	{
		this.cacheKey = evaluateString("matchingContentsTag", "cacheKey", cacheKey);
	}

	public void setCacheName(String cacheName) throws JspException
	{
		this.cacheName = evaluateString("matchingContentsTag", "cacheName", cacheName);;
	}

	public void setCacheResult(boolean cacheResult)
	{
		this.cacheResult = cacheResult;
	}

}
