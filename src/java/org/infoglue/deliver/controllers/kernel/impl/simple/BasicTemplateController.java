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

package org.infoglue.deliver.controllers.kernel.impl.simple;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.exolab.castor.jdo.Database;
import org.infoglue.cms.applications.common.VisualFormatter;
import org.infoglue.cms.applications.databeans.ReferenceBean;
import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryConditions;
import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
import org.infoglue.cms.controllers.kernel.impl.simple.DigitalAssetController;
import org.infoglue.cms.controllers.kernel.impl.simple.ExportImportController;
import org.infoglue.cms.controllers.kernel.impl.simple.ExtendedSearchController;
import org.infoglue.cms.controllers.kernel.impl.simple.ExtendedSearchCriterias;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.GroupPropertiesController;
import org.infoglue.cms.controllers.kernel.impl.simple.InfoGluePrincipalControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RegistryController;
import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.RolePropertiesController;
import org.infoglue.cms.controllers.kernel.impl.simple.SearchController;
import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
import org.infoglue.cms.controllers.kernel.impl.simple.WorkflowController;
import org.infoglue.cms.entities.content.Content;
import org.infoglue.cms.entities.content.ContentVO;
import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.entities.content.DigitalAssetVO;
import org.infoglue.cms.entities.management.ContentTypeAttribute;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.entities.structure.SiteNode;
import org.infoglue.cms.entities.structure.SiteNodeVO;
import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.io.FileHelper;
import org.infoglue.cms.security.AuthenticationModule;
import org.infoglue.cms.security.InfoGlueGroup;
import org.infoglue.cms.security.InfoGluePrincipal;
import org.infoglue.cms.security.InfoGlueRole;
import org.infoglue.cms.util.CmsPropertyHandler;
import org.infoglue.cms.util.DesEncryptionHelper;
import org.infoglue.cms.util.DocumentConverterHelper;
import org.infoglue.cms.util.dom.DOMBuilder;
import org.infoglue.cms.util.sorters.SiteNodeComparator;
import org.infoglue.deliver.applications.actions.InfoGlueComponent;
import org.infoglue.deliver.applications.databeans.ComponentBinding;
import org.infoglue.deliver.applications.databeans.ComponentProperty;
import org.infoglue.deliver.applications.databeans.DatabaseWrapper;
import org.infoglue.deliver.applications.databeans.DeliveryContext;
import org.infoglue.deliver.applications.databeans.WebPage;
import org.infoglue.deliver.controllers.kernel.URLComposer;
import org.infoglue.deliver.invokers.DecoratedComponentBasedHTMLPageInvoker;
import org.infoglue.deliver.util.BrowserBean;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.HttpHelper;
import org.infoglue.deliver.util.MathHelper;
import org.infoglue.deliver.util.ObjectConverter;
import org.infoglue.deliver.util.RequestAnalyser;
import org.infoglue.deliver.util.Timer;
import org.infoglue.deliver.util.VelocityTemplateProcessor;
import org.infoglue.deliver.util.charts.ChartHelper;
import org.infoglue.deliver.util.forms.FormHelper;
import org.infoglue.deliver.util.graphics.AdvancedImageRenderer;
import org.infoglue.deliver.util.graphics.ColorHelper;
import org.infoglue.deliver.util.graphics.FOPHelper;
import org.infoglue.deliver.util.graphics.FontHelper;
import org.infoglue.deliver.util.graphics.ImageRenderer;
import org.infoglue.deliver.util.rss.RssHelper;
import org.infoglue.deliver.util.webservices.InfoGlueWebServices;
import org.infoglue.deliver.util.webservices.WebServiceHelper;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

/**
 * This is the most basic template controller supplying the templates using it with
 * methods to fetch contents, structure and other suff needed for a site. Mostly this class just acts as a
 * delegator to other more specialized classes.
 */

public class BasicTemplateController implements TemplateController
{
	private final static DOMBuilder domBuilder = new DOMBuilder();
	
    private final static Logger logger = Logger.getLogger(BasicTemplateController.class.getName());

	private URLComposer urlComposer = null; 
	
	public static final String META_INFO_BINDING_NAME 					= "Meta information";
	public static final String TEMPLATE_ATTRIBUTE_NAME   				= "Template";
	public static final String TITLE_ATTRIBUTE_NAME     		 		= "Title";
	public static final String NAV_TITLE_ATTRIBUTE_NAME 		 		= "NavigationTitle";
	/*
	protected static final String DISABLE_PAGE_CACHE_ATTRIBUTE_NAME		= "DisablePageCache";
	protected static final String PAGE_CONTENT_TYPE_ATTRIBUTE_NAME		= "ContentType";
	protected static final String ENABLE_PAGE_PROTECTION_ATTRIBUTE_NAME = "ProtectPage";
	protected static final String DISABLE_EDIT_ON_SIGHT_ATTRIBUTE_NAME	= "DisableEditOnSight";
	*/
	protected static final boolean USE_LANGUAGE_FALLBACK        = true;
	protected static final boolean DO_NOT_USE_LANGUAGE_FALLBACK = false;
	protected static final boolean USE_INHERITANCE 				= true;
	protected static final boolean DO_NOT_USE_INHERITANCE 		= false;

	protected Integer siteNodeId = null;
	protected Integer languageId = null;
	protected Integer contentId  = null;
	
	protected HttpServletRequest request = null;
	protected DeliveryContext deliveryContext = null;
	
	protected BrowserBean browserBean = null;
	
	protected NodeDeliveryController nodeDeliveryController = null;
	protected ContentDeliveryController contentDeliveryController = null;
	protected IntegrationDeliveryController integrationDeliveryController = null;
	
	protected ComponentLogic componentLogic = null;

	protected InfoGluePrincipal infoGluePrincipal = null;
	
	// For adding objects to be used in subsequent parsing
	// like getParsedContentAttribute, include, etc
	protected Map templateLogicContext = new HashMap();
	protected boolean persistedContext = false;

	protected DatabaseWrapper databaseWrapper = null;
	
	private boolean threatFoldersAsContents = false;
	
	/**
	 * The constructor for the templateController. It should be used to initialize the 
	 * templateController for efficient use.
	 */
	
	public BasicTemplateController(DatabaseWrapper databaseWrapper, InfoGluePrincipal infoGluePrincipal)
	{
	    this.databaseWrapper = databaseWrapper;
	    this.infoGluePrincipal = infoGluePrincipal;
	    this.urlComposer = URLComposer.getURLComposer(); 
	}

	public void clear()
	{
		try
		{
			this.databaseWrapper = null;
		    this.infoGluePrincipal = null;
		    this.urlComposer = null; 
		    templateLogicContext = null;
		    componentLogic = null;
		    browserBean = null;
		    nodeDeliveryController = null;
			contentDeliveryController = null;
			integrationDeliveryController = null;
			request = null;
			deliveryContext = null;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
     * Gets the transaction the controller should work within. This is to limit the number of connections we use. 
     */
	
    public Database getDatabase() throws SystemException
    {
        if(this.databaseWrapper.getDatabase() == null || this.databaseWrapper.getDatabase().isClosed() || !this.databaseWrapper.getDatabase().isActive())
        {
            beginTransaction();
        }

        return this.databaseWrapper.getDatabase();
    }

	public DatabaseWrapper getDatabaseWrapper()
	{
		return databaseWrapper;
	}

	/**
     * Commits and reopens a database object so we don't have to long transaction. 
     */
	
    public void commitDatabase() throws SystemException
    {
    	logger.debug("Committing database in the middle of the run....");
    	try
		{
    		this.databaseWrapper.getDatabase().commit();
		    this.databaseWrapper.getDatabase().close();
		    logger.info("Closed transaction...");
		    this.databaseWrapper.setDatabase(CastorDatabaseService.getDatabase());
		    //this.databaseWrapper = new DatabaseWrapper(CastorDatabaseService.getDatabase());
		    beginTransaction();
		    logger.info("Begun a new transaction...");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SystemException("An error occurred when we tried to commit an transaction. Reason:" + e.getMessage(), e);    
		}
    }

    /**
     * Commit transactions so far so a different call can be made which otherwise gets a deadlock.
     */

    public void closeTransaction() throws SystemException
	{
        try
		{
		    this.databaseWrapper.getDatabase().commit();
			//this.db.close();
		    logger.info("Closed transaction...");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SystemException("An error occurred when we tried to commit an transaction. Reason:" + e.getMessage(), e);    
		}
	}

    /**
     * Starts a new transaction so a different call can be made.
     */
    
    private void beginTransaction() throws SystemException
	{
	    try
		{
			this.databaseWrapper.getDatabase().begin();
			logger.info("Started new transaction...");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SystemException("An error occurred when we tried to begin an transaction. Reason:" + e.getMessage(), e);    
		}
	}
 
	
	
    
	/** 
	 * Add objects to be used in subsequent parsing
	 * like getParsedContentAttribute, include, etc 
	 */
	public void addToContext(String name, Object object)
	{
		templateLogicContext.put(name, object);
	}
	
	/** 
	 * Add objects to be used in subsequent parsing
	 * like getParsedContentAttribute, include, etc
	 * this method adds all objects in the map to
	 * the templateLogicContext, the method is protected
	 * and is used by getTemplateController to pass on 
	 * the current context to the new controller if
	 * persistedContext is true.  
	 */
	protected void addToContext(Map context)
	{
		templateLogicContext.putAll(context);
	}	
	
	/** 
	 * Gets objects from the context
	 */
	public Object getFromContext(String name)
	{
		return templateLogicContext.get(name);
	}
	
	/**
	 * Setter for the template to get all the parameters from the user.
	 */
	
	public void setStandardRequestParameters(Integer siteNodeId, Integer languageId, Integer contentId)
	{
		this.siteNodeId = siteNodeId;
		this.languageId = languageId;
		this.contentId  = contentId;
	}	

	/**
	 * Setter for the template to get all the parameters from the user.
	 */

	public void setHttpRequest(HttpServletRequest request)
	{
		this.request = request;
	}
	

	/**
	 * Setter for the bean which contains information about the users browser.
	 */
	 
	public void setBrowserBean(BrowserBean browserBean)
	{
		this.browserBean = browserBean;
	}

	/**
	 * Getter for the template attribute name.
	 */
	
	public String getTemplateAttributeName()
	{
		return TEMPLATE_ATTRIBUTE_NAME;
	}
	
	/**
	 * Getter for the siteNodeId
	 */
	
	public Integer getSiteNodeId()
	{
		return this.siteNodeId;
	}
	
	/**
	 * Getter for the languageId
	 */
	
	public Integer getLanguageId()
	{
		return this.languageId;
	}

	

	/**
	 * Getter for the contentId
	 */
	
	public Integer getContentId()
	{
		return this.contentId;
	}
	
	/**
	 * This method gets a component logic helper object.
	 */
	
	public ComponentLogic getComponentLogic()
	{
		return this.componentLogic;
	}
	
    /**
     * This method gets the contentId of the component.
     */
    public Integer getComponentContentId()
    {
    	return getComponentLogic().getInfoGlueComponent().getContentId();
    }

	/**
	 * This method gets a component logic helper object.
	 */
	
	public void setComponentLogic(ComponentLogic componentLogic)
	{
		this.componentLogic = componentLogic;
	}


	/**
	 * This method gets the formatter object that helps with formatting of data.
	 */
	
	public VisualFormatter getVisualFormatter()
	{
		return new VisualFormatter();
	}

	/**
	 * This method gets the color utility.
	 */
	
	public ColorHelper getColorHelper()
	{
		return new ColorHelper();
	}

	/**
	 * This method gets the form utility.
	 */
	
	public FormHelper getFormHelper()
	{
		return new FormHelper();
	}
	
	/**
	 * This method gets the color utility.
	 */
	
	public FontHelper getFontHelper()
	{
		return new FontHelper();
	}

	/**
	 * This method gets the math utility.
	 */
	
	public MathHelper getMathHelper()
	{
		return new MathHelper();
	}
	
	/**
	 * This method gets the math utility.
	 */
	
	public HttpHelper getHTTPHelper()
	{
		return new HttpHelper();
	}
	
	/**
	 * This method gets the math utility.
	 */
	
	public ChartHelper getChartHelper()
	{
		return new ChartHelper(this);
	}
	
	/**
	 * This method gets the webservice utility.
	 */
	
	public WebServiceHelper getWebServiceHelper()
	{
		return new WebServiceHelper();
	}
	
	/**
	 * This method gets the webservice utility which has special api:s to call the infoglue system.
	 */
	
	public InfoGlueWebServices getInfoGlueWebServiceHelper()
	{
		return new InfoGlueWebServices();
	}
	
	/**
	 * This method gets the NumberFormat instance with the proper locale.
	 */
	public NumberFormat getNumberFormatHelper() throws SystemException
	{
	 	return NumberFormat.getInstance	(
	 			LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(getDatabase(), this.languageId)
	 		);
	}

	/**
	 * This method gets the DesEncryptionHelper.
	 */
	
	public DesEncryptionHelper getDesEncryptionHelper()
	{
		return new DesEncryptionHelper();
	}

	/**
	 * This method gets the rss utility.
	 */
	
	public RssHelper getRssHelper()
	{
		return new RssHelper();
	}
	
	/**
	 * This method gets the rss utility.
	 */
	
	public DocumentConverterHelper getDocumentTransformerHelper()
	{
		return new DocumentConverterHelper();
	}

	/**
	 * This method gets the object converter utility.
	 */
	
	public ObjectConverter getObjectConverter()
	{
		return new ObjectConverter();
	}

	
	/** 
	 * This method delivers a map with all unparsed content attributes
	 */
	public Map getContentAttributes(Integer contentId)
	{
	    Map result = new HashMap();
	    ContentTypeDefinitionVO typeDefinitionVO = getContentTypeDefinitionVO(contentId);
	    List contentAttributes = getContentAttributes(typeDefinitionVO.getSchemaValue());
	    
	    for(Iterator i=contentAttributes.iterator();i.hasNext();)
	    {
	        ContentTypeAttribute contentTypeAttribute = (ContentTypeAttribute) i.next();
	        String name = contentTypeAttribute.getName();
	        result.put(name, getContentAttribute(contentId, name));
	    }
	    return result;
	} 
	
	/** 
	 * This method delivers a map with all parsed content attributes
	 */
	public Map getParsedContentAttributes(Integer contentId)
	{
	    Map result = new HashMap();
	    ContentTypeDefinitionVO typeDefinitionVO = getContentTypeDefinitionVO(contentId);
	    List contentAttributes = getContentAttributes(typeDefinitionVO.getSchemaValue());
	    
	    for(Iterator i=contentAttributes.iterator();i.hasNext();)
	    {
	        ContentTypeAttribute contentTypeAttribute = (ContentTypeAttribute) i.next();
	        String name = contentTypeAttribute.getName();
	        result.put(name, getParsedContentAttribute(contentId, name));
	    }
	    return result;
	} 
	
	
	/**
	 * Getter for the current content
	 */
	
	public ContentVO getContent()
	{
		ContentVO content = null;

		try
		{
			content = ContentDeliveryController.getContentDeliveryController().getContentVO(getDatabase(), this.contentId, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get the current content:" + e.getMessage());
		}

		return content;
	}
	
	/**
	 * Getter for the current content
	 */
	
	public ContentVO getContent(Integer contentId)
	{
		ContentVO content = null;

		try
		{
			content = ContentDeliveryController.getContentDeliveryController().getContentVO(getDatabase(), contentId, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get the content with id " + contentId + ":" + e.getMessage());
		}

		return content;
	}

    /**
     * Getter content with a certain path in a repository
     */
    public ContentVO getContentWithPath(Integer repositoryId, String path)
    {
    	ContentVO content = null;

		try
		{
			content = ContentDeliveryController.getContentDeliveryController().getContentWithPath(repositoryId, path, getPrincipal(), getDatabase());
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get the content with path " + path + " on repository " + repositoryId + ":" + e.getMessage());
		}

		return content;    	
    }

    /**
     * Getter content with a certain path in a repository
     */
    public Boolean getHasContentVersionInState(Integer contentId, Integer languageId, Integer stateId)
    {
    	Boolean hasContentVersionInState = null;

		try
		{
			ContentVersionVO cvo = ContentDeliveryController.getContentDeliveryController().getContentVersionVOInState(contentId, languageId, stateId, getDatabase(), deliveryContext, infoGluePrincipal);
			hasContentVersionInState = (cvo == null ? false : true);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to execute getHasContentVersionInState with " + contentId + ":" + languageId + ":" + stateId + ":" + e.getMessage());
		}

		return hasContentVersionInState;    	
    }

	/**
	 * Getter for the most recent contentVersion based on a contentVersionId
	 */
	
	public ContentVersionVO getContentVersionById(Integer contentVersionId)
	{
		ContentVersionVO contentVersionVO = null;

		try
		{
		    contentVersionVO = ContentVersionController.getContentVersionController().getContentVersionVOWithId(contentVersionId, getDatabase());
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get the contentVersion with contentVersionId " + contentVersionId + ":" + e.getMessage(), e);
		}

		return contentVersionVO;
	}

	/**
	 * Getter for the most recent contentVersion on a content
	 */
	
	public ContentVersionVO getContentVersion(Integer contentId)
	{
		ContentVersionVO contentVersionVO = null;

		try
		{
		    contentVersionVO = ContentDeliveryController.getContentDeliveryController().getContentVersionVO(getDatabase(), this.siteNodeId, contentId, this.languageId, true, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get the contentVersion with contentId " + contentId + ":" + e.getMessage(), e);
		}

		return contentVersionVO;
	}

	/**
	 * Getter for the most recent contentVersion on a content
	 */
	
	public ContentVersionVO getContentVersion(Integer contentId, Integer languageId, boolean useLanguageFallback)
	{
		ContentVersionVO contentVersionVO = null;

		try
		{
		    contentVersionVO = ContentDeliveryController.getContentDeliveryController().getContentVersionVO(getDatabase(), this.siteNodeId, contentId, languageId, useLanguageFallback, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get the contentVersion with contentId " + contentId + ":" + e.getMessage(), e);
		}

		return contentVersionVO;
	}
	
	/**
	 * Getter for all content versions of a content in a certain language or for all versions no matter language if languageId is null.
	 */
	
	public List getContentVersions(Integer contentId, Integer languageId)
	{
		List contentVersions = new ArrayList();

		try
		{
			contentVersions = ContentDeliveryController.getContentDeliveryController().getContentVersionVOList(getDatabase(), this.siteNodeId, contentId, languageId, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.warn("An error occurred trying to get the contentVersion with contentId " + contentId + ":" + e.getMessage(), e);
		}

		return contentVersions;
	}

	/**
	 * Returns the logged in user - that is the one currently looking at the page
	 */
	public InfoGluePrincipal getPrincipal()
	{
	    return this.infoGluePrincipal;
	}

	/**
	 * Returns a list of InfoGlueRoles
	 */
	public List getAllRoles() throws Exception
	{
	    return RoleControllerProxy.getController(this.getDatabase()).getAllRoles();
	}

	/**
	 * Returns a list of InfoGlueGroups
	 */
	public List getAllGroups() throws Exception
	{
	    return GroupControllerProxy.getController(this.getDatabase()).getAllGroups();
	}

    /**
     * This method returns the InfoGlue Principal requested
     * 
     * @param userName
     */
    
    public InfoGluePrincipal getPrincipal(String userName)
    {
        if(userName == null || userName.equals(""))
            return null;
        
        InfoGluePrincipal infoGluePrincipal = null;
        
        try
        {
            infoGluePrincipal = UserControllerProxy.getController(getDatabase()).getUser(userName);
        }
        catch(Exception e)
        {
            logger.warn("An error occurred when getting principal:" + e.getMessage(), e);
        }
        
        return infoGluePrincipal;
    }

    /**
     * Getting all assets for a certain user
     */
    public List<DigitalAssetVO> getPrincipalAssets(InfoGluePrincipal infoGluePrincipal) throws Exception
    {
    	return InfoGluePrincipalControllerProxy.getController().getPrincipalAssets(this.getDatabase(), infoGluePrincipal, this.languageId);
    }

    /**
     * Getting all assets for a certain user
     */
    public List<DigitalAssetVO> getPrincipalAssets(InfoGluePrincipal infoGluePrincipal, Integer languageId) throws Exception
    {
    	return InfoGluePrincipalControllerProxy.getController().getPrincipalAssets(this.getDatabase(), infoGluePrincipal, languageId);
    }

    /**
     * Getting all assets for a certain user
     */
    public DigitalAssetVO getPrincipalAsset(InfoGluePrincipal infoGluePrincipal, String assetKey) throws Exception
    {
    	return InfoGluePrincipalControllerProxy.getController().getPrincipalAsset(this.getDatabase(), infoGluePrincipal, this.languageId, assetKey);
    }

    /**
     * Getting all assets for a certain user in a certain language
     */
    public DigitalAssetVO getPrincipalAsset(InfoGluePrincipal infoGluePrincipal, String assetKey, Integer languageId) throws Exception
    {
    	return InfoGluePrincipalControllerProxy.getController().getPrincipalAsset(this.getDatabase(), infoGluePrincipal, languageId, assetKey);
    }

	/**
	 * Getting a property for the current Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public String getPrincipalPropertyValue(String propertyName)
	{
		return getPrincipalPropertyValue(propertyName, true);
	}

	public String getPrincipalPropertyValue(String propertyName, Integer languageId)
	{
		return getPrincipalPropertyValue(propertyName, true, languageId);
	}

	/**
	 * Getting a property for the current Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public Map getPrincipalPropertyHashValues(String propertyName)
	{
		return getPrincipalPropertyHashValues(propertyName, true);
	}

	public Map getPrincipalPropertyHashValues(String propertyName, Integer languageId)
	{
		return getPrincipalPropertyHashValues(propertyName, true, languageId);
	}

	/**
	 * Getting a property for the current Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public String getPrincipalPropertyValue(InfoGluePrincipal infoGluePrincipal, String propertyName)
	{
		return getPrincipalPropertyValue(infoGluePrincipal, propertyName, true);
	}

	public String getPrincipalPropertyValue(InfoGluePrincipal infoGluePrincipal, String propertyName, Integer languageId)
	{
		return getPrincipalPropertyValue(infoGluePrincipal, propertyName, true, languageId);
	}

	/**
	 * Getting a property for a Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public Map getPrincipalPropertyHashValues(InfoGluePrincipal infoGluePrincipal, String propertyName)
	{
		return getPrincipalPropertyHashValues(infoGluePrincipal, propertyName, true);
	}

	public Map getPrincipalPropertyHashValues(InfoGluePrincipal infoGluePrincipal, String propertyName, Integer languageId)
	{
		return getPrincipalPropertyHashValues(infoGluePrincipal, propertyName, true, languageId);
	}
	
	/**
	 * Getting a property for a Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public String getPrincipalPropertyValue(InfoGluePrincipal infoGluePrincipal, String propertyName, boolean escapeSpecialCharacters)
	{
		String value = "";
		
		try
		{
			value = getPrincipalPropertyValue(infoGluePrincipal, propertyName, escapeSpecialCharacters, false);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}	

	/**
	 * Getting a property for a Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public String getPrincipalPropertyValue(InfoGluePrincipal infoGluePrincipal, String propertyName, boolean escapeSpecialCharacters, Integer languageId)
	{
		String value = "";
		
		try
		{
			value = getPrincipalPropertyValue(infoGluePrincipal, propertyName, escapeSpecialCharacters, false, languageId);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}	

	/**
	 * Getting a property for a Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public String getPrincipalPropertyValue(InfoGluePrincipal infoGluePrincipal, String propertyName, boolean escapeSpecialCharacters, boolean findLargestValue)
	{
		String value = "";
		
		try
		{
			value = InfoGluePrincipalControllerProxy.getController().getPrincipalPropertyValue(this.getDatabase(), infoGluePrincipal, propertyName, this.languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, escapeSpecialCharacters, findLargestValue);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}	

	public String getPrincipalPropertyValue(InfoGluePrincipal infoGluePrincipal, String propertyName, boolean escapeSpecialCharacters, boolean findLargestValue, Integer languageId)
	{
		String value = "";
		
		try
		{
			value = InfoGluePrincipalControllerProxy.getController().getPrincipalPropertyValue(this.getDatabase(), infoGluePrincipal, propertyName, languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, escapeSpecialCharacters, findLargestValue);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}	

	
	/**
	 * Getting a property for the current Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public String getPrincipalPropertyValue(String propertyName, boolean escapeSpecialCharacters)
	{
		String value = "";
		
		try
		{
		    InfoGluePrincipal infoGluePrincipal = this.getPrincipal();
		    value = getPrincipalPropertyValue(propertyName, escapeSpecialCharacters, false);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}

	public String getPrincipalPropertyValue(String propertyName, boolean escapeSpecialCharacters, Integer languageId)
	{
		String value = "";
		
		try
		{
		    InfoGluePrincipal infoGluePrincipal = this.getPrincipal();
		    value = getPrincipalPropertyValue(propertyName, escapeSpecialCharacters, false, languageId);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}


	/**
	 * Getting a property for the current Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public String getPrincipalPropertyValue(String propertyName, boolean escapeSpecialCharacters, boolean findLargestValue)
	{
		String value = "";
		
		try
		{
		    InfoGluePrincipal infoGluePrincipal = this.getPrincipal();
		    value = InfoGluePrincipalControllerProxy.getController().getPrincipalPropertyValue(this.getDatabase(), infoGluePrincipal, propertyName, this.languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, escapeSpecialCharacters, findLargestValue);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}

	public String getPrincipalPropertyValue(String propertyName, boolean escapeSpecialCharacters, boolean findLargestValue, Integer languageId)
	{
		String value = "";
		
		try
		{
		    InfoGluePrincipal infoGluePrincipal = this.getPrincipal();
		    value = InfoGluePrincipalControllerProxy.getController().getPrincipalPropertyValue(this.getDatabase(), infoGluePrincipal, propertyName, languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, escapeSpecialCharacters, findLargestValue);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}

	
	/**
	 * Getting a property for a Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public Map getPrincipalPropertyHashValues(InfoGluePrincipal infoGluePrincipal, String propertyName, boolean escapeSpecialCharacters)
	{
		Map value = new HashMap();
		
		try
		{
		    value = InfoGluePrincipalControllerProxy.getController().getPrincipalPropertyHashValues(this.getDatabase(), infoGluePrincipal, propertyName, this.languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, escapeSpecialCharacters);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}	

	public Map getPrincipalPropertyHashValues(InfoGluePrincipal infoGluePrincipal, String propertyName, boolean escapeSpecialCharacters, Integer languageId)
	{
		Map value = new HashMap();
		
		try
		{
		    value = InfoGluePrincipalControllerProxy.getController().getPrincipalPropertyHashValues(this.getDatabase(), infoGluePrincipal, propertyName, languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, escapeSpecialCharacters);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}	

	
	/**
	 * Getting a property for the current Principal - used for personalisation. 
	 * This method starts with getting the property on the user and if it does not exist we check out the
	 * group-properties as well.
	 */
	
	public Map getPrincipalPropertyHashValues(String propertyName, boolean escapeSpecialCharacters)
	{
		Map value = new HashMap();
		
		try
		{
			InfoGluePrincipal infoGluePrincipal = this.getPrincipal();
			value = getPrincipalPropertyHashValues(this.infoGluePrincipal, propertyName, escapeSpecialCharacters);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}	

	public Map getPrincipalPropertyHashValues(String propertyName, boolean escapeSpecialCharacters, Integer languageId)
	{
		Map value = new HashMap();
		
		try
		{
			InfoGluePrincipal infoGluePrincipal = this.getPrincipal();
			value = getPrincipalPropertyHashValues(this.infoGluePrincipal, propertyName, escapeSpecialCharacters, languageId);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}	

	
	public boolean getHasPrincipalGroup(InfoGluePrincipal infoGluePrincipal, String groupName)
	{
	    boolean isValid = false;
	    Iterator groupsIterator = infoGluePrincipal.getGroups().iterator();
	    while(groupsIterator.hasNext())
	    {
	        InfoGlueGroup infoglueGroup = (InfoGlueGroup)groupsIterator.next();
	        if(infoglueGroup.getName().equalsIgnoreCase(groupName))
	        {
	            isValid = true;
	        }
	    }
	    
	    return isValid;
	}
	
	/**
	 * Getting all related contents for the current Principals group - used for personalisation. 
	 */
	
	public List getPrincipalGroupRelatedContents(String groupName, String propertyName)
	{
	    return getPrincipalGroupRelatedContents(this.infoGluePrincipal, groupName, propertyName);
	}
	
	/**
	 * Getting all related contents for the current Principals group - used for personalisation. 
	 */
	
	public List getPrincipalGroupRelatedContents(InfoGluePrincipal infoGluePrincipal, String groupName, String propertyName)
	{
		List contents = new ArrayList();
		
		try
		{
		    if(getHasPrincipalGroup(infoGluePrincipal, groupName))
		        contents = GroupPropertiesController.getController().getRelatedContents(groupName, this.languageId, propertyName);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return contents;
	}	

	/**
	 * Getting related pages for a Principals group - used for personalisation. 
	 */
	
	public List getPrincipalGroupRelatedPages(String groupName, String propertyName)
	{
	    return getPrincipalGroupRelatedPages(this.infoGluePrincipal, groupName, propertyName);
	}

	/**
	 * Getting related pages for a Principals group - used for personalisation. 
	 */
	
	public List getPrincipalGroupRelatedPages(String groupName, String propertyName, boolean escapeHTML)
	{
	    return getPrincipalGroupRelatedPages(this.infoGluePrincipal, groupName, propertyName, escapeHTML);
	}


	/**
	 * Getting related pages for a Principals group - used for personalisation. 
	 */
	
	public List getPrincipalGroupRelatedPages(InfoGluePrincipal infoGluePrincipal, String groupName, String propertyName)
	{
	    return getPrincipalGroupRelatedPages(this.infoGluePrincipal, groupName, propertyName, false);
	}
	
	/**
	 * Getting related pages for a Principals group - used for personalisation. 
	 */
	
	public List getPrincipalGroupRelatedPages(InfoGluePrincipal infoGluePrincipal, String groupName, String propertyName, boolean escapeHTML)
	{
		List pages = new ArrayList();
		
		try
		{
		    if(getHasPrincipalGroup(infoGluePrincipal, groupName))
		    {
			    List siteNodeVOList = GroupPropertiesController.getController().getRelatedSiteNodes(groupName, this.languageId, propertyName);
			    
			    Iterator i = siteNodeVOList.iterator();
				while(i.hasNext())
				{
				    SiteNodeVO siteNodeVO = (SiteNodeVO)i.next();
				    try
					{
						WebPage webPage = new WebPage();						
						webPage.setSiteNodeId(siteNodeVO.getSiteNodeId());
						webPage.setLanguageId(this.languageId);
						webPage.setContentId(null);
						webPage.setNavigationTitle(this.nodeDeliveryController.getPageNavigationTitle(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, META_INFO_BINDING_NAME, NAV_TITLE_ATTRIBUTE_NAME, USE_LANGUAGE_FALLBACK, this.deliveryContext, escapeHTML));
						webPage.setMetaInfoContentId(this.nodeDeliveryController.getMetaInfoContentId(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), META_INFO_BINDING_NAME, USE_INHERITANCE, this.deliveryContext));
						webPage.setUrl(this.nodeDeliveryController.getPageUrl(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, this.deliveryContext));
						pages.add(webPage);
					}
					catch(Exception e)
					{
					    logger.info("An error occurred when looking up one of the related pages:" + e.getMessage(), e);
					}
				}
		    }
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return pages;
	}	

	/**
	 * Getting all categories assigned to a property for a Role - used for personalisation. 
	 */
	
	public List getPrincipalGroupCategories(String groupName, String propertyName)
	{
	    return getPrincipalGroupCategories(this.infoGluePrincipal, groupName, propertyName);
	}
	
	/**
	 * Getting all categories assigned to a property for a Role - used for personalisation. 
	 */
	
	public List getPrincipalGroupCategories(InfoGluePrincipal infoGluePrincipal, String groupName, String propertyName)
	{
		List categories = new ArrayList();
		
		try
		{
		    if(getHasPrincipalGroup(infoGluePrincipal, groupName))
		        categories = GroupPropertiesController.getController().getRelatedCategories(groupName, this.languageId, propertyName);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return categories;
	}	

	
	public boolean getHasPrincipalRole(InfoGluePrincipal infoGluePrincipal, String roleName)
	{
	    boolean isValid = false;
	    Iterator rolesIterator = infoGluePrincipal.getRoles().iterator();
	    while(rolesIterator.hasNext())
	    {
	        InfoGlueRole infoglueRole = (InfoGlueRole)rolesIterator.next();
	        if(infoglueRole.getName().equalsIgnoreCase(roleName))
	        {
	            isValid = true;
	        }
	    }
	    
	    return isValid;
	}

	/**
	 * Getting all related contents for the current Principals role - used for personalisation. 
	 */
	
	public List getPrincipalRoleRelatedContents(String roleName, String propertyName)
	{
	    return getPrincipalRoleRelatedContents(this.infoGluePrincipal, roleName, propertyName);
	}
	
	/**
	 * Getting all related contents for the current Principals role - used for personalisation. 
	 */
	
	public List getPrincipalRoleRelatedContents(InfoGluePrincipal infoGluePrincipal, String roleName, String propertyName)
	{
		List contents = new ArrayList();
		
		try
		{
		    if(getHasPrincipalRole(infoGluePrincipal, roleName))
		        contents = RolePropertiesController.getController().getRelatedContents(roleName, this.languageId, propertyName);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return contents;
	}	

	/**
	 * Getting related pages for a Principals role - used for personalisation. 
	 */
	
	public List getPrincipalRoleRelatedPages(String roleName, String propertyName)
	{
	    return getPrincipalRoleRelatedPages(this.infoGluePrincipal, roleName, propertyName);
	}

	/**
	 * Getting related pages for a Principals role - used for personalisation. 
	 */
	
	public List getPrincipalRoleRelatedPages(String roleName, String propertyName, boolean escapeHTML)
	{
	    return getPrincipalRoleRelatedPages(this.infoGluePrincipal, roleName, propertyName, escapeHTML);
	}

	/**
	 * Getting related pages for a Principals role - used for personalisation. 
	 */
	
	public List getPrincipalRoleRelatedPages(InfoGluePrincipal infoGluePrincipal, String roleName, String propertyName)
	{
	    return getPrincipalRoleRelatedPages(this.infoGluePrincipal, roleName, propertyName, false);
	}

	/**
	 * Getting related pages for a Principals role - used for personalisation. 
	 */
	
	public List getPrincipalRoleRelatedPages(InfoGluePrincipal infoGluePrincipal, String roleName, String propertyName, boolean escapeHTML)
	{
		List pages = new ArrayList();
		
		try
		{
		    if(getHasPrincipalRole(infoGluePrincipal, roleName))
		    {
			    List siteNodeVOList = GroupPropertiesController.getController().getRelatedSiteNodes(roleName, this.languageId, propertyName);
			    
			    Iterator i = siteNodeVOList.iterator();
				while(i.hasNext())
				{
				    SiteNodeVO siteNodeVO = (SiteNodeVO)i.next();
				    try
					{
						WebPage webPage = new WebPage();						
						webPage.setSiteNodeId(siteNodeVO.getSiteNodeId());
						webPage.setLanguageId(this.languageId);
						webPage.setContentId(null);
						webPage.setNavigationTitle(this.nodeDeliveryController.getPageNavigationTitle(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, META_INFO_BINDING_NAME, NAV_TITLE_ATTRIBUTE_NAME, USE_LANGUAGE_FALLBACK, this.deliveryContext, escapeHTML));
						webPage.setMetaInfoContentId(this.nodeDeliveryController.getMetaInfoContentId(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), META_INFO_BINDING_NAME, USE_INHERITANCE, this.deliveryContext));
						webPage.setUrl(this.nodeDeliveryController.getPageUrl(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, this.deliveryContext));
						pages.add(webPage);
					}
					catch(Exception e)
					{
					    logger.info("An error occurred when looking up one of the related pages:" + e.getMessage(), e);
					}
				}
		    }
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return pages;
	}	

	/**
	 * Getting all categories assigned to a property for a Group - used for personalisation. 
	 */
	
	public List getPrincipalRoleCategories(String roleName, String propertyName)
	{
	    return getPrincipalRoleCategories(this.infoGluePrincipal, roleName, propertyName);
	}
	
	/**
	 * Getting all categories assigned to a property for a Group - used for personalisation. 
	 */
	
	public List getPrincipalRoleCategories(InfoGluePrincipal infoGluePrincipal, String roleName, String propertyName)
	{
		List categories = new ArrayList();
		
		try
		{
		    if(getHasPrincipalRole(infoGluePrincipal, roleName))
		        categories = GroupPropertiesController.getController().getRelatedCategories(roleName, this.languageId, propertyName);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return categories;
	}	
		
	public InfoGlueGroup getGroup(String groupName)
	{
		InfoGlueGroup group = null;
		
		try
		{
			group = GroupControllerProxy.getController().getGroup(groupName);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get group " + groupName + ":" + e.getMessage(), e);
		}
		
		return group;
	}	

	public Set<InfoGlueGroup> getGroupsByMatchingProperty(String propertyName, String value, Integer languageId, Boolean useLanguageFallback)
	{
		Set<InfoGlueGroup> groups = new HashSet<InfoGlueGroup>();
		
		try
		{
			groups = GroupPropertiesController.getController().getGroupsByMatchingProperty(propertyName, value, languageId, useLanguageFallback, getDatabase());
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get groups based on propertysearch" + propertyName + ":" + value + ":" + languageId + ":" + useLanguageFallback + ":" + e.getMessage(), e);
		}
		
		return groups;
	}	

	public InfoGlueRole getRole(String roleName)
	{
		InfoGlueRole role = null;
		
		try
		{
			role = RoleControllerProxy.getController().getRole(roleName);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get role " + roleName + ":" + e.getMessage(), e);
		}
		
		return role;
	}	

	public Set<InfoGlueRole> getRolesByMatchingProperty(String propertyName, String value, Integer languageId, Boolean useLanguageFallback)
	{
		Set<InfoGlueRole> roles = new HashSet<InfoGlueRole>();
		
		try
		{
			roles = RolePropertiesController.getController().getRolesByMatchingProperty(propertyName, value, languageId, useLanguageFallback, getDatabase());
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get roles based on propertysearch" + propertyName + ":" + value + ":" + languageId + ":" + useLanguageFallback + ":" + e.getMessage(), e);
		}
		
		return roles;
	}	

	public String getGroupPropertyValue(InfoGlueGroup group, String propertyName, Integer languageId, Boolean useLanguageFallback)
	{
		String value = "";
		
		try
		{
			value = GroupPropertiesController.getController().getAttributeValue(group.getName(), languageId, propertyName, this.getDatabase());
			if(useLanguageFallback && (value == null || value.equalsIgnoreCase("")))
			{
				LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(this.getDatabase(), getSiteNodeId());
				value = GroupPropertiesController.getController().getAttributeValue(group.getName(), masterLanguageVO.getId(), propertyName, this.getDatabase());
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from InfoGlueGroup:" + e.getMessage(), e);
		}
		
		return value;
	}	

	public String getRolePropertyValue(InfoGlueRole role, String propertyName, Integer languageId, Boolean useLanguageFallback)
	{
		String value = "";
		
		try
		{
			value = RolePropertiesController.getController().getAttributeValue(role.getName(), languageId, propertyName, this.getDatabase());
			if(useLanguageFallback && (value == null || value.equalsIgnoreCase("")))
			{
				LanguageVO masterLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(this.getDatabase(), getSiteNodeId());
				value = RolePropertiesController.getController().getAttributeValue(role.getName(), masterLanguageVO.getId(), propertyName, this.getDatabase());
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get property " + propertyName + " from infoGluePrincipal:" + e.getMessage(), e);
		}
		
		return value;
	}	
	
	/**
	 * Getter for request-object
	 */
	
	public HttpServletRequest getHttpServletRequest()
	{
		return this.request;
	}


	/**
	 * Getter for request-parameters
	 */

	public Enumeration getRequestParamenterNames()
	{
		return this.request.getParameterNames();
	}
	
	/**
	 * Getter for request-parameter
	 */
	public String getRequestParameter(String parameterName)
	{
		String value = "";
		try
		{
			value = this.request.getParameter(parameterName);
			if(value == null)
				value = "";
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get parameterName " + parameterName + " from request:" + e.getMessage(), e);
		}
		
		return value;
	}	

	/**
	 * Getter for request-parameters
	 */
	public String[] getRequestParameterValues(String parameterName)
	{
		String value[] = null;
		try
		{
			value = this.request.getParameterValues(parameterName);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get parameterName " + parameterName + " from request:" + e.getMessage(), e);
		}
		
		return value;
	}	
	
	/**
	 * Getter for the browserBean which supplies information about the users browser, OS and other stuff.
	 */
	
	public BrowserBean getBrowserBean()
	{
		return browserBean;
	}

	
	/**
	 * Setting to enable us to set initialized versions of the Node and Content delivery Controllers.
	 */
	public void setDeliveryControllers(NodeDeliveryController nodeDeliveryController, ContentDeliveryController contentDeliveryController, IntegrationDeliveryController integrationDeliveryController)
	{
		this.nodeDeliveryController        = nodeDeliveryController;
		this.contentDeliveryController     = contentDeliveryController;
		this.integrationDeliveryController = integrationDeliveryController;
	}
	
    /**
     * This method gets assignedCategories for a content on a specific categoryKey.
     */
    public List getAssignedCategories(Integer contentId, String categoryKey, Integer languageId, boolean useLanguageFallback)
    {
		List assignedCategories = new ArrayList();
		
		this.deliveryContext.addUsedContent("content_" + contentId);
		
		try
		{
			assignedCategories = ContentDeliveryController.getContentDeliveryController().getAssignedCategoryVOsForContentVersionId(getDatabase(), contentId, languageId, categoryKey, this.siteNodeId, useLanguageFallback, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assignedCategories=" + categoryKey + " on content " + contentId + ":" + e.getMessage(), e);
		}
				
		return assignedCategories;
    }

    /**
     * This method gets the full path/name of a given categoryId.
     */
    
    public String getCategoryPath(Integer categoryId)
    {
    	String categoryPath = null;
    	
    	try
		{
			categoryPath = CategoryController.getController().getCategoryPath(categoryId, getDatabase());
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the path for categoryId " + categoryId + ":" + e.getMessage(), e);
		}
		
    	return categoryPath; 
    }

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */
	 
	public String getContentAttribute(String contentBindningName, String attributeName, boolean clean) 
	{				
		return getContentAttribute(contentBindningName, attributeName);
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */
	public String getContentAttribute(String attributeName, boolean clean) 
	{				
		return getContentAttribute(attributeName);
	}

    /**
     * This method deliveres a String with the content-attribute asked for a
     * specific content and ensure not to get decorated attributes if EditOnSite is
     * turned on.
     * 
     * @param contentId
     *            the contentId of a content
     * @param attributeName
     *            the attribute name in the content. (ie. Title, Leadin etc)
     * @param clean
     *            has no effect in this class.
     * @return the contentAttribute or empty string if none found.
     * @see org.infoglue.deliver.controllers.kernel.impl.simple.EditOnSiteTemplateController#getContentAttribute(java.lang.String,
     *      boolean)
     */
    public String getContentAttribute(Integer contentId, String attributeName, boolean clean)
    {
        return getContentAttribute(contentId, attributeName);
    }
	
	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */
 
	public String getContentAttribute(Integer contentId, Integer languageId, String attributeName, boolean clean) 
	{				
	    return getContentAttribute(contentId, languageId, attributeName);
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */
 
	public String getContentAttribute(ContentVersionVO contentVersionValue, String attributeName, boolean clean) 
	{				
	    return getContentAttribute(contentVersionValue, attributeName);
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */
 
	public String getMetaInfoContentAttribute(Integer contentId, Integer languageId, String attributeName, boolean clean) 
	{				
	    return getMetaInfoContentAttribute(contentId, languageId, attributeName);
	}


	/**
	 * This method deliveres a String with the content-attribute asked for in the language asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getContentAttributeWithReturningId(Integer contentId, Integer languageId, String attributeName, boolean clean, Set contentVersionId) 
	{
	    return getContentAttribute(contentId, languageId, attributeName, contentVersionId);
	}

	/**
	 * This method deliveres a String with the content-attribute asked for if it exists in the content
	 * defined in the url-parameter contentId.
	 */
	 
	public String getContentAttribute(String attributeName) 
	{
		String attributeValue = "";
		
		try
		{
		    attributeValue = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), this.contentId, this.languageId, attributeName, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal, false);
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}

	/**
	 * This method deliveres a String with the content-attribute asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getContentAttribute(String contentBindningName, String attributeName) 
	{
		String attributeValue = "";
		
		try
		{
			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
			if(contentVO != null)
			{
				attributeValue = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), contentVO.getContentId(), this.languageId, attributeName, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal, false);
			}
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on contentBindning " + contentBindningName + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}

	
	/**
	 * This method deliveres a String with the content-attribute asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getContentAttribute(Integer contentId, String attributeName) 
	{
		String attributeValue = "";
		
		this.deliveryContext.addUsedContent("content_" + contentId);
		
		try
		{
			attributeValue = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), contentId, this.languageId, attributeName, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal, false);
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on content " + contentId + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}


	/**
	 * This method deliveres a String with the content-attribute asked for in the language asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getContentAttributeValue(Integer contentId, String attributeName, boolean escapeHTML) 
	{
	    return getContentAttributeValue(contentId, this.getLanguageId(), attributeName, escapeHTML);
	}

	/**
	 * This method deliveres a String with the content-attribute asked for in the language asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getContentAttributeValue(Integer contentId, String attributeName, boolean clean, boolean escapeHTML) 
	{
	    return getContentAttributeValue(contentId, this.getLanguageId(), attributeName, escapeHTML);
	}

	/**
	 * This method deliveres a String with the content-attribute asked for in the language asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getContentAttributeValue(Integer contentId, Integer languageId, String attributeName, boolean escapeHTML) 
	{
		String attributeValue = "";
		
		this.deliveryContext.addUsedContent("content_" + contentId);
		
		try
		{
		    attributeValue = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), contentId, languageId, attributeName, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal, escapeHTML);
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on content " + contentId + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}



	/**
	 * This method deliveres a String with the content-attribute asked for in the language asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getContentAttribute(Integer contentId, Integer languageId, String attributeName) 
	{
		String attributeValue = "";
		
		this.deliveryContext.addUsedContent("content_" + contentId);

		try
		{
		    attributeValue = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), contentId, languageId, attributeName, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal, false);
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on content " + contentId + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}
	

	/**
	 * This method deliveres a String with the content-attribute asked for in the language asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getMetaInfoContentAttribute(Integer contentId, Integer languageId, String attributeName) 
	{
		String attributeValue = "";
		
		this.deliveryContext.addUsedContent("content_" + contentId);

		try
		{
		    attributeValue = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), contentId, languageId, attributeName, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal, false, true);
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on content " + contentId + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}

	/**
	 * This method deliveres a String with the content-attribute asked for in the language asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getContentAttribute(Integer contentId, Integer languageId, String attributeName, Set contentVersionId) 
	{
		String attributeValue = "";
		
		this.deliveryContext.addUsedContent("content_" + contentId);
		
		try
		{
		    attributeValue = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), contentId, languageId, attributeName, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal, false, contentVersionId);
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on content " + contentId + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}

	/**
	 * Finds an attribute on the provided ContentVersion.
	 */
	public String getContentAttribute(ContentVersionVO version, String attributeName)
	{
		try
		{
		    return ContentDeliveryController.getContentDeliveryController().getAttributeValue(getDatabase(), version, attributeName, false);
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on content version" + version.getId() + "\nReason:" + e.getMessage(), e);
		}
	
		return "";
	}

	/**
	 * This method deliveres a String with the content-attribute asked for in the language asked for.
	 * If the attribute is not found in the language requested it fallbacks to the master language.
	 */

	public String getContentAttributeUsingLanguageFallback(Integer contentId, String attributeName, boolean disableEditOnSight) 
	{
	    logger.info("getContentAttributeUsingLanguageFallback: " + contentId + ":" + attributeName + ":" + disableEditOnSight);
		String attributeValue = "";
		
		try
		{
		    attributeValue = this.getContentAttribute(contentId, attributeName, true);
		    if(attributeValue != null && attributeValue.trim().equals(""))
		    {
		        LanguageVO masteLanguageVO = LanguageDeliveryController.getLanguageDeliveryController().getMasterLanguageForSiteNode(getDatabase(), this.siteNodeId);
		        attributeValue = this.getContentAttribute(contentId, masteLanguageVO.getLanguageId(), attributeName, disableEditOnSight);
		    }
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on content " + contentId + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}
	
	/**
	 * This method deliveres a String with the content-attribute asked for after it has been parsed and all special tags have been converted.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getParsedContentAttribute(String attributeName) 
	{
		String attributeValue = "";
		
		try
		{
			if(this.contentId != null)
			{
				String unparsedAttributeValue = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), this.contentId, this.languageId, attributeName, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal, false);
				logger.info("Found unparsedAttributeValue:" + unparsedAttributeValue);
				
				templateLogicContext.put("inlineContentId", this.contentId);
				
				Map context = new HashMap();
				context.put("inheritedTemplateLogic", this);
				context.put("templateLogic", getTemplateController(this.siteNodeId, this.languageId, this.contentId, this.request, this.infoGluePrincipal, this.deliveryContext));
				
				// Add the templateLogicContext objects to this context. (SS - 040219)
				context.putAll(templateLogicContext);
				
				StringWriter cacheString = new StringWriter();
				PrintWriter cachedStream = new PrintWriter(cacheString);
				new VelocityTemplateProcessor().renderTemplate(context, cachedStream, unparsedAttributeValue, true);
				attributeValue = cacheString.toString();
				//logger.info("result:" + result);
			}
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on sent in content with id:" + this.contentId + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}


	/**
	 * This method deliveres a String with the content-attribute asked for after it has been parsed and all special tags have been converted.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getParsedContentAttribute(String contentBindningName, String attributeName) 
	{
		logger.info("getParsedContentAttribute:" + contentBindningName + ":" + attributeName);
		
		String attributeValue = "";
		
		try
		{
			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
			if(contentVO != null)
			{
				logger.info("contentVO:" + contentVO.getContentId());
		
				String unparsedAttributeValue = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), contentVO.getId(), this.languageId, attributeName, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal, false);
				logger.info("Found unparsedAttributeValue:" + unparsedAttributeValue);
							
				templateLogicContext.put("inlineContentId", contentVO.getId());
				
				Map context = new HashMap();
				context.put("inheritedTemplateLogic", this);
				context.put("templateLogic", getTemplateController(this.siteNodeId, this.languageId, contentVO.getId(), this.request, this.infoGluePrincipal, this.deliveryContext));
				
				// Add the templateLogicContext objects to this context. (SS - 040219)
				context.putAll(templateLogicContext);
								
				StringWriter cacheString = new StringWriter();
				PrintWriter cachedStream = new PrintWriter(cacheString);
				new VelocityTemplateProcessor().renderTemplate(context, cachedStream, unparsedAttributeValue, true);
				attributeValue = cacheString.toString();
			}
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on contentBindning " + contentBindningName + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}

	

	/**
	 * This method deliveres a String with the content-attribute asked for after it has been parsed and all special tags have been converted.
	 * The attribute is fetched from the specified content.
	 */
	 
	public String getParsedContentAttribute(Integer contentId, String attributeName) 
	{
		String attributeValue = "";
		
		try
		{
			if(contentId != null)
			{
				String unparsedAttributeValue = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), contentId, this.languageId, attributeName, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal, false);
				logger.info("Found unparsedAttributeValue:" + unparsedAttributeValue);
				
				templateLogicContext.put("inlineContentId", contentId);
				
				Map context = new HashMap();
				context.put("inheritedTemplateLogic", this);
				context.put("templateLogic", getTemplateController(this.siteNodeId, this.languageId, contentId, this.request, this.infoGluePrincipal, this.deliveryContext));

				// Add the templateLogicContext objects to this context. (SS - 040219)
				context.putAll(templateLogicContext);
				
				StringWriter cacheString = new StringWriter();
				PrintWriter cachedStream = new PrintWriter(cacheString);
				new VelocityTemplateProcessor().renderTemplate(context, cachedStream, unparsedAttributeValue, true);
				attributeValue = cacheString.toString();
			}
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on content with id " + contentId + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}

	/**
	 * This method deliveres a String with the content-attribute asked for after it has been parsed and all special tags have been converted.
	 * The attribute is fetched from the specified content.
	 */
	 
	public String getParsedContentAttribute(Integer contentId, Integer languageId, String attributeName) 
	{
		String attributeValue = "";
		
		try
		{
			if(contentId != null)
			{
				String unparsedAttributeValue = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), contentId, languageId, attributeName, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal, false);
				//logger.info("Found unparsedAttributeValue:" + unparsedAttributeValue);
				
				templateLogicContext.put("inlineContentId", contentId);
				
				Map context = new HashMap();
				context.put("inheritedTemplateLogic", this);
				context.put("templateLogic", getTemplateController(this.siteNodeId, languageId, contentId, this.request, this.infoGluePrincipal, this.deliveryContext));

				// Add the templateLogicContext objects to this context. (SS - 040219)
				context.putAll(templateLogicContext);
				
				StringWriter cacheString = new StringWriter();
				PrintWriter cachedStream = new PrintWriter(cacheString);
				new VelocityTemplateProcessor().renderTemplate(context, cachedStream, unparsedAttributeValue, true);
				attributeValue = cacheString.toString();
			}
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on content with id " + contentId + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */

	public String getEscapedParsedContentAttribute(Integer contentId, Integer languageId, String attributeName, boolean clean)
	{
		return getEscapedParsedContentAttribute(contentId, languageId, attributeName);
	}

	/**
	 * This method deliveres a String with the content-attribute asked for after it has been parsed and all special tags have been converted.
	 * The attribute is fetched from the specified content.
	 */
	 
	public String getEscapedParsedContentAttribute(Integer contentId, Integer languageId, String attributeName) 
	{
		String attributeValue = "";
		
		try
		{
			if(contentId != null)
			{
				String unparsedAttributeValue = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), contentId, languageId, attributeName, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal, false);
				
				unparsedAttributeValue = unparsedAttributeValue.replaceAll("\\$(?!(\\.|\\(|templateLogic\\.(getPageUrl|getInlineAssetUrl|languageId)))", "&#36;");
				//logger.info("Found unparsedAttributeValue:" + unparsedAttributeValue);
				
				templateLogicContext.put("inlineContentId", contentId);
				
				Map context = new HashMap();
				context.put("inheritedTemplateLogic", this);
				context.put("templateLogic", getTemplateController(this.siteNodeId, languageId, contentId, this.request, this.infoGluePrincipal, this.deliveryContext));

				// Add the templateLogicContext objects to this context. (SS - 040219)
				context.putAll(templateLogicContext);
				
				StringWriter cacheString = new StringWriter();
				PrintWriter cachedStream = new PrintWriter(cacheString);
				new VelocityTemplateProcessor().renderTemplate(context, cachedStream, unparsedAttributeValue, true);
				attributeValue = cacheString.toString();
			}
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on content with id " + contentId + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}

	/**
	 * This method deliveres a String with the content-attribute asked for after it has been parsed and all special tags have been converted.
	 * The attribute is fetched from the specified content.
	 */
	 
	public String getParsedContentAttribute(ContentVersionVO contentVersionVO, String attributeName) 
	{
		String attributeValue = "";
		
		try
		{
			if(contentId != null)
			{
				String unparsedAttributeValue = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), contentVersionVO, attributeName, false);
				logger.info("Found unparsedAttributeValue:" + unparsedAttributeValue);
				
				templateLogicContext.put("inlineContentId", contentId);
				
				Map context = new HashMap();
				context.put("inheritedTemplateLogic", this);
				context.put("templateLogic", getTemplateController(this.siteNodeId, languageId, contentId, this.request, this.infoGluePrincipal, this.deliveryContext));

				// Add the templateLogicContext objects to this context. (SS - 040219)
				context.putAll(templateLogicContext);
				
				StringWriter cacheString = new StringWriter();
				PrintWriter cachedStream = new PrintWriter(cacheString);
				new VelocityTemplateProcessor().renderTemplate(context, cachedStream, unparsedAttributeValue, true);
				attributeValue = cacheString.toString();
			}
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\n    ComponentName=[ " + this.getComponentLogic().getInfoGlueComponent().getName() + " ]\nAn error occurred trying to get attributeName=" + attributeName + " on content " + this.contentId + "\nReason:" + e.getMessage());
			//logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get attributeName=" + attributeName + " on content with id " + contentId + "\nReason:" + e.getMessage(), e);
		}
				
		return attributeValue;
	}

	/**
	 * This method parses an infoglue text - which may contain wysiwyg-content including internal links and images.
	 */
	 
	public String getParsedText(String text) 
	{
		String parsedText = "";
		
		try
		{
			if(text != null)
			{
				String unparsedAttributeValue = text;
				
				Map context = new HashMap();
				context.put("inheritedTemplateLogic", this);
				context.put("templateLogic", getTemplateController(this.siteNodeId, languageId, contentId, this.request, this.infoGluePrincipal, this.deliveryContext));
				context.putAll(templateLogicContext);
				
				StringWriter cacheString = new StringWriter();
				PrintWriter cachedStream = new PrintWriter(cacheString);
				new VelocityTemplateProcessor().renderTemplate(context, cachedStream, unparsedAttributeValue, true);
				parsedText = cacheString.toString();
			}
		}
		catch(Exception e)
		{
			logger.error("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to parse text=" + text + "\nReason:" + e.getMessage(), e);
		}
				
		return parsedText;
	}

	
	
	
	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */
	 
	public String getParsedContentAttribute(String attributeName, boolean clean)
	{
		return getParsedContentAttribute(attributeName);
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */

	public String getParsedContentAttribute(String contentBindningName, String attributeName, boolean clean)
	{
		return getParsedContentAttribute(contentBindningName, attributeName);
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */

	public String getParsedContentAttribute(Integer contentId, String attributeName, boolean clean)
	{
		return getParsedContentAttribute(contentId, attributeName);
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */

	public String getParsedContentAttribute(Integer contentId, Integer languageId, String attributeName, boolean clean)
	{
		return getParsedContentAttribute(contentId, languageId, attributeName);
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */

	public String getParsedContentAttribute(Integer contentId, Integer languageId, String attributeName, boolean clean, boolean escapeVelocityCode)
	{
		return getParsedContentAttribute(contentId, languageId, attributeName);
	}

	/**
	 * This method is just a dummy method used to ensure that we can ensure to not get a decorated attribute
	 * value if OnSiteEdit is on.
	 */

	public String getParsedContentAttribute(ContentVersionVO contentVersionVO, String attributeName, boolean clean)
	{
		return getParsedContentAttribute(contentVersionVO, attributeName);
	}

	public String getParsedContentAttribute(ContentVersionVO contentVersionVO, String attributeName, boolean clean, boolean escapeVelocityCode)
	{
		return getParsedContentAttribute(contentVersionVO, attributeName);
	}

	/**
	 * This method deliveres a list of strings which represents all assetKeys for a content.
	 */
	 
	public Collection getAssetKeys(String contentBindningName) 
	{
		Collection assetKeys = new ArrayList();
		
		try
		{
			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
			assetKeys = ContentDeliveryController.getContentDeliveryController().getAssetKeys(getDatabase(), contentVO.getContentId(), this.languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get assetKeys on content with id: " + contentId + ":" + e.getMessage());
		}
				
		return assetKeys;
	}

	/**
	 * This method deliveres a list of strings which represents all assetKeys for a content.
	 */
	 
	public Collection getAssetKeys(Integer contentId) 
	{
		Collection assetKeys = new ArrayList();
		
		try
		{
			assetKeys = ContentDeliveryController.getContentDeliveryController().getAssetKeys(getDatabase(), contentId, this.languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get assetKeys on content with id: " + contentId + ":" + e.getMessage());
		}
				
		return assetKeys;
	}

	/**
	 * This method deliveres a list of strings which represents all assetKeys for a content.
	 */
	 
	public Collection getAssetIds(Integer contentId) 
	{
		Collection assetKeys = new ArrayList();
		
		try
		{
			assetKeys = ContentDeliveryController.getContentDeliveryController().getAssetIds(getDatabase(), contentId, this.languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get assetKeys on content with id: " + contentId + ":" + e.getMessage());
		}
				
		return assetKeys;
	}

	/**
	 * This method deliveres a list of DigitalAssetVO-objects which represents a certain asset for a content.
	 */
	 
	public DigitalAssetVO getAsset(Integer contentId, String assetKey)
	{
		DigitalAssetVO digitalAssetVO = null;
		
		try
		{
			digitalAssetVO = ContentDeliveryController.getContentDeliveryController().getAsset(getDatabase(), contentId, this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get asset on content with id: " + contentId + " and assetKey:" + assetKey + ":" + e.getMessage());
		}
				
		return digitalAssetVO;
	}
	
	/**
	 * This method deliveres a list of DigitalAssetVO-objects which represents all assets for a content.
	 */
	 
	public List getAssets(Integer contentId) 
	{
		List assets = new ArrayList();
		
		try
		{
			assets = ContentDeliveryController.getContentDeliveryController().getAssets(getDatabase(), contentId, this.languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get assetKeys on content with id: " + contentId + ":" + e.getMessage());
		}
				
		return assets;
	}

	/**
	 * This method deliveres a list of strings which represents all assetKeys defined for a contentTypeDefinition.
	 */
	 
	public Collection getContentTypeDefinitionAttributes(String schemaValue) 
	{
		Collection attributes = new ArrayList();
		
		try
		{
			attributes = ContentTypeDefinitionController.getController().getContentTypeAttributes(schemaValue);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get attributes on content with id: " + contentId + ":" + e.getMessage(), e);
		}
				
		return attributes;
	}
 
	/**
	 * This method deliveres a list of strings which represents all assetKeys defined for a contentTypeDefinition.
	 */
	 
	public Collection getContentTypeDefinitionAssetKeys(String schemaValue) 
	{
		Collection assetKeys = new ArrayList();
		
		try
		{
			assetKeys = ContentTypeDefinitionController.getController().getDefinedAssetKeys(schemaValue);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetKeys on content with id: " + contentId + ":" + e.getMessage(), e);
		}
				
		return assetKeys;
	}
 
	/**
	 * This method deliveres a list all categories defined for a contentTypeDefinition.
	 */
	 
	public Collection getContentTypeDefinitionCategories(String schemaValue) 
	{
		Collection categories = new ArrayList();
		
		try
		{
			categories = ContentTypeDefinitionController.getController().getDefinedCategoryKeys(schemaValue);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetKeys on content with id: " + contentId + ":" + e.getMessage(), e);
		}
				
		return categories;
	}

	/**
	 * This method deliveres a String with the URL to the thumbnail for the digital asset asked for.
	 * This method assumes that the content sent in only has one asset attached.
	 */
	 
	public String getAssetThumbnailUrl(Integer contentId, int width, int height) 
	{
		String assetThumbnailUrl = "";
		
		try
		{
			assetThumbnailUrl = ContentDeliveryController.getContentDeliveryController().getAssetThumbnailUrl(getDatabase(), contentId, this.languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, width, height, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetThumbnailUrl on contentId " + contentId + ":" + e.getMessage(), e);
		}
				
		return assetThumbnailUrl;
	}

	/**
	 * This method deliveres a String with the URL to the thumbnail for the digital asset asked for.
	 * This method takes a key for the asset you want to make a thumbnail from.
	 */
	 
	public String getAssetThumbnailUrl(Integer contentId, String assetKey, int width, int height) 
	{
		String assetThumbnailUrl = "";
		
		try
		{
			assetThumbnailUrl = ContentDeliveryController.getContentDeliveryController().getAssetThumbnailUrl(getDatabase(), contentId, this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, width, height, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetThumbnailUrl on contentId " + contentId + ":" + e.getMessage(), e);
		}
				
		return assetThumbnailUrl;
	}

	/**
	 * This method deliveres a String with the URL to the thumbnail for the digital asset asked for.
	 * This method takes a key for the asset you want to make a thumbnail from.
	 */
	 
	public String getAssetThumbnailUrlForAssetWithId(Integer digitalAssetId, int width, int height) 
	{
		String assetThumbnailUrl = "";
		
		try
		{
			assetThumbnailUrl = ContentDeliveryController.getContentDeliveryController().getAssetThumbnailUrl(getDatabase(), digitalAssetId, this.siteNodeId, width, height, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetThumbnailUrl on contentId " + contentId + ":" + e.getMessage(), e);
		}
				
		return assetThumbnailUrl;
	}

	/**
	 * This method deliveres a String with the URL to the thumbnail of the digital asset asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getAssetThumbnailUrl(String contentBindningName, int width, int height) 
	{
		String assetUrl = "";
		
		try
		{
			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
			assetUrl = ContentDeliveryController.getContentDeliveryController().getAssetThumbnailUrl(getDatabase(), contentVO.getContentId(), this.languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, width, height, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetUrl on contentBindningName " + contentBindningName + ":" + e.getMessage(), e);
		}
				
		return assetUrl;
	}

	/**
	 * This method deliveres a String with the URL to the thumbnail of the digital asset asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getAssetThumbnailUrl(String contentBindningName, String assetKey, int width, int height) 
	{
		String assetThumbnailUrl = "";
		
		try
		{
			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
			assetThumbnailUrl = ContentDeliveryController.getContentDeliveryController().getAssetThumbnailUrl(getDatabase(), contentVO.getContentId(), this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, width, height, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetUrl on contentBindningName " + contentBindningName + ":" + e.getMessage(), e);
		}
				
		return assetThumbnailUrl;
	}

	/**
	 * This method deliveres a String with the URL to the digital asset asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getAssetUrl(String contentBindningName) 
	{
		String assetUrl = "";
		
		try
		{
			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
			assetUrl = ContentDeliveryController.getContentDeliveryController().getAssetUrl(getDatabase(), contentVO.getContentId(), this.languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetUrl on contentBindningName " + contentBindningName + ":" + e.getMessage(), e);
		}
				
		return assetUrl;
	}

	
	public String getEncodedUrl(String s, String enc)
	{
		String ret = "";
		try 
		{
			ret = java.net.URLEncoder.encode(s, enc);
		} 
		catch (UnsupportedEncodingException e) 
		{
			logger.error("An error occurred trying to encode the url: " + s + " with encoding: " + enc + ": " + e.getMessage(), e);
		}		
		return ret;
	}
	
	/**
	 * This method deliveres a String with the URL to the digital asset asked for.
	 */
	 
	public String getAssetUrl(Integer contentId) 
	{
		String assetUrl = "";
		
		try
		{
			assetUrl = ContentDeliveryController.getContentDeliveryController().getAssetUrl(getDatabase(), contentId, this.languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get assetUrl on content with id: " + contentId + ":" + e.getMessage());
		}
				
		return assetUrl;
	}

    /**
     * This method deliveres a collection of strings with the URL to the digital assets for a certain content.
     */
    public List getAssetUrls(Integer contentId)
    {
		List assetUrls = new ArrayList();
		
		try
		{
			List digitalAssetVOList = getAssets(contentId);
			Iterator digitalAssetVOListIterator = digitalAssetVOList.iterator();
			while(digitalAssetVOListIterator.hasNext())
			{
				DigitalAssetVO digitalAssetVO = (DigitalAssetVO)digitalAssetVOListIterator.next();
				String assetUrl = getAssetUrlForAssetWithId(digitalAssetVO.getId());
				assetUrls.add(assetUrl);
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetUrls on content with id: " + contentId + ":" + e.getMessage(), e);
		}
				
		return assetUrls;    	
    }


	/**
	 * This method deliveres a String with the URL to the digital asset asked for.
	 */
	 
	public String getAssetUrl(Integer contentId, String assetKey) 
	{
		String assetUrl = "";
		
		try
		{
			assetUrl = ContentDeliveryController.getContentDeliveryController().getAssetUrl(getDatabase(), contentId, this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get assetUrl on content with id: " + contentId + " and assetKey:" + assetKey + " : " + e.getMessage());
		}
				
		return assetUrl;
	}

	/**
	 * This method deliveres a String with the URL to the digital asset asked for.
	 */
	 
	public String getProtectedAssetUrl(Integer contentId, String assetKey) 
	{
		String assetUrl = "";
		
		try
		{
			assetUrl = ContentDeliveryController.getContentDeliveryController().getAssetUrl(getDatabase(), contentId, this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get assetUrl on content with id: " + contentId + " and assetKey:" + assetKey + " : " + e.getMessage());
		}
				
		return assetUrl;
	}

	/**
	 * This method deliveres a String with the URL to the digital asset asked for.
	 */
	 
	public String getAssetUrlForAssetWithId(Integer digitalAssetId) 
	{
		String assetUrl = "";
		
		try
		{
			assetUrl = ContentDeliveryController.getContentDeliveryController().getAssetUrl(getDatabase(), digitalAssetId, this.siteNodeId, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetUrl on content with digitalAssetId:" + digitalAssetId + " : " + e.getMessage(), e);
		}
				
		return assetUrl;
	}

	/**
	 * This method deliveres a String with the assetFilePath to the digital asset asked for.
	 */
	 
	public String getAssetFilePathForAssetWithId(Integer digitalAssetId) 
	{
		String assetFilePath = "";
		
		try
		{
			assetFilePath = ContentDeliveryController.getContentDeliveryController().getAssetFilePath(getDatabase(), digitalAssetId, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetFilePath on content with digitalAssetId:" + digitalAssetId + " : " + e.getMessage(), e);
		}
				
		return assetFilePath;
	}

	/**
	 * This method deliveres a String with the URL to the digital asset asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getAssetUrl(String contentBindningName, int index) 
	{
		String assetUrl = "";
		
		try
		{
			List contentVOList = this.nodeDeliveryController.getBoundContents(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, USE_INHERITANCE, true, this.deliveryContext);		
			if(contentVOList != null && contentVOList.size() > index)
			{
				ContentVO contentVO = (ContentVO)contentVOList.get(index);
				assetUrl = ContentDeliveryController.getContentDeliveryController().getAssetUrl(getDatabase(), contentVO.getContentId(), this.languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetUrl on contentBindningName " + contentBindningName + ":" + e.getMessage(), e);
		}
				
		return assetUrl;
	}


	/**
	 * This method deliveres a String with the URL to the digital asset asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getAssetUrl(String contentBindningName, String assetKey) 
	{
		String assetUrl = "";
		
		try
		{
			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
			assetUrl = ContentDeliveryController.getContentDeliveryController().getAssetUrl(getDatabase(), contentVO.getContentId(), this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetUrl on contentBindningName " + contentBindningName + " with assetKey " + assetKey + ":" + e.getMessage(), e);
		}
				
		return assetUrl;
	}


	/**
	 * This method deliveres a String with the URL to the digital asset asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getAssetUrl(InfoGluePrincipal principal, String assetKey, Integer languageId, Integer siteNodeId, boolean useLanguageFallback) 
	{
		String assetUrl = "";
		
		try
		{
		    assetUrl = ExtranetController.getController().getPrincipalAssetUrl(this.getDatabase(), principal, assetKey, languageId, siteNodeId, useLanguageFallback, deliveryContext);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetUrl on principal " + principal.getName() + " with assetKey " + assetKey + ":" + e.getMessage(), e);
		}
				
		return assetUrl;
	}
	
	/**
	 * This method deliveres a String with the URL to the digital asset asked for.
	 * As the siteNode can have multiple bindings as well as a content as a parameter this
	 * parameter requires a bindingName which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getAssetThumbnailUrl(InfoGluePrincipal principal, String assetKey, Integer languageId, Integer siteNodeId, boolean useLanguageFallback, int width, int height) 
	{
		String assetUrl = "";
		
		try
		{
		    assetUrl = ExtranetController.getController().getPrincipalThumbnailAssetUrl(this.getDatabase(), principal, assetKey, languageId, siteNodeId, useLanguageFallback, width, height, deliveryContext);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetUrl on principal " + principal.getName() + " with assetKey " + assetKey + ":" + e.getMessage(), e);
		}
				
		return assetUrl;
	}
	
	/**
	 * This method deliveres a String with the URL to the digital asset asked for. In this special case the image
	 * is fetched from the article being generated. This means that this method only is of interest if you have attached
	 * assets to either a template or to an content and are useing parsedContentAttribute.
	 */
	 
	public String getInlineAssetUrl(String assetKey) 
	{
		String assetUrl = "";
		
		try
		{	
		    Integer inlineContentId = this.contentId;
		    if(inlineContentId == null || inlineContentId.intValue() == -1)
		        inlineContentId = (Integer)this.templateLogicContext.get("inlineContentId");
		        
			logger.info("getInlineAssetUrl:" + inlineContentId + ":" + this.languageId + ":" + assetKey + ":" + this.siteNodeId);
			assetUrl = ContentDeliveryController.getContentDeliveryController().getAssetUrl(getDatabase(), inlineContentId, this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get assetUrl on content with id: " + this.contentId + ":" + e.getMessage());
		}
				
		return assetUrl;
	}


	/**
	 * This method deliveres a String with the URL to the digital asset asked for. In this special case the image
	 * is fetched from the article being generated. This means that this method only is of interest if you have attached
	 * assets to either a template or to an content and are useing parsedContentAttribute.
	 */
	 
	public String getInlineAssetUrl(Integer contentId, String assetKey) 
	{
		String assetUrl = "";
		
		try
		{	
		    Integer inlineContentId = contentId;
			assetUrl = ContentDeliveryController.getContentDeliveryController().getAssetUrl(getDatabase(), inlineContentId, this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.warn("\nError on url: " + this.getOriginalFullURL() + "\nAn error occurred trying to get InlineAssetUrl for contentId: " + contentId + " with assetKey: " + assetKey + "\nReason:" + e.getMessage());
		}
				
		return assetUrl;
	}

	/*
	 *  Provide the same interface for getting asset filesize as for getting url. 
	 *  This should be refactored soon, to supply a assetVO instead.   
	 *
	 */

	public Integer getAssetFileSize(Integer contentId) 
	{
		Integer AssetFileSize = null;
		try
		{
			AssetFileSize = ContentDeliveryController.getContentDeliveryController().getAssetFileSize(getDatabase(), contentId, this.languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get AssetFileSize on content with id: " + contentId + ":" + e.getMessage(), e);
		}
		return AssetFileSize;
	}
	
	public Integer getAssetFileSize(Integer contentId, String assetKey) 
	{
		Integer AssetFileSize = null;
		try
		{
			AssetFileSize = ContentDeliveryController.getContentDeliveryController().getAssetFileSize(getDatabase(), contentId, this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get AssetFileSize on content with id: " + contentId + " and assetKey:" + assetKey + " : " + e.getMessage(), e);
		}
		return AssetFileSize;
	}
	
	public Integer getAssetFileSize(String contentBindningName, int index) 
	{
		Integer AssetFileSize = null;
		try
		{
			List contentVOList = this.nodeDeliveryController.getBoundContents(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, USE_INHERITANCE, true, this.deliveryContext);		
			if(contentVOList != null && contentVOList.size() > index)
			{
				ContentVO contentVO = (ContentVO)contentVOList.get(index);
				AssetFileSize = ContentDeliveryController.getContentDeliveryController().getAssetFileSize(getDatabase(), contentVO.getContentId(), this.languageId, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get AssetFileSize on contentBindningName " + contentBindningName + ":" + e.getMessage(), e);
		}
		return AssetFileSize;
	}

	/**
	 * Returns assetFileSize for a digital asset identified by id.
	 * 
	 * @param digitalAssetId
	 * @return
	 */

	public Integer getAssetFileSizeForAssetWithId(Integer digitalAssetId) 
	{
		Integer assetFileSize = null;
		try
		{
			DigitalAssetVO digitalAsset = DigitalAssetController.getDigitalAssetVOWithId(digitalAssetId);
			if(digitalAsset != null)
			{
				assetFileSize = digitalAsset.getAssetFileSize();
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetFileSize with digitalAssetId " + digitalAssetId + ":" + e.getMessage(), e);
		}
		return assetFileSize;
	}

	public Integer getAssetFileSize(String contentBindningName, String assetKey) 
	{
		Integer AssetFileSize = null;
		try
		{
			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
			AssetFileSize = ContentDeliveryController.getContentDeliveryController().getAssetFileSize(getDatabase(), contentVO.getContentId(), this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get AssetFileSize on contentBindningName " + contentBindningName + " with assetKey " + assetKey + ":" + e.getMessage(), e);
		}
		return AssetFileSize;
	}

	
	/**
	 * This method gets a List of related contents defined in an attribute as an xml-definition.
	 * This is an ugly method right now. Later we should have xmlDefinitions that are fully qualified so it can be
	 * used to access other systems than our own.
	 */
	
	public List getRelatedContentsByQualifyer(String qualifyerXML)
	{
		List relatedContentVOList = new ArrayList();
		
		try
		{
			relatedContentVOList = this.getRelatedContentsFromXML(qualifyerXML);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get related contents from qualifyer: " + qualifyerXML + ":" + e.getMessage(), e);
		}
		
		return relatedContentVOList;
	}

	/**
	 * This method gets a List of related contents defined in an attribute as an xml-definition.
	 * This is an ugly method right now. Later we should have xmlDefinitions that are fully qualified so it can be
	 * used to access other systems than our own.
	 */
	
	public List getRelatedContents(String attributeName)
	{
		List relatedContentVOList = new ArrayList();
		
		try
		{
			String qualifyerXML = this.getContentAttribute(attributeName, true);
			
			relatedContentVOList = this.getRelatedContentsFromXML(qualifyerXML);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get related contents on contentId " + this.contentId + " with relationName " + attributeName + ":" + e.getMessage(), e);
		}
		
		return relatedContentVOList;
	}
	
	/**
	 * This method gets a List of related contents defined in an attribute as an xml-definition.
	 * This is an ugly method right now. Later we should have xmlDefinitions that are fully qualified so it can be
	 * used to access other systems than our own.
	 */
	
	public List getRelatedContents(String bindingName, String attributeName)
	{
		List relatedContentVOList = new ArrayList();
		
		try
		{
			String qualifyerXML = this.getContentAttribute(bindingName, attributeName, true);

			relatedContentVOList = this.getRelatedContentsFromXML(qualifyerXML);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get related contents on contentId " + this.contentId + " with relationName " + attributeName + ":" + e.getMessage(), e);
		}
		
		return relatedContentVOList;
	}

	
	
	/**
	 * This method gets a List of related contents defined in an attribute as an xml-definition.
	 * This is an ugly method right now. Later we should have xmlDefinitions that are fully qualified so it can be
	 * used to access other systems than our own.
	 */
	
	public List getRelatedContents(Integer contentId, String attributeName)
	{
		List relatedContentVOList = new ArrayList();
		
		try
		{
			logger.info("contentId " + this.contentId + " with relationName " + attributeName);
		    String qualifyerXML = this.getContentAttribute(contentId, attributeName, true);
			relatedContentVOList = getRelatedContentsFromXML(qualifyerXML);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get related contents on contentId " + this.contentId + " with relationName " + attributeName + ":" + e.getMessage(), e);
		}
		
		return relatedContentVOList;
	}

	/**
	 * This method gets a List of related contents defined in an attribute as an xml-definition.
	 * This is an ugly method right now. Later we should have xmlDefinitions that are fully qualified so it can be
	 * used to access other systems than our own.
	 */
	
	public List getRelatedContents(Integer contentId, String attributeName, boolean useAttributeLanguageFallBack)
	{
		List relatedContentVOList = new ArrayList();
		
		try
		{
			logger.info("contentId " + this.contentId + " with relationName " + attributeName);
		    String qualifyerXML = null;
		    if(useAttributeLanguageFallBack)
		    	qualifyerXML = this.getContentAttributeUsingLanguageFallback(contentId, attributeName, true);
		    else
		    	qualifyerXML = this.getContentAttribute(contentId, attributeName, true);
		    
			relatedContentVOList = getRelatedContentsFromXML(qualifyerXML);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get related contents on contentId " + this.contentId + " with relationName " + attributeName + ":" + e.getMessage(), e);
		}
		
		return relatedContentVOList;
	}

	/**
	 * This method gets a List of related contents defined in a principal property as an xml-definition.
	 */
	
	public List getRelatedContents(InfoGluePrincipal infogluePrincipal, String attributeName)
	{
		List relatedContentVOList = new ArrayList();
		
		try
		{
			String qualifyerXML = this.getPrincipalPropertyValue(infogluePrincipal, attributeName, false);
			
			relatedContentVOList = getRelatedContentsFromXML(qualifyerXML);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get related contents on contentId " + this.contentId + " with relationName " + attributeName + ":" + e.getMessage(), e);
		}
		
		return relatedContentVOList;
	}

	
	/**
	 * This method gets the related contents from an XML.
	 */

	private String idElementStart = "<id>";
	private String idElementEnd = "</id>";
	private String idAttribute1Start = "id=\"";
	private String idAttribute1End = "\"";
	private String idAttribute2Start = "id='";
	private String idAttribute2End = "'";
	
	private List getRelatedContentsFromXML(String qualifyerXML)
	{
		if(logger.isInfoEnabled())
			logger.info("qualifyerXML:" + qualifyerXML);
		
		Timer t = new Timer();
		
		List relatedContentVOList = new ArrayList();

		try
		{
			if(qualifyerXML != null && !qualifyerXML.equals(""))
			{
				String startExpression = idElementStart;
				String endExpression = idElementEnd;

				int idIndex = qualifyerXML.indexOf(startExpression);
				if(idIndex == -1)
				{
					startExpression = idAttribute1Start;
					idIndex = qualifyerXML.indexOf(startExpression);
					if(idIndex == -1)
					{
						startExpression = idAttribute2Start;
						endExpression = idAttribute2End;
						idIndex = qualifyerXML.indexOf(startExpression);						
					}
					else
					{
						endExpression = idAttribute1End;
					}
				}
				
				while(idIndex > -1)
				{
					int endIndex = qualifyerXML.indexOf(endExpression, idIndex + 4);
						
					String id = qualifyerXML.substring(idIndex + 4, endIndex);
					
					try
					{
						Integer contentId = new Integer(id);
						if(ContentDeliveryController.getContentDeliveryController().isValidContent(this.getDatabase(), contentId, this.languageId, USE_LANGUAGE_FALLBACK, true, getPrincipal(), this.deliveryContext))
							relatedContentVOList.add(this.getContent(contentId));
					}
					catch(Exception e)
					{
					    logger.info("An error occurred when looking up one of the related contents FromXML:" + e.getMessage(), e);
					}

					idIndex = qualifyerXML.indexOf(startExpression, idIndex + 5);
				}

				/*
				Document document = domBuilder.getDocument(qualifyerXML);
				
				List children = document.getRootElement().elements();
				Iterator i = children.iterator();
				while(i.hasNext())
				{
					Element child = (Element)i.next();
					
					String id = child.attributeValue("id");
					if(id == null || id.equals(""))
						id = child.getText();
		
					Integer contentId = new Integer(id);
					if(ContentDeliveryController.getContentDeliveryController().isValidContent(this.getDatabase(), contentId, this.languageId, USE_LANGUAGE_FALLBACK, true, getPrincipal(), this.deliveryContext))
						relatedContentVOList.add(this.getContent(contentId));
				}
				*/
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get related contents from qualifyerXML " + qualifyerXML + ":" + e.getMessage(), e);
		}
		
		return relatedContentVOList;
	}

	/**
	 * This method gets the related pages from an XML.
	 */
	
	private List getRelatedPagesFromXML(String qualifyerXML, boolean escapeHTML)
	{
		if(logger.isInfoEnabled())
			logger.info("qualifyerXML:" + qualifyerXML);

		Timer t = new Timer();

		List relatedPages = new ArrayList();
		
		try
		{
			if(qualifyerXML != null && !qualifyerXML.equals(""))
			{
				String startExpression = idElementStart;
				String endExpression = idElementEnd;

				int idIndex = qualifyerXML.indexOf(startExpression);
				if(idIndex == -1)
				{
					startExpression = idAttribute1Start;
					idIndex = qualifyerXML.indexOf(startExpression);
					if(idIndex == -1)
					{
						startExpression = idAttribute2Start;
						endExpression = idAttribute2End;
						idIndex = qualifyerXML.indexOf(startExpression);						
					}
					else
					{
						endExpression = idAttribute1End;
					}
				}
				
				while(idIndex > -1)
				{
					int endIndex = qualifyerXML.indexOf(endExpression, idIndex + 4);
						
					String id = qualifyerXML.substring(idIndex + 4, endIndex);
					
					try
					{
						SiteNodeVO siteNodeVO = this.nodeDeliveryController.getSiteNode(getDatabase(), new Integer(id)).getValueObject();
						if(this.nodeDeliveryController.isValidSiteNode(getDatabase(), siteNodeVO.getId()))
						{
							WebPage webPage = new WebPage();						
							webPage.setSiteNodeId(siteNodeVO.getSiteNodeId());
							webPage.setLanguageId(this.languageId);
							webPage.setContentId(null);
							webPage.setNavigationTitle(this.nodeDeliveryController.getPageNavigationTitle(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, META_INFO_BINDING_NAME, NAV_TITLE_ATTRIBUTE_NAME, USE_LANGUAGE_FALLBACK, this.deliveryContext, escapeHTML));
							webPage.setMetaInfoContentId(this.nodeDeliveryController.getMetaInfoContentId(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), META_INFO_BINDING_NAME, USE_INHERITANCE, this.deliveryContext));
							webPage.setUrl(this.nodeDeliveryController.getPageUrl(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, this.deliveryContext));
							relatedPages.add(webPage);
						}
					}
					catch(Exception e)
					{
					    logger.info("An error occurred when looking up one of the related Pages FromXML:" + e.getMessage(), e);
					}

					idIndex = qualifyerXML.indexOf(startExpression, idIndex + 5);
				}
					
				/*
				Document document = domBuilder.getDocument(qualifyerXML);
								
				List children = document.getRootElement().elements();
				Iterator i = children.iterator();
				while(i.hasNext())
				{
					Element child = (Element)i.next();
					
					String id = child.attributeValue("id");
					if(id == null || id.equals(""))
						id = child.getText();
		
					try
					{
						SiteNodeVO siteNodeVO = this.nodeDeliveryController.getSiteNode(getDatabase(), new Integer(id)).getValueObject();
						if(this.nodeDeliveryController.isValidSiteNode(getDatabase(), siteNodeVO.getId()))
						{
							WebPage webPage = new WebPage();						
							webPage.setSiteNodeId(siteNodeVO.getSiteNodeId());
							webPage.setLanguageId(this.languageId);
							webPage.setContentId(null);
							webPage.setNavigationTitle(this.nodeDeliveryController.getPageNavigationTitle(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, META_INFO_BINDING_NAME, NAV_TITLE_ATTRIBUTE_NAME, USE_LANGUAGE_FALLBACK, this.deliveryContext, escapeHTML));
							webPage.setMetaInfoContentId(this.nodeDeliveryController.getMetaInfoContentId(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), META_INFO_BINDING_NAME, USE_INHERITANCE, this.deliveryContext));
							webPage.setUrl(this.nodeDeliveryController.getPageUrl(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, this.deliveryContext));
							relatedPages.add(webPage);
						}
					}
					catch(Exception e)
					{
					    logger.info("An error occurred when looking up one of the related Pages FromXML:" + e.getMessage(), e);
					}
				}	
				*/
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get related contents from qualifyerXML " + qualifyerXML + ":" + e.getMessage(), e);
		}

		return relatedPages;
	}
	/**
	 * This method gets a List of related siteNodes defined in an attribute as an xml-definition.
	 * This is an ugly method right now. Later we should have xmlDefinitions that are fully qualified so it can be
	 * used to access other systems than our own.
	 */
	
	public List getRelatedPages(String attributeName)
	{
	    return getRelatedPages(attributeName, false);
	}

	/**
	 * This method gets a List of related siteNodes defined in an attribute as an xml-definition.
	 * This is an ugly method right now. Later we should have xmlDefinitions that are fully qualified so it can be
	 * used to access other systems than our own.
	 */
	
	public List getRelatedPages(String attributeName, boolean escapeHTML)
	{
	    List relatedPages = new ArrayList();
		
		try
		{
			String qualifyerXML = this.getContentAttribute(attributeName, true);
			
			relatedPages = getRelatedPagesFromXML(qualifyerXML, escapeHTML);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get related contents on contentId " + this.contentId + " with relationName " + attributeName + ":" + e.getMessage(), e);
		}
		
		return relatedPages;
	}

	/**
	 * This method gets a List of related contents defined in an attribute as an xml-definition.
	 * This is an ugly method right now. Later we should have xmlDefinitions that are fully qualified so it can be
	 * used to access other systems than our own.
	 */

	public List getRelatedPages(String bindingName, String attributeName)
	{
		return getRelatedPages(bindingName, attributeName, false);
	}

	/**
	 * This method gets a List of related contents defined in an attribute as an xml-definition.
	 * This is an ugly method right now. Later we should have xmlDefinitions that are fully qualified so it can be
	 * used to access other systems than our own.
	 */

	public List getRelatedPages(String bindingName, String attributeName, boolean escapeHTML)
	{
		List relatedPages = new ArrayList();
		
		try
		{
			String qualifyerXML = this.getContentAttribute(bindingName, attributeName, true);

			relatedPages = getRelatedPagesFromXML(qualifyerXML, escapeHTML);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get related contents on contentId " + this.contentId + " with relationName " + attributeName + ":" + e.getMessage(), e);
		}
		
		return relatedPages;
	}
	

	/**
	 * This method gets a List of related contents defined in an attribute as an xml-definition.
	 * This is an ugly method right now. Later we should have xmlDefinitions that are fully qualified so it can be
	 * used to access other systems than our own.
	 */

	public List getRelatedPages(Integer contentId, String attributeName)
	{
		List relatedPages = new ArrayList();
		
		try
		{
			String qualifyerXML = this.getContentAttribute(contentId, attributeName, true);
			
			relatedPages = getRelatedPagesFromXML(qualifyerXML, false);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get related contents on contentId " + this.contentId + " with relationName " + attributeName + ":" + e.getMessage(), e);
		}
		
		return relatedPages;
	}

	/**
	 * This method gets a List of related contents defined in an attribute as an xml-definition.
	 * This is an ugly method right now. Later we should have xmlDefinitions that are fully qualified so it can be
	 * used to access other systems than our own.
	 */

	public List getRelatedPages(InfoGluePrincipal infogluePrincipal, String attributeName)
	{
		List relatedPages = new ArrayList();
		
		try
		{
			String qualifyerXML = this.getPrincipalPropertyValue(infogluePrincipal, attributeName, false);
			
			relatedPages = getRelatedPagesFromXML(qualifyerXML, false);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get related pages on infogluePrincipal " + infogluePrincipal + " with relationName " + attributeName + ":" + e.getMessage(), e);
		}
		
		return relatedPages;
	}

	/**
	 * This method gets a List of pages referencing the given content.
	 */

	public List getReferencingPages(Integer contentId, int maxRows)
	{
		return getReferencingPages(contentId, maxRows, new Boolean(true));
	}

	/**
	 * This method gets a List of pages referencing the given content.
	 */

	public List getReferencingPages(Integer contentId, int maxRows, Boolean excludeCurrentPage)
	{
		String cacheKey = "content_" + contentId + "_" + maxRows + "_" + excludeCurrentPage;
		
		if(logger.isInfoEnabled())
			logger.info("cacheKey:" + cacheKey);
		
		List referencingPages = (List)CacheController.getCachedObject("referencingPagesCache", cacheKey);
		if(referencingPages != null)
		{
			if(logger.isInfoEnabled())
				logger.info("There was an cached referencingPages:" + referencingPages.size());
		}
		else
		{
			referencingPages = new ArrayList();
			try
			{
				List referencingObjects = RegistryController.getController().getReferencingObjectsForContent(contentId, maxRows, false, this.getDatabase());
				
				Iterator referencingObjectsIterator = referencingObjects.iterator();
				while(referencingObjectsIterator.hasNext())
				{
					ReferenceBean referenceBean = (ReferenceBean)referencingObjectsIterator.next();
					Object pageCandidate = referenceBean.getReferencingCompletingObject();
					if(pageCandidate instanceof SiteNodeVO)
					{
						if(!excludeCurrentPage || !((SiteNodeVO)pageCandidate).getId().equals(getSiteNodeId()))
							referencingPages.add(pageCandidate);
					}
				}
				
				if(referencingPages != null)
					CacheController.cacheObject("referencingPagesCache", cacheKey, referencingPages);
			}
			catch(Exception e)
			{
				logger.error("An error occurred trying to get referencing pages for the contentId " + this.contentId + ":" + e.getMessage(), e);
			}
		}
		
		return referencingPages;
	}

	/**
	 * This method deliveres a String with the URL to the base path of the directory resulting from 
	 * an unpacking of a uploaded zip-digitalAsset.
	 */
	 
	public String getArchiveBaseUrl(String contentBindningName, String assetKey) 
	{
		String assetUrl = "";
		
		try
		{
			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
			assetUrl = ContentDeliveryController.getContentDeliveryController().getArchiveBaseUrl(getDatabase(), contentVO.getContentId(), this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetUrl on contentBindningName " + contentBindningName + " with assetKey " + assetKey + ":" + e.getMessage(), e);
		}
				
		return assetUrl;
	}


	/**
	 * This method deliveres a String with the URL to the base path of the directory resulting from 
	 * an unpacking of a uploaded zip-digitalAsset identified by a digitalAssetId.
	 */
	 
	public String getArchiveBaseUrlForAssetWithId(Integer digitalAssetId) 
	{
		String assetUrl = "";
		
		try
		{
			assetUrl = ContentDeliveryController.getContentDeliveryController().getArchiveBaseUrl(getDatabase(), digitalAssetId, this.siteNodeId, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetUrl with digitalAssetId " + digitalAssetId + ":" + e.getMessage(), e);
		}
				
		return assetUrl;
	}

	/**
	 * This method deliveres a String with the URL to the base path of the directory resulting from 
	 * an unpacking of a uploaded zip-digitalAsset.
	 */
	 
	public String getArchiveBaseUrl(Integer contentId, String assetKey) 
	{
		String assetUrl = "";
		
		try
		{
			assetUrl = ContentDeliveryController.getContentDeliveryController().getArchiveBaseUrl(getDatabase(), contentId, this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetUrl on content with id " + contentId + " with assetKey " + assetKey + ":" + e.getMessage());
		}
				
		return assetUrl;
	}

	/**
	 * This method deliveres a String containing the URL to the directory resulting from unpacking of a uploaded zip-digitalAsset.
	 * This method is meant to be used for javascript plugins and similar bundles - and the target directory is therefore the infoglueDeliverXXXX/digitalAssets/extensions
	 */
	 
	public String getScriptExtensionUrls(Integer contentId, String assetKey, String fileNames, Boolean autoCreateMarkup, Boolean addToHeader, Boolean addToBody, Boolean addToBundledIncludes, String bundleName) 
	{
		String assetUrl = "";
		
		try
		{
			assetUrl = ContentDeliveryController.getContentDeliveryController().getScriptExtensionUrls(getDatabase(), contentId, this.languageId, assetKey, fileNames, autoCreateMarkup, addToHeader, addToBody, addToBundledIncludes, bundleName, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get script extension on content with id " + contentId + " with assetKey " + assetKey + ":" + e.getMessage());
		}
				
		return assetUrl;
	}

	public Vector getArchiveEntries(Integer contentId, String assetKey) 
	{
		Vector entries = null;
		
		try
		{
			entries = ContentDeliveryController.getContentDeliveryController().getArchiveEntries(getDatabase(), contentId, this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetUrl on content with id " + contentId + " with assetKey " + assetKey + ":" + e.getMessage());
		}
				
		return entries;
	}


	/**
	 * This method deliveres a String with the URL to the base path of the directory resulting from 
	 * an unpacking of a uploaded zip-digitalAsset.
	 */
	 
	public String getArchiveBaseUrl(String contentBindningName, int index, String assetKey) 
	{
		String assetUrl = "";
		
		try
		{
			List contentVOList = this.nodeDeliveryController.getBoundContents(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, USE_INHERITANCE, true, this.deliveryContext);		
			if(contentVOList != null && contentVOList.size() > index)
			{
				ContentVO contentVO = (ContentVO)contentVOList.get(index);
				assetUrl = ContentDeliveryController.getContentDeliveryController().getArchiveBaseUrl(getDatabase(), contentVO.getContentId(), this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get assetUrl on contentBindningName " + contentBindningName + " with assetKey " + assetKey + ":" + e.getMessage(), e);
		}
				
		return assetUrl;
	}


	
	/**
	 * This method uses the content-attribute to generate a pdf-file.
	 * The content-attribute is parsed before it is sent to the renderer, and the
	 * resulting string must follow the XSL-FO specification.
	 * 
	 * The method checks if a previous file exists that has the same attributes as the wanted one
	 * and if so - we don't generate it again.
	 * 
	 */
	public String getContentAttributeAsPDFUrl(String contentBindningName, String attributeName)
	{
		String pdfUrl = "";
		
		try 
		{
			String template = getParsedContentAttribute(contentBindningName, attributeName, true);
			String uniqueId = siteNodeId + "_" + attributeName + "_" + contentBindningName + template.hashCode();
			String fileName = uniqueId + ".pdf";

			int i = 0;
			String filePath = CmsPropertyHandler.getDigitalAssetPath0();
			while(filePath != null)
			{
				File pdfFile = new File(filePath + java.io.File.separator + fileName);
				if(!pdfFile.exists())
				{
					logger.info("Creating a foprenderer");
					FOPHelper fop = new FOPHelper();
					fop.generatePDF(template, pdfFile);
				}
				i++;
				filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
			}
			
			SiteNode siteNode = this.nodeDeliveryController.getSiteNode(getDatabase(), this.siteNodeId);
			String dnsName = CmsPropertyHandler.getWebServerAddress();
			if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
				dnsName = siteNode.getRepository().getDnsName();

			//pdfUrl = dnsName + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + fileName;
			pdfUrl = urlComposer.composeDigitalAssetUrl(dnsName, null, fileName, deliveryContext); 
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get ContentAttribute As PDFUrl:" + e.getMessage(), e);
		}
		
		return pdfUrl;
	}
	
	/**
	 * This method writes a string to a file and returns it as a digitalAssetURL.
	 */
	public String getStringAsDigitalAssetUrl(String data, String fileName, String suffix)
	{
		String assetUrl = "";
		
		try 
		{
			String uniqueFileName = "" + fileName + data.hashCode() + "." + suffix;
			VisualFormatter vf = new VisualFormatter();
			uniqueFileName = vf.replaceNonAscii(uniqueFileName, '_');
	
			int i = 0;
			String filePath = CmsPropertyHandler.getDigitalAssetPath0();
			while(filePath != null)
			{
				File file = new File(filePath + java.io.File.separator + uniqueFileName);
				if(!file.exists())
				{
					FileHelper.writeToFile(file, data, false);
				}
				i++;
				filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
			}
			
			SiteNode siteNode = this.nodeDeliveryController.getSiteNode(getDatabase(), this.siteNodeId);
			String dnsName = CmsPropertyHandler.getWebServerAddress();
			if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
				dnsName = siteNode.getRepository().getDnsName();

			assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, null, uniqueFileName, deliveryContext); 
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get text as Url:" + e.getMessage(), e);
		}
		
		return assetUrl;
	}
	
	/**
	 * This method uses the content-attribute to generate a pdf-file.
	 * The content-attribute is parsed before it is sent to the renderer, and the
	 * resulting string must follow the XSL-FO specification.
	 * 
	 * The method checks if a previous file exists that has the same attributes as the wanted one
	 * and if so - we don't generate it again.
	 * 
	 */
	public String getContentAttributeAsPDFUrl(Integer contentId, String attributeName)
	{
		String pdfUrl = "";
		
		try 
		{
			String template = getParsedContentAttribute(contentId, attributeName, true);
			String uniqueId = contentId + "_" + attributeName + template.hashCode();
			String fileName = uniqueId + ".pdf";
			
			int i = 0;
			String filePath = CmsPropertyHandler.getDigitalAssetPath0();
			while(filePath != null)
			{
				File pdfFile = new File(filePath + java.io.File.separator + fileName);
				if(!pdfFile.exists())
				{
					logger.info("Creating a foprenderer");
					FOPHelper fop = new FOPHelper();
					fop.generatePDF(template, pdfFile);
				}
				i++;
				filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
			}
			
			SiteNode siteNode = this.nodeDeliveryController.getSiteNode(getDatabase(), this.siteNodeId);
			String dnsName = CmsPropertyHandler.getWebServerAddress();
			if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
				dnsName = siteNode.getRepository().getDnsName();

			//pdfUrl = dnsName + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + fileName;
			pdfUrl = urlComposer.composeDigitalAssetUrl(dnsName, null, fileName, deliveryContext); 
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get ContentAttribute As PDFUrl:" + e.getMessage(), e);
		}
		
		return pdfUrl;
	}
	
	/**
	 * This method deliveres a String with the content-attribute asked for generated as a gif-file.
	 * That is - the text is printed as an image. You can specify a number of things to control the 
	 * generation. Just experiment and the names are pretty much self explainatory.
	 * The method checks if a previous file exists that has the same attributes as the wanted one
	 * and if so - we don't generate it again.
	 * 
	 * TODO: consider implement a more general getTextAsImageUrl so we dont need a zillion different variants
	 * for different ways to access the contentAttribute. (we need to calculate a unique string from
	 * the text and all the other stuff.)
	 * 	 
	 */
	
	public String getContentAttributeAsImageUrl(String contentBindningName, String attributeName, int canvasWidth, int canvasHeight) 
	{
		// Set some default values and pass on
		return getContentAttributeAsImageUrl(contentBindningName, attributeName, canvasWidth, canvasHeight, 5, 20, canvasWidth - 10, canvasHeight - 10, "Verdana", Font.BOLD, 28, Color.black, Color.white);
	}

	public String getContentAttributeAsImageUrl(String contentBindningName,String attributeName,int canvasWidth, 
													int canvasHeight,int textStartPosX,int textStartPosY,int textWidth, 
													int textHeight,String fontName,int fontStyle, 
													int fontSize,String foregroundColor,String backgroundColor) 
	{
		// Using contentBindingName: Convert color parameters and pass on
		return getContentAttributeAsImageUrl(	contentBindningName,attributeName,canvasWidth,canvasHeight,textStartPosX,textStartPosY,textWidth,textHeight,fontName,fontStyle,fontSize, 
												new Color(getMathHelper().hexToDecimal(foregroundColor)),
												new Color(getMathHelper().hexToDecimal(backgroundColor)));
		
	}

	public String getContentAttributeAsImageUrl(String contentBindningName,String attributeName,int canvasWidth, 
														int canvasHeight,int textStartPosX,int textStartPosY,int textWidth, 
														int textHeight,String fontName,int fontStyle, 
														int fontSize,String foregroundColor,String backgroundColor, String backgroundImageUrl) 
	{
		// Using contentBindingName: Convert color parameters and pass on
		return getContentAttributeAsImageUrl(	contentBindningName,attributeName,canvasWidth,canvasHeight,textStartPosX,textStartPosY,textWidth,textHeight,fontName,fontStyle,fontSize, 
												new Color(getMathHelper().hexToDecimal(foregroundColor)),
												new Color(getMathHelper().hexToDecimal(backgroundColor)));
	
	}
	
	public String getContentAttributeAsImageUrl(Integer contentId,String attributeName,int canvasWidth, 
													int canvasHeight,int textStartPosX,int textStartPosY, 
													int textWidth,int textHeight,String fontName,int fontStyle, 
													int fontSize,String foregroundColor,String backgroundColor) 
	{
		// Using contentId: Convert color parameters and pass on
		return getContentAttributeAsImageUrl(contentId,attributeName,canvasWidth,canvasHeight,textStartPosX,textStartPosY,textWidth,textHeight,fontName,fontStyle,fontSize,
												new Color(getMathHelper().hexToDecimal(foregroundColor)),
												new Color(getMathHelper().hexToDecimal(backgroundColor)));
	}

	public String getContentAttributeAsImageUrl(String contentBindningName,String attributeName,int canvasWidth,int canvasHeight,int textStartPosX,int textStartPosY,int textWidth,int textHeight,String fontName,int fontStyle,int fontSize,Color foregroundColor,	Color backgroundColor) 
	{
		// Get the contentId from the contentBindingName and pass on
		String assetUrl = "";
		try 
		{
			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);
			if(contentVO != null)
				assetUrl = getContentAttributeAsImageUrl(contentVO.getContentId(),attributeName,canvasWidth,canvasHeight,textStartPosX,textStartPosY,textWidth,textHeight,fontName,fontStyle,fontSize,foregroundColor,backgroundColor);			
		} 
		catch(Exception e) 
		{
			logger.error("An error occurred trying to get ContentAttribute As ImageUrl:" + e.getMessage(), e);
		}
		
		return assetUrl;
	}
	
	public String getContentAttributeAsImageUrl(String contentBindningName, String attributeName, int canvasWidth, int canvasHeight, int textStartPosX, int textStartPosY, int textWidth, int textHeight, String fontName, int fontStyle, int fontSize, Color foregroundColor, Color backgroundColor, String backgroundImageUrl) 
	{
		// Get the contentId from the contentBindingName and pass on
		String assetUrl = "";
		try 
		{
			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);
			if(contentVO != null)
				assetUrl = getContentAttributeAsImageUrl(contentVO.getContentId(),attributeName,canvasWidth,canvasHeight,textStartPosX,textStartPosY,textWidth,textHeight,fontName,fontStyle,fontSize,foregroundColor,backgroundColor, backgroundImageUrl);			
		} 
		catch(Exception e) 
		{
			logger.error("An error occurred trying to get ContentAttribute As ImageUrl:" + e.getMessage(), e);
		}
	
		return assetUrl;
	}	
	

	public String getContentAttributeAsImageUrl(Integer contentId,String attributeName,int canvasWidth, 
													int canvasHeight,int textStartPosX,int textStartPosY, 
													int textWidth,int textHeight,String fontName, 
													int fontStyle,int fontSize, 
													Color foregroundColor,Color backgroundColor) 
	{			
		return getContentAttributeAsImageUrl(contentId, attributeName, canvasWidth, canvasHeight, textStartPosX, textStartPosY, textWidth, textHeight, fontName, fontStyle, fontSize, foregroundColor, backgroundColor, null);
	}


	/**
     * Renders a text from values configured in a content, iterates over the
     * contenttype defenition names and look for font properties.
     * @param contentId a content id containing attributes to tell the image
     *            renderer how to look.
     * @param text the text to render
     * @param renderAttributes render attributes in a map to override the
     *            content settings
     * @return the asseturl or empty string if something is wrong or text is null or empty
     * @author Per Jonsson per.jonsson@it-huset.se
     */
    public String getRenderedTextUrl( Integer contentId, String text, Map renderAttributes )
    {
        String assetUrl = "";
        if ( text == null || text.length() == 0 )
        {
            logger.warn("Could not render text with a null or 0 lenght value on sitenode: " + this.getSiteNodeId() + ", contentId = " + contentId );
            return assetUrl;
        }
        try
        {
            ContentDeliveryController cdc = ContentDeliveryController.getContentDeliveryController();
            ContentTypeDefinitionController ctdc = ContentTypeDefinitionController.getController();

            ContentVersionVO contentVersionVO = cdc.getContentVersionVO(getDatabase(), this.siteNodeId, contentId,this.languageId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal );
            
            Integer contentTypeDefinitionId = cdc.getContentVO(contentId, getDatabase() ).getContentTypeDefinitionId();
          
            ContentTypeDefinitionVO contentTypeDefinitionVO = ctdc.getContentTypeDefinitionVOWithId( contentTypeDefinitionId );
            Iterator attrIterator = ctdc.getContentTypeAttributes(contentTypeDefinitionVO, true).iterator();
            
            String aText = text.replaceAll( "[^\\w]", "" );
            aText = aText.substring( 0, ( aText.length() < 8 ? aText.length() : 8 ) ).toLowerCase();
            StringBuffer uniqueId = new StringBuffer( aText );
            uniqueId.append( "_" + contentVersionVO.getId() );
            uniqueId.append( "_" + Math.abs( text.hashCode() ));
            uniqueId.append( "_" + Math.abs(contentVersionVO.getVersionValue().hashCode() ) );
            uniqueId.append( "_" + Math.abs(contentTypeDefinitionVO.getSchemaValue().hashCode() ) );
            uniqueId.append( "_" + ( renderAttributes != null ? Math.abs( renderAttributes.hashCode() ) : 4711 ) );

            AdvancedImageRenderer imageRenderer = new AdvancedImageRenderer();
            // set up the renderer
            while ( attrIterator.hasNext() )
            {
                ContentTypeAttribute contentTypeAttribute = (ContentTypeAttribute)(attrIterator.next());
                String attributeName = contentTypeAttribute.getName();
                if ( imageRenderer.hasAttribute( attributeName ) )
                {
                    String attribute = cdc.getContentAttribute(getDatabase(), contentVersionVO, attributeName, false );
                    imageRenderer.setAttribute( attributeName.toLowerCase(), attribute );
                }
            }
            // render the image
            imageRenderer.renderImage( text, renderAttributes );
            
            String fileName = uniqueId + "." + imageRenderer.getImageFormatName();	// default is png
            // write the result
            assetUrl = writeRenderedImage( imageRenderer, fileName );
        }
        catch ( Exception e )
        {
            logger.error( "An error occurred trying to getRenderedTextUrl(), text = " + text + ", on siteNodeId = " + this.siteNodeId + " :" + e.getMessage(), e );
        }

        return assetUrl;
    }

    /**
     * Renders a text from configuration stored in the propertyfile or in the
     * map.
     * @param text the text to render
     * @param renderAttributes render attributes in a map to override the
     *            default or propertyfile settings
     * @return the asseturl or empty string if something is wrong or text is null or empty
     * @author Per Jonsson - per.jonsson@it-huset.se
     */
    public String getRenderedTextUrl( String text, Map renderAttributes )
    {
    	return getRenderedTextUrl( text, renderAttributes, false);
	}
	
    public String getRenderedTextUrl( String text, Map renderAttributes, boolean distort )
    {
        String assetUrl = "";
        if ( text == null || text.length() == 0 )
        {
            logger.warn("Could not render text with a null or 0 lenght value on sitenode: " + this.getSiteNodeId() );
            return assetUrl;
        }
        try
        {
            String aText = text.replaceAll( "[^\\w]", "" );
            aText = aText.substring( 0, ( aText.length() < 12 ? aText.length() : 11 ) ).toLowerCase();
            StringBuffer uniqueId = new StringBuffer( aText );
            uniqueId.append( "_" + Math.abs( text.hashCode() ) );
            uniqueId.append( "_" + ( renderAttributes != null ? Math.abs( renderAttributes.hashCode() ) : 4711 ) );

            AdvancedImageRenderer imageRenderer = new AdvancedImageRenderer();
            // render the image
            imageRenderer.renderImage( text, renderAttributes );
            if( distort ) 
            {
            	uniqueId = new StringBuffer( "igcaptcha" );
                uniqueId.append( "_" + Math.abs( text.hashCode() ) );
                uniqueId.append( "_" + ( renderAttributes != null ? Math.abs( renderAttributes.hashCode() ) : 4711 ) );
            	imageRenderer.distortImage();
            }

            String fileName = uniqueId + "." + imageRenderer.getImageFormatName();	// default is png

            // write the result
            assetUrl = writeRenderedImage( imageRenderer, fileName );
        }
        catch ( Exception e )
        {
            logger.error( "An error occurred trying to getRenderedTextUrl(), text = " + text + ", on siteNodeId = " + this.siteNodeId + " :" + e.getMessage(), e );
        }

        return assetUrl;
    }


    /**
     * Writes a rendered imagefile,
     * @param imageRenderer a valid configured
     * @param fileName the filename of the image
     * @return an asseturl of the image
     * @throws SystemException if something goes wrong
     * @author Per Jonsson - per.jonsson@it-huset.se
     */
    private String writeRenderedImage( AdvancedImageRenderer imageRenderer, String fileName ) throws SystemException
    {
        // write the result
        int i = 0;
        String filePath = CmsPropertyHandler.getDigitalAssetPath0();
        while ( filePath != null )
        {
            File imageFile = new File( filePath, fileName );
            if ( !imageFile.exists() )
            {
                imageRenderer.writeImage( imageFile );
            }
            i++;
            filePath = CmsPropertyHandler.getProperty( "digitalAssetPath." + i );
        }

        SiteNode siteNode = this.nodeDeliveryController.getSiteNode( getDatabase(), this.siteNodeId );
        String dnsName = CmsPropertyHandler.getWebServerAddress();
        if ( siteNode != null && siteNode.getRepository().getDnsName() != null
                && !siteNode.getRepository().getDnsName().equals( "" ) )
        {
            dnsName = siteNode.getRepository().getDnsName();
        }

        return urlComposer.composeDigitalAssetUrl( dnsName, null, fileName, deliveryContext );
    }
    
    
	public String getContentAttributeAsImageUrl(Integer contentId,String attributeName,int canvasWidth, 
													int canvasHeight,int textStartPosX,int textStartPosY, 
													int textWidth,int textHeight,String fontName, 
													int fontStyle,int fontSize, 
													Color foregroundColor,Color backgroundColor, String backgroundImageUrl) 
	{
		// This one actually does something.
		String assetUrl = "";

		try
		{
			ContentVersionVO contentVersionVO = ContentDeliveryController.getContentDeliveryController().getContentVersionVO(getDatabase(), this.siteNodeId, contentId, this.languageId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);		
			
			String attribute = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), contentVersionVO, attributeName, false);
			
			String uniqueId = contentVersionVO.getId() + "_" + attributeName + canvasWidth + canvasHeight + textStartPosX + textStartPosY + textWidth + textHeight + fontName.replaceAll(" ", "") + fontStyle + fontSize + foregroundColor.getRed() + foregroundColor.getGreen() + foregroundColor.getBlue() + backgroundColor.getRed() + backgroundColor.getGreen() + backgroundColor.getBlue();
			
			String fileName = uniqueId + ".png";
			
			int i = 0;
			String filePath = CmsPropertyHandler.getDigitalAssetPath0();
			while(filePath != null)
			{
				File imageFile = new File(filePath + java.io.File.separator + fileName);
				if(!imageFile.exists())
				{
					logger.info("Creating a imagerenderer");
					ImageRenderer imageRenderer = new ImageRenderer();
					imageRenderer.setCanvasWidth(canvasWidth);
	    			imageRenderer.setCanvasHeight(canvasHeight);
			    	imageRenderer.setTextStartPosX(textStartPosX);
			    	imageRenderer.setTextStartPosY(textStartPosY);
			    	imageRenderer.setTextWidth(textWidth);
			    	imageRenderer.setTextHeight(textHeight);
					imageRenderer.setFontName(fontName);
					imageRenderer.setFontStyle(fontStyle);
					imageRenderer.setFontSize(fontSize);
					imageRenderer.setForeGroundColor(foregroundColor);
					imageRenderer.setBackgroundColor(backgroundColor);
					imageRenderer.setBackgroundImageUrl(backgroundImageUrl);
					
					logger.info("Created imageRenderer and printing to " + filePath + java.io.File.separator + fileName);					
					imageRenderer.generateGifImageFromText(filePath + java.io.File.separator + fileName, attribute, LanguageDeliveryController.getLanguageDeliveryController().getLanguageVO(getDatabase(), this.languageId).getCharset());
					logger.info("Rendered in getContentAttributeAsImageUrl");
				}

				i++;
				filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
			}
			/*
			String filePath = CmsPropertyHandler.getDigitalAssetPath();
			File imageFile = new File(filePath + java.io.File.separator + fileName);
			if(!imageFile.exists())
			{
				logger.info("Creating a imagerenderer");
				ImageRenderer imageRenderer = new ImageRenderer();
				imageRenderer.setCanvasWidth(canvasWidth);
    			imageRenderer.setCanvasHeight(canvasHeight);
		    	imageRenderer.setTextStartPosX(textStartPosX);
		    	imageRenderer.setTextStartPosY(textStartPosY);
		    	imageRenderer.setTextWidth(textWidth);
		    	imageRenderer.setTextHeight(textHeight);
				imageRenderer.setFontName(fontName);
				imageRenderer.setFontStyle(fontStyle);
				imageRenderer.setFontSize(fontSize);
				imageRenderer.setForeGroundColor(foregroundColor);
				imageRenderer.setBackgroundColor(backgroundColor);
				imageRenderer.setBackgroundImageUrl(backgroundImageUrl);
				
				logger.info("Created imageRenderer and printing to " + filePath + java.io.File.separator + fileName);					
				imageRenderer.generateGifImageFromText(filePath + java.io.File.separator + fileName, attribute, LanguageDeliveryController.getLanguageDeliveryController().getLanguageVO(getDatabase(), this.languageId).getCharset());
				logger.info("Rendered in getContentAttributeAsImageUrl");
			}
			*/
			
			SiteNode siteNode = this.nodeDeliveryController.getSiteNode(getDatabase(), this.siteNodeId);
			String dnsName = CmsPropertyHandler.getWebServerAddress();
			if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
				dnsName = siteNode.getRepository().getDnsName();

			//assetUrl = dnsName + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + fileName;
			assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, null, fileName, deliveryContext); 
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get ContentAttribute As ImageUrl:" + e.getMessage(), e);
		}
				
		return assetUrl;
	}

	/**
	 * This method returns a list of elements/attributes based on the contentType sent in. 
	 */
	
	public List getContentAttributes(String schemaValue)
	{
		return ContentTypeDefinitionController.getController().getContentTypeAttributes(schemaValue);
	}
	
	/**
	 * This method deliveres a String with the content-attribute asked for generated as a png-file.
	 * That is - the text is printed as an image. You can specify a number of things to control the 
	 * generation. Just experiment and the names are pretty much self explainatory.
	 * The method checks if a previous file exists that has the same attributes as the wanted one
	 * and if so - we don't generate it again.
	 */
	public String getStringAsImageUrl(String text,
										   int canvasWidth, 
										   int canvasHeight,
										   int textStartPosX,
										   int textStartPosY, 
										   int textWidth,
										   int textHeight,
										   String fontName, 
										   int fontStyle,
										   int fontSize, 
										   String foregroundColor,
										   String backgroundColor) 
		{
			return 	getStringAsImageUrl(text, canvasWidth, canvasHeight,textStartPosX, textStartPosY,textWidth,textHeight,fontName,fontStyle,fontSize, 
			new Color(getMathHelper().hexToDecimal(foregroundColor)),
			new Color(getMathHelper().hexToDecimal(backgroundColor)));
		}
	public String getStringAsImageUrl(String text,
			   int canvasWidth, 
			   int canvasHeight,
			   int textStartPosX,
			   int textStartPosY, 
			   int textWidth,
			   int textHeight,
			   String fontName, 
			   int fontStyle,
			   int fontSize, 
			   String foregroundColor,
			   String backgroundColor, String backGroundImageUrl) 
	{
		return 	getStringAsImageUrl(text, canvasWidth, canvasHeight,textStartPosX, textStartPosY,textWidth,textHeight,fontName,fontStyle,fontSize, 
		new Color(getMathHelper().hexToDecimal(foregroundColor)),
		new Color(getMathHelper().hexToDecimal(backgroundColor)), backGroundImageUrl);
	}

	public String getStringAsImageUrl(String text,
			   int canvasWidth, 
			   int canvasHeight,
			   int textStartPosX,
			   int textStartPosY, 
			   int textWidth,
			   int textHeight,
			   String fontName, 
			   int fontStyle,
			   int fontSize, 
			   Color foregroundColor,
			   Color backgroundColor) 
	{
		 return getStringAsImageUrl(text,canvasWidth,canvasHeight,textStartPosX,textStartPosY,textWidth,textHeight,fontName,fontStyle,fontSize,foregroundColor,backgroundColor,null);   
	}

	public String getStringAsImageUrl(String text,
									   int canvasWidth, 
									   int canvasHeight,
									   int textStartPosX,
									   int textStartPosY, 
									   int textWidth,
									   int textHeight,
									   String fontName, 
									   int fontStyle,
									   int fontSize, 
									   Color foregroundColor,
									   Color backgroundColor, String backgroundImageUrl) 
	{
		String assetUrl = "";
		 
		try
		{
			String uniqueId = text.hashCode() + "_" + canvasWidth + canvasHeight + textStartPosX + textStartPosY + textWidth + textHeight + fontName.replaceAll(" ", "") + fontStyle + fontSize + foregroundColor.getRed() + foregroundColor.getGreen() + foregroundColor.getBlue() + backgroundColor.getRed() + backgroundColor.getGreen() + backgroundColor.getBlue();
			
			String fileName = uniqueId + ".png";
			
			int i = 0;
			String filePath = CmsPropertyHandler.getDigitalAssetPath0();
			while(filePath != null)
			{
				File imageFile = new File(filePath + java.io.File.separator + fileName);
				if(!imageFile.exists())
				{
					logger.info("Creating a imagerenderer");
					ImageRenderer imageRenderer = new ImageRenderer();
					imageRenderer.setCanvasWidth(canvasWidth);
	    			imageRenderer.setCanvasHeight(canvasHeight);
			    	imageRenderer.setTextStartPosX(textStartPosX);
			    	imageRenderer.setTextStartPosY(textStartPosY);
			    	imageRenderer.setTextWidth(textWidth);
			    	imageRenderer.setTextHeight(textHeight);
					imageRenderer.setFontName(fontName);
					imageRenderer.setFontStyle(fontStyle);
					imageRenderer.setFontSize(fontSize);
					imageRenderer.setForeGroundColor(foregroundColor);
					imageRenderer.setBackgroundColor(backgroundColor);
					imageRenderer.setBackgroundImageUrl(backgroundImageUrl);
					
					logger.info("Created imageRenderer and printing to " + filePath + java.io.File.separator + fileName);					
					imageRenderer.generateGifImageFromText(filePath + java.io.File.separator + fileName, text, LanguageDeliveryController.getLanguageDeliveryController().getLanguageVO(getDatabase(), this.languageId).getCharset());
					logger.info("Rendered in getContentAttributeAsImageUrl");
				}

				i++;
				filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
			}
			/*
			String filePath = CmsPropertyHandler.getDigitalAssetPath();
			File imageFile = new File(filePath + java.io.File.separator + fileName);
			if(!imageFile.exists())
			{
				logger.info("Creating a imagerenderer");
				ImageRenderer imageRenderer = new ImageRenderer();
				imageRenderer.setCanvasWidth(canvasWidth);
    			imageRenderer.setCanvasHeight(canvasHeight);
		    	imageRenderer.setTextStartPosX(textStartPosX);
		    	imageRenderer.setTextStartPosY(textStartPosY);
		    	imageRenderer.setTextWidth(textWidth);
		    	imageRenderer.setTextHeight(textHeight);
				imageRenderer.setFontName(fontName);
				imageRenderer.setFontStyle(fontStyle);
				imageRenderer.setFontSize(fontSize);
				imageRenderer.setForeGroundColor(foregroundColor);
				imageRenderer.setBackgroundColor(backgroundColor);
				imageRenderer.setBackgroundImageUrl(backgroundImageUrl);
				
				logger.info("Created imageRenderer and printing to " + filePath + java.io.File.separator + fileName);					
				imageRenderer.generateGifImageFromText(filePath + java.io.File.separator + fileName, text, LanguageDeliveryController.getLanguageDeliveryController().getLanguageVO(getDatabase(), this.languageId).getCharset());
				logger.info("Rendered in getContentAttributeAsImageUrl");
			}
			*/
			
			SiteNode siteNode = this.nodeDeliveryController.getSiteNode(getDatabase(), this.siteNodeId);
			String dnsName = CmsPropertyHandler.getWebServerAddress();
			if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
				dnsName = siteNode.getRepository().getDnsName();
				
			//assetUrl = dnsName + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + fileName;
			assetUrl = urlComposer.composeDigitalAssetUrl(dnsName, null, fileName, deliveryContext); 
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to render string as an image:" + e.getMessage(), e);
		}
				
		return assetUrl;
	}

	
	/**
	 * This method returns the base url for the digital assets.
	 */
	
	public String getDigitalAssetBaseUrl() throws Exception
	{
		String url = getRepositoryBaseUrl() + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl();
		
		return url;
	}

	/**
	 * This method returns the Id the digital assets.
	 */
	
	public Integer getDigitalAssetId(Integer contentId, String assetKey) throws Exception
	{
		return ContentDeliveryController.getContentDeliveryController().getDigitalAssetId(getDatabase(), contentId, this.languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
	}

	/**
	 * This method returns the Id the digital assets.
	 */
	
	public Integer getDigitalAssetId(Integer contentId, Integer languageId, String assetKey) throws Exception
	{
		return ContentDeliveryController.getContentDeliveryController().getDigitalAssetId(getDatabase(), contentId, languageId, assetKey, this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal);
	}

	
	/**
	 * This method gets a property from the extra properties in the repository currently active
	 */
	
	public String getRepositoryExtraProperty(String propertyName)
	{
		String propertyValue = RepositoryDeliveryController.getRepositoryDeliveryController().getExtraPropertyValue(this.getSiteNode().getRepositoryId(), propertyName);
		
	    return propertyValue;
	}

	
    /**
     * This method returns the first repository with name.
     */
    public RepositoryVO getRepositoryWithName(String name)
    {
    	try
    	{
    		return RepositoryDeliveryController.getRepositoryDeliveryController().getRepositoryVOWithName(name, getDatabase());
    	}
    	catch (Exception e) 
    	{
    		logger.error("Problem getting repository with name:" + name + " - reason:" + e.getMessage());
    		return null;
		}
    }

	/**
	 * This method returns the base url for the digital assets.
	 */
	
	public String getRepositoryBaseUrl() throws Exception
	{
		String url = "";
	
        String context = CmsPropertyHandler.getServletContext();

		SiteNode siteNode = this.nodeDeliveryController.getSiteNode(getDatabase(), this.siteNodeId);
		String dnsName = CmsPropertyHandler.getWebServerAddress();
		if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
		{
			dnsName = siteNode.getRepository().getDnsName();

	        String useDNSNameInUrls = CmsPropertyHandler.getUseDNSNameInURI();
	 
	        if(!useDNSNameInUrls.equalsIgnoreCase("false"))
	        {
	        	String operatingMode = CmsPropertyHandler.getOperatingMode();
			    String keyword = "";
			    if(operatingMode.equalsIgnoreCase("0"))
			        keyword = "working=";
			    else if(operatingMode.equalsIgnoreCase("2"))
			        keyword = "preview=";
			    if(operatingMode.equalsIgnoreCase("3"))
			        keyword = "live=";
			    
			    if(dnsName != null)
			    {
	    		    int startIndex = dnsName.indexOf(keyword);
	    		    if(startIndex != -1)
	    		    {
	    		        int endIndex = dnsName.indexOf(",", startIndex);
	        		    if(endIndex > -1)
	    		            dnsName = dnsName.substring(startIndex, endIndex);
	    		        else
	    		            dnsName = dnsName.substring(startIndex);
	    		        
	    		        dnsName = dnsName.split("=")[1];
	    		    }
	    		    else
	    		    {
	    		        int endIndex = dnsName.indexOf(",");
	    		        if(endIndex > -1)
	    		            dnsName = dnsName.substring(0, endIndex);
	    		        else
	    		            dnsName = dnsName.substring(0);
	    		        
	    		    }
			    }
		            
	            dnsName = dnsName + context;
	        }
	        else
	        {
	        	dnsName = context;
	        }
		}
		
		url = dnsName;
		
		return url;
	}

	/**
	 * This method returns the root node for the current repository.
	 */
	
	public Integer getRepositoryId()
	{
	    return this.getSiteNode().getRepositoryId();
	}

	/**
	 * This method returns the root node for the current repository.
	 */
	
	public SiteNodeVO getRepositoryRootSiteNode() throws Exception
	{
	    Integer repositoryId = this.getSiteNode(this.siteNodeId).getRepositoryId();
	    SiteNodeVO siteNodeVO = getRepositoryRootSiteNode(repositoryId);		

	    return siteNodeVO;
	}

	/**
	 * This method returns the root node for the current repository.
	 */
	
	public SiteNodeVO getRepositoryRootSiteNode(Integer repositoryId) throws Exception
	{
	    SiteNodeVO siteNodeVO = this.nodeDeliveryController.getRootSiteNode(getDatabase(), repositoryId);		

	    return siteNodeVO;
	}

	/**
	 * This method returns the parent repositoryId if any for the given repository.
	 */
	
	public Integer getParentRepositoryId(Integer repositoryId)
	{
	    String parentRepository = RepositoryDeliveryController.getRepositoryDeliveryController().getPropertyValue(repositoryId, "parentRepository");
	    if(parentRepository != null && !parentRepository.equalsIgnoreCase(""))
	        return new Integer(parentRepository);
	    else
	        return null;
	}

	/**
	 * This method returns the parent repositoryId if any for the given repository.
	 */
	
	public Integer getParentRepositoryId()
	{
	    Integer repositoryId = this.getSiteNode(this.siteNodeId).getRepositoryId();
	    return getParentRepositoryId(repositoryId);
	}

	/**
	 * This method deliveres a String with the URL to the page asked for.
	 * As the siteNode can have multiple bindings the method requires a bindingName 
	 * which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getPageUrl(String structureBindningName) 
	{
		String pageUrl = "";
		
		try
		{
			SiteNodeVO siteNodeVO = this.nodeDeliveryController.getBoundSiteNode(getDatabase(), this.siteNodeId, structureBindningName);		
			if(siteNodeVO != null)
				pageUrl = this.nodeDeliveryController.getPageUrl(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get page url for structureBindningName " + structureBindningName + ":" + e.getMessage(), e);
		}
				
		return pageUrl;
	}


	/**
	 * This method just gets a new URL but with the given contentId in it.
	 */
	 
	public String getPageUrl(WebPage webpage, Integer contentId) 
	{
		String pageUrl = "";
		
		try
		{
			pageUrl = this.nodeDeliveryController.getPageUrl(getDatabase(), this.getPrincipal(), webpage.getSiteNodeId(), webpage.getLanguageId(), contentId, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the transformed page url " + contentId + ":" + e.getMessage(), e);
		}
				
		return pageUrl;
	}


	/**
	 * This method deliveres a String with the URL to the page asked for.
	 * As the siteNode can have multiple bindings the method requires a bindingName 
	 * which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getPageBaseUrl(String structureBindningName) 
	{
		String pageUrl = "";
		
		try
		{
			pageUrl = this.nodeDeliveryController.getPageBaseUrl(getDatabase());
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get page url for structureBindningName " + structureBindningName + ":" + e.getMessage(), e);
		}
				
		return pageUrl;
	}

	/**
	 * This method deliveres a String with the URL to the page asked for.
	 * As the siteNode can have multiple bindings the method requires a bindingName 
	 * which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getPageBaseUrl() 
	{
		String pageUrl = "";
		
		try
		{
			pageUrl = this.nodeDeliveryController.getPageBaseUrl(getDatabase());
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get page url the current url:" + e.getMessage(), e);
		}
				
		return pageUrl;
	}

	/**
	 * Getter for the current siteNode
	 */
	
	public SiteNodeVO getSiteNode()
	{
	    SiteNodeVO siteNodeVO = null;

		try
		{
			siteNodeVO = this.nodeDeliveryController.getSiteNodeVO(getDatabase(), this.siteNodeId);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get the current siteNode with id " + this.siteNodeId + " on URL: " + getOriginalFullURL() + "\nMessage: " + e.getMessage() + "");
		}

		return siteNodeVO;
	}


	/**
	 * This method fetches the given siteNode
	 */
	
	public SiteNodeVO getSiteNode(Integer siteNodeId)
	{
	    SiteNodeVO siteNodeVO = null;

		try
		{
			siteNodeVO = this.nodeDeliveryController.getSiteNodeVO(getDatabase(), siteNodeId);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get the siteNode with id " + siteNodeId + " on URL: " + getOriginalFullURL() + "\nMessage: " + e.getMessage() + "");
		}

		return siteNodeVO;
	}

	/**
	 * Getter for the siteNodeId on a specific bound page
	 */
	
	public Integer getSiteNodeId(String structureBindningName)
	{
		Integer siteNodeId = null;
		
		try
		{
			SiteNodeVO siteNodeVO = this.nodeDeliveryController.getBoundSiteNode(getDatabase(), this.siteNodeId, structureBindningName);		
			siteNodeId = siteNodeVO.getSiteNodeId();
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get siteNodeId for structureBindningName " + structureBindningName + ":" + e.getMessage(), e);
		}
				
		return siteNodeId;
	}


	/**
	 * Gets a corresponding list of siteNodeVO:s from a list of webpages.
	 */
	
	public Collection<SiteNodeVO> getSiteNodesFromWebPages(Collection webPages)
	{
	    Collection<SiteNodeVO> siteNodeVOList = new ArrayList<SiteNodeVO>();

		try
		{
			Iterator webPagesIterator = webPages.iterator();
			while(webPagesIterator.hasNext())
			{
				WebPage webPage = (WebPage)webPagesIterator.next();
				SiteNodeVO siteNodeVO = this.getSiteNode(webPage.getSiteNodeId());
				siteNodeVOList.add(siteNodeVO);				
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to convert the list of webpages:" + e.getMessage(), e);
		}

		return siteNodeVOList;
	}

	/**
	 * Getter for bound contentId for a binding
	 */
	
	public Integer getContentId(String contentBindningName)
	{
		Integer contentId = null;
		
		try
		{
			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
			if(contentVO != null)
			{
				contentId = contentVO.getId();
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get contentId for contentBindningName " + contentBindningName + ":" + e.getMessage(), e);
		}
				
		return contentId;
	}
	
	/**
	 * This method gets the meta information of the current sitenode.
	 */
	
	public Integer getMetaInformationContentId()
	{
		return this.getContentId(META_INFO_BINDING_NAME);
	}
	
	/**
	 * This method gets the meta information of a particular sitenode.
	 */
	
	public Integer getMetaInformationContentId(Integer siteNodeId)
	{
		return this.getContentId(siteNodeId, META_INFO_BINDING_NAME);
	}
	
	/**
	 * This method gets the children of a content.
	 */
	
	public Collection getChildContents(Integer contentId, boolean includeFolders)
	{
		List childContents = null;
		
		try
		{
			childContents = ContentDeliveryController.getContentDeliveryController().getChildContents(getDatabase(), this.getPrincipal(), contentId, this.languageId, USE_LANGUAGE_FALLBACK, includeFolders, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get childContents for contentId " + contentId + ":" + e.getMessage(), e);
		}
				
		return childContents;
	}


	/**
	 * Getter for bound contentId for a binding on a special siteNode
	 */
	
	public Integer getContentId(Integer siteNodeId, String contentBindningName)
	{
		Integer contentId = null;
		
		try
		{
			ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
			if(contentVO != null)
			{
				contentId = contentVO.getId();
			}
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get contentId for contentBindningName " + contentBindningName + ":" + e.getMessage(), e);
		}
				
		return contentId;
	}

	/**
	 * This method gets the content related to a category
	 */
	public List getContentVersionsByCategory(Integer categoryId, String attributeName)
	{
		try
		{
			return ContentDeliveryController.getContentDeliveryController().findContentVersionVOsForCategory(getDatabase(), categoryId, attributeName, getPrincipal(), siteNodeId, languageId, USE_LANGUAGE_FALLBACK, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get Content for categoryId " + categoryId + ":" + e.getMessage(), e);
		}

		return Collections.EMPTY_LIST;
	}

	/**
	 * This method searches for all content versions which was last modified by a given user.
	 */

	public List getPrincipalContentVersions(String contentTypeDefinitionName, String principalName, Date publishStartDate, Date publishEndDate, Date unpublishStartDate, Date unpublishEndDate)
	{
		try
		{
	        Integer contentTypeDefinitionId = null;
			ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName(contentTypeDefinitionName, getDatabase());
	        if(contentTypeDefinitionVO != null)
	        	contentTypeDefinitionId = contentTypeDefinitionVO.getId();

	        Set contentVersions = SearchController.getContentVersions(contentTypeDefinitionId, principalName, publishStartDate, publishEndDate, unpublishStartDate, unpublishEndDate);
			
			List result = new ArrayList();
			for(Iterator i = contentVersions.iterator(); i.hasNext(); ) 
			{
				ContentVersionVO contentVersionVO = (ContentVersionVO)i.next();
				if(ContentDeliveryController.getContentDeliveryController().isValidContent(this.getDatabase(), contentVersionVO.getContentId(), this.languageId, USE_LANGUAGE_FALLBACK, true, getPrincipal(), this.deliveryContext))
					result.add(contentVersionVO);
			}

			return result;
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get content version which was last changed by:" + principalName + ":" + e.getMessage(), e);
		}

		return Collections.EMPTY_LIST;
	}

	/**
	 * This method searches for all contents matching
	 */

	public List getMatchingContents(String contentTypeDefinitionNamesString, String categoryConditionString, boolean useLanguageFallback)
	{
		return getMatchingContents(contentTypeDefinitionNamesString, categoryConditionString, null, null, null, null, useLanguageFallback);
	}

	/**
	 * This method searches for all contents matching
	 */

	public List getMatchingContents(String contentTypeDefinitionNamesString, String categoryConditionString, String freeText, List freeTextAttributeNames, Date fromDate, Date toDate, boolean useLanguageFallback)
	{
		return getMatchingContents(contentTypeDefinitionNamesString, categoryConditionString, freeText, freeTextAttributeNames, fromDate, toDate, useLanguageFallback, false, -1, null, null);
	}

	/**
	 * This method searches for all contents matching
	 */

	public List getMatchingContents(String contentTypeDefinitionNamesString, String categoryConditionString, String freeText, List freeTextAttributeNames, Date fromDate, Date toDate, boolean useLanguageFallback, boolean cacheResult, int cacheInterval, String cacheName, String cacheKey)
	{
		return getMatchingContents(contentTypeDefinitionNamesString, categoryConditionString, freeText, freeTextAttributeNames, fromDate, toDate, useLanguageFallback, cacheResult, cacheInterval, cacheName, cacheKey, null);
	}

	/**
	 * This method searches for all contents matching
	 */
	
	public List getMatchingContents(String contentTypeDefinitionNamesString, String categoryConditionString, String freeText, List freeTextAttributeNames, Date fromDate, Date toDate, boolean useLanguageFallback, boolean cacheResult, int cacheInterval, String cacheName, String cacheKey, List<Integer> repositoryIdList)
	{
		return getMatchingContents(contentTypeDefinitionNamesString, categoryConditionString, freeText, freeTextAttributeNames, fromDate, toDate, null, null, null, null, useLanguageFallback, cacheResult, cacheInterval, cacheName, cacheKey, repositoryIdList);
	}

	/**
	 * This method searches for all contents matching
	 */
	
	public List getMatchingContents(String contentTypeDefinitionNamesString, String categoryConditionString, String freeText, List freeTextAttributeNames, Date fromDate, Date toDate, Date expireFromDate, Date expireToDate, String versionModifier, boolean useLanguageFallback, boolean cacheResult, int cacheInterval, String cacheName, String cacheKey, List<Integer> repositoryIdList)
	{
		return getMatchingContents(contentTypeDefinitionNamesString, categoryConditionString, freeText, freeTextAttributeNames, fromDate, toDate, expireFromDate, expireToDate, versionModifier, null, useLanguageFallback, cacheResult, cacheInterval, cacheName, cacheKey, repositoryIdList);
	}

	/**
	 * This method searches for all contents matching
	 */
	
	public List getMatchingContents(String contentTypeDefinitionNamesString, String categoryConditionString, String freeText, List freeTextAttributeNames, Date fromDate, Date toDate, Date expireFromDate, Date expireToDate, String versionModifier, Integer maximumNumberOfItems, boolean useLanguageFallback, boolean cacheResult, int cacheInterval, String cacheName, String cacheKey, List<Integer> repositoryIdList)
	{
		return getMatchingContents(contentTypeDefinitionNamesString, categoryConditionString, freeText, freeTextAttributeNames, fromDate, toDate, expireFromDate, expireToDate, versionModifier, maximumNumberOfItems, useLanguageFallback, cacheResult, cacheInterval, cacheName, cacheKey, repositoryIdList, null, null);
	}

	/**
	 * This method searches for all contents matching
	 */
	
	public List getMatchingContents(String contentTypeDefinitionNamesString, String categoryConditionString, String freeText, List freeTextAttributeNames, Date fromDate, Date toDate, Date expireFromDate, Date expireToDate, String versionModifier, Integer maximumNumberOfItems, boolean useLanguageFallback, boolean cacheResult, int cacheInterval, String cacheName, String cacheKey, List<Integer> repositoryIdList, Integer languageId)
	{
		return getMatchingContents(contentTypeDefinitionNamesString, categoryConditionString, freeText, freeTextAttributeNames, fromDate, toDate, expireFromDate, expireToDate, versionModifier, maximumNumberOfItems, useLanguageFallback, cacheResult, cacheInterval, cacheName, cacheKey, repositoryIdList, languageId, null);
	}

	/**
	 * This method searches for all contents matching
	 */
	
	public List getMatchingContents(String contentTypeDefinitionNamesString, String categoryConditionString, String freeText, List freeTextAttributeNames, Date fromDate, Date toDate, Date expireFromDate, Date expireToDate, String versionModifier, Integer maximumNumberOfItems, boolean useLanguageFallback, boolean cacheResult, int cacheInterval, String cacheName, String cacheKey, List<Integer> repositoryIdList, Integer languageId, Boolean skipLanguageCheck)
	{
		Timer t = new Timer();

		deliveryContext.addUsedContent("selectiveCacheUpdateNonApplicable");
		
		if((freeText != null && !freeText.equals("")) || (freeTextAttributeNames != null && freeTextAttributeNames.size() > 0) || fromDate != null || toDate != null || expireFromDate != null || expireToDate != null || (versionModifier != null && !versionModifier.equals("")) || !deliveryContext.getOperatingMode().equals(CmsPropertyHandler.getOperatingMode()))
			cacheResult = false;

		//TODO - add cache here
		if(cacheName == null || cacheName.equals(""))
			cacheName = "matchingContentsCache";

		//if(cacheKey == null || cacheKey.equals(""))
		//	cacheKey = "matchingContentsCache";

		Integer localLanguageId = this.getLanguageId();
		if(languageId != null)
			localLanguageId = languageId;
		
		StringBuffer repositoryIdString = new StringBuffer();
		if(repositoryIdList != null)
		{
			Iterator repositoryIdListIterator = repositoryIdList.iterator();
			while(repositoryIdListIterator.hasNext())
				repositoryIdString.append("," + repositoryIdListIterator.next());
		}
		
		String key = "sortedMatchingContents" + contentTypeDefinitionNamesString + "_" + categoryConditionString + "_publishDateTime_languageId_" + localLanguageId + "_" + useLanguageFallback + "_" + maximumNumberOfItems + "_" + repositoryIdString + "_" + skipLanguageCheck;
		if(cacheKey != null && !cacheKey.equals(""))
			key = cacheKey;
		
		List cachedMatchingContents = (List)CacheController.getCachedObjectFromAdvancedCache(cacheName, key, cacheInterval);
		if(cachedMatchingContents == null || !cacheResult)
		{
			logger.info("Getting matching contents from db for key:" + key);
			
			try
			{
			    List contentTypeDefinitionVOList = new ArrayList();
			    String[] contentTypeDefinitionNames = contentTypeDefinitionNamesString.split(",");
			    for(int i=0; i<contentTypeDefinitionNames.length; i++)
			    {
			        ContentTypeDefinitionVO contentTypeDefinitionVO = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName(contentTypeDefinitionNames[i], getDatabase());
			        if(contentTypeDefinitionVO != null)
			        	contentTypeDefinitionVOList.add(contentTypeDefinitionVO);
			    }
	
			    final CategoryConditions categoryConditions = CategoryConditions.parse(categoryConditionString);
			    
				final ExtendedSearchCriterias criterias = new ExtendedSearchCriterias(this.getOperatingMode().intValue());
				criterias.setCategoryConditions(categoryConditions);
				criterias.setLanguage(this.getLanguage(localLanguageId));
				if(skipLanguageCheck != null)
					criterias.setSkipLanguageCheck(skipLanguageCheck);
				if(freeText != null && freeTextAttributeNames != null)
					criterias.setFreetext(freeText, freeTextAttributeNames);
				criterias.setContentTypeDefinitions(contentTypeDefinitionVOList);
				criterias.setDates(fromDate, toDate);
				criterias.setExpireDates(expireFromDate, expireToDate);
				criterias.setMaximumNumberOfItems(maximumNumberOfItems);
				if(versionModifier != null)
					criterias.setVersionModifier(versionModifier);
				if(repositoryIdList != null && repositoryIdList.size() > 0)
					criterias.setRepositoryIdList(repositoryIdList);
				
				final Set set = ExtendedSearchController.getController().search(criterias, getDatabase());
				
				final List result = new ArrayList();
				for(Iterator i = set.iterator(); i.hasNext(); ) 
				{
					final Content content = (Content) i.next();
					//if(ContentDeliveryController.getContentDeliveryController().isValidContent(this.getDatabase(), content.getId(), localLanguageId, USE_LANGUAGE_FALLBACK, true, getPrincipal(), this.deliveryContext))
					if(ContentDeliveryController.getContentDeliveryController().isValidContent(this.getDatabase(), content, localLanguageId, USE_LANGUAGE_FALLBACK, true, getPrincipal(), this.deliveryContext, false, false))
					{
						result.add(content.getValueObject());
					}
				}

				if(cacheResult)
					CacheController.cacheObjectInAdvancedCache(cacheName, key, result, null, false);
				
				return result;
			}
			catch(Exception e)
			{
				logger.warn("An error occurred trying to get Matching Contents for contentTypeDefinitionNamesString: " + contentTypeDefinitionNamesString + ":" + e.getMessage(), e);
			}
		}
		else if(cachedMatchingContents != null)
		{
			logger.info("Getting cached contents for key:" + key);
			return cachedMatchingContents;
		}
		
		RequestAnalyser.getRequestAnalyser().registerComponentStatistics("getMatchingContents", t.getElapsedTime());

		return Collections.EMPTY_LIST;
	}

	/**
	 * This method returns which mode the delivery-engine is running in.
	 * The mode is important to be able to show working, preview and published data separate.
	 */
	
	public Integer getOperatingMode()
	{
		Integer operatingMode = new Integer(0); //Default is working
		try
		{
			if(this.getDeliveryContext().getOperatingMode() != null)
			{
				try
				{
					operatingMode = new Integer(this.getDeliveryContext().getOperatingMode());
				}
				catch (Exception e) 
				{
					operatingMode = new Integer(CmsPropertyHandler.getOperatingMode());
				}
			}
			else
				operatingMode = new Integer(CmsPropertyHandler.getOperatingMode());
			//logger.info("Operating mode is:" + operatingMode);
		}
		catch(Exception e)
		{
			logger.warn("We could not get the operating mode from the propertyFile:" + e.getMessage(), e);
		}
		return operatingMode;
	}

	/**
	 * This method returns the configured mailserver.
	 */
	
	public String getSmtpServer()
	{
		return CmsPropertyHandler.getMailSmtpHost();
	}

	/**
	 * This method returns the configured mailserver.
	 */
	
	public String getSmtpPort()
	{
		return CmsPropertyHandler.getMailSmtpPort();
	}

	/**
	 * This method deliveres a String with the URL to the page asked for.
	 * As the siteNode can have multiple bindings the method requires a bindingName 
	 * which refers to the AvailableServiceBinding.name-attribute. This method also allows the user
	 * to specify that the content is important. This method is mostly used for master/detail-pages.
	 */
	 
	public String getPageUrl(String structureBindningName, Integer contentId) 
	{
		String pageUrl = "";
		
		try
		{
			SiteNodeVO siteNodeVO = this.nodeDeliveryController.getBoundSiteNode(getDatabase(), this.siteNodeId, structureBindningName);		
			pageUrl = this.nodeDeliveryController.getPageUrl(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, contentId, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get page url for structureBindningName " + structureBindningName + ":" + e.getMessage(), e);
		}
				
		return pageUrl;
	}


	/**
	 * This method deliveres a String with the URL to the page asked for.
	 * As the siteNode can have multiple bindings the method requires a bindingName and also allows the user to specify a 
	 * special siteNode in an ordered collection. 
	 * which refers to the AvailableServiceBinding.name-attribute. 
	 */
	 
	public String getPageUrlOnPosition(String structureBindningName, int position) 
	{
		String pageUrl = "";
		
		try
		{
			SiteNodeVO siteNodeVO = this.nodeDeliveryController.getBoundSiteNode(getDatabase(), this.siteNodeId, structureBindningName, position);		
			pageUrl = this.nodeDeliveryController.getPageUrl(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get page url for structureBindningName " + structureBindningName + ":" + e.getMessage(), e);
		}
				
		return pageUrl;
	}


	/**
	 * This method deliveres a String with the URL to the page asked for.
	 * As the siteNode can have multiple bindings the method requires a bindingName and also allows the user to specify a 
	 * special siteNode in an ordered collection. 
	 * which refers to the AvailableServiceBinding.name-attribute. This method also allows the user
	 * to specify that the content is important. This method is mostly used for master/detail-pages.
	 */
	 
	public String getPageUrl(String structureBindningName, int position, Integer contentId) 
	{
		String pageUrl = "";
		
		try
		{
			SiteNodeVO siteNodeVO = this.nodeDeliveryController.getBoundSiteNode(getDatabase(), this.siteNodeId, structureBindningName, position);		
			pageUrl = this.nodeDeliveryController.getPageUrl(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, contentId, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get page url for structureBindningName " + structureBindningName + ":" + e.getMessage(), e);
		}
				
		return pageUrl;
	}


	/**
	 * This method deliveres a new url pointing to the same address as now but in the language 
	 * corresponding to the code sent in.
	 */
	 
	public String getCurrentPageUrl() 
	{
		String pageUrl = "";
		
		try
		{
			pageUrl = this.nodeDeliveryController.getPageUrl(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, this.contentId, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get current page url:" + e.getMessage(), e);
		}
				
		return pageUrl;
	}

	/**
	 * This method returns the exact full url excluding query string from the original request - not modified
	 * @return
	 */
	
	public String getOriginalURL()
	{
    	String originalRequestURL = this.getHttpServletRequest().getParameter("originalRequestURL");
    	if(originalRequestURL == null || originalRequestURL.length() == 0)
    		originalRequestURL = this.getHttpServletRequest().getRequestURL().toString();

    	return originalRequestURL;
	}

	/**
	 * This method returns the exact querystring from the original request - not modified
	 * @return
	 */
	
	public String getOriginalQueryString()
	{
    	String originalQueryString = this.getHttpServletRequest().getParameter("originalQueryString");
    	if(originalQueryString == null || originalQueryString.length() == 0)
    		originalQueryString = this.getHttpServletRequest().getQueryString();

    	return originalQueryString;
	}

	/**
	 * This method returns the exact full url from the original request - not modified
	 * @return
	 */
	
	public String getOriginalFullURL()
	{
    	String originalRequestURL = getOriginalURL();
    	String originalQueryString = getOriginalQueryString();

    	return originalRequestURL + (originalQueryString == null ? "" : "?" + originalQueryString);
	}

	/**
	 * This method deliveres a new url pointing to the same address as now but with new parameters.
	 */
	 
	public String getPageUrl(Integer siteNodeId, Integer languageId, Integer contentId) 
	{
		String pageUrl = "";
		
		try
		{
			pageUrl = this.nodeDeliveryController.getPageUrl(getDatabase(), this.getPrincipal(), siteNodeId, languageId, contentId, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get page url for siteNodeId[" + siteNodeId + "]:" + e.getMessage() + "\n" + "The page generating the error was:" + this.getOriginalFullURL());
		}
				
		return pageUrl;
	}

	/**
	 * This method calls an page and stores it as an digitalAsset - that way one can avoid having to 
	 * serve javascript-files and css-files through InfoGlue. Not suitable for use if you have very dynamic
	 * css:es or scripts which includes logic depending on user info etc.. mostly usable if you have a static css
	 * or controls it on the pageCache parameters.
	 */
	 
	public String getPageAsDigitalAssetUrl(Integer siteNodeId, Integer languageId, Integer contentId, String fileSuffix, boolean cacheUrl) 
	{
		String pageDigitalAssetUrl = "";
		
		try
		{
			pageDigitalAssetUrl = this.nodeDeliveryController.getPageAsDigitalAssetUrl(getDatabase(), this.getPrincipal(), siteNodeId, languageId, contentId, this.deliveryContext, fileSuffix, cacheUrl);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get current page digitalAsset url:" + e.getMessage(), e);
		}
				
		return pageDigitalAssetUrl;
	}
	

	/**
	 * This method constructs a string representing the path to the page with respect to where in the
	 * structure the page is. It also takes the page title into consideration.
	 */
	 
	public String getCurrentPagePath() 
	{
		String pagePath = "";
		
		try
		{
			pagePath = this.nodeDeliveryController.getPagePath(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, this.contentId, META_INFO_BINDING_NAME, NAV_TITLE_ATTRIBUTE_NAME, USE_LANGUAGE_FALLBACK, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get current page path:" + e.getMessage());
		}
				
		return pagePath;
	}

	/**
	 * This method constructs a string representing the path to the page with respect to where in the
	 * structure the page is. It also takes the page title into consideration.
	 */
	 
	public String getPagePath(Integer siteNodeId, Integer languageId) 
	{
		String pagePath = "";
		
		try
		{
			pagePath = this.nodeDeliveryController.getPagePath(getDatabase(), this.getPrincipal(), siteNodeId, languageId, this.contentId, META_INFO_BINDING_NAME, NAV_TITLE_ATTRIBUTE_NAME, USE_LANGUAGE_FALLBACK, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get current page path:" + e.getMessage());
		}
				
		return pagePath;
	}

	
	/**
	 * Returns the path to, and including, the supplied content.
	 * 
	 * @param contentId the content to 
	 * 
	 * @return String the path to, and including, this content "library/library/..."
	 * 
	 */
	public String getContentPath(Integer contentId)
    {
		return getContentPath(contentId, false, false);
    }


	/**
	 * Returns the path to, and including, the supplied content.
	 * 
	 * @param contentId the content to 
	 * 
	 * @return String the path to, and including, this content "library/library/..."
	 * 
	 */
	public String getContentPath(Integer contentId, boolean includeRootContent, boolean includeRepositoryName)
	{
		StringBuffer sb = new StringBuffer();

		ContentVO contentVO = getContent(contentId);

		if (contentVO != null)
		{
			sb.insert(0, contentVO.getName());
	
			while (contentVO.getParentContentId() != null)
			{
				contentVO = getContent(contentVO.getParentContentId());
	
				if (includeRootContent || contentVO.getParentContentId() != null)
				{
					sb.insert(0, contentVO.getName() + "/");
				}
			}
	
			if (includeRepositoryName)
			{
				try
				{
					RepositoryVO repositoryVO = RepositoryController.getController().getRepositoryVOWithId(contentVO.getRepositoryId(), getDatabase());
					if(repositoryVO != null)
						sb.insert(0, repositoryVO.getName() + " - /");
				}
				catch (Exception e) 
				{
					logger.error("The repository for content " + contentVO.getName() + ":" + contentVO.getId() + " did not exist. Must be an inconsistency.");
				}
			}
		}
		
		return sb.toString();
	}

	
	/**
	 * This method returns the parent siteNode to the given siteNode.
	 */
	 
	public SiteNodeVO getParentSiteNode(Integer siteNodeId) 
	{
		SiteNodeVO siteNodeVO = null;
		
		try
		{
			siteNodeVO = this.nodeDeliveryController.getParentSiteNode(getDatabase(), siteNodeId);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get parent siteNode:" + e.getMessage(), e);
		}
				
		return siteNodeVO;
	}

	/**
	 * This method deliveres a new url pointing to the same address as now but in the language 
	 * corresponding to the code sent in.
	 */
	 
	public String getPageUrlAfterLanguageChange(String languageCode) 
	{
		String pageUrl = "";
		
		try
		{
			LanguageVO languageVO = LanguageDeliveryController.getLanguageDeliveryController().getLanguageWithCode(getDatabase(), languageCode);		
			//pageUrl = this.nodeDeliveryController.getPageUrl(this.siteNodeId, languageVO.getLanguageId(), this.contentId);
			pageUrl = this.nodeDeliveryController.getPageUrlAfterLanguageChange(getDatabase(), this.getPrincipal(), this.siteNodeId, languageVO.getLanguageId(), this.contentId, this.deliveryContext); 
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the new page-url after language-change:" + e.getMessage(), e);
		}
				
		return pageUrl;
	}


	/**
	 * This method deliveres a String with the Navigation title the page the user are on has.
	 * The navigation-title is fetched from the meta-info-content bound to the site node.
	 */
	 
	public String getPageTitle() 
	{
		String navTitle = "";
		
		try
		{
			navTitle = this.nodeDeliveryController.getPageNavigationTitle(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, null, META_INFO_BINDING_NAME, TITLE_ATTRIBUTE_NAME, USE_LANGUAGE_FALLBACK, this.deliveryContext, false);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the page navigationtitle:" + e.getMessage(), e);
		}
				
		return navTitle;
	}

	/**
	 * This method deliveres a String with the Navigation title the page asked for has.
	 * As the siteNode can have multiple bindings the method requires a bindingName 
	 * which refers to the AvailableServiceBinding.name-attribute. The navigation-title is fetched
	 * from the meta-info-content bound to the site node.
	 */
	 
	public String getPageNavTitle(String structureBindningName) 
	{
	    return getPageNavTitle(structureBindningName, false);
	}
	
	/**
	 * This method deliveres a String with the Navigation title the page asked for has.
	 * As the siteNode can have multiple bindings the method requires a bindingName 
	 * which refers to the AvailableServiceBinding.name-attribute. The navigation-title is fetched
	 * from the meta-info-content bound to the site node.
	 */
	 
	public String getPageNavTitle(String structureBindningName, boolean escapeHTML) 
	{
		String navTitle = "";
		
		try
		{
			SiteNodeVO siteNodeVO = this.nodeDeliveryController.getBoundSiteNode(getDatabase(), this.siteNodeId, structureBindningName);
			logger.info(siteNodeVO.getName());
			navTitle = this.nodeDeliveryController.getPageNavigationTitle(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, META_INFO_BINDING_NAME, NAV_TITLE_ATTRIBUTE_NAME, USE_LANGUAGE_FALLBACK, this.deliveryContext, escapeHTML);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the page navigationtitle on " + this.getCurrentPagePath() + ": " + e.getMessage(), e);
		}
				
		return navTitle;
	}

	/**
	 * This method deliveres a String with the Navigation title the page asked for has.
	 * The navigation-title is fetched from the meta-info-content bound to the site node.
	 */
	 
	public String getPageNavTitle(Integer siteNodeId) 
	{
	    return getPageNavTitle(siteNodeId, false);
	}
	
	/**
	 * This method deliveres a String with the Navigation title the page asked for has.
	 * The navigation-title is fetched from the meta-info-content bound to the site node.
	 */
	 
	public String getPageNavTitle(Integer siteNodeId, boolean escapeHTML) 
	{
		String navTitle = "";
		
		try
		{
			navTitle = this.nodeDeliveryController.getPageNavigationTitle(getDatabase(), this.getPrincipal(), siteNodeId, this.languageId, null, META_INFO_BINDING_NAME, NAV_TITLE_ATTRIBUTE_NAME, USE_LANGUAGE_FALLBACK, this.deliveryContext, escapeHTML);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get the page navigationtitle on " + this.getCurrentPagePath() + ": " + e.getMessage(), e);
		}
				
		return navTitle;
	}

	/**
	 * This method deliveres a String with the Navigation title the page asked for has.
	 * As the siteNode can have multiple bindings the method requires a bindingName and a collection index. 
	 * The navigation-title is fetched from the meta-info-content bound to the site node.
	 */
	 
	public String getPageNavTitle(String structureBindningName, int index) 
	{
	    return getPageNavTitle(structureBindningName, index, false);
	}
	
	/**
	 * This method deliveres a String with the Navigation title the page asked for has.
	 * As the siteNode can have multiple bindings the method requires a bindingName and a collection index. 
	 * The navigation-title is fetched from the meta-info-content bound to the site node.
	 */
	 
	public String getPageNavTitle(String structureBindningName, int index, boolean escapeHTML) 
	{
		String navTitle = "";
		
		try
		{
			SiteNodeVO siteNodeVO = this.nodeDeliveryController.getBoundSiteNode(getDatabase(), this.siteNodeId, structureBindningName, index);
			logger.info(siteNodeVO.getName());
			navTitle = this.nodeDeliveryController.getPageNavigationTitle(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, META_INFO_BINDING_NAME, NAV_TITLE_ATTRIBUTE_NAME, USE_LANGUAGE_FALLBACK, this.deliveryContext, escapeHTML);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the page navigationtitle on " + this.getCurrentPagePath() + ": " + e.getMessage(), e);
		}
				
		return navTitle;
	}


	/**
	 * This method returns true if the if the page in question (ie sitenode) has page-caching disabled.
	 * This is essential to turn off when you have a dynamic page like an external application or searchresult.
	 */
	
	public boolean getIsPageCacheDisabled()
	{
		boolean isPageCacheDisabled = false;
		
		try
		{
			isPageCacheDisabled = this.nodeDeliveryController.getIsPageCacheDisabled(getDatabase(), this.siteNodeId);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled pageCache:" + e.getMessage(), e);
		}
				
		return isPageCacheDisabled;
	}
	
	/**
	 * This method returns the contenttype this page should return. This is important when sending assets or css:contents.
	 */
	
	public String getPageContentType()
	{
		String pageContentType = "text/html";
		
		try
		{
			SiteNodeVersionVO latestSiteNodeVersionVO = this.nodeDeliveryController.getLatestActiveSiteNodeVersionVO(getDatabase(), this.siteNodeId);
			if(latestSiteNodeVersionVO != null && latestSiteNodeVersionVO.getContentType() != null && latestSiteNodeVersionVO.getContentType().length() > 0)
				pageContentType = latestSiteNodeVersionVO.getContentType();
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get the content type of the page:" + e.getMessage(), e);
		}
				
		return pageContentType;
	}

	/**
	 * This method returns the pageCacheTimeout this page has.
	 */
	
	public Integer getPageCacheTimeout()
	{
		Integer pageCacheTimeout = null;
		
		try
		{
			pageCacheTimeout = this.nodeDeliveryController.getInheritedPageCacheTimeout(getDatabase(), this.siteNodeId);
			/*
			SiteNodeVersionVO latestSiteNodeVersionVO = this.nodeDeliveryController.getLatestActiveSiteNodeVersionVO(getDatabase(), this.siteNodeId);
			if(latestSiteNodeVersionVO != null && latestSiteNodeVersionVO.getContentType() != null && latestSiteNodeVersionVO.getContentType().length() > 0)
			{
				String pageCacheTimeoutString = latestSiteNodeVersionVO.getPageCacheTimeout();
				if(pageCacheTimeoutString != null && !pageCacheTimeoutString.equals(""))
					pageCacheTimeout = new Integer(pageCacheTimeoutString);
			}
			*/
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get the pageCacheTimeout of the page " + siteNodeId + ":" + e.getMessage(), e);
		}
		
		return pageCacheTimeout;
	}

	/**
	 * This method returns true if the page in question (ie sitenode) has it's protected property enabled.
	 * This is essential when checking if we should authenticate users before allowing them access.
	 */
	
	public boolean getIsPageProtected()
	{
		boolean isPageProtected = false;
		
		try
		{
			isPageProtected = this.nodeDeliveryController.getIsPageProtected(getDatabase(), this.siteNodeId);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has protect page:" + e.getMessage(), e);
		}
				
		return isPageProtected;
	}
	
	public boolean getIsInPageComponentMode()
	{
		boolean isInPageComponentMode = false;
		
		try
		{
			if(this instanceof EditOnSiteBasicTemplateController)
				isInPageComponentMode = true;
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the user was is editing mode:" + e.getMessage(), e);
		}
				
		return isInPageComponentMode;
	}

	/**
	 * This method returns true if the page in question (ie sitenode) has page-caching disabled.
	 * This is essential to turn off when you have a dynamic page like an external application or searchresult.
	 */
	
	public boolean getIsEditOnSightDisabled()
	{
		boolean isEditOnSightDisabled = false;
		
		try
		{
			isEditOnSightDisabled = this.nodeDeliveryController.getIsEditOnSightDisabled(getDatabase(), this.siteNodeId);
		}
		catch(Exception e)
		{
			logger.warn("An error occurred trying to get if the siteNodeVersion has disabled editOnSight:" + e.getMessage(), e);
		}
				
		return isEditOnSightDisabled;
	}


	/**
	 * This method returns a list of all languages available on the current site/repository.
	 */
	
	public List getAvailableLanguages()
	{
		List availableLanguages = new ArrayList();
		
		try
		{
			availableLanguages = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguages(getDatabase(), this.siteNodeId);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get all available languages:" + e.getMessage(), e);
		}
				
		return availableLanguages;
	}
	
	
	/**
	 * This method returns a list of all languages available on the current sitenode. The logic is that 
	 * we check which languages are found in the meta-content in the current mode.
	 * @deprecated - Use the new getPageLanguages instead
	 */
	
	public List getNodeAvailableLanguages()
	{
		return getNodeAvailableLanguages(this.siteNodeId);
	}

	/**
	 * This method returns a list of all languages available on the current sitenode. The logic is that 
	 * we check which languages are found in the meta-content in the current mode.
	 * @deprecated - Use the new getPageLanguages instead
	 */
	
	public List getNodeAvailableLanguages(Integer siteNodeId)
	{
		List availableLanguages = new ArrayList();
		
		try
		{
			availableLanguages = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguages(getDatabase(), siteNodeId);
			Iterator languageIterator = availableLanguages.iterator();
			while(languageIterator.hasNext())
			{
				LanguageVO languageVO = (LanguageVO)languageIterator.next();
				ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, META_INFO_BINDING_NAME, this.deliveryContext);		
				ContentVersionVO contentVersionVO = null;
				if(contentVO != null)
				{
					contentVersionVO = ContentDeliveryController.getContentDeliveryController().getContentVersionVO(getDatabase(), siteNodeId, contentVO.getId(), languageVO.getId(), false, this.deliveryContext, this.infoGluePrincipal);
				}
				
				if(contentVO == null || contentVersionVO == null)		
				{	
					logger.warn("The meta-info did not have a version of " + languageVO.getName());
					languageIterator.remove();
				}
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get all available languages:" + e.getMessage(), e);
		}
				
		return availableLanguages;
	}
	
	/**
	 * This method returns a list of all languages available on the current sitenode. This method will return all languages enabled for this repository minus 
	 * any disabled languages for the siteNode.
	 */
	
	public List getPageLanguages()
	{
		return getPageLanguages(this.siteNodeId);
	}

	/**
	 * This method returns a list of all languages available on the current sitenode. This method will return all languages enabled for this repository minus 
	 * any disabled languages for the siteNode.
	 */

	public List getPageLanguages(Integer siteNodeId)
	{
		List availableLanguages = new ArrayList();
		
		try
		{
			availableLanguages = LanguageDeliveryController.getLanguageDeliveryController().getLanguagesForSiteNode(getDatabase(), siteNodeId, this.getPrincipal());
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get all available languages:" + e.getMessage(), e);
		}
				
		return availableLanguages;
	}

	
	/**
     * The method returns a WebPage-object for the given page etc.
     */
    public WebPage getPage(Integer siteNodeId, Integer languageId, Integer contentId, boolean escapeHTML, boolean hideUnauthorizedPages)
    {
    	if(siteNodeId == null || siteNodeId.intValue() < 1)
    		return null;
    
    	WebPage page = null;
    	
    	try
    	{
	    	SiteNodeVO siteNodeVO = getSiteNode(siteNodeId);
	    	page = getPage(siteNodeVO, escapeHTML, hideUnauthorizedPages);
    	}
    	catch (Exception e) 
    	{
    		logger.warn("There was a problem getting the page for siteNodeId[" + siteNodeId + "]. Message: " + e.getMessage());
		}
    	
    	return page;
    }

	/**
	 * The method returns a list of WebPage-objects that is the children of the current 
	 * siteNode. The method is great for navigation-purposes on a structured site. 
	 */
	
	public List getChildPages()
	{
	    return getChildPages(false);
	}

	/**
	 * The method returns a list of WebPage-objects that is the children of the current 
	 * siteNode. The method is great for navigation-purposes on a structured site. 
	 */
	
	public List getChildPages(boolean escapeHTML)
	{
		return getChildPages(false, false);
	}
	
	/**
	 * The method returns a list of WebPage-objects that is the children of the current 
	 * siteNode. The method is great for navigation-purposes on a structured site. 
	 */
	
	public List getChildPages(boolean escapeHTML, boolean hideUnauthorizedPages)
	{
		List childPages = new ArrayList();
		try
		{
			List childNodeVOList = this.nodeDeliveryController.getChildSiteNodes(getDatabase(), this.siteNodeId);
			childPages = getPages(childNodeVOList, escapeHTML, hideUnauthorizedPages);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the page childPages:" + e.getMessage(), e);
		}
		
		return childPages;
	}
																																

	/**
	 * The method returns a list of WebPage-objects that is the children of the given 
	 * siteNode. The method is great for navigation-purposes on a structured site. 
	 */
	
	public List getChildPages(String structureBindingName)
	{
	    return getChildPages(structureBindingName, false);
	}

	/**
	 * The method returns a list of WebPage-objects that is the children of the given 
	 * siteNode. The method is great for navigation-purposes on a structured site. 
	 */
	
	public List getChildPages(String structureBindingName, boolean escapeHTML)
	{
	    return getChildPages(structureBindingName, escapeHTML, false);
	}

	/**
	 * The method returns a list of WebPage-objects that is the children of the given 
	 * siteNode. The method is great for navigation-purposes on a structured site. 
	 */
	
	public List getChildPages(String structureBindingName, boolean escapeHTML, boolean hideUnauthorizedPages)
	{
		List childPages = new ArrayList();
		try
		{
			List childNodeVOList = this.nodeDeliveryController.getChildSiteNodes(getDatabase(), this.getSiteNodeId(structureBindingName));
			childPages = getPages(childNodeVOList, escapeHTML, hideUnauthorizedPages);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the page childPages:" + e.getMessage(), e);
		}
		
		return childPages;
	}

	/**
	 * This method takes a sitenode and converts it to a webpage instead.
	 * 
	 * @param siteNodeVO
	 * @param escapeHTML
	 * @param hideUnauthorizedPages
	 * @return
	 * @throws Exception
	 */
	private WebPage getPage(SiteNodeVO siteNodeVO, boolean escapeHTML, boolean hideUnauthorizedPages) throws Exception
	{
		return getPage(siteNodeVO, escapeHTML, hideUnauthorizedPages, false);
	}
	
	/**
	 * This method takes a sitenode and converts it to a webpage instead.
	 * 
	 * @param siteNodeVO
	 * @param escapeHTML
	 * @param hideUnauthorizedPages
	 * @return
	 * @throws Exception
	 */
	private WebPage getPage(SiteNodeVO siteNodeVO, boolean escapeHTML, boolean hideUnauthorizedPages, boolean showHidden) throws Exception
	{
		WebPage page = null;

		if((!hideUnauthorizedPages || getHasUserPageAccess(siteNodeVO.getId())) && (showHidden || !siteNodeVO.getIsHidden()))
		{
			try
			{
				page = new WebPage();						
				page.setSiteNodeId(siteNodeVO.getSiteNodeId());
				page.setLanguageId(this.languageId);
				page.setContentId(null);
				page.setNavigationTitle(this.nodeDeliveryController.getPageNavigationTitle(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, META_INFO_BINDING_NAME, NAV_TITLE_ATTRIBUTE_NAME, USE_LANGUAGE_FALLBACK, this.deliveryContext, escapeHTML));
				page.setMetaInfoContentId(this.nodeDeliveryController.getMetaInfoContentId(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), META_INFO_BINDING_NAME, USE_INHERITANCE, this.deliveryContext));
				page.setUrl(this.nodeDeliveryController.getPageUrl(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, this.deliveryContext));
			}
			catch(Exception e)
			{
			    logger.info("An error occurred when looking up the page:" + e.getMessage(), e);
			}
		}
		
		return page;
	}

	/**
	 * This method takes a list of sitenodes and converts it to a page list instead.
	 * 
	 * @param childNodeVOList
	 * @param escapeHTML
	 * @param hideUnauthorizedPages
	 * @return
	 * @throws Exception
	 */
	private List getPages(List childNodeVOList, boolean escapeHTML, boolean hideUnauthorizedPages) throws Exception
	{
		return getPages(childNodeVOList, escapeHTML, hideUnauthorizedPages, true);
	}
	
	/**
	 * This method takes a list of sitenodes and converts it to a page list instead.
	 * 
	 * @param childNodeVOList
	 * @param escapeHTML
	 * @param hideUnauthorizedPages
	 * @return
	 * @throws Exception
	 */
	private List getPages(List childNodeVOList, boolean escapeHTML, boolean hideUnauthorizedPages, boolean showHidden) throws Exception
	{
		List childPages = new ArrayList();

		Iterator i = childNodeVOList.iterator();
		while(i.hasNext())
		{
			SiteNodeVO siteNodeVO = (SiteNodeVO)i.next();
			
			this.getDeliveryContext().addUsedSiteNode("siteNode_" + siteNodeVO.getId());
			
			if((!hideUnauthorizedPages || getHasUserPageAccess(siteNodeVO.getId())) && (showHidden || !siteNodeVO.getIsHidden()))
			{
				try
				{
					WebPage webPage = new WebPage();
					webPage.setSiteNodeId(siteNodeVO.getSiteNodeId());
					webPage.setLanguageId(this.languageId);
					webPage.setContentId(null);
					webPage.setNavigationTitle(this.nodeDeliveryController.getPageNavigationTitle(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, META_INFO_BINDING_NAME, NAV_TITLE_ATTRIBUTE_NAME, USE_LANGUAGE_FALLBACK, this.deliveryContext, escapeHTML));
					webPage.setMetaInfoContentId(this.nodeDeliveryController.getMetaInfoContentId(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), META_INFO_BINDING_NAME, USE_INHERITANCE, this.deliveryContext));
					
					SiteNodeVersionVO siteNodeVersionVO = this.nodeDeliveryController.getLatestActiveSiteNodeVersionVO(getDatabase(), siteNodeVO.getSiteNodeId());
					webPage.setSortOrder(siteNodeVersionVO.getSortOrder());
					webPage.setIsHidden(siteNodeVersionVO.getIsHidden());
					
					webPage.setUrl(this.nodeDeliveryController.getPageUrl(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, this.deliveryContext));
					childPages.add(webPage);
				}
				catch(Exception e)
				{
				    logger.info("An error occurred when looking up one of the childPages:" + e.getMessage(), e);
				}
			}
		}
		
		return childPages;
	}
	
	/**
	 * The method returns a list of WebPage-objects that is the children of the given 
	 * siteNode. The method is great for navigation-purposes on a structured site. 
	 */
	
	public List getChildPages(Integer siteNodeId)
	{
	    return getChildPages(siteNodeId, false, false);
	}

	/**
	 * The method returns a list of WebPage-objects that is the children of the given 
	 * siteNode. The method is great for navigation-purposes on a structured site. 
	 */
	
	public List getChildPages(Integer siteNodeId, boolean escapeHTML)
	{
	    return getChildPages(siteNodeId, escapeHTML, false);
	}

	/**
	 * The method returns a list of WebPage-objects that is the children of the given 
	 * siteNode. The method is great for navigation-purposes on a structured site. 
	 */
	
	public List getChildPages(Integer siteNodeId, boolean escapeHTML, boolean hideUnauthorizedPages)
	{
		return getChildPages(siteNodeId, escapeHTML, hideUnauthorizedPages, true);
	}

	/**
	 * The method returns a list of WebPage-objects that is the children of the given 
	 * siteNode. The method is great for navigation-purposes on a structured site. 
	 */
	
	public List getChildPages(Integer siteNodeId, boolean escapeHTML, boolean hideUnauthorizedPages, boolean showHidden)
	{
		List childPages = new ArrayList();
		try
		{
			List childNodeVOList = this.nodeDeliveryController.getChildSiteNodes(getDatabase(), siteNodeId);
			childPages = getPages(childNodeVOList, escapeHTML, hideUnauthorizedPages, showHidden);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the page childPages:" + e.getMessage(), e);
		}
		
		return childPages;
	}

	/**
	 * The method returns a list of WebPage-objects that is the children of the given 
	 * siteNode. The method is great for navigation-purposes on a structured site. 
	 */
	
	public List getChildPages(Integer siteNodeId, String sortAttribute, String sortOrder)
	{
	    return getChildPages(siteNodeId, sortAttribute, sortOrder, false, false);
	}

	/**
	 * The method returns a list of WebPage-objects that is the children of the given 
	 * siteNode. The method is great for navigation-purposes on a structured site. 
	 */
	
	public List getChildPages(Integer siteNodeId, String sortAttribute, String sortOrder, boolean hideUnauthorizedPages)
	{
	    return getChildPages(siteNodeId, sortAttribute, sortOrder, false, hideUnauthorizedPages);
	}

	/**
	 * The method returns a list of WebPage-objects that is the children of the given 
	 * siteNode. The method is great for navigation-purposes on a structured site. 
	 */
	
	public List getChildPages(Integer siteNodeId, String sortAttribute, String sortOrder, boolean escapeHTML, boolean hideUnauthorizedPages)
	{
		List childPages = new ArrayList();
		try
		{
			List childNodeVOList = this.nodeDeliveryController.getChildSiteNodes(getDatabase(), siteNodeId);
			Collections.sort(childNodeVOList, new SiteNodeComparator(sortAttribute, sortOrder, this));
			childPages = getPages(childNodeVOList, escapeHTML, hideUnauthorizedPages);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the page childPages:" + e.getMessage(), e);
		}
		
		return childPages;
	}
	

	/**
	 * The method returns a list of WebPage-objects that is the bound sitenodes of named binding. 
	 * The method is great for navigation-purposes on any site. 
	 */
	
	public List getBoundPages(String structureBindningName)
	{
		return getBoundPages(structureBindningName, false, false);
	}

	/**
	 * The method returns a list of WebPage-objects that is the bound sitenodes of named binding. 
	 * The method is great for navigation-purposes on any site. 
	 */

	public List getBoundPages(String structureBindningName, boolean escapeHTML)
	{
		return getBoundPages(structureBindningName, escapeHTML, false);
	}

	/**
	 * The method returns a list of WebPage-objects that is the bound sitenodes of named binding. 
	 * The method is great for navigation-purposes on any site. 
	 */

	public List getBoundPages(String structureBindningName, boolean escapeHTML, boolean hideUnauthorizedPages)
	{
		return getBoundPages(this.siteNodeId, structureBindningName, escapeHTML, false);
	}
	

	public List getBoundPages(Integer siteNodeId, String structureBindningName)
	{
		return getBoundPages(siteNodeId, structureBindningName, false, false);
	}

	public List getBoundPages(Integer siteNodeId, String structureBindningName, boolean escapeHTML)
	{
		return getBoundPages(siteNodeId, structureBindningName, escapeHTML, false);
	}

	private HashMap cachedBindings = new HashMap();

	/**
	 * This methods get a list of bound pages with the structureBindningName sent in which resides on the siteNodeId sent in.
	 */
	
	public List getBoundPages(Integer siteNodeId, String structureBindningName, boolean escapeHTML, boolean hideUnauthorizedPages)
	{
		//Checking for a read binding in this request...
		if(cachedBindings.containsKey(siteNodeId + "_" + structureBindningName + "_" + hideUnauthorizedPages))
			return (List)cachedBindings.get(siteNodeId + "_" + structureBindningName + "_" + hideUnauthorizedPages);

		List boundPages = new ArrayList();
		try
		{
			List siteNodeVOList = this.nodeDeliveryController.getBoundSiteNodes(getDatabase(), siteNodeId, structureBindningName);	
			boundPages = getPages(siteNodeVOList, escapeHTML, hideUnauthorizedPages);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the bound pages:" + e.getMessage(), e);
		}
		
		//Caching bindings
		cachedBindings.put(siteNodeId + "_" + structureBindningName + "_" + hideUnauthorizedPages, boundPages);
		
		return boundPages;
	}


	
	/**
	 * The method returns a list of WebPage-objects that is the bound sitenodes of named binding. 
	 * The method is great for navigation-purposes on any site. 
	 * We also filter out all pages that don't have a localized version of the page meta-content.
	 */

	public List getLocalizedBoundPages(String structureBindningName)
	{
		//Checking for a read binding in this request...
		if(cachedBindings.containsKey(structureBindningName))
			return (List)cachedBindings.get(structureBindningName);
			
		List boundPages = new ArrayList();
		try
		{
			List siteNodeVOList = this.nodeDeliveryController.getBoundSiteNodes(getDatabase(), this.siteNodeId, structureBindningName);	
			
			Iterator i = siteNodeVOList.iterator();
			while(i.hasNext())
			{
				SiteNodeVO siteNodeVO = (SiteNodeVO)i.next();

				try
				{	
					Integer metaInfoContentId = this.nodeDeliveryController.getMetaInfoContentId(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), META_INFO_BINDING_NAME, DO_NOT_USE_INHERITANCE, this.deliveryContext);
					String navigationTitle = this.nodeDeliveryController.getPageNavigationTitle(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, META_INFO_BINDING_NAME, NAV_TITLE_ATTRIBUTE_NAME, DO_NOT_USE_LANGUAGE_FALLBACK, this.deliveryContext, false);
					if(metaInfoContentId != null && navigationTitle != null && !navigationTitle.equals(""))
					{
						WebPage webPage = new WebPage();						
					    webPage.setSiteNodeId(siteNodeVO.getSiteNodeId());
						webPage.setLanguageId(this.languageId);
						webPage.setContentId(null);
						webPage.setNavigationTitle(navigationTitle);
						webPage.setMetaInfoContentId(metaInfoContentId);
						webPage.setUrl(this.nodeDeliveryController.getPageUrl(getDatabase(), this.getPrincipal(), siteNodeVO.getSiteNodeId(), this.languageId, null, this.deliveryContext));
						boundPages.add(webPage); 
					}
				}
				catch(Exception e)
				{
				    logger.info("An error occurred when looking up one of the getLocalizedBoundPages:" + e.getMessage(), e);
				}
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the bound pages:" + e.getMessage(), e);
		}
		
		//Caching bindings
		cachedBindings.put(structureBindningName, boundPages);
		
		return boundPages;
	}
																																


	/**
	 * The method returns a single ContentVO-objects that is the bound content of named binding. 
	 * It's used for getting one content. 
	 */
	
	public ContentVO getBoundContent(String structureBindningName)
	{
		ContentVO content = null;
		
		List contents = getBoundContents(structureBindningName);
		
		if(contents != null && contents.size() > 0)
			content = (ContentVO)contents.get(0);
		
		return content;
	}


	/**
	 * The method returns a list of ContentVO-objects that is the bound content of named binding. 
	 * The method is great for collection-pages on any site. 
	 */
	
	public List getBoundContents(String structureBindningName)
	{
		//Checking for a read binding in this request...
		if(cachedBindings.containsKey(structureBindningName))
			return (List)cachedBindings.get(structureBindningName);
			
		List boundContents = new ArrayList();
		try
		{
			boundContents = this.nodeDeliveryController.getBoundContents(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, structureBindningName, USE_INHERITANCE, true, this.deliveryContext);	
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the bound contents:" + e.getMessage(), e);
		}
		
		//Caching bindings
		cachedBindings.put(structureBindningName, boundContents);
		
		return boundContents;
	}


	/**
	 * The method returns a list of ContentVO-objects that is children to the bound content of named binding. 
	 * The method is great for collection-pages on any site where you want to bind to a folder containing all contents to list.
	 * You can also state if the method should recurse into subfolders and how the contents should be sorted.
	 * The recursion only deals with three levels at the moment for performance-reasons. 
	 */
	
	public List getBoundFolderContents(String structureBindningName, boolean searchRecursive, String sortAttribute, String sortOrder)
	{
		List boundContents = new ArrayList();
		try
		{
			boundContents = this.nodeDeliveryController.getBoundFolderContents(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, structureBindningName, searchRecursive, new Integer(3), sortAttribute, sortOrder, USE_LANGUAGE_FALLBACK, false, this.deliveryContext);	
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the bound contents:" + e.getMessage(), e);
		}
		
		return boundContents;
	}


	/**
	 * The method returns a list of ContentVO-objects that is children to the bound content of named binding on the siteNode sent in. 
	 * The method is great for collection-pages on any site where you want to bind to a folder containing all contents to list.
	 * You can also state if the method should recurse into subfolders and how the contents should be sorted.
	 * The recursion only deals with three levels at the moment for performance-reasons. 
	 */
	
	public List getBoundFolderContents(Integer siteNodeId, String structureBindningName, boolean searchRecursive, String sortAttribute, String sortOrder)
	{
	    List boundContents = new ArrayList();
		try
		{
			boundContents = this.nodeDeliveryController.getBoundFolderContents(getDatabase(), this.getPrincipal(), siteNodeId, this.languageId, structureBindningName, searchRecursive, new Integer(3), sortAttribute, sortOrder, USE_LANGUAGE_FALLBACK, false, this.deliveryContext);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the bound contents:" + e.getMessage(), e);
		}
		
		return boundContents;
	}

	/**
	 * The method returns a list of ContentVO-objects that is children to the bound content sent in. 
	 * The method is great for collection-pages on any site where you want to bind to a folder containing all contents to list.
	 * You can also state if the method should recurse into subfolders and how the contents should be sorted.
	 * The recursion only deals with three levels at the moment for performance-reasons. 
	 */
	
	public List getChildContents(Integer contentId, boolean searchRecursive, String sortAttribute, String sortOrder)
	{
	    return getChildContents(contentId, searchRecursive, sortAttribute, sortOrder, false);
	}


	/**
	 * The method returns a list of ContentVO-objects that is children to the bound content sent in. 
	 * The method is great for collection-pages on any site where you want to bind to a folder containing all contents to list.
	 * You can also state if the method should recurse into subfolders and how the contents should be sorted.
	 * The recursion only deals with three levels at the moment for performance-reasons. 
	 */
	
	public List getChildContents(Integer contentId, boolean searchRecursive, String sortAttribute, String sortOrder, boolean includeFolders)
	{
		List childContents = new ArrayList();
		try
		{
			childContents = this.nodeDeliveryController.getBoundFolderContents(getDatabase(), this.getPrincipal(), contentId, this.languageId, searchRecursive, new Integer(3), sortAttribute, sortOrder, USE_LANGUAGE_FALLBACK, includeFolders, this.deliveryContext);	
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the bound contents:" + e.getMessage(), e);
		}
		
		return childContents;
	}

	
	/**
	 * The method returns the ContentTypeVO-objects of the given contentId. 
	 */
	
	public ContentTypeDefinitionVO getContentTypeDefinitionVO(Integer contentId)
	{
		ContentTypeDefinitionVO contentTypeDefinition = null;
		
		try
		{
			contentTypeDefinition = ContentDeliveryController.getContentDeliveryController().getContentTypeDefinitionVO(this.getDatabase(), contentId);	
			
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the bound contents:" + e.getMessage(), e);
		}
		
		return contentTypeDefinition;
	}
	
	/**
	 * The method returns the ContentTypeVO-object with the given name. 
	 */
	
	public ContentTypeDefinitionVO getContentTypeDefinitionVO(String name)
	{
		ContentTypeDefinitionVO contentTypeDefinition = null;
		
		try
		{
			contentTypeDefinition = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithName(name, getDatabase());
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to get the bound contents:" + e.getMessage(), e);
		}
		
		return contentTypeDefinition;
	}

	/**
	 * The method returns a list of WebPage-objects that is the bound sitenodes of named binding. 
	 * The method is great for navigation-purposes on any site. Improve later so the list is cached
	 * once for every instance. Otherwise we fetch the whole list again and its not necessairy as
	 * this controller only concerns one request.
	 */
	
	public WebPage getBoundPage(String structureBindningName, int position)
	{
		List boundPages = getBoundPages(structureBindningName);
		
		if(boundPages.size() > position)
			return (WebPage)boundPages.get(position);
		else	
			return null;
	}


	/**
	 * This method allows a user to get any string rendered as a template.
	 */

	public String renderString(String template) 
	{
		String result = "";
		
		try
		{
			Map context = new HashMap();
			context.put("inheritedTemplateLogic", this);
			context.put("templateLogic", getTemplateController(this.siteNodeId, this.languageId, this.contentId, this.request, this.infoGluePrincipal, this.deliveryContext));

			// Add the templateLogicContext objects to this context. (SS - 040219)
			context.putAll(templateLogicContext);
			
			StringWriter cacheString = new StringWriter();
			PrintWriter cachedStream = new PrintWriter(cacheString);
			new VelocityTemplateProcessor().renderTemplate(context, cachedStream, template);
			result = cacheString.toString();
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to do an include:" + e.getMessage(), e);
		}
			
		return result;
	}	

	/**
	 * This method allows a user to get any string rendered as a template.
	 */

	public String renderString(String template, boolean useSubContext) 
	{
		String result = "";
		
		try
		{
			Map context = new HashMap();
			if(!useSubContext)
			{
			    context.put("templateLogic", this);
			}
			else
			{
			    context.put("inheritedTemplateLogic", this);
				context.put("templateLogic", getTemplateController(this.siteNodeId, this.languageId, this.contentId, this.request, this.infoGluePrincipal, this.deliveryContext));
			}
			
			// Add the templateLogicContext objects to this context. (SS - 040219)
			context.putAll(templateLogicContext);
			
			StringWriter cacheString = new StringWriter();
			PrintWriter cachedStream = new PrintWriter(cacheString);
			new VelocityTemplateProcessor().renderTemplate(context, cachedStream, template);
			result = cacheString.toString();
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to do an include:" + e.getMessage(), e);
		}
			
		return result;
	}	

	/**
	 * This method allows a user to get any string rendered as a template.
	 */

	public String renderString(String template, Integer contentId, boolean useSubContext, InfoGlueComponent component) 
	{
		String result = "";
		
		Integer includedComponentContentId = null;
		
		try
		{
			Map context = new HashMap();
			if(!useSubContext)
			{
			    context.put("templateLogic", this);
			    context.put("includedComponentContentId", contentId);
			}
			else
			{
				TemplateController tc = getTemplateController(this.siteNodeId, this.languageId, this.contentId, this.request, this.infoGluePrincipal, this.deliveryContext);
				tc.setComponentLogic(this.getComponentLogic());
				tc.getDeliveryContext().getUsageListeners().add(this.getComponentLogic().getComponentDeliveryContext());
				includedComponentContentId = this.getComponentLogic().getIncludedComponentContentId();
				tc.getComponentLogic().setIncludedComponentContentId(contentId);
				context.put("inheritedTemplateLogic", this);
				context.put("templateLogic", tc);
			    context.put("includedComponentContentId", contentId);
			}
			
			// Add the templateLogicContext objects to this context. (SS - 040219)
			context.putAll(templateLogicContext);
			
			StringWriter cacheString = new StringWriter();
			PrintWriter cachedStream = new PrintWriter(cacheString);
			new VelocityTemplateProcessor().renderTemplate(context, cachedStream, template);
			result = cacheString.toString();
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to do an include:" + e.getMessage());
			logger.error("Problem URL:" + getOriginalFullURL());
		}
		finally
		{
			this.getComponentLogic().setIncludedComponentContentId(includedComponentContentId);
		}
		return result;
	}	

	/**
	 * This method allows the current template to include another template which is also rendered 
	 * in the current context as if it were a part. The method assumes that the result can be cached.
	 * Use the other include method if you wish to be able to control if the result is cached or not.
	 */
	
	
	public String include(String contentBindningName, String attributeName) 
	{
		return include(contentBindningName, attributeName, true);
	}	

	public String include(String contentBindningName, String attributeName, boolean cacheInclude) 
	{
		return include(contentBindningName, attributeName, cacheInclude, null, null);
	}	
	
	/**
	 * This method allows the current template to include another template which is also rendered 
	 * in the current context as if it were a part.
	 * Use this method if you wish to be able to control if the result is cached or not.
	 */	
	public String include(String contentBindningName, String attributeName, boolean cacheInclude, String cName, Object cObject) 
	{
		String includeKey = "" + this.siteNodeId + "_" + this.languageId + "_" + this.contentId + "_" + browserBean.getUseragent() + "_" + contentBindningName + "_" + attributeName;
		logger.info("includeKey:" + includeKey);
		String result = (String)CacheController.getCachedObject("includeCache", includeKey);
		if(result != null)
		{
			logger.info("There was an cached include:" + result);
		}
		else
		{
			try
			{
				ContentVO contentVO = this.nodeDeliveryController.getBoundContent(getDatabase(), this.getPrincipal(), this.siteNodeId, this.languageId, USE_LANGUAGE_FALLBACK, contentBindningName, this.deliveryContext);		
				if(contentVO != null)
				{
					String includedTemplate = ContentDeliveryController.getContentDeliveryController().getContentAttribute(getDatabase(), contentVO.getContentId(), this.languageId, "Template", this.siteNodeId, USE_LANGUAGE_FALLBACK, this.deliveryContext, this.infoGluePrincipal, false);
					logger.info("Found included template:" + includedTemplate);
					
					Map context = new HashMap();
					context.put("inheritedTemplateLogic", this);
					context.put("templateLogic", getTemplateController(this.siteNodeId, this.languageId, this.contentId, this.request, this.infoGluePrincipal, this.deliveryContext));
					context.put("deliveryContext", this.deliveryContext);
					
					// Add the templateLogicContext objects to this context. (SS - 040219)
					context.putAll(templateLogicContext);

					if (cName != null)
						context.put(cName, cObject);						
		
					StringWriter cacheString = new StringWriter();
					PrintWriter cachedStream = new PrintWriter(cacheString);
					new VelocityTemplateProcessor().renderTemplate(context, cachedStream, includedTemplate);
					result = cacheString.toString();
					
					logger.info("result:" + result);
	
					if(cacheInclude)
						CacheController.cacheObject("includeCache", includeKey, result);
				}
			}
			catch(Exception e)
			{
				logger.error("An error occurred trying to do an include:" + e.getMessage(), e);
			}
		}
		
		return result;
	}	
	
	
	/**
	 * This method fetches a given URL contents. This means that we can include a external url's contents
	 * in our application.
	 */
	
	public String getUrlContent(String url)
	{
		String contents = "";
		
		try
		{
			logger.info("We are going to do an include on an external webpage: " + url);
			contents = this.integrationDeliveryController.getUrlContent(url, request, true);
			//logger.info("The respons was: " + contents);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to do an include the url:" + url, e);
		}
		
		return contents;
	}
	
	/**
	 * This method fetches a given URL contents. This means that we can include a external url's contents
	 * in our application. This second method is used to not send extra params through.
	 */
	
	public String getUrlContent(String url, boolean includeRequest)
	{
		String contents = "";
		
		try
		{
			logger.info("We are going to do an include on an external webpage: " + url);
			contents = this.integrationDeliveryController.getUrlContent(url, request, includeRequest);
			//logger.info("The respons was: " + contents);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to do an include the url:" + url, e);
		}
		
		return contents;
	}
	
	/**
	 * This method fetches a given URL contents. This means that we can include a external url's contents
	 * in our application.
	 */
	
	public String getUrlContent(String url, String encoding)
	{
		String contents = "";
		
		try
		{
			logger.info("We are going to do an include on an external webpage: " + url);
			contents = this.integrationDeliveryController.getUrlContent(url, request, true, encoding);
			//logger.info("The respons was: " + contents);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to do an include the url:" + url, e);
		}
		
		return contents;
	}
	
	/**
	 * This method fetches a given URL contents. This means that we can include a external url's contents
	 * in our application. This second method is used to not send extra params through.
	 */
	
	public String getUrlContent(String url, boolean includeRequest, String encoding)
	{
		String contents = "";
		
		try
		{
			logger.info("We are going to do an include on an external webpage: " + url);
			contents = this.integrationDeliveryController.getUrlContent(url, request, includeRequest, encoding);
			//logger.info("The respons was: " + contents);
		}
		catch(Exception e)
		{
			logger.error("An error occurred trying to do an include the url:" + url, e);
		}
		
		return contents;
	}
	

	public Object getObjectWithName(String classname)
	{
		try 
		{
			return this.integrationDeliveryController.getObjectWithName(classname, request);
		} 
		catch (InstantiationException e) 
		{
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) 
		{
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * This method lets a user substitute a string located in the page by a regular expression with another
	 * string. Very useful in certain situations.
	 */
	
	public String replace(String originalString, String expressionToReplace, String newString)
	{
		return originalString.replaceAll(expressionToReplace, newString);		
	}
	
	/**
	 * This method lets a user substitute a string located in the page by a regular expression with another
	 * string. This method also lets the user specify a subpart of the string to be able to be more
	 * specific. Very useful in certain situations.
	 */
	
	public String replace(String originalString, String substring, String stringToReplace, String newString)
	{
		StringBuffer result = new StringBuffer();
		int startIndex = 0;
		int stopIndex  = 0;
		int offset     = 0;
		
		try
		{
			List substrings = search(originalString, substring);
			
			Iterator substringsIterator = substrings.iterator();
			while(substringsIterator.hasNext())
			{
				String currentSubstring = (String)substringsIterator.next();
				String newSubstring = currentSubstring.replaceAll(stringToReplace, newString);
				startIndex = originalString.indexOf(currentSubstring, offset);
				stopIndex   = startIndex + currentSubstring.length();
				result.append(originalString.substring(offset, startIndex));
				result.append(originalString.substring(startIndex, stopIndex));
				result.append(newSubstring);
				offset = stopIndex;
			}
	
			if(offset < originalString.length())
			{
				result.append(originalString.substring(offset));
			}
		}
		catch(Exception e)
		{
			logger.error("The replace function experienced an error:" + e.getMessage(), e);
		}
		
		return result.toString();
	}
	
	

	/**
	 * This method searches for matches to a special expression. 
	 * TODO: Move to an utility class
	 * @param containsMatches
	 * @param regexp
	 * @return
	 */	  
	
	private List search(String containsMatches, String regexp) throws Exception
	{
		List foundMatches = new ArrayList(); 
		int matches = 0;

		PatternCompiler compiler = new Perl5Compiler();
		PatternMatcher matcher = new Perl5Matcher();
		Pattern pattern   = null;
		
		try
		{
			pattern = compiler.compile(regexp);
		}
		catch (MalformedPatternException e)
		{
			throw new Exception("A bad pattern was entered:" + e.getMessage());
		}
		
		PatternMatcherInput input = new PatternMatcherInput(containsMatches);
		
		while (matcher.contains(input, pattern))
		{
			MatchResult result = matcher.getMatch();
			++matches;
			foundMatches.add(result.toString());
		}
		
		return foundMatches;
	}
	
	
	/**
	 * This method helps us find out if the current site node is the same as the one sent in.
	 */
	
	public boolean getIsCurrentSiteNode(Integer siteNodeId)
	{
		return this.siteNodeId.equals(siteNodeId);
	}	
		
	/**
	 * This method helps us find out if the current site node is the same or a child to the sent in one.
	 * So if the current page is a child(in the entire hierarchy below) below the siteNode sent in the 
	 * method returns true. Useful for navigational purposes.  
	 */
	
	public boolean getIsParentToCurrent(Integer siteNodeId)
	{
		return getIsParentToCurrentRecursive(siteNodeId, this.siteNodeId);
	}	
	
	/**
	 * This method helps us find out if the current site node is the same or a child to the sent in one.
	 */
	
	private boolean getIsParentToCurrentRecursive(Integer siteNodeId, Integer currentSiteNodeId)
	{
		boolean isParentToCurrent = false;
		
		try
		{
			if(currentSiteNodeId != null && siteNodeId != null && currentSiteNodeId.intValue() == siteNodeId.intValue())
			{
				isParentToCurrent = true; 
			}
			else
			{
				SiteNodeVO parentSiteNodeVO = this.nodeDeliveryController.getParentSiteNode(getDatabase(), currentSiteNodeId);
				if(parentSiteNodeVO != null)
					isParentToCurrent = getIsParentToCurrentRecursive(siteNodeId, parentSiteNodeVO.getSiteNodeId());
			}
		}
		catch(Exception e)
		{
			logger.error("An error occurred:" + e.getMessage(), e);
		}
		
		return isParentToCurrent;
	}	
	
	/**
	 * This method return true if a localized version with the current language exist
	 */
	
	public boolean getHasLocalizedVersion(Integer contentId)
	{
		boolean ret = false;
		try 
		{
			ret = ContentDeliveryController.getContentDeliveryController().getContentVersionVO(getDatabase(), this.siteNodeId, contentId, this.languageId, false, this.deliveryContext, this.infoGluePrincipal) != null;
		} 
		catch(Exception e)
		{
			logger.error("An error occurred trying to get determine if content:" + contentId + " has a localized version:" + e.getMessage(), e);
		}
		return ret;
	}

	/**
	 * This method returns an access right list so one can get which roles, groups and users can access the page.
	 */
	
	public List getAccessRights(String interceptionPointName, String parameters)
	{
		List accessRights = new ArrayList();
		
		try
		{
			accessRights = AccessRightController.getController().getAccessRightVOList(interceptionPointName, parameters, this.getDatabase());
		}
		catch(Exception e)
		{
			logger.warn("Problems getting access rights for " + interceptionPointName + "(" + parameters + ")", e);
		}
		
		return accessRights;
	}

	/**
	 * This method returns an access right list so one can get which roles, groups and users can access the page.
	 */
	
	public List getPageAccessRights(String interceptionPointName, Integer siteNodeId)
	{
		List accessRights = new ArrayList();
		
		try
		{
		    Integer protectedSiteNodeVersionId = this.nodeDeliveryController.getProtectedSiteNodeVersionId(getDatabase(), siteNodeId);
			if(protectedSiteNodeVersionId == null)
			{
				logger.info("The page was not protected...");
			}
			else
			{
				accessRights = AccessRightController.getController().getAccessRightVOList(interceptionPointName, protectedSiteNodeVersionId.toString(), this.getDatabase());
			}
		}
		catch(Exception e)
		{
			logger.warn("Problems getting access rights for " + interceptionPointName + "(siteNodeId=" + siteNodeId + ")", e);
		}
		
		return accessRights;
	}

	/**
	 * This method return true if the user logged in has access to the siteNode sent in.
	 */
	
	public boolean getHasUserPageAccess(Integer siteNodeId)
	{
		if(siteNodeId == null || siteNodeId < 1)
			return false;
		
		boolean hasUserPageAccess = false;
		
		try 
		{
		    Integer protectedSiteNodeVersionId = this.nodeDeliveryController.getProtectedSiteNodeVersionId(getDatabase(), siteNodeId);
			if(protectedSiteNodeVersionId == null)
			{
				logger.info("The page was not protected...");
				hasUserPageAccess = true;
			}
			else
			{
				logger.info("The page was protected...");
				Principal principal = this.getPrincipal();
				logger.info("Principal:" + principal);
				
				if(principal != null)
				{
					hasUserPageAccess = AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString());
					if(!hasUserPageAccess && getIsDecorated() && getDeliveryContext().getConsiderEditorInDecoratedMode())
				    {
					    String cmsUserName = (String)getHttpServletRequest().getSession().getAttribute("cmsUserName");
					    if(cmsUserName != null)
					    {
						    InfoGluePrincipal cmsPrincipal = getPrincipal(cmsUserName);

						    if(cmsPrincipal != null)
						    	hasUserPageAccess = AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)principal, "SiteNodeVersion.Read", protectedSiteNodeVersionId.toString());
					    }
					}		
				}
			}
		} 
		catch(Exception e)
		{
			logger.error("An error occurred trying to determine if the user had access to page:" + siteNodeId + ":" + e.getMessage(), e);
		}
		
		return hasUserPageAccess;
	}

	/**
	 * This method return true if the user logged in has access to the siteNode sent in.
	 */
	
	public boolean getHasUserPageAccess(Integer siteNodeId, String interceptionPointName)
	{
		if(siteNodeId == null || siteNodeId < 1)
			return false;

		boolean hasUserPageAccess = false;
		
		try 
		{
		    Integer protectedSiteNodeVersionId = this.nodeDeliveryController.getProtectedSiteNodeVersionId(getDatabase(), siteNodeId);
			if(protectedSiteNodeVersionId == null)
			{
				logger.info("The page was not protected...");
				hasUserPageAccess = true;
			}
			else
			{
				logger.info("The page was protected...");
				Principal principal = this.getPrincipal();
				logger.info("Principal:" + principal);
				
				if(principal != null)
				{
					//SiteNodeVersionVO siteNodeVersionVO = this.nodeDeliveryController.getActiveSiteNodeVersionVO(siteNodeId);
					hasUserPageAccess = AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)principal, interceptionPointName, protectedSiteNodeVersionId.toString());
				    if(!hasUserPageAccess && getIsDecorated() && getDeliveryContext().getConsiderEditorInDecoratedMode())
				    {
					    String cmsUserName = (String)getHttpServletRequest().getSession().getAttribute("cmsUserName");
					    if(cmsUserName != null)
					    {
						    InfoGluePrincipal cmsPrincipal = getPrincipal(cmsUserName);

						    if(cmsPrincipal != null)
						    	hasUserPageAccess = AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)principal, interceptionPointName, protectedSiteNodeVersionId.toString());
					    }
					}

				}
			}
		} 
		catch(Exception e)
		{
			logger.error("An error occurred trying to determine if the user had access to page:" + siteNodeId + "-" + interceptionPointName + ":" + e.getMessage(), e);
		}
		
		return hasUserPageAccess;
	}

	
	/**
	 * This method return true if the user logged in has access to the content sent in.
	 */
	
	public boolean getHasUserContentAccess(Integer contentId)
	{
		if(contentId == null || contentId < 1)
			return false;

		boolean hasUserContentAccess = true;
		
		try 
		{
		    if(contentId != null)
		    {
				Integer protectedContentId = ContentDeliveryController.getContentDeliveryController().getProtectedContentId(getDatabase(), contentId);
				logger.info("IsProtected:" + protectedContentId);
				if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, "Content.Read", protectedContentId.toString()))
				{
				    hasUserContentAccess = false;
				    if(getIsDecorated() && getDeliveryContext().getConsiderEditorInDecoratedMode())
				    {
					    String cmsUserName = (String)getHttpServletRequest().getSession().getAttribute("cmsUserName");
					    if(cmsUserName != null)
					    {
						    InfoGluePrincipal cmsPrincipal = getPrincipal(cmsUserName);

						    if(cmsPrincipal != null && AccessRightController.getController().getIsPrincipalAuthorized(cmsPrincipal, "Content.Read", protectedContentId.toString()))
						    	hasUserContentAccess = true;				    		
					    }
					}
				}
		    }
		} 
		catch(Exception e)
		{
			logger.error("An error occurred trying to determine if the user had access to content:" + contentId + ":" + e.getMessage(), e);
		}
		
		return hasUserContentAccess;
	}

	/**
	 * This method return true if the user logged in has access to do the action on the content sent in.
	 * @parameter actionId Any action you wish to look for - example "Content.Read"
	 */
	
	public boolean getHasUserContentAccess(Integer contentId, String action)
	{
		if(contentId == null || contentId < 1)
			return false;

		boolean hasUserContentAccess = true;
		
		try 
		{
			Integer protectedContentId = ContentDeliveryController.getContentDeliveryController().getProtectedContentId(getDatabase(), contentId);
			logger.info("IsProtected:" + protectedContentId);
			if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, action, protectedContentId.toString()))
			{
			    hasUserContentAccess = false;
			    if(getIsDecorated() && getDeliveryContext().getConsiderEditorInDecoratedMode())
			    {
				    String cmsUserName = (String)getHttpServletRequest().getSession().getAttribute("cmsUserName");
				    if(cmsUserName != null)
				    {
					    InfoGluePrincipal cmsPrincipal = getPrincipal(cmsUserName);

					    if(cmsPrincipal != null && AccessRightController.getController().getIsPrincipalAuthorized(cmsPrincipal, action, protectedContentId.toString()))
					    	hasUserContentAccess = true;				    		
				    }
				}
			}
		} 
		catch(Exception e)
		{
			logger.error("An error occurred trying to determine if the user had access to content:" + contentId + "-" + action + ":" + e.getMessage(), e);
		}
		
		return hasUserContentAccess;
	}
	
	/**
	 * This method return true if the user logged in has access to the siteNode sent in.
	 */
	
	public boolean getHasUserPageWriteAccess(Integer siteNodeId)
	{
		if(siteNodeId == null || siteNodeId < 1)
			return false;

		boolean hasUserPageWriteAccess = false;
		
		try 
		{
		    Integer protectedSiteNodeVersionId = this.nodeDeliveryController.getProtectedSiteNodeVersionId(getDatabase(), siteNodeId);
			if(protectedSiteNodeVersionId == null)
			{
				logger.info("The page was not protected...");
				hasUserPageWriteAccess = true;
			}
			else
			{
				logger.info("The page was protected...");
				Principal principal = this.getPrincipal();
				logger.info("Principal:" + principal);
				
				if(principal != null)
				{
				    hasUserPageWriteAccess = AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)principal, "SiteNodeVersion.Write", protectedSiteNodeVersionId.toString());
					if(!hasUserPageWriteAccess && getIsDecorated() && getDeliveryContext().getConsiderEditorInDecoratedMode())
				    {
					    String cmsUserName = (String)getHttpServletRequest().getSession().getAttribute("cmsUserName");
					    if(cmsUserName != null)
					    {
						    InfoGluePrincipal cmsPrincipal = getPrincipal(cmsUserName);

						    if(cmsPrincipal != null)
						    	hasUserPageWriteAccess = AccessRightController.getController().getIsPrincipalAuthorized((InfoGluePrincipal)principal, "SiteNodeVersion.Write", protectedSiteNodeVersionId.toString());
					    }
					}		
				}
			}
		} 
		catch(Exception e)
		{
			logger.error("An error occurred trying to find out if the user had write access to page:" + siteNodeId + ": " + e.getMessage(), e);
		}
		
		return hasUserPageWriteAccess;
	}
	
    /**
     * This method return true if the user has access to the interception point.
     */
    public boolean getHasUserAccess(String interceptionPointName, String extraParameters)
    {
		boolean hasUserContentAccess = true;
		
		try 
		{
			if(!AccessRightController.getController().getIsPrincipalAuthorized(infoGluePrincipal, interceptionPointName, extraParameters))
			{
			    hasUserContentAccess = false;
				if(getIsDecorated() && getDeliveryContext().getConsiderEditorInDecoratedMode())
			    {
				    String cmsUserName = (String)getHttpServletRequest().getSession().getAttribute("cmsUserName");
				    if(cmsUserName != null)
				    {
					    InfoGluePrincipal cmsPrincipal = getPrincipal(cmsUserName);
					    
					    if(cmsPrincipal != null && AccessRightController.getController().getIsPrincipalAuthorized(cmsPrincipal, interceptionPointName, extraParameters))
					    	hasUserContentAccess = true;
				    }
				}		
			}
		} 
		catch(Exception e)
		{
			logger.error("An error occurred trying to determine if the user had access to interceptionPointName:" + interceptionPointName + "-" + extraParameters + ":" + e.getMessage(), e);
		}
		
		return hasUserContentAccess;
    }

	/**
	 * This method returns a list of form elements/attributes based on the schema sent in. 
	 * These consitutes the entire form and a template can then be used to render it in the appropriate technique.
	 */
	
	public List getFormAttributes(String contentBindningName, String attributeName)
	{
		String formDefinition = getContentAttribute(contentBindningName, attributeName, true);
		return FormDeliveryController.getFormDeliveryController().getContentTypeAttributes(formDefinition);
	}
	
	/**
	 * This method returns a list of form elements/attributes based on the schema sent in. 
	 * These consitutes the entire form and a template can then be used to render it in the appropriate technique.
	 */
	
	public List getFormAttributes(Integer contentId, String attributeName)
	{
		String formDefinition = getContentAttribute(contentId, attributeName, true);
		return FormDeliveryController.getFormDeliveryController().getContentTypeAttributes(formDefinition);
	}
	
	
	/**
	 * This method returns the full list of steps for a workflow. 
	 */
	
	public List getWorkflowSteps(String workflowId)
	{
	    List workflowSteps = null;
	    
	    try
	    {
	        InfoGluePrincipal infoGluePrincipal = this.getPrincipal();
	        if(infoGluePrincipal == null)
	        {
			    Map arguments = new HashMap();
			    arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
			    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());

	            infoGluePrincipal = (InfoGluePrincipal) ExtranetController.getController().getAuthenticatedPrincipal(arguments, null);
	        }
	        
			WorkflowController workflowController = WorkflowController.getController();
			logger.info("infoGluePrincipal:" + infoGluePrincipal);
			logger.info("workflowId:" + workflowId);
			workflowSteps = workflowController.getAllSteps(infoGluePrincipal, new Long(workflowId).longValue());
	    }
	    catch(Exception e)
	    {
	        logger.warn("An error occurred when trying to get the steps available: " + e.getMessage(), e);
	    }
	    
		return workflowSteps;
	}
	
	/**
	 * This method returns the list of hsitorical steps for a workflow instance. 
	 */
	
	public List getWorkflowHistoricalSteps(String workflowId)
	{
	    List workflowSteps = null;
	    
	    try
	    {
	        InfoGluePrincipal infoGluePrincipal = this.getPrincipal();
	        if(infoGluePrincipal == null)
	        {
	            Map arguments = new HashMap();
	            arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
			    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());
			    
			    infoGluePrincipal = (InfoGluePrincipal) ExtranetController.getController().getAuthenticatedPrincipal(arguments, null);
	        }
	        
			WorkflowController workflowController = WorkflowController.getController();
			workflowSteps = workflowController.getHistorySteps(infoGluePrincipal, new Long(workflowId).longValue());
	    }
	    catch(Exception e)
	    {
	        logger.warn("An error occurred when trying to get the steps available: " + e.getMessage(), e);
	    }
	    
		return workflowSteps;
	}
	
	/**
	 * This method returns the list of hsitorical steps for a workflow instance. 
	 */
	
	public List getWorkflowCurrentSteps(String workflowId)
	{
	    List workflowSteps = null;
	    
	    try
	    {
	        InfoGluePrincipal infoGluePrincipal = this.getPrincipal();
	        if(infoGluePrincipal == null)
	        {
	            Map arguments = new HashMap();
	            arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
			    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());

		        infoGluePrincipal = (InfoGluePrincipal) ExtranetController.getController().getAuthenticatedPrincipal(arguments, null);
	        }

			WorkflowController workflowController = WorkflowController.getController();
			workflowSteps = workflowController.getCurrentSteps(infoGluePrincipal, new Long(workflowId).longValue());
	    }
	    catch(Exception e)
	    {
	        logger.warn("An error occurred when trying to get the steps available: " + e.getMessage(), e);
	    }
	    
		return workflowSteps;
	}
	
	/**
	 * This method sets a cookie.
	 * 
	 * @param cookieName
	 * @param value
	 * @param domain
	 * @param path
	 * @param maxAge
	 */

	public void setCookie(String cookieName, String value, String domain, String path, Integer maxAge)
	{
	    Cookie cookie = new Cookie(cookieName, value);
	    if(domain != null)
	        cookie.setDomain(domain);
		
	    if(path != null)
	        cookie.setPath(path);
	    
	    if(maxAge != null)
	        cookie.setMaxAge(maxAge.intValue());
	    
	    this.deliveryContext.getHttpServletResponse().addCookie(cookie);	    
	}
	
	/**
	 * This method gets a cookie.
	 * 
	 * @param cookieName
	 */

	public String getCookie(String cookieName)
	{
	    if(this.request != null)
	    {
		    Cookie[] cookies = this.request.getCookies();
		    if(cookies != null)
		    {
			    for(int i=0; i<cookies.length; i++)
			    {
			        Cookie cookie = cookies[i];
			        if(cookie.getName().equals(cookieName))
			            return cookie.getValue();
			    }
		    }
	    }
	    
	    return null;
	}
	
	/**
	 * This method returns a list of actions the current user are assigned or at least allowed to see. 
	 */
	/*
	public List getWorkflowActions() 
	{
	    List workflowActionVOList = null;
	    
	    try
	    {
	        InfoGluePrincipal infoGluePrincipal = this.getPrincipal();
	        
	        if(infoGluePrincipal == null)
	        {
	            Map arguments = new HashMap();
	            arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
			    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());

	            infoGluePrincipal = (InfoGluePrincipal) ExtranetController.getController().getAuthenticatedPrincipal(arguments);
	        }
		   
			WorkflowController workflowController = WorkflowController.getController();
			workflowActionVOList = workflowController.getCurrentWorkflowActionVOList(infoGluePrincipal);
	    }
	    catch(Exception e)
	    {
	        logger.warn("An error occurred when trying to get the actions available: " + e.getMessage(), e);
	    }
	    
		return workflowActionVOList;
	}
	*/
	
	
	/**
	 * This method returns the properties for a specific workflow instance. 
	 */
	/*
	public Map getWorkflowProperties(String workflowId)
	{
	    Map properties = null;
	    
	    try
	    {
	        InfoGluePrincipal infoGluePrincipal = this.getPrincipal();
	        if(infoGluePrincipal == null)
	        {
	            Map arguments = new HashMap();
	            arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
			    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());

			    infoGluePrincipal = (InfoGluePrincipal) ExtranetController.getController().getAuthenticatedPrincipal(arguments);
	        }
	        
			WorkflowController workflowController = WorkflowController.getController();
			properties = workflowController.getProperties(infoGluePrincipal, new Long(workflowId));
	    }
	    catch(Exception e)
	    {
	        logger.warn("An error occurred when trying to get the actions available: " + e.getMessage(), e);
	    }
	    
		return properties;
	}
	*/
	
	/**
	 * This method returns the properties for a specific workflow instance. 
	 */
	/*
	public PropertySet getWorkflowPropertySet(String workflowId)
	{
	    PropertySet propertySet = null;
	    
	    try
	    {
	        InfoGluePrincipal infoGluePrincipal = this.getPrincipal();
	        if(infoGluePrincipal == null)
	        {
	            Map arguments = new HashMap();
	            arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
			    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());

			    infoGluePrincipal = (InfoGluePrincipal) ExtranetController.getController().getAuthenticatedPrincipal(arguments);
	        }
	        
			WorkflowController workflowController = WorkflowController.getController();
			propertySet = workflowController.getPropertySet(infoGluePrincipal, new Long(workflowId));
	    }
	    catch(Exception e)
	    {
	        logger.warn("An error occurred when trying to get the propertySet available: " + e.getMessage(), e);
	    }
	    
		return propertySet;
	}
	*/
	
	/**
	 * This method returns the properties for a specific workflow instance. 
	 */
	/*
	public void setWorkflowProperty(String workflowId, String propertyKey, String propertyValue)
	{
	    try
	    {
	        InfoGluePrincipal infoGluePrincipal = this.getPrincipal();
	        if(infoGluePrincipal == null)
	        {
	            Map arguments = new HashMap();
	            arguments.put("j_username", CmsPropertyHandler.getAnonymousUser());
			    arguments.put("j_password", CmsPropertyHandler.getAnonymousPassword());

			    infoGluePrincipal = (InfoGluePrincipal) ExtranetController.getController().getAuthenticatedPrincipal(arguments);
	        }
	        
			WorkflowController workflowController = WorkflowController.getController();
			workflowController.setProperty(infoGluePrincipal, new Long(workflowId), propertyKey, propertyValue);
	    }
	    catch(Exception e)
	    {
	        logger.warn("An error occurred when trying to get the actions available: " + e.getMessage(), e);
	    }
	}
	*/
	

	/**
	 * This method supplies a method to get the locale of the language currently in use.
	 */
	
	public LanguageVO getLanguage(Integer languageId)
	{
	    LanguageVO language = null;
        
        try
        {
            language = LanguageDeliveryController.getLanguageDeliveryController().getLanguageVO(getDatabase(), languageId);
        }
        catch(Exception e)
        {
            logger.warn("An error occurred when getting language:" + e.getMessage(), e);
        }
        
		return language;
	}

	/**
	 * This method supplies a method to get the locale of the language currently in use.
	 */
	
	public LanguageVO getLanguage(String languageCode)
	{
	    LanguageVO language = null;
        
        try
        {
            language = LanguageDeliveryController.getLanguageDeliveryController().getLanguageWithCode(getDatabase(), languageCode);
        }
        catch(Exception e)
        {
            logger.warn("An error occurred when getting language:" + e.getMessage(), e);
        }
        
		return language;
	}

	/**
	 * This method supplies a method to get the locale of the language sent in.
	 */
	
	public Locale getLanguageCode(Integer languageId) throws SystemException
	{
		return LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(getDatabase(), languageId);
	}

	/**
	 * This method supplies a method to get the locale of the language currently in use.
	 */
	
	public Locale getLocale() throws SystemException
	{
		return LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(getDatabase(), this.languageId);
	}

	/**
	 * This method supplies a method to get the locale the current user prefers in the tools if it's available.
	 */
	
	public Locale getLocaleAvailableInTool(InfoGluePrincipal principal) throws SystemException
	{
		String cacheKey = "principal_" + principal.getName() + "_locale";
		
		Locale locale = (Locale)CacheController.getCachedObject("principalToolPropertiesCache", cacheKey);
		if(locale != null)
		{
			logger.debug("Cached locale:" + locale);
		}
		else
		{
	        Map args = new HashMap();
		    args.put("globalKey", "infoglue");
		    PropertySet ps = PropertySetManager.getInstance("jdbc", args);
		    
		    String prefferredLanguageCode = ps.getString("principal_" + principal.getName() + "_languageCode");
			logger.info("prefferredLanguageCode:" + prefferredLanguageCode);
			if(prefferredLanguageCode != null && !prefferredLanguageCode.equals(""))
			{
				locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithCode(prefferredLanguageCode);
			}
			else
			{
				locale = LanguageDeliveryController.getLanguageDeliveryController().getLocaleWithId(getDatabase(), this.languageId);
			
				List toolLocales = CmsPropertyHandler.getToolLocales();
				if(toolLocales != null && toolLocales.size() > 0 && !toolLocales.contains(locale))
					locale = (Locale)toolLocales.get(0);
			}

			if(locale != null)
				CacheController.cacheObject("principalToolPropertiesCache", cacheKey, locale);
		}
		
		return locale;
	}

	/**
	 * This method returns the logout url.
	 * @author Mattias Bogeblad
	 */
	
	public String getLogoutURL() throws Exception
	{
		AuthenticationModule authenticationModule = AuthenticationModule.getAuthenticationModule(this.getDatabase(), null, this.getHttpServletRequest(), false);
	    return authenticationModule.getLogoutUrl();
	}

   	/**
	 * This method should be much more sophisticated later and include a check to see if there is a 
	 * digital asset uploaded which is more specialized and can be used to act as serverside logic to the template.
	 */
	
	public TemplateController getTemplateController(Integer siteNodeId, Integer languageId, Integer contentId, InfoGluePrincipal infoGluePrincipal, DeliveryContext deliveryContext) throws SystemException, Exception
	{
		return getTemplateController(siteNodeId, languageId, contentId, this.request, infoGluePrincipal, deliveryContext);
	}	
	
	public TemplateController getTemplateController(Integer siteNodeId, Integer languageId, Integer contentId, HttpServletRequest request, InfoGluePrincipal infoGluePrincipal, DeliveryContext deliveryContextt) throws SystemException, Exception
	{
		TemplateController templateController = null;
		templateController = new BasicTemplateController(this.databaseWrapper, infoGluePrincipal);
		templateController.setStandardRequestParameters(siteNodeId, languageId, contentId);	
		templateController.setHttpRequest(request);	
		templateController.setBrowserBean(this.browserBean);
		templateController.setDeliveryControllers(this.nodeDeliveryController, null, this.integrationDeliveryController);	
		templateController.setDeliveryContext(deliveryContext);
		if(isPersistedContext())
		{
		    ((BasicTemplateController) templateController).addToContext(templateLogicContext);
		    ((BasicTemplateController) templateController).setPersistedContext(true);
		}		
		
		String operatingMode = CmsPropertyHandler.getOperatingMode();
		String editOnSite = CmsPropertyHandler.getEditOnSite();
		boolean isEditOnSightDisabled = templateController.getIsEditOnSightDisabled();
		boolean allowEditOnSightAtAll = false;
		if(request.getRequestURL().indexOf("!renderDecoratedPage") > -1)
		    allowEditOnSightAtAll = true;
		
		if(!isEditOnSightDisabled && operatingMode != null && (operatingMode.equals("0") || operatingMode.equals("1") || operatingMode.equals("2")) && editOnSite != null && editOnSite.equalsIgnoreCase("true"))
		{
			templateController = new EditOnSiteBasicTemplateController(this.databaseWrapper, infoGluePrincipal);
			templateController.setStandardRequestParameters(siteNodeId, languageId, contentId);	
			templateController.setHttpRequest(request);	
			templateController.setBrowserBean(browserBean);
			templateController.setDeliveryControllers(this.nodeDeliveryController, null, this.integrationDeliveryController);	
			templateController.setDeliveryContext(deliveryContext);
		}

		
		return templateController;		
	}

	public String decoratePage(String page) 
	{
		return page;
	}

	/**
	 * This method adds the neccessairy html to a output for it to be editable.
	 * It returns an empty string if the delivery engine is in published mode and the showInPublishedMode
	 * is not set to true. Otherwise it shows the htnl you sent in within a clickable tag.
	 */	
	
	public String getEditOnSightTag(Integer contentId, String attributeName, String html, boolean showInPublishedMode)
	{
        return getEditOnSightTag(contentId, this.getLanguageId(), attributeName, html, showInPublishedMode);
	} 

	/**
	 * This method adds the neccessairy html to a output for it to be editable.
	 * It returns an empty string if the delivery engine is in published mode and the showInPublishedMode
	 * is not set to true. Otherwise it shows the htnl you sent in within a clickable tag.
	 */	
	
	public String getEditOnSightTag(Integer contentId, Integer languageId, String attributeName, String html, boolean showInPublishedMode)
	{
	    if(showInPublishedMode == false && this.getOperatingMode().intValue() == 3)
	        return "";
	    else
	    {
	    	String editOnSiteUrl = CmsPropertyHandler.getEditOnSiteUrl();
			String decoratedAttributeValue = "<a href=\"#\" onclick=\"openInlineDivImpl('" + editOnSiteUrl + "?contentId=" + contentId + "&amp;languageId=" + languageId + "&amp;attributeName=" + attributeName + "&amp;forceWorkingChange=true#" + attributeName + "Anchor" + "', 900, 850, true, true); return false;\">" + html + "</a>";
			return decoratedAttributeValue;
	    }
	} 

	public String getExportedContentsUrl(List contentIdList, String fileNamePrefix, boolean includeContentTypes, boolean includeCategories) throws Exception
	{
		String url = "";
		
		try
		{
			int i = 0;
			String fileName = null;
			String filePath = CmsPropertyHandler.getDigitalAssetPath0();
			while(filePath != null)
			{
				fileName = ExportImportController.getController().exportContent(contentIdList, filePath, fileNamePrefix, includeContentTypes, includeCategories);
	
				i++;
				filePath = CmsPropertyHandler.getProperty("digitalAssetPath." + i);
			}
			
			if(fileName != null)
			{
				SiteNode siteNode = this.nodeDeliveryController.getSiteNode(getDatabase(), this.siteNodeId);
				String dnsName = CmsPropertyHandler.getWebServerAddress();
				if(siteNode != null && siteNode.getRepository().getDnsName() != null && !siteNode.getRepository().getDnsName().equals(""))
					dnsName = siteNode.getRepository().getDnsName();
		
				url = urlComposer.composeDigitalAssetUrl(dnsName, null, fileName, deliveryContext); 
			}
		}
		catch (Exception e) 
		{
			logger.error("Problem with getExportedContentsUrl:" + e.getMessage(), e);
		}
		
		return url;
	}

	/**
	 * This method returns the neccessairy html to assign by klicking on a link.
	 * @param componentContentId
	 * @param propertyName
	 * @param html
	 * @param showInPublishedMode
	 * @return
	 */
	public String getAssignPropertyBindingTag(String propertyName, boolean createNew, String html, boolean showInPublishedMode, boolean showDecorated)
	{
		return getAssignPropertyBindingTag(propertyName, createNew, html, showInPublishedMode, showDecorated, null, true);
	}

	/**
	 * This method returns the neccessairy html to assign by klicking on a link.
	 * @param componentContentId
	 * @param propertyName
	 * @param html
	 * @param showInPublishedMode
	 * @return
	 */
	public String getAssignPropertyBindingTag(String propertyName, boolean createNew, String html, boolean showInPublishedMode, boolean showDecorated, String extraParameters, boolean hideComponentPropertiesOnLoad)
	{
		String result = "";
		
		try
		{
			String componentEditorUrl = CmsPropertyHandler.getComponentEditorUrl();
	
			DecoratedComponentBasedHTMLPageInvoker dec = new DecoratedComponentBasedHTMLPageInvoker();
			Collection properties = dec.getComponentProperties(this.getComponentLogic().getInfoGlueComponent().getId(), this, siteNodeId, languageId, this.getComponentLogic().getInfoGlueComponent().getContentId());
	
			ComponentProperty property = null;
			Iterator propertiesIterator = properties.iterator();
			while(propertiesIterator.hasNext())
			{
				ComponentProperty propertyCandidate = (ComponentProperty)propertiesIterator.next();
				if(logger.isInfoEnabled())
					logger.info("propertyCandidate:" + propertyCandidate.getName());
				if(propertyCandidate.getName().equals(propertyName))	
					property = propertyCandidate;
			}
			
			String contentId = "-1";
			
			String createUrl = "";
			String assignUrl = "";
			
			Integer componentId = this.getComponentLogic().getInfoGlueComponent().getId();
			Integer repositoryId = this.getSiteNode().getRepositoryId();
			
			if(property != null && property.getVisualizingAction() != null && !property.getVisualizingAction().equals(""))
			{
				assignUrl = componentEditorUrl + property.getVisualizingAction() + "?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad;
			}
			else
			{	
				if(property != null && property.getEntityClass().equalsIgnoreCase("Content"))
				{
				    String allowedContentTypeIdParameters = "";

				    if(property.getAllowedContentTypeNamesArray() != null && property.getAllowedContentTypeNamesArray().length > 0)
				    {
				        allowedContentTypeIdParameters = "&" + property.getAllowedContentTypeIdAsUrlEncodedString(getDatabase());
				        logger.info("allowedContentTypeIdParameters:" + allowedContentTypeIdParameters);
				    }
				    
					if(property.getIsMultipleBinding())
					{
						if(property.getIsAssetBinding())
						{
							if(CmsPropertyHandler.getComponentBindningAssetBrowser().equalsIgnoreCase("classic"))
								assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showContentTreeForMultipleAssetBinding.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + allowedContentTypeIdParameters + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad;
							else
								assignUrl = componentEditorUrl + "ViewContentVersion!viewAssetBrowserForMultipleComponentBindingV3.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + allowedContentTypeIdParameters + "&assetTypeFilter=" + property.getAssetMask() + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad;
						}
						else
							assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showContentTreeForMultipleBindingV3.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + allowedContentTypeIdParameters + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad;
					}
					else
					{
						if(property.getIsAssetBinding())
						{
							String assignedParameters = "";
							Iterator<ComponentBinding> bindingsIterator = property.getBindings().iterator();
							while(bindingsIterator.hasNext())
							{
								ComponentBinding componentBinding = bindingsIterator.next();
								assignedParameters = "&assignedContentId=" + componentBinding.getEntityId() + "&assignedAssetKey=" + componentBinding.getAssetKey() + "&assignedPath=" + getVisualFormatter().encodeURI(property.getValue());
							}
							
							if(CmsPropertyHandler.getComponentBindningAssetBrowser().equalsIgnoreCase("classic"))
								assignUrl = componentEditorUrl + "ViewContentVersion!viewAssetsForComponentBinding.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + allowedContentTypeIdParameters + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + assignedParameters + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad;
							else
								assignUrl = componentEditorUrl + "ViewContentVersion!viewAssetBrowserForComponentBindingV3.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + allowedContentTypeIdParameters + "&assetTypeFilter=" + property.getAssetMask() + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + assignedParameters + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad;
						}
						else
							assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showContentTreeV3.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + allowedContentTypeIdParameters + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad;
					}
				}
				else if(property.getEntityClass().equalsIgnoreCase("SiteNode"))
				{
					if(property.getIsMultipleBinding())
						assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showStructureTreeForMultipleBindingV3.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad;
					else
						assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showStructureTreeV3.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad;
				}
				else if(property.getEntityClass().equalsIgnoreCase("Category"))
				{
					if(property.getIsMultipleBinding())
						assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showCategoryTreeForMultipleBinding.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad;
					else
						assignUrl = componentEditorUrl + "ViewSiteNodePageComponents!showCategoryTree.action?repositoryId=" + repositoryId + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad;
				}
			}

			
			if(property.getCreateAction() != null && !property.getCreateAction().equals(""))
			{
				createUrl = componentEditorUrl + property.getCreateAction() + "?repositoryId=" + this.getSiteNode().getRepositoryId() + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad;
			}
			else
			{	
				if(property.getVisualizingAction() != null && !property.getVisualizingAction().equals(""))
				{
					createUrl = assignUrl;
				}
				else if(property.getEntityClass().equalsIgnoreCase("Content"))
				{
				    String allowedContentTypeIdParameters = "";
	
				    if(property.getAllowedContentTypeNamesArray() != null && property.getAllowedContentTypeNamesArray().length > 0)
				    {
				        allowedContentTypeIdParameters = "&" + property.getAllowedContentTypeIdAsUrlEncodedString(getDatabase());
				        logger.info("allowedContentTypeIdParameters:" + allowedContentTypeIdParameters);
				    }
				    
				    String key = "ViewSiteNodePageComponents!addComponentPropertyBinding.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=-1&entity=Content&entityId=#entityId&componentId=" + componentId + "&propertyName=" + property.getName() + "&path=#path&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad;
				    
			        String returnAddress = (String)CacheController.getCachedObjectFromAdvancedCache("encodedStringsCache", key);
			        if(returnAddress == null)
			        {
			        	returnAddress = URLEncoder.encode(key, "UTF-8");
			        	CacheController.cacheObjectInAdvancedCache("encodedStringsCache", key, returnAddress);
			        }
			        
			        String cancelKey = this.getOriginalFullURL();
			        String cancelAddress = (String)CacheController.getCachedObjectFromAdvancedCache("encodedStringsCache", cancelKey);
			        if(cancelAddress == null)
			        { 
			        	cancelAddress = URLEncoder.encode(cancelKey, "UTF-8");
			        	CacheController.cacheObjectInAdvancedCache("encodedStringsCache", cancelKey, cancelAddress);
			        }

				    //String returnAddress = URLEncoder.encode(key, "UTF-8");
					
					if(property.getIsMultipleBinding())
						createUrl = componentEditorUrl + "CreateContentWizardFinish!V3.action?repositoryId=" + this.getSiteNode().getRepositoryId() + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + allowedContentTypeIdParameters + "&refreshAddress=" + returnAddress + "&cancelAddress=" + cancelAddress + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad + (extraParameters != null ? "&" + extraParameters : "");
					else
						createUrl = componentEditorUrl + "CreateContentWizardFinish!V3.action?repositoryId=" + this.getSiteNode().getRepositoryId() + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + allowedContentTypeIdParameters + "&refreshAddress=" + returnAddress + "&cancelAddress=" + cancelAddress + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad + (extraParameters != null ? "&" + extraParameters : "");
				}
				else if(property.getEntityClass().equalsIgnoreCase("SiteNode"))
				{
				    String key = "ViewSiteNodePageComponents!addComponentPropertyBinding.action?siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=-1&entity=SiteNode&entityId=#entityId&componentId=" + componentId + "&propertyName=" + property.getName() + "&path=#path&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad;
				    
			        String returnAddress = (String)CacheController.getCachedObjectFromAdvancedCache("encodedStringsCache", key);
			        if(returnAddress == null)
			        {
			        	returnAddress = URLEncoder.encode(key, "UTF-8");
			        	CacheController.cacheObjectInAdvancedCache("encodedStringsCache", key, returnAddress);
			        }
			        
			        String cancelKey = this.getOriginalFullURL();
			        String cancelAddress = (String)CacheController.getCachedObjectFromAdvancedCache("encodedStringsCache", cancelKey);
			        if(cancelAddress == null)
			        {
			        	cancelAddress = URLEncoder.encode(cancelKey, "UTF-8");
			        	CacheController.cacheObjectInAdvancedCache("encodedStringsCache", cancelKey, cancelAddress);
			        }

					if(property.getIsMultipleBinding())
						createUrl = componentEditorUrl + "CreateSiteNodeWizardFinish.action?repositoryId=" + this.getSiteNode().getRepositoryId() + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + "&refreshAddress=" + returnAddress + "&cancelAddress=" + cancelAddress + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad + (extraParameters != null ? "&" + extraParameters : "");
					else
						createUrl = componentEditorUrl + "CreateSiteNodeWizardFinish.action?repositoryId=" + this.getSiteNode().getRepositoryId() + "&siteNodeId=" + siteNodeId + "&languageId=" + languageId + "&contentId=" + contentId + "&componentId=" + componentId + "&propertyName=" + property.getName() + "&refreshAddress=" + returnAddress + "&cancelAddress=" + cancelAddress + "&showSimple=" + getDeliveryContext().getShowSimple() + "&showDecorated=" + showDecorated + "&hideComponentPropertiesOnLoad=" +hideComponentPropertiesOnLoad + (extraParameters != null ? "&" + extraParameters : "");
				}
			}
	
		    if(showInPublishedMode == false && this.getOperatingMode().intValue() == 3)
		    	result = "";
		    else
		    {
			    String editOnSiteUrl = CmsPropertyHandler.getEditOnSiteUrl();

			    String url = assignUrl;
			    if(!createNew)
			    	result = "<a href=\"#\" onclick=\"openInlineDivImpl('" + assignUrl + "', 900, 850, true, true); return false;\">" + html + "</a>";
			    else
			    	result = "<a href=\"#\" onclick=\"openInlineDivImpl('" + createUrl + "', 900, 850, true, true); return false;\">" + html + "</a>";
		    }
		}
		catch(Exception e)
		{
			e.printStackTrace();
			result = "";
		}
		
		return result;
	}


	public DeliveryContext getDeliveryContext() 
	{
		return deliveryContext;
	}
	
	public void setDeliveryContext(DeliveryContext deliveryContext) 
	{
		this.deliveryContext = deliveryContext;
		this.nodeDeliveryController.setDeliveryContext(deliveryContext);
	}
	
	/**
	 * 
	 * @return true if this TemplateController will persist the 
	 * templateLogic context in nested parsing.
	 */
    public boolean isPersistedContext()
    {
        return persistedContext;
    }
    /**
     * If set to true, the current templateLogicContext
     * will be passed to any new templateControllers from 
     * this point. New TemplateControllers are created 
     * in all nested parsing of the template, like include
     * getParsedContentAttribute etc. 
     * 
     * @param persistedContext 
     */
    public void setPersistedContext(boolean persistedContext)
    {
        this.persistedContext = persistedContext;
    }	
    
    public boolean getThreatFoldersAsContents()
    {
        return threatFoldersAsContents;
    }
    
    public void setThreatFoldersAsContents(boolean threatFoldersAsContents)
    {
        this.threatFoldersAsContents = threatFoldersAsContents;
    }

    /**
     * A method to check if the current pagenode is decorated with EditOnSight
     * or not. Checks if it's the BasicTemplateController or the
     * EditOnSiteBasicTemplateController which is used as a render.
     * 
     * @return true if the pagenode is rendered with EditOnSight decoration.
     */
    public boolean getIsDecorated()
    {
        return ( this instanceof EditOnSiteBasicTemplateController );
    }
}