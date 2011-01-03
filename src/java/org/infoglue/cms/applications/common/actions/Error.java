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

package org.infoglue.cms.applications.common.actions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;


/**
 *
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 */
public final class Error 
{
    private Throwable throwable;
    private Throwable cause;

    /**
     *
     */
    public Error(Throwable throwable, Throwable cause) 
    { 
        this.throwable = throwable;
        this.cause     = cause;
    }



  /**
   *
   */
  public String getName() {
    final String fullyQualifiedName = this.throwable.getClass().getName();
    final int index                 = (fullyQualifiedName.lastIndexOf(".") == -1) ? 0 : fullyQualifiedName.lastIndexOf(".") + 1;
    return fullyQualifiedName.substring(index);
  }

  /**
   *
   */
  public String getMessage() {
    return this.throwable.getMessage();
  }

  /**
   *
   */
  public String getStackTrace() {
    return getStackTrace(this.throwable);
  }

  /**
   *
   */
  public boolean hasCause() {
    return this.cause != null;
  }

  /**
   *
   */
  public String getCauseMessage() {
    return hasCause() ? this.cause.getMessage() : "";
  }

  /**
   *
   */
  public String getCauseStackTrace() {
    return hasCause() ? getStackTrace(this.cause) : "";
  }

  

  // --- [X implementation] ----------------------------------------------------
  // --- [X Overrides] ---------------------------------------------------------
  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------

  /**
   *
   */
  private String getStackTrace(Throwable throwable) {
    final StringBuffer sb    = new StringBuffer();
    final StringTokenizer st = new StringTokenizer(stackTraceToString(throwable), "\n");
    
    while(st.hasMoreTokens()) {
      sb.append(st.nextToken() + "<br/>");
    }
    return sb.toString();
  }

  /**
   *
   */
  private String stackTraceToString(Throwable throwable) {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw  = new PrintWriter(sw);
    
    throwable.printStackTrace(pw);
    return sw.getBuffer().toString();
  }



  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}