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

import android.graphics.Color;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.HashMap;
import java.util.Map;

public class ColorConverter {
    final static Map<Float, String> map = new HashMap<>();

    static {
        map.put(BitmapDescriptorFactory.HUE_RED,     "#FF0000");
        map.put(BitmapDescriptorFactory.HUE_ORANGE,  "#FFA500");
        map.put(BitmapDescriptorFactory.HUE_YELLOW,  "#FFFF00");
        map.put(BitmapDescriptorFactory.HUE_GREEN,   "#008000");
        map.put(BitmapDescriptorFactory.HUE_CYAN,    "#00FFFF");
        map.put(BitmapDescriptorFactory.HUE_AZURE,   "#007FFF");
        map.put(BitmapDescriptorFactory.HUE_BLUE,    "#0000FF");
        map.put(BitmapDescriptorFactory.HUE_VIOLET,  "#8F00FF");
        map.put(BitmapDescriptorFactory.HUE_MAGENTA, "#FF00FF");
        map.put(BitmapDescriptorFactory.HUE_ROSE,    "#FF007F");
    }

    public static int hueToColor(float hue) {
        return Color.parseColor(map.get(hue));
    }
}
