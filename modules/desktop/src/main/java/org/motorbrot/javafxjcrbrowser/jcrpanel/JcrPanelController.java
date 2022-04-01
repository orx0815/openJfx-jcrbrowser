package org.motorbrot.javafxjcrbrowser.jcrpanel;

import org.motorbrot.javafxjcrbrowser.JcrBrowserSceneController;
import org.motorbrot.javafxjcrbrowser.jcrnodetree.JcrTreeCell;
import org.motorbrot.javafxjcrbrowser.jcrnodetree.JcrTreeSelectionListener;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import javax.jcr.Node;
import org.motorbrot.javafxjcrbrowser.SceneIncludeController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *  Left panel, jcr-tree with node-properties table below
 */
@Component
public class JcrPanelController extends SceneIncludeController {
  
  @Autowired
  private JcrTreeSelectionListener jcrTreeSelectionListener;

  // Jcr-Tree + node-properties
  @FXML
  private TreeView<Node> fxTree;
  @FXML
  private TableView<Map.Entry<String, String>> nodePropsTable;
  @FXML
  private TableColumn<Map.Entry<String, String>, String> propertyName;
  @FXML
  private TableColumn<Map.Entry<String, String>, String> propertyValue;
  
  /**
   * gets called after @FXML injects
   */
  public void initialize() {
    String msg = "- initializing JcrPanel  -";
    Logger.getLogger(getClass().getName()).log(Level.INFO, msg);
    
    jcrTreeSelectionListener.setJcrPanelController(this);
    
    // Fx-Tree gets filled by jcr-Tree
    fxTree.setCellFactory(treeView -> new JcrTreeCell(getParent().getRootPathField().getText()));
    
    // Define that propsTable displays the jcr-properties from selected TreeNode. 
    // The map-entries come from JcrTreeSelectionListener.jcrPropsToFxMap()
    propertyName.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>>() {
      @Override
      public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) {
        // for first column we use key
        return new SimpleStringProperty(p.getValue().getKey());
      }
    });
    // same thing like propCol1, just with lambda
    propertyValue.setCellValueFactory(
        (TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) 
            -> new SimpleStringProperty(p.getValue().getValue()) // for second column we use value
    );
    
    // attach select-event Listener to Tree, that changes propsTable
    fxTree.getSelectionModel().selectedItemProperty().addListener(jcrTreeSelectionListener);
    
    //enabled copy from jcr-property-table to clipboard
    this.nodePropsTable.getSelectionModel().setCellSelectionEnabled(true);
    this.nodePropsTable.setOnKeyPressed(e -> {
      
      if (e.getCode() == KeyCode.C && e.isControlDown()) { // Strg+C
        
        Map.Entry<String, String> selectedItem = this.nodePropsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
          String value = selectedItem.getValue();
          // create clipboard content
          final ClipboardContent clipboardContent = new ClipboardContent();
          clipboardContent.putString(value);

          // set clipboard content
          Clipboard.getSystemClipboard().setContent(clipboardContent);
        }
      }

    });
    
    
  } 

  /**
   * @return the jcr tree view
   */
  public TreeView<Node> getFxTreeView() {
    return fxTree;
  }

  /**
   * @return view of node-property's table
   */
  public TableView<Map.Entry<String, String>> getNodePropsTableView() {
    return nodePropsTable;
  }

  
}
