package org.motorbrot.javafxjcrbrowser.bling;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.ScrollBar;
//import javafx.scene.effect.PerspectiveTransform;
//import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 *  Copied from netbeans Display-Shelf example
 *  https://github.com/pakoito/javafxmobile-plugin-ensemble/blob/master/src/main/java/ensemble/samples/graphics2d/displayshelf/DisplayShelf.java
 *  Hacky extention to add mode images
 */
public class Shelf extends Region {

  private static final Duration DURATION = Duration.millis(500);
  private static final Interpolator INTERPOLATOR = Interpolator.EASE_BOTH;
  private static final double SPACING = 50;
  private static final double LEFT_OFFSET = -110;
  private static final double RIGHT_OFFSET = 110;
  private static final double SCALE_SMALL = 0.7;
  private List<PerspectiveImage> items;
  private Group centered = new Group();
  private Group left = new Group();
  private Group center = new Group();
  private Group right = new Group();
  private int centerIndex = 0;
  private Timeline timeline;
  private ScrollBar scrollBar = new ScrollBar();
  private boolean localChange = false;
  private Rectangle clip = new Rectangle();

  /**
   * Constructor
   * @param images initial content 
   */
  public Shelf(Image[] images) {
    // set clip
    setClip(clip);
    // set background gradient using css
    setStyle("-fx-background-color: linear-gradient(to bottom,"
            + " black 60, #141414 60.1%, black 100%);");
    // style scroll bar color
    scrollBar.setStyle("-fx-base: #777777; -fx-background: #202020;");
    // create items
    items = new ArrayList<PerspectiveImage>(images.length);
    for (int i = 0; i < images.length; i++) {
      Image image = images[i];

      addImageInitially(image);
    }
    // setup scroll bar
    scrollBar.setMax(items.size() - 1);
    scrollBar.setVisibleAmount(1);
    scrollBar.setUnitIncrement(1);
    scrollBar.setBlockIncrement(1);
    scrollBar.valueProperty().addListener(new InvalidationListener() {
      public void invalidated(Observable ov) {
        if (!localChange) {
          shiftToCenter(items.get((int)scrollBar.getValue()));
        }
      }
    });
    // create content
    centered.getChildren().addAll(left, right, center);
    getChildren().addAll(centered, scrollBar);
    // listen for keyboard events
    setFocusTraversable(true);
    setOnKeyPressed(new EventHandler<KeyEvent>() {
      public void handle(KeyEvent ke) {
        if (ke.getCode() == KeyCode.LEFT) {
          shift(1);
          localChange = true;
          scrollBar.setValue(centerIndex);
          localChange = false;
        }
        else if (ke.getCode() == KeyCode.RIGHT) {
          shift(-1);
          localChange = true;
          scrollBar.setValue(centerIndex);
          localChange = false;
        }
      }
    });
    // update
    update();
  }
  
  /**
   * @param image to add
   */
  public void addImage(Image image) {
    PerspectiveImage addedImage = addImageInitially(image);
    update();
    shiftToCenter(addedImage);
  }

  private PerspectiveImage addImageInitially(Image image) {
    PerspectiveImage perspectiveImage = new PerspectiveImage(image);
    perspectiveImage.setOnMouseClicked(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent me) {
        localChange = true;
        scrollBar.setValue(items.size() - 1);
        localChange = false;
        shiftToCenter(perspectiveImage);
      }
    });
    items.add(perspectiveImage);
    scrollBar.setMax(items.size() - 1);
    return perspectiveImage;
  }

  @Override
  protected void layoutChildren() {
    // update clip to our size
    clip.setWidth(getWidth());
    clip.setHeight(getHeight());
    // keep centered centered
    centered.setLayoutY((getHeight() - PerspectiveImage.HEIGHT) / 2);
    centered.setLayoutX((getWidth() - PerspectiveImage.WIDTH) / 2);
    // position scroll bar at bottom
    scrollBar.setLayoutX(10);
    scrollBar.setLayoutY(getHeight() - 25);
    scrollBar.resize(getWidth() - 20, 15);
  }

  private void update() {
    // move items to new homes in groups
    left.getChildren().clear();
    center.getChildren().clear();
    right.getChildren().clear();
    for (int i = 0; i < centerIndex; i++) {
      left.getChildren().add(items.get(i));
    }
    center.getChildren().add(items.get(centerIndex));
    for (int i = items.size() - 1; i > centerIndex; i--) {
      right.getChildren().add(items.get(i));
    }
    // stop old timeline if there is one running
    if (timeline != null) {
      timeline.stop();
    }
    // create timeline to animate to new positions
    timeline = new Timeline();
    // add keyframes for left items
    final ObservableList<KeyFrame> keyFrames = timeline.getKeyFrames();
    for (int i = 0; i < left.getChildren().size(); i++) {
      final PerspectiveImage it = items.get(i);
      double newX = -left.getChildren().size()
              * SPACING + SPACING * i + LEFT_OFFSET;
      keyFrames.add(new KeyFrame(DURATION,
              new KeyValue(it.translateXProperty(), newX, INTERPOLATOR),
              new KeyValue(it.scaleXProperty(), SCALE_SMALL, INTERPOLATOR),
              new KeyValue(it.scaleYProperty(), SCALE_SMALL, INTERPOLATOR),
              new KeyValue(it.angle, 45.0, INTERPOLATOR)));
    }
    // add keyframe for center item
    final PerspectiveImage centerItem = items.get(centerIndex);
    keyFrames.add(new KeyFrame(DURATION,
            new KeyValue(centerItem.translateXProperty(), 0, INTERPOLATOR),
            new KeyValue(centerItem.scaleXProperty(), 1.0, INTERPOLATOR),
            new KeyValue(centerItem.scaleYProperty(), 1.0, INTERPOLATOR),
            new KeyValue(centerItem.angle, 90.0, INTERPOLATOR)));
    // add keyframes for right items
    for (int i = 0; i < right.getChildren().size(); i++) {
      final PerspectiveImage it = items.get(items.size() - i - 1);
      final double newX = right.getChildren().size()
              * SPACING - SPACING * i + RIGHT_OFFSET;
      keyFrames.add(new KeyFrame(DURATION,
              new KeyValue(it.translateXProperty(), newX, INTERPOLATOR),
              new KeyValue(it.scaleXProperty(), SCALE_SMALL, INTERPOLATOR),
              new KeyValue(it.scaleYProperty(), SCALE_SMALL, INTERPOLATOR),
              new KeyValue(it.angle, 135.0, INTERPOLATOR)));
    }
    // play animation
    timeline.play();
  }

  private void shiftToCenter(PerspectiveImage item) {
    for (int i = 0; i < left.getChildren().size(); i++) {
      if (left.getChildren().get(i) == item) {
        int shiftAmount = left.getChildren().size() - i;
        shift(shiftAmount);
        return;
      }
    }
    if (center.getChildren().get(0) == item) {
      return;
    }
    for (int i = 0; i < right.getChildren().size(); i++) {
      if (right.getChildren().get(i) == item) {
        int shiftAmount = -(right.getChildren().size() - i);
        shift(shiftAmount);
        return;
      }
    }
  }

  /**
   * moves shelf
   * @param shiftAmount to move center image  
   */
  public void shift(int shiftAmount) {
    if (centerIndex <= 0 && shiftAmount > 0) {
      return;
    }
    if (centerIndex >= items.size() - 1 && shiftAmount < 0) {
      return;
    }
    centerIndex -= shiftAmount;
    update();
  }

}

/**
 * A Node that displays a image with some 2.5D perspective rotation around the Y
 * axis.
 */
class PerspectiveImage extends Parent {

  private static final double REFLECTION_SIZE = 0.25;
  static final double WIDTH = 200;
  static final double HEIGHT = WIDTH + (WIDTH * REFLECTION_SIZE);
  private static final double RADIUS_H = WIDTH / 2;
  private static final double BACK = WIDTH / 10;
//  private PerspectiveTransform transform = new PerspectiveTransform();
  /**
   * Angle Property
   */
  final DoubleProperty angle = new SimpleDoubleProperty(45) {
    @Override
    protected void invalidated() {
      // when angle changes calculate new transform
      double lx = (RADIUS_H - Math.sin(Math.toRadians(angle.get())) * RADIUS_H - 1);
      double rx = (RADIUS_H + Math.sin(Math.toRadians(angle.get())) * RADIUS_H + 1);
      double uly = (-Math.cos(Math.toRadians(angle.get())) * BACK);
      double ury = -uly;
//      transform.setUlx(lx);
//      transform.setUly(uly);
//      transform.setUrx(rx);
//      transform.setUry(ury);
//      transform.setLrx(rx);
//      transform.setLry(HEIGHT + uly);
//      transform.setLlx(lx);
//      transform.setLly(HEIGHT + ury);
    }
  };

  public final double getAngle() {
    return angle.getValue();
  }

  public final void setAngle(double value) {
    angle.setValue(value);
  }

  public final DoubleProperty angleModel() {
    return angle;
  }

  public PerspectiveImage(Image image) {
    ImageView imageView = new ImageView(image);
//    Reflection reflection = new Reflection();
//    reflection.setFraction(REFLECTION_SIZE);
//    imageView.setEffect(reflection);
//    setEffect(transform);
    getChildren().addAll(imageView);
  }
}
