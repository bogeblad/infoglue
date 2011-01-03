package org.infoglue.cms.util.sorters;

import java.util.Comparator;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.deliver.controllers.kernel.impl.simple.BasicTemplateController;

/**
 * Sort on the content version modifyer
 *
 * @author Mattias Bogeblad
 */

public class ComponentDeploymentComparator implements Comparator
{
    private final static Logger logger = Logger.getLogger(ComponentDeploymentComparator.class.getName());

	private String sortProperty;
	private String sortOrder;
	public long extractTime = 0;

	public ComponentDeploymentComparator(String sortProperty, String sortOrder)
	{
		this.sortProperty = sortProperty;
		this.sortOrder = sortOrder;
	}

    public int compare(Object o1, Object o2)
	{		
		ContentVO contentVO1 = (ContentVO)o1;
		ContentVO contentVO2 = (ContentVO)o2;

		ContentVersionVO contentVersionVO1 = contentVO1.getContentVersion();
		ContentVersionVO contentVersionVO2 = contentVO2.getContentVersion();

		int result = 0;

		if(contentVersionVO1 != null && contentVersionVO2 != null)
		{
			Comparable valueOne = contentVersionVO1.getModifiedDateTime();
			Comparable valueTwo = contentVersionVO2.getModifiedDateTime();
								
			if(sortOrder.equalsIgnoreCase("desc"))
			    result = valueTwo.compareTo(valueOne);
			else
			    result = valueOne.compareTo(valueTwo);
		}
		else
			logger.error("A parameter was null... should not happen....:" + contentVO1 + ":" + contentVO2);
		
		return result;
	}
    
	private Comparable getProperty(Object o, String property)
	{
		try
		{
			Object propertyObject = PropertyUtils.getProperty(o, sortProperty);
			if(propertyObject instanceof String)
				return (Comparable)propertyObject.toString().toLowerCase();
			else
				return (Comparable)propertyObject;
		}
		catch (Exception e)
		{
			logger.info(getClass().getName() + " Error finding property " + property, e);
			return null;
		}
	}
}
