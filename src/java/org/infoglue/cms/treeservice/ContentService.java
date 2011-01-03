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

package org.infoglue.cms.treeservice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.net.CommunicationEnvelope;
import org.infoglue.cms.net.Node;


public class ContentService extends JServiceBuilder
{
    private final static Logger logger = Logger.getLogger(ContentService.class.getName());

    public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
	}
	

	public CommunicationEnvelope execute(CommunicationEnvelope envelope)
	{
        CommunicationEnvelope responseEnvelope = new CommunicationEnvelope();
	    
		try
        {  
            String action = envelope.getAction();
            logger.info("ACTION:" + action);
            
            if(action.equals("selectRootNode"))
            {
                responseEnvelope = getRootContent(envelope);
            }
            if(action.equals("selectNode"))
            {
                responseEnvelope = getContent(envelope);
            }
            else if(action.equals("selectChildNodes"))
            {
                responseEnvelope = getChildContents(envelope);
            }
            /*
            else if(action.equals("createContent"))
            {
                responseEnvelope = createContent(envelope);
            }
            else if(action.equals("updateContent"))
            {
                responseEnvelope = updateContent(envelope);
            }
            else if(action.equals("deleteContent"))
            {
                responseEnvelope = deleteContent(envelope);
            }
            */
            logger.info("Executing in ContentService...");
        }
        catch (Exception e)
        {
            responseEnvelope.setStatus("1");
            e.printStackTrace();    
        }
        return responseEnvelope;
	}

    /**
     * This method fetches the root Content of this site
     */
    public CommunicationEnvelope getRootContent(CommunicationEnvelope envelope)
	{
        CommunicationEnvelope responseEnvelope = new CommunicationEnvelope();
	    
		try
        {  
        	List arguments = (List)envelope.getNodes();
        	logger.info("arguments:" + arguments.size());
        	Integer repositoryId = ((Node)arguments.get(0)).getId();
			logger.info("repositoryId:" + repositoryId);
            
            ContentVO contentVO = ContentControllerProxy.getController().getRootContentVO(repositoryId, this.request.getRemoteUser());
            
            logger.info("contentVO:" + contentVO.getContentId() + " " + contentVO.getName());
            Node node = new Node();
            node.setId(contentVO.getContentId());
            node.setName(contentVO.getName());
            node.setIsBranch(contentVO.getIsBranch());
            
            List nodes = new ArrayList();
            nodes.add(node);
            responseEnvelope.setNodes(nodes);
        }
        catch (Exception e)
        {
            responseEnvelope.setStatus("1");
            e.printStackTrace();    
        }
        return responseEnvelope;
	}

    /**
     * This method fetches the Content with the given id
     */
    public CommunicationEnvelope getContent(CommunicationEnvelope envelope)
	{
        CommunicationEnvelope responseEnvelope = new CommunicationEnvelope();
	    
		try
        {  
			List arguments = (List)envelope.getNodes();
        	Integer contentId = ((Node)arguments.get(0)).getId();
			logger.info("contentId:" + contentId);
            ContentVO contentVO = ContentController.getContentController().getContentVOWithId(contentId);
            logger.info("contentVO:" + contentVO.getContentId() + " " + contentVO.getName());
            Node node = new Node();
            node.setId(contentVO.getContentId());
            node.setName(contentVO.getName());
            node.setIsBranch(contentVO.getIsBranch());
            
            List nodes = new ArrayList();
            nodes.add(node);
            responseEnvelope.setNodes(nodes);
        }
        catch (Exception e)
        {
            responseEnvelope.setStatus("1");
            e.printStackTrace();    
        }
        return responseEnvelope;
	}


    /**
     * This method fetches the root Content of this site
     */
    public CommunicationEnvelope getChildContents(CommunicationEnvelope envelope)
	{
        CommunicationEnvelope responseEnvelope = new CommunicationEnvelope();
	    
		try
        {  
			List arguments = (List)envelope.getNodes();
        	Integer contentId = ((Node)arguments.get(0)).getId();
			logger.info("contentId:" + contentId);
            
            List childContents = ContentController.getContentController().getContentChildrenVOList(contentId, null, false);
			
			List nodes = new ArrayList();
			Iterator childIterator = childContents.iterator();
			
			while(childIterator.hasNext())
			{
				ContentVO contentVO = (ContentVO)childIterator.next();
				Node node = new Node();
	            node.setId(contentVO.getContentId());
	            node.setName(contentVO.getName());
	            node.setIsBranch(contentVO.getIsBranch());
	            nodes.add(node);
			}        
			 
            responseEnvelope.setNodes(nodes);
        }
        catch (Exception e)
        {
            responseEnvelope.setStatus("1");
            e.printStackTrace();    
        }
        return responseEnvelope;
	}


	

}