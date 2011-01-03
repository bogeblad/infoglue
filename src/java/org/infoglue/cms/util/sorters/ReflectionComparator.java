package org.infoglue.cms.util.sorters;

import java.util.Comparator;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

/**
 * Sort on a particular property, using reflection to find the value
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class ReflectionComparator implements Comparator
{
    private final static Logger logger = Logger.getLogger(ReflectionComparator.class.getName());

	private String sortProperty;

	public ReflectionComparator(String prop)
	{
		sortProperty = prop;
	}

	public int compare(Object o1, Object o2)
	{
		Comparable valueOne = getProperty(o1, sortProperty);
		Comparable valueTwo = getProperty(o2, sortProperty);
		return valueOne.compareTo(valueTwo);
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
			return new Comparable()
			{
				public int compareTo(Object o)
				{ 
					return 0; 
				}
			};
		}
	}
}
