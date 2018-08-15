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

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import cs.fau.de.since.radolan.Catalog.Spec;
import cs.fau.de.since.radolan.Catalog.Unit;

/**
 * migrated to Java from
 * https://gitlab.cs.fau.de/since/radolan/blob/master/header.go
 * 
 * @author wf
 *
 */
public class Header {
  // splitHeader splits the given header string into its fields. The returned
  // map is using the field name as key and the field content as value.
  public static Map<String, String> splitHeader(String header) {
    Map<String, String> m = new HashMap<String, String>();
    int beginKey = 0;
    int endKey = 0;
    int beginValue = 0;
    int endValue = 0;
    boolean dispatch = false;

    for (int i = 0; i < header.length(); i++) {
      char c = header.charAt(i);
      if (Character.isUpperCase(c)) {
        if (dispatch) {
          m.put(header.substring(beginKey, endKey),
              header.substring(beginValue, endValue));
          beginKey = i;
          dispatch = false;
        }
        endKey = i + 1;
      } else {
        if (i == 0) {
          return m; // no key prefixing value
        }
        if (!dispatch) {
          beginValue = i;
          dispatch = true;
        }
        endValue = i + 1;
      }
    }
    m.put(header.substring(beginKey, endKey),
        header.substring(beginValue, endValue));

    return m;
  }

  // parseHeader parses and the composite header and writes the related fields
  // as
  // described in [1] and [3].
  public static void parseHeader(Composite c) throws Exception {
    // Split header segments
    Map<String, String> section = splitHeader(c.header); // without delimiter

    // Parse Product - Example: "PG" or "FZ"
    c.setProduct(c.header.substring(0, 2));

    // Lookup Unit
    c.setDataUnit(Unit.Unit_unknown);
    Catalog catalog = Catalog.getInstance();
    if (catalog.unitCatalog.containsKey(c.getProduct())) {
      c.setDataUnit(catalog.unitCatalog.get(c.getProduct()));
    }

    // Parse DataLength - Example: "BY 405160"
    String dataLengthStr = section.get("BY").trim();
    if (dataLengthStr == null) {
      throw new Exception("parseHeader: missing dataLength/BY ######");
    }
    c.dataLength = Integer.parseInt(dataLengthStr);
    c.dataLength -= c.header.length(); // remove header length including
                                       // delimiter
    // Parse CaptureTime - Example: "PG262115100000616" or
    // "FZ211615100000716"
    String dateStr = c.header.substring(2, 8) + c.header.substring(13, 17); // cut
                                                                            // WMO
                                                                            // number
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("ddHHmmMMyy");
    c.CaptureTime = ZonedDateTime.parse(dateStr,
        dateFormatter.withZone(ZoneId.of("Z")));
    /*
     * if err != nil {
     * return newError("parseHeader",
     * "could not parse capture time: "+err.Error())
     * }
     */
    // Parse ForecastTime - Example: "VV 005"
    String vv = section.get("VV");

    c.setForecastTime(c.CaptureTime);
    int min = 0;
    if (vv != null) {
      min = Integer.parseInt(vv.trim());
    }

    c.setForecastTime(c.CaptureTime.plus(Duration.ofMinutes(min)));

    // Parse Interval - Example "INT 5" or "INT1008"
    String intv = section.get("INT");
    min = 0;
    if (intv != null) {
      min = Integer.parseInt(intv.trim());
    }

    c.Interval = Duration.ofMinutes(min);
    switch (c.getProduct()) {
    case "W1":
    case "W2":
    case "W3":
    case "W4":
      c.Interval = c.Interval.multipliedBy(10);
      break;
    }

    // Parse Dimensions - Example: "GP 450x 450" or "BG460460" or
    // "GP 1500x1400" (if defined)

    String dim = section.get("GP");
    if (dim != null) {
      String[] parts = dim.split("x");
      c.setDx(Integer.parseInt(parts[0].trim()));
      c.setDy(Integer.parseInt(parts[1].trim()));
      c.setPx(c.getDx());
      c.setPy(c.getDy()); // composite formats do not show elevation
    } else {
      dim = section.get("BG");
      Spec v;
      if (dim != null) {
        c.setDy(Integer.parseInt(dim.substring(0, 3)));
        c.setDx(Integer.parseInt(dim.substring(3, 6)));
        c.setPx(c.getDx());
        c.setPy(c.getDy()); // composite formats do not show elevation
      } else { // dimensions of local picture products not defined in header
        v = catalog.dimensionCatalog.get(c.getProduct());
        if (v == null) {
          throw new Exception(
              "parseHeader: no dimension information available");
        }
        c.setPx(v.px);
        c.setPy(v.py); // plain data dimensions
        c.setDx(v.dx);
        c.setDy(v.dy); // data layer dimensions
        c.setRx(v.rx);
        c.setRy(v.ry); // data resolution
      }
    }

    // Parse Precision - Example: "PR E-01" or "PR E+00"
    String prec = section.get("E");
    if (prec != null) {
      c.precision = Integer.parseInt(prec);
    }

    // Parse Level - Example "LV 6 1.0 19.0 28.0 37.0 46.0 55.0"
    // or "LV12-31.5-24.5-17.5-10.5 -5.5 -1.0 1.0 5.5 10.5 17.5 24.5 31.5"
    String lv = section.get("LV");
    if (lv != null) {
      if (lv.length() < 2) {
        throw new Exception("parseHeader: level field too short");
      }
      int cnt = Integer.parseInt(lv.substring(0, 2).trim());
      if (lv.length() != cnt * 5 + 2) { // fortran format I2 + F5.1
        throw new Exception("parseHeader: invalid level format: " + lv);
      }

      c.level = new float[cnt];
      for (int i = 0; i < cnt; i++) {
        int n = i * 5;
        c.level[i] = Float.parseFloat(lv.substring(n + 2, n + 7));
      }
    }
  }

}
