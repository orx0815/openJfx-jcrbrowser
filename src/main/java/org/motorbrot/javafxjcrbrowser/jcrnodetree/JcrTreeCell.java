package org.motorbrot.javafxjcrbrowser.jcrnodetree;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TreeCell;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * renders a Cell in Tree, according to data in jcrNode
 */
public class JcrTreeCell extends TreeCell<Node> {

  private static final Logger LOG = Logger.getLogger(JcrTreeCell.class.getName());
  
  private final String treeRootPath;

  /**
   * Constructor
   * @param rootPath of the tree to display
   */  
  public JcrTreeCell(final String rootPath) {
    this.treeRootPath = rootPath;
  }
  
  @Override
  protected void updateItem(Node item, boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setText(null);
      setGraphic(null);
    }
    else {
      try {
        String path = item.getPath();
        boolean isRoot = (path.length() == treeRootPath.length());
        if (!isRoot && !"/".equals(path)) {
          try {
            path = path.substring(item.getParent().getPath().length());
          }
          catch (javax.jcr.ItemNotFoundException infex) {
            LOG.warning("ItemNotFoundException. item-path:" + item.getPath());
          }
        }
        NodePane nodePane = new NodePane(path, isRoot, !item.hasNodes());
        setGraphic(nodePane);
      }
      catch (RepositoryException ex) {
        Logger.getLogger(JcrTreeCell.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  
}
