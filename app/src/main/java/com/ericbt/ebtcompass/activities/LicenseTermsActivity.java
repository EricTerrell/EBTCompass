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

package com.ericbt.ebtcompass.activities;

import androidx.preference.PreferenceManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;

import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;

public class LicenseTermsActivity extends CustomActivity {
    private AlertDialog alertDialog;

    private boolean allowCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_license_terms);

        allowCancel = getIntent().getExtras ().getBoolean(StringLiterals.ALLOW_CANCEL);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean userAcceptedTerms = preferences.getBoolean(StringLiterals.USER_ACCEPTED_TERMS, false);

        final RadioButton acceptLicenseTerms = findViewById(R.id.AcceptLicenseTerms);
        acceptLicenseTerms.setChecked(userAcceptedTerms);

        final RadioButton rejectLicenseTerms = findViewById(R.id.RejectLicenseTerms);
        rejectLicenseTerms.setChecked(!userAcceptedTerms);

        final Button okButton = findViewById(R.id.OKButton);

        okButton.setOnClickListener(view -> {
            final boolean userAccepted = acceptLicenseTerms.isChecked();

            preferences.edit().putBoolean(StringLiterals.USER_ACCEPTED_TERMS, userAccepted).apply();

            if (!userAccepted) {
                final Intent returnData = new Intent();
                returnData.putExtra(StringLiterals.EXIT, true);

                setResult(RESULT_CANCELED, returnData);
            }

            if (!userAccepted) {
                final AlertDialog.Builder userRejectedTermsDialogBuilder = new AlertDialog.Builder(LicenseTermsActivity.this);
                userRejectedTermsDialogBuilder.setTitle(String.format(getString(R.string.rejected_license_terms_format_string), getString(R.string.app_name)));
                userRejectedTermsDialogBuilder.setMessage(String.format(getString(R.string.rejected_license_terms_format_string_2), getString(R.string.app_name), getString(R.string.app_name)));
                userRejectedTermsDialogBuilder.setPositiveButton(getText(R.string.ok), (dialog, which) -> {
                    alertDialog.dismiss();

                    LicenseTermsActivity.this.finish();
                });

                userRejectedTermsDialogBuilder.setCancelable(false);

                alertDialog = userRejectedTermsDialogBuilder.create();
                alertDialog.show();
            }
            else {
                finish();
            }
        });
    }

    // When user navigates from AboutActivity, don't prevent the user from going back without
    // making a decision.

    @Override
    public void onBackPressed() {
        if (allowCancel) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (allowCancel) {
            finish();

            return true;
        } else {
            return false;
        }
    }
}