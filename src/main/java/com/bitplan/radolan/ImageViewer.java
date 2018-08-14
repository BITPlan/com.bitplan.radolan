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
/**
 * Copyright (c) 2018 BITPlan GmbH
 *
 * http://www.bitplan.com
 *
 * This file is part of the Opensource project at:
 * https://github.com/BITPlan/com.bitplan.sprinkler
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
 */


import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * inspired by
 * https://docs.oracle.com/javase/8/javafx/api/javafx/scene/image/ImageView.html
 * 
 * @author wf
 *
 */
@SuppressWarnings("restriction")
public class ImageViewer extends Application {

  public static Image image;
  public static String title="Image Viewer";
  static double rotate=0.0;

  @Override
  public void start(Stage stage) {
    // load the image
    if (image == null)
      image = new Image("https://www.dwd.de/DWD/wetter/radar/rad_brd_akt.jpg");

    // simple displays ImageView the image as is
    ImageView imageView = new ImageView();
    imageView.setImage(image);
    imageView.setRotate(rotate);

    Group root = new Group();
    Scene scene = new Scene(root);
    scene.setFill(Color.BLACK);
    HBox box = new HBox();
    box.getChildren().add(imageView);
    root.getChildren().add(box);
    imageView.fitWidthProperty().bind(stage.widthProperty());
    imageView.fitHeightProperty().bind(stage.heightProperty().subtract(20));
    
    stage.setTitle(title);
    stage.setWidth(900);
    stage.setHeight(900);
    stage.setScene(scene);
    stage.sizeToScene();
    stage.show();
  }

  /**
   * launch me with the given arguments
   * 
   * @param args
   */
  public static void main(String[] args) {
    Application.launch(args);
  }
}