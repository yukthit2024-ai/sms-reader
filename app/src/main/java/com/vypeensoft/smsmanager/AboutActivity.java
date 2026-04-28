package com.vypeensoft.smsmanager;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("About");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView tvBuildDetails = findViewById(R.id.tvBuildDetails);
        String details = "Build Timestamp: " + BuildConfig.BUILD_TIMESTAMP + "\n" +
                         "Git SHA: " + BuildConfig.GIT_SHA + "\n" +
                         "Git SHA Full: " + BuildConfig.GIT_SHA_FULL + "\n" +
                         "Git Tag: " + BuildConfig.GIT_TAG;
        tvBuildDetails.setText(details);
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
