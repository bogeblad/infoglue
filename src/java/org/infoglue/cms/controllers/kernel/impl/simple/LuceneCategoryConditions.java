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
interface ILuceneCategoryCondition {
	/**
	 * 
	 */
	String getWhereClause();
	
	/**
	 * 
	 */
	boolean hasCondition();
}

/**
 * 
 */
interface ILuceneCategoryContainerCondition extends ILuceneCategoryCondition {
	/**
	 * 
	 */
	void add(ILuceneCategoryCondition condition);
	
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
	ILuceneCategoryContainerCondition and();

	/**
	 * 
	 */
	ILuceneCategoryContainerCondition or();
}

/**
 * 
 */
abstract class AbstractLuceneCategoryCondition implements ILuceneCategoryCondition {
	protected static final String LEFT   = "(";
	protected static final String RIGHT  = ")";
	protected static final String SPACE  = " ";
	protected static final String COMMA  = ",";
	protected static final String AND    = "AND";
	protected static final String OR     = "OR";
	
	//NEW WAY	
	//protected static final String CATEGORY_CANBE_SET_CLAUSE_GENERAL   	= "+{1}EQ{0}";
	protected static final String CATEGORY_CANBE_SET_CLAUSE_GENERAL   	= "{1}EQ{0}";
	protected static final String CATEGORY_IS_SET_CLAUSE_GENERAL   	= "+{0}EQ*";
	protected static final String CATEGORY_IS_NOT_SET_CLAUSE_GENERAL   = "-{0}EQ*";
	
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
	AbstractLuceneCategoryCondition() {
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
	protected String getOneCategoryClause(final String attributeName, final CategoryVO categoryVO) 
	{		
		String categoryClause = CATEGORY_CANBE_SET_CLAUSE_GENERAL;
		return MessageFormat.format(categoryClause, new Object[] { "" + categoryVO.getId(), attributeName.toLowerCase().replaceAll(" ", "_") });
	}

	/**
	 * 
	 */
	protected String getOneExcludingCategoryClause(final String attributeName, final CategoryVO categoryVO) throws NotSupportedException
	{
		String categoryClause = CATEGORY_IS_NOT_SET_CLAUSE_GENERAL;
		return MessageFormat.format(categoryClause, new Object[] { attributeName.toLowerCase().replaceAll(" ", "_") });
	}

	/**
	 * 
	 */
	public boolean hasCondition() { return true; }
}

/**
 * 
 */
class LuceneCategoryAndCondition extends AbstractLuceneCategoryCondition 
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
	LuceneCategoryAndCondition(final String attributeName, final CategoryVO categoryVO, Boolean notSetArgument, Boolean isSetArgument) 
	{
		this.attributeName 	= attributeName;
		this.categoryVO    	= categoryVO;
		this.notSetArgument = notSetArgument;
		this.isSetArgument 	= isSetArgument;
	}
	
	/**
	 * 
	 */
	public String getWhereClause() 
	{
		if(notSetArgument)
		{
			return MessageFormat.format(CATEGORY_IS_NOT_SET_CLAUSE_GENERAL, new Object[] { attributeName.toLowerCase().replaceAll(" ", "_") });				
		}
		else if(isSetArgument)
		{
			return MessageFormat.format(CATEGORY_IS_SET_CLAUSE_GENERAL, new Object[] { attributeName.toLowerCase().replaceAll(" ", "_") });								
		}
		else
		{		
			return MessageFormat.format(CATEGORY_CANBE_SET_CLAUSE_GENERAL, new Object[] { "" + categoryVO.getId(), attributeName.toLowerCase().replaceAll(" ", "_") });
		}
	}
	

}

/**
 * 
 */
class LuceneCategoryOrCondition extends AbstractLuceneCategoryCondition {
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
	LuceneCategoryOrCondition(final String attributeName, final CategoryVO categoryVO) {
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
	public String getWhereClause() {
		final StringBuffer categoryClauses = new StringBuffer();
		for(int i=0; i<names.size(); ++i) {
			final String attributeName  = (String) names.get(i);
			final CategoryVO categoryVO = (CategoryVO) categories.get(i); 
			
			if(i > 0)
				categoryClauses.append(SPACE + OR + SPACE);
			categoryClauses.append(getOneCategoryClause(attributeName, categoryVO));
		}
		
		return MessageFormat.format(LuceneCategoryAndCondition.CATEGORY_CANBE_SET_CLAUSE_GENERAL, new Object[] { getUniqueID(), LEFT + categoryClauses.toString() + RIGHT });
	}
}

/**
 * 
 */
public class LuceneCategoryConditions implements ILuceneCategoryContainerCondition {
	private static final String LEFT   = "(";
	private static final String RIGHT  = ")";
	private static final String SPACE  = " ";

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
	protected LuceneCategoryConditions(final String delimiter) {
		this.delimiter = delimiter;
	}
	
	/**
	 * 
	 */
	public void add(final ILuceneCategoryCondition condition) {
		if(condition != null)
			children.add(condition);
	}

	/**
	 * 
	 */
	public void addCategory(final String attributeName, final CategoryVO categoryVO) {
		children.add(new LuceneCategoryAndCondition(attributeName, categoryVO, false, false));
	}

	/**
	 * 
	 */
	public void addCategory(final String attributeName, final CategoryVO categoryVO, final Boolean notSetArgument, final Boolean isSetArgument) {
		children.add(new LuceneCategoryAndCondition(attributeName, categoryVO, notSetArgument, isSetArgument));
	}
	
	/**
	 * 
	 */
	public ILuceneCategoryContainerCondition and() {
		final ILuceneCategoryContainerCondition container = createAndConditions();
		add(container);
		return container;
	}

	/**
	 * 
	 */
	public ILuceneCategoryContainerCondition or() {
		final ILuceneCategoryContainerCondition container = createOrConditions();
		add(container);
		return container;
	}

	/**
	 * 
	 */
	public static LuceneCategoryConditions createAndConditions() { return new LuceneCategoryAndConditions(); }
	
	/**
	 * 
	 */
	public static LuceneCategoryConditions createOrConditions() { return new LuceneCategoryOrConditions(); }
	
	/**
	 * 
	 */
	public static String parse(final String s) { return new LuceneConditionsParser().parse(s); }
	
	/**
	 * 
	 */
	public String getWhereClause() {
		final StringBuffer sb = new StringBuffer();
		int counter = 0;
		for(Iterator i=children.iterator(); i.hasNext(); ) {
			ILuceneCategoryCondition condition = (ILuceneCategoryCondition) i.next();
			if(condition.hasCondition()) {
				if(counter++ > 0)
					sb.append(SPACE + delimiter + SPACE);
				sb.append(condition.getWhereClause());
			}
		}
		return (counter > 1) ? (LEFT + sb.toString() + RIGHT) : sb.toString();
	}
	
	/**
	 * 
	 */
	public boolean hasCondition() { 
		for(Iterator i=children.iterator(); i.hasNext(); ) {
			ILuceneCategoryCondition condition = (ILuceneCategoryCondition) i.next();
			if(condition.hasCondition())
				return true;
		}
		return false;
	}
}

/**
 * 
 */
class LuceneCategoryAndConditions extends LuceneCategoryConditions {
	/**
	 * 
	 */
	LuceneCategoryAndConditions() {
		super("AND");
	}
}

/**
 * 
 */
class LuceneCategoryOrConditions extends LuceneCategoryConditions {
	/**
	 * 
	 */
	private LuceneCategoryOrCondition compound;
	
	/**
	 * 
	 */
	LuceneCategoryOrConditions() {
		super("OR");
	}

	/**
	 * 
	 */
	public void addCategory(final String attributeName, final CategoryVO categoryVO) {
		if(compound == null) {
			compound = new LuceneCategoryOrCondition(attributeName, categoryVO);
			super.add(compound);
		}
		else
			compound.addCategory(attributeName, categoryVO);
	}
}

/**
 * 
 */
class LuceneConditionsParser {
	private static final String AND_START           = "{";
	private static final String AND_END             = "}";
	private static final String OR_START            = "[";
	private static final String OR_END              = "]";
	private static final String CONDITION_DELIMITER = ",";
	private static final String CATEGORY_DELIMITER  = "=";
	
	
	/**
	 * 
	 */
	LuceneConditionsParser() {}
	
	/**
	 * 
	 */
	public String parse(final String s) 
	{
		final String parseString = (s == null ? "" : s);
		final StringTokenizer st = new StringTokenizer(AND_START + parseString + AND_END, AND_START + AND_END + OR_START + OR_END + CONDITION_DELIMITER, true);
		final List<String> tokens = tokensToList(st);
		
		final LuceneCategoryConditions conditions = createContainer(tokens);
		parse(conditions, tokens);
		
		return conditions.getWhereClause();
	}
	
	/**
	 * 
	 */
	private void parse(LuceneCategoryConditions conditions, final List tokens) {
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
	private void parseContainer(LuceneCategoryConditions conditions, final List tokens) {
		final LuceneCategoryConditions newConditions = createContainer(tokens);
		
		final String startToken = (String) tokens.remove(0);
		parse(newConditions, tokens);
		matchContainerTokens(startToken, tokens);
		conditions.add(newConditions);
	}
	
	/**
	 * 
	 */
	private void parseConditionDelimiter(LuceneCategoryConditions conditions, final List tokens) {
		if(!conditions.hasCondition())
			throw new IllegalArgumentException("ConditionsParser.parseConditionDelimiter() - empty condition.");
		tokens.remove(0);
	}
	
	/**
	 * 
	 */
	private void parseCategory(LuceneCategoryConditions conditions, final List tokens) {
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
	private LuceneCategoryConditions createContainer(final List tokens) {
		if(tokens.size() < 2)
			throw new IllegalArgumentException("ConditionsParser.createContainer() - no trailing container delimiter.");

		final String startToken = (String) tokens.get(0);

		if(AND_START.equals(startToken))
			return LuceneCategoryConditions.createAndConditions();
		if(OR_START.equals(startToken))
			return LuceneCategoryConditions.createOrConditions();
		
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
	private List<String> tokensToList(final StringTokenizer st) {
		final List<String> result = new ArrayList<String>();
		while(st.hasMoreElements())
			result.add((String)st.nextElement());
		return result;
	}
}