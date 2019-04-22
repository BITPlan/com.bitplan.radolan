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
package com.bitplan.display;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class Draw {

  /**
   * draw a circle with given text on the given pane
   * 
   * @param pane
   * @param text
   * @param radius
   * @param color
   * @param x
   * @param y
   */
  public static Circle drawCircleWithText(Pane pane, String text, double radius,
      Color color, double x, double y) {
    return drawCircleWithText(pane, text, radius, color, 1.0, color, x, y);
  }

  /**
   * draw a circle with the given parameters
   * 
   * @param pane
   * @param text
   * @param radius
   * @param circleColor
   * @param circleOpacity
   * @param textColor
   * @param x
   * @param y
   * @return
   */
  public static Circle drawCircleWithText(Pane pane, String text, double radius,
      Color circleColor, double circleOpacity, Color textColor, double x, double y) {
    Circle circle = new Circle();
    circle.setRadius(radius);
    circle.setFill(circleColor);
    circle.setOpacity(circleOpacity);
    circle.setTranslateX(x);
    circle.setTranslateY(y);

    Label label = new Label(text);
    label.setTranslateX(x + radius);
    label.setTranslateY(y + radius);
    label.setTextFill(textColor);
    pane.getChildren().addAll(circle, label);
    return circle;
  }

  /**
   * draw a cross on the given pane with the given stroke width and color
   * 
   * @param pane
   * @param strokeWidth
   * @param color
   */
  public static void drawCross(Pane pane, double strokeWidth, Color color) {
    double w = pane.getWidth();
    double h = pane.getHeight();
    Line line = new Line(0, 0, w, h);
    line.setStrokeWidth(strokeWidth);
    line.setStroke(color);
    Line line2 = new Line(w, 0, 0, h);
    line2.setStrokeWidth(strokeWidth);
    line2.setStroke(color);
    pane.getChildren().addAll(line, line2);
  }

}
