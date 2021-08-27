package com.ericbt.ebtcompass.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;

import com.ericbt.ebtcompass.Points;
import com.ericbt.ebtcompass.R;
import com.ericbt.ebtcompass.StringLiterals;

public class UpdatePointActivity extends BaseSaveUpdatePointActivity {
    private EditText name;

    @Override
    protected void onSaveButtonClicked() {
        Points.delete(this, originalName);

        super.onSaveButtonClicked();
    }

    private String originalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        originalName = getIntent().getStringExtra(StringLiterals.ORIGINAL_NAME);
        name = findViewById(R.id.name);
        name.setText(originalName);

        setColorSpinner();
    }

    private void setColorSpinner() {
        final String[] values = getResources().getStringArray(R.array.color_values);

        final String originalColor = getIntent().getStringExtra(StringLiterals.COLOR);

        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(originalColor)) {
                final Spinner colorSpinner = findViewById(R.id.color);
                colorSpinner.setSelection(i);

                break;
            }
        }
    }
}
