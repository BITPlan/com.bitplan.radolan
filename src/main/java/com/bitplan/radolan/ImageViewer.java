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
package com.bitplan.radolan;

import com.bitplan.display.MapView;
import com.bitplan.javafx.WaitableApp;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * inspired by
 * https://docs.oracle.com/javase/8/javafx/api/javafx/scene/image/ImageView.html
 * 
 * @author wf
 *
 */
public class ImageViewer extends WaitableApp {
  
  private String title = "Image Viewer";
  private DisplayContext displayContext;
  private MapView mapView;
  private Scene scene;

  /**
   * construct me from a DisplayContext
   * 
   * @param displayContext
   */
  public ImageViewer(DisplayContext displayContext) {
    this.displayContext = displayContext;
    this.title = displayContext.title;
    this.mapView = displayContext.mapView;
  }

  public ImageViewer() {
  }

  @Override
  public void start(Stage stage) {
    super.start(stage);
    if (displayContext != null) {
      mapView = displayContext.mapView;
    }
    // load the image
    if (mapView == null)
      mapView = new MapView("https://www.dwd.de/DWD/wetter/radar/rad_brd_akt.jpg");

    // container with a fill property
    scene = new Scene(mapView.getStackPane());
    scene.setFill(Color.WHITE);

    // inform the display context
    if (displayContext != null) {
      stage.setWidth(mapView.getWidth());
      stage.setHeight(mapView.getHeight());
    }
    stage.setTitle(title);
    stage.setScene(scene);
    stage.sizeToScene();
    stage.show();
    if (debug)
      mapView.getStackPane().setOnMouseClicked(event -> {
        showSizes();
      });
    // JavaFX: stage's minHeight considering titlebar's height
    // https://stackoverflow.com/a/43346746/1497139
    // stage.setMinHeight(stage.getHeight());
    // stage.setMinWidth(stage.getWidth());
    // How to make javafx.scene.Scene resize while maintaining an aspect ratio?
    // https://stackoverflow.com/a/18638505/1497139
    // stage.minWidthProperty().bind(scene.heightProperty());
    // stage.minHeightProperty().bind(scene.widthProperty());
  }

  /**
   * show the sizes of stage, scene, imageView and Image
   */
  public void showSizes() {
    System.out.println("Sizes: ");
    ObservableList<Screen> screens = Screen.getScreensForRectangle(stage.getX(),
        stage.getY(), stage.getWidth(), stage.getHeight());
    for (Screen screen : screens) {
      Rectangle2D s = screen.getVisualBounds();
      showSize(screen, s.getWidth(), s.getHeight());
    }
    showSize(stage, stage.getWidth(), stage.getHeight());
    showSize(scene, scene.getWidth(), scene.getHeight());
    ImageView imageView = mapView.getImageView();
    showSize(imageView, imageView.getFitWidth(), imageView.getFitHeight());
    // showSize(borderPane,borderPane.getWidth(),borderPane.getHeight());
    Pane drawPane = mapView.getDrawPane();
    showSize(drawPane, drawPane.getWidth(), drawPane.getHeight());
    Image image = mapView.getImage();
    showSize(image, image.getHeight(), image.getWidth());
  }

  public void showSize(Object o, double width, double height) {
    System.out.println(String.format("%s %.0f x %.0f",
        o.getClass().getSimpleName(), width, height));
  }

  public void close() {
    super.close();
    // if (!testMode)
    // System.exit(0);
  }

  @Override
  public void stop() {
    // https://docs.oracle.com/javase/8/javafx/api/javafx/application/Application.html
    System.err.println("ImageViewer stopped");
  }

  /**
   * launch me with the given arguments
   * 
   * @param args
   */
  public void maininstance(String[] args) {
    toolkitInit();
    // System.err.println("imageViewer created - now showing");
    show();
    // System.err.println("imageViewer showed - now waiting to open");
    waitOpen();
    // System.err.println("imageViewer opened - now waiting to close");
    waitClose();
    // System.err.println("imageViewer closed");
  }

  /**
   * launch me with the given arguments
   * 
   * @param args
   */
  public static void main(String[] args) {
    ImageViewer imageViewer = new ImageViewer();
    imageViewer.maininstance(args);
  }

}