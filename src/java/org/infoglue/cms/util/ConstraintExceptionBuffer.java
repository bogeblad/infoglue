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

package org.infoglue.cms.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.infoglue.cms.exception.AccessConstraintException;
import org.infoglue.cms.exception.ConstraintException;


/**
 *
 * Any duplicate (ConstraintException.equals()) will be silently discarded.
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 */

public class ConstraintExceptionBuffer 
{
    // Note! The chainedException field for all exceptions is set to null.
    protected Set exceptions = new HashSet(); // type: <ConstraintException>


  /**
   * Constructs a ConstraintExceptionBuffer with no exceptions in it. 
   */
  public ConstraintExceptionBuffer() {}

  /**
   * Constructs a ConstraintExceptionBuffer containing the specified exception(s). 
   *
   * @param exception the initial exception(s) to add.
   */
  public ConstraintExceptionBuffer(ConstraintException exception) {
    add(exception);
  }



  // --- [Public] --------------------------------------------------------------

  /**
   * Returns true if this buffer contains no exceptions; false otherwise.
   *
   * @return true if this buffer contains no exceptions; false otherwise.
   */
  public final boolean isEmpty() {
    return this.exceptions.isEmpty();
  }

  /**
   * Adds the exceptions of the specified buffer to this buffer.
   * Any duplicate (ConstraintException.equals()) will be silently discarded.
   *
   * @param ceb the buffer to add from (empty buffers legal).
   */
  public final void add(ConstraintExceptionBuffer ceb) { 
    if(ceb != null) {
      this.exceptions.addAll(ceb.exceptions);
    }
  }

  /**
   * Adds the specified exception to the exceptions of this buffer.
   * Any duplicate (ConstraintException.equals()) will be silently discarded.
   *
   * @param exception the exception to add (chained exceptions legal).
   */
  public final void add(ConstraintException exception) {
    if(exception != null) {
      // set chained to null but don't mess with the parameter.
      final String fieldName = exception.getFieldName();
      final String errorCode = exception.getErrorCode();
		ConstraintException ce = new ConstraintException(fieldName, errorCode);
      this.exceptions.add(ce);
      add(exception.getChainedException());
    }
  }

  /**
   * Throws the root exception; if this buffer contains no exceptions, nothing happens.
   *
   * @throws org.infoglue.cms.exception.ConstraintException if this buffer contains any exceptions.
   */
  public void throwIfNotEmpty() throws AccessConstraintException, ConstraintException {
    if(!isEmpty()) {
      throw toConstraintException();
    }
  }

  /**
   * Converts the exceptions of this buffer to chained constraint exceptions.
   *
   * @return the root exception of the chain.
   */
  public ConstraintException toConstraintException() 
  {
  	ConstraintException rootException = null;
    for(Iterator iterator = this.exceptions.iterator(); iterator.hasNext(); ) 
    {
    	ConstraintException ce = (ConstraintException) iterator.next();
    	ce.setChainedException(rootException);
    	rootException = ce;
    }
    return rootException;
  }



  // --- [X implementation] ----------------------------------------------------
  // --- [java.lang.Object Overrides] ------------------------------------------

  /**
   *
   */
  public String toString() {
    final StringBuffer sb = new StringBuffer();

    sb.append("<ConstraintExceptionBuffer>[{ ");
    for(Iterator iterator = this.exceptions.iterator(); iterator.hasNext(); ) {
      sb.append(iterator.next() + (iterator.hasNext() ? ", " : ""));
    }    
    sb.append("}]");
    return sb.toString();
  }

  /**
   * Returns true if the specified object is a buffer containing exactly the same 
   * exceptions as this one.
   *
   * @param o the reference object with which to compare.
   * @return true if this object is the same as the specified object; false otherwise.
   */
  public boolean equals(Object o) {
    if(o == null || !(o instanceof ConstraintExceptionBuffer)) {   
      return false;
    }
    final ConstraintExceptionBuffer ceb = (ConstraintExceptionBuffer) o;
    return this.exceptions.equals(ceb.exceptions);
  }

  /**
   * Note! It is generally necessary to override the hashCode method whenever the equals() method is overridden.
   * Returns a hash code value for the object.
   * 
   * @return a hash code value for this object.
   */
  public int hashCode() {
    return this.exceptions.hashCode();
  }



  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------
  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}