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

package org.cirdles.tripoli.gui.constants;

import javafx.scene.paint.Color;
import org.jetbrains.annotations.NonNls;

/**
 * @author James F. Bowring
 */
public enum ConstantsTripoliApp {
    ;

    public static final Color TRIPOLI_STARTING_YELLOW = new Color(243.0 / 256.0, 227.0 / 256.0, 118.0 / 256.0, 1.0);
    public static final Color TRIPOLI_SESSION_LINEN = Color.LINEN;
    public static final Color TRIPOLI_ANALYSIS_YELLOW = Color.web("#fbf6d5");
    public static final Color TRIPOLI_ANALYSIS_GREEN = Color.web("#AFFF80");
    public static final Color TRIPOLI_ANALYSIS_RED = Color.web("#FF7377");

    // https://www.learnui.design/tools/data-color-picker.html
    public static final String[] TRIPOLI_PALLETTE_ONE = {"#003f5c", "#2f4b7c", "#665191", "#a05195", "#f95d6a", "#ff7c43", "#ffa600"};
    // https://www.vis4.net/palettes/#/9|d|00429d,96ffea,ffffe0|ffffe0,ff005e,93003a|1|1
    public static final String[] TRIPOLI_PALLETTE_TWO = {"#00429d", "#4771b2", "#73a2c6", "#a5d5d8", "#ffffe0", "#ffbcaf", "#f4777f", "#cf3759", "#93003a"};
    // https://colorbrewer2.org/#type=diverging&scheme=BrBG&n=8

    public static final String[] TRIPOLI_PALLETTE_THREE = {"#8c510a", "#bf812d", "#dfc27d", "#f6e8c3", "#c7eae5", "#80cdc1", "#35978f", "#01665e"};
    //https://docs.oracle.com/javase/8/javafx/api/javafx/scene/paint/Color.html
    public static final String[] TRIPOLI_PALLETTE_FOUR = {"RED", "BLUE", "GREEN", "BLACK", "ORANGE", "INDIGO", "#35978f", "#01665e"};

    public static final Color TRIPOLI_MOVING_SHADE = new Color(255.0 / 256.0, 182.0 / 256.0, 193.0 / 256.0, 0.5);

    public static @NonNls String convertColorToHex(Color color) {
        String red = Integer.toHexString((int) (color.getRed() * 255));
        String green = Integer.toHexString((int) (color.getGreen() * 255));
        String blue = Integer.toHexString((int) (color.getBlue() * 255));

        return "#" + red + green + blue;
    }

    public static enum PlotLayoutStyle {
        TILE(),
        STACK(),
        CASCADE();
    }


}