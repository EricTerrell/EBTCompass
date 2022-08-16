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

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TurnTests {
    @Test
    public void noTurnsNecessary() {
        float goToHeading = 0.0f; // north
        float bearing = 9.0f;     // a little east of north

        Turn.Direction direction = Turn.getDirection(goToHeading, bearing);
        assertEquals(Turn.Direction.NONE, direction);

        bearing = 351.0f; // a little west of north

        direction = Turn.getDirection(goToHeading, bearing);
        assertEquals(Turn.Direction.NONE, direction);
    }

    @Test
    public void turnsNecessary() {
        float goToHeading = 270.0f;
        float bearing     = 300.0f;

        Turn.Direction direction = Turn.getDirection(goToHeading, bearing);
        assertEquals(Turn.Direction.LEFT, direction);

        bearing = 240.0f;

        direction = Turn.getDirection(goToHeading, bearing);
        assertEquals(Turn.Direction.RIGHT, direction);

        goToHeading = 10.0f;
        bearing = 359.0f;

        direction = Turn.getDirection(goToHeading, bearing);
        assertEquals(Turn.Direction.RIGHT, direction);

        goToHeading = 359.0f;
        bearing = 340.0f;

        direction = Turn.getDirection(goToHeading, bearing);
        assertEquals(Turn.Direction.RIGHT, direction);

        bearing = 10.0f;

        direction = Turn.getDirection(goToHeading, bearing);
        assertEquals(Turn.Direction.LEFT, direction);

        goToHeading = 1.0f;
        bearing = 345.0f;

        direction = Turn.getDirection(goToHeading, bearing);
        assertEquals(Turn.Direction.RIGHT, direction);

        goToHeading = 359.0f;
        bearing = 11.0f;

        direction = Turn.getDirection(goToHeading, bearing);
        assertEquals(Turn.Direction.LEFT, direction);
    }

    @Test
    public void mostlyLeftTurns() {
        final Turn.Direction[] values = { Turn.Direction.LEFT, Turn.Direction.NONE, Turn.Direction.LEFT, Turn.Direction.RIGHT };
        final List<Turn.Direction> directions = Arrays.asList(values);

        final Turn.Direction mostCommonDirection = Turn.getMostCommonDirection(directions);
        assertEquals(Turn.Direction.LEFT, mostCommonDirection);
    }
}