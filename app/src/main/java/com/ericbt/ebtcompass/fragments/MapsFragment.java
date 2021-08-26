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

package com.ericbt.ebtcompass.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ericbt.ebtcompass.Point;
import com.ericbt.ebtcompass.Points;
import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment {
    private String name;

    private double latitude, longitude;

    private GoogleMap googleMap;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsFragment.this.googleMap = googleMap;

            googleMap.getUiSettings().setMapToolbarEnabled(false);

            final Point[] points = Points.getAll(getContext());

            for (final Point point : points) {
                final LatLng location = new LatLng(point.getLatitude(), point.getLongitude());

                final boolean isPointToView = point.getName().equals(name);

                final float zIndex = isPointToView ? 10.0f : 1.0f;

                final MarkerOptions markerOptions = new MarkerOptions()
                        .position(location)
                        .title(point.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(point.getColor()))
                        .zIndex(zIndex);

                final Marker marker = googleMap.addMarker(markerOptions);

                if (isPointToView) {
                    marker.showInfoWindow();
                }
            }

            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            final float zoom = preferences.getFloat(StringLiterals.PREFERENCE_ZOOM_LEVEL, -1.0f);

            if (zoom >= 0.0f) {
                googleMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
            }

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        latitude = getActivity().getIntent().getDoubleExtra(StringLiterals.LATITUDE, 0.0f);
        longitude = getActivity().getIntent().getDoubleExtra(StringLiterals.LONGITUDE, 0.0f);
        name = getActivity().getIntent().getStringExtra(StringLiterals.NAME);
    }

    @Override
    public void onDestroyView() {
        final float zoom = googleMap.getCameraPosition().zoom;

        Log.i(StringLiterals.LOG_TAG, String.format("zoom: %f", zoom));

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(StringLiterals.PREFERENCE_ZOOM_LEVEL, zoom);

        editor.apply();

        super.onDestroyView();
    }
}