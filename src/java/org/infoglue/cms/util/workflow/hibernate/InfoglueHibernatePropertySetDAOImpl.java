package org.infoglue.cms.util.workflow.hibernate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.infoglue.deliver.util.Timer;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;

import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.hibernate.HibernatePropertySetDAO;
import com.opensymphony.module.propertyset.hibernate.PropertySetItem;


/**
 * Quickfix
 */
public class InfoglueHibernatePropertySetDAOImpl implements HibernatePropertySetDAO {
	/**
	 * 
	 */
    private SessionFactory sessionFactory;

	/**
	 * 
	 */
    public InfoglueHibernatePropertySetDAOImpl(SessionFactory sessionFactory) {
    	
		this.sessionFactory = sessionFactory;
    }

	/**
	 * 
	 */
	public void setImpl(PropertySetItem item, boolean isUpdate) {
        Session session = null;
        
        String entityNameIdKey = "" + item.getEntityName() + "_" + item.getEntityId();
    	System.out.println("setImpl....:" + entityNameIdKey);
    	System.out.println("setImpl....:" + item.getKey() + "=" + item.getStringVal());

    	Map<String,Object> keyMap = entityNameIdKeyMap.get(entityNameIdKey);
        Map<String,Object> valueMap = entityNameIdValueMap.get(entityNameIdKey);
        
        //System.out.println("entityNameIdValueMap:" + entityNameIdValueMap);
        
        //System.out.println("valueMap:" + valueMap);
       
        if(valueMap != null)
        {
            //System.out.println("Removing :" + item.getKey());
        	valueMap.remove(item.getKey());
        }
        
        if(keyMap != null)
        {
        	//System.out.println("Removing knowledge of " + item.getKey() + " so that it will get search for in the database");
        	if(keyMap.containsKey(item.getKey()))
        	{
        		keyMap.remove(item.getKey());        		
            	//System.out.println("Rereading object...");
        		findByKey(item.getEntityName(), item.getEntityId(), item.getKey());
        	}
        	else
        	{
        		keyMap.put(item.getKey(), true);  
            	//System.out.println("Rereading object...");
        		findByKey(item.getEntityName(), item.getEntityId(), item.getKey());
        	}
        }
        
        
        try 
        {
        	session = this.sessionFactory.openSession();
        	        	
            if (isUpdate) {
                session.update(item);
            } else {
                session.save(item);
            }

            session.flush();
        } catch (HibernateException he) {
            throw new PropertyException("Could not save key '" + item.getKey() + "':" + he.getMessage());
        } finally {
            try {
                if (session != null) {
                    if (!session.connection().getAutoCommit()) {
                        session.connection().commit();
                    }

                    session.close();
                }
            } catch (Exception e) {
            }
        }
        
        if(valueMap != null)
        {
            //System.out.println("Removing :" + item.getKey());
        	valueMap.put(item.getKey(), item);
        }
    }

    public Collection getKeys(String entityName, Long entityId, String prefix, int type) {
        //System.out.println("getKeys");

        Session session = null;
        List list = null;

        try {
            session = this.sessionFactory.openSession();
            list = InfoglueHibernatePropertySetDAOUtils.getKeysImpl(session, entityName, entityId, prefix, type);
        } catch (HibernateException e) {
            list = Collections.EMPTY_LIST;
        } finally {
            try {
                if (session != null) {
                    session.flush();
                    session.close();
                }
            } catch (Exception e) {
            }
        }

        return list;
    }

    public PropertySetItem create(String entityName, long entityId, String key) {
        return new InfogluePropertySetItemImpl(entityName, entityId, key);
    }

    private static Map<String, Map<String,Object>> entityNameIdKeyMap = new HashMap<String, Map<String,Object>>();
    private static Map<String, Map<String,Object>> entityNameIdValueMap = new HashMap<String, Map<String,Object>>();
    
    public PropertySetItem findByKey(String entityName, Long entityId, String key) 
    {
        Timer t = new Timer();
        
        String entityNameIdKey = "" + entityName + "_" + entityId;
        //System.out.println("findByKey: " + entityNameIdKey);
        Map<String,Object> keyMap = entityNameIdKeyMap.get(entityNameIdKey);
        Map<String,Object> valueMap = entityNameIdValueMap.get(entityNameIdKey);
        
        //System.out.println("entityNameIdValueMap:" + entityNameIdValueMap);
        
        if(keyMap == null)
        {
        	keyMap = new HashMap<String,Object>();
        	entityNameIdKeyMap.put(entityNameIdKey, keyMap);
        	List<String> keyList = (List<String>)getKeys(entityName, entityId, null, 0);
        	for(String currentKey : keyList)
        	{
        		//System.out.println("Found:" + currentKey);
        		keyMap.put(currentKey, true);
        	}
        }
        if(valueMap == null)
        {
        	valueMap = new HashMap<String,Object>();
        	entityNameIdValueMap.put(entityNameIdKey, valueMap);
        }
        
        /*
        if(valueMap != null && valueMap.get(key) != null)
        {
        	PropertySetItem item = (PropertySetItem)valueMap.get(key);
            if(key.equals("workflow_status") || key.indexOf("languageId") > -1)
            {
            	System.out.println("cached Key:" + key);
            	System.out.println("cached Item:" + item.getType());
            	System.out.println("cached Item:" + item.getStringVal());
            }
        	//System.out.println("Cached item exists:" + key);
        	return item;
        }
        if(keyMap != null && keyMap.get(key) == null)
        {
        	//System.out.println("No key in cached key map... returning null for:" + key);
        	return null;
        }
        */
        
        
        Session session = null;
        PropertySetItem item = null;

        try {
            session = this.sessionFactory.openSession();
            item = InfoglueHibernatePropertySetDAOUtils.getItem(session, entityName, entityId, key);
            session.flush();
        } catch (HibernateException e) {
            //t.printElapsedTime("FindByKey empty: " + entityName + ":" + entityId + ":" + key);
        	//e.printStackTrace();
            
            return null;
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
            }
        }
        
        if(valueMap != null)
        	valueMap.put(key, item);
        if(key.equals("workflow_status") || key.indexOf("languageId") > -1)
        {
        	System.out.println("Key:" + key);
        	System.out.println("Item:" + item.getType());
        	System.out.println("Item:" + item.getStringVal());
        }

        t.printElapsedTime("FindByKey: " + entityName + ":" + entityId + ":" + key);
        return item;
    }

    public void remove(String entityName, Long entityId) {
        System.out.println("remove:" + entityName + "_" + entityId);
        Session session = null;

        try {
            session = this.sessionFactory.openSession();

            //hani: todo this needs to be optimised rather badly, but I have no idea how
            Collection keys = getKeys(entityName, entityId, null, 0);
            Iterator iter = keys.iterator();

            while (iter.hasNext()) {
                String key = (String) iter.next();
                
                String entityNameIdKey = "" + entityName + "_" + entityId;
                Map<String,Object> valueMap = entityNameIdValueMap.get(entityNameIdKey);
                if(valueMap != null)
                {
                	System.out.println("Removing " + key);
                	valueMap.remove(key);
                }
                
                session.delete(InfoglueHibernatePropertySetDAOUtils.getItem(session, entityName, entityId, key));
            }

            session.flush();
        } catch (HibernateException e) {
            throw new PropertyException("Could not remove all keys: " + e.getMessage());
        } finally {
            try {
                if (session != null) {
                    if (!session.connection().getAutoCommit()) {
                        session.connection().commit();
                    }

                    session.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public void remove(String entityName, Long entityId, String key) {
    	System.out.println("Remove full");
        Session session = null;

        try {
            session = this.sessionFactory.openSession();
            
            String entityNameIdKey = "" + entityName + "_" + entityId;
            Map<String,Object> valueMap = entityNameIdValueMap.get(entityNameIdKey);
            if(valueMap != null)
            {
            	System.out.println("Removing " + key);
            	valueMap.remove(key);
            }

            session.delete(InfoglueHibernatePropertySetDAOUtils.getItem(session, entityName, entityId, key));
            session.flush();
        } catch (HibernateException e) {
            throw new PropertyException("Could not remove key '" + key + "': " + e.getMessage());
        } finally {
            try {
                if (session != null) {
                    if (!session.connection().getAutoCommit()) {
                        session.connection().commit();
                    }

                    session.close();
                }
            } catch (Exception e) {
            }
        }
    }
}
