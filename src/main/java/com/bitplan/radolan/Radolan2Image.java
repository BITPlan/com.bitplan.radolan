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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.bitplan.geo.UnLocode;

import cs.fau.de.since.radolan.FloatFunction;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

/**
 * transfer RADOLAN composite data to an image
 * 
 * @author wf
 *
 */
public class Radolan2Image {
  // prepare a LOGGER
  protected static Logger LOGGER = Logger.getLogger("com.bitplan.radolan");
  public static boolean debug = false;

  public static Color borderColor = Color.BROWN;
  public static Color meshColor = Color.BLACK;

  /**
   * get the Image for the given composite
   * https://www.dwd.de/DE/leistungen/radarniederschlag/rn_info/download_niederschlagsbestimmung.pdf?__blob=publicationFile&v=4
   * 
   * @param displayContext
   *          - the container for the image and it's details
   * @throws Exception
   */
  public static void getImage(DisplayContext displayContext) throws Exception {
    // get the Image for the displayContext
    Platform.runLater(() -> {
      getImageContent(displayContext);
      // draw borders and mesh if asked for
      if (displayContext.composite.isProjection()) {
        drawBorders(displayContext);
        drawMesh(displayContext);
      }
    });
  }

  /**
   * draw Borders
   * 
   * @param displayContext
   *          - the image and it's details
   */
  protected static void drawBorders(DisplayContext displayContext) {
    /*
     * Pane borderPane = displayContext.borderPane; if (borderPane == null)
     * return; Platform.runLater(() -> { borderPane.getChildren().clear(); if
     * (debug) { drawCross(borderPane, 2, Color.rgb(0xff, 0x00, 0x00, 0.5)); }
     * });
     */
    RadarImage comp = displayContext.composite;
    WritableImage image = displayContext.getWriteableImage();
    String msg = String.format("detected grid: %.1f km * %.1f km\n",
        comp.getGridWidth() * comp.getResX(),
        comp.getGridHeight() * comp.getResY());
    if (debug)
      LOGGER.log(Level.INFO, msg);
    Borders borders = new Borders(displayContext.borderName);
    IPoint prevIp = null;
    for (DPoint latlon : borders.getPoints()) {
      DPoint p = displayContext.composite.translateLatLonToGrid(latlon.x,
          latlon.y);
      IPoint ip = new IPoint(p);
      // getScreenPointForLatLon(displayContext,borderPane,point);
      double dist = ip.dist(prevIp);
      if ((ip.x > 0 && ip.y > 0) && (dist < 30)) {
        /*
         * Line line = new Line(prevIp.x, prevIp.y, ip.x, ip.y);
         * line.setStrokeWidth(2); line.setStroke(borderColor);
         * Platform.runLater(() -> { borderPane.getChildren().add(line); });
         */
        image.getPixelWriter().setColor(ip.x, ip.y, borderColor);
      }
      prevIp = ip;
    }
  }

  /**
   * draw a coordinate mesh
   * 
   * @param displayContext
   *          - the image and it's details
   */
  protected static void drawMesh(DisplayContext displayContext) {
    RadarImage comp = displayContext.composite;
    WritableImage image = displayContext.getWriteableImage();
    // draw mesh
    // loop over east and north
    for (double e = 1.0; e < 16.0; e += 0.1) {
      for (double n = 46.0; n < 55.0; n += 0.1) {
        double edist = Math.abs(e - Math.round(e));
        double ndist = Math.abs(n - Math.round(n));
        if ((edist < 0.01) || (ndist < 0.01)) {
          DPoint dp = comp.translateLatLonToGrid(n, e);
          IPoint ip = new IPoint(dp);
          if (ip.x >= 0 && ip.y >= 0 && ip.x < comp.getGridWidth()
              && ip.y < comp.getGridHeight())
            image.getPixelWriter().setColor(ip.x, ip.y, meshColor);
        }
      }
    }
  }

  /**
   * get the Image for the given composite and color map
   * 
   * @param displayContext
   */
  public static void getImageContent(DisplayContext displayContext) {
    RadarImage c = displayContext.composite;
    FloatFunction<Color> colorMap = displayContext.heatmap;
    int width = c.getGridWidth();
    int height = c.getGridHeight();
    WritableImage img = displayContext.getWriteableImage();
    if (img == null)
      return;
    PixelWriter pw = img.getPixelWriter();
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        float value = c.getValue(x, y);
        Color color = colorMap.apply(value);
        pw.setColor(x, y, color);
      }
    }
  }

  /**
   * activate the onShow Event of the given tooltip
   * 
   * @param displayContext
   *          - details of the image e.g. view and tooltip
   */
  public static void activateEvents(DisplayContext displayContext) {
    ImageView imageView = displayContext.imageView;
    RadarImage composite = displayContext.composite;
    Pane drawOnGlass = displayContext.drawPane;
    
    Bounds viewBounds = imageView.getBoundsInParent();
    String imsg = String.format(
        "events activated in a view with size %.0f x %.0f for composite %d x %d",
        viewBounds.getWidth(), viewBounds.getHeight(), composite.getGridWidth(),
        composite.getGridHeight());
    if (debug)
      LOGGER.log(Level.INFO, imsg);

    // initial addLocation
    Platform.runLater(() -> addLocation(displayContext));

    //  handle a mouseclick
    displayContext.drawPane.setOnMouseClicked(event -> {
      // view point
      DPoint vp = new DPoint(event.getSceneX(), event.getSceneY());
      // grid point
      IPoint gp = composite.translateViewToGrid(vp, viewBounds.getWidth(),
          viewBounds.getHeight());
      Zoom zoom=new Zoom(displayContext);
      String text=zoom.arm(gp,vp);
      Circle circle = drawCircleWithText(displayContext.drawPane, text, 4, Color.BLUE, vp.x, vp.y);
      zoom.popOver.show(circle);
    });
   
    ChangeListener<Number> sizeListener = (observable, oldValue, newValue) -> {
      // too slow
      // drawBorders(displayContext);
      addLocation(displayContext);
    };
    drawOnGlass.widthProperty().addListener(sizeListener);
    drawOnGlass.heightProperty().addListener(sizeListener);
  }

  /**
   * add a location if there is one
   * 
   * @param displayContext
   */
  public static void addLocation(DisplayContext displayContext) {
    // make sure the necessary prerequisites are there
    if (displayContext.location == null || displayContext.composite == null)
      return;
    Pane drawOnGlass = displayContext.drawPane;
    // clear the drawPane
    drawOnGlass.getChildren().clear();
    UnLocode loc = displayContext.location;
    DPoint latlon = new DPoint(loc.getLat(), loc.getLon());
    DPoint gpd = displayContext.composite.translateLatLonToGrid(latlon.x,
        latlon.y);
    IPoint gp = new IPoint(gpd);
    double value = displayContext.composite.getValue(gp.x, gp.y);
    // Position now needs to be adapted to screen size
    DPoint vp = displayContext.composite.translateGridToView(gp, drawOnGlass.getWidth(),
        drawOnGlass.getHeight());
    String text = String.format("%s - %.1f mm", loc.getName(), value);
    Circle circle = drawCircleWithText(displayContext.drawPane, text, 4, Color.BLUE, vp.x, vp.y);
    Zoom zoom=new Zoom(displayContext);
    zoom.arm(gp, vp);
    zoom.triggerOnMouseEntered(circle);
  }

  /**
   * draw a circle with given text on the given pane
   * 
   * @param pane
   * @param text
   * @param radius
   * @param color
   * @param x
   * @param y
   */
  public static Circle drawCircleWithText(Pane pane, String text, double radius,
      Color color, double x, double y) {
    Circle circle = new Circle();
    circle.setRadius(radius);
    circle.setFill(color);
    circle.setTranslateX(x);
    circle.setTranslateY(y);

    Label label = new Label(text);
    label.setTranslateX(x + radius);
    label.setTranslateY(y + radius);
    label.setTextFill(color);
    pane.getChildren().addAll(circle, label);
    return circle;
  }

  /**
   * draw a cross on the given pane with the given stroke width and color
   * 
   * @param pane
   * @param strokeWidth
   * @param color
   */
  public static void drawCross(Pane pane, double strokeWidth, Color color) {
    double w = pane.getWidth();
    double h = pane.getHeight();
    Line line = new Line(0, 0, w, h);
    line.setStrokeWidth(strokeWidth);
    line.setStroke(color);
    Line line2 = new Line(w, 0, 0, h);
    line2.setStrokeWidth(strokeWidth);
    line2.setStroke(color);
    pane.getChildren().addAll(line, line2);
  }
}
