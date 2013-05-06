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
	public ConstraintExceptionBuffer validate(ContentTypeDefinitionVO contentType, ContentVersionVO contentVersionVO, String languageCode) {
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
				return populateConstraintExceptionBuffer(results, contentVersionVO, languageCode);
		} catch(Exception e) {
			return new ConstraintExceptionBuffer();
		}
	}

	/**
	 * 
	 */
	private static ConstraintExceptionBuffer populateConstraintExceptionBuffer(ValidatorResults results, ContentVersionVO contentVersionVO, String languageCode) {
		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
		Set s = results.getPropertyNames();
		logger.info("s:" + s);
		for(Iterator i=s.iterator(); i.hasNext(); ) 
		{
			ValidatorResult r = results.getValidatorResult((String) i.next());
			logger.info("r:" + r);
			Field field       = r.getField();
			String name       = "ContentVersion" + "." + field.getKey();
			for(Iterator messages=field.getMessages().values().iterator(); messages.hasNext();) 
			{
				Msg m = (Msg) messages.next();
				logger.info("m:" + m);
			    String errorMessage = m.getKey();
			    logger.info("errorMessage: " + errorMessage);
			    if(languageCode != null && errorMessage != null && errorMessage.contains(languageCode + "="))
			    {
			    	String[] languageMessages = errorMessage.split(",");
			    	for(String message : languageMessages)
			    	{
			    		if(message.contains(languageCode + "="))
			    			errorMessage = message.substring(4, message.length() - 1);
			    	}
			    }
			    else if(errorMessage.contains("=["))
			    {
			    	String[] languageMessages = errorMessage.split(",");
			    	for(String message : languageMessages)
			    	{
			   			errorMessage = message.substring(4, message.length() - 1);
			   			break;
			    	}
			    }
			    
				ceb.add(new ConstraintException(name, errorMessage));
				//ceb.add(new ConstraintException(name, m.getKey()));
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
