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

import java.io.File;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
import org.infoglue.cms.applications.databeans.ProcessBean;
import org.infoglue.cms.applications.databeans.ReferenceBean;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DeleteRepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.ConstraintException;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.jobs.CleanOldVersionsJob;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.FileUploadHelper;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SimpleTrigger;
import org.quartz.jobs.NoOpJob;
import org.quartz.spi.TriggerFiredBundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import webwork.action.ActionContext;

/**
 * This class represents the optimization tools of the system
 * It offers the user a few options in which to optimize the database and model.
 * 
 * @author mattias
 */

public class ViewArchiveToolAction extends InfoGlueAbstractAction
{
	private static final long serialVersionUID = 1L;
	
	private int numberOfVersionsToKeep = 3;
	private int assetFileSizeLimit = 5000000;
	private int assetNumberLimit = 50;
	private List optimizationBeanList = new ArrayList();
	
	private String[] digitalAssetId;
	private String archiveUrl;
	private StringBuffer archiveFileSize = new StringBuffer();
	private boolean nullAssets = false;
	
	private boolean deleteVersions = false;
	private Integer redoNumberOfTimes = 1;
	private Integer numberOfCleanedSiteNodeVersions = null;
	private Integer numberOfCleanedContentVersions = null;
	private Map<String,Integer> cleaningMap = null;

	private Integer contentId = null;
	private Boolean recurse = false;
	private Map<Integer,Long> sizes = new HashMap<Integer,Long>();
	
	private String processId = null;

	
	public String doInput() throws Exception
    {
    	return "input";
    }

	public String doInputArchiveOldAssets() throws Exception
    {
		//optimizationBeanList = ContentVersionController.getContentVersionController().getHeavyContentVersions(numberOfVersionsToKeep, assetFileSizeLimit, assetNumberLimit);
		optimizationBeanList = ContentVersionController.getContentVersionController().getAssetsPossibleToArchive(numberOfVersionsToKeep, assetFileSizeLimit, assetNumberLimit);
        		
        return "inputArchiveOldAssets";
    }

	public String doInputRestoreAssetArchive() throws Exception
    {        		
        return "inputRestoreAssetArchive";
    }
	
	public String doArchiveOldAssets() throws Exception
    {
		archiveUrl = DigitalAssetController.getController().archiveDigitalAssets(digitalAssetId, archiveFileSize, nullAssets);
		
        return "successArchive";
    }

	public String doRestoreAssetArchive() throws Exception
    {
		File file = FileUploadHelper.getUploadedFile(ActionContext.getContext().getMultiPartRequest());
		if(file == null || !file.exists())
			throw new SystemException("The file upload must have gone bad as no file reached the restore utility.");

		DigitalAssetController.getController().restoreAssetArchive(file);
		
        return "successRestoreArchive";
    }

	public String doCleanOldVersions() throws Exception
    {
		if(!deleteVersions)
			return doCleanOldVersionsPreview();
		
		
		VisualFormatter vf = new VisualFormatter();
		String exportId = "ArchiveTool_" + vf.formatDate(new Date(), "yyyy-MM-dd_HHmm");

		ProcessBean processBean = ProcessBean.createProcessBean(DeleteRepositoryAction.class.getName(), exportId, "Archiving versions");
		CleanOldVersionsJobController.CleanOldVersions(processBean, deleteVersions, redoNumberOfTimes);
		
		return "successRedirectToProcesses";
    }

	public String doCleanOldVersionsPreview() throws Exception
    {
		JobDetail jobDetail = new JobDetail();

		SimpleTrigger trig = new SimpleTrigger();

		JobExecutionContext jec = new JobExecutionContext(null, new TriggerFiredBundle(jobDetail, trig, null, false, null, null, null, null), new NoOpJob());
		jec.put("deleteVersions", new Boolean(deleteVersions));
		jec.put("redoNumberOfTimes", redoNumberOfTimes);
		new CleanOldVersionsJob().execute(jec);

		Map<String,Integer> result = (Map<String,Integer>)jec.getResult();
		this.cleaningMap = result;
		
        return "input";
    }

	public String doCleanOldVersionsForContent() throws Exception
    {
		Map<String,Integer> totalCleanedContentVersions = new HashMap<String,Integer>();
		
		ContentVO contentVOToClean = ContentController.getContentController().getContentVOWithId(contentId);
		ContentTypeDefinitionVO ctdVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(contentVOToClean.getContentTypeDefinitionId());
		
    	String keepOnlyOldPublishedVersionsString = CmsPropertyHandler.getKeepOnlyOldPublishedVersionsDuringClean();
    	long minimumTimeBetweenVersionsDuringClean = CmsPropertyHandler.getMinimumTimeBetweenVersionsDuringClean();
    	boolean keepOnlyOldPublishedVersions = Boolean.parseBoolean(keepOnlyOldPublishedVersionsString);

		int cleanedContentVersions = ContentVersionController.getContentVersionController().cleanContentVersions(contentVOToClean, recurse, numberOfVersionsToKeep, keepOnlyOldPublishedVersions, minimumTimeBetweenVersionsDuringClean, deleteVersions);
		totalCleanedContentVersions.put(ctdVO.getName(), cleanedContentVersions);
		
		this.cleaningMap = totalCleanedContentVersions;
		
        return "input";
    }

    public String doExecute() throws Exception
    {
        return "success";
    }

	public int getNumberOfVersionsToKeep() 
	{
		return numberOfVersionsToKeep;
	}

	public void setNumberOfVersionsToKeep(int numberOfVersionsToKeep) 
	{
		this.numberOfVersionsToKeep = numberOfVersionsToKeep;
	}

	public List getOptimizationBeanList() 
	{
		return optimizationBeanList;
	}

	public void setDigitalAssetId(String[] digitalAssetId) 
	{
		this.digitalAssetId = digitalAssetId;
	}

	public String getArchiveUrl() 
	{
		return archiveUrl;
	}

	public String getArchiveFileSize() 
	{
		return archiveFileSize.toString();
	}

	public int getAssetFileSizeLimit() 
	{
		return assetFileSizeLimit;
	}

	public int getAssetNumberLimit() 
	{
		return assetNumberLimit;
	}

	public void setAssetFileSizeLimit(int assetFileSizeLimit) 
	{
		this.assetFileSizeLimit = assetFileSizeLimit;
	}

	public void setAssetNumberLimit(int assetNumberLimit) 
	{
		this.assetNumberLimit = assetNumberLimit;
	}

	public boolean getNullAssets()
	{
		return nullAssets;
	}

	public void setNullAssets(boolean nullAssets)
	{
		this.nullAssets = nullAssets;
	}

    public Integer getNumberOfCleanedContentVersions()
	{
		return numberOfCleanedContentVersions;
	}

    public Integer getNumberOfCleanedSiteNodeVersions()
	{
		return numberOfCleanedSiteNodeVersions;
	}

	public boolean getDeleteVersions()
	{
		return deleteVersions;
	}

	public void setDeleteVersions(boolean deleteVersions)
	{
		this.deleteVersions = deleteVersions;
	}

	public void setRedoNumberOfTimes(Integer redoNumberOfTimes)
	{
		this.redoNumberOfTimes = redoNumberOfTimes;
	}
	
    public Integer getContentId()
	{
		return contentId;
	}

    public void setContentId(Integer contentId)
	{
		this.contentId = contentId;
	}
    
    public Boolean getRecurse()
	{
		return recurse;
	}
	
    public void setRecurse(Boolean recurse)
	{
		this.recurse = recurse;
	}

	public Map<String, Integer> getCleaningMap() 
	{
		return cleaningMap;
	}

	public Map<Integer, Long> getContentSizes()
	{
		return this.sizes;
	}
	
	public String doShowProcesses() throws Exception
	{
		return "successShowProcesses";
	}

	public String doShowProcessesAsJSON() throws Exception
	{
		// TODO it would be nice we could write JSON to the OutputStream but we get a content already transmitted exception then.
		return "successShowProcessesAsJSON";
	}


	public String getStatusAsJSON()
	{
		Gson gson = new GsonBuilder()
			.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
			.setDateFormat("dd MMM HH:mm:ss").create();
		JsonObject object = new JsonObject();

		try
		{
			List<ProcessBean> processes = getProcessBeans();
			Type processBeanListType = new TypeToken<List<ProcessBean>>() {}.getType();
			JsonElement list = gson.toJsonTree(processes, processBeanListType);
			object.add("processes", list);
			object.addProperty("memoryMessage", getMemoryUsageAsText());
			 
			Iterator<ProcessBean> beanIterator = processes.iterator();
			while(beanIterator.hasNext())
			{
				ProcessBean bean = beanIterator.next();
				if(bean.getStatus() == ProcessBean.REDIRECTED || bean.getStatus() == ProcessBean.FINISHED)
				{	
					bean.setStatus(ProcessBean.FINISHED);
					bean.removeProcess();
				}
			}
		}
		catch (Throwable t)
		{
			JsonObject error = new JsonObject(); 
			error.addProperty("message", t.getMessage());
			error.addProperty("type", t.getClass().getSimpleName());
			object.add("error", error);
		}

		return gson.toJson(object);
	}
	
	public List<ProcessBean> getProcessBeans()
	{
		return ProcessBean.getProcessBeans(DeleteRepositoryAction.class.getName());
	}
	
	/**
	 * This deletes a process info bean and related files etc.
	 * @return
	 * @throws Exception
	 */	

	public String doDeleteProcessBean() throws Exception
	{
		if(this.processId != null)
		{
			ProcessBean pb = ProcessBean.getProcessBean(DeleteRepositoryAction.class.getName(), processId);
			if(pb != null)
				pb.removeProcess();
		}
		
		return "successRedirectToProcesses";
	}

	public void setProcessId(String processId) 
	{
		this.processId = processId;
	}
	
}

class CleanOldVersionsJobController implements Runnable
{
    public final static Logger logger = Logger.getLogger(CleanOldVersionsJobController.class.getName());

    private ProcessBean processBean;
    private Boolean deleteVersions;
    private Integer redoNumberOfTimes;
    
	private CleanOldVersionsJobController(ProcessBean processBean, Boolean deleteVersions, Integer redoNumberOfTimes)
	{
		this.processBean = processBean;
		this.deleteVersions = deleteVersions;
		this.redoNumberOfTimes = redoNumberOfTimes;
	}
	
	/**
	 * Factory method to get object
	 */
	
	public static void CleanOldVersions(ProcessBean processBean, Boolean deleteVersions, Integer redoNumberOfTimes) throws Exception
	{
		CleanOldVersionsJobController cleanOldVersionsJobController = new CleanOldVersionsJobController(processBean, deleteVersions, redoNumberOfTimes);
		Thread thread = new Thread(cleanOldVersionsJobController);
		thread.start();
	}
	   	
	public synchronized void run()
	{
		try
		{
			processBean.setStatus(ProcessBean.RUNNING);

			JobDetail jobDetail = new JobDetail();

			SimpleTrigger trig = new SimpleTrigger();

			JobExecutionContext jec = new JobExecutionContext(null, new TriggerFiredBundle(jobDetail, trig, null, false, null, null, null, null), new NoOpJob());
			jec.put("deleteVersions", new Boolean(deleteVersions));
			jec.put("redoNumberOfTimes", redoNumberOfTimes);
			jec.put("processBean", processBean);
			new CleanOldVersionsJob().execute(jec);

			Map<String,Integer> result = (Map<String,Integer>)jec.getResult();
			//Map cleaningMap = result;
			
			StringBuffer resultSB = new StringBuffer();
			for(String key : result.keySet())
			{
				resultSB.append(key + " - cleaned " + result.get(key) + " versions<br/>");
			}
			
			processBean.updateProcess("" + resultSB);
			
			processBean.updateProcess("Done - go back by clicking <a href='ViewArchiveTool!input.action'>here</a>");
			//processBean.setRedirectUrl("Deletion complete..", "" + "ViewArchiveTool!input.action");
			//processBean.setStatus(ProcessBean.REDIRECTED);
			processBean.updateProcess("...");
			processBean.setStatus(ProcessBean.FINISHED);
		}
		catch (Exception e) 
		{
			processBean.setError("Something went wrong. Please consult the logfiles.");
			logger.error("Error in monitor:" + e.getMessage(), e);
		}
	}
}