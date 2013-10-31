package org.infoglue.deliver.taglib.management;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Logger;
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;
import org.infoglue.deliver.taglib.TemplateControllerTag;

/**
 * Tag that provides an interface to the general settings feature.
 * @author Erik Stenbacka <stenbacka@gmail.com>
 */
public class GeneralSettingTag extends TemplateControllerTag
{
	private static final long serialVersionUID = 774587456364945754L;
	private static final Logger logger = Logger.getLogger(GeneralSettingTag.class);

	private String key;
	private String defaultValue;

	@Override
	public int doEndTag() throws JspException
	{
		TemplateController tc = getController();
		if (logger.isInfoEnabled())
		{
			logger.info("Getting value for key <" + key + ">. SiteNode.id: " + tc.getSiteNodeId() + ". Component.id: " + tc.getComponentContentId());
		}

		produceResult(tc.getGeneralSetting(key, defaultValue));

		this.key = null;
		this.defaultValue = null;
		return EVAL_PAGE;
	}

	public void setKey(String key) throws JspException
	{
		this.key = evaluateString("GeneralSettingTag", "key", key);
	}

	public void setDefault(String defaultValue) throws JspException
	{
		this.defaultValue = evaluateString("GeneralSettingTag", "defaultValue", defaultValue);
	}
}
