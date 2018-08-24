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

import java.io.File;
import java.util.logging.Level;

import org.junit.Test;

import com.bitplan.display.BorderDraw;
import com.bitplan.display.MapView;
import com.bitplan.geo.Borders;
import com.bitplan.geo.Projection;
import com.bitplan.geo.ProjectionImpl;
import com.bitplan.javafx.SampleApp;

import cs.fau.de.since.radolan.Translate;
import javafx.application.Platform;
import javafx.scene.paint.Color;

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

  @Test
  public void testBorders() throws Exception {
    for (String name : names) {
      Borders borders = new Borders(name);
      if (debug)
        LOGGER.log(Level.INFO, String.format("border %s has %d points", name,
            borders.getPoints().size()));
    }
  }

  static int SHOW_TIME = 4* 1000; // millisecs

  @Test
  public void testDrawingBorders() throws Exception {
    Borders.debug=true;
    File imageFile = new File("src/test/data/image/rad_brd900x900.png");
    for (String name : names) {
      SampleApp.toolkitInit();
      MapView mapView = new MapView(imageFile.toURI().toURL().toExternalForm());
      Projection projection = new ProjectionImpl(900, 900);
      Translate.calibrateProjection(projection);
      BorderDraw borderDraw = new BorderDraw(mapView, projection, name,
          Color.ORANGE);
      SampleApp sampleApp = new SampleApp("BorderPlot", mapView.getPane());
      sampleApp.show();
      sampleApp.waitOpen();
      double iwidth = mapView.getImage().getWidth();
      double iheight = mapView.getImage().getHeight();
      sampleApp.getStage().setWidth(mapView.getImage().getWidth());
      sampleApp.getStage().setHeight(mapView.getImage().getHeight());
      double width = sampleApp.getStage().getWidth();
      double height = sampleApp.getStage().getHeight();
      LOGGER.log(Level.INFO,
          String.format("stage: %.0f x %.0f image: %.0f x %.0f ", width, height,
              iwidth, iheight));
      sampleApp.getStage().setHeight(mapView.getImage().getHeight() + 61);
      mapView.addSizeListener(sampleApp.getStage());
      Platform.runLater(() -> borderDraw.drawBorders());
      Thread.sleep(SHOW_TIME);
      sampleApp.close();
      // Platform.exit();
    }
  }

}
