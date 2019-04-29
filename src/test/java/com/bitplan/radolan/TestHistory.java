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
package com.bitplan.radolan;

import java.time.LocalDate;
import java.time.Period;
import java.util.Date;

import org.junit.Test;

import com.bitplan.dateutils.DateUtils;
import com.bitplan.geo.DPoint;

import cs.fau.de.since.radolan.Composite;

/**
 * test the History 
 * @author wf
 *
 */
public class TestHistory extends BaseTest {

  @Test
  public void testRainEventSequence() throws Throwable {
    // if (!super.isTravis()) {
    // Composite.debug = true;
    CompositeManager cm=new CompositeManager();
    LocalDate today = DateUtils.asLocalDate(new Date());
    DPoint schiefbahn = new DPoint(51.244, 6.52);
    for (int dayOfs=1;dayOfs<=14;dayOfs++) {
      LocalDate day=today.minus(Period.ofDays(dayOfs));
      Composite comp=cm.getRainSum(day);
      float rain=comp.getValueAtCoord(schiefbahn);
      Date wdate = DateUtils.asDate(day);
      System.out.println(String.format("%3d: %s %5.2f mm",dayOfs,
          KnownUrl.hourFormat.format(wdate), rain));
    }
  }

}
