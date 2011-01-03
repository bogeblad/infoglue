/* ===============================================================================
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

package org.infoglue.cms.controllers.kernel.impl.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Node;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.mydesktop.ShortcutVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.dom.DOMBuilder;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * This controller fetches all shortcuts available. They can exist on personal, role or global level.
 * 
 * @author Mattias Bogeblad
 */

public class ShortcutController extends BaseController
{
    private final static Logger logger = Logger.getLogger(ShortcutController.class.getName());

	private static final ShortcutController controller = new ShortcutController();

	/**
	 * Returns the WorkflowController singleton
	 * @return a reference to a WorkflowController
	 */
	public static ShortcutController getController()
	{
		return controller;
	}

	private ShortcutController() {}

	/**
	 * Returns a list of all available shortcuts defined in the system including personal 
	 * @param userPrincipal a user principal
	 * @return a list ShortcutVOs representing available shortcuts
	 */
	public List getAvailableShortcutVOList(InfoGluePrincipal userPrincipal) throws SystemException
	{
	    List availableShortcutVOList = new ArrayList();
	     
	    try
	    {
	        Map args = new HashMap();
		    args.put("globalKey", "infoglue");
		    PropertySet propertySet = PropertySetManager.getInstance("jdbc", args);
	
		    String xml = getDataPropertyValue(propertySet, "serverNode_-1_shortcuts");
		    logger.info("xml:" + xml);
		    
			if(xml != null)
			{	
				DOMBuilder domBuilder = new DOMBuilder();
				
				Document document = domBuilder.getDocument(xml);
				
				List nodes = document.getRootElement().selectNodes("shortcut");
				logger.info("nodes:" + nodes.size());
				
				Iterator nodesIterator = nodes.iterator();
				while(nodesIterator.hasNext())
				{
				    Node node = (Node)nodesIterator.next();
				    logger.info("Node:" + node.asXML());
					
				    Node nameNode = node.selectSingleNode("name");
				    Node urlNode = node.selectSingleNode("url");
				    Node popupNode = node.selectSingleNode("popup");

				    String name = nameNode.getStringValue();
				    String url = urlNode.getStringValue();
				    String popup = popupNode.getStringValue();
				    
				    ShortcutVO shortcut = new ShortcutVO(name, url, Boolean.valueOf(popup).booleanValue());
				    
				    availableShortcutVOList.add(shortcut);}
			}
		    
	    }
	    catch(Exception e)
	    {
	        logger.error("An error occurred when reading shortcuts:" + e.getMessage(), e);
	    }
	    
		return availableShortcutVOList;
	}

	public String getDataPropertyValue(PropertySet propertySet, String key) throws Exception
	{
		byte[] valueBytes = propertySet.getData(key);
	    if(valueBytes != null)
	    	return new String(valueBytes, "utf-8");
	    else
	    	return null;
	}

	/**
	 * Returns a new WorkflowActionVO.  This method is apparently unused, but is required by BaseController.  We don't
	 * use it internally because it requires a cast; it is simpler to just use <code>new</code> to create an instance.
	 * @return a new WorkflowActionVO.
	 */
	public BaseEntityVO getNewVO()
	{
		return null;
	}
}
