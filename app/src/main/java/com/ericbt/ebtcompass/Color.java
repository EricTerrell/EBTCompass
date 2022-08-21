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

import android.content.Context;

import java.util.Objects;

public class Color {
    public final static Color RED     = new Color(R.string.color_red,       0);
    public final static Color ORANGE  = new Color(R.string.color_orange,   30);
    public final static Color YELLOW  = new Color(R.string.color_yellow,   60);
    public final static Color GREEN   = new Color(R.string.color_green,   120);
    public final static Color CYAN    = new Color(R.string.color_cyan,    180);
    public final static Color AZURE   = new Color(R.string.color_azure,   210);
    public final static Color BLUE    = new Color(R.string.color_blue,    240);
    public final static Color VIOLET  = new Color(R.string.color_violet,  270);
    public final static Color MAGENTA = new Color(R.string.color_magenta, 300);
    public final static Color ROSE    = new Color(R.string.color_rose,    330);

    private int nameId;

    private final int hue;

    public Color(int nameId, int hue) {
        this.nameId = nameId;
        this.hue = hue;
    }

    public Color(int hue) {
        this.hue = hue;
    }

    public String getName(Context context) {
        return context.getString(nameId);
    }

    public int getHue() {
        return hue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Color color = (Color) o;
        return hue == color.hue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hue);
    }
}
