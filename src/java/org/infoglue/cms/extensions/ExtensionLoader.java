package org.infoglue.cms.extensions;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.log4j.Logger;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.deliver.invokers.DecoratedComponentBasedHTMLPageInvoker;

import webwork.config.XMLActionConfigurationExtendor;

public class ExtensionLoader 
{
	private final static Logger logger = Logger.getLogger(ExtensionLoader.class.getName());

	public void startExtensions() 
	{
		try 
		{
			logger.info("Starting extension in:" + CmsPropertyHandler.getContextRootPath());
			String extensionBasePath = CmsPropertyHandler.getContextRootPath() + "WEB-INF" + File.separator + "libextensions";
			File extensionBaseFile = new File(extensionBasePath);
			extensionBaseFile.mkdirs();
			File[] extensionFiles = extensionBaseFile.listFiles();
			for(File extensionFile : extensionFiles)
			{
				if(extensionFile.getName().endsWith(".jar"))
				{
					//ClassLoaderUtil.addFile(extensionFile.getPath());
					logger.info("extensionFile:" + extensionFile.getPath());	
				
					String extensionDirName = extensionFile.getName().replaceAll(".jar", "");

					File extensionDir = new File(CmsPropertyHandler.getContextRootPath() + "extensions" + File.separator + extensionDirName);
					extensionDir.delete();
					extensionDir.mkdirs();

					FileHelper.unjarFile(extensionFile, extensionDir.getPath(), new String[]{".class"});
					logger.info("Unpacking resource into cms directory extensions/" + extensionDirName);
					
					Set<String> actionClassNames = new HashSet<String>();
					File customActionsXmlFile = new File(extensionDir.getPath() + File.separator + "actions.xml");
					if(customActionsXmlFile.exists() && customActionsXmlFile.length() > 0)
					{
						String customActionsXML = FileHelper.getFileAsStringOpt(customActionsXmlFile);
						logger.info("customActionsXML:" + customActionsXML);
						customActionsXML = customActionsXML.replaceAll("\\$extensionPath", "/extensions" + File.separator + extensionDirName);
						logger.info("customActionsXML:" + customActionsXML);
						//String customAction = "<action name=\"org.infoglue.cms.applications.managementtool.actions.ViewMySettingsAction\" alias=\"ViewEcommerce\"><view name=\"success\">viewEcommerce.vm</view><view name=\"error\">/cms/managementtool/error.vm</view></action>";
						new XMLActionConfigurationExtendor().getMappingsFromString(customActionsXML, actionClassNames);
					}
					
					URL url = extensionFile.toURL();
					URLClassLoader classLoader = new URLClassLoader(new URL[]{url}, this.getClass().getClassLoader());
					
					JarInputStream jarFileInputStream = new JarInputStream(new FileInputStream (extensionFile));
					JarEntry jarEntry;

					while(true) 
					{
						jarEntry = jarFileInputStream.getNextJarEntry ();
						if(jarEntry == null)
						{
							break;
						}
						
						if(jarEntry.getName ().endsWith (".class")) 
						{
							String className = jarEntry.getName().replaceAll("/", "\\.").replaceAll(".class", "");
						
							logger.info("Found " + className);
							Class c = classLoader.loadClass(className);
							boolean isOk = InfoglueExtension.class.isAssignableFrom(c);
							logger.info("isOk:" + isOk + " for " + className);
							if(isOk)
							{
								logger.info("Adding class:" + className);
								InfoglueExtension extension = (InfoglueExtension)c.newInstance();
								extension.init();
							}
						}
					}
					
					for(String actionClassName : actionClassNames)
					{
						logger.info("Found actionClassName:" + actionClassName);
						try
						{
							Class c = classLoader.loadClass(actionClassName);
							logger.info("C:" + c.newInstance());

							Class c2 = Class.forName(actionClassName, true, classLoader);
							logger.info("C2:" + c2.newInstance());
						}
						catch (Exception e) 
						{
							e.printStackTrace();
						}
					}
					
				}
			}
		}
		catch (Throwable t) 
		{
			t.printStackTrace();
		}
	}

}
