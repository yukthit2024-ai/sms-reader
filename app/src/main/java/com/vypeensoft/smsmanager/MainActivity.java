package com.vypeensoft.smsmanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_SMS = 101;

    private RecyclerView rvSmsList;
    private SmsAdapter smsAdapter;
    private List<SmsModel> allSmsList = new ArrayList<>();
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvEmptyState;
    private View emptyStateContainer;
    private Button btnRequestPermission;
    private RadioGroup rgViewGroup;
    private boolean isGroupView = false;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_help) {
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
            } else if (id == R.id.nav_about) {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
            drawerLayout.closeDrawers();
            return true;
        });

        initViews();
        checkPermissions();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            loadSms();
        } else {
            updateVisibility();
        }
    }

    private void initViews() {
        rvSmsList = findViewById(R.id.rvSmsList);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        btnRequestPermission = findViewById(R.id.btnRequestPermission);
        rgViewGroup = findViewById(R.id.rgViewGroup);

        rvSmsList.setLayoutManager(new LinearLayoutManager(this));
        smsAdapter = new SmsAdapter(new ArrayList<>(), sms -> {
            if (isGroupView) {
                Intent intent = new Intent(MainActivity.this, GroupedMessagesActivity.class);
                intent.putExtra("group_name", extractSenderName(sms.getSender()));
                intent.putExtra("search_query", etSearch.getText().toString().toLowerCase().trim());
                startActivity(intent);
            } else {
                Intent intent = new Intent(MainActivity.this, MessageDetailActivity.class);
                intent.putExtra("sms_data", sms);
                startActivity(intent);
            }
        });
        rvSmsList.setAdapter(smsAdapter);

        btnSearch.setOnClickListener(v -> performSearch());

        // Real-time filtering as well for better UX
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        rgViewGroup.setOnCheckedChangeListener((group, checkedId) -> {
            isGroupView = (checkedId == R.id.rbGroup);
            performSearch();
        });

        btnRequestPermission.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS) &&
                        getSharedPreferences("prefs", MODE_PRIVATE).getBoolean("permission_requested", false)) {
                    // Open Settings
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    checkPermissions();
                }
            }
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            getSharedPreferences("prefs", MODE_PRIVATE).edit().putBoolean("permission_requested", true).apply();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, PERMISSION_REQUEST_READ_SMS);
        } else {
            loadSms();
        }
    }

    private void loadSms() {
        SmsRepository.getAllSms(getContentResolver(), smsList -> {
            runOnUiThread(() -> {
                allSmsList = smsList;
                performSearch(); // Re-apply search filter if any, which also calls updateVisibility
            });
        });
    }

    private void performSearch() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        List<SmsModel> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(allSmsList);
        } else {
            for (SmsModel sms : allSmsList) {
                if (sms.getSender().toLowerCase().contains(query) || sms.getBody().toLowerCase().contains(query)) {
                    filteredList.add(sms);
                }
            }
        }

        if (isGroupView) {
            Map<String, SmsModel> latestMessages = new LinkedHashMap<>();
            for (SmsModel sms : filteredList) {
                String senderName = extractSenderName(sms.getSender());
                if (!latestMessages.containsKey(senderName)) {
                    // Since allSmsList is already sorted by date DESC, the first one encountered is the latest
                    latestMessages.put(senderName, sms);
                }
            }
            filteredList = new ArrayList<>(latestMessages.values());
        }

        smsAdapter.setShowTrimmedSender(isGroupView);
        smsAdapter.updateList(filteredList);
        updateVisibility();
    }

    public static String extractSenderName(String originalSender) {
        if (originalSender == null) return "";
        String sender = originalSender;

        // If the third character in sender name is a "-", then ignore the first three characters
        if (sender.length() >= 3 && sender.charAt(2) == '-') {
            sender = sender.substring(3);
        }

        // Then if the second last character is "-", then ignore the last two characters
        if (sender.length() >= 2 && sender.charAt(sender.length() - 2) == '-') {
            sender = sender.substring(0, sender.length() - 2);
        }

        return sender;
    }

    private void updateVisibility() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            tvEmptyState.setText("SMS permission is required to view messages.");
            btnRequestPermission.setVisibility(View.VISIBLE);
            rvSmsList.setVisibility(View.GONE);
            
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS) &&
                    getSharedPreferences("prefs", MODE_PRIVATE).getBoolean("permission_requested", false)) {
                btnRequestPermission.setText("Open Settings");
            } else {
                btnRequestPermission.setText("Grant Permission");
            }
        } else if (smsAdapter.getItemCount() == 0) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No messages found");
            btnRequestPermission.setVisibility(View.GONE);
            rvSmsList.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            rvSmsList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSms();
            } else {
                Toast.makeText(this, "Permission denied. Cannot load SMS.", Toast.LENGTH_SHORT).show();
                updateVisibility();
            }
        }
    }
}
