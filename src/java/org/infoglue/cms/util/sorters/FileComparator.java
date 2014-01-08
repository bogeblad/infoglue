package org.infoglue.cms.util.sorters;

import java.io.File;
import java.util.Comparator;

import org.apache.log4j.Logger;

/**
 * Sort on either file size or modified date
 *
 * @author Mattias Bogeblad
 */

public class FileComparator implements Comparator<Object>
{
    private final static Logger logger = Logger.getLogger(FileComparator.class.getName());

	private String sortProperty;

	public FileComparator(String prop)
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
			if(property.equals("lastModified"))
				return new Long(((File)o).lastModified());
			else if(property.equals("length"))
				return new Long(((File)o).length());
			else
				return (File)o;
		}
		catch (Exception e)
		{
			logger.warn(getClass().getName() + " Error finding property " + property, e);
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
