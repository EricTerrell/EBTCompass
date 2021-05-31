package com.ericbt.ebtcompass.utils;

public class UnitUtils {
    public static double toFeet(double meters) {
        return meters * 3.2808f;
    }

    public static double toMilesPerHour(double metersPerSecond) {
        return metersPerSecond * 2.236936f;
    }

    public static double toKilometersPerHour(double metersPerSecond) {
        return metersPerSecond * 3.6f;
    }
}
