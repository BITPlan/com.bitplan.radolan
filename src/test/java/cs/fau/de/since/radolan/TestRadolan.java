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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.function.Consumer;

import org.junit.Test;

import com.bitplan.radolan.Radolan;

import cs.fau.de.since.radolan.vis.Vis;
import cs.fau.de.since.radolan.vis.Vis.ColorRange;
import javafx.scene.paint.Color;

/**
 * test the main application
 * 
 * @author wf
 *
 */
@SuppressWarnings("restriction")
public class TestRadolan {
  /**
   * check if we are in the Travis-CI environment
   * 
   * @return true if Travis user was detected
   */
  public boolean isTravis() {
    String user = System.getProperty("user.name");
    return user.equals("travis");
  }

  String tmpDir = System.getProperty("java.io.tmpdir");

  /**
   * test radolan display
   * 
   * @param url
   * @param viewTimeSecs
   */
  public void testRadolan(String url, int viewTimeSecs, String output) {
    String outputPath = "";
    if (output != null)
      outputPath = tmpDir + "/" + output;
    String args[] = { "-d", "-i", url, "-t", "" + viewTimeSecs, "-o",
        outputPath };
    Radolan.testMode = true;
    Radolan.main(args);
    if (output != null) {
      File outputFile = new File(outputPath);
      assertTrue(outputFile.exists());
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
      testRadolan(url, 4, product + ".png");
    }
  }

  @Test
  public void testHistory() {
    if (!isTravis()) {
      String url = "ftp://ftp-cdc.dwd.de/pub/CDC/grids_germany/daily/radolan/recent/raa01-sf_10000-1805301650-dwd---bin.gz";
      testRadolan(url, 75, "sf-2018-05-30_1650.png");
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
    Composite.setPostInit(fakeGradient);
    File sfHistoryFile = new File(
        "src/test/data/history/raa01-sf_10000-1805301650-dwd---bin.gz");
    assertTrue(sfHistoryFile.exists());
    String url = sfHistoryFile.toURI().toURL().toExternalForm();
    testRadolan(url, 5, "sf-ColorGradient.png");
  }

  @Test
  public void testColors() {
    FloatFunction<Color> heatmap = Vis.RangeMap(Vis.DWD_Style_Colors);
    // taken from https://www.dwd.de/DE/leistungen/radolan/radolan_info/sf_karte.png?view=nasImage&nn=16102
   
    for (ColorRange colorRange:Vis.DWD_Style_Colors) {
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
    testRadolan(url, 3, "rad_brd_akt.png");
  }

  @Test
  public void testRadarfilm() {
    String url = "https://www.dwd.de/DWD/wetter/radar/radfilm_brd_akt.gif";
    testRadolan(url, 12, null);
  }
}
