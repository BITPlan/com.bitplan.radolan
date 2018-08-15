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

/**
 * migrated to Java from
 * https://gitlab.cs.fau.de/since/radolan/blob/master/translate.go
 * 
 * @author wf
 *
 */
public class Translate {
  public static final double earthRadius = 6370.04; // km

  public static final double junctionNorth = 60.0; // N
  public static final double junctionEast = 10.0; // E

  enum GridType {
    unknownGrid, //
    nationalGrid, // resolution: 900km * 900km
    nationalPictureGrid, // resolution: 920km * 920km
    extendedNationalGrid, // resolution: 900km * 1100km
    middleEuropeanGrid // resolution: 1400km * 1500km
  }

  public static final IPoint nationalGrid = minRes(new IPoint(900, 900));
  public static final IPoint nationalPictureGrid = minRes(new IPoint(920, 920));
  public static final IPoint extendedNationalGrid = minRes(new IPoint(900, 1100));
  public static final IPoint middleEuropeanGrid = minRes(new IPoint(1400, 1500));

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

  // detectGrid identifies the used projection grid based on the composite
  // dimensions
  public static GridType detectGrid(Composite c) {
    IPoint d = minRes(new IPoint(c.getDx(), c.getDy()));
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

  // calibrateProjection initializes fields that are necessary for coordinate
  // translation
  public static void calibrateProjection(Composite c) {
    // get corner points
    CornerPoints cp = cornerPoints(c);
    if (cp == null) {
      double nan = Double.NaN;
      c.setRx(nan);
      c.setRy(nan);
      c.offx = nan;
      c.offy = nan;
      return;
    }

    // found matching projection rule
    c.setHasProjection(true);

    // set resolution to 1 km for calibration
    c.setRx(1.0);
    c.setRy(1.0);

    // calibrate offset correction
    DPoint off = translate(c, cp.originTop, cp.originLeft);
    c.offx = off.x;
    c.offy = off.y;

    // calibrate scaling
    DPoint res = translate(c, cp.edgeBottom, cp.edgeRight);
    c.setRx((res.x) / c.getDx());
    c.setRy((res.y) / c.getDy());
  }

  public static double rad(double deg) {
    return deg * Math.PI / 180.0;
  }

  // Translate translates geographical coordinates (latitude north, longitude
  // east) to the
  // according data indices in the coordinate system of the composite.
  // NaN is returned when no projection is available. Procedures adapted from
  // [1].
  public static DPoint translate(Composite c, double north, double east) {
    if (!c.isHasProjection()) {
      return new DPoint(Double.NaN,Double.NaN);
    }

    double lamda0 = rad(junctionEast);
    double phi0 = rad(junctionNorth);
    double lamda = rad(east);
    double phi = rad(north);

    double m = (1.0 + Math.sin(phi0)) / (1.0 + Math.sin(phi));
    double x = (earthRadius * m * Math.cos(phi) * Math.sin(lamda - lamda0));
    double y = (earthRadius * m * Math.cos(phi) * Math.cos(lamda - lamda0));

    // offset correction
    x -= c.offx;
    y -= c.offy;

    // scaling
    x /= c.getRx();
    y /= c.getRy();
    return new DPoint(x,y);
  }
}
