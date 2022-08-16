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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class PointsTest {
    private final static int CABIN_COLOR = 10;
    private final static int HOUSE_COLOR = 5;

    private Point[] points;

    private static final double HOUSE_C_LATITUDE = 54.23423d;
    private static final double HOUSE_C_LONGITUDE = 102.34234234d;
    private static final double HOUSE_C_ALTITUDE = 5278.234234243d;

    private static final String DOES_NOT_EXIST = "DOES NOT EXIST";

    @Before
    public void setup() {
        final List<Point> pointList = new ArrayList<>();

        Point point = new Point("Cabin A", "Cabin B", 37.123d, 108.23423d, 5220d, CABIN_COLOR);
        pointList.add(point);

        point = new Point("Cabin D", "Cabin A", 37.123d, 108.23423d, 5220d, CABIN_COLOR);
        pointList.add(point);

        point = new Point("Cabin C", "Cabin D", 37.123d, 108.23423d, 5220d, CABIN_COLOR);
        pointList.add(point);

        point = new Point("Cabin B", "Cabin C", 37.123d, 108.23423d, 5220d, CABIN_COLOR);
        pointList.add(point);

        point = new Point("Cabin", null, 37.123d, 108.23423d, 5220d, CABIN_COLOR);
        pointList.add(point);

        point = new Point("House A", "House B", 37.123d, 108.23423d, 5220d, HOUSE_COLOR);
        pointList.add(point);

        point = new Point("House D", "House A", 37.123d, 108.23423d, 5220d, HOUSE_COLOR);
        pointList.add(point);

        point = new Point("House C", "House D", HOUSE_C_LATITUDE, HOUSE_C_LONGITUDE, HOUSE_C_ALTITUDE, HOUSE_COLOR);
        pointList.add(point);

        point = new Point("House B", "House C", 37.123d, 108.23423d, 5220d, HOUSE_COLOR);
        pointList.add(point);

        point = new Point("House", null, 37.123d, 108.23423d, 5220d, HOUSE_COLOR);
        pointList.add(point);

        Collections.sort(pointList, new PointComparitor());

        points = pointList.toArray(new Point[0]);
    }

    @Test
    public void exists() {
        boolean result = Points.exists(points, "House C");
        assertTrue(result);

        result = Points.exists(points, null);
        assertFalse(result);

        result = Points.exists(points, "");
        assertFalse(result);

        result = Points.exists(points, "Hello Eric!");
        assertFalse(result);
    }

    @Test
    public void get() {
        Point point = Points.get(points, "House C");
        assertNotNull(point);
        assertEquals(HOUSE_C_LATITUDE, point.getLatitude(), 0.0d);
        assertEquals(HOUSE_C_LONGITUDE, point.getLongitude(), 0.0d);
        assertEquals(HOUSE_C_ALTITUDE, point.getAltitude(), 0.0d);
        assertEquals("House C", point.getName());
        assertEquals("House D", point.getLineToName());

        point = Points.get(points, null);
        assertNull(point);

        point = Points.get(points, "");
        assertNull(point);

        point = Points.get(points, DOES_NOT_EXIST);
        assertNull(point);
    }

    @Test
    public void sort() {
        assertEquals("House", points[0].getName());
        assertEquals("House D", points[4].getName());

        assertEquals("Cabin D", points[9].getName());
    }

    @Test
    public void ordinalPosition() {
        int ordinalPosition = Points.getOrdinalPosition(points, DOES_NOT_EXIST);
        assertTrue(ordinalPosition < 0);

        ordinalPosition = Points.getOrdinalPosition(points, "House");
        assertEquals(0, ordinalPosition);

        ordinalPosition = Points.getOrdinalPosition(points, "Cabin D");
        assertEquals(9, ordinalPosition);
    }
}