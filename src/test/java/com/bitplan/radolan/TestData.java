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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import cs.fau.de.since.radolan.Composite;
import cs.fau.de.since.radolan.Data.Encoding;

/**
 * test access to the OpenData results of DWD
 * 
 * @author wf
 *
 */
public class TestData extends BaseTest {

  private String csv;
  private Composite composite;

  @Test
  public void testPrecision() {
    int[] precisions = { 2, 1, 0, -1, -2 };
    float[] factors = { 100f, 10f, 1f, 0.1f, 0.01f };
    Composite c = new Composite();
    int i = 0;
    for (int precision : precisions) {
      c.setPrecision(precision);
      assertEquals(factors[i], c.getPrecisionFactor(), 0.00001);
      assertEquals(Math.pow(10, precision), c.getPrecisionFactor(), 0.00001);
      i++;
    }

  }

  @Test
  public void testOpendata() throws Throwable {
    // Composite.activateDebug();
    String products[] = { "rw", "ry", "sf" };
    for (String product : products) {
      String url = String.format(
          "https://opendata.dwd.de/weather/radar/radolan/%s/raa01-%s_10000-latest-dwd---bin",
          product, product);
      Composite c = new Composite(url);
      checkLittleEndian(c, product.toUpperCase());
      System.out.println(product + "->" + c.getStatistics());
    }
  }

  @Ignore
  public void testSF1805301650() throws Throwable {
    String history = "src/test/data/history/";
    debug=true;
    if (debug)
      Debug.activateDebug();
    File sfHistoryFile = new File(
        history + "raa01-sf_10000-1805301650-dwd---bin.gz");
    assertTrue(sfHistoryFile.getName(), sfHistoryFile.exists());
    String url = sfHistoryFile.toURI().toURL().toExternalForm();
    composite = new Composite(url);
    checkLittleEndian(composite, "SF");
    System.out.println(composite.getStatistics());
    // show the original
    super.testRadolan(url, 5, "sf-history2018-05-30_1650.png", null);

    // now get the content of the zip file
    File zippedCsvFile = new File(
        history + "raa01-sf_10000-1805301650-dwd---bin.txt.gz");
    assertTrue(zippedCsvFile.getName(), zippedCsvFile.exists());
    csv = gzipFileToString(zippedCsvFile);
    assertNotNull(csv);
    assertEquals(10275223, csv.length());
    // apply it with readCSV and show results
    super.testRadolan(url, 5, "sf-history2018-05-30_1650JS.png", readCsv);
    // read original again
    Composite.setPostInit(readCsv);
    Composite cr = new Composite(url);
    int errCount = 0;
    int limit = 2000;
    // check the whole grid

    for (int y = 0; y < cr.getGridHeight(); y++) {
      for (int x = 0; x < cr.getGridWidth(); x++) {
        float cval = composite.getValue(x, y);
        float crval = cr.getValue(x, y);
        if (Math.abs(crval - cval) > 0.001) {
          errCount++;
          if (errCount < limit) {
            float d10 = (crval-cval)*10;
            System.out.println(
                String.format("%3d,%3d: %5.1f != %5.1f d*10=%5.1f", x, y, cval, crval,d10));
          }
        }
      }
    }
    assertEquals(0, errCount);
  }

  /**
   * set the values from a csv file
   */
  public Consumer<Composite> readCsv = composite -> {
    System.out.println(composite.getStatistics());
    composite.getStatistics().clear();
    Scanner scanner = new Scanner(csv);
    scanner.useDelimiter(";|\\n");

    while (scanner.hasNext()) {
      int y = Integer.parseInt(scanner.next());
      int x = Integer.parseInt(scanner.next());
      float val = Float.NaN;
      String floatStr = scanner.next();
      if (!floatStr.equals("Nan"))
        val = Float.parseFloat(floatStr);
      composite.setValue(x, y, val);
    }
    scanner.close();
    System.out.println(composite.getStatistics());
  };

  /**
   * get the utf-8 String content of the given gzipped file
   * 
   * @param file
   * @return the text read
   * @throws Exception
   */
  public static String gzipFileToString(File file) throws Exception {
    GZIPInputStream gzipIn = new GZIPInputStream(new FileInputStream(file));
    String text = IOUtils.toString(gzipIn, "UTF-8");
    return text;
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
    // assertEquals(-1,c.getPrecision());
    // assertEquals(0.1,c.getPrecisionFactor(),0.0001);
    assertEquals(c.getDx() * c.getDy() * 2, c.getDataLength());
    assertEquals(c.getDataLength() + c.header.length(), c.bytes.length);
    assertEquals(c.PlainData.length, c.getDy());
    assertEquals(Encoding.littleEndian, c.identifyEncoding());
  }
}
