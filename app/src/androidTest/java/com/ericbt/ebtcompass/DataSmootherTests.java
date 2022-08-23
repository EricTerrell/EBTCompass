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
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ericbt.ebtcompass.utils.DataSmoother;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DataSmootherTests {
    @Test
    public void singleValue() {
        final DataSmoother dataSmoother = new DataSmoother(10);

        final Float value = 234.123f;

        Float average = dataSmoother.add(value);

        assertEquals(average, value);
    }

    @Test
    public void twoValues() {
        final DataSmoother dataSmoother = new DataSmoother(2);

        final Float value1 = 1.0f;
        final Float value2 = 2.0f;

        dataSmoother.add(value1);
        Float average = dataSmoother.add(value2);

        assertTrue(average == (value1 + value2) / 2.0f);
    }

    @Test
    public void fourValues() {
        final DataSmoother dataSmoother = new DataSmoother(2);

        final Float value1 = 1234.23452f;
        final Float value2 = 221234.12f;
        final Float value3 = 343.2342342f;
        final Float value4 = 41234.567567f;

        dataSmoother.add(value1);
        dataSmoother.add(value2);
        dataSmoother.add(value3);

        final Float average = dataSmoother.add(value4);

        assertTrue(average == (value3 + value4) / 2.0f);
    }

}