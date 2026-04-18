package com.vypeensoft.smsmanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_SMS = 101;

    private RecyclerView rvSmsList;
    private SmsAdapter smsAdapter;
    private List<SmsModel> allSmsList = new ArrayList<>();
    private EditText etSearch;
    private Button btnSearch;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkPermissions();
    }

    private void initViews() {
        rvSmsList = findViewById(R.id.rvSmsList);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvSmsList.setLayoutManager(new LinearLayoutManager(this));
        smsAdapter = new SmsAdapter(new ArrayList<>());
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
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, PERMISSION_REQUEST_READ_SMS);
        } else {
            loadSms();
        }
    }

    private void loadSms() {
        SmsRepository.getAllSms(getContentResolver(), smsList -> {
            runOnUiThread(() -> {
                allSmsList = smsList;
                smsAdapter.updateList(allSmsList);
                updateVisibility();
            });
        });
    }

    private void performSearch() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        List<SmsModel> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList = allSmsList;
        } else {
            for (SmsModel sms : allSmsList) {
                if (sms.getSender().toLowerCase().contains(query) || sms.getBody().toLowerCase().contains(query)) {
                    filteredList.add(sms);
                }
            }
        }

        smsAdapter.updateList(filteredList);
        updateVisibility();
    }

    private void updateVisibility() {
        if (smsAdapter.getItemCount() == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvSmsList.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
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
                Toast.makeText(this, "Permission denied. Cannot load SMS.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
