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

package com.ericbt.ebtcompass;

import java.util.Comparator;

public class PointComparitor implements Comparator<Point> {
    @Override
    public int compare(Point point1, Point point2) {
        final int colorComparison = point1.getColor() - point2.getColor();

        if (colorComparison != 0) {
            return colorComparison;
        }

        return point1.getName().toLowerCase().compareTo(point2.getName().toLowerCase());
    }
}

