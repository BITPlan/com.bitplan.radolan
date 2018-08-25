/**
 * Copyright (c) 2018 BITPlan GmbH
 *
 * http://www.bitplan.com
 *
 * This file is part of the Opensource project at:
 * https://github.com/BITPlan/com.bitplan.radolan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Parts which are derived from https://gitlab.cs.fau.de/since/radolan are also
 * under MIT license.
 */
package com.bitplan.display;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * a Map View
 * 
 * @author wf
 *
 */
public class MapView {
  public static boolean debug = false;

  protected static Logger LOGGER = Logger.getLogger("com.bitplan.display");
  ImageView imageView;
  private Image image;
  Pane drawPane;
  private StackPane stackPane;

  /**
   * construct me from the given url
   * 
   * @param url
   */
  public MapView(String url) { 
    getImageView(url);
    drawPane = new Pane();
    drawPane.setStyle(
        "-fx-background-color: rgba(240, 240, 240, 0.05); -fx-background-radius: 10;");

    /*
     * borderPane =new Pane(); borderPane.setStyle(
     * "-fx-background-color: rgba(255, 128, 0, 0.05); -fx-background-radius: 10;"
     * );
     */
    stackPane = new StackPane();
    StackPane.setAlignment(imageView, Pos.CENTER);
    stackPane.getChildren().addAll(imageView, drawPane);
  }

  public Pane getPane() {
    return stackPane;
  }

  /**
   * get my ImageView
   * 
   * @param url
   * @return the imageView
   */
  public ImageView getImageView(String url) {
    // load the image
    if (url != null)
      setImage(new Image(url));

    // simple displays ImageView the image as is
    imageView = new ImageView();
    imageView.setImage(getImage());
    imageView.setSmooth(true);
    imageView.setCache(true);
    return imageView;
  }

  public Image getImage() {
    return image;
  }

  public void setImage(Image image) {
    this.image = image;
  }

  /**
   * add a size Listener
   */
  public void addSizeListener(ObservableValue<Number> widthProperty,
      ObservableValue<Number> heightProperty) {
    ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> {
      imageView.setFitWidth((double) newValue);
      if (debug)
        LOGGER.log(Level.INFO,
            String.format("width %.0f->%.0f", oldValue, newValue));
    };
    ChangeListener<Number> heightListener = (observable, oldValue,
        newValue) -> {
      imageView.setFitHeight((double) newValue);
      if (debug)
        LOGGER.log(Level.INFO,
            String.format("height %.0f->%.0f", oldValue, newValue));
    };
    widthProperty.addListener(widthListener);
    heightProperty.addListener(heightListener);
  }

  /**
   * get the writeAbleImage (if available)
   * 
   * @return the writeAbleImage or null if the image is not writeAble
   */
  public WritableImage getWriteableImage() {
    if (getImage() instanceof WritableImage) {
      return (WritableImage) getImage();
    } else {
      if (debug)
        LOGGER.log(Level.INFO,
            "image is not writeable will create a writeable copy");
      WritableImage copyImage = copyImage(getImage());
      setImage(copyImage);
      imageView.setImage(getImage());
      return copyImage;
    }
  }

  /**
   * copy the given image to a writeable image
   * 
   * @param image
   * @return a writeable image
   */
  public static WritableImage copyImage(Image image) {
    int height = (int) image.getHeight();
    int width = (int) image.getWidth();
    PixelReader pixelReader = image.getPixelReader();
    WritableImage writableImage = new WritableImage(width, height);
    PixelWriter pixelWriter = writableImage.getPixelWriter();

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        Color color = pixelReader.getColor(x, y);
        pixelWriter.setColor(x, y, color);
      }
    }
    return writableImage;
  }

  /**
   * add a size listener for the given stage
   * 
   * @param stage
   */
  public void addSizeListener(Stage stage) {
    this.addSizeListener(stage.widthProperty(), stage.heightProperty());
  }
}