/* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
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

package org.infoglue.deliver.applications.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.infoglue.deliver.applications.databeans.Slot;

public class InfoGlueComponent
{	
	public final static String PREPROCESSING_ORDER_PROPERTYNAME = "PreRenderOrder";
	public final static String CACHE_RESULT_PROPERTYNAME 		= "CacheResult";
	public final static String UPDATE_INTERVAL_PROPERTYNAME 	= "UpdateInterval";
	public final static String CACHE_KEY_PROPERTYNAME 			= "CacheKey";
	
	private Integer id							= null;
	private Integer contentId 					= null;
	private String name 	 					= null;
	private String slotName						= null;
	private String componentDivId				= null;
	private Slot containerSlot					= null;
	private boolean isInherited 				= false;
	private Integer pagePartTemplateContentId	= null;
	private Map properties     					= new HashMap();
	private Map tasks     						= new HashMap();
	private List slotList 						= new ArrayList();
	private List restrictions 					= new ArrayList();
	private Map slots 							= new HashMap();
	private Map components 						= new HashMap();
	private InfoGlueComponent parentComponent 	= null;
	private InfoGlueComponent pagePartTemplateComponent = null;
	private Integer positionInSlot				= null;

	private boolean cacheResult					= false;
	private int updateInterval					= -1;
	private String cacheKey						= null;
	private String preProcessingOrder			= "";
	
	private Map<String,Object> model			= new HashMap<String,Object>();
	public Map<String, Object> getModel() 
	{
		return model;
	}

	public InfoGlueComponent()
	{
	}
	
	public Map getComponents()
	{
		return this.components;
	}

	public Integer getContentId()
	{
		return this.contentId;
	}

	public String getName()
	{
		return this.name;
	}

	public void setComponents(Map components)
	{
		this.components = components;
	}

	public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Integer getId()
	{
		return this.id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public Map getSlots()
	{
		return this.slots;
	}

	public void setSlots(Map slots)
	{
		this.slots = slots;
	}
	
	public Map getProperties()
	{
		return this.properties;
	}
	
	/*
	public void setProperties(Map properties)
	{
		this.properties = properties;
	}
	*/
	
	public Map getTasks()
	{
		return this.tasks;
	}

	public void setTasks(Map tasks)
	{
		this.tasks = tasks;
	}

	public boolean getIsInherited()
	{
		return isInherited;
	}

	public void setIsInherited(boolean isInherited)
	{
		this.isInherited = isInherited;
	}

	public Integer getPagePartTemplateContentId()
	{
		return pagePartTemplateContentId;
	}

	public void setPagePartTemplateContentId(Integer pagePartTemplateContentId)
	{
		this.pagePartTemplateContentId = pagePartTemplateContentId;
	}

	public List getSlotList()
	{
		return slotList;
	}

	public void setSlotList(List list)
	{
		slotList = list;
	}

	public InfoGlueComponent getParentComponent()
	{
		return parentComponent;
	}

	public void setParentComponent(InfoGlueComponent component)
	{
		parentComponent = component;
	}

	public InfoGlueComponent getPagePartTemplateComponent()
	{
		return pagePartTemplateComponent;
	}

	public void setPagePartTemplateComponent(InfoGlueComponent pagePartTemplateComponent)
	{
		this.pagePartTemplateComponent = pagePartTemplateComponent;
	}

    public String getSlotName()
    {
        return slotName;
    }
    
    public void setSlotName(String slotName)
    {
        this.slotName = slotName;
    }
    
	public Slot getContainerSlot() 
	{
		return containerSlot;
	}

	public void setContainerSlot(Slot containerSlot) 
	{
		this.containerSlot = containerSlot;
	}
	
    public List getRestrictions()
    {
        return restrictions;
    }

	public Integer getPositionInSlot()
	{
		return positionInSlot;
	}

	public void setPositionInSlot(Integer positionInSlot)
	{
		this.positionInSlot = positionInSlot;
	}

	public Slot getSlot(String slotId)
	{
		Slot slot = null;
		Iterator<Slot> slotIterator = this.getSlotList().iterator();
		while(slotIterator.hasNext())
		{
			Slot candidateSlot = slotIterator.next();
			if(candidateSlot.getId().equalsIgnoreCase(slotId))
			{
				slot = candidateSlot;
				break;
			}
		}
		
		return slot;
	}

	public String getPreProcessingOrder()
	{
		return preProcessingOrder;
	}

	public void setPreProcessingOrder(String preProcessingOrder)
	{
		this.preProcessingOrder = preProcessingOrder;
	}

	public boolean getCacheResult()
	{
		return cacheResult;
	}

	public void setCacheResult(boolean cacheResult)
	{
		this.cacheResult = cacheResult;
	}

	public int getUpdateInterval()
	{
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval)
	{
		this.updateInterval = updateInterval;
	}

	public String getCacheKey()
	{
		return cacheKey;
	}

	public void setCacheKey(String cacheKey)
	{
		this.cacheKey = cacheKey;
	}
	
	public String getComponentDivId()
	{
		return componentDivId;
	}

	public void setComponentDivId(String componentDivId)
	{
		this.componentDivId = componentDivId;
	}

}