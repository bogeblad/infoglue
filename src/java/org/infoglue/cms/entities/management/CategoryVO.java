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
 *
 * $Id: CategoryVO.java,v 1.6 2009/10/08 21:34:12 mattias Exp $
 */
package org.infoglue.cms.entities.management;

import java.util.ArrayList;
import java.util.List;

import org.infoglue.cms.entities.kernel.Persistent;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.DomainUtils;
import org.infoglue.cms.util.validators.ValidatorFactory;

/**
 * This is a Category to be used for categorizing Content
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class CategoryVO extends Persistent
{
	private Integer categoryId;
	private String name;
	private String displayName;
	private String description;
	private boolean active = true;
	private Integer parentId;
	private List children = new ArrayList();
	
	//Can be null if not set by user for extra info
	private String categoryPath = null;
	
	public CategoryVO() {}

	public CategoryVO(String name)
	{
		setName(name);
	}

	public CategoryVO(Integer id, String name)
	{
		this(name);
		setCategoryId(id);
	}

	public Integer getId()
	{
		return getCategoryId();
	}

	public Integer getCategoryId()
	{
		return categoryId;
	}

	public void setCategoryId(Integer i)
	{
		categoryId = i;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String s)
	{
		s = s.replaceAll(",", "");
		name = s;
	}

	public String getDisplayName()
	{
		return (displayName == null ? name : displayName);
	}

	public String getLocalizedDisplayName(String languageCode, String fallbackLanguageCode)
	{
    	String localizedName = getDisplayName();
    	
    	if(localizedName == null)
    		return "";
    	
    	String startSeparator = "=[";
    	String endSeparator = "],";
    	if(localizedName.indexOf("[") == -1)
    	{
    		startSeparator = "=";
    		endSeparator = ",";
    	}
    		
    	int startIndex = localizedName.indexOf(languageCode + startSeparator);
    	if(startIndex > -1)
    	{
    		int stopIndex = getDisplayName().indexOf(endSeparator, startIndex + languageCode.length() + startSeparator.length());
    		if(stopIndex > -1)
    			localizedName = getDisplayName().substring(startIndex + languageCode.length() + startSeparator.length(), stopIndex);
    		else
    			localizedName = getDisplayName().substring(startIndex + languageCode.length() +  + startSeparator.length());    			
    	}
    	else
    	{
    		startIndex = getDisplayName().indexOf(fallbackLanguageCode + startSeparator);
        	if(startIndex > -1)
        	{
        		int stopIndex = getDisplayName().indexOf(endSeparator, startIndex + fallbackLanguageCode.length() + startSeparator.length());
        		if(stopIndex > -1)
        			localizedName = getDisplayName().substring(startIndex + fallbackLanguageCode.length() + startSeparator.length(), stopIndex);
        		else
        			localizedName = getDisplayName().substring(startIndex + fallbackLanguageCode.length() + startSeparator.length());    			
        	}
    	}
    	
    	if(localizedName.indexOf(endSeparator) > -1)
    	{
    		localizedName = localizedName.substring(0, localizedName.indexOf(endSeparator));
    	}
    	else if(localizedName.indexOf("]") > -1)
    	{
    		localizedName = localizedName.substring(0, localizedName.indexOf("]"));
    	}
    	if(localizedName.indexOf(startSeparator) > -1)
		{
			localizedName = localizedName.substring(localizedName.indexOf(startSeparator) + startSeparator.length());
		}
    	
    	return localizedName;
	}

	public void setDisplayName(String s)
	{
		displayName = s;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String s)
	{
		description = s;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean b)
	{
		active = b;
	}

	public Integer getParentId()
	{
		return parentId;
	}

	public void setParentId(Integer i)
	{
		parentId = i;
	}

	public List getChildren()
	{
		return children;
	}

	public void setChildren(List l)
	{
		children = l;
	}

	public boolean isRoot()
	{
		return getParentId() == null;
	}

	public ConstraintExceptionBuffer validate()
	{
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		ValidatorFactory.createStringValidator("Category.name", true, 1, 100).validate(name, ceb);
		ValidatorFactory.createStringValidator("Category.description", false, 255).validate(description, ceb);
		return ceb;
	}

	public boolean equals(Object o)
	{
		if (super.equals(o))
		{
			CategoryVO category = (CategoryVO)o;
			return DomainUtils.equals(categoryId, category.categoryId)
					&& DomainUtils.equals(name, category.name)
					&& DomainUtils.equals(description, category.description)
					&& DomainUtils.equals(parentId, category.parentId)
					&& children.size() == category.children.size()
					&& (active == category.active);
		}

		return false;
	}

	public StringBuffer toStringBuffer()
	{
		StringBuffer sb = super.toStringBuffer();
		sb.append(" name=").append(name)
				.append(" description=").append(description)
				.append(" active=").append(active)
				.append(" parentId=").append(parentId)
				.append(" children.size=").append(children.size());
		return sb;
	}

	public String getCategoryPath()
	{
		return categoryPath;
	}

	public void setCategoryPath(String categoryPath)
	{
		this.categoryPath = categoryPath;
	}
}
