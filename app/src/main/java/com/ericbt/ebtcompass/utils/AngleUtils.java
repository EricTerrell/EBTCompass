package com.ericbt.ebtcompass.utils;

import static com.ericbt.ebtcompass.Constants.*;

public class AngleUtils {
    public static String toDMS(double decimalDegrees) {
        final int sign = decimalDegrees < 0.0f ? -1 : +1;

        decimalDegrees = Math.abs(decimalDegrees);

        final int degrees = (int) decimalDegrees;

        decimalDegrees -= degrees;

        final int minutes = (int) (decimalDegrees * MINUTES_PER_DEGREE);

        decimalDegrees -= minutes / MINUTES_PER_DEGREE;

        final int seconds = (int) (decimalDegrees * SECONDS_PER_DEGREE);

        decimalDegrees -= seconds / SECONDS_PER_DEGREE;

        final int fractionalSeconds = (int) (decimalDegrees * MINUTES_PER_DEGREE * 1000.0d);

        return String.format(LocaleUtils.getDefaultLocale(), "%s%d°%02d'%02d.%03d",
                sign == +1 ? "" : "-",
                degrees,
                minutes,
                seconds,
                fractionalSeconds);
    }

    public static String latitudeDirection(double latitude) {
        return latitude >= 0.0d ? "N" : "S";
    }

    public static String longitudeDirection(double longitude) {
        return longitude >= 0.0d ? "E" : "W";
    }

    private final static String[] directions = {
            "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
            "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW",
            "N"};

    // https://stackoverflow.com/questions/2131195/cardinal-direction-algorithm-in-java
    public static String formatBearing(double bearing) {
        return directions[(int) Math.floor(((bearing + 11.25d) % DEGREES_PER_CIRCLE) / 22.5d)];
    }
}
