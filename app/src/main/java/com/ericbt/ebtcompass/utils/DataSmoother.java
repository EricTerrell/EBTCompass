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

public class DataSmoother {
    private Float[] data;
    private int index = 0;

    public DataSmoother(int maxValues) {
        data = new Float[maxValues];

        for (int i = 0; i < data.length; i++) {
            data[i] = Float.NaN;
        }
    }

    public float add(float value) {
        data[index] = value;

        index = (index + 1) % data.length;

        return average();
    }

    private float average() {
        int n = 0;
        float sum = 0.0f;

        for (int i = 0; i < data.length; i++) {
            if (!data[i].isNaN()) {
                n++;
                sum += data[i];
            } else {
                break;
            }
        }

        return sum / (float) n;
    }
}
