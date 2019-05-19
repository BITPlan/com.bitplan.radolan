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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * calculate the convex Hull of a list of points (or polygons)
 * 
 * @author wf
 *
 */
public class ConvexHull {
  private List<DPoint> points = new ArrayList<DPoint>();
  private List<DPoint> hull;

  /**
   * @return the hull
   */
  public List<DPoint> getHull() {
    return hull;
  }

  /**
   * @return the points
   */
  public List<DPoint> getPoints() {
    return points;
  }

  /**
   * @param points
   *          the points to set
   */
  public void setPoints(List<DPoint> points) {
    this.points=points;
  }

  public ConvexHull() {
  }

  /**
   * create me from a list of polygons
   * 
   * @param polygons
   */
  public static ConvexHull fromPolygons(List<Polygon> polygons) {
    ConvexHull ch = new ConvexHull();
    for (Polygon polygon : polygons) {
      ch.addPoints(polygon);
    }
    ch.convexHull();
    return ch;
  }

  /**
   * construct me from an array of points
   * 
   * @param points
   */
  public static ConvexHull fromPointList(List<DPoint> points) {
    ConvexHull ch = new ConvexHull();
    ch.setPoints(points);
    ch.convexHull();
    return ch;
  }

  /**
   * construct me from an array of points
   * 
   * @param points
   */
  public static ConvexHull fromPointArray(DPoint[] points) {
    ConvexHull ch = new ConvexHull();
    ch.setPoints(Arrays.asList(points));
    ch.convexHull();
    return ch;
  }

  /**
   * add the points of the given polygon
   * 
   * @param polygon
   */
  private void addPoints(Polygon polygon) {
    ObservableList<Double> ppoints = polygon.getPoints();
    for (int i = 0; i < ppoints.size(); i += 2) {
      DPoint point=new DPoint(ppoints.get(i), ppoints.get(i + 1));
      getPoints().add(point);
    }
  }

  /**
   * To find orientation of ordered triplet (p, q, r). The function returns
   * following values 0 --> p, q and r are colinear 1 --> Clockwise 2 -->
   * Counterclockwise
   * 
   * @param p
   * @param q
   * @param r
   * @return the orientation
   */
  public static int orientation(DPoint p, DPoint q, DPoint r) {
    double val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);

    if (val == 0)
      return 0; // colinear
    return (val > 0) ? 1 : 2; // clock or counterclock wise
  }
  
  /**
   * Create the convex hull with Jarvis's Algorithm (Gift Wrapping) see
   * <a href="https://en.wikipedia.org/wiki/Gift_wrapping_algorithm">Gift
   * Wrapping algorithm</a> and see <a href=
   * "https://www.geeksforgeeks.org/convex-hull-set-1-jarviss-algorithm-or-wrapping">Java
   * Implementation</a>
   * 
   */
  public void convexHull() {
    int n = getPoints().size();
    hull = new ArrayList<DPoint>();
    // There must be at least 3 points
    if (n < 3)
      return;
    // Find the leftmost point
    int l = 0;
    for (int i = 1; i < n; i++)
      if (getPoints().get(i).x < getPoints().get(l).x)
        l = i;
    // Start from leftmost point, keep moving
    // counterclockwise until reach the start point
    // again. This loop runs O(h) times where h is
    // number of points in result or output.
    int p = l, q;
    do {
      // Add current point to result
      getHull().add(getPoints().get(p));

      // Search for a point 'q' such that
      // orientation(p, x, q) is counterclockwise
      // for all points 'x'. The idea is to keep
      // track of last visited most counterclock-
      // wise point in q. If any point 'i' is more
      // counterclock-wise than q, then update q.
      q = (p + 1) % n;

      for (int i = 0; i < n; i++) {
        // If i is more counterclockwise than
        // current q, then update q
        if (orientation(getPoints().get(p), getPoints().get(i),
            getPoints().get(q)) == 2)
          q = i;
      }

      // Now q is the most counterclockwise with
      // respect to p. Set p as q for next iteration,
      // so that q is added to result 'hull'
      p = q;

    } while (p != l); // While we don't come to first
                      // point
  }

  /**
   * get my hull as a polygon
   * @return - the hull polygon
   */
  public Polygon asPolygon() {
    Polygon hPolygon=new Polygon();
    for (DPoint point : getHull()) {
      hPolygon.getPoints().add(point.x);
      hPolygon.getPoints().add(point.y);
    }
    return hPolygon;
  }

}
