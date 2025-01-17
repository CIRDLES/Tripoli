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

import javafx.scene.paint.Color;

import java.io.Serializable;

/**
 * @author James F. Bowring
 */
public class TripoliColor implements Serializable {
    private final int red;
    private final int green;
    private final int blue;

    private TripoliColor(int red, int green, int blue) {
        this.red = red % 256;
        this.green = green % 256;
        this.blue = blue % 256;
    }

    public static TripoliColor create(int red, int green, int blue) {
        return new TripoliColor(red, green, blue);
    }

    public static TripoliColor create(Color color) {
        return new TripoliColor((int) color.getRed() * 255, (int) color.getGreen() * 255, (int) color.getBlue() * 255);
    }

    public static TripoliColor create(String colorName) {
        Color color = Color.valueOf(colorName);
        return create(color);
    }

    public Color color() {
        return Color.rgb(red, green, blue);
    }

}