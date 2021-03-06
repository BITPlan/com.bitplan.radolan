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
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import com.github.filosganga.geogson.gson.GeometryAdapterFactory;
import com.github.filosganga.geogson.model.Feature;
import com.github.filosganga.geogson.model.FeatureCollection;
import com.github.filosganga.geogson.model.Geometry;
import com.github.filosganga.geogson.model.LineString;
import com.github.filosganga.geogson.model.LinearRing;
import com.github.filosganga.geogson.model.MultiPolygon;
import com.github.filosganga.geogson.model.Point;
import com.github.filosganga.geogson.model.Polygon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.scene.paint.Color;

/**
 * @author wf see https://tools.ietf.org/html/rfc7946
 */
public class Borders {
  // prepare a LOGGER
  protected static Logger LOGGER = Logger.getLogger("com.bitplan.radolan");

  public static boolean debug = true;

  private List<DPoint> points = new ArrayList<DPoint>();
  private List<LineString> lineStrings = new ArrayList<LineString>();

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

  /**
   * get the line string
   * 
   * @return - the line strings
   */
  public List<LineString> getLineStrings() {
    if (lineStrings.size() == 0) {
      this.fetchLineStrings();
    }
    return lineStrings;
  }

  /**
   * https://github.com/isellsoap/deutschlandGeoJSON
   * https://raw.githubusercontent.com/isellsoap/deutschlandGeoJSON/master/2_bundeslaender/4_niedrig.geojson
   */
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
    for (LineString lineString : this.getLineStrings()) {
      for (Point point : lineString.points()) {
        points.add(new DPoint(point.lat(), point.lon()));
      }
    }
  }

  private void fetchLineStrings() {
    for (Feature feature : fc.features()) {
      if (debug) {
        // LOGGER.log(Level.INFO,feature.toString());
      }
      Geometry<?> geometry = feature.geometry();
      if (geometry instanceof MultiPolygon) {
        MultiPolygon mpolygon = (MultiPolygon) geometry;
        for (Polygon polygon : mpolygon.polygons()) {
          addLineString(polygon);
        }
      }
      if (geometry instanceof Polygon) {
        Polygon polygon = (Polygon) geometry;
        addLineString(polygon);
      }
    }
  }

  /**
   * add the lines strings for the given polygon
   * 
   * @param polygon
   */
  public void addLineString(Polygon polygon) {
    for (LinearRing ring : polygon.linearRings()) {
      addLineString(ring);
    }
    for (LineString string : polygon.lineStrings()) {
      addLineString(string);
    }
  }

  /**
   * add the given LineString
   * 
   * @param lineString
   */
  public void addLineString(LineString lineString) {
    lineStrings.add(lineString);
  }

  /**
   * convert me to a list of polygons
   * @param borderColor
   * @param translate
   * @return the list of polygons
   */
  public List<javafx.scene.shape.Polygon> asPolygons(double strokeWidth,Color borderColor,double opacity,BiFunction<Double,Double,DPoint> translate) {
    List<javafx.scene.shape.Polygon> polygons=new ArrayList<javafx.scene.shape.Polygon>();
    List<LineString> lineStrings = getLineStrings();

    int lineCount = 0;
    for (LineString lineString : lineStrings) {
      List<Double> polygonPoints = new ArrayList<Double>();

      // IPoint prevIp = null;
      for (Point point : lineString.points()) {
        DPoint p = translate.apply(point.lat(),point.lon());

        polygonPoints.add(p.x);
        polygonPoints.add(p.y);

      }
      double points[] = new double[polygonPoints.size()];
      for (int i = 0; i < polygonPoints.size(); i++) {
        points[i] = polygonPoints.get(i);
      }
      javafx.scene.shape.Polygon polygon = new javafx.scene.shape.Polygon(points);
      polygon.setStrokeWidth(strokeWidth);
      polygon.setStroke(borderColor);
      if (lineCount % 2 == 0)
        polygon.setFill(Color.rgb(0xF8, 0xF8, 0xF8, opacity));
      else
        polygon.setFill(Color.rgb(0xFA, 0xFA, 0xFA, opacity));
      lineCount++;
      polygons.add(polygon);
    }
    return polygons;
  }

}
