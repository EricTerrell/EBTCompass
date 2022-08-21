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

package com.ericbt.ebtcompass;

import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.Objects;

public class Point {
    private static final int NAME_INDEX         = 0; // not used but included for reference
    private static final int LATITUDE_INDEX     = 1; // not used but included for reference
    private static final int LONGITUDE_INDEX    = 2; // not used but included for reference
    private static final int COLOR_INDEX        = 3;
    private static final int ALTITUDE_INDEX     = 4;
    private static final int LINE_TO_NAME_INDEX = 5;

    private final static String DELIMITER = "\0";

    public final static int DEFAULT_COLOR = (int) BitmapDescriptorFactory.HUE_BLUE;

    private final double latitude, longitude, altitude;

    private String name, lineToName = null;

    private final int color;

    public Point(String name, String lineToName, double latitude, double longitude, double altitude, int color) {
        this.name = name;
        this.lineToName = lineToName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.color = color;
    }

    public Point(String name) {
        this.name = name;
        this.latitude = Double.NaN;
        this.longitude = Double.NaN;
        this.altitude = Double.NaN;
        this.color = DEFAULT_COLOR;
    }

    public String getName() { return name; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    public double getAltitude() { return altitude; }

    public int getColor() { return color; }

    public String getLineToName() {
        return lineToName;
    }

    @Override
    public String toString() {
        return String.format(LocaleUtils.getLocale(),
                "%s%s%.20f%s%.20f%s%d%s%.20f%s%s",
                name, DELIMITER,
                latitude, DELIMITER,
                longitude, DELIMITER,
                color, DELIMITER,
                altitude, DELIMITER,
                lineToName != null ? lineToName : StringLiterals.EMPTY_STRING);
    }

    public static Point fromString(String delimitedString) {
        final String[] values = delimitedString.split(DELIMITER);

        int color = DEFAULT_COLOR;

        if (values.length > COLOR_INDEX) {
            color = Integer.parseInt(values[COLOR_INDEX]);
        }

        double altitude = Double.NaN;

        if (values.length > ALTITUDE_INDEX) {
            altitude = Double.parseDouble(values[ALTITUDE_INDEX]);
        }

        String lineToName = null;

        if (values.length > LINE_TO_NAME_INDEX) {
            lineToName = values[LINE_TO_NAME_INDEX];
        }

        return new Point(values[0], lineToName, Double.parseDouble(values[1]), Double.parseDouble(values[2]), altitude, color);
    }

    public boolean isLineTo() {
        return lineToName != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Point point = (Point) o;
        return Objects.equals(name, point.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
