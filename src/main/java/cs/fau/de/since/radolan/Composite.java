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

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.ZonedDateTime;

import org.apache.commons.io.IOUtils;

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
//PG, PC, PX*, ...        | cloud reflectivity       | dBZ
//RX, WX, EX, FZ, FX, ... | cloud reflectivity     | dBZ
//RW, SF,  ...            | aggregated precipitation | mm/interval
//PR*, ...                | doppler radial velocity  | m/s
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
public class Composite {
  String Product; // composite product label
  
  ZonedDateTime CaptureTime;
  ZonedDateTime ForecastTime;
  Duration Interval;
  
  Unit DataUnit;
  
  public float[][] PlainData; // data for parsed plain data element [y][x]
  
  int Px;  // plain data width
  int Py;  // plain data height
  
  int Dx; // data width
  int Dy;// data height

  double Rx; // horizontal resolution in km/px
  double Ry; // vertical resolution in km/px

  boolean HasProjection; // coordinate translation available
  
  int dataLength; // length of binary section in bytes

  int precision;       // multiplicator 10^precision for each raw value
  float[] level; // maps data value to corresponding index value in runlength based formats

  double offx; // horizontal projection offset
  double offy; // vertical projection offset
  public byte bytes[];
  public String header;

  // for lambda error handling
  public Throwable error;
  protected String url;

  /**
   * default constructor
   */
  public Composite() {}

  /**
   * construct me from the given parameters
   * 
   * @param product
   * @param dx
   * @param dy
   */
  public Composite(String product, int dx, int dy) {
    this.Product = product;
    this.Dx = dx;
    this.Dy = dy;
  }
  
  /**
   * construct me from an url;
   * @param url
   * @throws Throwable 
   */
  public Composite(String url) throws Throwable {
    this.url=url;
    InputStream inputStream = new URL(url).openStream();
    read(inputStream);
    init();
  }
  
  public void init() throws Throwable {
    parseData();
    arrangeData();
    calibrateProjection();
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
   * read all bytes from the given InpuStream
   * @param inputStream
   * @throws Exception
   */
  public void read(InputStream inputStream) throws Exception {
    bytes = IOUtils.toByteArray(inputStream);
    StringBuffer headerBuffer=new StringBuffer();
    int pos=0;
    // read until 0x03 is found or we are way into the binary 2 x typical width 900 should suffice to terminate ...
    while (bytes[pos]!=0x03 && pos<=1800) {
      headerBuffer.append((char)bytes[pos]);
      pos++;
    }
    headerBuffer.append((char)bytes[pos]);
    if (pos>1799 ||pos<21) {
      throw new Exception("header length "+pos+" out of valid range");
    }
    header=headerBuffer.toString();
    this.parseHeader();
  }
  
  public void parseHeader() throws Exception {
    Header.parseHeader(this);
  }
  
  public void parseData() throws Throwable {
    Data.getInstance().parseData(this);
  }
  
  public void arrangeData() throws Throwable {
    Data.getInstance().parseData(this);
  }

  public void calibrateProjection() {
    Translate.calibrateProjection(this);
  }
  
  public Encoding identifyEncoding() {
    return Data.getInstance().identifyEncoding(this);
  }

  /**
   * translate the given coordinates to a double precision point
   * 
   * @param lat
   * @param lon
   * @return the translation
   */
  public DPoint translate(double lat, double lon) {
    return Translate.translate(this, lat, lon);
  }

}
