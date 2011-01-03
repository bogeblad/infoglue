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
 *
 * This interface are to be followed by each class that wants to be notified by deliver about what content and sitenodes are
 * used where when they are used. 
 */

public interface UsageListener
{
    public abstract void addUsedContent(String usedContent);

    public abstract void addUsedSiteNode(String usedSiteNode);

    public abstract void addUsedContentVersion(String usedContentVersion);

    public abstract void addUsedSiteNodeVersion(String usedSiteNodeVersion);

    public abstract String[] getAllUsedEntities();
}