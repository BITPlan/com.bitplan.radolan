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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.bitplan.geo.DPoint;
import com.bitplan.radolan.Testing;

/**
 * migrated to Java from
 * https://gitlab.cs.fau.de/since/radolan/blob/master/translate_test.go
 * 
 * @author wf
 *
 */
public class TestTranslate extends Testing {

  public boolean absequal(double a, double b, double epsilon) {
    return Math.abs(a - b) < epsilon;
  }
  
  
  @Test
  public void testResolution() {

    double srcLat = 48.173146;
    double srcLon = 11.546604; // Munich
    double dstLat = 53.534366;
    double dstLon = 08.576135; // Bremerhaven
    double expDist = 663.629945199998; // km

    Composite[] dummys = { Composite.NewDummy("SF", 900, 900),
        Composite.NewDummy("SF", 450, 450), Composite.NewDummy("SF", 225, 225),
        Composite.NewDummy("WX", 900, 1100), Composite.NewDummy("WX", 450, 550),
        Composite.NewDummy("EX", 1400, 1500),
        Composite.NewDummy("EX", 700, 750), };

    for (Composite comp : dummys) {
      DPoint src = comp.translateLatLonToGrid(srcLat, srcLon);
      DPoint dst = comp.translateLatLonToGrid(dstLat, dstLon);

      double resDist = Translate.dist(src.x * comp.getResX(), src.y * comp.getResY(),
          dst.x * comp.getResX(), dst.y * comp.getResY());

      if (!absequal(resDist, expDist, 0.000001)) { // inaccuracy by 1mm
        Errorf(
            "dummy.Rx = %.2f, dummy.Ry = %.2f; distance: %.3f km expected: %.3f km)",
            comp.getResX(), comp.getResY(), resDist, expDist);
      }
    }
  }

  public class TranslateTestCase {
    public Composite comp;
    public double edge[][];

    public TranslateTestCase(Composite comp, double[][] edge) {
      this.comp = comp;
      this.edge = edge;
    }
  }

  @Test
  public void testTranslate() {
    double nationalGridPG[][] = { { 54.66218275, 1.900684377, 0, 0 },
        { 54.81884457, 15.88724008, 460, 0 },
        { 51.00000000, 09.00000000, 230, 230 },
        { 46.86029310, 3.481345126, 0, 460 },
        { 46.98044293, 14.73300934, 460, 460 } };

    double nationalGridHalf[][] = { { 54.5877, 02.0715, 0, 0 },
        { 54.7405, 15.7208, 450, 0 }, { 51.0000, 09.0000, 225, 225 },
        { 46.9526, 03.5889, 0, 450 }, { 47.0705, 14.6209, 450, 450 } };

    double nationalGrid[][] = { { 54.5877, 02.0715, 0, 0 },
        { 54.7405, 15.7208, 900, 0 }, { 51.0000, 09.0000, 450, 450 },
        { 46.9526, 03.5889, 0, 900 }, { 47.0705, 14.6209, 900, 900 }, };

    double extendedNationalGrid[][] = { { 55.5482, 03.0889, 0, 0 },
        { 55.5342, 17.1128, 900, 0 }, { 51.0000, 09.0000, 370, 550 },
        { 46.1929, 04.6759, 0, 1100 }, { 46.1827, 15.4801, 900, 1100 }, };

    double middleEuropeanGrid[][] = { { 56.5423, -0.8654, 0, 0 },
        { 56.4505, 21.6986, 1400, 0 }, { 51.0000, 09.0000, 600, 700 },
        { 43.9336, 02.3419, 0, 1500 }, { 43.8736, 18.2536, 1400, 1500 }, };

    Composite dummyPG = Composite.NewDummy("PG", 460, 460);
    Composite dummyFZ = Composite.NewDummy("FZ", 450, 450);
    Composite dummyRX = Composite.NewDummy("RX", 900, 900);
    Composite dummyWX = Composite.NewDummy("WX", 900, 1100);
    Composite dummyEX = Composite.NewDummy("EX", 1400, 1500);

    TranslateTestCase testcases[] = {
        new TranslateTestCase(dummyPG, nationalGridPG),
        new TranslateTestCase(dummyFZ, nationalGridHalf),
        new TranslateTestCase(dummyRX, nationalGrid),
        new TranslateTestCase(dummyWX, extendedNationalGrid),
        new TranslateTestCase(dummyEX, middleEuropeanGrid) };

    for (TranslateTestCase test : testcases) {
      Logf("dummy%s: Rx = %f; Ry = %f\n", test.comp.getProduct(),
          test.comp.getResX(), test.comp.getResY());
      Logf("dummy%s: offx = %f; offy = %f\n", test.comp.getProduct(),
          test.comp.getOffSetX(), test.comp.getOffSetY());
      //debug=true;
      for (double[] edge : test.edge) {
        DPoint latlon=new DPoint(edge[0], edge[1]);
        // result
        DPoint r = test.comp.translateLatLonToGrid(latlon.x,latlon.y);
        // expected
        DPoint e = new DPoint(edge[2], edge[3]);

        // allowed inaccuracy by 100 meters
        if (Translate.dist(r.x, r.y, e.x, e.y) > 0.1) {
          Errorf(
              "dummy%s.Translate(%.2f, %.2f) = (%.2f, %.2f); expected: (%.2f, %.2f)",
              test.comp.getProduct(), edge[0], edge[1], r.x, r.y, e.x, e.y);
        }
        // inverse Polarsterographic projection
        DPoint latlon2 = test.comp.translateGridToLatLon(e);
        if (Translate.haversine(latlon2.x, latlon2.y, latlon.x, latlon.y) > 0.1) {
          Errorf(
              "dummy%s.TranslateXYtoLatLon(%.2f, %.2f) = (%.2f, %.2f); expected: (%.2f, %.2f)",
              test.comp.getProduct(), e.x,e.y, latlon2.x, latlon2.y, latlon.x, latlon.y);
        }

      }
    }
  }

  /**
   * PolarStereo Projection
   * test data for conversion from lat - north / lon - east to cartesian coordinate
   * see description and pointer to PDF file in Translate.java
   * @author wf
   *
   */
  class PolarStereoProjection {
    double north;
    double east;
    double x;
    double y;

    /**
     * construct me
     * @param east
     * @param north
     * @param x
     * @param y
     */
    public PolarStereoProjection(double east, double north, double x, double y) {
      this.east = east;
      this.north = north;
      this.x = x;
      this.y = y;
    }
  }

  @Test
  public void testFormulas() {
    assertEquals(Translate.rad(180),Math.toRadians(180),0.0001);
    // Data from Table on page 13 of 
    // https://www.dwd.de/DE/leistungen/radolan/radolan_info/radolan_radvor_op_komposit_format_pdf.pdf?__blob=publicationFile&v=10
    PolarStereoProjection[] pspTable = { 
        new PolarStereoProjection(Translate.junctionEast, Translate.junctionNorth, 0, -3185.02),
        // 1.3
        new PolarStereoProjection(3.5889, 46.9526, -523.4622, -4658.645),
        new PolarStereoProjection(14.6209, 47.0705, 376.5378, -4658.645),
        new PolarStereoProjection(15.7208, 54.7405, 376.5378, -3758.645),
        new PolarStereoProjection(2.0715, 54.5877, -523.4622, -3758.645),
        // 3.2 Middle European Grid
        new PolarStereoProjection(2.3419, 43.9336, -673.4656656, -5008.642536),
        new PolarStereoProjection(18.2536, 43.8736, 726.5343344, -5008.642536),
        new PolarStereoProjection(21.6989, 56.4505, 726.5343344, -3508.642536),
        new PolarStereoProjection(-0.8654, 56.5423, -673.4656656, -3508.642536)
        };
    for (PolarStereoProjection psp : pspTable) {
      DPoint p = Translate.polarStereoProjection(psp.north, psp.east);
      assertEquals("x",psp.x, p.x, 0.007);
      assertEquals("y",psp.y, -p.y, 0.005);
      DPoint ne=Translate.inversePolarStereoProjection(psp.x, -psp.y);
      assertEquals("north",psp.north,ne.x,0.005);
      assertEquals("east",psp.east,ne.y,0.005);
    }
  }
  
  @Test
  public void testLatLon() {
    DPoint center=new DPoint(Translate.junctionNorth,Translate.junctionEast);
    assertEquals("60° 0’ 0” 10° 0’ 0”",center.toDMSString());
    //System.out.println(center.toFormattedDMSString());
    assertEquals("60° 0’ N 10° 0’ E",center.toFormattedDMSString());
  }

  enum GridMode {
    bottom, center
  };

  @Test
  public void testGrid() throws Exception {
    testGrid(GridMode.center, Composite.NewDummy("SF", 900, 900),
        Composite.NewDummy("EX", 1400, 1500),
        Composite.NewDummy("WX", 900, 1100));

    testGrid(GridMode.bottom, Composite.NewDummy("SF", 900, 900),
        Composite.NewDummy("EX", 1400, 1500)
    // NewDummy("WX", 900, 1100), testdata unavailable
    );
  }

  /**
   * test the GridMode for the given Composite dummies
   * 
   * @param mode
   * @param dummys
   * @throws IOException
   */
  public void testGrid(GridMode mode, Composite... dummys) throws Exception {
    // t.Helper()

    double offx = 0.0;
    double offy = 0.0;

    switch (mode) {
    case bottom:
      offx = 0.0;
      offy = 1.0;
      break;
    case center:
      offx = 0.5;
      offy = 0.5;
      break;
    default:
      Fatalf("unknown grid mode %s", mode.toString());
    }

    for (Composite comp : dummys) {
      String lname = String.format("src/test/data/lambda_%s_%dx%d.txt", mode,
          comp.getDy(), comp.getDx());
      String pname = String.format("src/test/data/phi_%s_%dx%d.txt", mode,
          comp.getDy(), comp.getDx());

      String lbuf = FileUtils.readFileToString(new File(lname), "UTF-8");
      String pbuf = FileUtils.readFileToString(new File(pname), "UTF-8");

      // fortran format F8.5 means read 8 bytes, the last 5 bytes are decimal
      // places
      final int length = 8;

      int l = 0;
      int p = 0;

      // Beschreibung-E-Produkte-Raster.pdf Radolan-Cons Version 1.0:
      // "Die Dateien beginnen mit dem Referenzwert des Datenelements in der
      // linken unteren Ecke des
      // Komposits, spaltenweise von Westen nach Osten und zeilenweise von Süden
      // nach Norden."
      //
      // "Für die Dateien mit der Bezeichnung _bottom beziehen sich die
      // Koordinaten
      // jeweils auf die linke untere Ecke jedes Datenelements. Für Dateien mit
      // der Bezeichnung
      // _center auf den Zentralpunkt."

      for (int y = comp.getDy() - 1; y >= 0; y--) {
        for (int x = 0; x < comp.getDx(); x++) {

          // newlines can occur in input files.
          while ('\n' == lbuf.charAt(l)) {
            l++;
          }
          while ('\n' == pbuf.charAt(p)) {
            p++;
          }
          String lstring = lbuf.substring(l, l + length).trim();
          double lamda = Double.parseDouble(lstring);
          /*
           * if err != nil {
           * t.Fatalf("invalid grid coordinate at (%d, %d): %#v %s", x, y,
           * lstring, lname) }
           */
          l += length;

          String pstring = pbuf.substring(p, p + length).trim();
          double phi = Double.parseDouble(pstring);
          /*
           * if err != nil {
           * t.Fatalf("invalid grid coordinate at (%d, %d): %#v %s", x, y,
           * pstring, pname) }
           */
          p += length;

          DPoint t = comp.translateLatLonToGrid(phi, lamda);
          DPoint e = new DPoint(x + offx, y + offy);

          if (Translate.dist(t.x, t.y, e.x, e.y) > 0.01) { // 10m
            Errorf(
                "dummy%s.Translate(%.2f, %.2f) = (%.2f, %.2f); expected: (%.2f, %.2f)",
                comp.getProduct(), phi, lamda, t.x, t.y, e.x, e.y);
          }
        }
      }

      String lTail = lbuf.substring(l, lbuf.length() - 1);
      if (lTail.length() > 0) {
        Fatalf("unprocessed data remaining in %s", lname);
      }
      String pTail = pbuf.substring(p, pbuf.length() - 1);
      if (pTail.length() > 0) {
        Fatalf("unprocessed data remaining in %s", pname);
      }
    }
  }
}
