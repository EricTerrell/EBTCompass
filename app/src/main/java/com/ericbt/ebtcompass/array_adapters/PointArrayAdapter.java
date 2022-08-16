/*
  EBT Compass
  (C) Copyright 2022, Eric Bergman-Terrell

  This file is part of EBT Compass.

    EBT Music Player is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    EBT Music Player is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EBT Music Player.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ericbt.ebtcompass.array_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ericbt.ebtcompass.ColorConverter;
import com.ericbt.ebtcompass.Point;
import com.ericbt.ebtcompass.R;

public class PointArrayAdapter extends ArrayAdapter<Point> {
    private final Context context;
    private final int resource;
    private final int textViewResourceId;

    public PointArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, textViewResourceId);

        this.context = context;
        this.resource = resource;
        this.textViewResourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(resource, null);
        }

        final Point point = getItem(position);

        final ImageView marker = convertView.findViewById(R.id.marker);
        marker.setColorFilter(ColorConverter.hueToColor(point.getColor()));

        final TextView textView = convertView.findViewById(textViewResourceId);
        textView.setText(point.getName());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
