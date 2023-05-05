package org.motorbrot.javafxjcrbrowser;

import java.util.logging.Logger;
import org.motorbrot.javafxjcrbrowser.services.JcrService;
import org.motorbrot.javafxjcrbrowser.services.SpringDIConfiguration;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Starting point for javaFx
 */
@SpringBootApplication
public class JavaFxJcrBrowserApplication extends Application {

  static final String MAIN_FXML_RESOURCE_PATH = "/fxml/JcrBrowserScene.fxml";
  private static final Logger LOG = Logger.getLogger(JavaFxJcrBrowserApplication.class.getName());

  private ConfigurableApplicationContext springContext;
  
  @Override
  public void start(Stage stage) throws Exception {

    LOG.info("Starting spring context ....");
    // run spring-context
    
    springContext = new SpringApplicationBuilder(JavaFxJcrBrowserApplication.class)
      .web(WebApplicationType.NONE)
      .build()
      .run(SpringDIConfiguration.class);
      
    // load fxml
    FXMLLoader fxmlLoader = new FXMLLoader();

    // inject spring services into fxml controllers
    
    // very short
    // fxmlLoader.setControllerFactory(springContext::getBean);
    
    // short
    // fxmlLoader.setControllerFactory((Class<?> p) -> {
    // return springContext.getBean(p);
    // });

    // long version 

    LOG.info("Set ControllerFactory");
    fxmlLoader.setControllerFactory(new Callback<Class<?>, Object>() {
      @Override
      public Object call(final Class<?> param) {
        return springContext.getBean(param);
      }
    });
    LOG.info("Load fxml");
    fxmlLoader.setLocation(getClass().getResource(MAIN_FXML_RESOURCE_PATH));

    Parent root = fxmlLoader.load();
    Scene scene = new Scene(root);
    scene.getStylesheets().add("/styles/Styles.css");

    stage.setTitle("OpenJFX Jcr Browser");
    stage.setScene(scene);
    stage.show();
  }

  @Override
  public void stop() throws Exception {
    JcrService jcr = springContext.getBean(JcrService.class);
    jcr.logout();
    springContext.stop();
  }
  
  /**
   * jPro needs main-class extends JavaFx Application
   * @param args app params
   */
  public static void main(String[] args) {
    launch(args);
    System.exit(0);
  }
  
}
