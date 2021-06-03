package com.ericbt.ebtcompass.utils;

public class GoogleMapsUtils {
    public static String getMapUri(double latitude, double longitude) {
        final int zoom = 13;

        final String uri = String.format(
                LocaleUtils.getDefaultLocale(),
                "http://www.google.com/maps/place/%f,%f/@%f,%f,%dz",
                latitude,
                longitude,
                latitude,
                longitude,
                zoom);

        return uri;
    }
}
