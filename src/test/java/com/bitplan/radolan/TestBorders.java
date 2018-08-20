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

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.junit.Test;

import com.bitplan.geo.Borders;
import com.bitplan.geo.DPoint;
import com.bitplan.geo.IPoint;
import com.bitplan.geo.Projection;
import com.bitplan.geo.ProjectionImpl;
import com.bitplan.javafx.SampleApp;

import cs.fau.de.since.radolan.Translate;
import javafx.application.Platform;
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
import javafx.scene.shape.Line;
import javafx.stage.Stage;

/**
 * test the border files
 * 
 * @author wf
 *
 */
public class TestBorders extends BaseTest {
  String names[] = { "1_deutschland/3_mittel.geojson",
      "1_deutschland/4_niedrig.geojson", "2_bundeslaender/2_hoch.geojson",
      "2_bundeslaender/3_mittel.geojson", "2_bundeslaender/4_niedrig.geojson",
      "3_regierungsbezirke/2_hoch.geojson",
      "3_regierungsbezirke/3_mittel.geojson",
      "3_regierungsbezirke/4_niedrig.geojson", "4_kreise/2_hoch.geojson",
      "4_kreise/3_mittel.geojson", "4_kreise/4_niedrig.geojson" };

  @Test
  public void testBorders() throws Exception {
    for (String name : names) {
      Borders borders = new Borders(name);
      if (debug)
        LOGGER.log(Level.INFO, String.format("border %s has %d points", name,
            borders.getPoints().size()));
    }
  }

  static int SHOW_TIME = 4* 1000; // millisecs

  static class MapView {
    ImageView imageView;
    Image image;
    Pane drawPane;
    private StackPane stackPane;

    /**
     * construct me from the given url
     * 
     * @param url
     */
    public MapView(String url) {
      if (url != null)
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
        image = new Image(url);

      // simple displays ImageView the image as is
      imageView = new ImageView();
      imageView.setImage(image);
      imageView.setSmooth(true);
      imageView.setCache(true);
      return imageView;
    }

    /**
     * add a size Listener
     */
    public void addSizeListener(ObservableValue<Number> widthProperty,
        ObservableValue<Number> heightProperty) {
      ChangeListener<Number> widthListener = (observable, oldValue,
          newValue) -> {
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
      if (image instanceof WritableImage) {
        return (WritableImage) image;
      } else {
        LOGGER.log(Level.INFO,
            "image is not writeable will create a writeable copy");
        WritableImage copyImage = copyImage(image);
        image = copyImage;
        imageView.setImage(image);
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

  public class BorderDraw {

    private MapView mapView;
    private Borders borders;
    private Color borderColor;
    private Projection projection;

    /**
     * construct me
     * 
     * @param mapView
     * @param borderName
     */
    public BorderDraw(MapView mapView, Projection projection, String borderName,
        Color borderColor) {
      this.mapView = mapView;
      this.projection = projection;
      borders = new Borders(borderName);
      this.borderColor = borderColor;
    }

    /**
     * draw the Borders
     */
    public void drawBorders() {
      WritableImage image = mapView.getWriteableImage();
      if (image == null) {
        LOGGER.log(Level.WARNING, "can't draw Borders - image is null");
        return;
      }
      IPoint prevIp = null;
      List<DPoint> points = borders.getPoints();
      LOGGER.log(Level.INFO, String.format("drawing %d border points",points.size()));
      for (DPoint latlon : points) {
        DPoint p = Translate.translate(projection, latlon.x, latlon.y);
        IPoint ip = new IPoint(p);
        // getScreenPointForLatLon(displayContext,borderPane,point);
        double dist = ip.dist(prevIp);
        if ((ip.x > 0 && ip.y > 0) && (dist < 40)) {
           Line line = new Line(prevIp.x, prevIp.y, ip.x, ip.y);
           line.setStrokeWidth(1); line.setStroke(borderColor);
           mapView.drawPane.getChildren().add(line);
          // image.getPixelWriter().setColor(ip.x, ip.y, borderColor);
        }
        prevIp = ip;
      }
      LOGGER.log(Level.INFO,"drawing done");
    }
   }

  @Test
  public void testDrawingBorders() throws Exception {
    Borders.debug=true;
    File imageFile = new File("src/test/data/image/rad_brd900x900.jpg");
    for (String name : names) {
      SampleApp.toolkitInit();
      MapView mapView = new MapView(imageFile.toURI().toURL().toExternalForm());
      Projection projection = new ProjectionImpl(900, 900);
      Translate.calibrateProjection(projection);
      BorderDraw borderDraw = new BorderDraw(mapView, projection, name,
          Color.ORANGE);
      SampleApp sampleApp = new SampleApp("BorderPlot", mapView.getPane());
      sampleApp.show();
      sampleApp.waitOpen();
      double iwidth = mapView.image.getWidth();
      double iheight = mapView.image.getHeight();
      sampleApp.getStage().setWidth(mapView.image.getWidth());
      sampleApp.getStage().setHeight(mapView.image.getHeight());
      double width = sampleApp.getStage().getWidth();
      double height = sampleApp.getStage().getHeight();
      LOGGER.log(Level.INFO,
          String.format("stage: %.0f x %.0f image: %.0f x %.0f ", width, height,
              iwidth, iheight));
      sampleApp.getStage().setHeight(mapView.image.getHeight() + 61);
      mapView.addSizeListener(sampleApp.getStage());
      Platform.runLater(() -> borderDraw.drawBorders());
      Thread.sleep(SHOW_TIME);
      sampleApp.close();
      // Platform.exit();
    }
  }

}
