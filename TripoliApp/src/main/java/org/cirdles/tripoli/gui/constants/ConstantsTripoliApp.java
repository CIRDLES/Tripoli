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


    public static @NonNls String convertColorToHex(Color color) {
        String red = Integer.toHexString((int) (color.getRed() * 255));
        String green = Integer.toHexString((int) (color.getGreen() * 255));
        String blue = Integer.toHexString((int) (color.getBlue() * 255));

        return "#" + red + green + blue;
    }
}