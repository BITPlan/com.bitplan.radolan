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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;

import org.junit.Test;

import com.bitplan.display.BorderDraw;
import com.bitplan.display.MapView;
import com.bitplan.geo.Borders;
import com.bitplan.geo.ConvexHull;
import com.bitplan.geo.DPoint;
import com.bitplan.geo.GeoProjection;
import com.bitplan.geo.ProjectionImpl;
import com.bitplan.javafx.SampleApp;
import com.bitplan.javafx.WaitableApp;

import cs.fau.de.since.radolan.Translate;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
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
  protected SampleApp sampleApp;

  @Test
  public void testBorders() throws Exception {
    // debug = true;
    for (String name : names) {
      Borders borders = new Borders(name);
      if (debug)
        LOGGER.log(Level.INFO, String.format("border %s has %d lineStrings",
            name, borders.getLineStrings().size()));
    }
  }

  static int SHOW_TIME = 500; // millisecs

  /**
   * change the screen on which to view the results
   * 
   * @param stage
   * @param screenNumber
   */
  public void setupStageLocation(Stage stage, int screenNumber) {
    ObservableList<Screen> screens = Screen.getScreens();
    Screen screen = screens.size() <= screenNumber ? Screen.getPrimary()
        : screens.get(screenNumber);

    Rectangle2D bounds = screen.getBounds();
    boolean primary = screen.equals(Screen.getPrimary()); // WORKAROUND: this
                                                          // doesn't work nice
                                                          // in combination with
                                                          // full screen, so
                                                          // this hack is used
                                                          // to prevent going
                                                          // fullscreen when
                                                          // screen is not
                                                          // primary

    if (primary) {
      stage.setX(bounds.getMinX());
      stage.setY(bounds.getMinY());
      stage.setWidth(bounds.getWidth());
      stage.setHeight(bounds.getHeight());
      stage.setFullScreen(true);
    } else {
      stage.setX(bounds.getMinX());
      stage.setY(bounds.getMinY());
      stage.setWidth(bounds.getWidth());
      stage.setHeight(bounds.getHeight());
      stage.toFront();
    }
  }

  @Test
  public void testDrawingMap() throws Exception {
    showBorderDraw("4_kreise/2_hoch.geojson");
  }

  public BorderDraw getBorderDraw(String name) throws Exception {
    GeoProjection projection = new ProjectionImpl(900, 900);
    Translate.calibrateProjection(projection);
    BorderDraw borderDraw = prepareBorderDraw(projection, name);
    return borderDraw;
  }

  /**
   * show the given border Draw with the given name for the borders
   * 
   * @param name
   * @throws Exception
   */
  public void showBorderDraw(String name) throws Exception {
    BorderDraw borderDraw = getBorderDraw(name);
    showBorderDraw(name, borderDraw, 1);
  }

  /**
   * show the given border draw
   * 
   * @param borderDraw
   * @throws InterruptedException
   */
  public void showBorderDraw(String name, BorderDraw borderDraw, int factor)
      throws InterruptedException {
    Platform.runLater(() -> borderDraw.drawBorders());
    waitClose(name,borderDraw,factor);
  }
    
  /**
   * waitClose
   * @param name
   * @param borderDraw
   * @param factor
   * @throws InterruptedException
   */
  public void waitClose(String name, BorderDraw borderDraw, int factor) throws InterruptedException {  
    Thread.sleep(SHOW_TIME * factor);
    saveSnapShot(name, borderDraw.getPane());
    sampleApp.close();
    // Platform.exit();
  }

  @Test
  public void testDrawingBorders() throws Exception {
    // Borders.debug = true;
    for (String name : names) {
      showBorderDraw(name);
    }
  }

  @Test
  public void testConvexHullAlgorithm() throws Exception {
    DPoint points[] = new DPoint[7];
    points[0] = new DPoint(0, 3);
    points[1] = new DPoint(2, 3);
    points[2] = new DPoint(1, 1);
    points[3] = new DPoint(2, 1);
    points[4] = new DPoint(3, 0);
    points[5] = new DPoint(0, 0);
    points[6] = new DPoint(3, 3);
    ConvexHull ch = ConvexHull.fromPointArray(points);
    List<DPoint> hull = ch.getHull();
    assertEquals(4, hull.size());
    debug = true;
    if (debug)
      for (DPoint point : hull) {
        System.out.println(String.format("%.0f %.0f", point.x, point.y));
      }
    assertTrue(new DPoint(0, 3).equals(hull.get(0)));
    assertTrue(new DPoint(0, 0).equals(hull.get(1)));
    assertTrue(new DPoint(3, 0).equals(hull.get(2)));
    assertTrue(new DPoint(3, 3).equals(hull.get(3)));
  }

  @Test
  public void testConvexHull() throws Exception {
    String names[] = { "1_deutschland/4_niedrig.geojson",
        "4_kreise/4_niedrig.geojson" };
    double strokeWidth=1;
    for (String name : names) {
      BorderDraw borderDraw = getBorderDraw(name);
      Borders borders = new Borders(name);
      List<Polygon> polygons = borders.asPolygons(strokeWidth,borderDraw.getBorderColor(),
          borderDraw.getOpacity(),
          (lat, lon) -> borderDraw.translateLatLonToView(lat, lon));
      ConvexHull ch = ConvexHull.fromPolygons(polygons);
      List<DPoint> hull = ch.getHull();
      System.out.println(String.format("%s %3d -> %3d", name,
          ch.getPoints().size(), hull.size()));
    }
  }

  @Test
  public void testClipping() throws Exception {
    Debug.activateDebug();
    String name = "4_kreise/4_niedrig.geojson";
    BorderDraw borderDraw = getBorderDraw(name);
    Borders borders = borderDraw.getBorders();
    double strokeWidth=1.5;
    List<Polygon> polygons = borders.asPolygons(strokeWidth,Color.RED,
        borderDraw.getOpacity(),
        (lat, lon) -> borderDraw.translateLatLonToView(lat, lon));
    ConvexHull ch = ConvexHull.fromPolygons(polygons);
    Polygon hpolygon = ch.asPolygon();
    Rectangle clipRect = new Rectangle(500, 500);
    clipRect.setTranslateX(100);
    clipRect.setTranslateY(100);
    borderDraw.setClip(hpolygon);
    Platform.runLater(() -> borderDraw.drawBorders());
    /*for (Polygon polygon : polygons)
      Platform.runLater(() -> borderDraw.draw(polygon));
      */
    // Platform.runLater(() -> borderDraw.draw(hpolygon));
    
    waitClose(name, borderDraw, 1);
  }

  /**
   * get the MapView
   * 
   * @return
   * @throws MalformedURLException
   */
  public MapView getMapView() throws MalformedURLException {
    File imageFile = new File("src/test/data/image/empty900x900.png");
    SampleApp.toolkitInit();
    String url = imageFile.toURI().toURL().toExternalForm();
    MapView lMapView = new MapView(url);
    return lMapView;
  }

  /**
   * prepare the border drawing for the given geo projection
   * 
   * @param projection
   * @param name
   * @return - the BorderDraw
   * @throws Exception
   */
  public BorderDraw prepareBorderDraw(GeoProjection projection, String name) throws Exception {
    MapView mapView = getMapView();
    if (debug)
      System.out.println(String.format("border %s", name));
    sampleApp = new SampleApp(name, mapView.getStackPane());
    sampleApp.show();
    sampleApp.waitOpen();
    BorderDraw borderDraw = new BorderDraw(mapView, projection, name,
        Color.ORANGE);
    // display on another monitor
    // Platform.runLater(() ->this.setupStageLocation(sampleApp.getStage(), 2));
    double iwidth = mapView.getImage().getWidth();
    double iheight = mapView.getImage().getHeight();
    sampleApp.getStage().setWidth(mapView.getImage().getWidth());
    sampleApp.getStage().setHeight(mapView.getImage().getHeight());
    double width = sampleApp.getStage().getWidth();
    double height = sampleApp.getStage().getHeight();
    if (debug)
      LOGGER.log(Level.INFO,
          String.format("stage: %.0f x %.0f image: %.0f x %.0f ", width, height,
              iwidth, iheight));
    sampleApp.getStage().setHeight(mapView.getImage().getHeight() + 61);
    mapView.addSizeListener(sampleApp.getStage());
    return borderDraw;
  }

  /**
   * save a snapShot
   * 
   * @param name
   * @param pane
   */
  public void saveSnapShot(String name, Pane pane) {
    File snapShot = new File("/tmp/" + name.replace("/", "_") + ".png");
    Platform.runLater(() -> WaitableApp.saveAsPng(pane, snapShot));
  }

}
