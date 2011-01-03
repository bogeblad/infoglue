package org.infoglue.common.settings.controllers;

import java.util.List;

import org.exolab.castor.jdo.Database;
import org.infoglue.cms.entities.management.InfoGlueProperty;

public interface CastorSettingsPersister
{
    /**
     * This method returns a Property based on it's primary key inside a transaction
     * @return Property
     * @throws Exception
     */

	public InfoGlueProperty getProperty(Long id, Database database) throws Exception;
    
    
    /**
     * Gets a list of all events available for a particular day.
     * @return List of Event
     * @throws Exception
     */
    
    public InfoGlueProperty getProperty(String nameSpace, String name, Database database) throws Exception;

    
    /**
     * This method is used to create a new Property object in the database inside a transaction.
     */
    
    public InfoGlueProperty createProperty(String nameSpace, String name, String value, Database database) throws Exception;
    

    /**
     * Updates an property.
     * 
     * @throws Exception
     */
    
    public void updateProperty(String nameSpace, String name, String value, Database database) throws Exception;
    
    
    /**
     * Updates an property inside an transaction.
     * 
     * @throws Exception
     */
    
    public void updateProperty(InfoGlueProperty property, String value, Database database) throws Exception;

}
