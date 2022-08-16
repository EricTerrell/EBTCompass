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

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

public class ColorUtils {
    private final static Map<Integer, Integer> pointColor2MapColor = new HashMap<>();

    static {
        // Must agree with color_entries string array.

        final int alpha = 0xff;

        pointColor2MapColor.put(  0, Color.argb(alpha, 0xff,  0x00, 0x00)); // RED
        pointColor2MapColor.put( 30, Color.argb(alpha, 0xff,  0xa5, 0x00)); // ORANGE
        pointColor2MapColor.put( 60, Color.argb(alpha, 0xff,  0xff, 0x00)); // YELLOW
        pointColor2MapColor.put(120, Color.argb(alpha, 0x00,  0x80, 0x00)); // GREEN
        pointColor2MapColor.put(180, Color.argb(alpha, 0x00,  0xff, 0xff)); // CYAN
        pointColor2MapColor.put(210, Color.argb(alpha, 0x00,  0x7f, 0xff)); // AZURE
        pointColor2MapColor.put(240, Color.argb(alpha, 0x00,  0x00, 0xff)); // BLUE
        pointColor2MapColor.put(270, Color.argb(alpha, 0x8f,  0x00, 0xff)); // VIOLET
        pointColor2MapColor.put(300, Color.argb(alpha, 0xff,  0x00, 0xff)); // MAGENTA
        pointColor2MapColor.put(330, Color.argb(alpha, 0xff,  0x00, 0x00)); // ROSE
    }

    public static int PointColorToLineColor(int pointColor) {
        return pointColor2MapColor.get(pointColor);
    }
}
