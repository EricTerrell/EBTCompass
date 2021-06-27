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

    public static double toMiles(double feet) {
        return feet / 5280.0f;
    }
}
