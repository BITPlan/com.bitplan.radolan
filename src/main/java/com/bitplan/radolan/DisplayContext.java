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

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bitplan.geo.UnLocode;
import com.bitplan.geo.UnLocodeManager;

import cs.fau.de.since.radolan.Composite;
import cs.fau.de.since.radolan.FloatFunction;
import cs.fau.de.since.radolan.vis.Vis;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * Display context
 * has the details about displaying a RADOLAN composite image
 * 
 * @author wf
 *
 */
public class DisplayContext {
  // prepare a LOGGER
  protected static Logger LOGGER = Logger.getLogger("com.bitplan.radolan");
  
  public static boolean debug=false;
  
  /**
   * the composite RADOLAN data to be displayed
   */
  Composite composite; 
  
  /**
   * the image (will be writable if processed from a composite)
   */
  Image image;
  
  /**
   * the view in which the image is contained
   */
  ImageView imageView;
  
  /**
   * the title to use for the display
   */
  String title;
  
  /**
   * label to display infos
   */
  Label infoLabel;
  
  /**
   * the name of the border to use - will be read from a json file
   */
  String borderName;
  
  /**
   * the pane on which the borders and mesh might be shown
   */
  // Pane borderPane;
  
  /**
   * the pane on which we might draw extra information
   * drawPane.clear() is allowed
   */
  Pane drawPane;
  
  /**
   * the visual transformation function of data to colors
   * the default heatMap is DWD style - same colors as on DWD internet site
   */
  FloatFunction<Color> heatmap = Vis.RangeMap(Vis.DWD_Style_Colors);
  
  /**
   * the zoom area in km
   */
  double zoomKm;
  
  /**
   * the location which is a the center of the zoom
   */
  UnLocode location;

  /**
   * maximum expected value
   */
  float max=400.0f;

  /**
   * create a DisplayContext
   * 
   * @param composite
   * @param borderName
   * @param zoomKm
   * @param locationName
   */
  public DisplayContext(Composite composite, String borderName, double zoomKm, String locationName) {
    this.composite = composite;
    this.borderName = borderName;
    this.zoomKm=zoomKm;
    setUpUnitsAndHeatMap();
    // lookup location
    if (locationName!=null) {
      UnLocodeManager ulm=UnLocodeManager.getInstance();
      this.location=ulm.lookup(locationName);
      if (location==null) {
        LOGGER.log(Level.WARNING,"could not find location "+locationName);
      }
    }
  }

  /**
   * setup the units and the heatmap
   */
  private void setUpUnitsAndHeatMap() {
    Duration interval = composite.getInterval();
    switch (composite.getDataUnit()) {
    case Unit_mm:
      /**
       * http://www.wetter-eggerszell.de/besondere-wetterereignisse/wetter-und-klima/wetterrekorde-deutschland--und-weltweit/index.html
       * Höchste 24-Stunden-Menge (07-07 MEZ): 312mm am 12./13.08.02 in
       * Zinnwald-Georgenfeld (Erzgebirge) Größte Tagesniederschlagsmenge: 260mm
       * am 06.07.1954 in Stein (Kreis Rosenheim)
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
    
  }

  /**
   * get the writeAbleImage (if available)
   * @return the writeAbleImage or null if the image is not writeAble
   */
  public WritableImage getWriteableImage() {
    if (image instanceof WritableImage) {
      return (WritableImage) image;
    } else
      return null;
  }

  public void replaceImage(WritableImage img) {
    // imageView.fitHeightProperty().unbind();
    // imageView.fitWidthProperty().unbind();
    imageView.setImage(image);  
  }
}
