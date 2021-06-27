/*
  EBT Compass
  (C) Copyright 2021, Eric Bergman-Terrell

  This file is part of EBT Compass.

    EBT Compass is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    EBT Compass is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EBT Compass.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ericbt.ebtcompass.utils;

import com.ericbt.ebtcompass.StringLiterals;
import com.ibm.util.CoordinateConversion;

import static com.ericbt.ebtcompass.Constants.*;

public class AngleUtils {
    private final static CoordinateConversion coordinateConversion = new CoordinateConversion();

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

        return String.format(LocaleUtils.getDefaultLocale(), "%s%d°%02d'%02d.%03d\"",
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

    public static String formatUTM(double latitude, double longitude) {
        final String utmCoordinates[] = coordinateConversion
                .latLon2UTM(latitude, longitude)
                .split(StringLiterals.REGEX_WORDS);

        final String utmZone = String.format(LocaleUtils.getDefaultLocale(), "%s%s", utmCoordinates[0], utmCoordinates[1]);

        final String utmEasting = utmCoordinates[2];
        final String utmNorthing = utmCoordinates[3];

        return String.format(
                LocaleUtils.getDefaultLocale(),
                "UTM: %s %sE %sN",
                utmZone,
                utmEasting,
                utmNorthing);
    }
}
