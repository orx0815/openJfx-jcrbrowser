package org.motorbrot.javafxjcrbrowser.jcrnodetree;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 * Creating TreeItem instances on-demand in a memory-efficient way. Adapted from FileSystem example from:
 * https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TreeItem.html
 */
public class NodeTreeItem extends TreeItem<Node> {

  // We cache whether the Node is a leaf or not. A File is a leaf if
  // it is not a directory and does not have any files contained within
  // it. We cache this as isLeaf() is called often, and doing the 
  // actual check on File is expensive.
  private boolean isLeaf;

  // We do the children and leaf testing only once, and then set these
  // booleans to false so that we do not check again during this
  // run. A more complete implementation may need to handle more 
  // dynamic file system situations (such as where a folder has files
  // added after the TreeView is shown). Again, this is left as an
  // exercise for the reader.
  private boolean isFirstTimeChildren = true;
  private boolean isFirstTimeLeaf = true;

  // debug-textpanel from main controller
  private final TextArea debugTxt;

  /**
   * Constructor
   * @param rootNode tree's root node
   * @param debugTxt the log panel
   */
  public NodeTreeItem(Node rootNode, TextArea debugTxt) {
    super(rootNode);
    this.debugTxt = debugTxt;
  }

  @Override
  public ObservableList<TreeItem<Node>> getChildren() {
    if (isFirstTimeChildren) {
      isFirstTimeChildren = false;

      try {
        // First getChildren() call, so we actually go off and
        // determine the children of the File contained in this TreeItem.
        super.getChildren().setAll(buildChildren(this));
      }
      catch (RepositoryException ex) {
        Logger.getLogger(NodeTreeItem.class.getName()).log(Level.SEVERE, null, ex);
        debugTxt.appendText(ex.getMessage()+"\n");
        isFirstTimeChildren = true;
      }

    }
    return super.getChildren();
  }

  @Override
  public boolean isLeaf() {
    if (isFirstTimeLeaf) {
      isFirstTimeLeaf = false;
      Node n = getValue();
      try {
        isLeaf = !n.hasNodes();
      }
      catch (RepositoryException ex) {
        Logger.getLogger(NodeTreeItem.class.getName()).log(Level.SEVERE, null, ex);
        debugTxt.appendText(ex.getMessage()+"\n");
        isLeaf = true;
        isFirstTimeLeaf = true;
      }
    }
    
    return isLeaf;
  }

  private ObservableList<TreeItem<Node>> buildChildren(TreeItem<Node> treeItem) throws RepositoryException {
    Node n = treeItem.getValue();
    if (n != null && n.hasNodes()) {
      NodeIterator nodeIter = n.getNodes();
      if (nodeIter.hasNext()) {
        ObservableList<TreeItem<Node>> children = FXCollections.observableArrayList();
        while (nodeIter.hasNext()) {
          NodeTreeItem childnodeTreeItem = new NodeTreeItem(nodeIter.nextNode(), this.debugTxt);
          children.add(childnodeTreeItem);
        }
        return children;
      }
    }
    return FXCollections.emptyObservableList();
  }

}
