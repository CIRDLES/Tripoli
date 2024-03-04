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
package org.cirdles.tripoli.gui.utilities;

import javafx.stage.Window;
import org.cirdles.tripoli.gui.TripoliGUI;
import org.cirdles.tripoli.gui.dialogs.TripoliMessageDialog;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


/**
 * @author James F. Bowring
 */
public enum BrowserControl {
    ;

    public static void showURI(String location, Window ownerWindow) {
        try {
            URI oURL;
            if (location.contains("http")) {
                oURL = new URI(location);
            } else {
                // assume file
                File file = new File(location);
                oURL = file.toURI();
            }

            if (isMacOperatingSystem()) {
                Runtime.getRuntime().exec("open " + oURL);
            } else if (!isLinuxOrUnixOperatingSystem()) {
                java.awt.Desktop.getDesktop().browse(oURL);
            } else {
                Runtime.getRuntime().exec("xdg-open " + oURL);
            }
        } catch (URISyntaxException | IOException e) {
            TripoliMessageDialog.showWarningDialog("An error occurred:\n" + e.getMessage(), ownerWindow);
        }
    }

    public static void showURI(String location) {
        showURI(location, TripoliGUI.primaryStageWindow);
    }

    public static String urlEncode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private static String getOperatingSystem() {
        return System.getProperty("os.name");
    }

    private static boolean isLinuxOrUnixOperatingSystem() {
        return getOperatingSystem().toLowerCase().matches(".*(nix|nux).*");
    }

    private static boolean isMacOperatingSystem() {
        return getOperatingSystem().toLowerCase().startsWith("mac");
    }


    public static void main(String[] args) {
        System.out.println("OS: " + getOperatingSystem());
        System.out.println("Is Linux or Unix: " + isLinuxOrUnixOperatingSystem());
    }
}