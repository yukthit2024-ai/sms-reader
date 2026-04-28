package com.vypeensoft.smsmanager;

import android.content.Intent;
import android.os.Bundle;
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
        
        rvGroupedSmsList = findViewById(R.id.rvGroupedSmsList);
        rvGroupedSmsList.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new SmsAdapter(new ArrayList<>(), sms -> {
            Intent intent = new Intent(GroupedMessagesActivity.this, MessageDetailActivity.class);
            intent.putExtra("sms_data", sms);
            startActivity(intent);
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
}
