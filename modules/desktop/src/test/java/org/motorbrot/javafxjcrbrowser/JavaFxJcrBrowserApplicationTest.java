package org.motorbrot.javafxjcrbrowser;

import org.motorbrot.javafxjcrbrowser.JavaFxJcrBrowserApplication;
import static org.motorbrot.javafxjcrbrowser.JavaFxJcrBrowserApplication.MAIN_FXML_RESOURCE_PATH;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Tests if all fxml ids match to java @FXML Annotations
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { SpringTestConfiguration.class })
public class JavaFxJcrBrowserApplicationTest extends ApplicationTest {
  
  @Override
  public void start(Stage stage) throws Exception {
            
    ConfigurableApplicationContext springContext = new SpringApplicationBuilder(JavaFxJcrBrowserApplication.class)
      .web(WebApplicationType.NONE)
      .build()
      .run(SpringTestConfiguration.class);
    
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAIN_FXML_RESOURCE_PATH));
    fxmlLoader.setControllerFactory(springContext::getBean);
    
    Parent mainNode = fxmlLoader.load();
    stage.setScene(new Scene(mainNode));
    stage.show();
    stage.toFront();
    
    assertTrue(true, "fx:id's in fxml match to @FXML in JavaCode");
  }
  
  @Test
    void myFirstTest() {
      
      // https://github.com/TestFX/TestFX/wiki
      // Pick Gui elements by fx:id
      
      Button loginButton = lookup("#loginButton").queryAs(Button.class);
      
      assertTrue(loginButton.isVisible());
      assertEquals("LOGIN", loginButton.getText());
      
      TextField jcrPathField = lookup("#rootPathField").queryAs(TextField.class);
      jcrPathField.setText("/content/testfx/csv");
      
      clickOn(loginButton);
      TextArea debugTxt = lookup("#debugTxt").queryAs(TextArea.class);
      String debugMsg = debugTxt.getText();
      
      TabPane tabPane = lookup("#tabPane").queryAs(TabPane.class);
      tabPane.getSelectionModel().select(1);
      
      TableView<ObservableList<StringProperty>> csvTableView = lookup("#csvTableView").queryAs(TableView.class);
      String cell123 = (String)csvTableView.getColumns().get(3).getCellObservableValue(4).getValue();
      
      //sleep(5000);
      assertEquals("123", cell123);
      assertTrue(StringUtils.contains(debugMsg, "Set browser-root to: /content/testfx/csv"), "Debug message should appear in bottom-panel: " + debugMsg);
    }
    
  
}
