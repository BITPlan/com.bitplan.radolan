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
//Package radolan parses the DWD RADOLAN / RADVOR radar composite format. This data

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.bitplan.geo.DPoint;
import com.bitplan.geo.IPoint;
import com.bitplan.geo.Projection;
import com.bitplan.geo.ProjectionImpl;
import com.bitplan.geo.UnLocodeManager;
import com.bitplan.radolan.DisplayContext;
import com.bitplan.radolan.ImageViewer;
import com.bitplan.radolan.RadarImage;
import com.bitplan.radolan.Radolan2Image;
import com.bitplan.radolan.Statistics;
import com.bitplan.radolan.Zoom;

import cs.fau.de.since.radolan.Catalog.Unit;
import cs.fau.de.since.radolan.Data.Encoding;

//is available at the Global Basic Dataset (http://www.dwd.de/DE/leistungen/gds/gds.html).
//The obtained results can be processed and visualized with additional functions.
//
//Currently the national grid [1][4] and the extended european grid [5] are supported.
//Tested input products are PG, FZ, SF, RW, RX and EX. Those can be considered working with
//sufficient accuracy.
//
//In cases, where the publicly available format specification is unprecise or contradictory,
//reverse engineering was used to obtain reasonable approaches.
//Used references:
//
//[1] https://www.dwd.de/DE/leistungen/radolan/radolan_info/radolan_radvor_op_komposit_format_pdf.pdf
//[2] https://www.dwd.de/DE/leistungen/gds/weiterfuehrende_informationen.zip
//[3]  - legend_radar_products_fz_forecast.pdf
//[4]  - legend_radar_products_pg_coordinates.pdf
//[5]  - legend_radar_products_radolan_rw_sf.pdf
//[6] https://www.dwd.de/DE/leistungen/radarniederschlag/rn_info/download_niederschlagsbestimmung.pdf

//Radolan radar data is provided as single local sweeps or so called composite
//formats. Each composite is a combined image consisting of mulitiple radar
//sweeps spread over the composite area.
//The 2D composite c has a an internal resolution of c.Dx (horizontal) * c.Dy
//(vertical) records covering a real surface of c.Dx * c.Rx * c.Dy * c.Dy
//square kilometers.
//The pixel value at the position (x, y) is represented by
//c.Data[ y ][ x ] and is stored as raw float value (NaN if the no-data flag
//is set). Some 3D radar products feature multiple layers in which the voxel
//at position (x, y, z) is accessible by c.DataZ[ z ][ y ][ x ].
//
//The data value is used differently depending on the product type:
//(also consult the DataUnit field of the Composite)
//
//Product label            | values represent         | unit
//-------------------------+--------------------------+------------------------
//PG, PC, PX*, ...         | cloud reflectivity       | dBZ
//RX, WX, EX, FZ, FX, ...  | cloud reflectivity       | dBZ
//RW, SF,  ...             | aggregated precipitation | mm/interval
//PR*, ...                 | doppler radial velocity  | m/s
//
//The cloud reflectivity (in dBZ) can be converted to rainfall rate (in mm/h)
//via PrecipitationRate().
//
//The cloud reflectivity factor Z is stored in its logarithmic representation dBZ:
//dBZ = 10 * log(Z)
//Real world geographical coordinates (latitude, longitude) can be projected into the
//coordinate system of the composite by using the translation method:
//// if c.HasProjection
//x, y := c.Translate(52.51861, 13.40833) // Berlin (lat, lon)
//
//dbz := c.At(int(x), int(y))         // Raw value is Cloud reflectivity (dBZ)
//rat := radolan.PrecipitationRate(radolan.Doelling98, dbz) // Rainfall rate (mm/h) using Doelling98 as Z-R relationship
//
//fmt.Println("Rainfall in Berlin [mm/h]:", rat)
//

/**
 * migrated to Java from
 * https://gitlab.cs.fau.de/since/radolan/blob/master/radolan.go
 * 
 * @author wf
 *
 */
public class Composite extends ProjectionImpl implements RadarImage,Projection  {

  // by default files from known URL are cached locally
  // see https://github.com/BITPlan/com.bitplan.radolan/issues/3
  public static boolean useCache = true;

  // if not set the default $HOME/.radolan will be used
  public static String cacheRootPath = null;

  public static String knownUrls[] = {
      "https://opendata.dwd.de/weather/radar/radolan/",
      "ftp://ftp-cdc.dwd.de/pub/CDC/grids_germany/daily/radolan" };
  private String Product; // composite product label

  ZonedDateTime CaptureTime;
  private ZonedDateTime ForecastTime;
  Duration Interval;

  private Unit DataUnit;

  public float[][] PlainData; // data for parsed plain data element [y][x]

  private int Px; // plain data width
  private int Py; // plain data height

  private int dataLength; // length of binary section in bytes

  private int precision; // multiplicator 10^precision for each raw value
  private double precisionFactor;
  float[] level; // maps data value to corresponding index value in runlength
                 // based formats


  public byte bytes[];
  public String header;

  private Statistics statistics;

  // for lambda error handling
  public Throwable error;
  protected String url;

  // CallBacks
  private static Consumer<Composite> postInit;

  public int getDx() {
    return Dx;
  }

  public void setDx(int dx) {
    Dx = dx;
  }

  public int getDy() {
    return Dy;
  }

  public void setDy(int dy) {
    Dy = dy;
  }

  public int getPx() {
    return Px;
  }

  public void setPx(int px) {
    Px = px;
  }

  public int getPy() {
    return Py;
  }

  public void setPy(int py) {
    Py = py;
  }

  public int getDataLength() {
    return dataLength;
  }

  public void setDataLength(int dataLength) {
    this.dataLength = dataLength;
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision(int precision) {
    this.precision = precision;
    setPrecisionFactor(Math.pow(10, precision));
  }

  public double getPrecisionFactor() {
    return precisionFactor;
  }

  public void setPrecisionFactor(double precisionFactor) {
    this.precisionFactor = precisionFactor;
  }

  public Unit getDataUnit() {
    return DataUnit;
  }

  public void setDataUnit(Unit dataUnit) {
    DataUnit = dataUnit;
  }

  public String getProduct() {
    return Product;
  }

  public void setProduct(String product) {
    Product = product;
  }

  public Statistics getStatistics() {
    return statistics;
  }

  public void setStatistics(Statistics statistics) {
    this.statistics = statistics;
  }

  public ZonedDateTime getForecastTime() {
    return ForecastTime;
  }

  public void setForecastTime(ZonedDateTime forecastTime) {
    ForecastTime = forecastTime;
  }

  public Duration getInterval() {
    return Interval;
  }

  public void setInterval(Duration interval) {
    Interval = interval;
  }

  public static Consumer<Composite> getPostInit() {
    return postInit;
  }

  public static void setPostInit(Consumer<Composite> pPostInit) {
    postInit = pPostInit;
  }

  /**
   * default constructor
   */
  public Composite() {
    setStatistics(new Statistics());
  }

  /**
   * construct me from the given parameters
   * 
   * @param product
   * @param dx
   * @param dy
   */
  public Composite(String product, int dx, int dy) {
    this();
    this.setProduct(product);
    this.setDx(dx);
    this.setDy(dy);
  }

  /**
   * construct me from an url - read the data immediately from url or if the
   * cache is active and the content is available from the cache
   * 
   * @param url
   * @throws Throwable
   */
  public Composite(String url) throws Throwable {
    this();
    if (debug)
      LOGGER.log(Level.INFO, "getting composite for url " + url);
    this.url = checkCache(url);

    InputStream inputStream = new URL(url).openStream();
    read(inputStream);
    init();
  }

  /**
   * if the cache is not active or the url is not starting with a known url then
   * return the url as is when the local cache is active then check if the url
   * content is not available locally and if needed then read the url content to
   * the cache after having made sure the file is available locally return the
   * url for the file
   * 
   * @param url
   * @return the (potentially replaced)
   * @throws Exception
   */
  public static String checkCache(String url) throws Exception {
    if (!useCache)
      return url;
    if (url.contains("-latest-"))
      return url;
    for (String knownUrl : knownUrls) {
      if (url.startsWith(knownUrl)) {
        return useCache(url, knownUrl);
      }
    }
    return url;
  }

  /**
   * use the cache for the given URL
   * 
   * @param url
   * @param knownUrl
   * @return - the URL of the cached file
   * @throws Exception
   *           - if the URL is malformed
   */
  public static String useCache(String url, String knownUrl) throws Exception {
    File cacheFile = cacheForUrl(url, knownUrl);
    if (!cacheFile.exists()) {
      URL uri = new URL(url);
      if (debug)
        LOGGER.log(Level.INFO, "caching to " + cacheFile.getPath());
      // cache the URL content
      FileUtils.copyURLToFile(uri, cacheFile);
    } else {
      if (debug)
        LOGGER.log(Level.INFO,
            "getting cached file from " + cacheFile.getPath());
    }
    return cacheFile.toURI().toURL().toExternalForm();
  }

  /**
   * get the cache File for the given url in reference to the given knownUrl
   * 
   * @param url
   * @param knownUrl
   * @return - the cacheFile
   */
  public static File cacheForUrl(String url, String knownUrl) {
    String filePath = url.substring(knownUrl.length(), url.length());
    if (cacheRootPath == null)
      cacheRootPath = System.getProperty("user.home") + java.io.File.separator
          + ".radolan";
    File cacheRoot = new File(cacheRootPath);
    if (!cacheRoot.exists()) {
      if (debug)
        LOGGER.log(Level.INFO,
            "Creating radolan data cache directory " + cacheRoot.getPath());
      cacheRoot.mkdirs();
    }
    File cacheFile = new File(cacheRoot, filePath);
    return cacheFile;
  }

  /**
   * initialize me
   * 
   * @throws Throwable
   */
  public void init() throws Throwable {
    parseData();
    arrangeData();
    if (debug)
      LOGGER.log(Level.INFO,
          String.format("parsed %7d grid values for %3d x %3d grid",
              statistics.getTotal(),
              this.getGridWidth(), this.getGridHeight()));
    calibrateProjection();
    // is there a callback installed?
    if (postInit != null) {
      postInit.accept(this);
    }
  }

  // NewDummy creates a blank dummy composite with the given product label and
  // dimensions. It can
  // be used for generic coordinate translation.
  public static Composite NewDummy(String product, int dx, int dy) {
    Composite comp = new Composite(product, dx, dy);
    comp.calibrateProjection();
    return comp;
  }

  /**
   * read all bytes from the given InpuStream - autodetect zipped input
   * 
   * @param inputStream
   * @throws Exception
   */
  public void read(InputStream inputStream) throws Exception {
    bytes = IOUtils.toByteArray(inputStream);
    // https://tools.ietf.org/html/rfc1952
    // check for gzip header
    if (bytes.length < 2) {
      throw new Exception("input is empty");
    }
    int magic = ((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff);
    if (magic == 0x1f8b) {
      int zippedLength = bytes.length;
      InputStream gzStream = new GZIPInputStream(
          new ByteArrayInputStream(bytes));
      byte[] unzipped = IOUtils.toByteArray(gzStream);
      bytes = unzipped;
      String msg = String.format("unzipped %d to %d bytes", zippedLength,
          bytes.length);
      if (debug)
        LOGGER.log(Level.INFO, msg);
    }
    StringBuffer headerBuffer = new StringBuffer();
    int pos = 0;
    // read until 0x03 is found or we are way into the binary 2 x typical width
    // 900 should suffice to terminate ...
    while (bytes[pos] != 0x03 && pos <= 1800) {
      headerBuffer.append((char) bytes[pos]);
      pos++;
    }
    headerBuffer.append((char) bytes[pos]);
    if (pos > 1799 || pos < 21) {
      throw new Exception("header length " + pos + " out of valid range");
    }
    header = headerBuffer.toString();
    this.parseHeader();
  }

  public void parseHeader() throws Exception {
    Header.parseHeader(this);
  }

  /**
   * parse the data
   * 
   * @throws Throwable
   */
  public void parseData() throws Throwable {
    Data.getInstance().parseData(this);
  }

  /**
   * arrange the data
   * 
   * @throws Throwable
   */
  public void arrangeData() throws Throwable {
    Data.getInstance().arrangeData(this);
  }

  public void calibrateProjection() {
    Translate.calibrateProjection(this);
  }

  public Encoding identifyEncoding() {
    return Data.getInstance().identifyEncoding(this);
  }

  public double rvp6Raw(int value) {
    double conv=Conversion.rvp6Raw(this, value);
    return conv;
  }

  
  /**
   * get the byte at the given x,y position in the binary data
   * 
   * @param x
   * @param y
   * @return - the byte
   */
  public byte getByte(int x, int y) {
    int ofs = header.length();
    int pos = y * getDx() * 2 + x + ofs;
    return bytes[pos];
  }

  /**
   * get the value at the given x,y coordinate
   * 
   * @param x
   * @param y
   * @return - the value
   */
  public float getValue(int x, int y) {
    float value = Float.NaN;
    if (y >= 0 && y < PlainData.length)
      if (x >= 0 && x < PlainData[y].length)
        value = PlainData[y][x];
    return value;
  }

  /**
   * set a data value
   * 
   * @param x
   * @param y
   * @param value
   */
  public void setValue(int x, int y, float value) {
    if (y >= 0 && y < PlainData.length)
      if (x >= 0 && x < PlainData[y].length) {
        /*float oldvalue = PlainData[y][x];
        if (oldvalue != 0f && value == oldvalue) {
          throw new RuntimeException(
              "" + x + "," + y + " already set to " + oldvalue);
        }*/
        PlainData[y][x] = value;
        getStatistics().add(value);
      }
  }

  /**
   * activate the static debug flags of components used
   */
  public static void activateDebug() {
    Composite.debug = true;
    ImageViewer.debug = true;
    Radolan2Image.debug = true;
    Zoom.debug = true;
    DisplayContext.debug = true;
    UnLocodeManager.debug = true;
  }

}
