package com.ericbt.ebtcompass.utils;

public class CompassUtils {
    public static String getDeclinationDirection(double declination) {
        String result = "";

        if (declination < 0.0d) {
            result = "W";
        } else if (declination > 0.0d) {
            result = "E";
        }

        return result;
    }
}
