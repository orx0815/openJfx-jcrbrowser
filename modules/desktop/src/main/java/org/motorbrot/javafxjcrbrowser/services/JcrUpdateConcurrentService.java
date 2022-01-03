package org.motorbrot.javafxjcrbrowser.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.motorbrot.javafxjcrbrowser.cli.JcrMigrationTask;
import org.motorbrot.javafxjcrbrowser.cli.JcrMigrationTask.ActivityCallback;
import org.motorbrot.javafxjcrbrowser.cli.JcrMigrationTask.JcrMigrationParameters;
import org.motorbrot.javafxjcrbrowser.cli.JcrMigrationTask.JcrMigrationResult;


/**
 * Start/Stop behavior
 */
public class JcrUpdateConcurrentService extends Service<JcrMigrationResult> {

  private final JcrMigrationParameters jcrMigrationParameters;
  private final ActivityCallback callback;
  
  JcrMigrationTask contentUpdateTask;
  
  ExecutorService executorService = Executors.newFixedThreadPool(2);

  public JcrUpdateConcurrentService(JcrMigrationParameters jcrMigrationParameters, ActivityCallback callback) {
    this.jcrMigrationParameters = jcrMigrationParameters;
    this.callback = callback;
  }
  
  @Override
  protected Task<JcrMigrationResult> createTask() {
    contentUpdateTask = new JcrMigrationTask(jcrMigrationParameters, callback);
    Future<JcrMigrationResult> future = executorService.submit(contentUpdateTask);
    return new Task<JcrMigrationResult>() {
      
      @Override
      protected JcrMigrationResult call() throws Exception {
        return future.get();
      }
      
    };
  }

}
