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

import java.util.Locale;

import org.apache.log4j.Logger;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.util.StringManager;
import org.infoglue.cms.util.StringManagerFactory;

import webwork.action.CommandDriven;


/**
 *
 *
 * @author <a href="mailto:meat_for_the_butcher@yahoo.com">Patrik Nyborg</a>
 */
public abstract class AbstractFormAction extends AbstractAction implements CommandDriven 
{
    private final static Logger logger = Logger.getLogger(AbstractFormAction.class.getName());

  // --- [Constants] -----------------------------------------------------------
  // --- [Attributes] ----------------------------------------------------------

  //
  private Errors errors = new Errors();



  // --- [Static] --------------------------------------------------------------
  // --- [Constructors] --------------------------------------------------------
  // --- [Public] --------------------------------------------------------------

  /**
   *
   */
  public Errors getErrors() {
    return errors;
  }



  // --- [X Overrides] ---------------------------------------------------------
  // --- [webwork.action.Action implementation] --------------------------------

  /**
   *
   */
  public String execute() throws Exception {
    try {
      return super.execute();
    } catch(ConstraintException e) {
      setErrors(e);
      return INPUT;
    }
  }



  // --- [Package protected] ---------------------------------------------------
  // --- [Private] -------------------------------------------------------------

  /**
   *
   */
  private void setErrors(ConstraintException exception) {
    final Locale locale = getSession().getLocale();

    for(ConstraintException ce = exception; ce != null; ce = ce.getChainedException()) {
      final String fieldName             = ce.getFieldName();
      final String errorCode             = ce.getErrorCode();
      final String localizedErrorMessage = getLocalizedErrorMessage(locale, errorCode);

      getErrors().addError(fieldName, localizedErrorMessage);
    }
    logger.debug(getErrors().toString());
  }

  /**
   * <todo>Move to a ConstraintExceptionHelper class?</todo>
   */
  private String getLocalizedErrorMessage(Locale locale, String errorCode) {
    // <todo>fetch packagename from somewhere</todo>
    final StringManager stringManager = StringManagerFactory.getPresentationStringManager("org.infoglue.cms.entities", locale);

    // check if a specific error message exists - <todo/>
    // nah, use the general error message
    return stringManager.getString(errorCode);
  }



  // --- [Protected] -----------------------------------------------------------
  // --- [Inner classes] -------------------------------------------------------
}