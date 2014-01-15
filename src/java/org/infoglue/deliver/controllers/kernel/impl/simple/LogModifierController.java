package org.infoglue.deliver.controllers.kernel.impl.simple;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.infoglue.cms.controllers.kernel.impl.simple.BaseController;
import org.infoglue.cms.entities.kernel.BaseEntityVO;

public class LogModifierController extends BaseController
{
	private static final Logger classLogger = Logger.getLogger(LogModifierController.class);
	private static LogModifierController controller;

	private Map<Logger, ModificationInformation> currentModifications;
	private Appender debugAppender;
	private Map<Integer, Level> levels;

	private static synchronized void initController()
	{
		if (controller == null)
		{
			controller = new LogModifierController();
		}
	}

	public static LogModifierController getController()
	{
		if (controller == null)
		{
			initController();
		}
		return controller;
	}

	private LogModifierController()
	{
		this.currentModifications = new HashMap<Logger, ModificationInformation>();
		initDebugAppender();
	}

	private void initDebugAppender()
	{
		if (debugAppender == null)
		{
			debugAppender = Logger.getLogger("org.infoglue.debug-dummy").getAppender("INFOGLUE-DEBUG");
			if (debugAppender == null)
			{
				classLogger.warn("Did not find a debug appender. There should be an appender named INFOGLUE-DEBUG and it should be referenced by a category named org.infoglue.debug-dummy. Will use a Console appender instead.");
				Layout layout = new PatternLayout("%d{dd MMM yyyy HH:mm:ss.SSS} [Debug log] [%-5p] [%t] [%c] - %m%n");
				ConsoleAppender appender = new ConsoleAppender(layout);
				appender.setThreshold(Level.TRACE);
				debugAppender = appender;
			}
		}
	}

	private void populateLevels()
	{
		classLogger.debug("Populating levels");
		levels = new TreeMap<Integer, Level>();
		levels.put(Level.TRACE.toInt(), Level.TRACE);
		levels.put(Level.DEBUG.toInt(), Level.DEBUG);
		levels.put(Level.INFO.toInt(), Level.INFO);
		levels.put(Level.WARN.toInt(), Level.WARN);
		levels.put(Level.ERROR.toInt(), Level.ERROR);
		levels.put(Level.FATAL.toInt(), Level.FATAL);
		levels.put(Level.OFF.toInt(), Level.OFF);
	}

	public Map<Integer, Level> getLevels()
	{
		if (levels == null)
    	{
    		populateLevels();
    	}
		return levels;
	}

	public void changeLogLevel(String loggerName, Integer loggerLevel)
	{
		Logger logger = Logger.getLogger(loggerName);
		Level level = loggerLevel == null ? null : Level.toLevel(loggerLevel);
		changeLogLevel(logger, level);
	}

	public void changeLogLevel(String loggerName, Level level)
	{
		Logger logger = Logger.getLogger(loggerName);
		changeLogLevel(logger, level);
	}

	public void changeLogLevel(Logger logger, Integer loggerLevel)
	{
		Level level = loggerLevel == null ? null : Level.toLevel(loggerLevel);
		changeLogLevel(logger, level);
	}

	public synchronized void changeLogLevel(Logger logger, Level level)
	{
		if (logger == null)
		{
			classLogger.warn("Tried to change logging level without the required information. Logger: " + logger + ". Level " + level);
		}
		else
		{
			if (level == null)
			{
				clearModifications(logger);
			}
			else
			{
				ModificationInformation currentState = currentModifications.get(logger);
				if (currentState == null)
				{
					classLogger.info("Logger has not been modified. Logger: " + logger.getName() + ". New level: " + level);
					currentState = new ModificationInformation();
					currentState.originalLevel = logger.getLevel();
					currentState.currentLevel = level;
					logger.setLevel(level);
					currentModifications.put(logger, currentState);

					logger.addAppender(debugAppender);
				}
				else
				{
					classLogger.info("Logger has been modified. Logger: " + logger.getName() + ". New level: " + level);
					currentState.currentLevel = level;
					logger.setLevel(level);
				}
			}
		}
	}

	public void clearModifications(String loggerName)
	{
		clearModifications(Logger.getLogger(loggerName));
	}

	public synchronized void clearModifications(Logger logger)
	{
		ModificationInformation currentState = currentModifications.get(logger);
		if (currentState == null)
		{
			classLogger.info("No modifications to clear in logger. Logger: " + logger.getName());
		}
		else
		{
			classLogger.debug("Clearing modifications for Logger. Logger: " + logger.getName() + ". Modificatons: " + currentState);
			logger.setLevel(currentState.originalLevel);
			logger.removeAppender(debugAppender);
			currentModifications.remove(logger);
		}
	}

	public Set<Logger> getCurrentModifications()
	{
		return currentModifications.keySet();
	}

	@Override
	public BaseEntityVO getNewVO()
	{
		return null;
	}

	private static class ModificationInformation
	{
		Level currentLevel;
		Level originalLevel;

		@Override
		public String toString()
		{
			return "[currentLevel: " + currentLevel + ", originalLevel: " + originalLevel + "]";
		}
	}

}
