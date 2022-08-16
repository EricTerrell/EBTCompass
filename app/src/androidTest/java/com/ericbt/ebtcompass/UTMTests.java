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

import com.ibm.util.CoordinateConversion;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * https://www.latlong.net/lat-long-utm.html
 */
@RunWith(AndroidJUnit4.class)
public class UTMTests {
    private final static double DELTA = 0.0001f;

    private final static double LATITUDE = 37.3764444;
    private final static double LONGITUDE = -108.695213;

    private final static String UTM = "12 S 704070 4139126";

    @Test
    public void latLong2UTM() {
        final CoordinateConversion coordinateConversion = new CoordinateConversion();

        String utm = coordinateConversion.latLon2UTM(LATITUDE, LONGITUDE);

        assertEquals(UTM, utm);
    }

    @Test
    public void utm2LatLong() {
        final CoordinateConversion coordinateConversion = new CoordinateConversion();

        double[] result = coordinateConversion.utm2LatLon(UTM);

        final double latitude = result[0];
        final double longitude = result[1];

        assertEquals(LATITUDE, latitude, DELTA);
        assertEquals(LONGITUDE, longitude, DELTA);
    }

}