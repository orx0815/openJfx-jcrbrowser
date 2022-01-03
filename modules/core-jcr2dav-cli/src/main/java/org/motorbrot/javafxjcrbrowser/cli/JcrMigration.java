package org.motorbrot.javafxjcrbrowser.cli;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;

import org.apache.jackrabbit.commons.JcrUtils;

import org.motorbrot.javafxjcrbrowser.cli.JcrMigrationTask.*;

/**
 * Command line interface for content manipulation.
 */
public final class JcrMigration {

  /**
   * Command line options
   */
  public static final Options CLI_OPTIONS = new Options();
  static {
    CLI_OPTIONS.addOption("name", true, "Name of migration-ru, used as part of report's filename.");
    CLI_OPTIONS.addOption("user", true, "User to log into crx, defaults to admin.");
    CLI_OPTIONS.addOption("password", true, "Password to log into crx, defaults to admin.");
    CLI_OPTIONS.addOption("repository", true, "Repository url, defaults to http://localhost:8080/server .");
    CLI_OPTIONS.addOption("rootNode", true, "Jcr root-node, defaults to /content/javafxjcrbrowser");
    
    CLI_OPTIONS.addOption("?", false, "Print usage help.");
  }
  
  // order in help
  private static final String OPTS_ORDER = "name user password repository rootNode desktop ?"; // short option names

  private static final Logger LOG = Logger.getLogger(JcrMigration.class.getName());
  
  private JcrMigration() {
    // static methods only
  }

  /**
   * CLI entry point
   * @param args Command line arguments
   * @throws Exception Exception
   */
  public static void main(String[] args) throws Exception {
    
    CommandLine commandLine = new DefaultParser().parse(CLI_OPTIONS, args);

    if (args.length == 0 || commandLine.hasOption("?")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.setWidth(150);
        formatter.setOptionComparator((Option o1, Option o2) -> {
          return OPTS_ORDER.indexOf(o1.getOpt()) - OPTS_ORDER.indexOf(o2.getOpt());
      });
      formatter.printHelp("java -jar openjfx-jcr-browser.cli-<version>.jar " , CLI_OPTIONS);
      return;
    }
      
    // collect login info
    String user = commandLine.getOptionValue("user", "admin");
    String password = commandLine.getOptionValue("password", "admin");
    String repository = commandLine.getOptionValue("repository", "http://localhost:8080/server");
    String rootNodeStr = commandLine.getOptionValue("rootNode", "/content/javafxjcrbrowser");
      
    // login
    Repository crxRepo = JcrUtils.getRepository(repository);
    Session jcrSession = crxRepo.login(new SimpleCredentials(user, password.toCharArray()),"crx.default");
    Node rootNode = jcrSession.getNode(rootNodeStr);
        
    // params
    String name = commandLine.getOptionValue("name", "cli-jcr-migration-report");
    
    String query = "SELECT * FROM [nt:base] AS comp "
    + "WHERE ISDESCENDANTNODE(comp, \"" + rootNode.getPath() + "\") "
    + "AND comp.[sling:resourceType] LIKE 'javafxjcrbrowser:sample'";
    
    String value = "Jobname that set the value was " + name;
    
    JcrMigrationParameters params = new JcrMigrationParameters(rootNode, query, "example-jcr-prop_1", value, name);
      
    ActivityCallbackLog activityLog = new ActivityCallbackLog();
    
    // prepare task
    final JcrMigrationTask jcrMigrationTask = new JcrMigrationTask(params, activityLog);

    // write incomplete results after Ctrl+C
    CanceledReportWriteThread canceledReportWriteThread = new CanceledReportWriteThread(jcrMigrationTask, repository, activityLog);
    Runtime.getRuntime().addShutdownHook(canceledReportWriteThread);

    // run
    JcrMigrationResult result = jcrMigrationTask.call();
    result.setStatus(JcrMigrationResult.FINNISHED_STATE);
    
    LOG.info("Saving session!");
    jcrSession.save();

    // report
    File reportFile = FileReporter.toFile(result, repository, params, activityLog.getLog());
    LOG.info("Report Content: " + FileUtils.readFileToString(reportFile, Charset.defaultCharset()));

    Runtime.getRuntime().removeShutdownHook(canceledReportWriteThread);
    
  }

  private static class CanceledReportWriteThread extends Thread {

    private final JcrMigrationTask migrationTask;
    private final ActivityCallbackLog activityLog;
    private final String serverUrl;

    public CanceledReportWriteThread(JcrMigrationTask migrationTask, String serverUrl, ActivityCallbackLog activityLog) {
      this.migrationTask = migrationTask;
      this.serverUrl = serverUrl;
      this.activityLog = activityLog;
    }

    @Override
    public void run() {
      JcrMigrationResult incompleteResult = migrationTask.getIncompletejcrMigrationResult();
      if (JcrMigrationResult.FINNISHED_STATE.equals(incompleteResult.getStatus())) {
        // report
        JcrMigrationParameters parameters = migrationTask.getJcrMigrationParameters();
        File reportFile = FileReporter.toFile(incompleteResult, serverUrl, parameters, activityLog.getLog());
        try {
          System.out.println("Canceled Migration Task: " + FileUtils.readFileToString(reportFile, Charset.defaultCharset()));
        }
        catch (IOException ex) {
          Logger.getLogger(JcrMigration.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
  }

  /**
   * Simple logger for commandline
   */
  static class ActivityCallbackLog implements ActivityCallback {

    public final StringBuilder log = new StringBuilder();

    @Override
    public void logActivity(String msg) {
      LOG.info(msg);
      log.append(msg).append("\n");
    }

    public String getLog() {
      return log.toString();
    }
    
  }

}
