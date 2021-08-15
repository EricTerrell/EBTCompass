package com.ericbt.ebtcompass;

import com.ericbt.ebtcompass.utils.LocaleUtils;

import java.util.Objects;

public class Point {
    private final static String DELIMITER = "\0";

    private final double latitude, longitude;

    private String name;

    public Point(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Point(String name) {
        this.name = name;
        this.latitude = 0.0f;
        this.longitude = 0.0f;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    @Override
    public String toString() {
        return String.format(LocaleUtils.getDefaultLocale(), "%s%s%.20f%s%.20f",
                name, DELIMITER, latitude, DELIMITER, longitude);
    }

    public static Point fromString(String delimitedString) {
        final String[] values = delimitedString.split(DELIMITER);

        return new Point(values[0], Double.parseDouble(values[1]), Double.parseDouble(values[2]));
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
