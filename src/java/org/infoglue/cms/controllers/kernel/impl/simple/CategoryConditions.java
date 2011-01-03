package org.infoglue.cms.controllers.kernel.impl.simple;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.transaction.NotSupportedException;

import org.infoglue.cms.entities.management.CategoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;

// TODO: cleanup

/**
 * 
 */
interface ICategoryCondition {
	/**
	 * 
	 */
	String getWhereClauseOQL(final List bindings);
	
	/**
	 * 
	 */
	Collection getFromClauseTables();
	
	/**
	 * 
	 */
	boolean hasCondition();
}

/**
 * 
 */
interface ICategoryContainerCondition extends ICategoryCondition {
	/**
	 * 
	 */
	void add(ICategoryCondition condition);
	
	/**
	 * 
	 */
	void addCategory(final String attributeName, final CategoryVO categoryVO);

	/**
	 * 
	 */
	void addCategory(final String attributeName, final CategoryVO categoryVO, final Boolean notSetArgument, final Boolean isSetArgument);
	
	/**
	 * 
	 */
	ICategoryContainerCondition and();

	/**
	 * 
	 */
	ICategoryContainerCondition or();
}

/**
 * 
 */
abstract class AbstractCategoryCondition implements ICategoryCondition {
	protected static final String LEFT   = "(";
	protected static final String RIGHT  = ")";
	protected static final String SPACE  = " ";
	protected static final String COMMA  = ",";
	protected static final String AND    = "AND";
	protected static final String OR     = "OR";
	
	private static final String CATEGORY_ALIAS_PREFIX         = "cat";
	private static final String CONTENT_CATEGORY_ALIAS_PREFIX = "ccat";
	private static final String CONTENT_VERSION_ALIAS         = "cv";

	private static final String CATEGORY_TABLE                = "cmCategory";
	private static final String CONTENT_CATEGORY_TABLE        = "cmContentCategory";

	//OLD WAY
	protected static final String ONE_CATEGORY_CLAUSE 		  = SPACE + LEFT + CATEGORY_ALIAS_PREFIX + "{0}.categoryId={1} " + AND + SPACE + CONTENT_CATEGORY_ALIAS_PREFIX + "{0}.attributeName={2}" + RIGHT + SPACE;
	protected static final String CATEGORY_CLAUSE   		  = "(" + CATEGORY_ALIAS_PREFIX + "{0}.active=1 " + AND + " {1} " + AND + SPACE + CONTENT_CATEGORY_ALIAS_PREFIX + "{0}.categoryId = " + CATEGORY_ALIAS_PREFIX + "{0}.categoryId  " + AND + SPACE + CONTENT_CATEGORY_ALIAS_PREFIX + "{0}.ContentVersionId=" + CONTENT_VERSION_ALIAS + ".ContentVersionId)";
	protected static final String CATEGORY_CLAUSE_SHORT		  = "(" + CATEGORY_ALIAS_PREFIX + "{0}.active=1 " + AND + " {1} " + AND + SPACE + CONTENT_CATEGORY_ALIAS_PREFIX + "{0}.categoryId = " + CATEGORY_ALIAS_PREFIX + "{0}.categoryId  " + AND + SPACE + CONTENT_CATEGORY_ALIAS_PREFIX + "{0}.ContVerId=" + CONTENT_VERSION_ALIAS + ".ContVerId)";

	//NEW WAY						
	protected static final String CATEGORY_CLAUSE_GENERAL   	= "(" + CONTENT_VERSION_ALIAS + ".contentVersionId IN (SELECT contentVersionId from " + CONTENT_CATEGORY_TABLE + " WHERE categoryId={0} AND attributeName={1}))";
	protected static final String CATEGORY_CLAUSE_GENERAL_SHORT = "(" + CONTENT_VERSION_ALIAS + ".contVerId IN (SELECT contVerId from " + CONTENT_CATEGORY_TABLE + " WHERE categoryId={0} AND attributeName={1}))";

	protected static final String CATEGORY_NOT_SET_CLAUSE_GENERAL   	= "(" + CONTENT_VERSION_ALIAS + ".contentVersionId NOT IN (SELECT contentVersionId from " + CONTENT_CATEGORY_TABLE + " WHERE attributeName={0}))";
	protected static final String CATEGORY_NOT_SET_CLAUSE_GENERAL_SHORT = "(" + CONTENT_VERSION_ALIAS + ".contVerId NOT IN (SELECT contVerId from " + CONTENT_CATEGORY_TABLE + " WHERE attributeName={0}))";

	protected static final String CATEGORY_IS_SET_CLAUSE_GENERAL   	= "(" + CONTENT_VERSION_ALIAS + ".contentVersionId IN (SELECT contentVersionId from " + CONTENT_CATEGORY_TABLE + " WHERE attributeName={0}))";
	protected static final String CATEGORY_IS_SET_CLAUSE_GENERAL_SHORT = "(" + CONTENT_VERSION_ALIAS + ".contVerId IN (SELECT contVerId from " + CONTENT_CATEGORY_TABLE + " WHERE attributeName={0}))";

	/**
	 * 
	 */
	private static int counter;
	
	/**
	 * 
	 */
	private Integer uniqueID;
	
	

	
	/**
	 * 
	 */
	private synchronized Integer createUniqueId() {
		return new Integer(counter++);
	}
	
	/**
	 * 
	 */
	AbstractCategoryCondition() {
		this.uniqueID = createUniqueId(); 
	}

	/**
	 * 
	 */
	protected String getUniqueID() {
		return uniqueID == null ? "" : uniqueID.toString();
	}
	
	/**
	 * 
	 */
	protected String getBindingVariable(final Collection bindings) {
		return "$" + (bindings.size() + 1);
	}

	/**
	 * 
	 */
	public Collection getFromClauseTables() {
		final Collection result = new ArrayList();
		String useImprovedContentCategorySearch = CmsPropertyHandler.getUseImprovedContentCategorySearch();
		if(useImprovedContentCategorySearch != null && useImprovedContentCategorySearch.equalsIgnoreCase("false"))
		{
			result.add(CATEGORY_TABLE + SPACE + CATEGORY_ALIAS_PREFIX + uniqueID);
			result.add(CONTENT_CATEGORY_TABLE + SPACE + CONTENT_CATEGORY_ALIAS_PREFIX + uniqueID);
		}
		
		return result;
	}

	/**
	 * 
	 */
	protected String getOneCategoryClause(final String attributeName, final CategoryVO categoryVO, final List bindings) 
	{
		final String categoryVariable = getBindingVariable(bindings);
		bindings.add(categoryVO.getId());
		final String nameVariable = getBindingVariable(bindings);
		bindings.add(attributeName);
		
		String useImprovedContentCategorySearch = CmsPropertyHandler.getUseImprovedContentCategorySearch();
		if(useImprovedContentCategorySearch != null && useImprovedContentCategorySearch.equalsIgnoreCase("false"))
		{
			return MessageFormat.format(ONE_CATEGORY_CLAUSE, new Object[] { getUniqueID(), categoryVariable, nameVariable });
		}
		else
		{
			String categoryClause = ExtendedSearchController.useFull() ? CATEGORY_CLAUSE_GENERAL : CATEGORY_CLAUSE_GENERAL_SHORT;
			return MessageFormat.format(categoryClause, new Object[] { categoryVariable, nameVariable });
		}
	}

	/**
	 * 
	 */
	protected String getOneExcludingCategoryClause(final String attributeName, final List bindings) throws NotSupportedException
	{
		final String nameVariable = getBindingVariable(bindings);
		bindings.add(attributeName);
		
		String useImprovedContentCategorySearch = CmsPropertyHandler.getUseImprovedContentCategorySearch();
		if(useImprovedContentCategorySearch != null && useImprovedContentCategorySearch.equalsIgnoreCase("false"))
		{
			throw new NotSupportedException("Not in searches are not supported in the old category search - use the new one (application settings).");
		}
		else
		{
			String categoryClause = ExtendedSearchController.useFull() ? CATEGORY_NOT_SET_CLAUSE_GENERAL : CATEGORY_NOT_SET_CLAUSE_GENERAL_SHORT;
			return MessageFormat.format(categoryClause, new Object[] { nameVariable });
		}
	}

	/**
	 * 
	 */
	public boolean hasCondition() { return true; }
}

/**
 * 
 */
class CategoryAndCondition extends AbstractCategoryCondition 
{
	/**
	 * 
	 */
	private String attributeName;
	
	/**
	 * 
	 */
	private CategoryVO categoryVO;
	
	private Boolean notSetArgument = false;
	private Boolean isSetArgument = false;
	
	/**
	 * 
	 */
	CategoryAndCondition(final String attributeName, final CategoryVO categoryVO, Boolean notSetArgument, Boolean isSetArgument) 
	{
		this.attributeName 	= attributeName;
		this.categoryVO    	= categoryVO;
		this.notSetArgument = notSetArgument;
		this.isSetArgument 	= isSetArgument;
	}
	
	/**
	 * 
	 */
	public String getWhereClauseOQL(final List bindings) 
	{
		String useImprovedContentCategorySearch = CmsPropertyHandler.getUseImprovedContentCategorySearch();
		if(useImprovedContentCategorySearch != null && useImprovedContentCategorySearch.equalsIgnoreCase("false"))
		{
			final String categoryClause = getOneCategoryClause(attributeName, categoryVO, bindings);
			return MessageFormat.format(getCATEGORY_CLAUSE(), new Object[] { getUniqueID(), categoryClause });
		}
		else
		{
			if(notSetArgument)
			{
				final String nameVariable = getBindingVariable(bindings);
				bindings.add(attributeName);
			
				return MessageFormat.format(getCATEGORY_NOT_SET_CLAUSE(), new Object[] { nameVariable });				
			}
			else if(isSetArgument)
			{
				final String nameVariable = getBindingVariable(bindings);
				bindings.add(attributeName);
			
				return MessageFormat.format(getCATEGORY_IS_SET_CLAUSE(), new Object[] { nameVariable });								
			}
			else
			{
				final String categoryVariable = getBindingVariable(bindings);
				bindings.add(categoryVO.getId());
				final String nameVariable = getBindingVariable(bindings);
				bindings.add(attributeName);
			
				return MessageFormat.format(getCATEGORY_CLAUSE(), new Object[] { categoryVariable, nameVariable });
			}
		}
	}
	
    public static String getCATEGORY_CLAUSE()
    {
		String useImprovedContentCategorySearch = CmsPropertyHandler.getUseImprovedContentCategorySearch();
		if(useImprovedContentCategorySearch != null && useImprovedContentCategorySearch.equalsIgnoreCase("false"))
        	return (ExtendedSearchController.useFull()) ? CATEGORY_CLAUSE : CATEGORY_CLAUSE_SHORT;
        else
			return ExtendedSearchController.useFull() ? CATEGORY_CLAUSE_GENERAL : CATEGORY_CLAUSE_GENERAL_SHORT;
    }

    public static String getCATEGORY_NOT_SET_CLAUSE() throws RuntimeException
    {
		String useImprovedContentCategorySearch = CmsPropertyHandler.getUseImprovedContentCategorySearch();
		if(useImprovedContentCategorySearch != null && useImprovedContentCategorySearch.equalsIgnoreCase("false"))
			throw new RuntimeException("Not in searches are not supported in the old category search - use the new one (application settings).");
        else
			return ExtendedSearchController.useFull() ? CATEGORY_NOT_SET_CLAUSE_GENERAL : CATEGORY_NOT_SET_CLAUSE_GENERAL_SHORT;
    }

    public static String getCATEGORY_IS_SET_CLAUSE() throws RuntimeException
    {
		String useImprovedContentCategorySearch = CmsPropertyHandler.getUseImprovedContentCategorySearch();
		if(useImprovedContentCategorySearch != null && useImprovedContentCategorySearch.equalsIgnoreCase("false"))
			throw new RuntimeException("In searches are not supported in the old category search - use the new one (application settings).");
        else
			return ExtendedSearchController.useFull() ? CATEGORY_IS_SET_CLAUSE_GENERAL : CATEGORY_IS_SET_CLAUSE_GENERAL_SHORT;
    }

}

/**
 * 
 */
class CategoryOrCondition extends AbstractCategoryCondition {
	/**
	 * 
	 */
	private List names = new ArrayList();

	/**
	 * 
	 */
	private List categories = new ArrayList();
	
	
	/**
	 * 
	 */
	CategoryOrCondition(final String attributeName, final CategoryVO categoryVO) {
		addCategory(attributeName, categoryVO);
	}
	
	/**
	 * 
	 */
	void addCategory(final String attributeName, final CategoryVO categoryVO) {
		names.add(attributeName);
		categories.add(categoryVO);
	}
	
	/**
	 * 
	 */
	public String getWhereClauseOQL(final List bindings) {
		final StringBuffer categoryClauses = new StringBuffer();
		for(int i=0; i<names.size(); ++i) {
			final String attributeName  = (String) names.get(i);
			final CategoryVO categoryVO = (CategoryVO) categories.get(i); 
			
			if(i > 0)
				categoryClauses.append(SPACE + OR + SPACE);
			categoryClauses.append(getOneCategoryClause(attributeName, categoryVO, bindings));
		}
		
		String useImprovedContentCategorySearch = CmsPropertyHandler.getUseImprovedContentCategorySearch();
		if(useImprovedContentCategorySearch != null && useImprovedContentCategorySearch.equalsIgnoreCase("false"))
		{
			return MessageFormat.format(CategoryAndCondition.getCATEGORY_CLAUSE(), new Object[] { getUniqueID(), LEFT + categoryClauses.toString() + RIGHT });
		}
		else
		{	
			String apa2 = LEFT + categoryClauses.toString() + RIGHT;
			return apa2;
		}
	}
}

/**
 * 
 */
public class CategoryConditions implements ICategoryContainerCondition {
	private static final String LEFT   = "(";
	private static final String RIGHT  = ")";
	private static final String SPACE  = " ";
	private static final String AND    = "AND";
	private static final String OR     = "OR";

	/**
	 * 
	 */
	private List children = new ArrayList();
	
	/**
	 * 
	 */
	private String delimiter;
	
	
	
	/**
	 * 
	 */
	protected CategoryConditions(final String delimiter) {
		this.delimiter = delimiter;
	}
	
	/**
	 * 
	 */
	public void add(final ICategoryCondition condition) {
		if(condition != null)
			children.add(condition);
	}

	/**
	 * 
	 */
	public void addCategory(final String attributeName, final CategoryVO categoryVO) {
		children.add(new CategoryAndCondition(attributeName, categoryVO, false, false));
	}

	/**
	 * 
	 */
	public void addCategory(final String attributeName, final CategoryVO categoryVO, final Boolean notSetArgument, final Boolean isSetArgument) {
		children.add(new CategoryAndCondition(attributeName, categoryVO, notSetArgument, isSetArgument));
	}
	
	/**
	 * 
	 */
	public ICategoryContainerCondition and() {
		final ICategoryContainerCondition container = createAndConditions();
		add(container);
		return container;
	}

	/**
	 * 
	 */
	public ICategoryContainerCondition or() {
		final ICategoryContainerCondition container = createOrConditions();
		add(container);
		return container;
	}

	/**
	 * 
	 */
	public static CategoryConditions createAndConditions() { return new CategoryAndConditions(); }
	
	/**
	 * 
	 */
	public static CategoryConditions createOrConditions() { return new CategoryOrConditions(); }
	
	/**
	 * 
	 */
	public static CategoryConditions parse(final String s) { return new ConditionsParser().parse(s); }
	
	/**
	 * 
	 */
	public String getWhereClauseOQL(final List bindings) {
		final StringBuffer sb = new StringBuffer();
		int counter = 0;
		for(Iterator i=children.iterator(); i.hasNext(); ) {
			ICategoryCondition condition = (ICategoryCondition) i.next();
			if(condition.hasCondition()) {
				if(counter++ > 0)
					sb.append(SPACE + delimiter + SPACE);
				sb.append(condition.getWhereClauseOQL(bindings));
			}
		}
		return (counter > 1) ? (LEFT + sb.toString() + RIGHT) : sb.toString();
	}
	
	/**
	 * 
	 */
	public Collection getFromClauseTables() {
		final List result = new ArrayList();
		for(Iterator i=children.iterator(); i.hasNext(); ) {
			ICategoryCondition condition = (ICategoryCondition) i.next();
		
			String useImprovedContentCategorySearch = CmsPropertyHandler.getUseImprovedContentCategorySearch();
			if(useImprovedContentCategorySearch != null && useImprovedContentCategorySearch.equalsIgnoreCase("false"))
				result.addAll(condition.getFromClauseTables());
		}
		return result;
	}

	/**
	 * 
	 */
	public boolean hasCondition() { 
		for(Iterator i=children.iterator(); i.hasNext(); ) {
			ICategoryCondition condition = (ICategoryCondition) i.next();
			if(condition.hasCondition())
				return true;
		}
		return false;
	}
}

/**
 * 
 */
class CategoryAndConditions extends CategoryConditions {
	/**
	 * 
	 */
	CategoryAndConditions() {
		super("AND");
	}
}

/**
 * 
 */
class CategoryOrConditions extends CategoryConditions {
	/**
	 * 
	 */
	private CategoryOrCondition compound;
	
	/**
	 * 
	 */
	CategoryOrConditions() {
		super("OR");
	}

	/**
	 * 
	 */
	public void addCategory(final String attributeName, final CategoryVO categoryVO) {
		if(compound == null) {
			compound = new CategoryOrCondition(attributeName, categoryVO);
			super.add(compound);
		}
		else
			compound.addCategory(attributeName, categoryVO);
	}
}

/**
 * 
 */
class ConditionsParser {
	private static final String AND_START           = "{";
	private static final String AND_END             = "}";
	private static final String OR_START            = "[";
	private static final String OR_END              = "]";
	private static final String CONDITION_DELIMITER = ",";
	private static final String CATEGORY_DELIMITER  = "=";
	
	
	/**
	 * 
	 */
	ConditionsParser() {}
	
	/**
	 * 
	 */
	public CategoryConditions parse(final String s) {
		final String parseString = (s == null ? "" : s);
		final StringTokenizer st = new StringTokenizer(AND_START + parseString + AND_END, AND_START + AND_END + OR_START + OR_END + CONDITION_DELIMITER, true);
		final List tokens = tokensToList(st);
		
		final CategoryConditions conditions = createContainer(tokens);
		parse(conditions, tokens);
		return conditions;
	}
	
	/**
	 * 
	 */
	private void parse(CategoryConditions conditions, final List tokens) {
		if(tokens.isEmpty() || isContainerEndToken(tokens))
			return;
		if(isContainerStartToken(tokens))
			parseContainer(conditions, tokens);
		else if(isConditionDelimiterToken(tokens))
			parseConditionDelimiter(conditions, tokens);
		else
			parseCategory(conditions, tokens);
		
		parse(conditions, tokens);
	}
	
	/**
	 * 
	 */
	private void parseContainer(CategoryConditions conditions, final List tokens) {
		final CategoryConditions newConditions = createContainer(tokens);
		
		final String startToken = (String) tokens.remove(0);
		parse(newConditions, tokens);
		matchContainerTokens(startToken, tokens);
		conditions.add(newConditions);
	}
	
	/**
	 * 
	 */
	private void parseConditionDelimiter(CategoryConditions conditions, final List tokens) {
		if(!conditions.hasCondition())
			throw new IllegalArgumentException("ConditionsParser.parseConditionDelimiter() - empty condition.");
		tokens.remove(0);
	}
	
	/**
	 * 
	 */
	private void parseCategory(CategoryConditions conditions, final List tokens) {
		final String token = (String) tokens.remove(0);
		final List terms = tokensToList(new StringTokenizer(token, CATEGORY_DELIMITER, true));
		if(terms.size() != 3)
			throw new IllegalArgumentException("ConditionsParser.parseCategory() - illegal category syntax.");
		
		final String attributeName  	= (String) terms.get(0);
		final String path           	= (String) terms.get(2);
		final Boolean isNotSetArgument 	= (path.equalsIgnoreCase("UNDEFINED") ? true : false);
		final Boolean isSetArgument 	= (path.equalsIgnoreCase("*") ? true : false);
		
		try 
		{
			CategoryVO categoryVO = null;
			if(!isNotSetArgument && !isSetArgument)
			{
				categoryVO = CategoryController.getController().findByPath(path); 
				if(categoryVO == null)
					throw new IllegalArgumentException("ConditionsParser.parseCategory() - no such category [" + path + "].");
			}
			conditions.addCategory(attributeName, categoryVO, isNotSetArgument, isSetArgument);
		} 
		catch(SystemException e) 
		{
			//e.printStackTrace();
			throw new IllegalArgumentException("ConditionsParser.parseCategory() - unknown category path [" + path + "].");
		}
	}
	
	/**
	 * 
	 */
	private CategoryConditions createContainer(final List tokens) {
		if(tokens.size() < 2)
			throw new IllegalArgumentException("ConditionsParser.createContainer() - no trailing container delimiter.");

		final String startToken = (String) tokens.get(0);
		final String endToken   = (String) tokens.get(tokens.size() - 1);

		if(AND_START.equals(startToken))
			return CategoryConditions.createAndConditions();
		if(OR_START.equals(startToken))
			return CategoryConditions.createOrConditions();
		
		throw new IllegalArgumentException("ConditionsParser.createContainer() - illegal state.");
	}

	/**
	 * 
	 */
	private boolean isContainerStartToken(final List tokens) {
		if(tokens.isEmpty())
			return false;
		final String token = (String) tokens.get(0);
		return AND_START.equals(token) || OR_START.equals(token);
	}

	/**
	 * 
	 */
	private boolean isContainerEndToken(final List tokens) {
		if(tokens.isEmpty())
			return false;
		final String token = (String) tokens.get(0);
		return AND_END.equals(token) || OR_END.equals(token);
	}
	
	/**
	 * 
	 */
	private boolean isConditionDelimiterToken(final List tokens) {
		if(tokens.isEmpty())
			return false;
		final String token = (String) tokens.get(0);
		return CONDITION_DELIMITER.equals(token);
	}
	
	/**
	 * 
	 */
	private void matchContainerTokens(final String startToken, final List tokens) {
		if(tokens.isEmpty())
			throw new IllegalArgumentException("ConditionsParser.matchContainerTokens() - no closing container token.");
		final String endToken = (String) tokens.remove(0);
		if(startToken.equals(AND_START) && !endToken.equals(AND_END))
			throw new IllegalArgumentException("ConditionsParser.matchContainerTokens() - no matching closing container token.");
		if(startToken.equals(OR_START) && !endToken.equals(OR_END))
			throw new IllegalArgumentException("ConditionsParser.matchContainerTokens() - no matching closing container token.");
	}
	
	/**
	 * 
	 */
	private List tokensToList(final StringTokenizer st) {
		final List result = new ArrayList();
		while(st.hasMoreElements())
			result.add(st.nextElement());
		return result;
	}
}