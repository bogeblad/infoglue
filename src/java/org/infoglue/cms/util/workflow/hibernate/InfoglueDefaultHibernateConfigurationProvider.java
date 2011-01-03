package org.infoglue.cms.util.workflow.hibernate;

import net.sf.hibernate.SessionFactory;

import com.opensymphony.module.propertyset.hibernate.DefaultHibernateConfigurationProvider;
import com.opensymphony.module.propertyset.hibernate.HibernatePropertySetDAO;


/**
 * Quickfix
 */
public class InfoglueDefaultHibernateConfigurationProvider extends DefaultHibernateConfigurationProvider 
{
    private HibernatePropertySetDAO propertySetDAO;
    private SessionFactory sessionFactory;

	/**
	 * 
	 */
    public HibernatePropertySetDAO getPropertySetDAO() 
    {
        if (propertySetDAO == null)
            propertySetDAO = new InfoglueHibernatePropertySetDAOImpl(sessionFactory);
        return propertySetDAO;
    }

	/**
	 * 
	 */
    public void setSessionFactory(SessionFactory sessionFactory) 
    {
        this.sessionFactory = sessionFactory;
		super.setSessionFactory(sessionFactory);
    }
}
