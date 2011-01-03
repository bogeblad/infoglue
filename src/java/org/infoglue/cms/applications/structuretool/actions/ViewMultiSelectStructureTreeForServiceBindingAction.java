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

package org.infoglue.cms.applications.structuretool.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.AvailableServiceBindingController;
import org.infoglue.cms.controllers.kernel.impl.simple.QualifyerController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.management.ServiceDefinitionVO;
import org.infoglue.cms.entities.structure.QualifyerVO;
import org.infoglue.cms.entities.structure.ServiceBindingVO;
import org.infoglue.cms.util.ConstraintExceptionBuffer;

/**
 * This action shows the Content-tree when binding stuff.
 */ 

public class ViewMultiSelectStructureTreeForServiceBindingAction extends InfoGlueAbstractAction
{
    private final static Logger logger = Logger.getLogger(ViewMultiSelectStructureTreeForServiceBindingAction.class.getName());

	private static final long serialVersionUID = 1L;

    private Integer siteNodeVersionId;
    private Integer repositoryId;
    private Integer availableServiceBindingId;
    private Integer serviceDefinitionId;
    private Integer bindingTypeId;
    private ConstraintExceptionBuffer ceb;
   	private Integer siteNodeId;
   	private ServiceDefinitionVO singleServiceDefinitionVO;
   	private String qualifyerXML;
	private String tree;
	private String exp="";	
	private List repositories;
   	
   	//Test
   	private String qualifyerString = "";
   	private List qualifyers = null;
   	private Integer entityId;
   	private Integer direction;
   	private Integer oldSortOrder;
   	private Integer serviceBindingId;
   	
   	// More test by ss
   	private Integer requestedPosition;
   	
   	private ServiceBindingVO serviceBindingVO = null;
   
  
  	public ViewMultiSelectStructureTreeForServiceBindingAction()
	{
		this(new ServiceBindingVO());
	}
	
	public ViewMultiSelectStructureTreeForServiceBindingAction(ServiceBindingVO serviceBindingVO)
	{
		this.serviceBindingVO = serviceBindingVO;
		this.ceb = new ConstraintExceptionBuffer();			
	}	

	public void setSiteNodeVersionId(Integer siteNodeVersionId)
	{
		this.siteNodeVersionId = siteNodeVersionId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

	public void setAvailableServiceBindingId(Integer availableServiceBindingId)
	{
		this.availableServiceBindingId = availableServiceBindingId;
	}

	public void setServiceDefinitionId(Integer serviceDefinitionId)
	{
		this.serviceDefinitionId = serviceDefinitionId;
	}

	public void setBindingTypeId(Integer bindingTypeId)
	{
		this.serviceBindingVO.setBindingTypeId(bindingTypeId);
	}

	public void setPath(String path)
	{
		this.serviceBindingVO.setPath(path);
	}
	
	public Integer getSiteNodeVersionId()
	{
		return this.siteNodeVersionId;
	}

	public Integer getSiteNodeId()
	{
		return this.siteNodeId;
	}
	    
	public Integer getRepositoryId()
	{
		return this.repositoryId;
	}

	public Integer getAvailableServiceBindingId()
	{
		return this.availableServiceBindingId;
	}
    
	public Integer getServiceDefinitionId()
	{
		return this.singleServiceDefinitionVO.getServiceDefinitionId();
	}
	
	public Integer getBindingTypeId()
	{
		return this.bindingTypeId;
	}

	public ServiceDefinitionVO getSingleServiceDefinitionVO()
	{
		return this.singleServiceDefinitionVO;
	}

	public void setQualifyerXML(String qualifyerXML)
	{
		this.qualifyerXML = qualifyerXML;
	}
	
	public String getQualifyerXML()
	{
		return this.qualifyerXML;
	}
	
	
	
	//Test
	public void setQualifyerString(String qualifyerString)
	{
		this.qualifyerString = qualifyerString;
	}

	public void setEntityId(Integer entityId)
	{
		this.entityId = entityId;
	}

	public void setDirection(Integer direction)
	{
		this.direction = direction;
	}

	public void setOldSortOrder(Integer oldSortOrder)
	{
		this.oldSortOrder = oldSortOrder;
	}

	public String getQualifyerString()
	{
		return this.qualifyerString;
	}
	
	public List getQualifyers()
	{
		return this.qualifyers;
	}
	
	public void setServiceBindingId(Integer serviceBindingId)
	{
		this.serviceBindingId = serviceBindingId;
	}
	
	public Integer getServiceBindingId()
	{
		return this.serviceBindingId;
	}
	
	public String getTree()
	{
		return tree;
	}

	public void setTree(String string)
	{
		tree = string;
	}
	
	public String getCurrentAction()
	{
		return "ViewMultiSelectStructureTreeForServiceBinding.action";
	}
	
	public List getRepositories()
	{
		return repositories;
	}  
	
	public String getQualifyerPath(String entityId)
	{	
		try
		{	
			return SiteNodeController.getController().getSiteNodeVOWithId(new Integer(entityId)).getName();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return "";
	}
	 
	private void initialize() throws Exception
	{
		this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);

		List serviceDefinitions = AvailableServiceBindingController.getController().getServiceDefinitionVOList(this.availableServiceBindingId);
		if(serviceDefinitions.size() == 1)    
			this.singleServiceDefinitionVO = (ServiceDefinitionVO)serviceDefinitions.get(0);	    
		
	}
	     
    public String doExecute() throws Exception
    {
    	this.repositories = RepositoryController.getController().getAuthorizedRepositoryVOList(this.getInfoGluePrincipal(), true);
		
		if(this.repositoryId == null)
			this.repositoryId = RepositoryController.getController().getFirstRepositoryVO().getRepositoryId();
		
    	List serviceDefinitions = AvailableServiceBindingController.getController().getServiceDefinitionVOList(this.availableServiceBindingId);
    	if(serviceDefinitions == null || serviceDefinitions.size() == 0)
    	{
	    	//throw new SystemException();
	        return "error";

	    }
    	else if(serviceDefinitions.size() == 1)
    	{
			this.singleServiceDefinitionVO = (ServiceDefinitionVO)serviceDefinitions.get(0);	    
	    	if(this.serviceBindingId != null)
		        this.qualifyers = QualifyerController.getBindingQualifyers(this.serviceBindingId);
	
	        return "success";
    	}
    	else
    	{
    		return "chooseService";
    	}
    }
    
    
	public String doChangeRepository() throws Exception
	{
		this.qualifyers = parseQualifyers(qualifyerString);
		
		initialize();
		  	
		return "success";
	}
	
	public String doChangeTree() throws Exception
	{
		this.qualifyers = parseQualifyers(qualifyerString);
		
		initialize();
		  	
		return "success";
	}
	
    public String doAddQualifyer() throws Exception
    {
    	if(this.qualifyerString != null && !this.qualifyerString.equals(""))
    		this.qualifyerString += "," + this.entityId;
    	else
			this.qualifyerString += this.entityId;
			
		this.qualifyers = parseQualifyers(qualifyerString);
		
		initialize();
		  	
    	return "success";
    }
	public String doAddQualifyerAtPosition() throws Exception
	{
		if(this.qualifyerString == null || this.qualifyerString.equals("") || this.requestedPosition == null )
			this.qualifyerString += "," + this.entityId;
		else
		{
			StringBuffer buf = new StringBuffer(); 
			StringTokenizer qs = new StringTokenizer(qualifyerString, ",");
			int i = 0;
			while (qs.hasMoreTokens()) 
			{
				if(i++==this.requestedPosition.intValue())
					buf.append(this.entityId + ",");

				String qToken = qs.nextToken();
				if(qToken.length() > 0)
					buf.append(qToken + ",");
			}
			this.qualifyerString = buf.toString();
		}
		this.qualifyers = parseQualifyers(qualifyerString);
		
		initialize();
		  	
		return "success";
	}
	public String doMoveQualifyerToPosition() throws Exception
	{
		this.entityId = new Integer(((QualifyerVO) parseQualifyers(qualifyerString).get(oldSortOrder.intValue())).getValue());
		this.qualifyers = parseQualifyers(qualifyerString);
		this.qualifyers = deleteQualifyer(this.oldSortOrder, this.qualifyers);
		this.qualifyerString = parseQualifyers(this.qualifyers);		
		if(requestedPosition.intValue()==-1)
			return doAddQualifyer();
		
		if (oldSortOrder.intValue() < requestedPosition.intValue()) requestedPosition=new Integer(requestedPosition.intValue()-1);  	
		return doAddQualifyerAtPosition();
	}

    public String doMoveQualifyer() throws Exception
    {	
    	logger.info("------------------------------------->");
		this.qualifyers = parseQualifyers(qualifyerString);
		this.qualifyers = moveQualifyer(this.direction, this.oldSortOrder, this.qualifyers);  	

		initialize();
		
    	return "success";
    }


    public String doDeleteQualifyer() throws Exception
    {	
    	logger.info("------------------------------------->");
		this.qualifyers = parseQualifyers(qualifyerString);
		this.qualifyers = deleteQualifyer(this.oldSortOrder, this.qualifyers);  	
		
		initialize();
		
    	return "success";
    }
        
    private List parseQualifyers(String qualifyerString)
    {
    	List qualifyers = new ArrayList(); 
    	StringTokenizer st = new StringTokenizer(qualifyerString, ",");
    	int i = 0;
    	while (st.hasMoreTokens()) 
    	{
        	String next = st.nextToken();
        	QualifyerVO qualifyerVO = new QualifyerVO();
        	qualifyerVO.setName("siteNodeId");
			qualifyerVO.setValue(next);    
			qualifyerVO.setSortOrder(new Integer(i));
        	qualifyers.add(qualifyerVO);
        	i++;
     	}
		return qualifyers;
    }
	private String parseQualifyers(List qualifyers)
	{
		StringBuffer buf = new StringBuffer();
		Iterator i = qualifyers.iterator();
		while(i.hasNext())
		{
			QualifyerVO q = (QualifyerVO) i.next(); 
			buf.append(q.getValue() + ",");
		}
		return buf.toString();
	}
    

    private List moveQualifyer(Integer direction, Integer oldSortOrder, List qualifyers)
    {
    	logger.info("-------------------------------------> About to move the qualifyer in direction " + direction + " and old sortOrder was " + oldSortOrder);
    	ArrayList newQualifyers = new ArrayList();
    	
    	Iterator iterator = qualifyers.iterator();
    	int i = 0;
    	while(iterator.hasNext())
    	{
    		QualifyerVO qualifyer = (QualifyerVO)iterator.next();
    		logger.info("Found qualifyer " + qualifyer.getValue() + ":" + qualifyer.getSortOrder());
			if(qualifyer.getSortOrder().equals(oldSortOrder) && direction.intValue() == 0) //down
			{
				logger.info("About to move it down...");
				if(iterator.hasNext())
				{
					QualifyerVO nextQualifyer = (QualifyerVO)iterator.next();
		    		logger.info("nextQualifyer " + nextQualifyer.getValue() + ":" + nextQualifyer.getSortOrder());
					nextQualifyer.setSortOrder(qualifyer.getSortOrder());	
					logger.info("Set the nextQualifyer sortOrder to " + qualifyer.getSortOrder());
					qualifyer.setSortOrder(new Integer(qualifyer.getSortOrder().intValue() + 1));
					logger.info("Set the qualifyer sortOrder to " + qualifyer.getSortOrder());
					newQualifyers.add(nextQualifyer);
					newQualifyers.add(qualifyer);
				}
				else
					newQualifyers.add(qualifyer);
						
			}
			else if(qualifyer.getSortOrder().equals(oldSortOrder) && direction.intValue() == 1) //up
			{
				logger.info("About to move it up...");
				if(i > 0)
				{
					QualifyerVO previousQualifyer = (QualifyerVO)newQualifyers.get(i-1);
		    		logger.info("Previous qualifyer " + previousQualifyer.getValue() + ":" + previousQualifyer.getSortOrder());
					previousQualifyer.setSortOrder(qualifyer.getSortOrder());	
					logger.info("Set the previous qualifyer sortOrder to " + qualifyer.getSortOrder());
					qualifyer.setSortOrder(new Integer(qualifyer.getSortOrder().intValue() - 1));
					logger.info("Set the qualifyer sortOrder to " + qualifyer.getSortOrder());
					newQualifyers.remove(qualifyer);
					newQualifyers.add(i-1, qualifyer);
				}
				else
					newQualifyers.add(qualifyer);
			}
			else
				newQualifyers.add(qualifyer);	 		
    	
    		i++;
    	}	
    	
    	return newQualifyers;
    }

    private List deleteQualifyer(Integer oldSortOrder, List qualifyers)
    {
    	ArrayList newQualifyers = new ArrayList();
    	
    	Iterator iterator = qualifyers.iterator();
    	int i = 0;
    	while(iterator.hasNext())
    	{
    		QualifyerVO qualifyer = (QualifyerVO)iterator.next();
    		logger.info("-------------------------------->Found qualifyer " + qualifyer.getValue() + ":" + qualifyer.getSortOrder());
			if(!qualifyer.getSortOrder().equals(oldSortOrder))
			{
				logger.info("qualifyer:" + qualifyer.getSortOrder());
				logger.info("qualifyer:" + qualifyer.getValue());
				logger.info("Adding this qualifyer again as it did not match the delete-one:" + oldSortOrder);
				qualifyer.setSortOrder(new Integer(i)); //Bugfix ss
				newQualifyers.add(qualifyer);				
				i++;
			}
    	}	
    	
    	return newQualifyers;
    }

	/**
	 * @return
	 */
	public String getExp() {
		return exp;
	}

	/**
	 * @param string
	 */
	public void setExp(String string) {
		exp = string;
	}

	/**
	 * @return
	 */
	public Integer getRequestedPosition() {
		return requestedPosition;
	}

	/**
	 * @param integer
	 */
	public void setRequestedPosition(Integer integer) {
		requestedPosition = integer;
	}

}
