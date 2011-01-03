package org.infoglue.cms.util.validators;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.validator.Field;
import org.apache.commons.validator.Msg;
import org.apache.commons.validator.Validator;
import org.apache.commons.validator.ValidatorResources;
import org.apache.commons.validator.ValidatorResult;
import org.apache.commons.validator.ValidatorResults;
import org.apache.log4j.Logger;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.security.SimplifiedFallbackJNDIBasicAuthorizationModule;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

public class ContentVersionValidator 
{
    private final static Logger logger = Logger.getLogger(ContentVersionValidator.class.getName());

	/**
	 * 
	 */
	public ContentVersionValidator() {}

	/**
	 * 
	 */
	public ConstraintExceptionBuffer validate(ContentTypeDefinitionVO contentType, ContentVersionVO contentVersionVO) {
		try {
			ContentVersionBean bean = new ContentVersionBean(contentType, contentVersionVO);
			ValidatorResources resources = loadResources(contentType);
			Validator validator = new Validator(resources, "requiredForm");
			validator.setOnlyReturnErrors(true);
			validator.setParameter(Validator.BEAN_PARAM, bean);
			ValidatorResults results = validator.validate();
			if(results.isEmpty())
				return new ConstraintExceptionBuffer();
			else
				return populateConstraintExceptionBuffer(results);
		} catch(Exception e) {
			return new ConstraintExceptionBuffer();
		}
	}

	/**
	 * 
	 */
	private static ConstraintExceptionBuffer populateConstraintExceptionBuffer(ValidatorResults results) {
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		Set s = results.getPropertyNames();
		for(Iterator i=s.iterator(); i.hasNext(); ) {
			ValidatorResult r = results.getValidatorResult((String) i.next());
			Field field       = r.getField();
			String name       = "ContentVersion" + "." + field.getKey();
			for(Iterator messages=field.getMessages().values().iterator(); messages.hasNext();) {
				Msg m = (Msg) messages.next();
				ceb.add(new ConstraintException(name, m.getKey()));
			}
		}
		return ceb;
	}

	/**
	 * 
	 */
    private ValidatorResources loadResources(ContentTypeDefinitionVO contentType) {
		try {
			InputStream is = readValidatorXML(contentType);
			return new ValidatorResources(is);
		} catch(Exception e) {
			logger.error("Error loading resource: " + e.getMessage());
		}
		return null;
    }
	
	/**
	 * TODO: remove - read from ContentTypeDefinition
	 */
	private InputStream readValidatorXML(ContentTypeDefinitionVO contentTypeDefinition) throws Exception
	{
		String xml = contentTypeDefinition.getSchemaValue();
		String validationSchema = xml.substring(xml.indexOf("<form-validation>"), xml.indexOf("</form-validation>") + 18);
		
		return new ByteArrayInputStream(validationSchema.getBytes("UTF-8"));		 
	}
}
