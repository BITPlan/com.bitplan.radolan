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
import com.bitplan.geo.IPoint;
import com.bitplan.geo.Projection;

import cs.fau.de.since.radolan.Translate;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

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
    if (debug)
      LOGGER.log(Level.INFO,
          String.format("drawing %d border points", points.size()));
    for (DPoint latlon : points) {
      DPoint p = Translate.translate(projection, latlon.x, latlon.y);
      IPoint ip = new IPoint(p);
      // getScreenPointForLatLon(displayContext,borderPane,point);
      double dist = ip.dist(prevIp);
      if ((ip.x > 0 && ip.y > 0) && (dist < 40)) {
        Line line = new Line(prevIp.x, prevIp.y, ip.x, ip.y);
        line.setStrokeWidth(1);
        line.setStroke(borderColor);
        mapView.drawPane.getChildren().add(line);
        // image.getPixelWriter().setColor(ip.x, ip.y, borderColor);
      }
      prevIp = ip;
    }
    if (debug)
      LOGGER.log(Level.INFO, "drawing done");
  }
}