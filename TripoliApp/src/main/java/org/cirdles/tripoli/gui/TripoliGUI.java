/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.tripoli.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.cirdles.tripoli.valueModels.ValueModel;

import java.awt.*;
import java.io.*;

/**
 * @author James F. Bowring
 */
public class TripoliGUI extends Application {

    public static final String Tripoli_LOGO_SANS_TEXT_URL = "images/TripoliNoah2.png";
    public static Window primaryStageWindow;
    protected static Stage primaryStage;
    protected static TripoliAboutWindow TripoliAboutWindow;

    public static void updateStageTitle(String fileName) {
        String fileSpec = "[Project File: NONE]";
        fileSpec = fileName.length() > 0 ? fileSpec.replace("NONE", fileName) : fileSpec;
        primaryStage.setTitle("Tripoli  " + fileSpec);
        TripoliGUIController.projectFileName = fileName;
    }

    public static void main(String[] args) {

        // arg[0] : -v[erbose]
        boolean verbose = false;
        if (args.length > 0) {
            verbose = args[0].startsWith("-v");
        }

//  http://patorjk.com/software/taag/#p=display&c=c%2B%2B&f=Varsity&t=Tripoli
//   _________          _                  __    _
//  |  _   _  |        (_)                [  |  (_)
//  |_/ | | \_|_ .--.  __  _ .--.    .--.  | |  __
//      | |   [ `/'`\][  |[ '/'`\ \/ .'`\ \| | [  |
//     _| |_   | |     | | | \__/ || \__. || |  | |
//    |_____| [___]   [___]| ;.__/  '.__.'[___][___]
//                        [__|

        StringBuilder logo = new StringBuilder();
        logo.append("        _________          _                  __    _   \n");
        logo.append("       |  _   _  |        (_)                [  |  (_)  \n");
        logo.append("       |_/ | | \\_|_ .--.  __  _ .--.    .--.  | |  __   \n");
        logo.append("           | |   [ `/'`\\][  |[ '/'`\\ \\/ .'`\\ \\| | [  |  \n");
        logo.append("          _| |_   | |     | | | \\__/ || \\__. || |  | |  \n");
        logo.append("         |_____| [___]   [___]| ;.__/  '.__.'[___][___] \n");
        logo.append("                             [__|                       \n");
        System.out.println(logo);


        // detect if running from jar file
        if (!verbose && (ClassLoader.getSystemResource("org/cirdles/tripoli/gui/TripoliGUI.class").toExternalForm().startsWith("jar"))) {
            System.out.println(
                    "Running Tripoli from Jar file ... suppressing terminal output.\n"
                            + "\t use '-verbose' argument after jar file name to enable terminal output.");
            System.setOut(new PrintStream(new OutputStream() {
                public void write(int b) {
                    // NO-OP
                }
            }));
            System.setErr(new PrintStream(new OutputStream() {
                public void write(int b) {
                    // NO-OP
                }
            }));
        }

        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException, AWTException {
        TripoliGUI.primaryStage = primaryStage;
        Parent root = new AnchorPane();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        updateStageTitle("");

        // this produces non-null window after .show()
        primaryStageWindow = primaryStage.getScene().getWindow();

        primaryStage.setOnCloseRequest((WindowEvent e) -> {
            Platform.exit();
            System.exit(0);
        });

        // postpone loading to allow for stage creation and use in controller
        FXMLLoader loader = new FXMLLoader(TripoliGUI.class.getResource("TripoliGUI.fxml"));
        scene.setRoot(loader.load());
        scene.setUserData(loader.getController());

        primaryStage.setMinHeight(scene.getHeight() + 15);
        primaryStage.setMinWidth(scene.getWidth());
        primaryStage.getIcons().add(new Image(TripoliGUI.class.getResourceAsStream(Tripoli_LOGO_SANS_TEXT_URL)));

        TripoliAboutWindow = new TripoliAboutWindow(primaryStage);

        primaryStage.show();

        // create stops for color gradient
        Stop[] stop = {new Stop(0,  new Color(243.0/256.0, 227.0/256.0, 118.0/256.0, 1.0)),
//                new Stop(0.5, new Color(24.0/256.0, 162.0/256.0, 74.0/256.0, 1.0)),
                new Stop(1, new Color(236.0/256.0, 123.0/256.0, 56.0/256.0, 1.0))};
        // create a Linear gradient object
        LinearGradient linear_gradient = new LinearGradient(0, 0,
                1, 0, true, CycleMethod.NO_CYCLE, stop);
        BackgroundFill bgFill = new BackgroundFill(linear_gradient, CornerRadii.EMPTY, Insets.EMPTY);
        ((AnchorPane)((VBox)scene.getRoot()).getChildren().get(1)).setBackground(new Background(bgFill));
    }
}