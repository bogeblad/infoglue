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

package org.infoglue.cms.entities.kernel;

import java.util.Collection;
import java.util.Iterator;

import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.validators.Constants;
import org.infoglue.cms.util.validators.ConstraintRule;
import org.infoglue.cms.util.validators.ConstraintRuleList;
import org.infoglue.cms.util.validators.EmailValidator;
import org.infoglue.cms.util.validators.StringValidator;

/**
 * ValitadeableEntity.java
 * Created on 2002-sep-16 
 * @author Stefan Sik, ss@frovi.com 
 * ss
 * 
 * Provides entityvaluobjects with a set of validation rules.
 * The rules can be retrieved and reviewed by action classes
 * with getConstraintRules.
 * 
 * 
 */

// Implement BaseEntityVO to get the interface

public abstract class ValidatableEntityVO implements BaseEntityVO
{
	// BaseEntityVO
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	// public abstract Integer getId();
	// end BaseEntityVO	

	protected ConstraintRuleList rules = new ConstraintRuleList();;
	
	public ConstraintRule getRule(String fieldName)
	{
		return rules.getRule(fieldName);
	}
	
	
	/**
	 * getConstraintRules
	 * returns a collection of ConstraintRule objects
	 * this is the collection returned by getConstraintRuleList().getRules()
	 */
	public Collection getConstraintRules()
	{
		return rules.getRules();	
	}

	/**
	 * getConstraintRuleList
	 * returns the ConstraintRuleList object
	 */	
	public ConstraintRuleList getConstraintRuleList()
	{
		return rules;
	}
	
	public abstract void PrepareValidation();
	
	/**
	 * @return ConstraintExceptionBuffer
	 */	
    public ConstraintExceptionBuffer validate()
    {
    	return validate(this);
    }
    
    public ConstraintExceptionBuffer validate(ValidatableEntityVO vo)
    {
    	// This method loops through the rulelist and creates
    	// validators according to the settings in each rule.
    	// The old validators are used to do the actual validation
    	// but I have changed them to use less constructor
    	// parameter passing in favour for setters.
    	    	
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		
		// Prepare the object for validation
		vo.PrepareValidation();
		
		// Loop through rules and create validators
    	Iterator iterator = vo.getConstraintRules().iterator();
    	while (iterator.hasNext()) 
    	{
    		ConstraintRule cr = (ConstraintRule) iterator.next();
    		Integer intId = vo.getId();

			// an ugly switch for now.    		
    		switch (cr.getConstraintType())
    		{
    			case Constants.EMAIL:
    			{
					if (cr.getValue() != null)
					{
						// Create validator
	    				EmailValidator v = new EmailValidator(cr.getFieldName());
	    				
	    				// Set properties
	    				v.setObjectClass(vo.getConstraintRuleList().getEntityClass());
	    				v.setRange(cr.getValidRange());
	    				v.setIsRequired(cr.required);
	    				v.setMustBeUnique(cr.unique);
	    				v.setExcludeId(intId);

						// Do the limbo
	    				v.validate((String) cr.getValue(), ceb);
	    				
	    				// <todo>
	    				// Note: the actual value validated should be extracted
	    				// from the vo using the fieldname with reflection.
	    				// </todo>
	    				
					}		 	    	 
    				break;
    			}
				case Constants.STRING:
    			{
					if (cr.getValue() != null)
					{    				
	    				StringValidator v = new StringValidator(cr.getFieldName());
	    				v.setObjectClass(vo.getConstraintRuleList().getEntityClass());
	    				v.setRange(cr.getValidRange());
	    				v.setIsRequired(cr.required);
	    				v.setMustBeUnique(cr.unique);
	    				v.setExcludeId(intId);

	    				v.validate((String) cr.getValue(), ceb);
					}		 	    	 
    				break;
    			}
    			case Constants.FLOAT:
    			{
    				break;
    			}
    			case Constants.INTEGER:
    			{
    				break;
    			}
    			case Constants.PROPERNOUN:
    			{
    				break;
    			}
    			
    		} // switch
    		    		
    	} // while
				
		return ceb;
    }
    	
    
}
