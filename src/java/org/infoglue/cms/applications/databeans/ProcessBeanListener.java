package org.infoglue.cms.applications.databeans;

import java.io.File;

/**
 * This interface defines what messages to expect from a ProcessBean.

 * @author Mattias Bogeblad
 *
 */
public interface ProcessBeanListener 
{
	public void processUpdated(String eventDescription);
	public void processUpdatedLastDescription(String eventDescription);
	public void processArtifactsUpdated(String artifactId, String url, File file);
}
