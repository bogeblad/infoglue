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

package org.infoglue.cms.util.validators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;



/**
 * ConstraintRuleList.java
 * Created on 2002-sep-16 
 * @author Stefan Sik, ss@frovi.com 
 * ss
 * 
 * This class holds a list of constraintrules for an entityVO
 * 
 */
public class ConstraintRuleList
{
    private final static Logger logger = Logger.getLogger(ConstraintRuleList.class.getName());

	private Collection rules = new ArrayList();
	private Class entityClass;
		
	/* 
	 * This method adds a constraint rule to the collection
	 * 
	 */
	public void addRule(ConstraintRule rule)
	{
		rules.add(rule);
	}
	
	/*
	 * This method retrieves all constraintrules
	 * 
	 */
	public Collection getRules()
	{
		return rules;
	}

	public ConstraintRule getRule(String fieldName)
	{
		ConstraintRule res = null;
		Iterator it = rules.iterator();
		while (it.hasNext())
		{
			ConstraintRule s = (ConstraintRule) it.next();
			if (s.getFieldName().compareTo(fieldName) ==0 )
			{
				res = s;
				break;
			}
		}
		
		return res;
	}
	
	public Class getEntityClass()
	{
		return entityClass;
	}

	public void setEntityClass(Class entityClass)
	{
		this.entityClass = entityClass;
		logger.info("RULELIST:: ENTITYCLASS : " + entityClass.getName());
	}

}
