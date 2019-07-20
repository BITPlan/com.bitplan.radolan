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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import org.junit.Test;

import com.bitplan.javafx.WaitableApp;
import com.bitplan.util.CachedUrl;

import cs.fau.de.since.radolan.Composite;
import cs.fau.de.since.radolan.FloatFunction;
import cs.fau.de.since.radolan.vis.Vis;
import cs.fau.de.since.radolan.vis.Vis.ColorRange;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * test the main application
 * 
 * @author wf
 *
 */
public class TestRadolan extends BaseTest {

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
   * test the OpenData for the given product ry/rw for the past minutes taken
   * into account the given delay in minutes
   * 
   * @param product
   *          - the product ry/rw
   * @param minutes
   *          - how many minutes of the previous period to cover
   * @param minDelay
   *          - the delay of this product
   * @param startMin
   *          - the start Minute e.g. 50 for rw 5 for ry
   * @param minRaster
   *          - the raster of this product e.g. 60 for rw 5 for ry
   * @throws Exception
   */
  public void testOpenDataRecent(String product, int minutes, int minDelay,
      int startMin, int startRest, int minRaster) throws Exception {
    LocalDateTime starttime = LocalDateTime
        .ofInstant(Instant.now(), ZoneOffset.UTC)
        .truncatedTo(ChronoUnit.MINUTES);
    // safety margin
    starttime = starttime.minus(Duration.ofMinutes(minDelay));
    int count = 0;
    while ((starttime.getMinute() % startMin) != startRest) {
      starttime = starttime.minus(Duration.ofMinutes(1));
      if (count++ > 20 * 365 * 24 * 60) {
        throw new Exception(
            "'endless' loop detected in finding recent data start");
      }
    }
    LocalDateTime datetime = starttime;
    LocalDateTime endtime = datetime.minus(Duration.ofMinutes(minutes));
    for (; datetime.isAfter(
        endtime); datetime = datetime.minus(Duration.ofMinutes(minRaster))) {
      String url = (String.format(
          "https://opendata.dwd.de/weather/radar/radolan/%s/raa01-%s_10000-%02d%02d%02d%02d%02d-dwd---bin",
          product, product, datetime.getYear() % 2000, datetime.getMonthValue(),
          datetime.getDayOfMonth(), datetime.getHour(), datetime.getMinute()));
      String picture = String.format("%s-%04d-%02d-%02d_%02d%02d.png", product,
          datetime.getYear(), datetime.getMonthValue(),
          datetime.getDayOfMonth(), datetime.getHour(), datetime.getMinute());
      testRadolan(url, 4, picture, null);
    }
  }

  @Test
  public void testOpenDataRecent() throws Exception {
    int rwminutes = 60 * 2;
    int ryminutes = 10;
    this.testOpenDataRecent("rw", rwminutes, 30, 60, 50, 60);
    this.testOpenDataRecent("ry", ryminutes, 5, 5, 0, 5);
  }

  @Test
  public void testHistory() {
    if (!isTravis()) {
      // debug=true;
      LocalDate date = LocalDate.of(2019, 4, 13);
      LocalDate end = LocalDate.of(2019, 5, 16); // actually one day before this
                                                 // ...
      String knownUrl = "ftp://ftp-cdc.dwd.de/climate_environment/CDC/grids_germany/daily/radolan/";
      do {
        String url = String.format(
            knownUrl + "recent/raa01-sf_10000-%02d%02d%02d1650-dwd---bin.gz",
            date.getYear() % 2000, date.getMonthValue(), date.getDayOfMonth());
        if (!CachedUrl.cacheForUrl(url, knownUrl).exists() || date.isEqual(end))
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

  String rad_brd_akt_url = "https://www.dwd.de/DWD/wetter/radar/rad_brd_akt.jpg";
  String radar_film_url = "https://www.dwd.de/DWD/wetter/radar/radfilm_brd_akt.gif";

  @Test
  public void testRadarPicture() {
    testRadolan(rad_brd_akt_url, 4, "rad_brd_akt.png", null);
  }

  @Test
  public void testImage() throws InterruptedException {
    Radolan.disableSslVerification();
    WaitableApp.toolkitInit();
    String urls[] = {
        "https://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/Flag_of_Germany.svg/320px-Flag_of_Germany.svg.png",
        "https://via.placeholder.com/50x50", rad_brd_akt_url, radar_film_url };
    int expectedWidth[][] = { { 160, 25, 540, 540 }, { 320, 50, 540, 540 } };
    int expectedHeight[][] = { { 96, 251, 500, 500 }, { 192, 50, 500, 500 } };
    boolean backgrounds[] = { false, true };
    int i = 0;
    for (boolean background : backgrounds) {
      int j = 0;
      for (String url : urls) {
        Image image = new Image(url, background);
        assertNotNull(image);
        if (background)
          while (image.getProgress() < 1)
            Thread.sleep(40);
        if (image.isError())
          System.out.println(image.getException().getMessage());
        else {
          if (!(super.isTravis() && !background)) {
            assertEquals("w " + i + "," + j, expectedWidth[i][j],
                image.getWidth(), 0.1);
            assertEquals("h " + i + "," + j, expectedHeight[i][j],
                image.getHeight(), 0.1);
          }
        }
        j++;
      }
      i++;
    }
  }

  @Test
  public void testRadarfilm() {
    testRadolan(radar_film_url, 12, null, null);
  }
}
