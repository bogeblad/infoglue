package org.infoglue.cms.util.workflow.hibernate;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.infoglue.cms.applications.workflowtool.util.InfogluePropertySet;
import org.infoglue.cms.util.workflow.WorkflowFacade;
import org.infoglue.deliver.util.CacheController;
import org.infoglue.deliver.util.Timer;

//import org.hibernate.SessionFactory;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import com.opensymphony.module.propertyset.cached.CachingPropertySet;
import com.opensymphony.workflow.StoreException;
//import com.opensymphony.workflow.spi.hibernate3.HibernateWorkflowStore;
import com.opensymphony.workflow.spi.hibernate.HibernateWorkflowStore;


/**
 * Quickfix
 */
public class InfoglueHibernateWorkflowStore extends HibernateWorkflowStore 
{
	private final static Logger logger = Logger.getLogger(InfoglueHibernateWorkflowStore.class.getName());

	/**
	 * 
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * 
	 */
    public InfoglueHibernateWorkflowStore() {}

	/**
	 * 
	 */
    public InfoglueHibernateWorkflowStore(SessionFactory sessionFactory) throws StoreException {
		super(sessionFactory);
		this.sessionFactory = sessionFactory;
    }

    

	/**
	 * 
	 */
    public void init(Map props) throws StoreException 
    {
    	sessionFactory = (SessionFactory) props.get("sessionFactory");
		super.init(props);
    }

	/**
	 * 
	 */
    public PropertySet getPropertySet(long entryId) {
        
    	String key = "psCache_" + entryId;
    	PropertySet ps = (PropertySet)CacheController.getCachedObject("propertySetCache", key);
    	
    	if(ps == null)
    	{
    		try
    		{
		    	HashMap args = new HashMap();
		        args.put("entityName", "OSWorkflowEntry");
		        args.put("entityId", new Long(entryId));
		
				InfoglueDefaultHibernateConfigurationProvider configurationProvider = new InfoglueDefaultHibernateConfigurationProvider();
		        configurationProvider.setSessionFactory(sessionFactory);
		
		        args.put("configurationProvider", configurationProvider);
		        
				ps = new CachingPropertySet();
				
				Map args2 = new HashMap();
				args2.put("PropertySet", PropertySetManager.getInstance("hibernate", args));
				args2.put("bulkload", new Boolean(true));
				
				ps.init(new HashMap(), args2);
				CacheController.cacheObject("propertySetCache", key, ps);
	    		//logger.info("Caching propertySet for entry: " + entryId + ":" + ps);
				
		        ps = PropertySetManager.getInstance("hibernate", args);
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			
    			try
    			{
					logger.error("\n\nRestoring the session factory....");
	    			//sessionFactory.close();
					sessionFactory = new Configuration().configure().buildSessionFactory();
					
					InfoglueDefaultHibernateConfigurationProvider configurationProvider = new InfoglueDefaultHibernateConfigurationProvider();
			        configurationProvider.setSessionFactory(sessionFactory);
			
			        Map args = new HashMap();
			        args.put("configurationProvider", configurationProvider);
			        
					ps = new CachingPropertySet();
					
					Map args2 = new HashMap();
					args2.put("PropertySet", PropertySetManager.getInstance("hibernate", args));
					args2.put("bulkload", new Boolean(true));
					
					ps.init(new HashMap(), args2);
    			}
				catch (HibernateException he)
				{
					he.printStackTrace();
				}
    		}
    	}
    	
        return ps;
    }
     
}
