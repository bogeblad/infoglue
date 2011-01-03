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

package org.infoglue.deliver.applications.databeans;


/**
 * @author mattias
 * This class represents a slot in a page component structure.
 * A slot can contain any number of components. 
 */

public class ComponentRestriction
{
	private String type;
	private String slotId;
	private String arguments;

    public String getArguments()
    {
        return arguments;
    }
    
    public void setArguments(String arguments)
    {
        this.arguments = arguments;
    }
    
    public String getSlotId()
    {
        return slotId;
    }
    
    public void setSlotId(String slotId)
    {
        this.slotId = slotId;
    }
    
    public String getType()
    {
        return type;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }
}
