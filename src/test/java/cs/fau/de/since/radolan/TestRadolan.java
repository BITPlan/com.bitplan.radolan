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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.bitplan.radolan.Radolan;

/**
 * test the main application
 * 
 * @author wf
 *
 */
public class TestRadolan {

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
    String args[] = { "-i", url, "-t", "" + viewTimeSecs, "-o", outputPath };
    Radolan.testMode = true;
    Radolan.main(args);
    if (output != null) {
      File outputFile = new File(outputPath);
      assertTrue(outputFile.exists());
      System.out.println(outputFile.getAbsolutePath());
    }
  }

  @Test
  public void testSF() {
    String url = "https://opendata.dwd.de/weather/radar/radolan/sf/raa01-sf_10000-latest-dwd---bin";
    testRadolan(url, 5, "SF.png");
  }
  
  @Test
  public void testRW() {
    String url = "https://opendata.dwd.de/weather/radar/radolan/rw/raa01-rw_10000-latest-dwd---bin";
    testRadolan(url, 5, "RW.png");
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
