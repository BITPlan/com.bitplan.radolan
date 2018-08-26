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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import com.bitplan.display.MapView;
import com.bitplan.javafx.Main;

import cs.fau.de.since.radolan.Composite;
import javafx.application.Platform;
import javafx.scene.image.Image;

/**
 * Main for radolan.jar and radolan.exe
 * 
 * @author wf
 *
 */
public class Radolan extends Main {

  @Option(name = "-cp", aliases = {
      "--cachePath" }, usage = "path to Cache\nthe path to the Cache")
  protected String cachePath = System.getProperty("user.home")
      + java.io.File.separator + ".radolan";;

  @Option(name = "-b", aliases = { "--borderName" }, usage = "borderName\n")
  protected String borderName = "2_bundeslaender/2_hoch.geojson";

  @Option(name = "-i", aliases = {
      "--input" }, usage = "input\nurl/file of the input")
  protected String input = "https://www.dwd.de/DWD/wetter/radar/radfilm_brd_akt.gif"; // "https://www.dwd.de/DWD/wetter/radar/rad_brd_akt.jpg";

  @Option(name = "-nc", aliases = {
      "--noCache" }, usage = "noCache\ndo not use local cache")
  protected boolean noCache = false;

  @Option(name = "-l", aliases = {
      "--location" }, usage = "location/show data at the given location")
  protected String location;

  @Option(name = "-o", aliases = {
      "--output" }, usage = "output/e.g. path of png/jpg/gif file")
  protected String output;

  @Option(name = "-p", aliases = {
      "--product" }, usage = "product e.g. SF,RW,RY or alias daily,hourly,5min")
  protected String product = "sf";

  @Option(name = "-s", aliases = {
      "--show" }, usage = "show\nshow resulting image")
  protected boolean showImage = true;

  @Option(name = "-st", aliases = {
      "--showTime" }, usage = "showTime\nshow result for the given time in seconds")
  protected int showTimeSecs = Integer.MAX_VALUE / 1100; // over 20 years

  @Option(name = "-z", aliases = {
      "--zoom" }, usage = "zoom/zoom to a grid size of zxz km")
  protected double zoomKm = 1.0;

  @Argument
  private List<String> arguments = new ArrayList<String>();

  private DisplayContext displayContext;

  /**
   * stop the application with the given exit Code
   * 
   * @param pExitCode
   */
  public void stop(int pExitCode) {
    Platform.runLater(() -> {
      Platform.exit();
    });
    exitCode = pExitCode;
    if (!testMode) {
      System.exit(pExitCode);
    }
  }

  /**
   * prepare the ImageViewer
   */
  protected void prepareImageViewer() {
    ImageViewer.testMode = testMode;
    ImageViewer.toolkitInit();
  }

  /**
   * show the given image
   * 
   * @param displayContext
   *          - the image andit's details
   * @throws Exception
   */
  public void showImage(DisplayContext displayContext) throws Exception {
    prepareImageViewer();
    ImageViewer imageViewer = new ImageViewer(displayContext);
    imageViewer.limitShowTime(this.showTimeSecs);
    imageViewer.show(); // will set view and toolTip
    imageViewer.waitOpen();
    // do we show a radolan image?
    if (displayContext.composite != null) {
      Radolan2Image.getImage(displayContext);
      Radolan2Image.activateEvents(displayContext);
    }
    imageViewer.waitClose();
  }

  /**
   * show the image from the given input
   * 
   * @param input
   * @throws Exception
   */
  public void showImage(String input) throws Exception {
    prepareImageViewer();
    Image image = new Image(input);
    displayContext = new DisplayContext(null, null, null, zoomKm, null);
    displayContext.mapView = new MapView(image);
    displayContext.title = input;
    showImage(displayContext);
  }

  /**
   * do the required work
   */
  public void work() {
    try {
      if (debug) {
        Composite.activateDebug();
      }
      if (noCache) {
        Composite.useCache = false;
      } else {
        Composite.cacheRootPath = cachePath;
      }
      if (showVersion)
        this.showVersion();
      if (showHelp)
        this.showHelp();
      else {
        if (this.arguments.size() > 0) {
          if (debug)
            LOGGER.log(Level.INFO, arguments.toString());
          for (String argument : arguments) {
            input = KnownUrl.getUrl(product, argument);
            this.showCompositeForUrl(input);
          }
        } else if (input.contains(".png") || input.contains(".jpg")
            || input.contains(".gif")) {
          if (this.showImage)
            showImage(input);
          else {
            // silently fail here?
          }
        } else {
          this.showCompositeForUrl(input);
        }
        // shall we save the (latest) image
        if (displayContext != null && displayContext.mapView.getImage() != null
            && output != null && !output.isEmpty()) {
          String ext = FilenameUtils.getExtension(output);
          File outputFile = new File(output);
          BufferedImage bImage = ImageViewer
              .fromFXImage(displayContext.mapView.getImage(), null);
          String formatName = ext;
          ImageIO.write(bImage, formatName, outputFile);
        }
      }
    } catch (Throwable th) {
      ErrorHandler.handle(th);
    }
  }

  /**
   * show the composite for the given url
   * 
   * @param url
   * @throws Throwable
   */
  public void showCompositeForUrl(String url) throws Throwable {
    Composite composite = new Composite(url);
    displayContext = new DisplayContext(composite, borderName,
        Radolan2Image.borderColor, zoomKm, location);
    displayContext.title = String.format("%s-image (%s) showing %s",
        composite.getProduct(), composite.getDataUnit(),
        composite.getForecastTime());
    if (debug)
      LOGGER.log(Level.INFO, displayContext.title);
    if (this.showImage)
      showImage(displayContext);
  }

  /**
   * main routine
   * 
   * @param args
   */
  public static void main(String[] args) {
    Radolan radolan = new Radolan();
    radolan.maininstance(args);
    if (!testMode)
      System.exit(exitCode);
  }

  @Override
  public String getSupportEMail() {
    return "support@bitplan.com";
  }

  @Override
  public String getSupportEMailPreamble() {
    return "Dear Radolan user";
  }
}
