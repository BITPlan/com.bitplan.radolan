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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bitplan.geo.Borders;
import com.bitplan.geo.DPoint;
import com.bitplan.geo.GeoProjection;
import com.bitplan.geo.IPoint;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * Helper class to draw borders
 * 
 * @author wf
 *
 */
public class BorderDraw {
  public static boolean debug = false;

  protected static Logger LOGGER = Logger.getLogger("com.bitplan.display");

  private MapView mapView;
  private Borders borders;
  Color borderColor;
  double opacity=0.2;
  double strokeWidth=1;
  private GeoProjection projection;

  private DoubleProperty widthProperty=new SimpleDoubleProperty();
  private DoubleProperty heightProperty=new SimpleDoubleProperty();

  public GeoProjection getProjection() {
    return projection;
  }

  public void setProjection(GeoProjection projection) {
    this.projection = projection;
  }

  /**
   * @return the borders
   */
  public Borders getBorders() {
    return borders;
  }

  /**
   * @param borders the borders to set
   */
  public void setBorders(Borders borders) {
    this.borders = borders;
  }

  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public double getOpacity() {
    return opacity;
  }

  public void setOpacity(double opacity) {
    this.opacity = opacity;
  }

  /**
   * construct me
   * 
   * @param mapView
   * @param borderName
   */
  public BorderDraw(MapView mapView, GeoProjection projection, String borderName,
      Color borderColor) {
    this.mapView = mapView;
    Pane pane = mapView.getDrawPane();
    widthProperty.bind(pane.widthProperty());
    heightProperty.bind(pane.heightProperty());
    this.projection = projection;
    setBorders(new Borders(borderName));
    this.setBorderColor(borderColor);
  }
  
  /**
   * set the clipping for this mapView
   * @param clipNode
   */
  public final void setClip(Node clipNode) {
    mapView.setClip(clipNode);
  }

  public Pane getPane() {
    return  mapView.getDrawPane();
  }
  
  /**
   * translate a lat/lon value to a point in this view (pane)
   * @param lat
   * @param lon
   * @return the point
   */
  public DPoint translateLatLonToView(double lat, double lon) {
    
    DPoint dgp = projection.translateLatLonToGrid(lat,lon);
    IPoint igp=new IPoint(dgp);
    double width=widthProperty.doubleValue();
    double height=heightProperty.doubleValue();
    DPoint p =projection.translateGridToView(igp, width,height);
    return p;
  }
  
  /**
   * draw the Borders
   */
  public void drawBorders() {
    Pane pane = mapView.getDrawPane();
    WritableImage image = mapView.getWriteableImage();
    if (image == null) {
      LOGGER.log(Level.WARNING, "can't draw Borders - image is null");
      return;
    }
   
    List<Polygon> polygons=getBorders().asPolygons(strokeWidth,getBorderColor(),getOpacity(),(lat,lon)->this.translateLatLonToView(lat, lon));
    if (debug)
      LOGGER.log(Level.INFO,
          String.format("drawing %d border polygons in %.0f x %.0f", polygons.size(),pane.getWidth(),pane.getHeight()));

    pane.getChildren().addAll(polygons);
    if (debug)
      LOGGER.log(Level.INFO, "drawing done");
  }
  
  /**
   * draw a single node
   * @param node
   */
  public void draw(Node node) {
    mapView.getDrawPane().getChildren().add(node);
  }
}