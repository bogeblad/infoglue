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

package org.infoglue.cms.exception;

import java.util.ArrayList;
import java.util.List;

import org.infoglue.cms.applications.databeans.LinkBean;


/**
 * <p>Thrown to indicate that some business rule rule has been violated. Examples
 * include trying to assign non-unique values, illegal syntax, missing values for required
 * fields...</p>
 * <p>As there is a possibility that more than one rule is validated, ConstraintException
 * can be chained.</p>
 * <p>Note! This is not an internal exception </p>
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 * @author <a href="mailto:bogeblad@yahoo.com">Mattias Bogeblad</a>
 */
public class ConstraintException extends Exception 
{
    // Indicates the error type (basically a resource bundle key).
	private String errorCode;
  
	// The name of the (entity) field causing the exception.
	private String fieldName;
  
	//Extra info about the entity causing the exception.
	private String extraInformation = "";
  
	//Extra info about the entity causing the exception.
	private String result = null;
  
	// The next ConstraintException in the chain (may be null).
	private ConstraintException chainedException;

	private List<LinkBean> linkBeans = new ArrayList<LinkBean>();
	
	// --- [Static] --------------------------------------------------------------
	// --- [Constructors] --------------------------------------------------------

/**
   * Construct a ConstraintException with the specified field name and error code.
   *
   * @param fieldName the name of the (entity) field causing the exception.
   * @param errorCode indicates the error type.
   */
	public ConstraintException(String fieldName, String errorCode) 
  	{
		super();

	    // defensive, otherwise add null checks in equals()
	    this.fieldName = (fieldName == null) ? "" : fieldName;      
	    this.errorCode = (errorCode == null) ? "" : errorCode;      
	}

	/**
	 * Construct a ConstraintException with the specified field name and error code.
	 *
	 * @param fieldName the name of the (entity) field causing the exception.
	 * @param errorCode indicates the error type.
	 */
	public ConstraintException(String fieldName, String errorCode, String extraInformation) 
	{
		this(fieldName, errorCode, extraInformation, null);
	}

  /**
   * Construct a ConstraintException with the specified field name and error code.
   *
   * @param fieldName the name of the (entity) field causing the exception.
   * @param errorCode indicates the error type.
   */
	public ConstraintException(String fieldName, String errorCode, String extraInformation, String result) 
  	{
		super();

	    // defensive, otherwise add null checks in equals()
	    this.fieldName = (fieldName == null) ? "" : fieldName;      
	    this.errorCode = (errorCode == null) ? "" : errorCode;      
	    this.extraInformation = (extraInformation == null) ? "" : extraInformation;
	    this.result = (result == null) ? "" : result;
	}

	/**
	 *
	 */
	public ConstraintException(String fieldName, String errorCode, ConstraintException chainedException) 
	{
		this(fieldName, errorCode);
		this.chainedException = chainedException;
	}



  // --- [Public] --------------------------------------------------------------

  /**
   *
   */
  public void setChainedException(ConstraintException chainedException) {
    this.chainedException = chainedException;
  }

  /**
   *
   */
  public ConstraintException getChainedException() {
    return this.chainedException;
  }

  /**
   *
   */
  public String getFieldName() {
    return this.fieldName;
  }

  /**
   *
   */
  public String getErrorCode() {
    return this.errorCode;
  }

  /**
  *
  */
 public String getExtraInformation() {
   return this.extraInformation;
 }

 /**
 *
 */
 public String getResult() {
   return this.result;
 }

 /**
 *
 */
 public void setResult(String result) {
   this.result = result;
 }


  // --- [X implementation] ----------------------------------------------------
  // --- [java.lang.Exception Overrides] ---------------------------------------
  
  /**
   *
   */
  public String getMessage() {
    return "Constrain violated on field [" + this.fieldName + "], code [" + this.errorCode + "], extra [" + this.extraInformation + "]"; 
  }
  


  // --- [java.lang.Object Overrides] ------------------------------------------

  /**
   *
   */
  public String toString() {
    return "<ConstraintException>[" + getFieldName() + "," + getErrorCode() + "]";
  }

  /**
   * <p>Returns true if the specified object is a constraint exception with the same field name and
   * error code.</p>
   * <p>Note! Does not include equals check for any chained exceptions. To check for the, use
   * the ConstraintExceptionBuffer class.</p>
   *
   * @param o the reference object with which to compare.
   * @return true if this object is the same as the specified object; false otherwise.
   */
  public boolean equals(Object o) {
    if(o == null || !(o instanceof ConstraintException)) {   
      return false;
    }
    final ConstraintException e = (ConstraintException) o;
    return getFieldName().equals(e.getFieldName()) && getErrorCode().equals(e.getErrorCode());
  }

  /**
   * Note! It is generally necessary to override the hashCode method whenever the equals() method is overridden.
   * Returns a hash code value for the object.
   * 
   * @return a hash code value for this object.
   */
  public int hashCode() {
    return (getFieldName() + getErrorCode()).hashCode();
  }


	public List<LinkBean> getLinkBeans() 
	{
		return linkBeans;
	}

  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------
  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}