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
public class HardcodedPageComparator implements Comparator
{
    private final static Logger logger = Logger.getLogger(HardcodedPageComparator.class.getName());

	private String sortProperty;
	private String sortOrder;
	private boolean numberOrder;
	private String nameProperty;
	private String namesInOrderString;
	
	private TemplateController templateController;

	public HardcodedPageComparator(String sortProperty, String sortOrder, boolean numberOrder, String nameProperty, String namesInOrderString, TemplateController templateController)
	{
		this.sortProperty = sortProperty;
		this.sortOrder = sortOrder;
		this.numberOrder = numberOrder;
		this.nameProperty = nameProperty;
		this.namesInOrderString = namesInOrderString;
		this.templateController = templateController;
	}

	public int compare(Object o1, Object o2)
	{
	    Comparable valueOne = getProperty(o1, sortProperty);
		Comparable valueTwo = getProperty(o2, sortProperty);
		
		Comparable valueOneName = getProperty(o1, nameProperty);
		Comparable valueTwoName = getProperty(o2, nameProperty);
		
		if(valueOne == null)
		{
		    WebPage webPage1 = (WebPage)o1;
		    WebPage webPage2 = (WebPage)o2;
		    
		    Integer meta1Id = webPage1.getMetaInfoContentId(); //this.templateController.getMetaInformationContentId(webPage1.getSiteNodeId());
		    Integer meta2Id = webPage2.getMetaInfoContentId(); //this.templateController.getMetaInformationContentId(webPage2.getSiteNodeId());

		    valueOne = this.templateController.getContentAttribute(meta1Id, this.templateController.getLanguageId(), sortProperty);
			valueTwo = this.templateController.getContentAttribute(meta2Id, this.templateController.getLanguageId(), sortProperty);

			if(valueOneName == null)
			{
			    valueOneName = this.templateController.getContentAttribute(meta1Id, this.templateController.getLanguageId(), nameProperty);
				valueTwoName = this.templateController.getContentAttribute(meta2Id, this.templateController.getLanguageId(), nameProperty);
			}
			
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

		if(after(valueOne, valueTwo, valueOneName, valueTwoName))
		    return 1;
		else
		    return -1;
	}

	private boolean after(Comparable valueOne, Comparable valueTwo, Comparable valueOneName, Comparable valueTwoName)
	{	    
	    int index1 = namesInOrderString.indexOf(valueOneName.toString());
	    int index2 = namesInOrderString.indexOf(valueTwoName.toString());
	    
	    if(index1 != -1 && index2 != -1)
	    {
	        if(index1 > index2)
	            return true;
	        else
	            return false;
	    }
	    else
	    {
	        if(index1 == -1 && index2 != -1)
	            return true;
	        else if(index2 == -1 && index1 != -1)
	            return false;
	        else
	        {
	            int result;
	    	    if(sortOrder.equalsIgnoreCase("desc"))
	    	    {  
	    	        if((valueOne != null && !valueOne.toString().equalsIgnoreCase("")) && (valueTwo == null || valueTwo.toString().equalsIgnoreCase("")))
	    	            result = -1;
	    		    if((valueTwo != null && !valueTwo.toString().equalsIgnoreCase("")) && (valueOne == null || valueOne.toString().equalsIgnoreCase("")))
	    		        result = 1;
	    	        
	    		    result = valueTwo.compareTo(valueOne);
	    	    }
	    	    else
	    		{
	    		    if((valueOne != null && !valueOne.toString().equalsIgnoreCase("")) && (valueTwo == null || valueTwo.toString().equalsIgnoreCase("")))
	    		        result = -1;
	    		    if((valueTwo != null && !valueTwo.toString().equalsIgnoreCase("")) && (valueOne == null || valueOne.toString().equalsIgnoreCase("")))
	    		        result = 1;
	    	        
	    		    result = valueOne.compareTo(valueTwo);
	    		}
	    	    
	    	    if(result > 0)
	    	        return true;
	    	    else
	    	        return false;	    	    
	        }
	    }
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
