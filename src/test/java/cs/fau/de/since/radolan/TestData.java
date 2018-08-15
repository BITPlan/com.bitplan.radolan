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

import org.junit.Test;

import cs.fau.de.since.radolan.Data.Encoding;

/**
 * test access to the OpenData results of DWD
 * 
 * @author wf
 *
 */
public class TestData {

  @Test
  public void testOpendata() throws Throwable {
    String products[] = { "rw", "ry", "sf" };
    for (String product : products) {
      String url = String.format(
          "https://opendata.dwd.de/weather/radar/radolan/%s/raa01-%s_10000-latest-dwd---bin",
          product, product);
      Composite c = new Composite(url);
      checkLittleEndian(c, product.toUpperCase());
    }
  }

  /**
   * check the given composite
   * 
   * @param c
   *          - the composite to check
   * @param product
   */
  public void checkLittleEndian(Composite c, String product) {
    assertEquals(product, c.getProduct());
    assertEquals(900, c.getDx());
    assertEquals(900, c.getDy());
    assertEquals(c.getDx() * c.getDy() * 2, c.dataLength);
    assertEquals(c.dataLength + c.header.length(), c.bytes.length);
    assertEquals(c.PlainData.length,c.getDy());
    assertEquals(Encoding.littleEndian, c.identifyEncoding());
  }
}
