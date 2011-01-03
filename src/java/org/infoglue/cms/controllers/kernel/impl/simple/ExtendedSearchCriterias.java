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
package org.infoglue.cms.controllers.kernel.impl.simple;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;

/**
 * Criterias for the <code>ExtendedSearchController</code>.
 */
public class ExtendedSearchCriterias 
{
	/**
	 * Indicates that no date criteria should be used.
	 */
	public static final int NO_DATE_CRITERIA_TYPE   = 0;

	/**
	 * Indicates that the from date criteria should be used. 
	 */
	public static final int FROM_DATE_CRITERIA_TYPE = 1;

	/**
	 * Indicates that the to date criteria should be used. 
	 */
	public static final int TO_DATE_CRITERIA_TYPE   = 2;

	/**
	 * Indicates that the between date criteria should be used. 
	 */
	public static final int BOTH_DATE_CRITERIA_TYPE = 3;

	/**
	 * Indicates that no date criteria should be used.
	 */
	public static final int NO_EXPIRE_DATE_CRITERIA_TYPE   = 4;

	/**
	 * Indicates that the from date criteria should be used. 
	 */
	public static final int EXPIRE_FROM_DATE_CRITERIA_TYPE = 5;

	/**
	 * Indicates that the to date criteria should be used. 
	 */
	public static final int EXPIRE_TO_DATE_CRITERIA_TYPE   = 6;

	/**
	 * Indicates that the between date criteria should be used. 
	 */
	public static final int EXPIRE_BOTH_DATE_CRITERIA_TYPE = 7;

	/**
	 * Only fetch content versions having at least the present state.
	 */
	private Integer stateId;
	
	/**
	 * If set the search only allows exakt matches - forces stateIds to be similar
	 */
	private Integer forcedOperatingMode = null;
	
	/**
	 * If set to true the language parameter is not used at all during the search.
	 */
	private Boolean skipLanguageCheck = false;
	
	/**
	 * If present, only fetch content version that has at least one attribute fulfilling:
	 * (a) the attribute is present in the <code>xmlAttributes</code> list.
	 * (b) the value of the attribute contains the freetext value.
	 */
	private String freetext;
	
	/**
	 * The list of content version attributes to search in the freetext search.
	 */
	private List xmlAttributes; // type: <String>
	
	/**
	 * If present, only fetch content versions with this language. 
	 */
	private LanguageVO languageVO;
	
	/**
	 * If present, only fetch contents whose type is present in the list.
	 */
	private List contentTypeDefinitionVOs; // type: <ContentTypeDefinitionVO>
	
	/**
	 * If present, only fetch content versions fulfilling the category condition.
	 */
	private CategoryConditions categories;
	
	/**
	 * If present, only fetch contents published after this date.
	 */
	private Timestamp fromDate;
	
	/**
	 * If present, only fetch contents published before this date.
	 */
	private Timestamp toDate;

	/**
	 * If present, only fetch contents expiring after this date.
	 */
	private Timestamp expireFromDate;
	
	/**
	 * If present, only fetch contents expiring before this date.
	 */
	private Timestamp expireToDate;

	/**
	 * If present, only fetch content version last modified by this author.
	 */
	private String versionModifier;
	
	/**
	 * If present limit the number of results.
	 */
	private Integer maximumNumberOfItems;
	
	private List<Integer> repositoryIdList;
	
	/**
	 * Constructs a criteria object with the state critera set to <code>ContentVersionVO.WORKING_STATE</code>.
	 */
	public ExtendedSearchCriterias()
	{
		this(ContentVersionVO.WORKING_STATE.intValue());
	}
	
	/**
	 * Constructs a criteria object with the specified state critera.
	 * 
	 * @param stateId the stateId to use.
	 */
	public ExtendedSearchCriterias(final int stateId) 
	{
		super();
		this.stateId = new Integer(stateId);
	}

	/**
	 * Sets the forced state criteria. 
	 */
	public void setForcedOperatingMode(final Integer forcedOperatingMode)
	{
		this.forcedOperatingMode = forcedOperatingMode;
	}

	/**
	 * Sets the freetext critera. 
	 * Note that at least one attribute must be specified to enabled freetext search. 
	 * 
	 * @param freetext the freetext to use.
	 * @param xmlAttributes the list of attribute names to use.
	 */
	public void setFreetext(final String freetext, final List xmlAttributes)
	{
		this.freetext = freetext;
		if(xmlAttributes != null)
		{
			this.xmlAttributes = new ArrayList(xmlAttributes);
		}
	}

	/**
	 * Sets the language criteria.
	 * 
	 * @param languageVO the language to use.
	 */
	public void setLanguage(final LanguageVO languageVO)
	{
		this.languageVO = languageVO;
	}
	
	/**
	 * Sets the content type definition critiera.
	 * 
	 * @param contentTypeDefinitionVO the content type definition to use.
	 */
	public void setContentTypeDefinitions(final ContentTypeDefinitionVO contentTypeDefinitionVO)
	{
		if(contentTypeDefinitionVO != null)
		{
			contentTypeDefinitionVOs = new ArrayList();
			contentTypeDefinitionVOs.add(contentTypeDefinitionVO);
		}
	}
	
	/**
	 * Sets the content type definition critiera.
	 * 
	 * @param contentTypeDefinitionVOs the list of <code>ContentTypeDefinitionVO</code> to use.
	 */
	public void setContentTypeDefinitions(final List contentTypeDefinitionVOs)
	{
		if(contentTypeDefinitionVOs != null)
		{
			this.contentTypeDefinitionVOs = new ArrayList(contentTypeDefinitionVOs);
		}
	}
	
	/**
	 * Sets the category criteria.
	 * 
	 * @param categories the category condition to use.
	 */
	public void setCategoryConditions(final CategoryConditions categories)
	{
		this.categories = categories;
	}
	
	/**
	 * Sets the date critiera. 
	 * 
	 * @param from the from date to use (null is used to indicate an open end).
	 * @param to the to date to use (null is used to indicate an open end).
	 */
	public void setDates(final Date from, final Date to)
	{
		this.fromDate = (from == null) ? null : new Timestamp(from.getTime());
		this.toDate   = (to == null)   ? null : new Timestamp(to.getTime());
	}

	/**
	 * Sets the expire date critiera. 
	 * 
	 * @param from the expire from date to use (null is used to indicate an open end).
	 * @param to the to expire date to use (null is used to indicate an open end).
	 */
	public void setExpireDates(final Date expireFrom, final Date expireTo)
	{
		this.expireFromDate = (expireFrom == null) ? null : new Timestamp(expireFrom.getTime());
		this.expireToDate   = (expireTo == null)   ? null : new Timestamp(expireTo.getTime());
	}

	/**
	 * Sets the version modifier critiera. 
	 */
	public void setVersionModifier(final String versionModifier)
	{
		this.versionModifier = versionModifier;
	}

	/**
	 * Sets the maxNumberOfItems criteria.
	 * 
	 * @param languageVO the language to use.
	 */
	public void setMaximumNumberOfItems(final Integer maximumNumberOfItems)
	{
		this.maximumNumberOfItems = maximumNumberOfItems;
	}

	/**
	 * Sets the repository criteria. 
	 * 
	 * @param repositoryIdList limits the search to the repositories listed.
	 * @param to the to date to use (null is used to indicate an open end).
	 */
	public void setRepositoryIdList(final List<Integer> repositoryIdList)
	{
		this.repositoryIdList = repositoryIdList;
	}

	/**
	 * Returns true if the freetext criteria should be used; false otherwise.
	 * 
	 * @return true if the criteria should be used; false otherwise.
	 */
	public boolean hasFreetextCritera() 
	{
		return freetext != null && freetext.length() > 0 && xmlAttributes != null && !xmlAttributes.isEmpty();
	}

	/**
	 * Returns true if the language criteria should be used; false otherwise.
	 * 
	 * @return true if the criteria should be used; false otherwise.
	 */
	public boolean hasLanguageCriteria() 
	{
		return languageVO != null;
	}
	
	/**
	 * Returns true if the content type definition criteria should be used; false otherwise.
	 * 
	 * @return true if the criteria should be used; false otherwise.
	 */
	public boolean hasContentTypeDefinitionVOsCriteria() 
	{
		return contentTypeDefinitionVOs != null && !contentTypeDefinitionVOs.isEmpty();
	}
	
	/**
	 * Returns true if the category criteria should be used; false otherwise.
	 * 
	 * @return true if the criteria should be used; false otherwise.
	 */
	public boolean hasCategoryConditions() 
	{
		return categories != null && categories.hasCondition();
	}
	
	/**
	 * Returns the type of date critiera to use.
	 * 
	 * @return the type of date criteria to use.
	 */
	public int getDateCriteriaType()
	{
		if(toDate == null && fromDate == null)
		{
			return NO_DATE_CRITERIA_TYPE;
		}
		if(toDate != null && fromDate == null)
		{
			return TO_DATE_CRITERIA_TYPE;
		}
		if(toDate == null && fromDate != null)
		{
			return FROM_DATE_CRITERIA_TYPE;
		}
		return BOTH_DATE_CRITERIA_TYPE;
	}

	/**
	 * Returns the type of date critiera to use.
	 * 
	 * @return the type of date criteria to use.
	 */
	public int getExpireDateCriteriaType()
	{
		if(expireToDate == null && expireFromDate == null)
		{
			return NO_EXPIRE_DATE_CRITERIA_TYPE;
		}
		if(expireToDate != null && expireFromDate == null)
		{
			return EXPIRE_TO_DATE_CRITERIA_TYPE;
		}
		if(expireToDate == null && expireFromDate != null)
		{
			return EXPIRE_FROM_DATE_CRITERIA_TYPE;
		}
		return EXPIRE_BOTH_DATE_CRITERIA_TYPE;
	}

	/**
	 * Returns true if the version modifier criteria should be used; false otherwise.
	 * 
	 * @return true if the criteria should be used; false otherwise.
	 */
	public boolean hasVersionModifierCritera() 
	{
		return versionModifier != null;
	}

	/**
	 * Returns true if the limit criteria should be used; false otherwise.
	 * 
	 * @return true if the criteria should be used; false otherwise.
	 */
	public boolean hasMaximumNumberOfItemsCritera() 
	{
		return maximumNumberOfItems != null;
	}
	
	/**
	 * Returns the state to use in the state criteria. 
	 * 
	 * @return the state.
	 */
	public Integer getStateId()
	{
		return this.stateId;
	}
	
	public Object getForcedOperatingMode()
	{
		return this.forcedOperatingMode;
	}
	
	/**
	 * Returns the freetext to use in the freetext criteria. 
	 * 
	 * @return the freetext.
	 */
	public String getFreetext()
	{
		return this.freetext;
	}

	/**
	 * Returns the attributes to use in the freetext criteria. 
	 * 
	 * @return the list of attribute names.
	 */
	public List getXmlAttributes()
	{
		return this.xmlAttributes;
	}

	/**
	 * Returns the language to use in the language criteria. 
	 * 
	 * @return the language.
	 */
	public LanguageVO getLanguage()
	{
		return this.languageVO;
	}
	
	/**
	 * Returns the content type definitions to use in the content type definition criteria. 
	 * 
	 * @return the list of <code>ContentTypeDefinitionVO</code>:s.
	 */
	public List getContentTypeDefinitions()
	{
		return this.contentTypeDefinitionVOs;
	}

	/**
	 * Returns the category condition to use in the category condition criteria. 
	 * 
	 * @return the category condition.
	 */
	public CategoryConditions getCategories()
	{
		return this.categories;
	}

	/**
	 * Returns the from date to use in the date criteria. 
	 * 
	 * @return the from date.
	 */
	public Timestamp getFromDate()
	{
		return this.fromDate;
	}

	/**
	 * Returns the to date to use in the date criteria. 
	 * 
	 * @return the to date.
	 */
	public Timestamp getToDate()
	{
		return this.toDate;
	}

	/**
	 * Returns the from date to use in the date criteria. 
	 * 
	 * @return the from date.
	 */
	public Timestamp getExpireFromDate()
	{
		return this.expireFromDate;
	}

	/**
	 * Returns the to date to use in the date criteria. 
	 * 
	 * @return the to date.
	 */
	public Timestamp getExpireToDate()
	{
		return this.expireToDate;
	}

	/**
	 * Returns the to versionModifier to use in the date criteria. 
	 */
	public String getVersionModifier()
	{
		return this.versionModifier;
	}

	/**
	 * Returns the to maximumNumberOfItems to use in the date criteria. 
	 */
	public Integer getMaximumNumberOfItems()
	{
		return this.maximumNumberOfItems;
	}

	public Boolean getSkipLanguageCheck()
	{
		return skipLanguageCheck;
	}

	public void setSkipLanguageCheck(Boolean skipLanguageCheck)
	{
		this.skipLanguageCheck = skipLanguageCheck;
	}

	public List<Integer> getRepositoryIdList()
	{
		return this.repositoryIdList;
	}
}
