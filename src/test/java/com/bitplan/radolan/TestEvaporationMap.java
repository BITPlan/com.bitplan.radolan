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

import static org.junit.Assert.*;

import org.junit.Test;

import com.bitplan.display.BorderDraw;
import com.bitplan.geo.GeoProjection;
import com.bitplan.geo.ProjectionImpl;

import cs.fau.de.since.radolan.Translate;
import javafx.application.Platform;

/**
 * test a map for Evaporation data
 */
public class TestEvaporationMap extends TestBorders {

  @Test
  public void testEvaporationMap() throws Exception {
    String name="3_regierungsbezirke/3_mittel.geojson";
    GeoProjection projection = new ProjectionImpl(900, 900);
    Translate.calibrateProjection(projection);
    BorderDraw borderDraw = prepareBorderDraw(projection, name);
    Platform.runLater(() -> borderDraw.drawBorders());
    Thread.sleep(SHOW_TIME*5);
    sampleApp.close();
  }

}
