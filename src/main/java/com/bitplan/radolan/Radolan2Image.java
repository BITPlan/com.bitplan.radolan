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

import com.bitplan.display.Draw;
import com.bitplan.geo.DPoint;
import com.bitplan.geo.GeoProjection;
import com.bitplan.geo.IPoint;
import com.bitplan.geo.UnLocode;

import cs.fau.de.since.radolan.FloatFunction;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

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
  public static Color meshColor = Color.rgb(0, 0, 0, 0.5);

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
        drawBordersMeshAndLocation(displayContext);
      }
    });
  }

  /**
   * draw Borders
   * 
   * @param displayContext
   *          - the image and it's details
   */
  protected static void drawBordersMeshAndLocation(DisplayContext displayContext) {
    displayContext.mapView.getDrawPane().getChildren().clear();
    if (displayContext.borderDraw != null)
      displayContext.borderDraw.drawBorders();
    drawMesh(displayContext);
    addLocation(displayContext);
  }

  /**
   * draw a coordinate mesh
   * 
   * @param displayContext
   *          - the image and it's details
   */
  protected static void drawMesh(DisplayContext displayContext) {
    GeoProjection proj = displayContext.composite;
    DPoint topLeft = proj.getBounds().getTopLeft();
    DPoint bottomRight = proj.getBounds().getBottomRight();
    Pane pane = displayContext.mapView.getDrawPane();
    /*
     * double meshD=1.0; for (double lon = topLeft.y; lon < bottomRight.y-meshD;
     * lon += meshD) { for (double lat = topLeft.x+meshD; lat >
     * bottomRight.x+meshD; lat -= meshD) { // FIXME Grid to View is missing
     * DPoint topLeftV = proj.translateLatLonToGrid(lat, lon); DPoint
     * bottomRightV = proj.translateLatLonToGrid(lat-meshD, lon+meshD); Polygon
     * p=new Polygon(topLeftV.x,topLeftV.y,bottomRightV.x,topLeftV.y,
     * bottomRightV.x,bottomRightV.y,topLeftV.x,bottomRightV.y);
     * p.setStrokeWidth(1); p.setFill(Color.TRANSPARENT);
     * p.getStrokeDashArray().addAll(2d,2d); p.setStroke(meshColor);
     * displayContext.mapView.getDrawPane().getChildren().add(p); } }
     */
    // WritableImage image = displayContext.mapView.getWriteableImage();
    // draw mesh
    // loop over east and north
    for (double lon = Math.round(topLeft.y-1); lon < Math.round(bottomRight.y+1); lon += 0.1) {
      for (double lat = Math.round(bottomRight.x-1); lat < Math.round(topLeft.x+1); lat += 0.1) {
        double edist = Math.abs(lon - Math.round(lon));
        double ndist = Math.abs(lat - Math.round(lat));
        if ((edist < 0.01) || (ndist < 0.01)) {
          DPoint dpG = proj.translateLatLonToGrid(lat, lon);
          IPoint ipG = new IPoint(dpG);
          DPoint ip=proj.translateGridToView(ipG, pane.getWidth(),pane.getHeight());
          if (ip.x >= 0 && ip.y >= 0 && ip.x < pane.getWidth()
              && ip.y < pane.getHeight()) {
            // image.getPixelWriter().setColor(ip.x, ip.y, meshColor);
            Circle circle = new Circle(ip.x, ip.y, 0.3, meshColor);
            circle.setStroke(meshColor);
            pane.getChildren().add(circle);
          }
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
    WritableImage img = displayContext.mapView.getWriteableImage();
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
    ImageView imageView = displayContext.mapView.getImageView();
    RadarImage composite = displayContext.composite;
    Pane drawOnGlass = displayContext.mapView.getDrawPane();

    Bounds viewBounds = imageView.getBoundsInParent();
    String imsg = String.format(
        "events activated in a view with size %.0f x %.0f for composite %d x %d",
        viewBounds.getWidth(), viewBounds.getHeight(), composite.getGridWidth(),
        composite.getGridHeight());
    if (debug)
      LOGGER.log(Level.INFO, imsg);

    // initial addLocation
    Platform.runLater(() -> addLocation(displayContext));

    // handle a mouseclick
    displayContext.mapView.getDrawPane().setOnMouseClicked(event -> {
      Pane pane = displayContext.mapView.getDrawPane();
      // view point
      DPoint mouseP = new DPoint(event.getSceneX(), event.getSceneY());
      Point2D lp = pane.sceneToLocal(mouseP.x,mouseP.y);
      DPoint vp=new DPoint(lp.getX(),lp.getY());
      // grid point
      IPoint gp = composite.translateViewToGrid(vp, pane.getWidth(),
          pane.getHeight());
      Zoom zoom = new Zoom(displayContext, 12);
      String text = zoom.arm(gp, vp);
      Circle circle = Draw.drawCircleWithText(displayContext.mapView.getDrawPane(),
          text, 4, Color.BLUE, vp.x, vp.y);
      zoom.popOver.show(circle);
    });

    ChangeListener<Number> sizeListener = (observable, oldValue, newValue) -> {
      if (oldValue.intValue()!=0) {
        drawBordersMeshAndLocation(displayContext);
      }
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
    Pane drawOnGlass = displayContext.mapView.getDrawPane();

    UnLocode loc = displayContext.location;
    DPoint latlon = new DPoint(loc.getLat(), loc.getLon());
    DPoint gpd = displayContext.composite.translateLatLonToGrid(latlon.x,
        latlon.y);
    IPoint gp = new IPoint(gpd);
    double value = displayContext.composite.getValue(gp.x, gp.y);
    // Position now needs to be adapted to screen size
    DPoint vp = displayContext.composite.translateGridToView(gp,
        drawOnGlass.getWidth(), drawOnGlass.getHeight());
    String text = String.format("%s - %.1f mm", loc.getName(), value);
    Circle circle = Draw.drawCircleWithText(displayContext.mapView.getDrawPane(),
        text, 4, Color.BLUE, vp.x, vp.y);
    if (debug)
      LOGGER.log(Level.INFO,String.format("x: %.0f y: %.0f",vp.x,vp.y));
    //Zoom zoom = new Zoom(displayContext, 12);
    //zoom.arm(gp, vp);
    //zoom.triggerOnMouseEntered(circle);
  }
}
