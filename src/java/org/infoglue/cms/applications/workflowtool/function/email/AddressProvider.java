/**
 * 
 */
package org.infoglue.cms.applications.workflowtool.function.email;

import java.util.ArrayList;
import java.util.Collection;

import javax.mail.internet.InternetAddress;

import org.infoglue.cms.applications.workflowtool.function.InfoglueFunction;

import com.opensymphony.workflow.WorkflowException;

/**
 * 
 */
public abstract class AddressProvider extends InfoglueFunction
{
	/**
	 * 
	 */
	private static final String REQUIRED_ARGUMENT = "required";
	
	/**
	 * The addresses.
	 */
	private Collection addresses; // type: <InternetAddress>

	/**
	 * The illegal addresses.
	 */
	private Collection illegalAddresses; // type: <String>

	/**
	 * Indicates if empty addresses should be silently discarded.
	 */
	private boolean required;
	
	/**
	 * Default constructor. 
	 */
	public AddressProvider() 
	{
		super();
	}

	/**
	 * Add all recipients. Note that empty email-addresses will be discarded
	 * if the <code>required</code> attribute is <code>false</code>.
	 */
	protected abstract void populate() throws WorkflowException;

	/**
	 * 
	 */
	protected final void execute() throws WorkflowException 
	{
		populate();
		setParameter(EmailFunction.TO_PARAMETER, addresses);
	}
	
	/**
	 * 
	 */
	protected final void addRecipient(final String email)
	{
		final boolean isEmpty = (email == null || email.trim().length() == 0); 
		
		if(!isEmpty)
		{
			try
			{
				addresses.add(new InternetAddress(email.trim()));
			}
			catch(Exception e)
			{
				illegalAddresses.add(email);
			}
		}
		else if(required)
		{
			illegalAddresses.add("");
		}
	}
	
	/**
	 * Method used for initializing the function; will be called before <code>execute</code> is called.
	 * <p><strong>Note</strong>! You must call <code>super.initialize()</code> first.</p>
	 * 
	 * @throws WorkflowException if an error occurs during the initialization.
	 */
	protected void initialize() throws WorkflowException 
	{
		super.initialize();
		this.required         = getArgument(REQUIRED_ARGUMENT, "true").equalsIgnoreCase("true");
		this.addresses        = (Collection) getParameter(EmailFunction.TO_PARAMETER, new ArrayList());
		this.illegalAddresses = (Collection) getParameter(EmailFunction.ILLEGAL_ADDRESSES_PARAMETER, new ArrayList());
	}
}
