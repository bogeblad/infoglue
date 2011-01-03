package org.infoglue.cms.util.validators;

import org.apache.commons.validator.Field;
import org.apache.commons.validator.GenericTypeValidator;
import org.apache.commons.validator.GenericValidator;
import org.apache.commons.validator.Validator;
import org.apache.commons.validator.ValidatorException;
import org.apache.commons.validator.util.ValidatorUtils;

/**                                                       
 * Contains validation methods for different unit tests.
 */                                                       
public class CommonsValidator {
          
    /**
     * Throws a runtime exception if the value of the argument is "RUNTIME", 
     * an exception if the value of the argument is "CHECKED", and a 
     * ValidatorException otherwise.
     * 
     * @param value string which selects type of exception to generate
     * @throws RuntimeException with "RUNTIME-EXCEPTION as message" 
     * if value is "RUNTIME"
     * @throws Exception with "CHECKED-EXCEPTION" as message 
     * if value is "CHECKED"
     * @throws ValidatorException with "VALIDATOR-EXCEPTION" as message  
     * otherwise
     */
    public static boolean validateRaiseException(
        final Object bean,
        final Field field)
        throws Exception {
            
        final String value =
            ValidatorUtils.getValueAsString(bean, field.getProperty());
            
        if ("RUNTIME".equals(value)) {
            throw new RuntimeException("RUNTIME-EXCEPTION");
            
        } else if ("CHECKED".equals(value)) {
            throw new Exception("CHECKED-EXCEPTION");
            
        } else {
            throw new ValidatorException("VALIDATOR-EXCEPTION");
        }
    }
                                                          
   /**
    * Checks if the field is required.
    *
    * @param value The value validation is being performed on.
    * @return boolean If the field isn't <code>null</code> and 
    * has a length greater than zero, <code>true</code> is returned.  
    * Otherwise <code>false</code>.
    */
   public static boolean validateRequired(Object bean, Field field) {
      String value = ValidatorUtils.getValueAsString(bean, field.getProperty());
      return !GenericValidator.isBlankOrNull(value);
   }

   /**
    * Checks if the field can be successfully converted to a <code>byte</code>.
    *
    * @param 	value 		The value validation is being performed on.
    * @return	boolean		If the field can be successfully converted 
    *                           to a <code>byte</code> <code>true</code> is returned.  
    *                           Otherwise <code>false</code>.
    */
   public static boolean validateByte(Object bean, Field field) {
      String value = ValidatorUtils.getValueAsString(bean, field.getProperty());

      return GenericValidator.isByte(value);
   }

   /**
    * Checks if the field can be successfully converted to a <code>short</code>.
    *
    * @param 	value 		The value validation is being performed on.
    * @return	boolean		If the field can be successfully converted 
    *                           to a <code>short</code> <code>true</code> is returned.  
    *                           Otherwise <code>false</code>.
    */
   public static boolean validateShort(Object bean, Field field) {
      String value = ValidatorUtils.getValueAsString(bean, field.getProperty());

      return GenericValidator.isShort(value);
   }

   /**
    * Checks if the field can be successfully converted to a <code>int</code>.
    *
    * @param 	value 		The value validation is being performed on.
    * @return	boolean		If the field can be successfully converted 
    *                           to a <code>int</code> <code>true</code> is returned.  
    *                           Otherwise <code>false</code>.
    */
   public static boolean validateInt(Object bean, Field field) {
      String value = ValidatorUtils.getValueAsString(bean, field.getProperty());

      return GenericValidator.isInt(value);
   }

   /**
    * Checks if field is positive assuming it is an integer
    * 
    * @param    value       The value validation is being performed on.
    * @param    field       Description of the field to be evaluated
    * @return   boolean     If the integer field is greater than zero, returns
    *                        true, otherwise returns false.
    */
   public static boolean validatePositive(Object bean , Field field) {
      String value = ValidatorUtils.getValueAsString(bean, field.getProperty());
   
      return GenericTypeValidator.formatInt(value).intValue() > 0;                                                      
   }

   /**
    * Checks if the field can be successfully converted to a <code>long</code>.
    *
    * @param 	value 		The value validation is being performed on.
    * @return	boolean		If the field can be successfully converted 
    *                           to a <code>long</code> <code>true</code> is returned.  
    *                           Otherwise <code>false</code>.
    */
   public static boolean validateLong(Object bean, Field field) {
      String value = ValidatorUtils.getValueAsString(bean, field.getProperty());

      return GenericValidator.isLong(value);
   }

   /**
    * Checks if the field can be successfully converted to a <code>float</code>.
    *
    * @param 	value 		The value validation is being performed on.
    * @return	boolean		If the field can be successfully converted 
    *                           to a <code>float</code> <code>true</code> is returned.  
    *                           Otherwise <code>false</code>.
    */
   public static boolean validateFloat(Object bean, Field field) {
      String value = ValidatorUtils.getValueAsString(bean, field.getProperty());

      return GenericValidator.isFloat(value);
   }
   
   /**
    * Checks if the field can be successfully converted to a <code>double</code>.
    *
    * @param 	value 		The value validation is being performed on.
    * @return	boolean		If the field can be successfully converted 
    *                           to a <code>double</code> <code>true</code> is returned.  
    *                           Otherwise <code>false</code>.
    */
   public static boolean validateDouble(Object bean, Field field) {
      String value = ValidatorUtils.getValueAsString(bean, field.getProperty());

      return GenericValidator.isDouble(value);
   }

   /**
    * Checks if the field is an e-mail address.
    *
    * @param 	value 		The value validation is being performed on.
    * @return	boolean		If the field is an e-mail address
    *                           <code>true</code> is returned.  
    *                           Otherwise <code>false</code>.
    */
   public static boolean validateEmail(Object bean, Field field) {
      String value = ValidatorUtils.getValueAsString(bean, field.getProperty());

      return GenericValidator.isEmail(value);
   }

   /**
    * Checks if the field value matches a regexp.
    */
   public static boolean validateRegexp(Object bean, Field field) {
      String value = ValidatorUtils.getValueAsString(bean, field.getProperty());
      String regexp = field.getVarValue("regexp");
      
      //boolean valid = GenericValidator.matchRegexp(value, regexp);
      boolean valid = value.matches(regexp);
      return valid;
   }
   
  public final static String FIELD_TEST_NULL = "NULL";
  public final static String FIELD_TEST_NOTNULL = "NOTNULL";
  public final static String FIELD_TEST_EQUAL = "EQUAL";

  public static boolean validateRequiredIf(Object bean, Field field, Validator validator) 
  {
      final String value          = ValidatorUtils.getValueAsString(bean, field.getProperty());
      final String dependentValue = ValidatorUtils.getValueAsString(bean, field.getVarValue("dependent"));
      return dependentValue == null || dependentValue.length() == 0 || (value != null && value.length() > 0);  
  }
  
  private static Class stringClass = new String().getClass();

  private static boolean isString(Object o) {
    if (o == null) return true;
    return (stringClass.isInstance(o));
  }
      
}                                                         
