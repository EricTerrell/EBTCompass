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

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.ericbt.ebtcompass.StringLiterals;

public class UnitUtils {
    private static final double FEET_PER_METER = 3.2808f;

    public static double toFeet(double meters) {
        return meters * FEET_PER_METER;
    }

    public static double toMeters(double feet) {
        return feet / FEET_PER_METER;
    }

    public static double toMilesPerHour(double metersPerSecond) {
        return metersPerSecond * 2.236936f;
    }

    public static double toKilometersPerHour(double metersPerSecond) {
        return metersPerSecond * 3.6f;
    }

    public static double toMiles(double feet) {
        return feet / 5280.0f;
    }

    public static boolean userPrefersMetric(Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        final String distanceUnits = preferences.getString(StringLiterals.PREFERENCE_KEY_DISTANCE_UNITS, StringLiterals.METRIC);

        return StringLiterals.METRIC.equals(distanceUnits);
    }
}
