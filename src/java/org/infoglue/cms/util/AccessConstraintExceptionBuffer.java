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

import java.util.Iterator;

import org.infoglue.cms.exception.AccessConstraintException;


/**
 * An extension of the ConstraintExceptionBuffer to handle access control.
 */

public class AccessConstraintExceptionBuffer extends ConstraintExceptionBuffer
{
  /**
   * Constructs a ConstraintExceptionBuffer with no exceptions in it. 
   */
  public AccessConstraintExceptionBuffer() {}

  
  /**
   * Adds the specified exception to the exceptions of this buffer.
   * Any duplicate (ConstraintException.equals()) will be silently discarded.
   *
   * @param exception the exception to add (chained exceptions legal).
   */
  
  public final void add(AccessConstraintException exception) {
	if(exception != null) {
	  // set chained to null but don't mess with the parameter.
	  final String fieldName = exception.getFieldName();
	  final String errorCode = exception.getErrorCode();
	  AccessConstraintException ce = new AccessConstraintException(fieldName, errorCode);
	  this.exceptions.add(ce);
	  add(exception.getChainedException());
	}
  }
  
  /**
   * Throws the root exception; if this buffer contains no exceptions, nothing happens.
   *
   * @throws org.infoglue.cms.exception.ConstraintException if this buffer contains any exceptions.
   */
  public void throwIfNotEmpty() throws AccessConstraintException {
    if(!isEmpty()) {
      throw toAccessConstraintException();
    }
  }

  /**
   * Converts the exceptions of this buffer to chained constraint exceptions.
   *
   * @return the root exception of the chain.
   */
  public AccessConstraintException toAccessConstraintException() 
  {
	AccessConstraintException rootException = null;
  	try
  	{
	
    for(Iterator iterator = this.exceptions.iterator(); iterator.hasNext(); ) 
    {
		AccessConstraintException ce = (AccessConstraintException) iterator.next();
    	ce.setChainedException(rootException);
    	rootException = ce;
    }
  	}
  	catch(Exception e)
  	{
  		e.printStackTrace();
  	}
    return rootException;
  }
}