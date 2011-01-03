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
package org.infoglue.cms.controllers.kernel.impl.simple;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.velocity.runtime.parser.node.GetExecutor;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.QueryResults;
import org.infoglue.cms.entities.content.impl.simple.SmallContentImpl;
import org.infoglue.cms.entities.kernel.BaseEntityVO;
import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.entities.management.LanguageVO;
import org.infoglue.cms.entities.management.RepositoryVO;
import org.infoglue.cms.exception.SystemException;
import org.infoglue.cms.util.CmsPropertyHandler;

/**
 * 
 */
public class ExtendedSearchController extends BaseController 
{
    private final static Logger logger = Logger.getLogger(ExtendedSearchController.class.getName());

    /**
	 * The singleton controller.
	 */
	private static final ExtendedSearchController instance = new ExtendedSearchController();

	/**
	 * Singleton class; don't allow instantiation.
	 */
	private ExtendedSearchController() 
	{
		super();
	}

	/**
	 * Returns the singleton controller.
	 */
	public static ExtendedSearchController getController()
	{
		return instance;
	}

	/**
	 * @deprecated Use search(ExtendedSearchCriterias)
	 */
	public Set search(final Integer stateId, final ContentTypeDefinitionVO contentTypeDefinitionVO, final LanguageVO languageVO, final CategoryConditions categories) throws SystemException
	{
		final ExtendedSearchCriterias criterias = new ExtendedSearchCriterias(stateId.intValue());
		criterias.setContentTypeDefinitions(contentTypeDefinitionVO);
		criterias.setLanguage(languageVO);
		criterias.setCategoryConditions(categories);
		return search(criterias);
	}

	/**
	 * @deprecated Use search(ExtendedSearchCriterias)
	 */
	public Set search(final Integer stateId, final ContentTypeDefinitionVO contentTypeDefinitionVO, final LanguageVO languageVO, final CategoryConditions categories, final Database db) throws SystemException
	{
		final ExtendedSearchCriterias criterias = new ExtendedSearchCriterias(stateId.intValue());
		criterias.setContentTypeDefinitions(contentTypeDefinitionVO);
		criterias.setLanguage(languageVO);
		criterias.setCategoryConditions(categories);
		return search(criterias, db);
	}
	
	/**
	 * @deprecated Use search(ExtendedSearchCriterias)
	 */
	public Set search(final Integer stateId, final List contentTypeDefinitionVOs, final LanguageVO languageVO, final CategoryConditions categories) throws SystemException
	{
		final ExtendedSearchCriterias criterias = new ExtendedSearchCriterias(stateId.intValue());
		criterias.setContentTypeDefinitions(contentTypeDefinitionVOs);
		criterias.setLanguage(languageVO);
		criterias.setCategoryConditions(categories);
		return search(criterias);
	}

	/**
	 * @deprecated Use search(ExtendedSearchCriterias)
	 */
	public Set search(final Integer stateId, final List contentTypeDefinitionVOs, final LanguageVO languageVO, final CategoryConditions categories, final Database db) throws SystemException
	{
		final ExtendedSearchCriterias criterias = new ExtendedSearchCriterias(stateId.intValue());
		criterias.setContentTypeDefinitions(contentTypeDefinitionVOs);
		criterias.setLanguage(languageVO);
		criterias.setCategoryConditions(categories);
		return search(criterias, db);
	}
	
	/**
	 * @deprecated Use search(ExtendedSearchCriterias)
	 */
	public Set search(final Integer stateId, final ContentTypeDefinitionVO contentTypeDefinitionVO, final LanguageVO languageVO, final CategoryConditions categories, final List xmlAttributes, final String freetext) throws SystemException
	{
		final ExtendedSearchCriterias criterias = new ExtendedSearchCriterias(stateId.intValue());
		criterias.setContentTypeDefinitions(contentTypeDefinitionVO);
		criterias.setLanguage(languageVO);
		criterias.setCategoryConditions(categories);
		criterias.setFreetext(freetext, xmlAttributes);
		return search(criterias);
	}
	
	/**
	 * 
	 */
	public Set search(final ExtendedSearchCriterias criterias) throws SystemException
	{
		final Database db = beginTransaction();
		try
		{
			final Set result = search(criterias, db);
			commitTransaction(db);
			return result;
		}
		catch (Exception e)
		{
			rollbackTransaction(db);
			throw new SystemException(e.getMessage());
		}
	}
	
	/**
	 * 
	 */
	public Set search(final ExtendedSearchCriterias criterias, final Database db) throws SystemException
	{
		if(criterias == null)
			return new HashSet();
		
		try 
		{
			final SqlBuilder sqlBuilder = new SqlBuilder(criterias);
		    if(logger.isDebugEnabled())
		    	logger.debug("sql:" + sqlBuilder.getSQL());
		    //System.out.println("sql:" + sqlBuilder.getSQL());
		    
			final OQLQuery oql = db.getOQLQuery(sqlBuilder.getSQL());
			for(Iterator i=sqlBuilder.getBindings().iterator(); i.hasNext(); )
			{
			    Object o = i.next();
			    if(logger.isDebugEnabled())
			    	logger.debug("o:" + o.toString());
		    	//System.out.println("o:" + o.toString());
			    oql.bind(o);
			}
			
			QueryResults results = oql.execute(Database.ReadOnly);
			Set matchingResults = createResults(results);
			//System.out.println("\n\nmatchingResults:" + matchingResults.size() + "\n\n");
			
			results.close();
			oql.close();

			return matchingResults;
		} 
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SystemException(e.getMessage());
		}
	}

	/**
	 * 
	 */
	private Set createResults(final QueryResults qr) throws PersistenceException, SystemException 
	{
		final Set results = new HashSet();
		while(qr.hasMore())
		{
			results.add(qr.next());
		}
		
		return results;
	}
	
	private static Boolean useFull = null;
    public static boolean useFull()
    {
        if(useFull == null)
        {
            String useShortTableNames = CmsPropertyHandler.getUseShortTableNames();
            if(useShortTableNames == null || !useShortTableNames.equalsIgnoreCase("true"))
            {
                useFull = new Boolean(true);
            }
            else
            {
                useFull = new Boolean(false);
            }
        }
        
        return useFull.booleanValue();
    }

    
	private static Boolean useSQLServerDialect = null;
    public static boolean useSQLServerDialect()
    {
        if(useSQLServerDialect == null)
        {
            String databaseEngine = CmsPropertyHandler.getDatabaseEngine();
            if(databaseEngine == null || (!databaseEngine.equalsIgnoreCase("mssql") && !databaseEngine.equalsIgnoreCase("sql-server") && !databaseEngine.equalsIgnoreCase("SQL Server")))
            {
            	useSQLServerDialect = new Boolean(false);
            }
            else
            {
            	useSQLServerDialect = new Boolean(true);
            }
        }
        
        return useSQLServerDialect.booleanValue();
    }

	/**
	 * Unused. 
	 */
	public BaseEntityVO getNewVO() 
	{ 
		return null; 
	}
}

/**
 * 
 */
class SqlBuilder 
{
	/**
	 * 
	 */
    private final static Logger logger = Logger.getLogger(SqlBuilder.class.getName());
	
	//
	private static final String SELECT_KEYWORD                = "SELECT";
	private static final String FROM_KEYWORD                  = "FROM";
	private static final String WHERE_KEYWORD                 = "WHERE";
	
	private static final String SPACE                         = " ";
	private static final String COMMA                         = ",";
	private static final String AND                           = "AND";
	private static final String OR                            = "OR";
	
	//
	private static final String CONTENT_ALIAS                 = "c";
	private static final String CONTENT_VERSION_ALIAS         = "cv";

	//Here is all the table names used for building the search query.
	private static final String CONTENT_TABLE_SHORT           = "cmCont";
	private static final String CONTENT_TABLE                 = "cmContent";
	private static final String CONTENT_VERSION_TABLE_SHORT   = "cmContVer";
	private static final String CONTENT_VERSION_TABLE         = "cmContentVersion";
	
	//
	private static final String CV_ACTIVE_CLAUSE              = CONTENT_VERSION_ALIAS + ".isActive=1";
	private static final String CV_LANGUAGE_CLAUSE            = CONTENT_VERSION_ALIAS + ".languageId={0}";
	//private static final String CV_STATE_CLAUSE               = CONTENT_VERSION_ALIAS + ".stateId={0}";
	private static final String CV_STATE_CLAUSE               = CONTENT_VERSION_ALIAS + ".stateId>={0}";

	private static final String CV_CONTENT_JOIN_SHORT         = CONTENT_ALIAS + ".ContId=" + CONTENT_VERSION_ALIAS + ".ContId";
	private static final String CV_CONTENT_JOIN               = CONTENT_ALIAS + ".contentId=" + CONTENT_VERSION_ALIAS + ".contentId";
	private static final String CV_LATEST_VERSION_CLAUSE_SHORT						= CONTENT_VERSION_ALIAS + ".ContVerId in (select max(ContVerId) from " + CONTENT_VERSION_TABLE_SHORT + " cv2 where cv2.ContId=" + CONTENT_VERSION_ALIAS + ".ContId AND cv2.languageId={0} AND cv2.stateId>={1} AND cv2.isActive=1)";
	private static final String CV_LATEST_VERSION_CLAUSE      						= CONTENT_VERSION_ALIAS + ".contentVersionId in (select max(contentVersionId) from " + CONTENT_VERSION_TABLE + " cv2 where cv2.contentId=" + CONTENT_VERSION_ALIAS + ".contentId AND cv2.languageId={0} AND cv2.stateId>={1} AND cv2.isActive=1)";
	private static final String CV_LATEST_LANGUAGE_IGNORANT_VERSION_CLAUSE_SHORT	= CONTENT_VERSION_ALIAS + ".ContVerId in (select max(ContVerId) from " + CONTENT_VERSION_TABLE_SHORT + " cv2 where cv2.ContId=" + CONTENT_VERSION_ALIAS + ".ContId AND cv2.stateId>={0} AND cv2.isActive=1)";
	private static final String CV_LATEST_LANGUAGE_IGNORANT_VERSION_CLAUSE      	= CONTENT_VERSION_ALIAS + ".contentVersionId in (select max(contentVersionId) from " + CONTENT_VERSION_TABLE + " cv2 where cv2.contentId=" + CONTENT_VERSION_ALIAS + ".contentId AND cv2.stateId>={0} AND cv2.isActive=1)";
	
	private static final String C_CONTENT_TYPE_CLAUSE_SHORT   = CONTENT_ALIAS + ".contentTypeDefId={0}";
	private static final String C_CONTENT_TYPE_CLAUSE         = CONTENT_ALIAS + ".contentTypeDefinitionId={0}";

	private static final String C_REPOSITORY_CLAUSE_SHORT     = CONTENT_ALIAS + ".repositoryId={0}";
	private static final String C_REPOSITORY_CLAUSE           = CONTENT_ALIAS + ".repositoryId={0}";

	private static final String FREETEXT_EXPRESSION_SHORT     = CONTENT_VERSION_ALIAS + ".VerValue like {0}";
	private static final String FREETEXT_EXPRESSION           = CONTENT_VERSION_ALIAS + ".versionValue like {0}";

	private static final String FROM_DATE_CLAUSE              = CONTENT_ALIAS + ".publishDateTime>={0}";
	private static final String TO_DATE_CLAUSE                = CONTENT_ALIAS + ".publishDateTime<={0}";

	private static final String EXPIRE_FROM_DATE_CLAUSE       = CONTENT_ALIAS + ".expireDateTime>={0}";
	private static final String EXPIRE_TO_DATE_CLAUSE         = CONTENT_ALIAS + ".expireDateTime<={0}";

	private static final String VERSION_MODIFIER_CLAUSE 	  = CONTENT_VERSION_ALIAS + ".versionModifier={0}";
	// BETWEEN DOESN'T SEEMS TO WORK : use FROM_DATE_CLAUSE + TO_DATE_CLAUSE instead
	//private static final String BETWEEN_DATE_CLAUSE           = CONTENT_ALIAS + ".publishDateTime between {0} and {1}";
	
	private static final String FREETEXT_EXPRESSION_VARIABLE  					 = "%<{0}><![CDATA[%{1}%]]></{0}>%";
	private static final String FREETEXT_EXPRESSION_VARIABLE_SQL_SERVER_ESCAPED  = "%<{0}><![[]CDATA[[]%{1}%]]></{0}>%";

	private final ExtendedSearchCriterias criterias;
	
	//
	private String sql;
	private List bindings;
	
	
	/**
	 *
	 */
	public SqlBuilder(final ExtendedSearchCriterias criterias) 
	{
		super();
		this.criterias = criterias;
		this.bindings  = new ArrayList();
		
		logger.debug("===[sql]==============================================================");
		this.sql = generate();
		logger.debug("======================================================================");
		logger.debug(sql);
		logger.debug("===[/sql]=============================================================");
	}

	/**
	 * 
	 */
	public String getSQL() 
	{
		return sql;
	}
	
	/**
	 * 
	 */
	public List getBindings() 
	{
		return bindings;
	}
	
	/**
	 * 
	 */
	private String generate() 
	{
		return "CALL SQL" + SPACE + (ExtendedSearchController.useFull() ? generateSelectClause() : generateSelectClauseShort()) + SPACE + generateFromClause() + SPACE + generateWhereClause() + SPACE + (ExtendedSearchController.useFull() ? generateOrderByClause() : generateOrderByClauseShort()) + generateLimitClause() + SPACE + "AS " + SmallContentImpl.class.getName();
	}
	
	/**
	 * 
	 */
	private String generateSelectClauseShort() 
	{
		return 	SELECT_KEYWORD + SPACE + 
		CONTENT_ALIAS + ".ContId" +
		COMMA + CONTENT_ALIAS + ".name" +
		COMMA + CONTENT_ALIAS + ".publishDateTime" +
		COMMA + CONTENT_ALIAS + ".expireDateTime" +
		COMMA + CONTENT_ALIAS + ".isBranch" +
		COMMA + CONTENT_ALIAS + ".isProtected" +
		COMMA + CONTENT_ALIAS + ".isDeleted" + 
		COMMA + CONTENT_ALIAS + ".creator" + 
		COMMA + CONTENT_ALIAS + ".contentTypeDefId" +
		COMMA + CONTENT_ALIAS + ".repositoryId" +
		COMMA + CONTENT_ALIAS + ".parentContId" +
		COMMA + CONTENT_ALIAS + ".ContId";
	}

	/**
	 * 
	 */
	private String generateSelectClause() 
	{
		return 	SELECT_KEYWORD + SPACE + 
		CONTENT_ALIAS + ".contentId" +
		COMMA + CONTENT_ALIAS + ".name" +
		COMMA + CONTENT_ALIAS + ".publishDateTime" +
		COMMA + CONTENT_ALIAS + ".expireDateTime" +
		COMMA + CONTENT_ALIAS + ".isBranch" +
		COMMA + CONTENT_ALIAS + ".isProtected" +
		COMMA + CONTENT_ALIAS + ".isDeleted" + 
		COMMA + CONTENT_ALIAS + ".creator" + 
		COMMA + CONTENT_ALIAS + ".contentTypeDefinitionId" +
		COMMA + CONTENT_ALIAS + ".repositoryId" +
		COMMA + CONTENT_ALIAS + ".parentContentId" +
		COMMA + CONTENT_ALIAS + ".contentId";
	}

	/**
	 * 
	 */
	private String generateFromClause() 
	{
		final List tables = new ArrayList();
		tables.add(getCONTENT_TABLE() + SPACE + CONTENT_ALIAS);
		tables.add(getCONTENT_VERSION_TABLE() + SPACE + CONTENT_VERSION_ALIAS);
		tables.addAll(getCategoryTables());
		
		return FROM_KEYWORD + SPACE + joinCollection(tables, COMMA);
	}

	/**
	 * 
	 */
	private String generateWhereClause() 
	{
		final List clauses = new ArrayList();
		clauses.addAll(getContentWhereClauses());
		clauses.add(getContentTypeDefinitionWhereClauses());
		if(criterias.getRepositoryIdList() != null && criterias.getRepositoryIdList().size() > 0)
		{
			clauses.add(getContentRepositoryWhereClauses());
		}
		if(criterias.hasFreetextCritera())
		{
			clauses.add(getFreetextWhereClause());
		}
		clauses.addAll(getCategoriesWhereClauses());
		clauses.addAll(getDateWhereClauses());
		clauses.addAll(getExpireDateWhereClauses());
		if(criterias.hasVersionModifierCritera())
		{
			clauses.addAll(getVersionModifierWhereClause());
		}
		return WHERE_KEYWORD + SPACE + joinCollection(clauses, SPACE + AND + SPACE);
	}

	/**
	 * 
	 */
	private List getContentWhereClauses() 
	{
		final List clauses = new ArrayList();

		String mode = CmsPropertyHandler.getOperatingMode();
		if(!criterias.getStateId().toString().equals(mode))
			mode = "" + criterias.getStateId();
		if(criterias.getForcedOperatingMode() != null)
			mode = "" + criterias.getStateId();
		
		clauses.add(CV_ACTIVE_CLAUSE);
		logger.info("skipLanguageCheck:" + criterias.getSkipLanguageCheck());
		if(criterias.getSkipLanguageCheck())
			clauses.add(MessageFormat.format(getCV_LATEST_LANGUAGE_IGNORANT_VERSION_CLAUSE(), new Object[] { mode }));
		else
			clauses.add(MessageFormat.format(getCV_LATEST_VERSION_CLAUSE(), new Object[] { criterias.getLanguage().getId().toString(), mode }));

		clauses.add(getCV_CONTENT_JOIN());
		clauses.add(MessageFormat.format(CV_STATE_CLAUSE, new Object[] { getBindingVariable() }));
		bindings.add(criterias.getStateId());

		if(criterias.hasLanguageCriteria() && !criterias.getSkipLanguageCheck()) 
		{
			logger.info("Adding lang anyway:" + criterias.getSkipLanguageCheck());
			logger.debug(" CRITERA[language]");
			clauses.add(MessageFormat.format(CV_LANGUAGE_CLAUSE, new Object[] { getBindingVariable() }));
			bindings.add(criterias.getLanguage().getId());
		}

		return clauses;
	}

	/**
	 * 
	 */
	private String generateOrderByClauseShort() 
	{
		return "ORDER BY " + CONTENT_ALIAS + ".ContId";
	}

	/**
	 * 
	 */
	private String generateLimitClause() 
	{
		if(criterias.getMaximumNumberOfItems() != null)
		{
			String result = SPACE + " LIMIT " + getBindingVariable();
			bindings.add(criterias.getMaximumNumberOfItems());
			return result;
		}
		else 
			return "";
	}
	 
	/**
	 * 
	 */
	private String generateOrderByClause() 
	{
		return "ORDER BY " + CONTENT_ALIAS + ".contentId";
	}

	
	/**
	 * 
	 */
	private String getContentTypeDefinitionWhereClauses() 
	{
		final List expressions = new ArrayList();
		if(criterias.hasContentTypeDefinitionVOsCriteria())
		{
			logger.debug(" CRITERA[content type definition]");
			for(final Iterator i=criterias.getContentTypeDefinitions().iterator(); i.hasNext(); ) 
			{
				final ContentTypeDefinitionVO contentTypeDefinitionVO = (ContentTypeDefinitionVO) i.next();
				expressions.add(MessageFormat.format(getC_CONTENT_TYPE_CLAUSE(), new Object[] { getBindingVariable() }));
				bindings.add(contentTypeDefinitionVO.getId());
			}
		}
		return "(" + joinCollection(expressions, SPACE + OR + SPACE) + ")";
	}

	/**
	 * 
	 */
	private String getContentRepositoryWhereClauses() 
	{
		final List expressions = new ArrayList();
		if(criterias.getRepositoryIdList() != null && criterias.getRepositoryIdList().size() > 0)
		{
			logger.debug(" CRITERA[repository]");
			for(final Iterator<Integer> i=criterias.getRepositoryIdList().iterator(); i.hasNext(); ) 
			{
				final Integer repositoryId = i.next();
				expressions.add(MessageFormat.format(getC_REPOSITORY_CLAUSE(), new Object[] { getBindingVariable() }));
				bindings.add(repositoryId);
			}
			return "(" + joinCollection(expressions, SPACE + OR + SPACE) + ")";
		}
		else
			return "";
	}

	/**
	 * 
	 */
	private List getCategoriesWhereClauses() 
	{
		final List clauses = new ArrayList();
		if(criterias.hasCategoryConditions())
		{
			logger.debug(" CRITERA[categories]");
			clauses.add(criterias.getCategories().getWhereClauseOQL(bindings));
		}
		return clauses;
	}

	/**
	 * 
	 */
	private List getDateWhereClauses()
	{
		final List clauses = new ArrayList();
		switch(criterias.getDateCriteriaType()) 
		{
			case ExtendedSearchCriterias.FROM_DATE_CRITERIA_TYPE:
				logger.debug(" CRITERA[date : from]");
				clauses.add(MessageFormat.format(FROM_DATE_CLAUSE, new Object[] { getBindingVariable() }));
				bindings.add(criterias.getFromDate());
				break;
			case ExtendedSearchCriterias.TO_DATE_CRITERIA_TYPE:
				logger.debug(" CRITERA[date : to]");
				clauses.add(MessageFormat.format(TO_DATE_CLAUSE, new Object[] { getBindingVariable() }));
				bindings.add(criterias.getToDate());
				break;
			case ExtendedSearchCriterias.BOTH_DATE_CRITERIA_TYPE:
				logger.debug(" CRITERA[date : between]");
				clauses.add(MessageFormat.format(FROM_DATE_CLAUSE, new Object[] { getBindingVariable() }));
				bindings.add(criterias.getFromDate());
				clauses.add(MessageFormat.format(TO_DATE_CLAUSE, new Object[] { getBindingVariable() }));
				bindings.add(criterias.getToDate());
				break;
		}
		return clauses;
	}

	/**
	 * 
	 */
	private List getExpireDateWhereClauses()
	{
		final List clauses = new ArrayList();
		switch(criterias.getExpireDateCriteriaType()) 
		{
			case ExtendedSearchCriterias.EXPIRE_FROM_DATE_CRITERIA_TYPE:
				logger.debug(" CRITERA[expire date : from]");
				clauses.add(MessageFormat.format(EXPIRE_FROM_DATE_CLAUSE, new Object[] { getBindingVariable() }));
				bindings.add(criterias.getExpireFromDate());
				break;
			case ExtendedSearchCriterias.EXPIRE_TO_DATE_CRITERIA_TYPE:
				logger.debug(" CRITERA[expire date : to]");
				clauses.add(MessageFormat.format(EXPIRE_TO_DATE_CLAUSE, new Object[] { getBindingVariable() }));
				bindings.add(criterias.getExpireToDate());
				break;
			case ExtendedSearchCriterias.EXPIRE_BOTH_DATE_CRITERIA_TYPE:
				logger.debug(" CRITERA[expire date : between]");
				clauses.add(MessageFormat.format(EXPIRE_FROM_DATE_CLAUSE, new Object[] { getBindingVariable() }));
				bindings.add(criterias.getExpireFromDate());
				clauses.add(MessageFormat.format(EXPIRE_TO_DATE_CLAUSE, new Object[] { getBindingVariable() }));
				bindings.add(criterias.getExpireToDate());
				break;
		}
		return clauses;
	}

	/**
	 * 
	 */
	private String getFreetextWhereClause() 
	{
		logger.debug(" CRITERA[freetext]");
		final List expressions = new ArrayList();
		if(criterias.hasFreetextCritera())
		{
			for(final Iterator i=criterias.getXmlAttributes().iterator(); i.hasNext(); ) 
			{
				final String xmlAttribute = (String) i.next();
				final String freeTextExpression = MessageFormat.format(getFREETEXT_EXPRESSION(), new Object[] { getBindingVariable() }); 
				final String freeTextVariable   = MessageFormat.format(getFREETEXT_EXPRESSION_VARIABLE(), new Object[] { xmlAttribute, criterias.getFreetext() }); 
			
				bindings.add(freeTextVariable);
				expressions.add(freeTextExpression);
			}
		}
		return "(" + joinCollection(expressions, SPACE + OR + SPACE) + ")";
	}
	
	/**
	 * 
	 */
	private List getVersionModifierWhereClause() 
	{
		final List clauses = new ArrayList();
		logger.debug(" CRITERA[versionModifier]");
		if(criterias.hasVersionModifierCritera())
		{
			clauses.add(MessageFormat.format(VERSION_MODIFIER_CLAUSE, new Object[] { getBindingVariable() }));
			bindings.add(criterias.getVersionModifier());
		}
		
		return clauses;
	}
		
	/**
	 * 
	 */
	private List getCategoryTables() 
	{
		final List tables = new ArrayList();
		if(criterias.hasCategoryConditions())
		{
			tables.addAll(criterias.getCategories().getFromClauseTables());
		}
		return tables;
	}
	
	/**
	 * 
	 */
	private String joinCollection(final Collection collection, final String delimiter) 
	{
		final StringBuffer sb = new StringBuffer();
		for(Iterator i=collection.iterator(); i.hasNext(); ) 
		{
			String element = (String) i.next();
			sb.append(element + (i.hasNext() ? delimiter : ""));
		}
		return sb.toString();
	}

	/**
	 * 
	 */
	private String getBindingVariable() 
	{
		return "$" + (bindings.size() + 1);
	}
		
	public static String getCONTENT_TABLE()
    {
        return (ExtendedSearchController.useFull()) ? CONTENT_TABLE : CONTENT_TABLE_SHORT;
    }
    public static String getCONTENT_VERSION_TABLE()
    {
        return (ExtendedSearchController.useFull()) ? CONTENT_VERSION_TABLE : CONTENT_VERSION_TABLE_SHORT;
    }
    public static String getC_CONTENT_TYPE_CLAUSE()
    {
        return (ExtendedSearchController.useFull()) ? C_CONTENT_TYPE_CLAUSE : C_CONTENT_TYPE_CLAUSE_SHORT;
    }
    public static String getC_REPOSITORY_CLAUSE()
    {
        return (ExtendedSearchController.useFull()) ? C_REPOSITORY_CLAUSE : C_REPOSITORY_CLAUSE_SHORT;
    }
    public static String getCV_CONTENT_JOIN()
    {
        return (ExtendedSearchController.useFull()) ? CV_CONTENT_JOIN : CV_CONTENT_JOIN_SHORT;
    }
    public static String getCV_LATEST_VERSION_CLAUSE()
    {
        return (ExtendedSearchController.useFull()) ? CV_LATEST_VERSION_CLAUSE : CV_LATEST_VERSION_CLAUSE_SHORT;
    }
    public static String getCV_LATEST_LANGUAGE_IGNORANT_VERSION_CLAUSE()
    {
        return (ExtendedSearchController.useFull()) ? CV_LATEST_LANGUAGE_IGNORANT_VERSION_CLAUSE : CV_LATEST_LANGUAGE_IGNORANT_VERSION_CLAUSE_SHORT;
    }
    public static String getFREETEXT_EXPRESSION()
    {
        return (ExtendedSearchController.useFull()) ? FREETEXT_EXPRESSION : FREETEXT_EXPRESSION_SHORT;
    }
    public static String getFREETEXT_EXPRESSION_VARIABLE()
    {
        return (ExtendedSearchController.useSQLServerDialect()) ? FREETEXT_EXPRESSION_VARIABLE_SQL_SERVER_ESCAPED : FREETEXT_EXPRESSION_VARIABLE;
    }
}
