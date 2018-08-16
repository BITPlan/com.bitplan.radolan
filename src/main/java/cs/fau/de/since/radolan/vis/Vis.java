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
package cs.fau.de.since.radolan.vis;

import cs.fau.de.since.radolan.FloatFunction;
import javafx.scene.paint.Color;

/**
 * migrated to Java from
 * https://gitlab.cs.fau.de/since/radolan/blob/master/radolan2png/vis/vis.go
 * 
 * @author wf
 *
 */
@SuppressWarnings("restriction")
public class Vis {
  // Id is the identity (no compression)
  public static FloatFunction<Float> Id = (x) -> x;

  // Log is the natural logarithm (logarithmic compression)
  public static FloatFunction<Float> Log = (x) -> (float) Math.log(x);

  // A ColorFunc can be used to assign colors to data values for image creation.
  // type ColorFunc func(val float64) color.RGBA
  // we'll use a FloatFunction<Color> for this

  // Sample color and grayscale gradients for visualization with the image
  // method.
  // HeatmapReflectivityShort is a color gradient for cloud reflectivity
  // composites between 5dBZ and 75dBZ.
  public static FloatFunction<Color> HeatmapReflectivityShort = Heatmap(5.0f,
      75.0f, Id);

  // HeatmapReflectivity is a color gradient for cloud reflectivity
  // composites between 5dBZ and 75dBZ.
  public static FloatFunction<Color> HeatmapReflectivity = Heatmap(1.0f, 75.0f,
      Id);

  // HeatmapReflectivityWide is a color gradient for cloud reflectivity
  // composites between -32.5dBZ and 75dBZ.
  public static FloatFunction<Color> HeatmapReflectivityWide = Heatmap(-32.5f,
      75.0f, Id);

  // HeatmapAccumulatedHour is a color gradient for accumulated rainfall
  // composites (e.g RW) between 0.1mm/h and 100 mm/h using logarithmic
  // compression.
  public static FloatFunction<Color> HeatmapAccumulatedHour =

      Heatmap(0.1f, 100f, Log);

  // HeatmapAccumulatedDay is a color gradient for accumulated rainfall
  // composites (e.g. SF) between 0.1mm and 200mm using logarithmic
  // compression.
  public static FloatFunction<Color> HeatmapAccumulatedDay = Heatmap(0.1f, 200f,
      Log);

  public static FloatFunction<Color> HeatmapRadialVelocity = Radialmap(-31.5f,
      31.5f, Log);

  // GraymapLinear is a linear grayscale gradient between the (raw) rvp-6
  // values 0 and 409.5.
  public static FloatFunction<Color> GraymapLinear = Graymap(0f, 409.5f, Id);

  // GraymapLinearWide is a linear grayscale gradient between the (raw)
  // rvp-6 values 0 and 4095.
  public static FloatFunction<Color> GraymapLinearWide = Graymap(0f, 4095f, Id);

  /**
   * get the integer for the given color
   * 
   * @param c
   * @return the color integer
   */
  public static int getIntFromColor(Color c) {
    int R = (int) Math.round(255 * c.getRed());
    int G = (int) Math.round(255 * c.getGreen());
    int B = (int) Math.round(255 * c.getBlue());
    int A = (int) Math.round(255 * c.getOpacity());
    A = (A << 24) & 0xFF000000;
    R = (R << 16) & 0x00FF0000;
    G = (G << 8) & 0x0000FF00;
    B = B & 0x000000FF;

    return A | R | G | B;
  }

  // Image creates an image by evaluating the color function fn for each data
  // value in the given z-layer.
  /*
   * func Image(fn ColorFunc, c *radolan.Composite, layer int) *image.RGBA {
   * rec := image.Rect(0, 0, c.Dx, c.Dy)
   * img := image.NewRGBA(rec)
   * 
   * if layer < 0 || layer >= c.Dz {
   * return img
   * }
   * 
   * for y := 0; y < c.Dy; y++ {
   * for x := 0; x < c.Dx; x++ {
   * img.Set(x, y, fn(float64(c.DataZ[layer][y][x])))
   * }
   * }
   * 
   * return img
   * }
   */

  // Graymap returns a grayscale gradient function between min and max. A
  // compression function is used to
  // make logarithmic scales possible.
  public static FloatFunction<Color> Graymap(float pMin, float pMax,
      FloatFunction<Float> compression) {
    final float min = compression.apply(pMin);
    final float max = compression.apply(pMax);

    FloatFunction<Color> gradient = (val) -> {
      val = compression.apply(val);

      if (val < min) {
        return Color.BLACK; // black 
      }

      double p = (val - min) / (max - min);
      if (p > 1) {
        p = 1;
      }

      byte l = (byte) (0xFF * p);
      return Color.rgb(l, l, l, 0xFF);
    };
    return gradient;
  }

  // Radialmap returns a dichromatic gradient from min to 0 to max which can
  // be used for doppler radar radial velocity products.
  public static FloatFunction<Color> Radialmap(float min, float max,
      FloatFunction<Float> compression) {
    FloatFunction<Color> radialmap = (val) -> {
      if (val != val) {
        return Color.BLACK; // black
      }

      float base = Math.max(Math.abs(min), Math.abs(max));
      float p = compression.apply(Math.abs(val)) / compression.apply(base);

      if (p > 1) {
        p = 1;
      }
      int lev = (byte) (0xFF * p);

      int non = 0x00;
      if (Math.abs(val) <= 1) {
        lev = 0xFF;
        non = 0xCC;
      }

      if (val < 0) {
        return Color.rgb(non, lev, lev, 0xFF);
      }

      return Color.rgb(lev, non, non, 0xFF);
    };
    return radialmap;
  }

  public static final Color COLOR_INVALID=Color.rgb(0xF0,0xF0,0xF0,0.95);
  
  // Heatmap returns a colour gradient between min and max. A compression
  // function is used to
  // make logarithmic scales possible.
  public static FloatFunction<Color> Heatmap(float pMin, float pMax,
      FloatFunction<Float> compression) {
    final float min = compression.apply(pMin);
    final float max = compression.apply(pMax);

    FloatFunction<Color> heatMap = (val) -> {
      val = compression.apply(val);
      if (val < min) {
        return Color.rgb(0xE8,0xE8, 0xE8,0.75); // gray 3/4 opaque
      }
      if (val>max) {
        return Color.PURPLE; 
      }
      if (Float.isNaN(val)) {
        return COLOR_INVALID; // 95% opaque
      }

      double p = (val - min) / (max - min);
      if (p > 1) { // limit
        p = 1;
      }
      int h = (int) (Math.round(360 - (330 * p) + 240) % 360);

      double s = 1.0; // saturation
      double l = 0.5 * p + 0.25; // lightness

      // adapted from https://en.wikipedia.org/wiki/HSL_and_HSV#From_HSL
      double c = (1 - Math.abs(2 * l - 1)) * s; // calculate chroma

      int hh = h / 60;
      double x = c * (1 - Math.abs(hh % 2) - 1);

      double rr = 0;
      double gg = 0;
      double bb = 0;
      switch (hh) {
      case 0:
        rr = c;
        gg = x;
        bb = 0;
        break;
      case 1:
        rr = x;
        gg = c;
        bb = 0;
        break;
      case 2:
        rr = 0;
        gg = c;
        bb = x;
        break;
      case 3:
        rr = 0;
        gg = x;
        bb = c;
        break;
      case 4:
        rr = x;
        gg = 0;
        bb = c;
        break;
      case 5:
        rr = c;
        gg = 0;
        bb = x;
        break;
      }

      double m = l - c / 2;
      int r = (int) (0xFF * (rr + m));
      int g = (int) (0xFF * (gg + m));
      int b = (int) (0xFF * (bb + m));

      if (r>=0 && g>=0 && b>=0) {
        return Color.rgb(r, g, b, 1);
      } else {
        return Color.ORANGE;
      }
    };
    return heatMap;
  }

}
