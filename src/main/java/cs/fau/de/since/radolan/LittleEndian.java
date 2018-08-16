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

import cs.fau.de.since.radolan.Catalog.Unit;

/**
 * migrated to Java from
 * https://gitlab.cs.fau.de/since/radolan/blob/master/littleendian.go
 * 
 * @author wf
 *
 */
public class LittleEndian {

  // parseLittleEndian parses the little endian encoded composite as described
  // in [1] and [3].
  // Result are written into the previously created PlainData field of the
  // composite.
  public static void parseLittleEndian(Composite c) {
    int last = c.PlainData.length - 1;
    for (int y = 0; y < c.PlainData.length; y++) {
      byte[] line = readRowLittleEndian(c,y);
      try {
        decodeLittleEndian(c, c.PlainData[last - y], line); // write
                                                            // vertically
                                                            // flipped
      } catch (Exception e) {
        c.error = e;
        break;
      }
    }
  }

 
  /**
   * readLineLittleEndian reads a row at the given y position
   * This method is used to get a y-row of little endian encoded data.
   * @param c - the composite to read from
   * @param y - the y position at which to read
   * @return - a byte row of data with the with c.Dx*2
   */
  public static byte[] readRowLittleEndian(Composite c, int y) {
    byte[] yrow = new byte[c.getDx() * 2];
    for (int x=0;x<c.getDx()*2;x++)  {
      yrow[x]=c.getByte(x,y);
    }
    return yrow;
  }

  // decodeLittleEndian decodes the source line and writes to the given
  // destination.
  public static void decodeLittleEndian(Composite c, float[] dst, byte[] line)
      throws Exception {
    if ((line.length % 2 != 0) || (dst.length * 2 != line.length)) {
      throw new Exception(String.format(
          "decodeLittleEndian destination size %d and source size %d are not even or equal",
          dst.length, line.length));
    }
    for (int i = 0; i < dst.length; i++) {
      dst[i] = rvp6LittleEndian(c, line[2 * i], line[2 * i + 1]);
    }
  }

  // rvp6LittleEndian converts the raw two byte tuple of little endian encoded
  // composite products
  // to radar video processor values (rvp-6). NaN may be returned when the
  // no-data flag is set.
  public static float rvp6LittleEndian(Composite c, byte... tuple) {
    int value = 0x0F & tuple[1];
    value = (value << 8) | (tuple[0]&0x0f);

    if ((tuple[1] & (1 << 5)) != 0) { // error code: no-data
      return Float.NaN;
    }

    if ((tuple[1] & (1 << 6)) != 0) { // flag: negative value
      value *= -1;
    }

    float conv = (float) c.rvp6Raw(value); // set decimal point

    // little endian encoded formats are also used for mm/h
    if (c.getDataUnit() != Unit.Unit_dBZ) {
      return conv;
    }

    // Even though this format supports negative values and custom
    // precision they do not make use of this and we still have to subtract
    // the bias and scale it (RADVOR FX, dBZ)
    return Conversion.toDBZ(conv);
  }

}
