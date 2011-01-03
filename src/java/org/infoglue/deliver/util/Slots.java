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
package org.infoglue.deliver.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.infoglue.deliver.taglib.common.SlotsTag;

/**
 * 
 */
public class Slots 
{

    private final static Logger logger = Logger.getLogger(Slots.class.getName());

	/**
	 * The total valuespace. 
	 */
	private final List allElements;

	/**
	 * The index of the current slot.
	 */
	private int currentSlot;
	
	/**
	 * The maximum number of elements contained in a slot.
	 */
	private final int slotSize;
	
	/**
	 * The maximum number of visible slots.
	 */
	private final int slotCount;
	
	/**
	 * The maximum number of slots.
	 */
	private final int maxSlots;
	
	/**
	 * The elements in allElements contained in the current slot.
	 */
	private final List visibleElements;
	
	/**
	 * The indices of the visible slots.
	 */
	private final List visibleSlots; // list of Integer
	
	/**
	 * 
	 */
	public Slots(final List allElements, final int currentSlot, final int slotSize, final int slotCount) 
	{
		this.allElements     = (allElements == null) ? new ArrayList() : allElements;
		this.currentSlot     = currentSlot;
		this.slotSize        = slotSize;
		this.slotCount       = slotCount;
		this.maxSlots        = calculateMaxSlots(this.allElements.size());
		validateArguments();
		this.visibleElements = calculateVisibleElements();
		this.visibleSlots    = calculateVisibleSlots();
	}
	
	/**
	 * 
	 */
	public Slots(final int currentSlot, final int slotSize, final int slotCount, final int maxSlots) 
	{
		this.allElements     = null;
		this.currentSlot     = currentSlot;
		this.slotSize        = slotSize;
		this.slotCount       = slotCount;
		this.maxSlots        = maxSlots;
		validateArguments();
		this.visibleElements = null;
		this.visibleSlots    = calculateVisibleSlots();
	}
	
	/**
	 * 
	 */
	public List getVisibleElements() 
	{
		return Collections.unmodifiableList(visibleElements);
	}
	
	/**
	 * 
	 */
	public List getVisibleSlots() 
	{
		return Collections.unmodifiableList(visibleSlots);
	}
	
	/**
	 * 
	 */
	public Integer getLastSlot() 
	{
		return new Integer(maxSlots);
	}

	/**
	 * 
	 */
	private void validateArguments() 
	{
		if(slotSize <= 0)
		{
			throw new IllegalArgumentException("Slot size must be a positive number.");
		}
		if(slotCount <= 0)
		{
			throw new IllegalArgumentException("Slot count must be a positive number.");
		}
		if(currentSlot <= 0)
		{
			throw new IllegalArgumentException("Current slot must be a positive number.");
		}
		if(currentSlot > maxSlots)
		{
			logger.warn("Current slot is not a valid slot [" + currentSlot + ">" + maxSlots + "]. Setting current slot to maxSlots");
			currentSlot = maxSlots;
		}
	}
	
	/**
	 *  
	 */
	private List calculateVisibleElements() 
	{
		return allElements.subList(getFromElementIndex(), getToElementIndex());
	}

	/**
	 * 
	 */
	private List calculateVisibleSlots() 
	{
		final List result = new ArrayList();
		final int start   = startSlot();
		final int end     = Math.min(start + slotCount - 1, maxSlots); 
		
		for(int i=start; i<=end; ++i)
		{
			result.add(new Integer(i));
		}
		return result;
	}

	/**
	 * 
	 */
	private int startSlot() 
	{
		if(slotCount >= maxSlots)
		{
			return 1;
		}
		return Math.max(1, currentSlot - ((slotCount - 1) / 2)); 	
	}
	
	/**
	 * 
	 */
	private int getFromElementIndex()
	{
		return (currentSlot - 1) * slotSize; 
	}
	
	/**
	 * 
	 */
	private int getToElementIndex()
	{
		return Math.min(getFromElementIndex() + slotSize, allElements.size()); 
	}
	
	/**
	 * 
	 */
	private int calculateMaxSlots(final int numberOfElements) 
	{
		if(numberOfElements == 0 || slotSize == 0)
		{
			return 0;
		}
		
		final int mod = numberOfElements / slotSize;
		final int div = numberOfElements % slotSize;
		return mod + (div == 0 ? 0 : 1); 
	}
}
