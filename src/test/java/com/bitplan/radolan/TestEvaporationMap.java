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

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.values;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.openweathermap.weather.Coord;

import com.bitplan.display.BorderDraw;
import com.bitplan.display.Draw;
import com.bitplan.geo.DPoint;
import com.bitplan.geo.GeoProjection;
import com.bitplan.geo.ProjectionImpl;

import cs.fau.de.since.radolan.FloatFunction;
import cs.fau.de.since.radolan.Translate;
import cs.fau.de.since.radolan.vis.Vis;
import de.dwd.geoserver.Station;
import de.dwd.geoserver.StationManager;
import javafx.application.Platform;
import javafx.scene.paint.Color;

/**
 * test a map for Evaporation data
 */
public class TestEvaporationMap extends TestBorders {

  @Test
  public void testEvaporationMap() throws Exception {
    StationManager sm = StationManager.init();
    String name = "3_regierungsbezirke/3_mittel.geojson";
    GeoProjection projection = new ProjectionImpl(900, 900);
    Translate.calibrateProjection(projection);
    BorderDraw borderDraw = prepareBorderDraw(projection, name);
    FloatFunction<Color> evapColorMap = Vis.Heatmap(0.0f,
        6.0f, Vis.Id);
    Map<Object, Object> evapmap = sm.g().V().hasLabel("observation").has("name", "evaporation").group()
    .by("stationid").by(values("value").mean()).next();
    Platform.runLater(() -> borderDraw.drawBorders());
    Platform.runLater(() -> {
      sm.g().V().hasLabel("station").forEachRemaining(v -> {
        Station s = new Station();
        s.fromVertex(v);
        Coord coord = s.getCoord();
        DPoint p = borderDraw.translateLatLonToView(coord.getLat(),
            coord.getLon());
        Number evap = (Number)evapmap.get(s.getId());
        Color evapColor=evapColorMap.apply(evap.floatValue());
        Draw.drawCircleWithText(borderDraw.getPane(), s.getShortName(), 40., evapColor,0.5,Color.BLUE,p.x, p.y,true);
      });
    });
    Thread.sleep(SHOW_TIME * 100);
    sampleApp.close();
  }

}
