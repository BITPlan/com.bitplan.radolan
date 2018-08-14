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

import java.util.HashMap;
import java.util.Map;

public class Catalog {
  Map<String, Spec> dimensionCatalog = new HashMap<String, Spec>();
  Map<String, Unit> unitCatalog = new HashMap<String, Unit>();

  enum Unit {
    Unit_unknown("unknown unit"), Unit_mm("mm"), // mm/interval
    Unit_dBZ("dbZ"), // dBZ
    Unit_km("km"), // km
    Unit_mps("m/s"); // m/s
    private String unit;

    Unit(final String unit) {
      this.unit = unit;
    }

    @Override
    public String toString() {
      return unit;
    }
  };

  class Spec {
    String comment;
    int px; // plain data dimensions
    int py;

    int dx; // data (layer) dimensions
    int dy;

    double rx; // resolution
    double ry;

    /**
     * construct me from the given parameters
     * 
     * @param px
     * @param py
     * @param dx
     * @param dy
     * @param rx
     * @param ry
     * @param comment
     */
    public Spec(int px, int py, int dx, int dy, int rx, int ry,
        String comment) {
      this.px = px;
      this.py = py;
      this.dx = dx;
      this.dy = dy;
      this.rx = rx;
      this.ry = ry;
      this.comment = comment;
    }
  }

  static Catalog instance;

  /**
   * singleton default constructor
   */
  private Catalog() {
    dimensionCatalog = new HashMap<String, Spec>();
    // local picture products do not provide dimensions in header

    dimensionCatalog.put("OL", new Spec(200, 224, 200, 200, 2, 2,
        "reflectivity (no clutter detection)"));
    dimensionCatalog.put("OX", new Spec(200, 224, 200, 200, 1, 1,
        "reflectivity (no clutter detection)"));
    dimensionCatalog.put("PD",
        new Spec(200, 224, 200, 200, 1, 1, "radial velocity"));
    dimensionCatalog.put("PE", new Spec(200, 224, 200, 200, 2, 2, "echotop"));
    dimensionCatalog.put("PF",
        new Spec(200, 224, 200, 200, 1, 1, "reflectivity (15 classes)"));
    dimensionCatalog.put("PH",
        new Spec(200, 224, 200, 200, 1, 1, "accumulated rainfall"));
    dimensionCatalog.put("PL",
        new Spec(200, 224, 200, 200, 2, 2, "reflectivity"));
    dimensionCatalog.put("PM",
        new Spec(200, 224, 200, 200, 2, 2, "max. reflectivity"));
    dimensionCatalog.put("PR",
        new Spec(200, 224, 200, 200, 1, 1, "radial velocity"));
    dimensionCatalog.put("PU",
        new Spec(200, 2400, 200, 200, 1, 1, "3D radial velocity"));
    dimensionCatalog.put("PV",
        new Spec(200, 224, 200, 200, 1, 1, "radial velocity"));
    dimensionCatalog.put("PX",
        new Spec(200, 224, 200, 200, 1, 1, "reflectivity (6 classes)"));
    dimensionCatalog.put("PY",
        new Spec(200, 224, 200, 200, 1, 1, "accumulated rainfall"));
    dimensionCatalog.put("PZ",
        new Spec(200, 2400, 200, 200, 2, 2, "3D reflectivity CAPPI"));
    unitCatalog.put("CH", Unit.Unit_mm);
    unitCatalog.put("CX", Unit.Unit_dBZ);
    unitCatalog.put("D2", Unit.Unit_mm);
    unitCatalog.put("D3", Unit.Unit_mm);
    unitCatalog.put("EA", Unit.Unit_dBZ);
    unitCatalog.put("EB", Unit.Unit_mm);
    unitCatalog.put("EC", Unit.Unit_mm);
    unitCatalog.put("EH", Unit.Unit_mm);
    unitCatalog.put("EM", Unit.Unit_mm);
    unitCatalog.put("EW", Unit.Unit_mm);
    unitCatalog.put("EX", Unit.Unit_dBZ);
    unitCatalog.put("EY", Unit.Unit_mm);
    unitCatalog.put("EZ", Unit.Unit_mm);
    unitCatalog.put("FX", Unit.Unit_dBZ);
    unitCatalog.put("FZ", Unit.Unit_dBZ);
    unitCatalog.put("HX", Unit.Unit_dBZ);
    unitCatalog.put("OL", Unit.Unit_dBZ);
    unitCatalog.put("OX", Unit.Unit_dBZ);
    unitCatalog.put("PA", Unit.Unit_dBZ);
    unitCatalog.put("PC", Unit.Unit_dBZ);
    unitCatalog.put("PD", Unit.Unit_mps);
    unitCatalog.put("PE", Unit.Unit_km);
    unitCatalog.put("PF", Unit.Unit_dBZ);
    unitCatalog.put("PG", Unit.Unit_dBZ);
    unitCatalog.put("PH", Unit.Unit_mm);
    unitCatalog.put("PI", Unit.Unit_dBZ);
    unitCatalog.put("PK", Unit.Unit_dBZ);
    unitCatalog.put("PL", Unit.Unit_dBZ);
    unitCatalog.put("PM", Unit.Unit_dBZ);
    unitCatalog.put("PN", Unit.Unit_dBZ);
    unitCatalog.put("PR", Unit.Unit_mps);
    unitCatalog.put("PU", Unit.Unit_mps);
    unitCatalog.put("PV", Unit.Unit_mps);
    unitCatalog.put("PX", Unit.Unit_dBZ);
    unitCatalog.put("PY", Unit.Unit_mm);
    unitCatalog.put("PZ", Unit.Unit_dBZ);
    unitCatalog.put("RA", Unit.Unit_mm);
    unitCatalog.put("RB", Unit.Unit_mm);
    unitCatalog.put("RE", Unit.Unit_mm);
    unitCatalog.put("RH", Unit.Unit_mm);
    unitCatalog.put("RK", Unit.Unit_mm);
    unitCatalog.put("RL", Unit.Unit_mm);
    unitCatalog.put("RM", Unit.Unit_mm);
    unitCatalog.put("RN", Unit.Unit_mm);
    unitCatalog.put("RQ", Unit.Unit_mm);
    unitCatalog.put("RR", Unit.Unit_mm);
    unitCatalog.put("RU", Unit.Unit_mm);
    unitCatalog.put("RW", Unit.Unit_mm);
    unitCatalog.put("RX", Unit.Unit_dBZ);
    unitCatalog.put("RY", Unit.Unit_mm);
    unitCatalog.put("RZ", Unit.Unit_mm);
    unitCatalog.put("S2", Unit.Unit_mm);
    unitCatalog.put("S3", Unit.Unit_mm);
    unitCatalog.put("SF", Unit.Unit_mm);
    unitCatalog.put("SH", Unit.Unit_mm);
    unitCatalog.put("SQ", Unit.Unit_mm);
    unitCatalog.put("TB", Unit.Unit_mm);
    unitCatalog.put("TH", Unit.Unit_mm);
    unitCatalog.put("TW", Unit.Unit_mm);
    unitCatalog.put("TX", Unit.Unit_dBZ);
    unitCatalog.put("TZ", Unit.Unit_mm);
    unitCatalog.put("W1", Unit.Unit_mm);
    unitCatalog.put("W2", Unit.Unit_mm);
    unitCatalog.put("W3", Unit.Unit_mm);
    unitCatalog.put("W4", Unit.Unit_mm);
    unitCatalog.put("WX", Unit.Unit_dBZ);
  }

  public static Catalog getInstance() {
    if (instance == null)
      instance = new Catalog();
    return instance;
  }

}
