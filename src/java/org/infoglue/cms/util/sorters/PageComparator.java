package org.infoglue.cms.util.sorters;

import java.util.Comparator;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.infoglue.deliver.applications.databeans.WebPage;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;

/**
 * Sort on a particular property, using reflection to find the value
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class PageComparator implements Comparator
{
    private final static Logger logger = Logger.getLogger(PageComparator.class.getName());

	private String sortProperty;
	private String sortOrder;
	private boolean numberOrder;
	private TemplateController templateController;

	public PageComparator(String sortProperty, String sortOrder, boolean numberOrder, TemplateController templateController)
	{
		this.sortProperty = sortProperty;
		this.sortOrder = sortOrder;
		this.numberOrder = numberOrder;
		this.templateController = templateController;
	}

	public int compare(Object o1, Object o2)
	{
	    Comparable valueOne = getProperty(o1, sortProperty);
		Comparable valueTwo = getProperty(o2, sortProperty);
		
		if(valueOne == null)
		{
			WebPage webPage1 = (WebPage)o1;
			WebPage webPage2 = (WebPage)o2;
		    
		    Integer meta1Id = this.templateController.getMetaInformationContentId(webPage1.getSiteNodeId());
		    Integer meta2Id = this.templateController.getMetaInformationContentId(webPage2.getSiteNodeId());
		    
		    valueOne = this.templateController.getContentAttribute(meta1Id, this.templateController.getLanguageId(), sortProperty);
			valueTwo = this.templateController.getContentAttribute(meta2Id, this.templateController.getLanguageId(), sortProperty);
		
		    if(this.numberOrder)
		    {
		        try
		        {
		            if(valueOne != null && !valueOne.equals(""))
		                valueOne = (Comparable)new Long(valueOne.toString());
		            else
		            {
		                if(sortOrder.equalsIgnoreCase("desc"))
		                    valueOne = (Comparable)new Long(Long.MIN_VALUE);
		                else
		                    valueOne = (Comparable)new Long(Long.MAX_VALUE);
		            }
		        }
		        catch(Exception e)
		        {
		            logger.info("Not a number..." + e.getMessage());
		        }
		        
		        try
		        {
		            if(valueTwo != null && !valueTwo.equals(""))
		                valueTwo = (Comparable)new Long(valueTwo.toString());
		            else
		            {
		                if(sortOrder.equalsIgnoreCase("desc"))
		                    valueTwo = (Comparable)new Long(Long.MIN_VALUE);
		                else
		                    valueTwo = (Comparable)new Long(Long.MAX_VALUE);
		            }
		        }
		        catch(Exception e)
		        {
		            logger.info("Not a number..." + e.getMessage());
		        }
		    }
		}

	    if(sortOrder.equalsIgnoreCase("desc"))
	    {  
	        if((valueOne != null && !valueOne.toString().equalsIgnoreCase("")) && (valueTwo == null || valueTwo.toString().equalsIgnoreCase("")))
	            return -1;
		    if((valueTwo != null && !valueTwo.toString().equalsIgnoreCase("")) && (valueOne == null || valueOne.toString().equalsIgnoreCase("")))
	            return 1;
	        
	        return valueTwo.compareTo(valueOne);
	    }
	    else
		{
		    if((valueOne != null && !valueOne.toString().equalsIgnoreCase("")) && (valueTwo == null || valueTwo.toString().equalsIgnoreCase("")))
	            return -1;
		    if((valueTwo != null && !valueTwo.toString().equalsIgnoreCase("")) && (valueOne == null || valueOne.toString().equalsIgnoreCase("")))
	            return 1;
	        
		    return valueOne.compareTo(valueTwo);
		}
	}

	private Comparable getProperty(Object o, String property)
	{
		try
		{
			Object propertyObject = PropertyUtils.getProperty(o, sortProperty);
			if(propertyObject instanceof String)
			{
			    if(this.numberOrder)
			    {
			        try
			        {
			            return (Comparable)new Long(propertyObject.toString());
			        }
			        catch(Exception e)
			        {
			            logger.info("Not a number..." + e.getMessage());
			        }
			    }
			    
			    return (Comparable)propertyObject.toString().toLowerCase();
			}
			else
			{
				return (Comparable)propertyObject;
			}
		}
		catch (Exception e)
		{
			logger.info(getClass().getName() + " Error finding property " + property, e);
			return null;
		}
	}
}
