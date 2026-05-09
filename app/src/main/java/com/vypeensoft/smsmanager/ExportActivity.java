package com.vypeensoft.smsmanager;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExportActivity extends AppCompatActivity {

    private EditText etExportPath;
    private CheckBox cbXml, cbJson, cbCsv;
    private SharedPreferences prefs;
    private List<SmsModel> allSmsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Export");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE);
        etExportPath = findViewById(R.id.etExportPath);
        cbXml = findViewById(R.id.cbXml);
        cbJson = findViewById(R.id.cbJson);
        cbCsv = findViewById(R.id.cbCsv);
        Button btnSaveExportPath = findViewById(R.id.btnSaveExportPath);
        Button btnStartExport = findViewById(R.id.btnStartExport);

        String defaultPath = new File(Environment.getExternalStorageDirectory(), "SMS_Reader_Exports").getAbsolutePath();
        etExportPath.setText(prefs.getString("export_path", defaultPath));

        btnSaveExportPath.setOnClickListener(v -> {
            String newPath = etExportPath.getText().toString().trim();
            if (!newPath.isEmpty()) {
                prefs.edit().putString("export_path", newPath).apply();
                Toast.makeText(this, "Export path saved", Toast.LENGTH_SHORT).show();
            }
        });

        btnStartExport.setOnClickListener(v -> {
            checkPermissionsAndExport();
        });

        loadMessages();
    }

    private void loadMessages() {
        SmsRepository.getAllSms(getContentResolver(), smsList -> {
            allSmsList = smsList;
        });
    }

    private void checkPermissionsAndExport() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 102);
        } else {
            performExport();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            performExport();
        } else if (requestCode == 102) {
            Toast.makeText(this, "Storage permission required for export", Toast.LENGTH_SHORT).show();
        }
    }

    private void performExport() {
        if (allSmsList.isEmpty()) {
            Toast.makeText(this, "No messages to export", Toast.LENGTH_SHORT).show();
            return;
        }

        String exportPath = etExportPath.getText().toString().trim();
        if (exportPath.isEmpty()) {
            Toast.makeText(this, "Please specify an export folder", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> formats = new ArrayList<>();
        if (cbXml.isChecked()) formats.add("XML");
        if (cbJson.isChecked()) formats.add("JSON");
        if (cbCsv.isChecked()) formats.add("CSV");

        if (formats.isEmpty()) {
            Toast.makeText(this, "Please select at least one format", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String format : formats) {
            SmsRepository.exportMessages(this, allSmsList, format, exportPath, () -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, format + " export completed", Toast.LENGTH_SHORT).show();
                });
            });
        }
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
