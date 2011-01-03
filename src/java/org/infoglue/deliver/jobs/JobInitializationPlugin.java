/*
 * Copyright James House (c) 2001-2004
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1.
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 */
package org.infoglue.deliver.jobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.jobs.FileScanJob;
import org.quartz.jobs.FileScanListener;
import org.quartz.spi.SchedulerPlugin;
import org.quartz.xml.JobSchedulingDataProcessor;

/**
* This plugin loads an XML file to add jobs and schedule them with triggers
 * as the scheduler is initialized, and can optionally periodically scan the
 * file for changes.
 * 
 * @author James House
 * @author Pierre Awaragi
 */
public class JobInitializationPlugin implements SchedulerPlugin, FileScanListener {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private String name;

    private Scheduler scheduler;

    private boolean overWriteExistingJobs = false;

    private boolean failOnFileNotFound = true;

    private boolean fileFound = false;

    private String fileName = JobSchedulingDataProcessor.QUARTZ_XML_FILE_NAME;
    
    private String filePath = null;
    
    private boolean useContextClassLoader = true;
    
    private boolean validating = true;
    
    private boolean validatingSchema = true;

    private long scanInterval = 0; 
    
    boolean initializing = true;
    
    boolean started = false;
    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public JobInitializationPlugin() {
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * The file name (and path) to the XML file that should be read.
     * 
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * The file name (and path) to the XML file that should be read.
     * 
     * @param fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Whether or not jobs defined in the XML file should be overwrite existing
     * jobs with the same name.
     * 
     * @return
     */
    public boolean isOverWriteExistingJobs() {
        return overWriteExistingJobs;
    }

    /**
     * Whether or not jobs defined in the XML file should be overwrite existing
     * jobs with the same name.
     * 
     * @param overWriteExistingJobs
     */
    public void setOverWriteExistingJobs(boolean overWriteExistingJobs) {
        this.overWriteExistingJobs = overWriteExistingJobs;
    }

    /**
     * The interval (in seconds) at which to scan for changes to the file.  
     * If the file has been changed, it is re-loaded and parsed.   The default 
     * value for the interval is 0, which disables scanning.
     * 
     * @return Returns the scanInterval.
     */
    public long getScanInterval() {
        return scanInterval / 1000;
    }

    /**
     * The interval (in seconds) at which to scan for changes to the file.  
     * If the file has been changed, it is re-loaded and parsed.   The default 
     * value for the interval is 0, which disables scanning.
     * 
     * @param scanInterval The scanInterval to set.
     */
    public void setScanInterval(long scanInterval) {
        this.scanInterval = scanInterval * 1000;
    }
    
    /**
     * Whether or not initialization of the plugin should fail (throw an
     * exception) if the file cannot be found. Default is <code>true</code>.
     * 
     * @return
     */
    public boolean isFailOnFileNotFound() {
        return failOnFileNotFound;
    }

    /**
     * Whether or not initialization of the plugin should fail (throw an
     * exception) if the file cannot be found. Default is <code>true</code>.
     * 
     * @param overWriteExistingJobs
     */
    public void setFailOnFileNotFound(boolean failOnFileNotFound) {
        this.failOnFileNotFound = failOnFileNotFound;
    }
    
    /**
     * Whether or not the context class loader should be used. Default is <code>true</code>.
     * 
     * @return
     */
    public boolean isUseContextClassLoader() {
        return useContextClassLoader;
    }

    /**
     * Whether or not context class loader should be used. Default is <code>true</code>.
     * 
     * @param useContextClassLoader
     */
    public void setUseContextClassLoader(boolean useContextClassLoader) {
        this.useContextClassLoader = useContextClassLoader;
    }
    
    /**
     * Whether or not the XML should be validated. Default is <code>true</code>.
     * 
     * @return
     */
    public boolean isValidating() {
        return validating;
    }

    /**
     * Whether or not the XML should be validated. Default is <code>true</code>.
     * 
     * @param validating
     */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }
    
    /**
     * Whether or not the XML schema should be validated. Default is <code>true</code>.
     * 
     * @return
     */
    public boolean isValidatingSchema() {
        return validatingSchema;
    }

    /**
     * Whether or not the XML schema should be validated. Default is <code>true</code>.
     * 
     * @param validatingSchema
     */
    public void setValidatingSchema(boolean validatingSchema) {
        this.validatingSchema = validatingSchema;
    }

    protected static Log getLog() {
        return LogFactory.getLog(JobInitializationPlugin.class);
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * SchedulerPlugin Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Called during creation of the <code>Scheduler</code> in order to give
     * the <code>SchedulerPlugin</code> a chance to initialize.
     * </p>
     * 
     * @throws SchedulerConfigException
     *           if there is an error initializing.
     */
    public void initialize(String name, final Scheduler scheduler)
            throws SchedulerException {
        
        initializing = true;
        try {
            this.name = name;
            this.scheduler = scheduler;
    
            getLog().info("Registering Quartz Job Initialization Plug-in.");
            
            findFile();
        }
        finally {
            initializing = false;
        }
    }

    private String getFilePath() throws SchedulerException {
        if(this.filePath == null) {
            findFile();         
        }
        return this.filePath;
    }
    
    /**
     * 
     */
    private void findFile() throws SchedulerException {
        java.io.InputStream f = null;
        
        File file = new File(getFileName()); // files in filesystem
        if (file == null || !file.exists()) {
            // files in classpath
            URL url = Thread.currentThread()
                .getContextClassLoader()
                .getResource(getFileName());
            if(url != null) {
                file = new File(url.getPath()); 
            }
            if(file == null || !file.exists())
            {
                String fileName = CmsPropertyHandler.getContextRootPath() + "WEB-INF" + File.separator + "classes" + File.separator + "jobs.xml";
                file = new File(fileName);
            }
        }        
        try {              
            f = new java.io.FileInputStream(file);
        }catch (FileNotFoundException e) {
            // ignore
        }
        
        if (f == null && isFailOnFileNotFound()) {
            throw new SchedulerException("File named '" + getFileName()
                    + "' does not exist.");
        } else if (f == null) {
            getLog().warn("File named '" + getFileName() + "' does not exist.");
        } else {
            fileFound = true;
            try {
                this.filePath = file.getPath();
                f.close();
            } catch (IOException ioe) {
                getLog()
                        .warn("Error closing file named '" + getFileName(), ioe);
            }
        }
    }

    public void start() {

        if(scanInterval > 0) {
            try{
                SimpleTrigger trig = new SimpleTrigger(
                        "JobInitializationPlugin_"+name, 
                        "JobInitializationPlugin", 
                        new Date(), null, 
                        SimpleTrigger.REPEAT_INDEFINITELY, scanInterval);
                trig.setVolatility(true);
                JobDetail job = new JobDetail(
                        "JobInitializationPlugin_"+name, 
                        "JobInitializationPlugin",
                        FileScanJob.class);
                job.setVolatility(true);
                job.getJobDataMap().put(FileScanJob.FILE_NAME, getFilePath());
                job.getJobDataMap().put(FileScanJob.FILE_SCAN_LISTENER_NAME, "JobInitializationPlugin_"+name);
                
                scheduler.getContext().put("JobInitializationPlugin_"+name, this);
                scheduler.scheduleJob(job, trig);
            }
            catch(SchedulerException se) {
                getLog().error("Error starting background-task for watching jobs file.", se);
            }
        }
        
        try {
            processFile();
        }
        finally {
            started = true;
        }
    }

    /**
     * <p>
     * Called in order to inform the <code>SchedulerPlugin</code> that it
     * should free up all of it's resources because the scheduler is shutting
     * down.
     * </p>
     */
    public void shutdown() {
        // nothing to do
    }

    
    public void processFile() {
        if (!fileFound) return;

        JobSchedulingDataProcessor processor = 
            new JobSchedulingDataProcessor(isUseContextClassLoader(), isValidating(), isValidatingSchema());

        try {
            processor.processFileAndScheduleJobs(fileName, scheduler, true);
        } catch (Exception e) {
            getLog().error("Error scheduling jobs: " + e.getMessage(), e);
        }
    }

    /** 
     * @see org.quartz.jobs.FileScanListener#fileUpdated(java.lang.String)
     */
    public void fileUpdated(String fileName) {
        if(started)
            processFile();
    }
    
}

// EOF 