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

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.controlsfx.control.PopOver;

import com.bitplan.geo.DPoint;
import com.bitplan.geo.IPoint;
import com.bitplan.geo.UnLocode;
import com.bitplan.geo.UnLocodeManager;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class Zoom {
  // prepare a LOGGER
  protected static Logger LOGGER = Logger.getLogger("com.bitplan.radolan");
  public static boolean debug = false;
  PopOver popOver;
  ImageView zoomView;
  WritableImage zoomImage;
  VBox vbox;
  Label infoLabel;
  private DisplayContext displayContext;
  private int zoomFactor;

  /**
   * create a zoom for the given display Context
   * 
   * @param displayContext
   */
  public Zoom(DisplayContext displayContext, int zoomFactor) {
    this.zoomFactor=zoomFactor;
    this.displayContext = displayContext;
    zoomView = new ImageView();
    zoomImage = new WritableImage((int) displayContext.zoomKm * zoomFactor,
        (int) displayContext.zoomKm * zoomFactor);
    zoomView.setImage(zoomImage);
    //zoomView.setFitWidth(zoomImage.getWidth());
    //zoomView.setFitHeight(zoomImage.getHeight());
    infoLabel = new Label("no info");
    infoLabel.setTextFill(Color.BLUE);
    infoLabel.setBackground(new Background(
        new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    vbox = new VBox(infoLabel,zoomView);
    popOver = new PopOver(vbox);
  }

  /**
   * arm me for the given grid point
   * 
   * @param gp
   *          - the grid point
   * @param vp
   *          - the view point
   * @return the string to describe the grid point
   */
  public String arm(IPoint gp, DPoint vp) {
    RadarImage composite = displayContext.composite;
    copyZoomContent(displayContext, gp);
    // get the precipitation value for this point
    float value = composite.getValue(gp.x, gp.y);
    // get the location of the point as lat/lon
    DPoint latlon = composite.translateGridToLatLon(new DPoint(gp.x, gp.y));
    if (debug)
      LOGGER.log(Level.INFO, String.format(Locale.UK,
          "scene %.0f,%.0f -> grid %d,%d %dx%d -> latlon %.2f,%.2f -> value %.0f mm",
          vp.x, vp.y, gp.x, gp.y, composite.getGridWidth(),
          composite.getGridHeight(), latlon.x, latlon.y, value));
    // find the closest cities:
    Map<Double, UnLocode> closestCities = UnLocodeManager.getInstance()
        .lookup(latlon.x, latlon.y, 20);
    String cityInfo = "";
    if (closestCities.size() > 0) {
      Entry<Double, UnLocode> cityEntry = closestCities.entrySet().iterator()
          .next();
      cityInfo = String.format(" near %s (%.1f km)",
          cityEntry.getValue().getName(), cityEntry.getKey());
    }
    String displayMsg = String.format("%.1f %s%s\n%s", value,
        composite.getDataUnit(), cityInfo, latlon.toFormattedDMSString());
    String msg = String.format("%.0f,%.0f -> %s", vp.x, vp.y, displayMsg);
    if (debug)
      LOGGER.log(Level.INFO, msg);
    infoLabel.setText(displayMsg);
    return displayMsg;
  }

  /**
   * copy the zoom content for the given grid point
   * @param displayContext
   * @param gp
   */
  private void copyZoomContent(DisplayContext displayContext, IPoint gp) {
    int half = (int) (displayContext.zoomKm / 2);
    PixelReader pixelReader = displayContext.mapView.getImage().getPixelReader();
    if (debug) {
      String msg=String.format("zoom %3d,%3d - %3d,%3d half=%3d",gp.x-half,gp.y-half,gp.x+half,gp.y+half,half);
      LOGGER.log(Level.INFO,msg);
    }
    for (int x = gp.x - half; x <= gp.x + half; x++) {
      for (int y = gp.y - half; y <= gp.y + half; y++) {
        if (y >= 0 && y < displayContext.composite.getGridHeight())
          if (x >= 0 && x < displayContext.composite.getGridWidth()) {
            Color color = pixelReader.getColor(x, y);
            int tx0 = x - gp.x + half;
            int ty0 = y - gp.y + half;

            for (int tx = tx0*zoomFactor; tx < tx0*zoomFactor + zoomFactor; tx++) {
              for (int ty = ty0*zoomFactor; ty < ty0*zoomFactor + zoomFactor; ty++) {
                if (tx < zoomImage.getWidth() && ty < zoomImage.getHeight())
                  zoomImage.getPixelWriter().setColor(tx, ty, color);
              }
            }
          }
      }
    }
  }

  /**
   * trigger this zoom
   * 
   * @param node
   */
  public void triggerOnMouseEntered(Node node) {
    node.setOnMouseEntered(mouseEvent -> {
      // Show PopOver when mouse enters node
      popOver.show(node);
    });

    node.setOnMouseExited(mouseEvent -> {
      // Hide PopOver when mouse exits node
      popOver.hide();
    });
  }
}