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

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.controlsfx.control.PopOver;

import com.bitplan.geo.UnLocode;
import com.bitplan.geo.UnLocodeManager;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
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

  /**
   * create a zoom for the given display Context
   * 
   * @param displayContext
   */
  public Zoom (DisplayContext displayContext) {
    this.displayContext = displayContext;
    zoomView = new ImageView();
    zoomImage = new WritableImage((int) displayContext.zoomKm * 10,
        (int) displayContext.zoomKm * 10);
    zoomView.setImage(zoomImage);
    infoLabel = new Label("no info");
    infoLabel.setTextFill(Color.BLUE);
    infoLabel.setBackground(new Background(
        new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    vbox = new VBox(infoLabel);
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
    // get the precipitation value for this point
    float value = composite.getValue(gp.x, gp.y);
    // get the location of the point as lat/lon
    DPoint latlon = composite.translateGridToLatLon(new DPoint(gp.x, gp.y));
    if (debug)
      LOGGER.log(Level.INFO, String.format(
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
    Label dinfoLabel = displayContext.infoLabel;
    dinfoLabel.setTranslateX(vp.x);
    dinfoLabel.setTranslateY(vp.y);
    return displayMsg;
  }

  /**
   * trigger this zoom
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