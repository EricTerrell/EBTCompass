package com.ericbt.ebtcompass.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

// https://stackoverflow.com/questions/15686555/display-back-button-on-action-bar/37185334#37185334
public class CustomActivity extends AppCompatActivity {
    @Override
    public boolean onSupportNavigateUp() {
        finish();

        return true;
    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
