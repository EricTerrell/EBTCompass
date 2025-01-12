/*
  EBT Compass
  (C) Copyright 2025, Eric Bergman-Terrell

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

import static android.graphics.Color.WHITE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ericbt.ebtcompass.R;

public class AlertDialogUtils {
    public static AlertDialog.Builder getBuilder(Context context) {
        return new AlertDialog.Builder(context, R.style.Theme_EBTCompass);
    }
    
    // Call after AlertDialog.show() to improve button visibility.
    public static void show(Context context, AlertDialog alertDialog) {
        alertDialog.show();

        final Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        final Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        positiveButton.setTextColor(WHITE);
        negativeButton.setTextColor(WHITE);

        positiveButton.setBackgroundColor(
                context.getResources().getColor(R.color.colorPrimary,
                context.getTheme()));

        negativeButton.setBackgroundColor(
                context.getResources().getColor(R.color.colorPrimary,
                context.getTheme()));

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        
        params.setMargins(20,0,0,0);

        positiveButton.setLayoutParams(params);
        negativeButton.setLayoutParams(params);
    }
}
