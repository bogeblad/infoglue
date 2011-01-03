/* ===============================================================================
*
* Part of the InfoGlue SiteNode Management Platform (www.infoglue.org)
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
package org.infoglue.deliver.taglib.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.infoglue.cms.controllers.kernel.impl.simple.FormEntryController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.management.FormEntryVO;
import org.infoglue.cms.entities.management.FormEntryValueVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.util.XMLHelper;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.deliver.applications.databeans.FormStatisticsBean;
import org.infoglue.deliver.applications.databeans.FormStatisticsOptionBean;
import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
import org.infoglue.deliver.taglib.AbstractTag;
import org.infoglue.deliver.taglib.TemplateControllerTag;
import org.infoglue.deliver.taglib.common.GetCookieTag;

public class FormStatisticsTag extends TemplateControllerTag 
{
	/**
	 * The universal version identifier.
	 */
	private static final long serialVersionUID = 4482006814634520239L;

    private final static Logger logger = Logger.getLogger(FormStatisticsTag.class.getName());

	private Integer formContentId = null;
	private String type = "percentage"; //average, numeric or percentage
	private String fieldName = null;
	private String optionsPropertyName = "Options";
	
	/**
	 * Adds a parameter with the specified name and value to the parameters
	 * of the parent tag.
	 * 
	 * @return indication of whether to continue evaluating the JSP page.
	 * @throws JspException if an error occurred while processing this tag.
	 */
	public int doEndTag() throws JspException
    {
		//Disabling page cache as the statistics are mostly useful otherwise.
		getController().getDeliveryContext().setDisablePageCache(true);
		
		if(formContentId == null || formContentId.equals("-1"))
			 throw new JspException("Wrong input");
		
		FormStatisticsBean formStatisticsBean = new FormStatisticsBean();
		try
		{
			ContentVO formContentVO = getController().getContent(formContentId);
			if(formContentVO != null)
			{
				LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForRepository(getController().getDatabase(), formContentVO.getRepositoryId());
				Collection formEntryValueVOList = FormEntryController.getController().getFormEntryValueVOList(formContentId, fieldName);
	
				String componentStructure = getController().getContentAttribute(formContentId, masterLanguageVO.getId(), "ComponentStructure", true);
				DOMBuilder domBuilder = new DOMBuilder();
				Document document = domBuilder.getDocument(componentStructure);
				
				String path = "";
				
				Map defaultOptionBeans = new HashMap();
				//document.selectSingleNode("/components//component/properties[property[@name = 'Field name'][@path_sv='food']]/property[@name='Options']/@path_sv");
				Element optionsParameter = (Element)document.selectSingleNode("/components//component/properties[property[@name = 'Field name'][@path_sv='" + fieldName + "']]/property[@name='" + optionsPropertyName + "']");
				if(optionsParameter != null)
				{
					path = optionsParameter.attributeValue("path_" + getController().getLanguage(getController().getLanguageId()).getLanguageCode());
					if(path == null || path.equals(""))
						path = optionsParameter.attributeValue("path_" + masterLanguageVO.getLanguageCode());
					if(path == null || path.equals(""))
						path = optionsParameter.attributeValue("path");
					
					String[] optionPairs = path.split("igbr");
					for(int i=0; i<optionPairs.length; i++)
					{
						String optionPair = optionPairs[i];
						String[] nameValuePair = optionPair.split("=");
						if(nameValuePair.length == 2)
						{
							String name = nameValuePair[0];
							String value = nameValuePair[1];
							FormStatisticsOptionBean formStatisticsOptionBean = new FormStatisticsOptionBean(name, value, 0, 0F);
							defaultOptionBeans.put(name, formStatisticsOptionBean);
						}
					}
				}
	
				formStatisticsBean.getFormEntryValueVOList().addAll(formEntryValueVOList);
	
				Integer totalEntries = formEntryValueVOList.size();
				
				Float totalValue = 0F;
				
				Map<String,Object> statisticsMap = new HashMap<String,Object>();
				statisticsMap.put("totalEntries", totalEntries);
				
				Iterator formEntryValueVOListIterator = formEntryValueVOList.iterator();
				while(formEntryValueVOListIterator.hasNext())
				{
					FormEntryValueVO formEntryValueVO = (FormEntryValueVO)formEntryValueVOListIterator.next();
					
					if(type.equals("percentage") || type.equals("numeric"))
					{
						Integer count = (Integer)statisticsMap.get("" + formEntryValueVO.getValue());
						if(count == null)
							statisticsMap.put("" + formEntryValueVO.getValue(), 1);
						else
							statisticsMap.put("" + formEntryValueVO.getValue(), count + 1);
					}
					else if(type.equals("average"))
					{
						try
						{
							totalValue = totalValue + Integer.getInteger(formEntryValueVO.getValue());
						}
						catch (Exception e) 
						{
							logger.warn("Not a valid number:" + e.getMessage());
						}
					}
				}
				
				Map<String,Object> statisticsResultMap = new HashMap<String,Object>();
				statisticsResultMap.putAll(statisticsMap);
				if(type.equals("percentage") || type.equals("numeric"))
				{
					Iterator statisticsMapIterator = statisticsMap.keySet().iterator();
					while(statisticsMapIterator.hasNext())
					{
						String key = (String)statisticsMapIterator.next();
						Integer totalCount = (Integer)statisticsMap.get(key);
						statisticsResultMap.put(key + "_totalCount", totalCount);
						statisticsResultMap.put(key + "_percentage", (totalCount / totalEntries) * 100);
						
						String[] optionPairs = path.split("igbr");
						for(int i=0; i<optionPairs.length; i++)
						{
							String optionPair = optionPairs[i];
							String[] nameValuePair = optionPair.split("=");
							if(nameValuePair.length == 2)
							{
								String name = nameValuePair[0];
								String value = nameValuePair[1];
								if(name.equalsIgnoreCase(key))
								{
									FormStatisticsOptionBean formStatisticsOptionBean = new FormStatisticsOptionBean(name, value, totalCount, ((float)totalCount / totalEntries) * 100);
									formStatisticsBean.getFormStatisticsOptionBeanList().add(formStatisticsOptionBean);
									defaultOptionBeans.remove(name);
								}
							}
						}
					}
					
					formStatisticsBean.getFormStatisticsOptionBeanList().addAll(defaultOptionBeans.values());
					
					produceResult(formStatisticsBean);
				}
				else if(type.equals("average"))
				{
					if(totalValue > 0 && totalEntries > 0)
						statisticsResultMap.put("percentage", (totalValue / totalEntries) * 100);
						
					produceResult(formStatisticsBean);
				}
			}
			else
			{
				produceResult("No form content found with id:" + formContentId);
			}
		}
		catch (Exception e) 
		{
			logger.warn("Error getting statistics:" + e.getMessage(), e);
		}
		
		formContentId = null;
		type = "percentage";
		fieldName = null;
		optionsPropertyName = "Options";
		
		return EVAL_PAGE;
    }
	
	
	/**
	 * Sets the name attribute.
	 * 
	 * @param name the form content id to use.
	 * @throws JspException if an error occurs while evaluating form content id parameter.
	 */
	public void setFormContentId(final String formContentId) throws JspException
	{
		this.formContentId = evaluateInteger("FormStatisticsTag", "formContentId", formContentId);
	}

	/**
	 * Sets the type attribute.
	 * 
	 * @param type What kind of statistics - percentage or numeric.
	 * @throws JspException if an error occurs while evaluating value parameter.
	 */
	public void setType(final String type) throws JspException
	{
		this.type = evaluateString("FormStatisticsTag", "type", type);
	}

	/**
	 * Sets the fieldName attribute.
	 * 
	 * @param fieldName which field do you want to get statistics for.
	 * @throws JspException if an error occurs while evaluating parameterName parameter.
	 */
	public void setFieldName(final String fieldName) throws JspException
	{
		this.fieldName = evaluateString("FormStatisticsTag", "fieldName", fieldName);
	}

	public void setOptionsPropertyName(final String optionsPropertyName) throws JspException
	{
		this.optionsPropertyName = evaluateString("FormStatisticsTag", "optionsPropertyName", optionsPropertyName);
	}

}
