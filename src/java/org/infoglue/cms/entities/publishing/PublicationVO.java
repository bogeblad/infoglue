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

package org.infoglue.cms.entities.publishing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.kernel.ValidatableEntityVO;
import org.infoglue.cms.entities.publishing.impl.simple.PublicationImpl;
import org.infoglue.cms.util.validators.ConstraintRule;
import org.infoglue.cms.util.validators.Range;

/**
 * This class represents a published edition. The individual entries in the edition
 * are contained in the publicationDetails List.
 */
public class PublicationVO extends ValidatableEntityVO implements BaseEntityVO
{
    private Integer publicationId;
    private Integer repositoryId;
    private String name;
    private String description = "No description";;
    private Date publicationDateTime;
    private String publisher = null;
	private List publicationDetails = new ArrayList();

	public Integer getId()
	{
		return getPublicationId();
	}

    public Integer getPublicationId()
    {
        return this.publicationId;
    }

    public void setPublicationId(Integer publicationId)
    {
        this.publicationId = publicationId;
    }

	public Integer getRepositoryId()
	{
		return repositoryId;
	}

	public void setRepositoryId(Integer repositoryId)
	{
		this.repositoryId = repositoryId;
	}

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
    	if(description != null && !description.equals(""))
    		this.description = description;
    }

    public Date getPublicationDateTime()
    {
        return this.publicationDateTime;
    }

    public void setPublicationDateTime(Date publicationDateTime)
    {
        this.publicationDateTime = publicationDateTime;
    }

	public String getPublisher()
	{
		return this.publisher;
	}

	public void setPublisher(String publisher)
	{
		this.publisher = publisher;
	}

	public List getPublicationDetails()
	{
		return publicationDetails;
	}

	public void setPublicationDetails(List c)
	{
		publicationDetails = (c != null)? c : new ArrayList();
	}

	public void PrepareValidation()
	{
		// Define the constraint rules for this valueobject
		// maybe this belongs in the setters of this object?.
		// then this method would be obsolete, and the validation
		// should be initiated through a controller from the
		// action class??.
		// -----------------------------------------

		// On the rulelist set the class that holds this vo, the class
		// that is known to castor. This is for unique validation and
		// if possible should not be set in the valueobject, but preferably
		// in the actual castor-entity class. (Im not to satisfied with this
		// construction).
		rules.setEntityClass(PublicationImpl.class);

		// Create a new constraintrule, supply constraint type, and field that this rule
		// applies to.
 		ConstraintRule cr = new ConstraintRule(org.infoglue.cms.util.validators.Constants.STRING, "Publication.name");

 		// Set the constraints
 		cr.setValidRange(new Range(2, 50) );
 		cr.unique=false;  // public variabel will be changed to setter later
 		cr.required=true; // public variabel will be changed to setter later
 		cr.setValue(name);

 		// Add this rule to the rulelist
 		rules.addRule(cr);

		// Create a new constraintrule, supply constraint type, and field that this rule
		// applies to.
 		cr = new ConstraintRule(org.infoglue.cms.util.validators.Constants.STRING, "Publication.description");

 		// Set the constraints
 		cr.setValidRange(new Range(2, 50) );
 		cr.unique=false;	// public variabel will be changed to setter later
 		cr.required=true; // public variabel will be changed to setter later
 		cr.setValue(description);

 		// Add this rule to the rulelist
 		rules.addRule(cr);
	}
}

