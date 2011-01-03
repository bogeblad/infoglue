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

package org.infoglue.cms.util.workflow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.query.WorkflowQuery;
import com.opensymphony.workflow.spi.SimpleStep;
import com.opensymphony.workflow.spi.SimpleWorkflowEntry;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.jdbc.JDBCWorkflowStore;


/**
 * JDBC implementation just overiding the init-method of the default JDBCWorkflowStore as it demanded DataStores.
 *
 * The following properties are all <b>required</b>:
 * <ul>
 *  <li><b>datasource</b> - the JNDI location for the DataSource that is to be used.</li>
 *  <li><b>entry.sequence</b> - SQL query that returns the next ID for a workflow entry</li>
 *  <li><b>entry.table</b> - table name for workflow entry</li>
 *  <li><b>entry.id</b> - column name for workflow entry ID field</li>
 *  <li><b>entry.name</b> - column name for workflow entry name field</li>
 *  <li><b>entry.state</b> - column name for workflow entry state field</li>
 *  <li><b>step.sequence</b> - SQL query that returns the next ID for a workflow step</li>
 *  <li><b>history.table</b> - table name for steps in history</li>
 *  <li><b>current.table</b> - table name for current steps</li>
 *  <li><b>step.id</b> - column name for step ID field</li>
 *  <li><b>step.entryId</b> - column name for workflow entry ID field (foreign key relationship to [entry.table].[entry.id])</li>
 *  <li><b>step.stepId</b> - column name for step workflow definition step field</li>
 *  <li><b>step.actionId</b> - column name for step action field</li>
 *  <li><b>step.owner</b> - column name for step owner field</li>
 *  <li><b>step.caller</b> - column name for step caller field</li>
 *  <li><b>step.startDate</b> - column name for step start date field</li>
 *  <li><b>step.dueDate</b> - column name for optional step due date field</li>
 *  <li><b>step.finishDate</b> - column name for step finish date field</li>
 *  <li><b>step.status</b> - column name for step status field</li>
 *  <li><b>currentPrev.table</b> - table name for the previous IDs for current steps</li>
 *  <li><b>historyPrev.table</b> - table name for the previous IDs for history steps</li>
 *  <li><b>step.previousId</b> - column name for step ID field (foreign key relation to [history.table].[step.id] or [current.table].[step.id])</li>
 * </ul>
 *
 * @author Mattias Bogeblad
 */
public class InfoGlueJDBCWorkflowStore extends JDBCWorkflowStore 
{
    private final static Logger logger = Logger.getLogger(InfoGlueJDBCWorkflowStore.class.getName());

    //~ Instance fields ////////////////////////////////////////////////////////
    /*
    protected DataSource ds;
    protected String currentPrevTable;
    protected String currentTable;
    protected String entryId;
    protected String entryName;
    protected String entrySequence;
    protected String entryState;
    protected String entryTable;
    protected String historyPrevTable;
    protected String historyTable;
    protected String stepActionId;
    protected String stepCaller;
    protected String stepDueDate;
    protected String stepEntryId;
    protected String stepFinishDate;
    protected String stepId;
    protected String stepOwner;
    protected String stepPreviousId;
    protected String stepSequence;
    protected String stepStartDate;
    protected String stepStatus;
    protected String stepStepId;
    protected boolean closeConnWhenDone = false;
    */
    private String userName;
    private String password;
    private String driverClassName;
    private String url;
    
    public void init(Map props) throws StoreException 
    {
        super.init(props);
        
        userName = getInitProperty(props, "username", "root");
        password = getInitProperty(props, "password", "");
        driverClassName = getInitProperty(props, "driverClassName", "com.mysql.jdbc.Driver");
        url = getInitProperty(props, "url", "jdbc:mysql://localhost/infoglueWM?autoReconnect=true");

        /*
        entrySequence = getInitProperty(props, "entry.sequence", "SELECT nextVal('seq_os_wfentry')");
        stepSequence = getInitProperty(props, "step.sequence", "SELECT nextVal('seq_os_currentsteps')");
        entryTable = getInitProperty(props, "entry.table", "OS_WFENTRY");
        entryId = getInitProperty(props, "entry.id", "ID");
        entryName = getInitProperty(props, "entry.name", "NAME");
        entryState = getInitProperty(props, "entry.state", "STATE");
        historyTable = getInitProperty(props, "history.table", "OS_HISTORYSTEP");
        currentTable = getInitProperty(props, "current.table", "OS_CURRENTSTEP");
        currentPrevTable = getInitProperty(props, "currentPrev.table", "OS_CURRENTSTEP_PREV");
        historyPrevTable = getInitProperty(props, "historyPrev.table", "OS_HISTORYSTEP_PREV");
        stepId = getInitProperty(props, "step.id", "ID");
        stepEntryId = getInitProperty(props, "step.entryId", "ENTRY_ID");
        stepStepId = getInitProperty(props, "step.stepId", "STEP_ID");
        stepActionId = getInitProperty(props, "step.actionId", "ACTION_ID");
        stepOwner = getInitProperty(props, "step.owner", "OWNER");
        stepCaller = getInitProperty(props, "step.caller", "CALLER");
        stepStartDate = getInitProperty(props, "step.startDate", "START_DATE");
        stepFinishDate = getInitProperty(props, "step.finishDate", "FINISH_DATE");
        stepDueDate = getInitProperty(props, "step.dueDate", "DUE_DATE");
        stepStatus = getInitProperty(props, "step.status", "STATUS");
        stepPreviousId = getInitProperty(props, "step.previousId", "PREVIOUS_ID");
        */
    } 
    
    protected Connection getConnection() throws SQLException 
    {
        closeConnWhenDone = true;

        Connection conn = null;
		
        logger.info("Establishing connection to database '" + this.url + "'");

		try 
		{
	        Class.forName(this.driverClassName).newInstance();
			conn = DriverManager.getConnection(url, this.userName, this.password);
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}
		
        return conn;
    }

    public void setEntryState(long id, int state) throws StoreException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();

            String sql = "UPDATE " + entryTable + " SET " + entryState + " = ? WHERE " + entryId + " = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, state);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new StoreException("Unable to update state for workflow instance #" + id + " to " + state, e);
        } finally {
            cleanup(conn, ps, null);
        }
    }
    
    public Step createCurrentStep(long entryId, int wfStepId, String owner, Date startDate, Date dueDate, String status, long[] previousIds) throws StoreException {
        Connection conn = null;

        try {
            conn = getConnection();

            long id = createCurrentStep(conn, entryId, wfStepId, owner, startDate, dueDate, status);
            addPreviousSteps(conn, id, previousIds);

            return new SimpleStep(id, entryId, wfStepId, 0, owner, startDate, dueDate, null, status, previousIds, null);
        } catch (SQLException e) {
            throw new StoreException("Unable to create current step for workflow instance #" + entryId, e);
        } finally {
            cleanup(conn, null, null);
        }
    }

    public WorkflowEntry createEntry(String workflowName) throws StoreException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();

            String sql = "INSERT INTO " + entryTable + " (" + entryId + ", " + entryName + ", " + entryState + ") VALUES (?,?,?)";

            if (logger.isDebugEnabled()) {
                logger.debug("Executing SQL statement: " + sql);
            }

            stmt = conn.prepareStatement(sql);

            long id = getNextEntrySequence(conn);
            stmt.setLong(1, id);
            stmt.setString(2, workflowName);
            stmt.setInt(3, WorkflowEntry.CREATED);
            stmt.executeUpdate();

            return new SimpleWorkflowEntry(id, workflowName, WorkflowEntry.CREATED);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreException("Error creating new workflow instance", e);
        } finally {
            cleanup(conn, stmt, null);
        }
    }

    public List findCurrentSteps(long entryId) throws StoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        PreparedStatement stmt2 = null;

        try {
            conn = getConnection();

            String sql = "SELECT " + stepId + ", " + stepStepId + ", " + stepActionId + ", " + stepOwner + ", " + stepStartDate + ", " + stepDueDate + ", " + stepFinishDate + ", " + stepStatus + ", " + stepCaller + " FROM " + currentTable + " WHERE " + stepEntryId + " = ?";
            String sql2 = "SELECT " + stepPreviousId + " FROM " + currentPrevTable + " WHERE " + stepId + " = ?";

            if (logger.isDebugEnabled()) {
                logger.debug("Executing SQL statement: " + sql);
            }

            stmt = conn.prepareStatement(sql);

            if (logger.isDebugEnabled()) {
                logger.debug("Executing SQL statement: " + sql2);
            }

            stmt2 = conn.prepareStatement(sql2);
            stmt.setLong(1, entryId);

            rset = stmt.executeQuery();

            ArrayList currentSteps = new ArrayList();

            while (rset.next()) {
                long id = rset.getLong(1);
                int stepId = rset.getInt(2);
                int actionId = rset.getInt(3);
                String owner = rset.getString(4);
                Date startDate = rset.getTimestamp(5);
                Date dueDate = rset.getTimestamp(6);
                Date finishDate = rset.getTimestamp(7);
                String status = rset.getString(8);
                String caller = rset.getString(9);

                ArrayList prevIdsList = new ArrayList();
                stmt2.setLong(1, id);

                ResultSet rs = stmt2.executeQuery();

                while (rs.next()) {
                    long prevId = rs.getLong(1);
                    prevIdsList.add(new Long(prevId));
                }

                long[] prevIds = new long[prevIdsList.size()];
                int i = 0;

                for (Iterator iterator = prevIdsList.iterator();
                        iterator.hasNext();) {
                    Long aLong = (Long) iterator.next();
                    prevIds[i] = aLong.longValue();
                    i++;
                }

                SimpleStep step = new SimpleStep(id, entryId, stepId, actionId, owner, startDate, dueDate, finishDate, status, prevIds, caller);
                currentSteps.add(step);
            }

            return currentSteps;
        } catch (SQLException e) {
            throw new StoreException("Unable to locate current steps for workflow instance #" + entryId, e);
        } finally {
            cleanup(null, stmt2, null);
            cleanup(conn, stmt, rset);
        }
    }

    public WorkflowEntry findEntry(long theEntryId) throws StoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;

        try {
            conn = getConnection();

            String sql = "SELECT " + entryName + ", " + entryState + " FROM " + entryTable + " WHERE " + entryId + " = ?";

            if (logger.isDebugEnabled()) {
                logger.debug("Executing SQL statement: " + sql);
            }

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, theEntryId);

            rset = stmt.executeQuery();
            rset.next();

            String workflowName = rset.getString(1);
            int state = rset.getInt(2);

            return new SimpleWorkflowEntry(theEntryId, workflowName, state);
        } catch (SQLException e) {
            throw new StoreException("Error finding workflow instance #" + entryId);
        } finally {
            cleanup(conn, stmt, rset);
        }
    }

    public List findHistorySteps(long entryId) throws StoreException {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        ResultSet rset = null;

        try {
            conn = getConnection();

            String sql = "SELECT " + stepId + ", " + stepStepId + ", " + stepActionId + ", " + stepOwner + ", " + stepStartDate + ", " + stepDueDate + ", " + stepFinishDate + ", " + stepStatus + ", " + stepCaller + " FROM " + historyTable + " WHERE " + stepEntryId + " = ? ORDER BY " + stepId + " DESC";
            String sql2 = "SELECT " + stepPreviousId + " FROM " + historyPrevTable + " WHERE " + stepId + " = ?";

            if (logger.isDebugEnabled()) {
                logger.debug("Executing SQL statement: " + sql);
            }

            stmt = conn.prepareStatement(sql);

            if (logger.isDebugEnabled()) {
                logger.debug("Executing SQL statement: " + sql2);
            }

            stmt2 = conn.prepareStatement(sql2);
            stmt.setLong(1, entryId);

            rset = stmt.executeQuery();

            ArrayList currentSteps = new ArrayList();

            while (rset.next()) {
                long id = rset.getLong(1);
                int stepId = rset.getInt(2);
                int actionId = rset.getInt(3);
                String owner = rset.getString(4);
                Date startDate = rset.getTimestamp(5);
                Date dueDate = rset.getTimestamp(6);
                Date finishDate = rset.getTimestamp(7);
                String status = rset.getString(8);
                String caller = rset.getString(9);

                ArrayList prevIdsList = new ArrayList();
                stmt2.setLong(1, id);

                ResultSet rs = stmt2.executeQuery();

                while (rs.next()) {
                    long prevId = rs.getLong(1);
                    prevIdsList.add(new Long(prevId));
                }

                long[] prevIds = new long[prevIdsList.size()];
                int i = 0;

                for (Iterator iterator = prevIdsList.iterator();
                        iterator.hasNext();) {
                    Long aLong = (Long) iterator.next();
                    prevIds[i] = aLong.longValue();
                    i++;
                }

                SimpleStep step = new SimpleStep(id, entryId, stepId, actionId, owner, startDate, dueDate, finishDate, status, prevIds, caller);
                currentSteps.add(step);
            }

            return currentSteps;
        } catch (SQLException e) {
            throw new StoreException("Unable to locate history steps for workflow instance #" + entryId, e);
        } finally {
            cleanup(null, stmt2, null);
            cleanup(conn, stmt, rset);
        }
    }
    
    public Step markFinished(Step step, int actionId, Date finishDate, String status, String caller) throws StoreException 
	{
        Connection conn = null;
        PreparedStatement stmt = null;

        try 
		{
            conn = getConnection();

            String sql = "UPDATE " + currentTable + " SET " + stepStatus + " = ?, " + stepActionId + " = ?, " + stepFinishDate + " = ?, " + stepCaller + " = ? WHERE " + stepId + " = ?";

            logger.info("Executing SQL statement: " + sql);
            logger.info("status: " + status);
            logger.info("actionId: " + actionId);
            logger.info("new Timestamp(finishDate.getTime()): " + new Timestamp(finishDate.getTime()));
            logger.info("caller: " + caller);
            logger.info("step.getId(): " + step.getId());
            
            if (logger.isDebugEnabled()) 
            {
                logger.debug("Executing SQL statement: " + sql);
            }

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, actionId);
            stmt.setTimestamp(3, new Timestamp(finishDate.getTime()));
            stmt.setString(4, caller);
            stmt.setLong(5, step.getId());
            stmt.executeUpdate();

            SimpleStep theStep = (SimpleStep) step;
            theStep.setActionId(actionId);
            theStep.setFinishDate(finishDate);
            theStep.setStatus(status);
            theStep.setCaller(caller);

            return theStep;
        } 
        catch (SQLException e) 
		{
        	throw new StoreException("Unable to mark step finished for #" + step.getEntryId(), e);
        } 
        finally 
		{
            cleanup(conn, stmt, null);
        }
    }

    public void moveToHistory(Step step) throws StoreException 
	{
        Connection conn = null;
        PreparedStatement stmt = null;

        try 
		{
            conn = getConnection();

            String sql = "INSERT INTO " + historyTable + " (" + stepId + "," + stepEntryId + ", " + stepStepId + ", " + stepActionId + ", " + stepOwner + ", " + stepStartDate + ", " + stepFinishDate + ", " + stepStatus + ", " + stepCaller + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            logger.info("Executing SQL statement: " + sql);
            logger.info("step.getId(): " + step.getId());
            logger.info("step.getEntryId(): " + step.getEntryId());
            logger.info("step.getStepId(): " + step.getStepId());
            logger.info("step.getActionId(): " + step.getActionId());
            logger.info("step.getOwner(): " + step.getOwner());
            logger.info("new Timestamp(step.getStartDate().getTime()): " + new Timestamp(step.getStartDate().getTime()));
            
            if (logger.isDebugEnabled()) 
            {
                logger.debug("Executing SQL statement: " + sql);
            }

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, step.getId());
            stmt.setLong(2, step.getEntryId());
            stmt.setInt(3, step.getStepId());
            stmt.setInt(4, step.getActionId());
            stmt.setString(5, step.getOwner());
            stmt.setTimestamp(6, new Timestamp(step.getStartDate().getTime()));

            if (step.getFinishDate() != null) 
            {
                stmt.setTimestamp(7, new Timestamp(step.getFinishDate().getTime()));
            } 
            else 
            {
                stmt.setNull(7, Types.TIMESTAMP);
            }

            stmt.setString(8, step.getStatus());
            stmt.setString(9, step.getCaller());
            stmt.executeUpdate();

            long[] previousIds = step.getPreviousStepIds();

            if ((previousIds != null) && (previousIds.length > 0)) 
            {
                sql = "INSERT INTO " + historyPrevTable + " (" + stepId + ", " + stepPreviousId + ") VALUES (?, ?)";
                logger.debug("Executing SQL statement: " + sql);
                cleanup(null, stmt, null);
                stmt = conn.prepareStatement(sql);

                for (int i = 0; i < previousIds.length; i++) 
                {
                    long previousId = previousIds[i];
                    stmt.setLong(1, step.getId());
                    stmt.setLong(2, previousId);
                    stmt.executeUpdate();
                }
            }

            sql = "DELETE FROM " + currentPrevTable + " WHERE " + stepId + " = ?";

            if (logger.isDebugEnabled()) 
            {
                logger.debug("Executing SQL statement: " + sql);
            }

            cleanup(null, stmt, null);
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, step.getId());
            stmt.executeUpdate();

            sql = "DELETE FROM " + currentTable + " WHERE " + stepId + " = ?";

            if (logger.isDebugEnabled()) 
            {
                logger.debug("Executing SQL statement: " + sql);
            }

            cleanup(null, stmt, null);
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, step.getId());
            stmt.executeUpdate();
        } 
        catch (SQLException e) 
		{
            throw new StoreException("Unable to move current step to history step for #" + step.getEntryId(), e);
        } 
        finally 
		{
            cleanup(conn, stmt, null);
        }
    }

    public List query(WorkflowQuery query) throws StoreException 
	{
        List results = new ArrayList();

        // going to try to do all the comparisons in one query
        String sel;
        String table;

        int qtype = query.getType();

        if (qtype == 0) { // then not set, so look in sub queries
                          // todo: not sure if you would have a query that would look in both old and new, if so, i'll have to change this - TR
                          // but then again, why are there redundant tables in the first place? the data model should probably change

            if (query.getLeft() != null) 
            {
                qtype = query.getLeft().getType();
            }
        }

        if (qtype == WorkflowQuery.CURRENT) {
            table = currentTable;
        } else {
            table = historyTable;
        }

        sel = "SELECT DISTINCT(" + stepEntryId + ") FROM " + table + " WHERE ";
        sel += queryWhere(query);

        if (logger.isDebugEnabled()) {
            logger.debug(sel);
        }

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sel);

            while (rs.next()) {
                // get entryIds and add to results list
                Long id = new Long(rs.getLong(stepEntryId));
                results.add(id);
            }
        } catch (SQLException ex) {
            throw new StoreException("SQL Exception in query: " + ex.getMessage());
        } finally {
            cleanup(conn, stmt, rs);
        }

        return results;
    }


    private List doExpressionQuery(String sel, String columnName, List values) throws StoreException {
        if (logger.isDebugEnabled()) {
            logger.debug(sel);
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List results = new ArrayList();

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sel);

            if (!values.isEmpty()) {
                for (int i = 1; i <= values.size(); i++) {
                    stmt.setObject(i, values.get(i - 1));
                }
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                // get entryIds and add to results list
                Long id = new Long(rs.getLong(columnName));
                results.add(id);
            }

            return results;
        } catch (SQLException ex) {
            throw new StoreException("SQL Exception in query: " + ex.getMessage());
        } finally {
            cleanup(conn, stmt, rs);
        }
    }

    private String getInitProperty(Map props, String strName, String strDefault) 
    {
        Object o = props.get(strName);

        if (o == null) {
            return strDefault;
        }

        return (String) o;
    } 
    
    private String queryWhere(WorkflowQuery query) {
        if (query.getLeft() == null) {
            // leaf node
            return queryComparison(query);
        } else {
            int operator = query.getOperator();
            WorkflowQuery left = query.getLeft();
            WorkflowQuery right = query.getRight();

            switch (operator) {
            case WorkflowQuery.AND:
                return "(" + queryWhere(left) + " AND " + queryWhere(right) + ")";

            case WorkflowQuery.OR:
                return "(" + queryWhere(left) + " OR " + queryWhere(right) + ")";

            case WorkflowQuery.XOR:
                return "(" + queryWhere(left) + " XOR " + queryWhere(right) + ")";
            }
        }

        return ""; // not sure if we should throw an exception or how this should be handled
    } 
    
    private String queryComparison(WorkflowQuery query) {
        Object value = query.getValue();
        int operator = query.getOperator();
        int field = query.getField();

        //int type = query.getType();
        String oper;

        switch (operator) {
        case WorkflowQuery.EQUALS:
            oper = " = ";

            break;

        case WorkflowQuery.NOT_EQUALS:
            oper = " <> ";

            break;

        case WorkflowQuery.GT:
            oper = " > ";

            break;

        case WorkflowQuery.LT:
            oper = " < ";

            break;

        default:
            oper = " = ";
        }

        String left;
        String right;

        switch (field) {
        case WorkflowQuery.ACTION: // actionId
            left = stepActionId;

            break;

        case WorkflowQuery.CALLER:
            left = stepCaller;

            break;

        case WorkflowQuery.FINISH_DATE:
            left = stepFinishDate;

            break;

        case WorkflowQuery.OWNER:
            left = stepOwner;

            break;

        case WorkflowQuery.START_DATE:
            left = stepStartDate;

            break;

        case WorkflowQuery.STEP: // stepId
            left = stepStepId;

            break;

        case WorkflowQuery.STATUS:
            left = stepStatus;

            break;

        default:
            left = "1";
        }

        if (value != null) {
            right = "'" + escape(value.toString()) + "'";
        } else {
            right = "null";
        }

        return left + oper + right;
    } 
    
    private static String escape(String s) {
        StringBuffer sb = new StringBuffer(s);

        char c;
        char[] chars = s.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            c = chars[i];

            switch (c) {
            case '\'':
                sb.insert(i, '\'');
                i++;

                break;

            case '\\':
                sb.insert(i, '\\');
                i++;
            }
        }

        return sb.toString();
    }
 
} 

