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

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

@SuppressWarnings("restriction")
/**
 * https://stackoverflow.com/a/12717937/1497139
 * 
 * @author wf
 *
 */
public class TranslucentPane extends Application {
  @Override
  public void start(final Stage stage) throws Exception {
    final ImageView imageView = new ImageView(new Image(
        "https://upload.wikimedia.org/wikipedia/commons/b/b7/Idylls_of_the_King_3.jpg"));
    imageView.setFitHeight(300);
    imageView.setFitWidth(228);

    final Label label = new Label("The Once\nand\nFuture King");
    label.setStyle(
        "-fx-text-fill: goldenrod; -fx-font: italic 20 \"serif\"; -fx-padding: 0 0 20 0; -fx-text-alignment: center");

    StackPane glass = new StackPane();
    StackPane.setAlignment(label, Pos.BOTTOM_CENTER);
    glass.getChildren().addAll(label);
    glass.setStyle(
        "-fx-background-color: rgba(0, 100, 100, 0.5); -fx-background-radius: 10;");
    glass.setMaxWidth(imageView.getFitWidth() - 40);
    glass.setMaxHeight(imageView.getFitHeight() - 40);
    /*Line line = new Line(0,0,100,10);
    line.setStrokeWidth(2);
    line.setStroke(Color.BLACK);
    glass.getChildren().add(line);*/

    final StackPane layout = new StackPane();
    layout.getChildren().addAll(imageView, glass);
    layout.setStyle("-fx-background-color: silver; -fx-padding: 10;");
    stage.setScene(new Scene(layout));
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
