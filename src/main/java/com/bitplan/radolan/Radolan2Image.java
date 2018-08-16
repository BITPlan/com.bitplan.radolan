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

import java.awt.Point;
import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bitplan.geo.UnLocode;
import com.bitplan.geo.UnLocodeManager;

import cs.fau.de.since.radolan.Composite;
import cs.fau.de.since.radolan.DPoint;
import cs.fau.de.since.radolan.FloatFunction;
import cs.fau.de.since.radolan.IPoint;
import cs.fau.de.since.radolan.Translate;
import cs.fau.de.since.radolan.vis.Vis;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.Node;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

@SuppressWarnings("restriction")
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
   * @return - the image
   * @throws Exception
   */
  public static Image getImage(Composite comp) throws Exception {
    float max = 400f;
    FloatFunction<Color> heatmap = Vis.RangeMap(Vis.DWD_Style_Colors);
    Duration interval = comp.getInterval();
    switch (comp.getDataUnit()) {
    case Unit_mm:
      /**
       * http://www.wetter-eggerszell.de/besondere-wetterereignisse/wetter-und-klima/wetterrekorde-deutschland--und-weltweit/index.html
       * Höchste 24-Stunden-Menge (07-07 MEZ): 312mm am 12./13.08.02 in Zinnwald-Georgenfeld (Erzgebirge)
       * Größte Tagesniederschlagsmenge: 260mm am 06.07.1954 in Stein (Kreis Rosenheim)
       */
      max = 200.0f;
      if (interval.compareTo(Duration.ofHours(1)) < 0) {
        max = 100.0f;
      }
      if (interval.compareTo(Duration.ofDays(7)) > 0) {
        max = 400.0f;
      }
      break;
    case Unit_dBZ:
      heatmap = Vis.HeatmapReflectivity;
      break;
    case Unit_km:
      heatmap = Vis.Graymap(0, 15, Vis.Id);
      break;
    case Unit_mps:
      heatmap = Vis.HeatmapRadialVelocity;
      break;
    default:
      break;
    }
    WritableImage image = getImage(comp, heatmap);
    // draw borders
    if (comp.isHasProjection()) {
      drawBorders(comp, "2_bundeslaender/2_hoch.geojson", image);
      drawMesh(comp, image);
    }
    return image;
  }

  /**
   * draw Borders
   * 
   * @param comp
   * @param image
   * @throws Exception
   */
  protected static void drawBorders(Composite comp, String borderName,
      WritableImage image) throws Exception {
    String msg=String.format("detected grid: %.1f km * %.1f km\n",
        comp.getDx() * comp.getRx(), comp.getDy() * comp.getRy());
    if (debug)
      LOGGER.log(Level.INFO, msg);
    Borders borders = new Borders(borderName); // "1_deutschland/3_mittel.geojson"
    for (DPoint point : borders.getPoints()) {
      DPoint dp = comp.translate(point.x, point.y);
      IPoint ip = new IPoint(dp);
      if (ip.x > 0 && ip.y > 0) {
        image.getPixelWriter().setColor(ip.x, ip.y, borderColor);
      }
    }
  }

  /**
   * draw a coordinate mesh
   * 
   * @param comp
   *          - the composite to draw the mesh for
   * @param image
   */
  protected static void drawMesh(Composite comp, WritableImage image) {
    // draw mesh
    // loop over east and north
    for (double e = 1.0; e < 16.0; e += 0.1) {
      for (double n = 46.0; n < 55.0; n += 0.1) {
        double edist = Math.abs(e - Math.round(e));
        double ndist = Math.abs(n - Math.round(n));
        if ((edist < 0.01) || (ndist < 0.01)) {
          DPoint dp = comp.translate(n, e);
          IPoint ip = new IPoint(dp);
          if (ip.x >= 0 && ip.y >= 0 && ip.x < comp.getPx()
              && ip.y < comp.getPy())
            image.getPixelWriter().setColor(ip.x, ip.y, meshColor);
        }
      }
    }

  }

  /**
   * get the Image for the given composite and color map
   * 
   * @param c
   *          - the composite Radolan input
   * @param colorMap
   * @return - the image
   */
  public static WritableImage getImage(Composite c,
      FloatFunction<Color> colorMap) {
    int width = c.getPx();
    int height = c.getPy();
    WritableImage img = new WritableImage(width, height);
    PixelWriter pw = img.getPixelWriter();
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        float value = c.getValue(x, y);
        Color color = colorMap.apply(value);
        pw.setColor(x, y, color);
      }
    }
    return img;
  }

  /**
   * activate the onShow Event of the given tooltip
   * 
   * @param composite
   * @param toolTip
   */
  public static void activateToolTipOnShowEvent(Composite composite, Node view,
      Tooltip toolTip) {
    Bounds viewBounds = view.getBoundsInParent();
    String imsg = String.format(
        "ToolTip onShowEvent installed in a view with size %.0f x %.0f for composite %d x %d",
        viewBounds.getWidth(), viewBounds.getHeight(), composite.getPx(),
        composite.getPy());
    if (debug)
      LOGGER.log(Level.INFO, imsg);
    else if (viewBounds.getWidth() != composite.getPx()
        || viewBounds.getHeight() != composite.getPy()) {
      LOGGER.log(Level.WARNING, imsg);
    }
    // https://stackoverflow.com/a/39712217/1497139
    toolTip.setOnShowing(ev -> {
      // called just prior to the toolTip being shown
      // get the mouse location
      Point mouse = java.awt.MouseInfo.getPointerInfo().getLocation();
      // convert it to the view (we assume the views size is the composite's
      // grid size)
      Point2D local = view.screenToLocal(mouse.x, mouse.y);
      // get the precipitation value for this point
      float value = composite.getValue((int) local.getX(), (int) local.getY());
      // get the location of the point as lat/lon
      DPoint p = Translate.translateXYtoLatLon(composite,
          new DPoint(local.getX(), local.getY()));
      // find the closest cities:
      long startTime = System.nanoTime();
      Map<Double, UnLocode> closestCities = UnLocodeManager.getInstance().lookup(p.x, p.y, 20);
      long endTime = System.nanoTime();
      long duration = (endTime - startTime)/100000;
      if (debug)
        LOGGER.log(Level.INFO,String.format("city lookup took %d msecs and returned %d results",duration,closestCities.size()));
      String cityInfo="";
      if (closestCities.size()>0) {
        Entry<Double, UnLocode> cityEntry = closestCities.entrySet().iterator().next();
        cityInfo=String.format(" near %s (%.1f km)", cityEntry.getValue().getName(),cityEntry.getKey());
      }
      String displayMsg = String.format("%.1f %s%s %s", value,
          composite.getDataUnit(),cityInfo, p.toFormattedDMSString());
      String msg = String.format("%.0f,%.0f -> %s", local.getX(), local.getY(),
          displayMsg);
      if (debug)
        LOGGER.log(Level.INFO, msg);
      toolTip.setText(displayMsg);
    });

  }
}
