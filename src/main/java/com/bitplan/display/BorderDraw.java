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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bitplan.geo.Borders;
import com.bitplan.geo.DPoint;
import com.bitplan.geo.GeoProjection;
import com.bitplan.geo.IPoint;
import com.github.filosganga.geogson.model.LineString;
import com.github.filosganga.geogson.model.Point;

import cs.fau.de.since.radolan.Translate;
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
  private Color borderColor;
  private GeoProjection projection;

  /**
   * construct me
   * 
   * @param mapView
   * @param borderName
   */
  public BorderDraw(MapView mapView, GeoProjection projection, String borderName,
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
    Pane pane = mapView.getDrawPane();
    WritableImage image = mapView.getWriteableImage();
    if (image == null) {
      LOGGER.log(Level.WARNING, "can't draw Borders - image is null");
      return;
    }
    List<LineString> lineStrings = borders.getLineStrings();

    // List<DPoint> points = borders.getPoints();
    if (debug)
      LOGGER.log(Level.INFO,
          String.format("drawing %d border points", lineStrings.size()));
    int lineCount = 0;
    for (LineString lineString : lineStrings) {
      List<Double> polygonPoints = new ArrayList<Double>();

      // IPoint prevIp = null;
      for (Point point : lineString.points()) {   
        DPoint dgp = projection.translateLatLonToGrid(point.lat(), point.lon());
        IPoint igp=new IPoint(dgp);
        DPoint p =projection.translateGridToView(igp, pane.getWidth(),pane.getHeight());
        
        polygonPoints.add(p.x);
        polygonPoints.add(p.y);

        /*
         * IPoint ip = new IPoint(p); if (prevIp != null) { Line line = new
         * Line(prevIp.x, prevIp.y, ip.x, ip.y); line.setStrokeWidth(1);
         * line.setStroke(borderColor);
         * mapView.drawPane.getChildren().add(line); } prevIp = ip;
         */
        // image.getPixelWriter().setColor(ip.x, ip.y, borderColor);
      }
      double points[] = new double[polygonPoints.size()];
      for (int i = 0; i < polygonPoints.size(); i++) {
        points[i] = polygonPoints.get(i);
      }
      Polygon polygon = new Polygon(points);
      polygon.setStrokeWidth(1);
      polygon.setStroke(borderColor);
      if (lineCount % 2 == 0)
        polygon.setFill(Color.rgb(0xF8, 0xF8, 0xF8,0.2));
      else
        polygon.setFill(Color.rgb(0xFA, 0xFA, 0xFA,0.2));
      lineCount++;
      pane.getChildren().add(polygon);
    }
    if (debug)
      LOGGER.log(Level.INFO, "drawing done");
  }
}