package com.vypeensoft.smsmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class GroupedMessagesActivity extends AppCompatActivity {
    private RecyclerView rvGroupedSmsList;
    private SmsAdapter adapter;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grouped_messages);
        
        groupName = getIntent().getStringExtra("group_name");
        setTitle(groupName != null ? groupName : "Messages");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        rvGroupedSmsList = findViewById(R.id.rvGroupedSmsList);
        rvGroupedSmsList.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new SmsAdapter(new ArrayList<>(), new SmsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SmsModel sms) {
                Intent intent = new Intent(GroupedMessagesActivity.this, MessageDetailActivity.class);
                intent.putExtra("sms_data", sms);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(SmsModel sms) {
                new androidx.appcompat.app.AlertDialog.Builder(GroupedMessagesActivity.this)
                    .setTitle("Delete Message")
                    .setMessage("Are you sure you want to delete this message?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        SmsRepository.deleteSms(GroupedMessagesActivity.this, sms.getId(), () -> {
                            runOnUiThread(() -> {
                                android.widget.Toast.makeText(GroupedMessagesActivity.this, "Message deleted", android.widget.Toast.LENGTH_SHORT).show();
                                loadGroupedMessages();
                            });
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
        rvGroupedSmsList.setAdapter(adapter);
        
        loadGroupedMessages();
    }
    
    private void loadGroupedMessages() {
        String searchQuery = getIntent().getStringExtra("search_query");
        if (searchQuery == null) searchQuery = "";
        
        final String finalSearchQuery = searchQuery;

        SmsRepository.getAllSms(getContentResolver(), smsList -> {
            List<SmsModel> filtered = new ArrayList<>();
            for (SmsModel sms : smsList) {
                if (MainActivity.extractSenderName(sms.getSender()).equals(groupName)) {
                    if (finalSearchQuery.isEmpty() || 
                        sms.getSender().toLowerCase().contains(finalSearchQuery) || 
                        sms.getBody().toLowerCase().contains(finalSearchQuery)) {
                        filtered.add(sms);
                    }
                }
            }
            runOnUiThread(() -> {
                adapter.updateList(filtered);
            });
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

    @Override
    protected void onResume() {
        super.onResume();
        loadGroupedMessages();
    }
}
