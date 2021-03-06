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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import com.bitplan.display.MapView;
import com.bitplan.error.SoftwareVersion;
import com.bitplan.i18n.Translator;
import com.bitplan.javafx.Main;
import com.bitplan.util.CachedUrl;

import cs.fau.de.since.radolan.Composite;
import de.dwd.geoserver.StationManager;
import javafx.application.Platform;
import javafx.scene.image.Image;

/**
 * Main for radolan.jar and radolan.exe
 * 
 * @author wf
 *
 */
public class Radolan extends Main implements SoftwareVersion {

  @Option(name = "-cp", aliases = {
      "--cachePath" }, usage = "path to Cache\nthe path to the Cache")
  protected String cachePath = System.getProperty("user.home")
      + java.io.File.separator + ".radolan";

  @Option(name = "-b", aliases = { "--borderName" }, usage = "borderName\n")
  protected String borderName = "2_bundeslaender/2_hoch.geojson";

  @Option(name = "-i", aliases = {
      "--input" }, usage = "input\nurl/file of the input")
  protected String input = null;

  @Option(name = "-rec", aliases = {
      "--refreshEvaporationCache" }, usage = "refresh the evaporation cache\ndownload evaporation data for some 500 stations (takes some 3 mins)")
  protected boolean refreshEvaporationCache = false;

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
  protected double zoomKm = 30.0;

  @Argument
  private List<String> arguments = new ArrayList<String>();

  private DisplayContext displayContext;

  RadolanApp imageViewer;

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
    RadolanApp.testMode = testMode;
    RadolanApp.toolkitInit();
  }

  /**
   * disable SSL
   */
  public static void disableSslVerification() {
    try {
      // Create a trust manager that does not validate certificate chains
      TrustManager[] trustAllCerts = new TrustManager[] {
          new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
              return null;
            }

            public void checkClientTrusted(X509Certificate[] certs,
                String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs,
                String authType) {
            }
          } };

      // Install the all-trusting trust manager
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

      // Create all-trusting host name verifier
      HostnameVerifier allHostsValid = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      };

      // Install the all-trusting host verifier
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    }
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
    imageViewer = RadolanApp.getInstance(this, displayContext);
    imageViewer.limitShowTime(this.showTimeSecs);
    imageViewer.show(); // will set view and toolTip
    imageViewer.waitOpen();
    // do we show a radolan image?
    if (displayContext.composite != null) {
      Radolan2Image.getImage(displayContext);
      Radolan2Image.activateEvents(displayContext);
    } else {

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
    if (image.isError())
      throw image.getException();
    displayContext = new DisplayContext(null, null, null, zoomKm, null);
    displayContext.mapView = new MapView(image);
    displayContext.title = input;
    showImage(displayContext);
  }

  /**
   * do the required work
   */
  public void work() {
    Translator.APPLICATION_PREFIX = "radolan";
    Translator.initialize("radolan", "de");
    try {
      // make sure we do not get any SSL hassles
      disableSslVerification();
      if (debug) {
        Debug.activateDebug();
      }
      if (noCache) {
        Composite.useCache = false;
      } else {
        CachedUrl.cacheRootPath = cachePath;
      }
      if (showVersion)
        this.showVersion();
      if (showHelp)
        this.showHelp();
      else {
        if (refreshEvaporationCache) {
          StationManager.refreshEvaporationCache();
        } else {
          if (input == null && arguments.size() == 0)
            arguments.add("latest");
          if (this.arguments.size() > 0) {
            if (debug)
              LOGGER.log(Level.INFO, arguments.toString());
            for (String argument : arguments) {
              input = KnownUrl.getUrl(product, argument);
              this.showCompositeForUrl(input);
            }
          } else if (input != null && (input.contains(".png")
              || input.contains(".jpg") || input.contains(".gif"))) {
            if (this.showImage)
              showImage(input);
            else {
              // silently fail here?
            }
          } else {
            if (input != null)
              this.showCompositeForUrl(input);
          }
          // shall we save the (latest) image
          if (displayContext != null
              && displayContext.mapView.getImage() != null && output != null
              && !output.isEmpty()) {
            String ext = FilenameUtils.getExtension(output);
            File outputFile = new File(output);
            BufferedImage bImage = RadolanApp
                .fromFXImage(displayContext.mapView.getImage(), null);
            String formatName = ext;
            if (bImage != null)
              ImageIO.write(bImage, formatName, outputFile);
          }
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
    return "Dear BITPlan Radolan Open Source software support,";
  }
}
