package org.motorbrot.javafxjcrbrowser.contentupdate;

import org.motorbrot.javafxjcrbrowser.services.JcrUpdateConcurrentService;
import org.motorbrot.javafxjcrbrowser.JcrBrowserSceneController;
import org.motorbrot.javafxjcrbrowser.jcrpanel.JcrPanelController;
import org.motorbrot.javafxjcrbrowser.services.JcrService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.motorbrot.javafxjcrbrowser.SceneIncludeController;
import org.motorbrot.javafxjcrbrowser.cli.JcrMigrationTask.ActivityCallback;
import org.motorbrot.javafxjcrbrowser.cli.JcrMigrationTask.JcrMigrationParameters;
import org.motorbrot.javafxjcrbrowser.cli.JcrMigrationTask.JcrMigrationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * FXML Controller class
 */
@Component
public class JcrMigrationTabController extends SceneIncludeController {

  private static final Logger LOG = Logger.getLogger(JcrMigrationTabController.class.getName());

  @Autowired
  private JcrService jcrService;

  @Autowired
  private JcrPanelController jcrPanelController;

  private JcrUpdateConcurrentService backgroundService;

  @FXML
  private TextField resourceTypeField;
  @FXML
  private TextField propertyNameField;
  @FXML
  private TextField valueField;
  @FXML
  private TextField jobNameField;
  
  // Run Button
  @FXML
  private Button runTaskButton;

  @FXML
  private void runButtonClicked(ActionEvent event) {
    
    if (backgroundService != null && backgroundService.isRunning()) {
      getParent().getDebugTxt().appendText("Stopping content upadate job.\n");
      backgroundService.cancel();
      runTaskButton.setText("Start");
    } else {
      
      TreeItem<Node> selectedItem = this.jcrPanelController.getFxTreeView().getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        Node jcrNode = selectedItem.getValue();
        if (jcrNode != null) {

          try {
            String query = "SELECT * FROM [nt:base] AS comp "
              + "WHERE ISDESCENDANTNODE(comp, \"" + jcrNode.getPath() + "\") "
              + "AND comp.[sling:resourceType] LIKE '" + resourceTypeField.getText() + "'";

            JcrMigrationParameters jcrMigrationParameters = new JcrMigrationParameters(jcrNode, jobNameField.getText(), query,
              propertyNameField.getText(), valueField.getText() );

            backgroundService = new JcrUpdateConcurrentService(jcrMigrationParameters, new ActivityCallback() {
              @Override
              public void logActivity(String msg) {
                getParent().getDebugTxt().appendText(msg + "\n");
              }
            });
            backgroundService.setOnSucceeded((WorkerStateEvent evt) -> {
              getParent().getDebugTxt().appendText("Task finnished: " + JcrMigrationResult.FINNISHED_STATE + "\n");
              runTaskButton.setText("Start");
            });
            backgroundService.setOnFailed((WorkerStateEvent evt) -> {
              getParent().getDebugTxt().appendText("Task finnished: " + JcrMigrationResult.FAILED_STATE + "\n");
              runTaskButton.setText("Start");
            });
            backgroundService.setOnCancelled((WorkerStateEvent evt) -> {
              getParent().getDebugTxt().appendText("Task finnished: " + JcrMigrationResult.CANCELED_STATE + "\n");
              runTaskButton.setText("Start");
            });
            
            backgroundService.start();
            runTaskButton.setText("Cancel");

          }
          catch (RepositoryException ex) {
            getParent().getDebugTxt().appendText("Saving Session failed: " + ex.getMessage() + "\n");
            LOG.log(Level.SEVERE, null, ex);
          }

        }
        else {
          getParent().getDebugTxt().appendText("No jcrNode found in tree.\n");
          LOG.log(Level.SEVERE, "No jcrNode behind fxTreeItem.");
        }
      }
      else {
        getParent().getDebugTxt().appendText("No jcrNode selected in Tree.\n");
      }
      
    }

  }

  @FXML
  private void saveButtonClicked(ActionEvent event) {
    
    if (this.jcrService.getJcrSession() != null) {
      try {
        getParent().getDebugTxt().appendText("Saving Session ... ");
        this.jcrService.save();
        getParent().getDebugTxt().appendText("done.\n");
      }
      catch (RepositoryException ex) {
        getParent().getDebugTxt().appendText("Saving Session failed: " + ex.getMessage() + "\n");
        LOG.log(Level.SEVERE, "Saving Session failed.", ex);
      }
    } else {
      getParent().getDebugTxt().appendText("Login First.");
    }
  }
  
  @FXML
  private void discardButtonClicked(ActionEvent event) {
    if (this.jcrService.getJcrSession() != null) {
      try {
        getParent().getDebugTxt().appendText("Discarding Session ... ");
        this.jcrService.getJcrSession().refresh(false);
        getParent().getDebugTxt().appendText("done.\n");
      }
      catch (RepositoryException ex) {
        getParent().getDebugTxt().appendText("Discarding Session failed: " + ex.getMessage() + "\n");
        LOG.log(Level.SEVERE, "Discarding Session failed.", ex);
      }
    } else {
      getParent().getDebugTxt().appendText("Login First.");
    }
  }

  @FXML
  private void npe(ActionEvent event) {

    Java17Test java17Test = new Java17Test(123, "foo", "bar");

    LOG.info("java17 !!! " + java17Test.toString());
    LOG.info("java17 !!! " + java17Test.lastName());

    Java17Test java17Test_null = new Java17Test(123, "foo", null);

    try {
      LOG.info("v-v-v java17 Meaningfull NPE in log: " + java17Test_null.lastName().toUpperCase());
    }
    catch (NullPointerException npe) {
      LOG.log(Level.INFO, "Meaningful NullPointer: ", npe);
      getParent().getDebugTxt().appendText("Meaningful NullPointerException: " + npe.getMessage());
    }
  }
  
  private record Java17Test(int id, String firstName, String lastName){}

}
