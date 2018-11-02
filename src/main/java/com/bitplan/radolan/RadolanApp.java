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

import java.util.logging.Level;

import com.bitplan.display.MapView;
import com.bitplan.gui.App;
import com.bitplan.javafx.GenericApp;
import com.bitplan.javafx.GenericDialog;
import com.bitplan.javafx.TaskLaunch;

import cs.fau.de.since.radolan.Composite;
import javafx.beans.binding.NumberBinding;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * inspired by
 * https://docs.oracle.com/javase/8/javafx/api/javafx/scene/image/ImageView.html
 * 
 * @author wf
 *
 */
public class RadolanApp extends GenericApp {
  public static final String RESOURCE_PATH = "com/bitplan/radolan/gui";
  public static final String RADOLAN_APP_PATH = RESOURCE_PATH + "/radolan.json";
  private static RadolanApp instance;

  private String title = "RADOLAN Viewer";
  private DisplayContext displayContext;
  private MapView mapView;
  private Radolan radolan;

  /**
   * construct me
   * 
   * @param app
   * @param radolan
   * @param displayContext
   * @param resourcePath
   */
  public RadolanApp(App app, Radolan radolan, DisplayContext displayContext,
      String resourcePath) {
    super(app, radolan, resourcePath);
    this.radolan = radolan;
    this.displayContext = displayContext;
    this.title = displayContext.title;
    this.mapView = displayContext.mapView;
  }

  @Override
  public void start(Stage stage) {
    super.start(stage);
    stage.setTitle(title);

    VBox vbox = new VBox();
    setRoot(vbox);
    double sWidth = super.getScreenWidth();
    double sHeight = super.getScreenHeight();
    double heightAdjust = xyTabPane.getTabSize() + 29;
    // steps of 25 for size for an expected 900x900 image
    double height = Math.floor((sHeight - heightAdjust) / 25) * 25;
    double width = height;
    Rectangle2D sceneBounds = new Rectangle2D((sWidth - width) / 2,
        (sHeight - height) / 2, width, height);

    setScene(
        new Scene(getRoot(), sceneBounds.getWidth(), sceneBounds.getHeight()));
    stage.setScene(getScene());
    stage.setX(sceneBounds.getMinX());
    stage.setY(sceneBounds.getMinY());
    // create a Menu Bar and show it
    setMenuBar(createMenuBar(getScene(), app));
    showMenuBar(getScene(), getMenuBar(), true);

    // add the XY TabPane and set it's growing
    setupXyTabPane();
    // setup the forms
    setup(app);
    setupContent();
    stage.sizeToScene();
    stage.show();
    if (debug)
      mapView.getStackPane().setOnMouseClicked(event -> {
        showSizes(xyTabPane.getTabPane(RadolanI18n.RAIN_GROUP),
            mapView.getStackPane(), mapView.getDrawPane());
      });
    // JavaFX: stage's minHeight considering titlebar's height
    // https://stackoverflow.com/a/43346746/1497139
    // stage.setMinHeight(stage.getHeight());
    // stage.setMinWidth(stage.getWidth());
    // How to make javafx.scene.Scene resize while maintaining an aspect ratio?
    // https://stackoverflow.com/a/18638505/1497139
    // stage.minWidthProperty().bind(scene.heightProperty());
    // stage.minHeightProperty().bind(scene.widthProperty());
  }

  /**
   * setup the Content of the xyTabPanes
   */
  private void setupContent() {
    if (displayContext != null) {
      mapView = displayContext.mapView;
    }
    // load the image
    if (mapView == null)
      mapView = new MapView(
          "https://www.dwd.de/DWD/wetter/radar/rad_brd_akt.jpg");
    TabPane rainTabPane = xyTabPane.getTabPane(RadolanI18n.RAIN_GROUP);
    // set the stage width
    if (mapView != null) {
      Image image = mapView.getImage();
      double width = image.getWidth();
      double height = image.getHeight();
      if (debug)
        LOGGER.log(Level.INFO,
            String.format("image %.0f x %.0f", width, height));
      stage.setWidth(width);
      stage.setHeight(height);
      addTab(RadolanI18n.DAILY_SUM_FORM, mapView);
    }
    try {
      addTab("rw","latest",RadolanI18n.HOURLY_SUM_FORM);
    } catch (Throwable th) {
      super.handleException(th);
    }
    try {
      addTab("ry","latest",RadolanI18n.MINUTES5_FORM);
    } catch (Throwable th) {
      super.handleException(th);
    }
    MapView filmView = new MapView(
        "https://www.dwd.de/DWD/wetter/radar/radfilm_brd_akt.gif");
    Tab filmTab = xyTabPane.getTab(RadolanI18n.FILM_FORM);
    filmTab.setContent(filmView.getStackPane());
    // filmView.addSizeListener(rainTabPane);
    Tab pictureTab = xyTabPane.getTab(RadolanI18n.PICTURE_FORM);
    pictureTab.setContent(
        new MapView("https://www.dwd.de/DWD/wetter/radar/rad_brd_akt.jpg")
            .getStackPane());
  }

  /**
   * add a tab for the given product and timeDescription
   * @param product
   * @param timeDescription
   * @param tabId
   * @throws Throwable
   */
  private void addTab(String product, String timeDescription, String tabId) throws Throwable {
    String productUrl = KnownUrl.getUrl(product, timeDescription);
    Composite composite = new Composite(productUrl);
    displayContext = new DisplayContext(composite, radolan.borderName,
        Radolan2Image.borderColor, radolan.zoomKm, radolan.location);
    Radolan2Image.getImage(displayContext);
    Radolan2Image.activateEvents(displayContext);
    addTab(RadolanI18n.HOURLY_SUM_FORM,displayContext.mapView);
  }

  private void addTab(String tabId, MapView mapView) {
    Tab mapViewTab = xyTabPane.getTab(tabId);
    mapViewTab.setContent(mapView.getStackPane());
    NumberBinding heightAdjust = getScene().heightProperty()
        .subtract(xyTabPane.getTabSize()); // getMenuBar().heightProperty().add(
    NumberBinding widthAdjust = getScene().widthProperty()
        .subtract(xyTabPane.getTabSize());
    // mapView.addSizeListener(widthAdjust, heightAdjust);
    // NumberBinding
    // heightAdjust=rainTabPane.heightProperty().add(getMenuBar().heightProperty());
    mapView.getImageView().fitHeightProperty().bind(heightAdjust);
    mapView.getImageView().fitWidthProperty().bind(widthAdjust);
  }

  /**
   * show the sizes of stage, scene, and the given other nodes
   * 
   * @param region
   *          - the regions to show
   */
  public void showSizes(Region... regions) {
    System.out.println("Sizes: ");
    ObservableList<Screen> screens = Screen.getScreensForRectangle(stage.getX(),
        stage.getY(), stage.getWidth(), stage.getHeight());
    for (Screen screen : screens) {
      Rectangle2D s = screen.getVisualBounds();
      showSize(screen, s.getWidth(), s.getHeight());
    }
    showSize(stage, stage.getWidth(), stage.getHeight());
    showSize(getScene(), getScene().getWidth(), getScene().getHeight());
    ImageView imageView = mapView.getImageView();
    showSize(imageView, imageView.getFitWidth(), imageView.getFitHeight());

    Image image = mapView.getImage();
    showSize(image, image.getWidth(), image.getHeight());
    for (Region region : regions) {
      showSize(region, region.getWidth(), region.getHeight());
    }
  }

  /**
   * show the size of the given object
   * 
   * @param o
   * @param width
   * @param height
   */
  public void showSize(Object o, double width, double height) {
    System.out.println(String.format("%s %.0f x %.0f",
        o.getClass().getSimpleName(), width, height));
  }

  public void close() {
    super.close();
    instance=null;
    // if (!testMode)
    // System.exit(0);
  }

  @Override
  public void stop() {
    // https://docs.oracle.com/javase/8/javafx/api/javafx/application/Application.html
    System.err.println("ImageViewer stopped");
  }

  @Override
  public void handle(ActionEvent event) {
    try {
      Object source = event.getSource();
      if (source instanceof MenuItem) {
        MenuItem menuItem = (MenuItem) source;
        switch (menuItem.getId()) {
        case RadolanI18n.FILE_MENU__QUIT_MENU_ITEM:
          close();
          break;
        case RadolanI18n.HELP_MENU__ABOUT_MENU_ITEM:
          TaskLaunch.start(() -> showLink(app.getHome()));
          showAbout();
          break;
        case RadolanI18n.HELP_MENU__HELP_MENU_ITEM:
          TaskLaunch.start(() -> showLink(app.getHelp()));
          break;
        case RadolanI18n.HELP_MENU__FEEDBACK_MENU_ITEM:
          GenericDialog.sendReport(softwareVersion,
              softwareVersion.getName() + " feedback", "...");
          break;
        case RadolanI18n.HELP_MENU__BUG_REPORT_MENU_ITEM:
          TaskLaunch.start(() -> showLink(app.getFeedback()));
          break;
        case RadolanI18n.RAIN_MENU__DAILY_SUM_MENU_ITEM:
          this.selectTab(RadolanI18n.DAILY_SUM_FORM);
          break;
        case RadolanI18n.RAIN_MENU__HOURLY_SUM_MENU_ITEM:
          this.selectTab(RadolanI18n.HOURLY_SUM_FORM);
          break;
        case RadolanI18n.RAIN_MENU__MINUTES5_MENU_ITEM:
          this.selectTab(RadolanI18n.MINUTES5_FORM);
          break;
        case RadolanI18n.RAIN_MENU__FILM_MENU_ITEM:
          this.selectTab(RadolanI18n.FILM_FORM);
          break;
        case RadolanI18n.RAIN_MENU__PICTURE_MENU_ITEM:
          this.selectTab(RadolanI18n.PICTURE_FORM);
          break;
        default:
          LOGGER.log(Level.WARNING, "unhandled menu item " + menuItem.getId()
              + ":" + menuItem.getText());
        }
      } else {
        LOGGER.log(Level.INFO,
            "event from " + source.getClass().getName() + " received");
      }
    } catch (Exception e) {
      handleException(e);
    }
  }

  /**
   * get the instance for the RadolanApp
   * 
   * @param radolan
   * @return the RadolanApp
   * @throws Exception
   */
  public static RadolanApp getInstance(Radolan radolan,
      DisplayContext displayContext) throws Exception {
    if (instance == null) {
      App app = App.getInstance(RADOLAN_APP_PATH);
      GenericApp.debug = true;
      instance = new RadolanApp(app,radolan, displayContext, RESOURCE_PATH);
    }
    return instance;
  }

}