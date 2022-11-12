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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author James F. Bowring
 */
public class SessionManagerController implements Initializable {

        @FXML
        private TextField analystNameText;

        @FXML
        private TextField sessionNameText;

        @FXML
        private TextArea sessionNotesText;

        @FXML
        private Label summaryStatsLabel;

        @FXML
        private TextField tripoliFileNameText;

        /**
         * @param location  The location used to resolve relative paths for the root object, or
         *                  {@code null} if the location is not known.
         * @param resources The resources used to localize the root object, or {@code null} if
         *                  the root object was not localized.
         */
        @Override
        public void initialize(URL location, ResourceBundle resources) {

        }
}