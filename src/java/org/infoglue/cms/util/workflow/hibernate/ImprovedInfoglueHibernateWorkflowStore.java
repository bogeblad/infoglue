package org.infoglue.cms.util.workflow.hibernate;

import java.util.HashMap;
import java.util.Map;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;

import org.infoglue.deliver.util.CacheController;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import com.opensymphony.module.propertyset.cached.CachingPropertySet;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.spi.hibernate.AbstractHibernateWorkflowStore;
import com.opensymphony.workflow.util.PropertySetDelegate;


/**
 * This is the new Improved Hibernate workflow store. Works much better with session handling.
 */
public class ImprovedInfoglueHibernateWorkflowStore extends AbstractHibernateWorkflowStore 
{
	private Session session = null;
	private SessionFactory sessionFactory = null;
	
	private Session getSession()
	{
		return session;
	}

	private void setSession(Session session)
	{
		this.session = session;
	}

	private SessionFactory getSessionFactory()
	{
		return sessionFactory;
	}

	private void setSessionFactory(SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

    public ImprovedInfoglueHibernateWorkflowStore() 
    {
        super();
    }

    // Now session management is delegated to user
    
    public void init(Map props) throws StoreException 
    {
    	setSessionFactory((SessionFactory) props.get("sessionFactory"));
    	setSession((Session) props.get("session"));

        setPropertySetDelegate((PropertySetDelegate) props.get("propertySetDelegate"));
    }

    protected Object execute(InternalCallback action) throws StoreException 
    {
    	try 
        {
            return action.doInHibernate(getSession());
        } 
        catch (HibernateException e) 
        {
        	throw new StoreException(e);
        }
    }

	/**
	 * 
	 */
    public ImprovedInfoglueHibernateWorkflowStore(SessionFactory sessionFactory) 
    {
		super();
		this.setSessionFactory(sessionFactory);
    }

	/**
	 * 
	 */
    public PropertySet getPropertySet(long entryId) 
    {
    	
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
		        configurationProvider.setSessionFactory(getSessionFactory());
		
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
    			
    			//Proposal - remove the cache by key
	        	CacheController.clearCache("propertySetCache", key);
    		}
    	}
    	
        return ps;
    }
     
}
