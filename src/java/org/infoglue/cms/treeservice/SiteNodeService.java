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
import org.infoglue.cms.controllers.usecases.structuretool.ViewSiteNodeTreeUCC;
import org.infoglue.cms.controllers.usecases.structuretool.ViewSiteNodeTreeUCCFactory;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.net.CommunicationEnvelope;
import org.infoglue.cms.net.Node;
import org.infoglue.cms.security.InfoGlueAuthenticationFilter;
import org.infoglue.cms.security.InfoGluePrincipal;


public class SiteNodeService extends JServiceBuilder
{
    private final static Logger logger = Logger.getLogger(SiteNodeService.class.getName());

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
                responseEnvelope = getRootSiteNode(envelope);
            }
            if(action.equals("selectNode"))
            {
                responseEnvelope = getSiteNode(envelope);
            }
            else if(action.equals("selectChildNodes"))
            {
                responseEnvelope = getChildSiteNodes(envelope);
            }
            /*
            else if(action.equals("createSiteNode"))
            {
                responseEnvelope = createSiteNode(envelope);
            }
            else if(action.equals("updateSiteNode"))
            {
                responseEnvelope = updateSiteNode(envelope);
            }
            else if(action.equals("deleteSiteNode"))
            {
                responseEnvelope = deleteSiteNode(envelope);
            }
            */
            logger.info("Executing in SiteNodeService...");
        }
        catch (Exception e)
        {
            responseEnvelope.setStatus("1");
            e.printStackTrace();    
        }
        return responseEnvelope;
	}

    /**
     * This method fetches the root SiteNode of this site
     */
    public CommunicationEnvelope getRootSiteNode(CommunicationEnvelope envelope)
	{
        CommunicationEnvelope responseEnvelope = new CommunicationEnvelope();
	    
		try
        {  
         	List arguments = (List)envelope.getNodes();
        	Integer repositoryId = ((Node)arguments.get(0)).getId();
			logger.info("repositoryId:" + repositoryId);
            ViewSiteNodeTreeUCC viewSiteNodeTreeUCC = ViewSiteNodeTreeUCCFactory.newViewSiteNodeTreeUCC();
            SiteNodeVO siteNodeVO = viewSiteNodeTreeUCC.getRootSiteNode(repositoryId, getInfoGluePrincipal());
            logger.info("siteNodeVO:" + siteNodeVO.getSiteNodeId() + " " + siteNodeVO.getName());
            Node node = new Node();
            node.setId(siteNodeVO.getSiteNodeId());
            node.setName(siteNodeVO.getName());
            node.setIsBranch(siteNodeVO.getIsBranch());
            
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
     * This method fetches the SiteNode with the given id
     */
    public CommunicationEnvelope getSiteNode(CommunicationEnvelope envelope)
	{
        CommunicationEnvelope responseEnvelope = new CommunicationEnvelope();
	    
		try
        {  
        	List arguments = (List)envelope.getNodes();
        	Integer siteNodeId = ((Node)arguments.get(0)).getId();
			logger.info("siteNodeId:" + siteNodeId);
            ViewSiteNodeTreeUCC viewSiteNodeTreeUCC = ViewSiteNodeTreeUCCFactory.newViewSiteNodeTreeUCC();
            SiteNodeVO siteNodeVO = viewSiteNodeTreeUCC.getSiteNode(siteNodeId);
            logger.info("siteNodeVO:" + siteNodeVO.getSiteNodeId() + " " + siteNodeVO.getName());
            Node node = new Node();
            node.setId(siteNodeVO.getSiteNodeId());
            node.setName(siteNodeVO.getName());
            node.setIsBranch(siteNodeVO.getIsBranch());
            
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
     * This method fetches the root SiteNode of this site
     */
    public CommunicationEnvelope getChildSiteNodes(CommunicationEnvelope envelope)
	{
        CommunicationEnvelope responseEnvelope = new CommunicationEnvelope();
	    
		try
        {  
            List arguments = (List)envelope.getNodes();
        	Integer siteNodeId = ((Node)arguments.get(0)).getId();
			logger.info("siteNodeId:" + siteNodeId);
            
            ViewSiteNodeTreeUCC viewSiteNodeTreeUCC = ViewSiteNodeTreeUCCFactory.newViewSiteNodeTreeUCC();
            List childSiteNodes = viewSiteNodeTreeUCC.getSiteNodeChildren(siteNodeId);
			
			List nodes = new ArrayList();
			Iterator childIterator = childSiteNodes.iterator();
			
			while(childIterator.hasNext())
			{
				SiteNodeVO siteNodeVO = (SiteNodeVO)childIterator.next();
				Node node = new Node();
	            node.setId(siteNodeVO.getSiteNodeId());
	            node.setName(siteNodeVO.getName());
	            node.setIsBranch(siteNodeVO.getIsBranch());
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


	/**
	  * This method returns a logged in principal if existing.
	  */

	 public InfoGluePrincipal getInfoGluePrincipal()
	 {
		 return (InfoGluePrincipal)this.request.getSession().getAttribute(InfoGlueAuthenticationFilter.INFOGLUE_FILTER_USER);
	 }

}