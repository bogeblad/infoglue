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

package org.infoglue.cms.entities.management;


import java.util.Iterator;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.kernel.ValidatableEntityVO;
import org.infoglue.cms.entities.management.impl.simple.SystemUserImpl;
import org.infoglue.cms.util.ConstraintExceptionBuffer;
import org.infoglue.cms.util.validators.Constants;
import org.infoglue.cms.util.validators.ConstraintRule;
import org.infoglue.cms.util.validators.EmailValidator;
import org.infoglue.cms.util.validators.Range;
import org.infoglue.cms.util.validators.StringValidator;


public class SystemUserVO extends ValidatableEntityVO implements BaseEntityVO
{

	private long timeStamp = 0;
    private java.lang.String userName;
    private java.lang.String password;
    private java.lang.String firstName;
    private java.lang.String lastName;
    private java.lang.String email;
    
	/**
	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
	 */
	public Integer getId() 
	{
		return null;
	}

	public String toString()
	{  
		return getFirstName() + " " + getLastName();
	}
    
    public java.lang.String getUserName()
    {
        return this.userName;
    }
                
    public void setUserName(java.lang.String userName)
    {
        this.userName = userName;
    }
    
    public java.lang.String getPassword()
    {
        return this.password;
    }
                
    public void setPassword(java.lang.String password) 
    {
        this.password = password;
    }
    
    public java.lang.String getFirstName()
    {
        return this.firstName;
    }
                
    public void setFirstName(java.lang.String firstName) 
    {
        this.firstName = firstName;
    }
    
    public java.lang.String getLastName()
    {
        return this.lastName;
    }
                
    public void setLastName(java.lang.String lastName) 
    {
        this.lastName = lastName;
    }
    
    public java.lang.String getEmail()
    {
        return this.email;
    }
                
    public void setEmail(java.lang.String email) 
    {
        this.email = email;

    }

	public void PrepareValidation()
	{
		// Define the constraint rules for this valueobject
		// maybe this belongs in the setters of this object?.
		// then this method would be obsolete, and the validation
		// should be initiated through a controller from the
		// action class??.
		// -----------------------------------------
		
		// On the rulelist set the class that holds this vo, the class
		// that is known to castor. This is for unique validation and
		// if possible should not be set in the valueobject, but preferably
		// in the actual castor-entity class. (Im not to satisfied with this
		// construction).
		rules.setEntityClass(SystemUserImpl.class);
		
		// Create a new constraintrule, supply constraint type, and field that this rule
		// applies to.
 		ConstraintRule cr = new ConstraintRule(org.infoglue.cms.util.validators.Constants.STRING, "SystemUser.userName");
 		
 		// Set the constraints
 		cr.setValidRange(new Range(2, 60) );
 		cr.unique=true;	// public variabel will be changed to setter later
 		cr.required=true; // public variabel will be changed to setter later
 		cr.setValue(userName);
 		
 		// Add this rule to the rulelist
 		rules.addRule(cr);		

		// Set the rest of the rules
 		cr = new ConstraintRule(org.infoglue.cms.util.validators.Constants.STRING, "SystemUser.password");
 		cr.setValidRange(new Range(4, 15));
 		cr.required = true;
 		cr.setValue(password);
		rules.addRule(cr);
		
 		cr = new ConstraintRule(org.infoglue.cms.util.validators.Constants.STRING, "SystemUser.firstName");
 		cr.setValidRange(new Range(1, 30));
 		cr.required = true;
 		cr.setValue(firstName);
		rules.addRule(cr);
		
 		cr = new ConstraintRule(org.infoglue.cms.util.validators.Constants.STRING, "SystemUser.lastName");
 		cr.setValidRange(new Range(1, 30));
 		cr.required = true;
 		cr.setValue(lastName);
		rules.addRule(cr);

 		cr = new ConstraintRule(org.infoglue.cms.util.validators.Constants.EMAIL, "SystemUser.email");
 		cr.setValidRange(new Range(50));
 		cr.required = true;
 		cr.setValue(email);
		rules.addRule(cr);
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
			String userName = ((SystemUserVO)vo).getUserName();
			
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
						v.setExcludeId(null);
						v.setExcludeObject(userName);

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
						v.setExcludeId(null);
						v.setExcludeObject(userName);

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
