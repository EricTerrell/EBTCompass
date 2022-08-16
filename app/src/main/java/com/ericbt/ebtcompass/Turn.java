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

import com.ericbt.ebtcompass.utils.MathUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Turn {
    // A heading withing 10 degrees of desired is close enough. Do not signal LEFT or RIGHT.
    final private static float CLOSE_ENOUGH = 10.0f;

    public enum Direction { NONE, LEFT, RIGHT }

    public static Direction getDirection(float goToHeading, float bearing) {
        final float currentHeading = MathUtils.normalizeAngle(bearing - goToHeading);

        if (currentHeading >= 0.0f && currentHeading <= CLOSE_ENOUGH) {
            return Direction.NONE;
        }

        if (currentHeading >= Constants.DEGREES_PER_CIRCLE - CLOSE_ENOUGH && currentHeading <= Constants.DEGREES_PER_CIRCLE) {
            return Direction.NONE;
        }

        if (currentHeading >= 180.0f) {
            return Direction.RIGHT;
        } else {
            return Direction.LEFT;
        }
    }

    public static Direction getMostCommonDirection(List<Direction> directions) {
        final Map<Direction, Integer> directionCounts = new HashMap<>();

        directionCounts.put(Direction.NONE, 0);
        directionCounts.put(Direction.LEFT, 0);
        directionCounts.put(Direction.RIGHT, 0);

        for (final Direction direction : directions) {
            directionCounts.put(direction, directionCounts.get(direction) + 1);
        }

        int maxCount = -1;
        Direction maxDirection = Direction.NONE;

        for (final Map.Entry<Direction, Integer> entry : directionCounts.entrySet()) {
            if (entry.getValue().intValue() > maxCount) {
                maxCount = entry.getValue().intValue();
                maxDirection = entry.getKey();
            }
        }

        return maxDirection;
    }
}
