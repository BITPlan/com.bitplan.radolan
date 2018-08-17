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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import org.junit.Test;

import cs.fau.de.since.radolan.Composite;
import cs.fau.de.since.radolan.FloatFunction;
import cs.fau.de.since.radolan.vis.Vis;
import cs.fau.de.since.radolan.vis.Vis.ColorRange;
import javafx.scene.paint.Color;

/**
 * test the main application
 * 
 * @author wf
 *
 */
public class TestRadolan extends BaseTest {

  String tmpDir = System.getProperty("java.io.tmpdir");

  /**
   * test radolan display
   * 
   * @param url
   * @param viewTimeSecs
   * @param fakeGradient2
   */
  public void testRadolan(String url, int viewTimeSecs, String output,
      Consumer<Composite> postInit) {
    Composite.setPostInit(postInit);
    String outputPath = "";
    if (output != null)
      outputPath = tmpDir + "/" + output;
    String args[] = { "-d", "-i", url, "-t", "" + viewTimeSecs, "-o",
        outputPath };
    Radolan.testMode = true;
    Radolan.main(args);
    if (output != null) {
      File outputFile = new File(outputPath);
      assertTrue(outputFile.getPath(), outputFile.exists());
      System.out.println(outputFile.getAbsolutePath());
    }
  }

  @Test
  public void testOpenData() {
    String products[] = { "sf", "rw", "ry" };
    for (String product : products) {
      String url = String.format(
          "https://opendata.dwd.de/weather/radar/radolan/%s/raa01-%s_10000-latest-dwd---bin",
          product, product);
      testRadolan(url, 4, product + ".png", null);
    }
  }

  /**
   * test the OpenData for the given product ry/rw for the past minutes taken into
   * account the given delay in minutes
   * @param product - the product ry/rw
   * @param minutes - how many minutes of the previous period to cover
   * @param minDelay - the delay of this product
   * @param startMin - the start Minute e.g. 50 for rw 5 for ry
   * @param minRaster - the raster of this product e.g. 60 for rw 5 for ry
   */
  public void testOpenDataRecent(String product,int minutes, int minDelay,int startMin, int minRaster) {
    LocalDateTime starttime = LocalDateTime
        .ofInstant(Instant.now(), ZoneOffset.UTC)
        .truncatedTo(ChronoUnit.MINUTES);
    // safety marging
    starttime = starttime.minus(Duration.ofMinutes(minDelay));
    while (starttime.getMinute() % startMin != 0) {
      starttime = starttime.minus(Duration.ofMinutes(1));
    }
    LocalDateTime datetime=starttime;
    LocalDateTime endtime = datetime.minus(Duration.ofMinutes(minutes));
    for (; datetime
        .isAfter(endtime); datetime = datetime.minus(Duration.ofMinutes(minRaster))) {
      String url = (String.format(
          "https://opendata.dwd.de/weather/radar/radolan/%s/raa01-%s_10000-%02d%02d%02d%02d%02d-dwd---bin",
          product,product,datetime.getYear() % 2000, datetime.getMonthValue(),
          datetime.getDayOfMonth(), datetime.getHour(), datetime.getMinute()));
      String picture = String.format("%s-%04d-%02d-%02d_%02d%02d.png",
          product,datetime.getYear(), datetime.getMonthValue(),
          datetime.getDayOfMonth(), datetime.getHour(), datetime.getMinute());
      testRadolan(url, 4, picture, null);
    }
  }
  
  @Test
  public void testOpenDataRecent() {
    if (isTravis()) {
      int rwminutes=60*2;
      int ryminutes=10;
      this.testOpenDataRecent("rw",rwminutes,80,50,60);  
      this.testOpenDataRecent("ry",ryminutes,5,5,5);
    }
  }

  @Test
  public void testHistory() {
    if (!isTravis()) {
      LocalDate date = LocalDate.of(2018, 1, 1);
      LocalDate end = LocalDate.of(2018, 1, 5); // actually one day before this
                                                // ...
      String knownUrl = "ftp://ftp-cdc.dwd.de/pub/CDC/grids_germany/daily/radolan/";
      do {
        String url = String.format(
            knownUrl + "recent/raa01-sf_10000-%02d%02d%02d1650-dwd---bin.gz",
            date.getYear() % 2000, date.getMonthValue(), date.getDayOfMonth());
        if (!Composite.cacheForUrl(url, knownUrl).exists() || date.isEqual(end))
          break;
        testRadolan(url, 2, String.format("sf-%04d-%02d-%02d_1650.png",
            date.getYear(), date.getMonthValue(), date.getDayOfMonth()), null);
        date = date.plus(Period.ofDays(1));
      } while (true);
    }
  }

  /**
   * create a fake gradient of values
   */
  public static Consumer<Composite> fakeGradient = composite -> {
    for (int y = 0; y < composite.PlainData.length; y++) {
      for (int x = 0; x < composite.PlainData[y].length; x++) {
        composite.setValue(x, y, (float) (y / 900.0 * 250.0));
      }
    }
  };

  @Test
  public void testHistoryLocalWithFakeValueGradient() throws Exception {
    File sfHistoryFile = new File(
        "src/test/data/history/raa01-sf_10000-1805301650-dwd---bin.gz");
    assertTrue(sfHistoryFile.exists());
    String url = sfHistoryFile.toURI().toURL().toExternalForm();
    testRadolan(url, 5, "sf-ColorGradient.png", fakeGradient);
  }

  @Test
  public void testColors() {
    FloatFunction<Color> heatmap = Vis.RangeMap(Vis.DWD_Style_Colors);
    // taken from
    // https://www.dwd.de/DE/leistungen/radolan/radolan_info/sf_karte.png?view=nasImage&nn=16102

    for (ColorRange colorRange : Vis.DWD_Style_Colors) {
      // check upper and lower bound of color and a value in the middle
      // all three should have the same color
      float testValues[] = { colorRange.getFromValue(), colorRange.getToValue(),
          (colorRange.getFromValue() + colorRange.getToValue()) / 2.0f };
      for (float value : testValues) {
        Color rColor = heatmap.apply(value);
        assertEquals(colorRange.getColor(), rColor);
      }
    }
  }

  @Test
  public void testRadarPicture() {
    String url = "https://www.dwd.de/DWD/wetter/radar/rad_brd_akt.jpg";
    testRadolan(url, 3, "rad_brd_akt.png", null);
  }

  @Test
  public void testRadarfilm() {
    String url = "https://www.dwd.de/DWD/wetter/radar/radfilm_brd_akt.gif";
    testRadolan(url, 12, null, null);
  }
}
