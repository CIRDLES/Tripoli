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

/**
 * Viridis color palette utility.
 * Provides colorblind-friendly colors ranging from dark purple/blue through green to yellow.
 * Based on the matplotlib viridis colormap.
 */
public class ViridisColorPalette {
    
    // Key colors from the viridis palette (RGB values 0-255)
    // These represent the main stops in the viridis colormap
    // Based on matplotlib's viridis colormap: dark purple -> blue -> teal -> green -> yellow
    private static final double[][] VIRIDIS_KEY_COLORS = {
        {68, 1, 84},       // #440154 - Dark purple (start)
        {72, 39, 119},     // #482777 - Purple-blue
        {63, 74, 138},     // #3f4a8a - Blue
        {49, 104, 142},    // #31688e - Blue-teal
        {38, 131, 143},    // #26838f - Teal
        {31, 157, 138},    // #1f9d8a - Teal-green
        {108, 206, 90},    // #6cce5a - Green
        {182, 222, 43},    // #b6de2b - Yellow-green
        {254, 232, 37}     // #fee825 - Yellow (end)
    };
    
    /**
     * Gets a viridis color for a normalized value between 0.0 and 1.0.
     * 
     * @param normalizedValue Value between 0.0 (dark purple) and 1.0 (yellow)
     * @return Color from the viridis palette
     */
    public static Color getViridisColor(double normalizedValue) {
        // Clamp value to [0, 1]
        double clampedValue = Math.max(0.0, Math.min(1.0, normalizedValue));
        
        // Calculate which segment we're in
        double segmentSize = 1.0 / (VIRIDIS_KEY_COLORS.length - 1);
        int segmentIndex = (int) Math.min(clampedValue / segmentSize, VIRIDIS_KEY_COLORS.length - 2);
        segmentIndex = Math.min(segmentIndex, VIRIDIS_KEY_COLORS.length - 2);
        
        // Calculate position within segment [0, 1]
        double segmentPosition = (clampedValue - segmentIndex * segmentSize) / segmentSize;
        
        // Interpolate between the two key colors in this segment
        double[] color1 = VIRIDIS_KEY_COLORS[segmentIndex];
        double[] color2 = VIRIDIS_KEY_COLORS[segmentIndex + 1];
        
        double r = color1[0] + (color2[0] - color1[0]) * segmentPosition;
        double g = color1[1] + (color2[1] - color1[1]) * segmentPosition;
        double b = color1[2] + (color2[2] - color1[2]) * segmentPosition;
        
        return Color.rgb((int) Math.round(r), (int) Math.round(g), (int) Math.round(b));
    }
    
    /**
     * Gets a viridis color for an index within a range.
     * 
     * @param index Current index
     * @param totalCount Total number of items
     * @return Color from the viridis palette
     */
    public static Color getViridisColorForIndex(int index, int totalCount) {
        if (totalCount <= 1) {
            return getViridisColor(0.0);
        }
        double normalizedValue = (double) index / (totalCount - 1);
        return getViridisColor(normalizedValue);
    }
    
    /**
     * Gets a viridis color as a hex string for an index within a range.
     * 
     * @param index Current index
     * @param totalCount Total number of items
     * @return Hex color string (e.g., "#440154")
     */
    public static String getViridisColorHexForIndex(int index, int totalCount) {
        Color color = getViridisColorForIndex(index, totalCount);
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}

