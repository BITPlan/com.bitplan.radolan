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
 * https://gitlab.cs.fau.de/since/radolan/blob/master/conversion.go
 * @author wf
 *
 */
public class Conversion {
  
  /*
  var NaN = float32(math.NaN())

  func IsNaN(f float32) (is bool) {
    return f != f
  }*/

  // Z-R relationship
  static class ZR  {
    // intermediate caching
    double c1; // 10*b
    double c2; // a^(-1/b)
    double c3; // 10^(1/(10*b))
    double c4; // 10 * log10(a)
  }

  // Common Z-R relationships
  
  static ZR  Aniol80          = NewZR(256, 1.42); // operational use in germany, described in [6]
  static ZR  Doelling98       = NewZR(316, 1.50); // operational use in switzerland
  static ZR  JossWaldvogel70  = NewZR(300, 1.50);
  static ZR   MarshallPalmer55 = NewZR(200, 1.60); // operational use in austria
 
  // New Z-R returns a Z-R relationship mathematically expressed as Z = a * R^b
  public static ZR NewZR(double A, double B)  {
    ZR zr=new ZR();
    zr.c1 = 10.0 * B;
    zr.c2 = Math.pow(A, -1.0/B);
    zr.c3 = Math.pow(10.0, 1/zr.c1);
    zr.c4 = 10.0 * Math.log10(A);

    return zr;
  }

  // PrecipitationRate returns the estimated precipitation rate in mm/h for the given
  // reflectivity factor and Z-R relationship.
  public static double PrecipitationRate(ZR relation ,double dBZ) {
    return relation.c2 * Math.pow(relation.c3, dBZ);
  }

  // Reflectivity returns the estimated reflectivity factor for the given precipitation
  // rate (mm/h) and Z-R relationship.
  public static double Reflectivity(ZR relation, double rate)  {
    return relation.c4 + relation.c1 * Math.log10(rate);
  }

  // toDBZ converts the given radar video processor values (rvp-6) to radar reflectivity
  // factors in decibel relative to Z (dBZ).
  public static float toDBZ(float rvp)  {
    return rvp/2.0f - 32.5f;
  }

  // toRVP6 converts the given radar reflectivity factors (dBZ) to radar video processor
  // values (rvp-6).
  public static double toRVP6(float dBZ) {
    return (dBZ + 32.5) * 2;
  }

  // rvp6Raw converts the raw value to radar video processor values (rvp-6) by applying the
  // products precision field.
  public static double rvp6Raw(Composite c, int value)  {
    double rvalue=value * c.getPrecisionFactor();
    return rvalue;
  }

}
