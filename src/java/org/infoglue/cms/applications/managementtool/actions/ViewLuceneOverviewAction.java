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

package org.infoglue.cms.applications.managementtool.actions;

import java.util.Map;

import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.controllers.kernel.impl.simple.LuceneController;

/**
 * This class implements the action class for looking at the lucene index.
 * 
 * @author Mattias Bogeblad  
 */

public class ViewLuceneOverviewAction extends InfoGlueAbstractAction
{ 
	private static final long serialVersionUID = 1L;

	private static boolean running = false;
	private String statusMessage = "";
	
	private Map indexInformation;
	
	/**
     * The main method that fetches the Value-object for this use-case
     */
    
    public String doExecute() throws Exception
    {
		logUserActionInfo(getClass(), "doExecute");
    	indexInformation = LuceneController.getController().getIndexInformation();
    	
        return "success";
    }

    public String doDeleteIndex() throws Exception
    {
		logUserActionInfo(getClass(), "doDeleteIndex");
    	if(!running)
    	{
    		running = true;
    	
    		try
    		{
    	    	LuceneController.getController().clearIndex();
    	    	
    	    	indexInformation = LuceneController.getController().getIndexInformation();
        		
    	    	statusMessage = "Deletion complete.";
    		}
    		catch (Throwable t) 
    		{
    			statusMessage = "Deletion failed: " + t.getMessage();
			}
    		finally
    		{
    			running = false;
    		}
    	}
    	else
    	{
    		statusMessage = "Running... wait until complete.";
    	}
    	
    	return "success";
    }

    public String doIndexAll() throws Exception
    {
		logUserActionInfo(getClass(), "doIndexAll");
    	if(!running)
    	{
    		running = true;
    	
    		try
    		{
    	    	LuceneController.getController().indexAll();
    	    	
    	    	indexInformation = LuceneController.getController().getIndexInformation();
        		
    	    	statusMessage = "Reindex complete.";
    		}
    		catch (Throwable t) 
    		{
    			statusMessage = "Reindex failed: " + t.getMessage();
			}
    		finally
    		{
    			running = false;
    		}
    	}
    	else
    	{
    		statusMessage = "Running... wait until complete.";
    	}
    	
        return "success";
    }

    public Map getIndexInformation()
	{
		return indexInformation;
	}

	public String getStatusMessage()
	{
		return statusMessage;
	}

}
