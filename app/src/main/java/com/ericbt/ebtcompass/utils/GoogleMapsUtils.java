/*
  EBT Compass
  (C) Copyright 2023, Eric Bergman-Terrell

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

public class GoogleMapsUtils {
    public static String getMapUri(double latitude, double longitude) {
        final int zoom = 13;

        return String.format(
                LocaleUtils.getLocale(),
                "http://www.google.com/maps/place/%f,%f/@%f,%f,%dz",
                latitude,
                longitude,
                latitude,
                longitude,
                zoom);
    }
}
