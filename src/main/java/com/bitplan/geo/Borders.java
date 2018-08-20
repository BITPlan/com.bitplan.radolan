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
package com.bitplan.geo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.github.filosganga.geogson.gson.GeometryAdapterFactory;
import com.github.filosganga.geogson.model.Feature;
import com.github.filosganga.geogson.model.FeatureCollection;
import com.github.filosganga.geogson.model.Geometry;
import com.github.filosganga.geogson.model.LineString;
import com.github.filosganga.geogson.model.LinearGeometry;
import com.github.filosganga.geogson.model.LinearRing;
import com.github.filosganga.geogson.model.MultiPolygon;
import com.github.filosganga.geogson.model.Point;
import com.github.filosganga.geogson.model.Polygon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * @author wf
 *
 */
public class Borders {
  // prepare a LOGGER
  protected static Logger LOGGER = Logger.getLogger("com.bitplan.radolan");

  public static boolean debug = true;

  private List<DPoint> points = new ArrayList<DPoint>();

  private FeatureCollection fc;

  /**
   * get the points
   * 
   * @return - the points
   */
  public List<DPoint> getPoints() {
    if (points.size() == 0)
      fetchPoints();
    return points;
  }

  // https://github.com/isellsoap/deutschlandGeoJSON
  // https://raw.githubusercontent.com/isellsoap/deutschlandGeoJSON/master/2_bundeslaender/4_niedrig.geojson
  public Borders(String borderName) {
    // https://stackoverflow.com/a/21337734/1497139
    String json;
    try {
      json = IOUtils.toString(
          this.getClass().getClassLoader().getResource(borderName), "UTF-8");
      // System.out.println(json.length());
      Gson gson = new GsonBuilder()
          .registerTypeAdapterFactory(new GeometryAdapterFactory()).create();
      fc = gson.fromJson(json, FeatureCollection.class);

    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "could not load borders " + borderName);
    }
  }

  private void fetchPoints() {
    for (Feature feature : fc.features()) {
      if (debug) {
    
        if (debug)
          LOGGER.log(Level.INFO,feature.toString());
      }
      Geometry<?> geometry = feature.geometry();
      if (geometry instanceof MultiPolygon) {
        MultiPolygon mpolygon = (MultiPolygon) geometry;
        for (Polygon polygon : mpolygon.polygons()) {
          addPoints(polygon);
        }
      }
      if (geometry instanceof Polygon) {
        Polygon polygon = (Polygon) geometry;
        addPoints(polygon);
      }
    }
  }

  /**
   * add the points for the given polygon
   * 
   * @param polygon
   */
  public void addPoints(Polygon polygon) {
    for (LinearRing ring : polygon.linearRings()) {
      addPoints(ring);
    }
    for (LineString string : polygon.lineStrings()) {
      addPoints(string);
    }
  }

  /**
   * get the points
   * 
   * @param linearGeometry
   */
  public void addPoints(LinearGeometry linearGeometry) {
    for (Point point : linearGeometry.points()) {
      points.add(new DPoint(point.lat(), point.lon()));
    }
  }

}
