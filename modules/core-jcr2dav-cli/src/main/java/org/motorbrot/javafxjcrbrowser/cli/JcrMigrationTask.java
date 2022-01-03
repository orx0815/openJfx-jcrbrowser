package org.motorbrot.javafxjcrbrowser.cli;

import java.util.Date;
import java.util.concurrent.Callable;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

/**
 * Performs the actual job of modifying content and returning a JcrMigrationResult
 */
public class JcrMigrationTask implements Callable<JcrMigrationTask.JcrMigrationResult> {

  private final JcrMigrationParameters migrationParameters;
  private final ActivityCallback callback;
  private JcrMigrationResult migrationResult;

  /**
   * @param parameters with at least jcrNode root to start migration in
   * @param callback for reporting back to GUI or commandline
   */
  public JcrMigrationTask(JcrMigrationParameters parameters, ActivityCallback callback) {
    this.migrationParameters = parameters;
    this.callback = callback;
  }

  @Override
  public JcrMigrationResult call() throws Exception {

    callback.logActivity("Excecuting query: " + this.migrationParameters.sql2Query);
    QueryResult executeQuery = executeQuery(this.migrationParameters.jcrNode, this.migrationParameters.sql2Query);
    
    NodeIterator nodes = executeQuery.getNodes();
    if (!nodes.hasNext()) {
      callback.logActivity("No matching node found to modify.");
    }
    while (nodes.hasNext()) {
      Node node = nodes.nextNode();
      callback.logActivity("Modifying node: " + node.getPath());
      node.setProperty(this.migrationParameters.propertyName, this.migrationParameters.value);
    }
    JcrMigrationResult result = new JcrMigrationResult(this.migrationParameters.jcrNode.getPath(), this.migrationParameters.jobName);
    return result;
  }
  

  private QueryResult executeQuery(Node jcrRootNode, String queryString) throws RepositoryException {

    Workspace workspace = jcrRootNode.getSession().getWorkspace();
    QueryManager queryManager = workspace.getQueryManager();
    Query query = queryManager.createQuery(queryString, Query.JCR_SQL2);
    QueryResult result;
    try {

      result = query.execute();

    }
    catch (RepositoryException ex) {
      callback.logActivity("Query-Result too large? :" + ex.toString());
      migrationResult.setStatus(JcrMigrationResult.FAILED_STATE);
      throw ex;
    }
    return result;
  }

  public JcrMigrationResult getIncompletejcrMigrationResult() {
    return migrationResult;
  }

  public JcrMigrationParameters getJcrMigrationParameters() {
    return migrationParameters;
  }

  /**
   * For gui elements to update during run. 
   */
  public interface ActivityCallback {

    public void logActivity(String msg);

  }

  /**
   * Example input parameters for a content update.
   * Runs provided query and sets propertyName=value on the nodes in result-set
   */
  public static class JcrMigrationParameters {

    private final Node jcrNode;
    final String jobName;
    
    private final String sql2Query;
    private final String propertyName;
    private final String value;

    public JcrMigrationParameters(Node jcrNode, String jobName, String sql2Query, String propertyName, String value) {
      this.jcrNode = jcrNode;
      this.jobName = jobName;
      this.sql2Query = sql2Query;
      this.propertyName = propertyName;
      this.value = value;
    }
    
  }

  /**
   * Data Object holding report of a check
   */
  public class JcrMigrationResult {

    private final String path;
    private final String name;
    private final Date startDate;
    private final Integer numberOfChanges = 0;

    private String status = STARTED_STATE;
    public static final String STARTED_STATE = "Started";
    public static final String FINNISHED_STATE = "Successful";
    public static final String FAILED_STATE = "Failed";
    public static final String CANCELED_STATE = "Canceled";

    /**
     * Constructor
     *
     * @param path root node from where content gets crawled
     * @param name
     */
    public JcrMigrationResult(String path, String name) {
      this.path = path;
      this.name = name;
      startDate = new Date();
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public String getPath() {
      return path;
    }

    public String getName() {
      return name;
    }

    public Date getStartDate() {
      return startDate;
    }

    public Integer getNumberOfChanges() {
      return numberOfChanges;
    }
  }

}
