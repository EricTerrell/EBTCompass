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

package com.ericbt.ebtcompass;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Points {
    public static boolean exists(Context context, String name) {
        return Arrays.asList(Points.getAll(context)).contains(new Point(name));
    }

    public static void upsert(Context context, Point point) {
        final Set<String> points = new HashSet<>();

        boolean updated = false;

        // update
        for (Point currentPoint : getAll(context)) {
            if (!currentPoint.getName().equals(point.getName())) {
                points.add(currentPoint.toString());
            } else {
                points.add(point.toString());
                updated = true;
            }
        }

        // insert
        if (!updated) {
            points.add(point.toString());
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();

        editor.putStringSet(StringLiterals.PREFERENCE_POINTS, points).apply();
    }

    public static void delete(Context context, String name) {
        final Set<String> points = new HashSet<>();

        for (Point currentPoint : getAll(context)) {
            if (!currentPoint.getName().equals(name)) {
                points.add(currentPoint.toString());
            }
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();

        editor.putStringSet(StringLiterals.PREFERENCE_POINTS, points).apply();
    }
    public static void rename(Context context, String oldName, String newName) {
        final Set<String> points = new HashSet<>();

        for (Point currentPoint : getAll(context)) {
            if (currentPoint.getName().equals(oldName)) {
                currentPoint.setName(newName);
            }

            points.add(currentPoint.toString());
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();

        editor.putStringSet(StringLiterals.PREFERENCE_POINTS, points).apply();
    }

    private static class PointComparitor implements Comparator<Point> {
        @Override
        public int compare(Point point1, Point point2) {
            final int colorComparison = point1.getColor() - point2.getColor();

            if (colorComparison != 0) {
                return colorComparison;
            }

            return point1.getName().toLowerCase().compareTo(point2.getName().toLowerCase());
        }
    }

    public static Point[] getAll(Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        final Set<String> points = preferences.getStringSet(StringLiterals.PREFERENCE_POINTS, new HashSet<>());

        final List<Point> result = new ArrayList<>();

        for (String pointString : points.toArray(new String[0])) {
            result.add(Point.fromString(pointString));
        }

        Collections.sort(result, new PointComparitor());

        return result.toArray(new Point[0]);
    }
}
