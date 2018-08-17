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

import org.junit.Test;

import com.bitplan.radolan.Testing;

import static cs.fau.de.since.radolan.Conversion.*;
/**
 * migrated to java from https://gitlab.cs.fau.de/since/radolan/blob/master/conversion_test.go
 * @author wf
 *
 */
public class TestConversion extends Testing {
  class TestCase {
    float rvp;
    float dbz;
    double zr;
    public TestCase(float rvp, float dbz, double zr) {
      this.rvp=rvp;
      this.dbz=dbz;
      this.zr=zr;
    }
  }
  
  @Test
  public void testConversion() {
  TestCase[] testcases= {
  new TestCase(0f, -32.5f, 0.0001),
  new TestCase(65f, 0f, 0.0201),
  new TestCase(100f, 17.5f, 0.3439),
  new TestCase(200f, 67.5f, 1141.7670),
    };

  for (TestCase test:testcases) {
      float dbz = toDBZ(test.rvp);
      double zr = PrecipitationRate(Aniol80, dbz);
      double rz = Reflectivity(Aniol80, zr);
      double rvp = toRVP6(dbz);

      if (dbz != test.dbz) {
        Errorf("toDBZ(%f) = %f; expected: %f", test.rvp, dbz, test.dbz);
      }
      if (rvp != test.rvp) {
        Errorf("toRVP6(toDBZ(%f)) = %f; expected: %f", test.rvp, rvp, test.rvp);
      }
      if (Math.abs(test.zr-zr) > 0.0001) {
        Errorf("PrecipitationRate(Aniol80, toDBZ(%f)) = %f; expected: %f", test.rvp, zr, test.zr);
      }
      if (Math.abs((double)(test.dbz-rz)) > 0.0000001) {
        Errorf("Reflectivity(PrecipitationRate(toDBZ(%f))) = %f; expected: %f",
          test.rvp, rz, test.dbz);
      }
    }
  }

}
