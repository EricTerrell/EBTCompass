package com.ericbt.ebtcompass.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;

public class AboutActivity extends CustomActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        final Button readLicenseTerms = findViewById(R.id.read_license_terms);

        final ActivityResultLauncher<Intent> licenseTermsActivityResultLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                final Intent data = result.getData();

                                if (data != null && data.getBooleanExtra(StringLiterals.EXIT, false)) {
                                    final Intent returnData = new Intent();
                                    returnData.putExtra(StringLiterals.EXIT, true);

                                    setResult(RESULT_CANCELED, returnData);

                                    finish();
                                }
                            }
                        });

        readLicenseTerms.setOnClickListener(view -> {
            final Intent intent = new Intent(this, LicenseTermsActivity.class);
            intent.putExtra(StringLiterals.ALLOW_CANCEL, true);

            licenseTermsActivityResultLauncher.launch(intent);
        });
    }
}