package org.motorbrot.javafxjcrbrowser.xmlexport;

import org.motorbrot.javafxjcrbrowser.JcrBrowserSceneController;
import org.motorbrot.javafxjcrbrowser.jcrpanel.JcrPanelController;
import org.motorbrot.javafxjcrbrowser.services.JcrService;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * FXML Controller class
 */
@Controller
public class XmlExportTabController {

  private static final Logger LOG = Logger.getLogger(XmlExportTabController.class.getName());
  
  private static final String JCR_PLAYGROUND = "/content/jcr2dav_test/";
  
  @Autowired
  private JcrService jcrService;
  
  @Autowired
  private JcrBrowserSceneController jcrBrowserSceneController;
  
  @Autowired
  private JcrPanelController jcrPanelController;
  
  @FXML
  private Label copyLabel;
  @FXML
  private Button pasteButton;
  @FXML
  private Label pasteLabel;
  @FXML
  private TextField targetPath;

  @FXML
  private void copyButtonClicked(ActionEvent event) {
    
    TreeItem<Node> selectedItem = this.jcrPanelController.getFxTreeView().getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Node jcrNode = selectedItem.getValue();
      if (jcrNode != null) {
        try {
          String msg = "Copying jcrNode: ".concat(jcrNode.getPath()).concat("\n");
          LOG.log(Level.INFO, msg);
          jcrBrowserSceneController.getDebugTxt().appendText(msg);
          Session session = jcrService.getJcrSession();
          FileOutputStream fos;
        
          
          File dir = new File("jcrNodeDumps");
          if (!dir.exists()) dir.mkdirs();
          File tempFile = Files.createTempFile(dir.toPath(), "clipboard_"+jcrNode.getName(), ".xml").toFile();
          tempFile.deleteOnExit();
          
          fos = new FileOutputStream(tempFile);
          
          // exportSystemView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse)
          session.exportSystemView(jcrNode.getPath(), fos, false, false);
          
          copyLabel.setText(tempFile.getAbsolutePath());
          
          fos.close();
          
          pasteButton.setDisable(false);
          
          jcrBrowserSceneController.getDebugTxt().appendText("Save content-node to " + tempFile.getAbsolutePath() + "\n");
        }
        catch (RepositoryException | IOException ex) {
          LOG.log(Level.SEVERE, null, ex);
          jcrBrowserSceneController.getDebugTxt().appendText("ex: " + ex.getMessage() + "\n");
        }
        
      } else {
        copyLabel.setText("No jcrNode found in tree.");
        LOG.log(Level.SEVERE, "No jcrNode behind fxTreeItem.");
      }
    } else {
      copyLabel.setText("No jcrNode selected in Tree.");
    }
    
  }

  @FXML
  private void pasteButtonClicked(ActionEvent event) {
    LOG.log(Level.INFO, "pasteButtonClicked: " + event.toString());
    String filePath = copyLabel.getText();
    try {
      Session session = this.jcrService.getJcrSession();
      Node jcrNode = JcrUtils.getNodeIfExists(JCR_PLAYGROUND + targetPath.getText().trim(), session);

      if (jcrNode != null) {

        if (!jcrNode.getPath().startsWith(JCR_PLAYGROUND)) {
          pasteLabel.setText("Jcr-Writing only allowed under " + JCR_PLAYGROUND);
          return;
        }

        // dirty hack to be able to store in sdl without nodeType definitions
        FileInputStream fis = new FileInputStream(filePath);
        String xml = IOUtils.toString(fis, Charsets.toCharset("UTF-8"))
                .replaceAll("cq:PageContent", "nt:unstructured")
                .replaceAll("cq:Page", "nt:unstructured");

        InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

        session.getWorkspace().importXML(jcrNode.getPath(), stream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
        
         jcrBrowserSceneController.getDebugTxt().appendText("Inmported content-xml to " + jcrNode.getPath() + "\n");
      }
      else {
        pasteLabel.setText("Target not found in jcr.");
        LOG.log(Level.SEVERE, "Target not found in jcr.");
      }
    }
    catch (RepositoryException ex) {
      pasteLabel.setText(ex.getMessage());
      LOG.log(Level.SEVERE, null, ex);
    }
    catch (IOException ex) {
      pasteLabel.setText(ex.getMessage());
      LOG.log(Level.SEVERE, null, ex);
    }
  }

  @FXML
  private void saveButtonClicked(ActionEvent event) {
    try {
      LOG.log(Level.INFO, "saveButtonClicked: " + event.toString());
      this.jcrService.save();
    }
    catch (RepositoryException ex) {
      pasteLabel.setText("Saving Session failed: " + ex.getMessage());
      LOG.log(Level.SEVERE, null, ex);
    }
  }

  @FXML
  private void createTargetNodeClicked(ActionEvent event) {
    Session session = this.jcrService.getJcrSession();
    try {
      Node createdTarget = JcrUtils.getOrCreateByPath(JCR_PLAYGROUND + targetPath.getText().trim(), "nt:unstructured", session);
      jcrBrowserSceneController.getDebugTxt().appendText("Created target-node " + createdTarget.getPath() +"\n");
    }
    catch (RepositoryException ex) {
      LOG.log(Level.SEVERE, null, ex);
    }
  }

}
