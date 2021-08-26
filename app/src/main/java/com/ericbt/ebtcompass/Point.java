package com.ericbt.ebtcompass;

import com.ericbt.ebtcompass.utils.LocaleUtils;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.Objects;

public class Point {
    private final static String DELIMITER = "\0";

    public final static int DEFAULT_COLOR = (int) BitmapDescriptorFactory.HUE_BLUE;

    private final double latitude, longitude;

    private String name;

    private final int color;

    public Point(String name, double latitude, double longitude, int color) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.color = color;
    }

    public Point(String name) {
        this.name = name;
        this.latitude = 0.0f;
        this.longitude = 0.0f;
        this.color = DEFAULT_COLOR;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    public int getColor() { return color; }

    @Override
    public String toString() {
        return String.format(LocaleUtils.getDefaultLocale(), "%s%s%.20f%s%.20f%s%d",
                name, DELIMITER, latitude, DELIMITER, longitude, DELIMITER, color);
    }

    public static Point fromString(String delimitedString) {
        final String[] values = delimitedString.split(DELIMITER);

        int color = DEFAULT_COLOR;

        if (values.length >= 4) {
            color = Integer.parseInt(values[3]);
        }

        return new Point(values[0], Double.parseDouble(values[1]), Double.parseDouble(values[2]), color);
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
