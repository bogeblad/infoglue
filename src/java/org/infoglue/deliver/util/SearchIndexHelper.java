package org.infoglue.deliver.util;

import org.apache.log4j.Logger;
import org.infoglue.cms.controllers.kernel.impl.simple.LuceneController;
import org.infoglue.cms.util.NotificationMessage;

public class SearchIndexHelper implements Runnable
{
    public final static Logger logger = Logger.getLogger(SearchIndexHelper.class.getName());

	private NotificationMessage notificationMessage = null;
	private Long delay = null;
	
	public SearchIndexHelper(NotificationMessage notificationMessage)
	{
		this.notificationMessage = notificationMessage;
	}

	public SearchIndexHelper(NotificationMessage notificationMessage, Long delay)
	{
		this.notificationMessage = notificationMessage;
		this.delay = delay;
	}

	public void run()
	{
		logger.info("Going to index in SIH:" + notificationMessage.getClassName() + ":" + notificationMessage.getObjectId() + ":" + notificationMessage.getType());
		try
		{			
			LuceneController.getController().notify(this.notificationMessage);

			if(delay != null)
				Thread.sleep(delay);

			LuceneController.getController().notifyListeners(true, true);
			logger.info("------------------------------------------->Done indexing in search index helper");
		}
		catch (Exception e) 
		{
			logger.error("Error:" + e.getMessage());
		}
	}
}
