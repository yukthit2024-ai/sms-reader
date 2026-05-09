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

        org.json.JSONObject settings = SettingsManager.loadSettings(this);
        etExportPath = findViewById(R.id.etExportPath);
        cbXml = findViewById(R.id.cbXml);
        cbJson = findViewById(R.id.cbJson);
        cbCsv = findViewById(R.id.cbCsv);
        Button btnSaveExportPath = findViewById(R.id.btnSaveExportPath);
        Button btnStartExport = findViewById(R.id.btnStartExport);

        etExportPath.setText(settings.optString("export_path", SettingsManager.getDefaultExportPath()));
        cbXml.setChecked(settings.optBoolean("export_format_xml", true));
        cbJson.setChecked(settings.optBoolean("export_format_json", true));
        cbCsv.setChecked(settings.optBoolean("export_format_csv", true));

        btnSaveExportPath.setOnClickListener(v -> {
            String newPath = etExportPath.getText().toString().trim();
            if (!newPath.isEmpty()) {
                try {
                    settings.put("export_path", newPath);
                    SettingsManager.saveSettings(this, settings);
                    Toast.makeText(this, "Export path saved", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {}
            }
        });

        btnStartExport.setOnClickListener(v -> {
            // Save checkbox states before exporting
            try {
                settings.put("export_format_xml", cbXml.isChecked());
                settings.put("export_format_json", cbJson.isChecked());
                settings.put("export_format_csv", cbCsv.isChecked());
                SettingsManager.saveSettings(this, settings);
            } catch (Exception e) {}
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
        boolean hasStorage = true;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            hasStorage = android.os.Environment.isExternalStorageManager();
        } else {
            hasStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        if (!hasStorage) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                try {
                    android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(android.net.Uri.parse(String.format("package:%s", getPackageName())));
                    startActivity(intent);
                } catch (Exception e) {
                    android.content.Intent intent = new android.content.Intent();
                    intent.setAction(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 102);
            }
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
