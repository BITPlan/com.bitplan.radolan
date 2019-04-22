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

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.values;

import java.util.HashMap;
import java.util.Map;

import org.openweathermap.weather.Coord;

import com.bitplan.geo.DPoint;

import cs.fau.de.since.radolan.FloatFunction;
import cs.fau.de.since.radolan.vis.Vis;
import cs.fau.de.since.radolan.vis.Vis.ColorRange;
import de.dwd.geoserver.Station;
import de.dwd.geoserver.StationManager;
import javafx.scene.paint.Color;

/**
 * display of evaporation
 * 
 * @author wf
 *
 */
public class EvaporationView {

  public static final ColorRange[] DWD_Style_Colors = {
      new ColorRange(6.0f, 999999.0f, Color.rgb(204, 0, 0)),
      new ColorRange(5.0f, 5.9999f, Color.rgb(220, 91, 0)),
      new ColorRange(4.0f, 4.9999f, Color.rgb(232, 174, 0)),
      new ColorRange(3.0f, 3.9999f, Color.rgb(237, 237, 0)),
      new ColorRange(2.0f, 2.9999f, Color.rgb(127, 195, 0)),
      new ColorRange(1.0f, 1.9999f, Color.rgb(68, 171, 0)),
      new ColorRange(0.0f, 0.9999f, Color.rgb(0, 139, 0)) };

  public static FloatFunction<Color> heatmap = Vis.RangeMap(DWD_Style_Colors);

  private StationManager sm;
  private BorderDraw borderDraw;
  Map<Object, Object> evapmap;
  Map<String,Station> stationmap;

  /**
   * create an evaporationView for the given stationManager and border Draw
   * 
   * @param sm
   * @param borderDraw
   */
  public EvaporationView(StationManager sm, BorderDraw borderDraw) {
    this.sm = sm;
    this.borderDraw = borderDraw;
    evapmap= sm.g().V().hasLabel("observation")
        .has("name", "evaporation").group().by("stationid")
        .by(values("value").mean()).next();
    stationmap=new HashMap<String,Station>();
    sm.g().V().hasLabel("station").forEachRemaining(v -> {
      Station s = new Station();
      s.fromVertex(v);
      stationmap.put(s.id, s);
    });
  }

  /**
   * draw the evaporation
   * @param radius
   * @param opacity
   */
  public void draw(double radius,double opacity) {
    FloatFunction<Color> evapColorMap = EvaporationView.heatmap;
    for (Station s:stationmap.values()) {
      // get the station location
      Coord coord = s.getCoord();
      // translate it to the borderDraw coordinates
      DPoint p = borderDraw.translateLatLonToView(coord.getLat(),
          coord.getLon());
      // get the evaporation for this station
      Number evap = (Number) evapmap.get(s.getId());
      // calculate the color for that evaporation
      Color evapColor = evapColorMap.apply(evap.floatValue());
      // show a circle with the given color and the the short name of the station
      Draw.drawCircleWithText(borderDraw.getPane(), s.getShortName(), radius,
          evapColor, opacity, Color.BLUE, p.x, p.y, true);
    };
  }
  
  /**
   * draw interpolated
   * @param gridx
   * @param gridy
   */
  public void drawInterpolated(int gridx,int gridy) {
    Coord nw = sm.getNorthWest();
    Coord se = sm.getSouthEast();
    double dx=(se.getLon()-nw.getLon())/gridx;
    double dy=(se.getLat()-nw.getLat())/gridy;
    
    for (double lat=nw.getLat();lat<se.getLat();lat+=dy) {
      for (double lon=nw.getLon();lon<se.getLon();lon+=dx) {
        DPoint p = borderDraw.translateLatLonToView(lat,
            lon);
        Color evapColor=Color.BLUE;
        String text=String.format("%5.1f %s\n%5.1f %s", lat,lat>=0?"N":"S",lon,lon>=0?"E":"W");
        Draw.drawCircleWithText(borderDraw.getPane(), text, 4.,
            evapColor, 0.5, Color.BLUE, p.x, p.y, true);
      }
    }
  }
}
