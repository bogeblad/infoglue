package org.infoglue.cms.applications.common.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.infoglue.cms.entities.kernel.Persistent;
import org.infoglue.cms.exception.ConstraintException;

/**
 * Base class to provide Actions with some common semantics for dealing with
 * a specific domain object (model) and collections of domain objects (models).
 *
 * @author Frank Febbraro (frank@phase2technology.com)
 */
public abstract class ModelAction extends InfoGlueAbstractAction
{
	private Persistent model = createModel();
	private Collection models = new ArrayList();

	public Persistent getModel()
	{
		return model;
	}

	protected void setModel(Persistent o)
	{
		model = (o == null) ? createModel() : o;
	}

	public Collection getModels()
	{
		return models;
	}

	protected void setModels(Collection c)
	{
		models = (c == null) ? new ArrayList() : c;
	}

	/**
	 * Template method used by subclasses to provide a new instance of the model.
	 * @return a new PersistentObject
	 */
	protected abstract Persistent createModel();

	/**
	 * Perform the validation operation on the model
	 * @throws ConstraintException If a validation error exists
	 */
	protected void validateModel() throws ConstraintException
	{
		getModel().validate().throwIfNotEmpty();
	}

	/**
	 * Default implementation for WebworkAbstractAction, subclasses shold feel free to override
	 */
	protected String doExecute() throws Exception
	{
		return SUCCESS;
	}

	//-------------------------------------------------------------------------
	// Override some methods to remove the reliance on anything HTTP.
	// WebWork did a good job abstracting HTTP away, lets take advantage of it
	//-------------------------------------------------------------------------

}
