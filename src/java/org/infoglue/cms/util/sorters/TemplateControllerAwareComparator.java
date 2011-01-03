package org.infoglue.cms.util.sorters;

import java.util.Comparator;

/**
 *  
 */
import org.infoglue.deliver.controllers.kernel.impl.simple.TemplateController;

public interface TemplateControllerAwareComparator extends Comparator {

	public void setController(TemplateController controller);
	
}
