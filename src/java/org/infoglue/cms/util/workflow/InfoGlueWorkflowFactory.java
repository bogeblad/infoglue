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

package org.infoglue.cms.util.workflow;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowDefinitionController;
import org.infoglue.cms.entities.workflow.WorkflowDefinitionVO;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.util.CacheController;

import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.AbstractWorkflowFactory;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;


/**
 * @author Mattias Bogeblad
 */

public class InfoGlueWorkflowFactory extends AbstractWorkflowFactory
{
    private final static Logger logger = Logger.getLogger(InfoGlueWorkflowFactory.class.getName());

    protected boolean reload;

    public void setLayout(String workflowName, Object layout) 
    {
    }

    public Object getLayout(String workflowName) 
    {
        return null;
    }

    public boolean removeWorkflow(String name) throws FactoryException 
    {
        throw new FactoryException("remove workflow not supported");
    }

    public void renameWorkflow(String oldName, String newName) 
    {
    }

    public void save() 
    {
    }

    public boolean isModifiable(String name) 
    {
        return true;
    }

    public String getName() 
    {
        return "";
    }

    public WorkflowDescriptor getWorkflow(String name, boolean validate) throws FactoryException 
    {
        Map workflows = (Map)CacheController.getCachedObject("workflowCache", "workflowMap");
		
        if(workflows == null)
            initDone(); 

        workflows = (Map)CacheController.getCachedObject("workflowCache", "workflowMap");

        WorkflowConfig c = (WorkflowConfig) workflows.get(name);

        if (c == null) {
            throw new FactoryException("Unknown workflow name \"" + name + "\"");
        }

        if (c.descriptor != null) 
        {
            loadWorkflow(c, validate);
        } 
        else 
        {
            loadWorkflow(c, validate);
        }

        c.descriptor.setName(name);

        return c.descriptor;
    }

    public void reload() throws FactoryException
    {
        initDone();   
    }

    public String[] getWorkflowNames() 
    {
        Map workflows = (Map)CacheController.getCachedObject("workflowCache", "workflowMap");
		
        if(workflows == null)
        {
            try
            {
                initDone();
            } 
        	catch (FactoryException e)
            {
                e.printStackTrace();
            } 
        }

        workflows = (Map)CacheController.getCachedObject("workflowCache", "workflowMap");

        int i = 0;
        String[] res = new String[workflows.keySet().size()];
        Iterator it = workflows.keySet().iterator();

        while (it.hasNext()) 
        {
            res[i++] = (String) it.next();
        }

        return res;
    }

    public void createWorkflow(String name) 
    {
        try
        {
            initDone();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void initDone() throws FactoryException 
    {
        try
        {
            Map workflows = new HashMap();
            
            List list = WorkflowDefinitionController.getController().getWorkflowDefinitionVOList();

            Iterator listIterator = list.iterator();
            while(listIterator.hasNext())
            {
                WorkflowDefinitionVO workflowDefinitionVO = (WorkflowDefinitionVO)listIterator.next();
                WorkflowConfig config = new WorkflowConfig(workflowDefinitionVO);
                workflows.put(workflowDefinitionVO.getName(), config);
            }

            CacheController.cacheObject("workflowCache", "workflowMap", workflows);
        }
        catch (Exception e) 
        {
            throw new InvalidWorkflowDescriptorException("Error in workflow config", e);
        }
        
    }


    public boolean saveWorkflow(String name, WorkflowDescriptor descriptor, boolean replace) throws FactoryException 
    {
        throw new FactoryException("Not supported...");
    }


    private void loadWorkflow(WorkflowConfig c, boolean validate) throws FactoryException 
    {
        try 
        {
            String encoding = CmsPropertyHandler.getWorkflowEncoding();
            if(encoding == null || encoding.length() == 0 || encoding.equalsIgnoreCase("@workflowEncoding@"))
                encoding = "UTF-8";
            
            WorkflowDescriptor workflowDescriptor = (WorkflowDescriptor)CacheController.getCachedObject("workflowCache", "workflowDescriptor_" + c.workflowDefinitionVO.getName());
            if(workflowDescriptor == null)
            {
            	if(logger.isInfoEnabled())
            		logger.info("No cached workflow descriptor - reading it...");
            	workflowDescriptor = WorkflowLoader.load(new ByteArrayInputStream(c.workflowDefinitionVO.getValue().getBytes(encoding)) , validate);
            	CacheController.cacheObject("workflowCache", "workflowDescriptor_" + c.workflowDefinitionVO.getName(), workflowDescriptor);
            }
            else
            {
            	if(logger.isInfoEnabled())
            		logger.info("Found cached workflow descriptor - using it...");
            }
            c.descriptor = workflowDescriptor; //WorkflowLoader.load(new ByteArrayInputStream(c.workflowDefinitionVO.getValue().getBytes(encoding)) , validate);
        } 
        catch (Exception e) 
        {
            throw new FactoryException("Error in workflow descriptor: " + c.workflowDefinitionVO.getName(), e);
        }
    }

    //~ Inner Classes //////////////////////////////////////////////////////////

    static class WorkflowConfig 
    {
        WorkflowDescriptor descriptor;
        WorkflowDefinitionVO workflowDefinitionVO;

        public WorkflowConfig(WorkflowDefinitionVO workflowDefinitionVO) 
        {
            this.workflowDefinitionVO = workflowDefinitionVO;
        }
    }


} 