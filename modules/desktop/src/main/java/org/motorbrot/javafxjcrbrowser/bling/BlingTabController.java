package org.motorbrot.javafxjcrbrowser.bling;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import org.motorbrot.javafxjcrbrowser.SceneIncludeController;
import org.springframework.stereotype.Component;

/**
 * FXML Controller class
 */
@Component
public class BlingTabController extends SceneIncludeController {

  @FXML
  private Pane shelfPane;
  @FXML
  private Accordion accordion;
  @FXML
  private TitledPane accordionItemShelf;
  
  private Shelf displayShelf;
  
  private String theme1Url = getClass().getResource("/styles/theme1.css").toExternalForm();
  private String theme2Url = getClass().getResource("/styles/theme2.css").toExternalForm();

  /**
   * Initializes the controller class.
   */
  @FXML
  private void initialize() {
    String msg = "- initializing bling -";
    Logger.getLogger(getClass().getName()).log(Level.INFO, msg);
    
    accordion.setExpandedPane(accordionItemShelf);
    
     // load images
    Image[] images = new Image[14];
    for (int i = 0; i < 14; i++) {
        images[i] = new Image( getClass().getResource("/imgs/animals/animal"+(i+1)+".jpg").toExternalForm(),false);
    }
    
    displayShelf = new Shelf(images);
    displayShelf.prefWidthProperty().bind(shelfPane.widthProperty());
    displayShelf.prefHeightProperty().bind(shelfPane.heightProperty());
    
    shelfPane.getChildren().add(displayShelf);
    
  }  

  /**
   * @return the shelf
   */
  public Shelf getDisplayShelf() {
    return displayShelf;
  }

  @FXML
  private void cssButton1Pressed(ActionEvent event) {
    accordionItemShelf.getScene().getStylesheets().add(theme1Url);
  }

  @FXML
  private void cssButton2Pressed(ActionEvent event) {
    accordionItemShelf.getScene().getStylesheets().add(theme2Url);
  }

  @FXML
  private void cssButton3Pressed(ActionEvent event) {
    accordionItemShelf.getScene().getStylesheets().removeAll(theme1Url, theme2Url);
  }
  
}
