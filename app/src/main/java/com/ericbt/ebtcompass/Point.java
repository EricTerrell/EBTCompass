package com.ericbt.ebtcompass;

import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.Objects;

public class Point {
    private static final int NAME_INDEX      = 0;
    private static final int LATITUDE_INDEX  = 1;
    private static final int LONGITUDE_INDEX = 2;
    private static final int COLOR_INDEX     = 3;
    private static final int ALTITUDE_INDEX  = 4;

    private final static String DELIMITER = "\0";

    public final static int DEFAULT_COLOR = (int) BitmapDescriptorFactory.HUE_BLUE;

    private final double latitude, longitude, altitude;

    private String name;

    private final int color;

    public Point(String name, double latitude, double longitude, double altitude, int color) {
        this.name = name;
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

    public void setName(String name) { this.name = name; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    public double getAltitude() { return altitude; }

    public int getColor() { return color; }

    @Override
    public String toString() {
        return String.format(LocaleUtils.getDefaultLocale(), "%s%s%.20f%s%.20f%s%d%s%.20f",
                name, DELIMITER,
                latitude, DELIMITER,
                longitude, DELIMITER,
                color, DELIMITER,
                altitude);
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

        return new Point(values[0], Double.parseDouble(values[1]), Double.parseDouble(values[2]), altitude, color);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Objects.equals(name, point.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
