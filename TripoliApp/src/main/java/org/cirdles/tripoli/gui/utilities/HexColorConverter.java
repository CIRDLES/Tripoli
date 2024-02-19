package org.cirdles.tripoli.gui.utilities;

import javafx.scene.paint.Color;

import java.util.Arrays;

public class HexColorConverter {
    private static final char[] HEX_ALPHABET = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static String getTwoHexDigits(int value) {

        char[] digits = new char[2];
        Arrays.fill(digits, HEX_ALPHABET[0]);
        int rightDigitIndex = 0;
        int leftDigitIndex = 0;
        while (value > 0) {
            --value;
            ++rightDigitIndex;
            if (rightDigitIndex == HEX_ALPHABET.length) {
                ++leftDigitIndex;
                rightDigitIndex %= HEX_ALPHABET.length;
            }
        }
        digits[0] = HEX_ALPHABET[leftDigitIndex];
        digits[1] = HEX_ALPHABET[rightDigitIndex];
        StringBuilder builder = new StringBuilder();
        builder.append(digits);
        return builder.toString();
    }

    public static String getHexColor(Color inputColor) {
        StringBuilder builder = new StringBuilder("#");
        int red = (int) (inputColor.getRed() * 255);
        int green = (int) (inputColor.getGreen() * 255);
        int blue = (int) (inputColor.getBlue() * 255);
        builder.append(getTwoHexDigits(red));
        builder.append(getTwoHexDigits(green));
        builder.append(getTwoHexDigits(blue));
        return builder.toString();
    }
}
