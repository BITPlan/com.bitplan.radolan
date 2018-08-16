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
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

/**
 * migrated to Java from
 * https://gitlab.cs.fau.de/since/radolan/blob/master/header_test.go
 * 
 * @author wf
 *
 */
public class TestHeader extends Testing {

  class HeaderTestCase {
    // head of file
    String test;

    // expected
    String expBinary;
    String expProduct;
    ZonedDateTime expCaptureTime;
    ZonedDateTime expForecastTime;
    Duration expInterval;
    int expDx;
    int expDy;
    int expDataLength;
    int expPrecision;
    float[] expLevel;
  }

  @Test
  public void TestParseHeaderPG() throws Exception {
    HeaderTestCase ht = new HeaderTestCase();
    // var err1, err2 error

    // head of file
    ht.test = "PG262115100000616BY22205LV 6  1.0 19.0 28.0 37.0 46.0 55.0CS0MX 0MS "
        + "88<boo,ros,emd,hnr,umd,pro,ess,fld,drs,neu,nhb,oft,eis,tur,isn,fbg,mem> "
        + "are used, BG460460\u0003binarycontent";
    float[] expLevel = { 1.0f, 19.0f, 28.0f, 37.0f, 46.0f, 55.0f };

    // expected
    ht.expBinary = "binarycontent";
    ht.expProduct = "PG";
    ht.expCaptureTime = ZonedDateTime.parse("Sun, 26 Jun 2016 21:15:00 GMT",
        DateTimeFormatter.RFC_1123_DATE_TIME);
    ht.expForecastTime = ZonedDateTime.parse("Sun, 26 Jun 2016 21:15:00 GMT",
        DateTimeFormatter.RFC_1123_DATE_TIME);
    ht.expDx = 460;
    ht.expDy = 460;
    ht.expDataLength = 22205 - 159; // BY - header_etx_length
    ht.expPrecision = 0;
    ht.expLevel = expLevel;
    ht.expInterval=Duration.ofMinutes(0);
    /*
     * if err1 != nil || err2 != nil {
     * t.Errorf("%s.parseHeader(): wrong testcase time.Parse", ht.expProduct);
     * }
     */
    testParseHeader(ht);
  }

  @Test
  public void TestParseHeaderFZ() throws Exception {
    HeaderTestCase ht = new HeaderTestCase();
    // var err1, err2 error

    // head of file
    ht.test = "FZ282105100000716BY 405160VS 3SW   2.13.1PR E-01INT   5GP 450x 450VV 100MF "
        + "00000002MS 66<boo,ros,emd,hnr,umd,pro,ess,drs,neu,nhb,oft,eis,tur,isn,fbg,mem>"
        + "\u0003binarycontent";

    // ht.expected values
    ht.expBinary = "binarycontent";

    ht.expProduct = "FZ";
    ht.expCaptureTime = ZonedDateTime.parse("Thu, 28 Jul 2016 21:05:00 GMT",
        DateTimeFormatter.RFC_1123_DATE_TIME);
    ht.expForecastTime = ZonedDateTime.parse("Thu, 28 Jul 2016 22:45:00 GMT",
        DateTimeFormatter.RFC_1123_DATE_TIME);
    ht.expInterval = Duration.ofMinutes(5); // time.Minute;
    ht.expDx = 450;
    ht.expDy = 450;
    ht.expDataLength = 405160 - 154; // BY - header_etx_length
    ht.expPrecision = -1;
    float[] expLevel = {};
    ht.expLevel = expLevel;
    /*
     * if err1 != nil || err2 != nil {
     * t.Errorf("%s.parseHeader(): wrong testcase time.Parse", ht.expProduct);
     * }
     */
    testParseHeader(ht);
  }

  public void testParseHeader(HeaderTestCase ht) throws Exception {
    Composite dummy = new Composite();

    dummy.read(
        new ByteArrayInputStream(ht.test.getBytes(StandardCharsets.UTF_8)));
    /*
     * err != nil {
     * t.Errorf("%s.parseHeader(): returned error: %#v", err.Error())
     * }
     */

    // test results
    // Product
    if (!ht.expProduct.equals(dummy.getProduct())) {
      Errorf("%s.parseHeader(): Product: %s; expected: %s", ht.expProduct,
          dummy.getProduct(), ht.expProduct);
    }
    // CaptureTime
    assertNotNull("CaptureTime should be set for "+ht.expProduct,dummy.CaptureTime);
    if (!ht.expCaptureTime.equals(dummy.CaptureTime)) {
      Errorf("%s.parseHeader(): CaptureTime: %s; expected: %s", ht.expProduct,
          dummy.CaptureTime.toString(), ht.expCaptureTime.toString());
    }
    assertNotNull("ForecastTime should be set for "+ht.expProduct,dummy.getForecastTime());
    // ForecastTime
    if (!ht.expForecastTime.equals(dummy.getForecastTime())) {
      Errorf("%s.parseHeader(): ForecastTime: %s; expected: %s", ht.expProduct,
          dummy.getForecastTime().toString(), ht.expForecastTime.toString());
    }

    // Interval
    if (!ht.expInterval.equals(dummy.Interval)) {
      Errorf("%s.parseHeader(): Interval: %s; expected: %s", ht.expProduct,
          dummy.Interval.toString(), ht.expInterval.toString());
    }

    // Dx Dy
    if (dummy.getDx() != ht.expDx || dummy.getDy() != ht.expDy) {
      Errorf("%s.parseHeader(): Dx: %d Dy: %d; expected Dx: %d Dy: %d",
          ht.expProduct, dummy.getDx(), dummy.getDy(), ht.expDx, ht.expDy);
    }

    // dataLength
    if (dummy.dataLength != ht.expDataLength) {
      Errorf("%s.parseHeader(): dataLength: %d; expected: %d", ht.expProduct,
          dummy.dataLength, ht.expDataLength);
    }

    // precision
    if (dummy.getPrecision() != ht.expPrecision) {
      Errorf("%s.parseHeader(): precision: %#v; expected: %#v", ht.expProduct,
          dummy.getPrecision(), ht.expPrecision);
    }
    // level
    for (int i = 0; i < ht.expLevel.length; i++) {
      if (dummy.level.length != ht.expLevel.length
          || dummy.level[i] != ht.expLevel[i]) {
        Errorf("%s.parseHeader(): level: %s; expected: %s", ht.expProduct,
            dummy.level, ht.expLevel);
      }
    }

    // check consistency
    for (int i=0;i<ht.expBinary.length();i++) {
      assertEquals("binary data corrupted",ht.expBinary.charAt(i),(char)dummy.bytes[dummy.header.length()+i]);
    }
  }

}
