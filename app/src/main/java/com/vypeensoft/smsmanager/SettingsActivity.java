package com.vypeensoft.smsmanager;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        android.content.SharedPreferences prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE);
        // Default font size can be 16
        final int[] currentSize = {prefs.getInt("font_size", 16)};

        android.widget.TextView tvFontSize = findViewById(R.id.tvFontSize);
        android.widget.Button btnDecreaseFont = findViewById(R.id.btnDecreaseFont);
        android.widget.Button btnIncreaseFont = findViewById(R.id.btnIncreaseFont);

        tvFontSize.setText(String.valueOf(currentSize[0]));

        btnDecreaseFont.setOnClickListener(v -> {
            if (currentSize[0] > 10) { // Minimum size 10
                currentSize[0] -= 2;
                tvFontSize.setText(String.valueOf(currentSize[0]));
                prefs.edit().putInt("font_size", currentSize[0]).apply();
            }
        });

        btnIncreaseFont.setOnClickListener(v -> {
            if (currentSize[0] < 36) { // Maximum size 36
                currentSize[0] += 2;
                tvFontSize.setText(String.valueOf(currentSize[0]));
                prefs.edit().putInt("font_size", currentSize[0]).apply();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
