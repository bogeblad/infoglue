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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.structure.Qualifyer;
import org.infoglue.cms.entities.structure.QualifyerVO;
import org.infoglue.cms.entities.structure.ServiceBinding;
import org.infoglue.cms.entities.structure.impl.simple.QualifyerImpl;
import org.infoglue.cms.entities.structure.impl.simple.ServiceBindingImpl;
import org.infoglue.cms.exception.Bug;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Mattias Bogeblad
 */

public class QualifyerController extends BaseController 
{
    private final static Logger logger = Logger.getLogger(QualifyerController.class.getName());

	public static Qualifyer getQualifyerWithId(Integer qualifyerId, Database db) throws SystemException, Bug
	{
		return (Qualifyer) getObjectWithId(QualifyerImpl.class, qualifyerId, db);
	}


   	/**
   	 * This method creates a new qualifyer for a serviceBinding. It is basically this qualifyer that
   	 * specifies which stuff to fetch from the serviceDefinition.
   	 */
	/*
   	public static QualifyerVO create(String qualifyerXML, Integer serviceBindingId) throws ConstraintException, SystemException
   	{
		Database db = CastorDatabaseService.getDatabase();
        ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();

		Qualifyer qualifyer = null;
		
        beginTransaction(db);
		
 		ServiceBinding serviceBinding = ServiceBindingController.getServiceBindingWithId(serviceBindingId, db);
   		
   		List qualifyerVOList = parseQualifyerList(qualifyerXML);
   		
   		Iterator i = qualifyerVOList.iterator();
   		while(i.hasNext())
   		{
   			QualifyerVO qualifyerVO = (QualifyerVO)i.next();
	   		qualifyer = new QualifyerImpl();
	        qualifyer.setValueObject(qualifyerVO);
	 		qualifyer.setServiceBinding((ServiceBindingImpl)serviceBinding);
	        
	 		qualifyer = (Qualifyer) createEntity(qualifyer, db);			
   		}
   		        
        return qualifyer.getValueObject();
   	}
*/


   	/**
   	 * This method creates a new qualifyer for a serviceBinding. It is basically this qualifyer that
   	 * specifies which stuff to fetch from the serviceDefinition.
   	 */

   	public static Collection createQualifyers(String qualifyerXML, ServiceBinding serviceBinding) throws ConstraintException, SystemException
   	{
		Collection qualifyers = new ArrayList();
		
   		List qualifyerVOList = parseQualifyerList(qualifyerXML);
   		
   		Iterator i = qualifyerVOList.iterator();
   		while(i.hasNext())
   		{
   			QualifyerVO qualifyerVO = (QualifyerVO)i.next();
	   		Qualifyer qualifyer = new QualifyerImpl();
	        qualifyer.setValueObject(qualifyerVO);
	 		qualifyer.setServiceBinding((ServiceBindingImpl)serviceBinding);
	        qualifyers.add(qualifyer);
	        logger.info("ADDED:" + qualifyerVO.getValue());
	 		//qualifyer = (Qualifyer) createEntity(qualifyer, db);			
   		}
   		        
        return qualifyers;
   	}


   	/**
   	 * This method creates a new qualifyer for a serviceBinding. It is basically this qualifyer that
   	 * specifies which stuff to fetch from the serviceDefinition.
   	 */

   	public static QualifyerVO screate(QualifyerVO qualifyerVO, Integer serviceBindingId, Database db) throws ConstraintException, SystemException, Exception
   	{
		Qualifyer qualifyer = null;
		
 		ServiceBinding serviceBinding = ServiceBindingController.getServiceBindingWithId(serviceBindingId, db);
   		
   		qualifyer = new QualifyerImpl();
	    qualifyer.setValueObject(qualifyerVO);
	 	qualifyer.setServiceBinding((ServiceBindingImpl)serviceBinding);
	    db.create(qualifyer);
	            
        return qualifyer.getValueObject();
   	}


	  
	/**
	 * This method returns a sorted list of qualifyers.
	 */
	public static List getBindingQualifyers(Integer serviceBindingId) throws SystemException, Bug, Exception
	{
		List qualifyers = new ArrayList();
		
		Database db = CastorDatabaseService.getDatabase();
        
		beginTransaction(db);
        
        try
        {	
        	List unsortedQualifyers = ServiceBindingController.getQualifyerVOList(serviceBindingId);
			
			Iterator i = unsortedQualifyers.iterator();
			while(i.hasNext()) 
	        {
	        	boolean isAdded = false;
	        	QualifyerVO qualifyerVO = (QualifyerVO)i.next();
				Iterator newListIterator = qualifyers.iterator();
				int index = 0;
				while(newListIterator.hasNext())
				{
					QualifyerVO sortedQualifyerVO = (QualifyerVO)newListIterator.next();
					if(sortedQualifyerVO.getSortOrder().intValue() < qualifyerVO.getSortOrder().intValue())	
						logger.info("The old copy was before me... lets not do anything..");
					else
					{
						logger.info("The old copy was after me... lets insert the new one before it..");
						qualifyers.add(index, qualifyerVO);
						isAdded = true;
						break;
					}
				}
				
				if(!isAdded)
					qualifyers.add(qualifyerVO);
	        }
	        
	        commitTransaction(db);
        }
       	catch(Exception e)
        {
            e.printStackTrace();
            logger.error("An error occurred so we should not completes the transaction:" + e, e);
            rollbackTransaction(db);
            throw new SystemException(e.getMessage());
        }
        
		return qualifyers;
	}
	
   	
   	private static List parseQualifyerList(String qualifyerXML)
   	{
   		List qualifyerVOList = new ArrayList();
   		
 		if(qualifyerXML != null)
		{
			try
	        {
		        logger.info("qualifyerXML:" + qualifyerXML);
		        InputSource inputSource = new InputSource(new StringReader(qualifyerXML));
				
				DOMParser parser = new DOMParser();
				parser.parse(inputSource);
				Document document = parser.getDocument();
				
				NodeList nl = document.getDocumentElement().getChildNodes();
				for(int i=0; i<nl.getLength(); i++)
				{
					Node n = nl.item(i);
					String name  = n.getNodeName();
					String value = n.getFirstChild().getNodeValue();
					logger.info("name:" + name);
					logger.info("value:" + value);
					
					QualifyerVO qualifyerVO = new QualifyerVO();
					qualifyerVO.setName(name);
					qualifyerVO.setValue(value);
					qualifyerVO.setSortOrder(new Integer(i));
					
					qualifyerVOList.add(qualifyerVO);
				}		        	
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
		}
   		
   		return qualifyerVOList;
   	}

	/**
	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
	 * is handling.
	 */

	public BaseEntityVO getNewVO()
	{
		return new QualifyerVO();
	}

}
