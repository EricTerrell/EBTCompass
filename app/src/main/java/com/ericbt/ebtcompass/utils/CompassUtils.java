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

import android.content.Context;

import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;

public class CompassUtils {
    public static String getDeclinationDirection(double declination, Context context) {
        String result = StringLiterals.EMPTY_STRING;

        if (declination < 0.0d) {
            result = context.getString(R.string.west_abbreviation);
        } else if (declination > 0.0d) {
            result = context.getString(R.string.east_abbreviation);
        }

        return result;
    }
}
