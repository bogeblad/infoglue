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
 
package org.infoglue.cms.plugins.opentree;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.infoglue.cms.net.CommunicationEnvelope;
import org.infoglue.cms.net.Node;


public class Communicator
{
    private String serverAddress = null;
    private String entityName    = null;
    private Integer repositoryId = null;
    
    public Communicator(String serverAddress, String entityName, Integer repositoryId)
    {
        this.serverAddress = serverAddress;
        this.entityName = entityName;
        this.repositoryId = repositoryId;
    }
     
    public Node getRootNode() throws Exception
    {
        CommunicationEnvelope requestEnvelope = new CommunicationEnvelope();
        requestEnvelope.setAction("selectRootNode");
        
        List arguments = new ArrayList();
		Node n = new Node();
		n.setId(this.repositoryId);
        arguments.add(n);
        requestEnvelope.setNodes(arguments);
        
        CommunicationEnvelope responseEnvelope = callService(requestEnvelope);
        
        Node rootNode = null;
        int status = Integer.parseInt(responseEnvelope.getStatus());

        //System.out.println("status:" + status);
        
        if(status == 0)
        {
        	List nodes = responseEnvelope.getNodes();
        	//System.out.println("nodes:" + nodes);
            rootNode = (Node)nodes.get(0);   
            //System.out.println("Node:" + rootNode);
        }
        
        return rootNode;
    }

    public Node getNode(Integer nodeId)
    {
        CommunicationEnvelope requestEnvelope = new CommunicationEnvelope();
        requestEnvelope.setAction("selectNode");
        
		List arguments = new ArrayList();
		Node n = new Node();
		n.setId(nodeId);
        arguments.add(n);
        requestEnvelope.setNodes(arguments);
        
        CommunicationEnvelope responseEnvelope = callService(requestEnvelope);
        
        Node node = null;
        int status = Integer.parseInt(responseEnvelope.getStatus());
        
        if(status == 0)
        {
        	List nodes = responseEnvelope.getNodes();
        	//System.out.println("nodes:" + nodes);
            node = (Node)nodes.get(0);   
            //System.out.println("Node:" + node);
        }
        
        return node;
    }


    public List getChildNodeList(Integer parentId)
    {
        List childContents = null;
        CommunicationEnvelope requestEnvelope = new CommunicationEnvelope();
        requestEnvelope.setAction("selectChildNodes");

		List arguments = new ArrayList();
		Node n = new Node();
		n.setId(parentId);
        arguments.add(n);
        requestEnvelope.setNodes(arguments);
                
        CommunicationEnvelope responseEnvelope = callService(requestEnvelope);
        
        Node rootVO = null;
        int status = Integer.parseInt(responseEnvelope.getStatus());

        //System.out.println("status:" + status);
        
        if(status == 0)
        {
            childContents = responseEnvelope.getNodes();  
        }
        
        return childContents;
    }

    



    public CommunicationEnvelope callService(CommunicationEnvelope requestEnvelope)
    {
		PrintWriter outputToServlet     = null;
        BufferedInputStream inputFromServlet   = null;
        CommunicationEnvelope responseEnvelope = null;        
            
	    try
	    {     
			String url = serverAddress;
    			        	       
            try
            {
            	Hashtable hash = serializeEnvelope(requestEnvelope);
            	//System.out.println("Sending the envelope to the servlet...");
            	String response = postToUrl(serverAddress, hash);
	            //System.out.println("response:" + response);
	            
	            responseEnvelope = deserializeEnvelope(httpEncodedStringToHashtable(response));
	            //System.out.println("Status response:" + responseEnvelope.getStatus());
            }
            catch (Exception e)
            {
			    e.printStackTrace();    
            }

        }
	    catch (Exception e)
	    {
	        System.out.println(e.toString());
	    }
	    
	    return responseEnvelope;
    }

    
    /**
     * This method post information to an URL and returns a string.It throws
     * an exception if anything goes wrong.
     * (Works like most 'doPost' methods)
     * 
     * @param urlAddress The address of the URL you would like to post to.
     * @param inHash The parameters you would like to post to the URL.
     * @return The result of the postToUrl method as a string.
     * @exception java.lang.Exception
     */
    
    private String postToUrl(String urlAddress, Hashtable inHash) throws Exception
    {        
        URL url = new URL(urlAddress);
        URLConnection urlConn = url.openConnection();
        urlConn.setAllowUserInteraction(false); 
        urlConn.setDoOutput (true); 
        urlConn.setDoInput (true); 
        urlConn.setUseCaches (false); 
        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        PrintWriter printout = new PrintWriter(urlConn.getOutputStream(), true); 
        String argString = "";
        if(inHash != null)
        {
            argString = toEncodedString(inHash);
        }
        printout.print(argString);
        printout.flush();
        printout.close (); 
        InputStream inStream = null;
        inStream = urlConn.getInputStream();
        InputStreamReader inStreamReader = new InputStreamReader(inStream);
        BufferedReader buffer = new BufferedReader(inStreamReader);            
        StringBuffer strbuf = new StringBuffer();   
        String line; 
        while((line = buffer.readLine()) != null) 
        {
            strbuf.append(line); 
        }                                              
        String readData = strbuf.toString();   
        buffer.close();
        return readData;             
    }
  
  
    /**
	 * Encodes a hash table to an URL encoded string.
	 * 
	 * @param inHash The hash table you would like to encode
	 * @return A URL encoded string.
	 */
		
	private String toEncodedString(Hashtable inHash) throws Exception
	{
	    StringBuffer buffer = new StringBuffer();
	    Enumeration names = inHash.keys();
	    while(names.hasMoreElements())
	    {
	        String name = names.nextElement().toString();
	        String value = inHash.get(name).toString();
	        buffer.append(URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8"));
			if(names.hasMoreElements())
	        {
	            buffer.append("&");
	        }
	    }
	    return buffer.toString();
	}
	
	

	/**
	 * Converts a serialized hashtable in the url-encoded format to
	 * a Hashtable that one can use within the program. 
	 * A good technique when exchanging information between different
	 * applications.
	 * 
	 * @param encodedstrang The url-encoded string.
	 * @return A Hashtable.
	 */

	public Hashtable httpEncodedStringToHashtable(String encodedstrang) throws Exception
	{
	    Hashtable anropin = new Hashtable();
	    StringTokenizer andsplitter = new StringTokenizer(encodedstrang,"&");
	    while (andsplitter.hasMoreTokens()) 
	    {
	        String namevaluepair = andsplitter.nextToken();
            StringTokenizer equalsplitter = new StringTokenizer(namevaluepair,"=");
            if (equalsplitter.countTokens() == 2) 
            {
                String name = equalsplitter.nextToken();
                String value = equalsplitter.nextToken();
                anropin.put(URLDecoder.decode(URLDecoder.decode(name, "UTF-8"), "UTF-8"),URLDecoder.decode(URLDecoder.decode(value, "UTF-8"), "UTF-8"));
            }
        }
        return anropin;
    }




	private Hashtable serializeEnvelope(CommunicationEnvelope requestEnvelope)
	{
		Hashtable hash = new Hashtable();
		System.out.println("Serializing:" + requestEnvelope);
		hash.put("action", requestEnvelope.getAction());
		hash.put("status", requestEnvelope.getStatus());
		
		List nodes = requestEnvelope.getNodes();
		int i = 0;
		Iterator iterator = nodes.iterator();
		while(iterator.hasNext())
		{
			Node n = (Node)iterator.next();
			hash.put("nodeList." + i + ".id", "" + n.getId());
			hash.put("nodeList." + i + ".name", "" + n.getName());
			hash.put("nodeList." + i + ".isBranch", "" + n.getIsBranch());
			i++;
		}	
				
		return hash;		
	}


	private CommunicationEnvelope deserializeEnvelope(Hashtable hash)
	{
		CommunicationEnvelope communicationEnvelope = new CommunicationEnvelope();
		communicationEnvelope.setAction("" + hash.get("action"));
		communicationEnvelope.setStatus("" + hash.get("status"));
		//System.out.println("Action:" + communicationEnvelope.getAction());
		//System.out.println("Status:" + communicationEnvelope.getStatus());
		
		List nodes = new ArrayList();
		int i = 0;
		String id = (String)hash.get("nodeList." + i + ".id");
		while(id != null)
		{
			Node n = new Node();
			n.setId(new Integer(id));
			n.setName((String)hash.get("nodeList." + i + ".name"));
			n.setIsBranch(new Boolean((String)hash.get("nodeList." + i + ".isBranch")));
			nodes.add(n);
			//System.out.println("Node:" + n);
			i++;
			id = (String)hash.get("nodeList." + i + ".id");
		}	
		communicationEnvelope.setNodes(nodes);
				
		return communicationEnvelope;		
	}


}