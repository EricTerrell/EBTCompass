/*
  EBT Compass
  (C) Copyright 2022, Eric Bergman-Terrell

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

import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;
import com.ibm.util.CoordinateConversion;

import static com.ericbt.ebtcompass.Constants.*;

import android.content.Context;

public class AngleUtils {
    private final static CoordinateConversion coordinateConversion = new CoordinateConversion();

    public static String toDMS(double decimalDegrees, Context context) {
        final int sign = decimalDegrees < 0.0f ? -1 : +1;

        decimalDegrees = Math.abs(decimalDegrees);

        final int degrees = (int) decimalDegrees;

        decimalDegrees -= degrees;

        final int minutes = (int) (decimalDegrees * MINUTES_PER_DEGREE);

        decimalDegrees -= minutes / MINUTES_PER_DEGREE;

        final int seconds = (int) (decimalDegrees * SECONDS_PER_DEGREE);

        decimalDegrees -= seconds / SECONDS_PER_DEGREE;

        final int fractionalSeconds = (int) (decimalDegrees * SECONDS_PER_DEGREE * 1000.0d);

        final String degreeSymbol = context.getString(R.string.degree_symbol);
        final String minutesSymbol = context.getString(R.string.minutes_symbol);
        final String secondsSymbol = context.getString(R.string.seconds_symbol);

        final String toDMSFormatString = context.getString(R.string.to_DMS_format_string);

        final String positiveNumberSign = context.getString(R.string.positive_number_sign);
        final String negativeNumberSign = context.getString(R.string.negative_number_sign);

        return String.format(LocaleUtils.getLocale(),
                toDMSFormatString,
                sign == 1 ? positiveNumberSign : negativeNumberSign,
                degrees,
                degreeSymbol,
                minutes,
                minutesSymbol,
                seconds,
                I18NUtils.getDecimalPoint(),
                fractionalSeconds,
                secondsSymbol);
    }

    public static String latitudeDirection(double latitude, Context context) {

        return latitude >= 0.0d ?
                context.getString(R.string.north_abbreviation) :
                context.getString(R.string.south_abbreviation);
    }

    public static String longitudeDirection(double longitude, Context context) {
        return longitude >= 0.0d ?
                context.getString(R.string.east_abbreviation) :
                context.getString(R.string.west_abbreviation);
    }

    // https://stackoverflow.com/questions/2131195/cardinal-direction-algorithm-in-java
    public static String formatBearing(double bearing, Context context) {
        final String[] directions = context.getResources().getStringArray(R.array.bearing_directions);

        return directions[(int) Math.floor(((bearing + 11.25d) % DEGREES_PER_CIRCLE) / 22.5d)];
    }

    /***
     * Return UTM zone, easting, northing values
     * @param latitude latitude
     * @param longitude longitude
     * @return array of zone, easting, and northing values
     */
    public static String[] getUTMValues(double latitude, double longitude, Context context) {
        final String[] utmCoordinates = coordinateConversion
                .latLon2UTM(latitude, longitude)
                .split(StringLiterals.REGEX_WORDS);

        final String utmZone = String.format(
                LocaleUtils.getLocale(),
                context.getString(R.string.utm_zone_format_string),
                utmCoordinates[0],
                utmCoordinates[1]);

        final String utmEasting = utmCoordinates[2];
        final String utmNorthing = utmCoordinates[3];

        return new String[] { utmZone, utmEasting, utmNorthing };
    }

    public static String formatUTM(double latitude, double longitude, Context context) {
        final String[] utmValues = getUTMValues(latitude, longitude, context);

        return String.format(
                LocaleUtils.getLocale(),
                context.getString(R.string.utm_format_string),
                utmValues[0],
                utmValues[1],
                utmValues[2]);
    }
}
