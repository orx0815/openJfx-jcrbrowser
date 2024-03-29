package org.motorbrot.javafxjcrbrowser.jcrnodetree;

import org.motorbrot.javafxjcrbrowser.JcrBrowserSceneController;
import org.motorbrot.javafxjcrbrowser.bling.BlingTabController;
import org.motorbrot.javafxjcrbrowser.csv.CsvTabController;
import org.motorbrot.javafxjcrbrowser.jcrpanel.JcrPanelController;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import org.apache.jackrabbit.commons.JcrUtils;
import org.motorbrot.javafxjcrbrowser.SceneIncludeController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Changes the jcr-property table, when a Node it selected in the JcrTree
 */
@Component
public class JcrTreeSelectionListener extends SceneIncludeController implements ChangeListener<TreeItem<Node>> {

  private static final Logger LOG = Logger.getLogger(JcrTreeSelectionListener.class.getName());
  
  @Autowired
  private BlingTabController blingTabController;
  @Autowired
  private CsvTabController csvTabController;
  
  private JcrPanelController jcrPanelController;

  
  public void setJcrPanelController(JcrPanelController jcrPanelController) {
    this.jcrPanelController = jcrPanelController;
  }
  
          
  @Override
  public void changed(ObservableValue<? extends TreeItem<Node>> observable, TreeItem<Node> old_val, TreeItem<Node> new_val) {
    if (new_val == null) {
      return;
    }
    try {
      final Node jcrNode = new_val.getValue();
      String jcrType = JcrUtils.getStringProperty(jcrNode, "jcr:primaryType", null);
      String slingResourceType = JcrUtils.getStringProperty(jcrNode, "sling:resourceType", null);
      
      if ("dam:Asset".equals(jcrType)) {
        Node rendition = jcrNode.getNode("jcr:content/renditions/cq5dam.thumbnail.319.319.png/jcr:content");
        if (rendition.hasProperty("jcr:data")) {
          Binary binary = rendition.getProperty("jcr:data").getBinary();
          throwImgBinarytoGui(binary);
        }
      }
      else if ("javafxjcrbrowser:sample".equals(slingResourceType)) {
        // nodeType uploaded via uploadtestContent.sh 
        if (jcrNode.hasProperty("csvBlob")) {
          Binary binary = jcrNode.getProperty("csvBlob").getBinary();
          InputStream is = binary.getStream();
          getParent().switchToTab(1);
          csvTabController.loadCvsTable(is, jcrNode.getPath());
        }
      }
      else if ("sling:File".equals(slingResourceType)) {
        Node contentNode = JcrUtils.getNodeIfExists(jcrNode, "jcr:content");
        if (contentNode != null) {
          String mime = JcrUtils.getStringProperty(contentNode, "jcr:mimeType", "");
          if (mime.startsWith("image")) {
            Binary binary = JcrUtils.getBinaryProperty(contentNode, "jcr:data", null);
            throwImgBinarytoGui(binary);
          }
        }
      }
      
      getParent().getRootPathField().setText(jcrNode.getPath());
              
      updateNodePropertyTable(jcrNode);
    }
    catch (RepositoryException ex) {
      LOG.log(Level.SEVERE, null, ex);
      getParent().getDebugTxt().appendText(ex.getMessage()+"\n");
    }
  }

  private void throwImgBinarytoGui(Binary binary) throws RepositoryException {
    InputStream is = binary.getStream();
    Image image = new Image(is, 0, 200, true, false);
    getParent().getDamImageView().setImage(image);
    getParent().switchToTab(2);
    blingTabController.getDisplayShelf().addImage(image);
  }

  private void updateNodePropertyTable(final Node jcrNode) throws RepositoryException, IllegalStateException {
    Map<String, String> map = jcrPropsToFxMap(jcrNode);
    
    // https://docs.oracle.com/javase/9/docs/api/javafx/collections/ListChangeListener.Change.html
    ObservableList<Map.Entry<String, String>> myObservableList = FXCollections.observableArrayList(map.entrySet());
    myObservableList.addListener(new ListChangeListener<Map.Entry<String, String>>() {
      
      @Override
      public void onChanged(ListChangeListener.Change<? extends Map.Entry<String, String>> c) {
        while (c.next()) {
          if (c.wasPermutated()) {
            for (int i = c.getFrom(); i < c.getTo(); ++i) {
              //permutate
            }
          }
          else if (c.wasUpdated()) {
            //update item
          }
          else {
            try {
              for (Map.Entry<String, String> remItem : c.getRemoved()) {
                Value nullVal = null;
                jcrNode.getProperty(remItem.getKey()).setValue(nullVal);
              }
              for (Map.Entry<String, String> addItem : c.getAddedSubList()) {
                jcrNode.setProperty(addItem.getKey(), addItem.getValue());
              }
              
            }
            catch (RepositoryException ex) {
              LOG.log(Level.SEVERE, null, ex);
              getParent().getDebugTxt().appendText(ex.getMessage()+"\n");
            }
          }
        }
      }
      
    });
    
    TableView<Map.Entry<String, String>> propsTable = jcrPanelController.getNodePropsTableView();
    propsTable.setItems(myObservableList);
  }
  

  /**
   * takes a jcr-node and makes a propName:value Map from it 
   * @param jcrNode
   * @return
   * @throws RepositoryException
   * @throws IllegalStateException 
   */
  private Map<String, String> jcrPropsToFxMap(final Node jcrNode) throws RepositoryException, IllegalStateException {
    PropertyIterator properties = jcrNode.getProperties();
    Map<String, String> map = new HashMap<>();
    while (properties.hasNext()) {
      Property nextProperty = properties.nextProperty();
      int type = nextProperty.getType();
      String propName = nextProperty.getName();
      String propval;
      if (nextProperty.isMultiple()) {
        Value[] values = nextProperty.getValues();
        StringBuilder sb = new StringBuilder();
        for (Value value : values) {
          sb.append(jcrPropertyValue2String(type, value)).append("\n");
        }
        propval = sb.toString();
      }
      else {
        Value propvalue = nextProperty.getValue();
        propval = jcrPropertyValue2String(type, propvalue);
      }
      map.put(propName, propval);
    }
    return map;
  }

  private String jcrPropertyValue2String(int type, Value propvalue) throws IllegalStateException, RepositoryException {
    String propval = "toDo";
    if (type == PropertyType.BINARY) {
      propval = "=binary=";
    }
    else {
      try {
        propval = propvalue.getString();
      }
      catch (ValueFormatException vfex) {
        LOG.log(Level.SEVERE, "ValueFormatException {0}", vfex.getMessage());
      }
    }
    return propval;
  }

}
