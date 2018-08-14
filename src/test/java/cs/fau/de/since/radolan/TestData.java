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

public class TestData {

  @Test
  public void testRW() throws Throwable {
    Composite rw=new Composite("https://opendata.dwd.de/weather/radar/radolan/rw/raa01-rw_10000-latest-dwd---bin");
    assertEquals("RW",rw.Product);
    assertEquals(900,rw.Dx);
    assertEquals(900,rw.Dy);
    assertEquals(rw.Dx*rw.Dy*2,rw.dataLength);
    assertEquals(rw.dataLength+rw.header.length(),rw.bytes.length);
    assertEquals(Encoding.littleEndian,rw.identifyEncoding());
  }
}
