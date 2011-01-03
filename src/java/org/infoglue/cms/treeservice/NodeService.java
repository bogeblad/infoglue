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


public class NodeService //extends JServiceBuilder
{
/*
    public static final String DatabaseFile = "database.xml";
    public static final String MappingFile  = "mapping.xml";

    private Mapping  mapping;
    private JDO      jdo;

    
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
	    
	    try
	    {
	        mapping = new Mapping( getClass().getClassLoader() );
            mapping.loadMapping( getClass().getResource( MappingFile ) );

            jdo = new JDO();
            jdo.setConfiguration( getClass().getResource( DatabaseFile ).toString() );
            jdo.setDatabaseName( "maingate" );
        }
        catch(Exception e)
        {
            logger.info("Error trying to initialize db");
        }
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
                responseEnvelope = getRootNode(envelope);
            }
            else if(action.equals("selectNode"))
            {
            }
            else if(action.equals("createNode"))
            {
                responseEnvelope = createNode(envelope);
            }
            else if(action.equals("updateNode"))
            {
                responseEnvelope = updateNode(envelope);
            }
            else if(action.equals("deleteNode"))
            {
                responseEnvelope = deleteNode(envelope);
            }
            
            logger.info("Executing in NodeService...");
        }
        catch (Exception e)
        {
            responseEnvelope.setStatus(1);
            e.printStackTrace();    
        }
        return responseEnvelope;
	}

    public CommunicationEnvelope getRootNode(CommunicationEnvelope envelope)
	{
        CommunicationEnvelope responseEnvelope = new CommunicationEnvelope();
	    
		try
        {  
            Database db = jdo.getDatabase();
            
            db.begin();
            
            Node node = null;
            
            String oqlString = "SELECT n FROM Node n WHERE is_undefined(parent)";
            logger.info("oqlString:" + oqlString);
            OQLQuery oql = db.getOQLQuery(oqlString);
            logger.info("oql prepared");
            QueryResults results = oql.execute();
            logger.info("results fetched");
            if(results.hasMore())
                node = (Node)results.next();

            logger.info("Fetched a node:" + node);
            responseEnvelope.setData(node);
            
            db.commit();
        }
        catch (Exception e)
        {
            responseEnvelope.setStatus(1);
            e.printStackTrace();    
        }
        return responseEnvelope;
	}


    public CommunicationEnvelope updateNode(CommunicationEnvelope envelope)
	{
        CommunicationEnvelope responseEnvelope = new CommunicationEnvelope();
	    
		try
        {  
            Database db = jdo.getDatabase();
            
            db.begin();
            
            Node updatedNode = (Node)envelope.getData();
            logger.info("Node to update:" + updatedNode);
            
            //Kan man ändra så att den sparas direkt kanske?
            Node node = (Node)db.load(Node.class, updatedNode.getId());
            node.setChildren(updatedNode.getChildren());
            node.setName(updatedNode.getName());
            node.setParent(updatedNode.getParent());

            logger.info("Executing in NodeService...");
            responseEnvelope.setData(node);

            db.commit();
        }
        catch (Exception e)
        {
            responseEnvelope.setStatus(1);
            e.printStackTrace();    
        }
        return responseEnvelope;
	}


    public CommunicationEnvelope createNode(CommunicationEnvelope envelope)
	{
        CommunicationEnvelope responseEnvelope = new CommunicationEnvelope();
	    
		try
        {  
            Database db = jdo.getDatabase();
            
            db.begin();
            
            Node newNode = (Node)envelope.getData();
            logger.info("Node to create:" + newNode);
            
            //Kan man ändra så att den sparas direkt kanske?
            Node node = new Node();
            node.setName(newNode.getName());
            
            Node parentNode = (Node)db.load(Node.class, newNode.getParent().getId());
            node.setParent(parentNode);
            node.setChildren(newNode.getChildren());
            db.create(node);

            logger.info("Executing in NodeService...");
            responseEnvelope.setData(node);

            db.commit();
        }
        catch (Exception e)
        {
            responseEnvelope.setStatus(1);
            e.printStackTrace();    
        }
        return responseEnvelope;
	}
	
    public CommunicationEnvelope deleteNode(CommunicationEnvelope envelope)
	{
        CommunicationEnvelope responseEnvelope = new CommunicationEnvelope();
	    
		try
        {  
            Database db = jdo.getDatabase();
            
            db.begin();
            
            Node deleteNode = (Node)envelope.getData();
            Node parent = deleteNode.getParent();
            logger.info("Node to delete:" + deleteNode);
            
            //Kan man ändra så att den sparas direkt kanske?
            Node node = (Node)db.load(Node.class, deleteNode.getId());
            db.remove(node);

            logger.info("Executing in NodeService...");
            responseEnvelope.setData(parent);

            db.commit();
        }
        catch (Exception e)
        {
            responseEnvelope.setStatus(1);
            e.printStackTrace();    
        }
        return responseEnvelope;
	}
	
*/
}