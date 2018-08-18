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

import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * migrated to Java from
 * https://gitlab.cs.fau.de/since/radolan/blob/master/data.go
 */
public class Data {
  public enum Encoding {
    runlength, littleEndian, singleByte, unknown
  };

  Map<Encoding, Consumer<Composite>> parseMap = new HashMap<Encoding, Consumer<Composite>>();

  public Data() {
    parseMap.put(Encoding.runlength, c -> RunLength.parseRunlength(c));
    parseMap.put(Encoding.littleEndian, c -> LittleEndian.parseLittleEndian(c));
    parseMap.put(Encoding.singleByte, c -> SingleByte.parseSingleByte(c));
    parseMap.put(Encoding.unknown, c -> this.parseUnknown(c));
  }

  // identifyEncoding identifies the encoding type of the data section by
  // only comparing header characteristics.
  // This method requires header data to be already written.
  public Encoding identifyEncoding(Composite c) {
    int values = c.getPx() * c.getPy();
    if (c.level != null) {
      return Encoding.runlength;
    } else if (c.getDataLength() == values * 2) {
      return Encoding.littleEndian;
    } else if (c.getDataLength() == values) {
      return Encoding.singleByte;
    } else {
      return Encoding.unknown;
    }
  }

  // parseData parses the composite data and writes the related fields.
  // This method requires header data to be already written.
  public void parseData(Composite c) throws Throwable {
    if (c.getPx() == 0 || c.getPy() == 0) {
      c.error = new Exception("parseData - parsed header data required");
      return;
    }

    // create Data fields
    c.PlainData = new float[c.getPy()][c.getPx()];
    for (int i = 0; i < c.PlainData.length; i++) {
      c.PlainData[i] = new float[c.getPx()];
    }
    Encoding encoding = identifyEncoding(c);
    Consumer<Composite> parser = parseMap.get(encoding);
    if (parser != null) {
      parser.accept(c);
      if (c.error!=null)
        throw c.error;
    } else
      c.error = new Exception("no parser for encoding " + encoding);
  }

  // arrangeData slices plain data into its data layers or strips preceeding
  // vertical projection
  public void arrangeData(Composite c) {
    /*
     * if c.Py%c.Dy == 0 { // multiple layers are linked downwards
     * c.DataZ = make([][][]float32, c.Py/c.Dy)
     * for i := range c.DataZ {
     * c.DataZ[i] = c.PlainData[c.Dy*i : c.Dy*(i+1)] // split layers
     * }
     * } else { // only use bottom most part of plain data
     * c.DataZ = [][][]float32{c.PlainData[c.Py-c.Dy:]} // strip elevation
     * }
     * 
     * c.Dz = len(c.DataZ)
     * c.Data = c.DataZ[0] // alias
     */
  }

  // parseUnknown performs no action and always returns an error.
  public void parseUnknown(Composite c) {
    c.error = new Exception("parseUnknown - unknown encoding");
  }

  private static Data instance;

  public static Data getInstance() {
    if (instance == null)
      instance = new Data();
    return instance;
  }
}
