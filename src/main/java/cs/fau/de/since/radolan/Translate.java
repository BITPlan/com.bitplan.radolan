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
package cs.fau.de.since.radolan;

import com.bitplan.geo.DPoint;
import com.bitplan.geo.IPoint;
import com.bitplan.geo.Projection;

/**
 * migrated to Java from
 * https://gitlab.cs.fau.de/since/radolan/blob/master/translate.go
 * 
 * @author wf
 *
 */
public class Translate {
  // Polarstereographische Projektion
  public static final double earthRadius = 6370.04; // R (km)
  public static final double junctionNorth = 60.0; // phi0 60.0° N
  public static final double junctionEast = 10.0; // lamda0 10.0° E

  public static final double lambda0 = rad(junctionEast);
  public static final double phi0 = rad(junctionNorth);

  enum GridType {
    unknownGrid, //
    nationalGrid, // resolution: 900km * 900km
    nationalPictureGrid, // resolution: 920km * 920km
    extendedNationalGrid, // resolution: 900km * 1100km
    middleEuropeanGrid // resolution: 1400km * 1500km
  }

  public static final IPoint nationalGrid = minRes(new IPoint(900, 900));
  public static final IPoint nationalPictureGrid = minRes(new IPoint(920, 920));
  public static final IPoint extendedNationalGrid = minRes(
      new IPoint(900, 1100));
  public static final IPoint middleEuropeanGrid = minRes(
      new IPoint(1400, 1500));

  // minRes repeatedly bisects the given edges until no further step is possible
  // for at least one edge. The resulting dimensions are the returned.
  public static IPoint minRes(IPoint d) {
    // rdx, rdy = dx, dy
    IPoint rd = new IPoint(d.x, d.y);

    if ((rd.x == 0) || (rd.y == 0)) {
      return rd;
    }

    for (; (rd.x & 1) == 0 && (rd.y & 1) == 0;) {
      rd.x >>= 1;
      rd.y >>= 1;
    }
    return rd;
  }

  /**
   * detectGrid identifies the used projection grid based on the projection
   * dimensions
   * 
   * @param pro - the project
   * @return - the projection
   */
  public static GridType detectGrid(Projection pro) {
    IPoint d = minRes(new IPoint(pro.getGridWidth(), pro.getGridHeight()));
    if (d.x == nationalGrid.x && d.y == nationalGrid.y) {
      return GridType.nationalGrid;
    }
    if (d.x == nationalPictureGrid.x && d.y == nationalPictureGrid.y) {
      return GridType.nationalPictureGrid;
    }
    if (d.x == extendedNationalGrid.x && d.y == extendedNationalGrid.y) {
      return GridType.extendedNationalGrid;
    }
    if (d.x == middleEuropeanGrid.x && d.y == middleEuropeanGrid.y) {
      return GridType.middleEuropeanGrid;
    }
    return GridType.unknownGrid;
  }

  // cornerPoints returns corner coordinates of the national, extended or
  // middle-european grid based on the dimensions of the composite. The used
  // values are described in [1], [4] and [5]. If an error is returned,
  // translation methods will not work.
  public static class CornerPoints {
    public CornerPoints(double originTop, double originLeft, double edgeBottom,
        double edgeRight) {
      super();
      this.originTop = originTop;
      this.originLeft = originLeft;
      this.edgeBottom = edgeBottom;
      this.edgeRight = edgeRight;
    }

    double originTop;
    double originLeft;
    double edgeBottom;
    double edgeRight;
  }

  public static CornerPoints cornerPoints(Composite c) {
    switch (detectGrid(c)) {
    case nationalGrid: // described in [1]
      return new CornerPoints(54.5877, 02.0715 // N, E
          , 47.0705, 14.6209 // N, E
      );
    case nationalPictureGrid: // (pg) described in [4]
      return new CornerPoints(54.66218275, 1.900684377 // N, E
          , 46.98044293, 14.73300934 // N, E
      );
    case extendedNationalGrid: // described in [5]
      return new CornerPoints(55.5482, 03.0889 // N, E
          , 46.1827, 15.4801 // N, E
      );
    case middleEuropeanGrid: // described in [5]
      return new CornerPoints(56.5423, -0.8654 // N, E
          , 43.8736, 18.2536 // N, E
      );
    default:

    }
    return null;
  }

  /** calibrateProjection initializes fields that are necessary for coordinate
   * translation
   * @param comp
   */
  public static void calibrateProjection(Composite comp) {
    // get corner points
    CornerPoints cp = cornerPoints(comp);
    calibrateProjection(comp,cp);
  }
  
  /**
   * calibrate the given Projection with the given CornerPoints
   * @param pro the projection to calibrate
   * @param cp - the corner points
   */
  public static void calibrateProjection(Composite pro, CornerPoints cp) {
    if (cp == null) {
      double nan = Double.NaN;
      pro.setResX(nan);
      pro.setResY(nan);
      pro.setOffSetX(nan);
      pro.setOffSetY(nan);
      return;
    }

    // found matching projection rule
    pro.setProjection(true);

    // set resolution to 1 km for calibration
    pro.setResX(1.0);
    pro.setResY(1.0);

    // calibrate offset correction
    DPoint off = translate(pro, cp.originTop, cp.originLeft);
    pro.setOffSetX(off.x);
    pro.setOffSetY(off.y);

    // calibrate scaling
    DPoint res = translate(pro, cp.edgeBottom, cp.edgeRight);
    pro.setResX((res.x) / pro.getDx());
    pro.setResY((res.y) / pro.getDy());
  }

  public static double square(double n) {
    return n * n;
  }

  /**
   * same as Math.toRadians
   * 
   * @param deg
   * @return - the radiant
   */
  public static double rad(double deg) {
    // return deg * Math.PI / 180.0;
    return Math.toRadians(deg);
  }

  /**
   * Calculates the distance in km between two lat/long points using the
   * haversine formula https://en.wikipedia.org/wiki/Haversine_formula see
   * http://stackoverflow.com/a/18862550/1497139
   */
  public static double haversine(double lat1, double lng1, double lat2,
      double lng2) {
    int r = 6371; // average radius of the earth in km
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lng2 - lng1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double d = r * c;
    return d;
  }

  /**
   * calculate the distance of two cartesian coordinates
   * https://en.wikipedia.org/wiki/Pythagorean_theorem
   * 
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @return the distance
   */
  public static double dist(double x1, double y1, double x2, double y2) {
    double x = x1 - x2;
    double y = y1 - y2;
    return Math.sqrt(x * x + y * y);
  }

  // Translate translates geographical coordinates
  // (latitude north, longitude east) to the
  // according data indices in the coordinate system of the composite.
  // NaN is returned when no projection is available. Procedures adapted from
  // [1].
  public static DPoint translate(Projection pro, double north, double east) {
    if (!pro.isProjection()) {
      return new DPoint(Double.NaN, Double.NaN);
    }
    DPoint p = polarStereoProjection(north, east);

    // offset correction
    p.x -= pro.getOffSetX();
    p.y -= pro.getOffSetY();

    // scaling
    p.x /= pro.getResX();
    p.y /= pro.getResY();
    return p;
  }

  /**
   * convert the given north - latitude /east -longitude values to a cartesian
   * coordinate
   * 
   * @param north
   * @param east
   * @return the x/y cartesian coordinate
   */
  public static DPoint polarStereoProjection(double north, double east) {
    double lambda = rad(east);
    double phi = rad(north);

    // see
    // https://www.dwd.de/DE/leistungen/radolan/radolan_info/radolan_radvor_op_komposit_format_pdf.pdf?__blob=publicationFile&v=10
    // 1.3 Polarstereografische Projektion
    double m = (1.0 + Math.sin(phi0)) / (1.0 + Math.sin(phi));
    double x = (earthRadius * m * Math.cos(phi) * Math.sin(lambda - lambda0));
    double y = (earthRadius * m * Math.cos(phi) * Math.cos(lambda - lambda0));
    // will give negated y - why?
    return new DPoint(x, y);
  }

  /**
   * translate a coordinate to lat/lon
   * @param pro - the projection
   * @param p
   * @return the lat/lon point
   */
  public static DPoint translateXYtoLatLon(Projection pro, DPoint p) {
    if (!pro.isProjection()) {
      return new DPoint(Double.NaN, Double.NaN);
    }
    DPoint pt=new DPoint(p.x,p.y);
    // scaling
    pt.x *= pro.getResX();
    pt.y *= pro.getResY();

    // offset correction
    pt.x += pro.getOffSetX();
    pt.y += pro.getOffSetY();

    DPoint latlon = inversePolarStereoProjection(pt.x, pt.y);

    return latlon;
  }

  // see
  // https://www.dwd.de/DE/leistungen/radolan/radolan_info/radolan_radvor_op_komposit_format_pdf.pdf?__blob=publicationFile&v=10
  // 1.4 Inverse Polarstereografische Projektion
  /**
   * convert the given cartesian coordinate to a lat/lon coordinate
   * 
   * @param x
   * @param y
   * @return the lat/lon result
   */
  public static DPoint inversePolarStereoProjection(double x, double y) {
    y = -y; // negation of y component necessary - why?
    double lambda = Math.atan(-x / y) + lambda0;
    double term = square(earthRadius) * square((1 + Math.sin(phi0)));
    double phi = Math.asin(
        (term - (square(x) + square(y))) / (term + (square(x) + square(y))));
    return new DPoint(Math.toDegrees(phi), Math.toDegrees(lambda));
  }
}
