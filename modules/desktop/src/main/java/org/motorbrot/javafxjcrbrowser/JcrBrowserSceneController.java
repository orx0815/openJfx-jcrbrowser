package org.motorbrot.javafxjcrbrowser;

import org.motorbrot.javafxjcrbrowser.jcrnodetree.NodeTreeItem;
import org.motorbrot.javafxjcrbrowser.jcrpanel.JcrPanelController;
import org.motorbrot.javafxjcrbrowser.services.JcrService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import org.apache.jackrabbit.spi.commons.conversion.MalformedPathException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * FXML Controller for parent fxml that includes all others
 */
@Component
public class JcrBrowserSceneController {

  private static final Logger LOG = Logger.getLogger(JcrBrowserSceneController.class.getName());
  
  @Autowired
  private JcrService jcrService;
  
  @Autowired
  private JcrPanelController jcrPanelController;
  
  // Login Controls
  @FXML
  private TextField usernameField;
  @FXML
  private TextField passwdField;
  @FXML
  private TextField workSpaceField;
  @FXML
  private Button loginButton;
  @FXML
  private TextField jcrUrlField;
  
  @FXML
  private TextField rootPathField;

  @FXML
  private TabPane tabPane;

  @FXML
  private TextArea debugTxt;

  @FXML
  private ImageView damImageView;
  
  @FXML
  private Button refreshButton;

  
  /*
    NESTED CONTROLLERS  (Commented out but kept to show non-spring fxml way to inject inner controllers)

    Current spring-autowiring of fxml-controllers uses singletons.
    So it's not possible to inject two different controllers with the same fxml

    The field name the controller is injected to is always constructed by concatenating the fx:id of the <fx:include> tag with "Controller".
    https://stackoverflow.com/questions/44467982/javafx8-fxml-naming-of-nested-controllers
  
    Attention, netbeans deletes these @FXML annotations when clicking "make Controller" on the fxml
   */
//  @FXML
//  private CsvTabController csvTabController;
//  @FXML
//  private BlingTabController blingTabController;

  /**
   * The initialize method is called after all @FXML annotated members have been injected. 
   */
  public void initialize() {
    
    // switch to tab 3
    switchToTab(2);
    
    // javafx-bindings for login-state
    BooleanBinding sessionExists = Bindings.createBooleanBinding(() -> {
      return jcrService.getJcrSessionProperty().get() != null; 
    }, jcrService.getJcrSessionProperty()); // observe session
    
    // and disable login-inputs when logged in
    usernameField.disableProperty().bind(sessionExists);
    usernameField.disableProperty().bind(sessionExists);
    passwdField.disableProperty().bind(sessionExists);
    workSpaceField.disableProperty().bind(sessionExists);
    jcrUrlField.disableProperty().bind(sessionExists);
    refreshButton.disableProperty().bind(sessionExists.not());
    
    StringBinding loginButtonBinding = Bindings.createStringBinding(() -> {
      if (jcrService.getJcrSessionProperty().get() != null) {
        return "LOGOUT";
      } else {
        return "LOGIN";
      }
    },jcrService.getJcrSessionProperty());
    
    loginButton.textProperty().bind(loginButtonBinding);


    // litte trick, requestFocus doesn't work in initialize
    Platform.runLater(loginButton::requestFocus); // for quickly pressing enter

  }

  public void switchToTab(int tabIndex) {
    tabPane.getSelectionModel().select(tabIndex);
  }

  @FXML
  private void loginButtonPressed(ActionEvent event) {
    try {
      if (jcrService.getJcrSession() == null) {
        
        String user = usernameField.getText().trim();
        String passwd = passwdField.getText().trim();
        String repo = jcrUrlField.getText().trim();
        String workspace = workSpaceField.getText().trim();
        
        jcrService.login(repo, user, passwd, workspace);
        
        setBrowserTreeRoot(rootPathField.getText().trim());
      }
      else {
        // do logout
        jcrPanelController.getFxTreeView().setRoot(new TreeItem<>());
        jcrService.logout();
      }

    }
    catch (RepositoryException ex) {
      LOG.log(Level.SEVERE, null, ex);
      debugTxt.appendText("ERROR during login: " + ex.getMessage() + "\n");
    }

  }

  private void setBrowserTreeRoot(String rootPath) {
    if (jcrService.getJcrSession() != null) {
      try {
        Node rootNode = jcrService.getJcrSession().getNode(rootPath);
        TreeItem<Node> treeRootNode = new NodeTreeItem(rootNode, debugTxt);
        treeRootNode.setExpanded(true);
        jcrPanelController.getFxTreeView().setRoot(treeRootNode);
        jcrPanelController.getFxTreeView().getSelectionModel().select(treeRootNode);
        debugTxt.appendText("Set browser-root to: " + rootPath + "\n");
        
        jcrPanelController.getFxTreeView().requestFocus();
        
      }
      catch (PathNotFoundException | MalformedPathException pnfe) {
        debugTxt.appendText("Invalid Path: " + pnfe.getMessage() + "\n");
      }
      catch (RepositoryException ex) {
        LOG.log(Level.SEVERE, null, ex);
        debugTxt.appendText("RepositoryException: " + ex.getMessage() + "\n");
      }
    }
  }

  // on return-key in rootPathField, set root of jcr browser-tree
  @FXML
  private void rootPathSet(ActionEvent event) {
    setBrowserTreeRoot(rootPathField.getText().trim());
  }

  /**
   * @return text-field showing root-path of shown content-subtree
   */
  public TextField getRootPathField() {
    return rootPathField;
  }

  /**
   * @return textArea for Info-Log
   */
  public TextArea getDebugTxt() {
    return debugTxt;
  }

  /**
   * view for Digital Asset Management in AEM
   * @return ImageView
   */
  public ImageView getDamImageView() {
    return damImageView;
  }

  @FXML
  private void refreshButtonPressed(ActionEvent event) {
    if (jcrService.getJcrSession() != null) {
      try {
        jcrService.getJcrSession().refresh(true);
      }
      catch (RepositoryException ex) {
        LOG.log(Level.SEVERE, null, ex);
        debugTxt.appendText("RepositoryException: " + ex.getMessage() + "\n");
      }
    }
  }

  
}
