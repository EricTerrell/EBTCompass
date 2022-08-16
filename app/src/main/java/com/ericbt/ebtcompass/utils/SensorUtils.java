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

import java.util.HashMap;
import java.util.Map;

import static android.hardware.SensorManager.*;

public class SensorUtils {
    private static final Map<Integer, String> dictionary = new HashMap<>();

    static {
        dictionary.put(SENSOR_STATUS_ACCURACY_HIGH, "High");
        dictionary.put(SENSOR_STATUS_ACCURACY_MEDIUM, "Medium");
        dictionary.put(SENSOR_STATUS_ACCURACY_LOW, "Low");
        dictionary.put(SENSOR_STATUS_NO_CONTACT, "No Contact");
        dictionary.put(SENSOR_STATUS_UNRELIABLE, "Unreliable");
    }

    public static String getAccuracyText(int accuracy) {
        return dictionary.get(accuracy);
    }

}
