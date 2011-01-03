package org.infoglue.cms.util.sorters;

import java.util.Comparator;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;

/**
 * Sort on a particular property, using reflection to find the value
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public class SiteNodeComparator implements Comparator
{
    private final static Logger logger = Logger.getLogger(SiteNodeComparator.class.getName());

	private String sortProperty;
	private String sortOrder;
	private TemplateController templateController;

	public SiteNodeComparator(String sortProperty, String sortOrder, TemplateController templateController)
	{
		this.sortProperty = sortProperty;
		this.sortOrder = sortOrder;
		this.templateController = templateController;
	}

	public int compare(Object o1, Object o2)
	{
		SiteNodeVO siteNodeVO1 = (SiteNodeVO)o1;
		SiteNodeVO siteNodeVO2 = (SiteNodeVO)o2;

		Comparable valueOne = (String)siteNodeVO1.getExtraProperties().get(sortProperty);
		Comparable valueTwo = (String)siteNodeVO2.getExtraProperties().get(sortProperty);
		
        long previousTime = System.currentTimeMillis();

		if(valueOne == null)
		{
	        valueOne = getProperty(o1, sortProperty);
			valueTwo = getProperty(o2, sortProperty);
		}

		if(valueOne == null && this.templateController != null)
		{
		    Integer meta1Id = this.templateController.getMetaInformationContentId(siteNodeVO1.getId());
		    Integer meta2Id = this.templateController.getMetaInformationContentId(siteNodeVO2.getId());
		
		    valueOne = this.templateController.getContentAttribute(meta1Id, this.templateController.getLanguageId(), sortProperty);
			valueTwo = this.templateController.getContentAttribute(meta2Id, this.templateController.getLanguageId(), sortProperty);
		}

		if(valueOne != null && valueTwo != null)
		{
			if(sortOrder.equalsIgnoreCase("desc"))
			    return valueTwo.compareTo(valueOne);
			else
			    return valueOne.compareTo(valueTwo);
		}
		else
			return 0;
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
