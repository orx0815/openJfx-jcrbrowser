package org.motorbrot.javafxjcrbrowser.jcrnodetree;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

/**
 * @author orx
 */
public class NodePane extends Pane {

  /**
   * Constructor
   * @param text to display
   * @param isRoot true if it's the shown root node
   * @param isLeaf true if no children
   */
  public NodePane(final String text, boolean isRoot, boolean isLeaf) {

    HBox pane = new HBox(5.0);
    ImageView image;
    if (isRoot) {
      image = new ImageView(new Image("imgs/root.png", true));
    }
    else if (isLeaf) {
      image = new ImageView(new Image("imgs/emptynode.png", true));
    }
    else {
      image = new ImageView(new Image("imgs/node.gif", true));
    }
    pane.getChildren().add(image);

    Text txt = new Text(text);
    pane.getChildren().add(txt);

    this.getChildren().add(pane);
  }

}
