package com.vypeensoft.smsmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class GroupedMessagesActivity extends AppCompatActivity {
    private RecyclerView rvGroupedSmsList;
    private SmsAdapter adapter;
    private String groupName;
    private ActionMode actionMode;

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
                if (adapter.isSelectionMode()) {
                    toggleSelection(sms.getId());
                } else {
                    Intent intent = new Intent(GroupedMessagesActivity.this, MessageDetailActivity.class);
                    intent.putExtra("sms_data", sms);
                    startActivity(intent);
                }
            }

            @Override
            public void onDeleteClick(SmsModel sms) {
                android.content.SharedPreferences prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE);
                boolean confirmDelete = prefs.getBoolean("confirm_delete", true);

                if (confirmDelete) {
                    new androidx.appcompat.app.AlertDialog.Builder(GroupedMessagesActivity.this)
                        .setTitle("Delete Message")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            performDelete(sms);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                } else {
                    performDelete(sms);
                }
            }

            @Override
            public void onItemLongClick(SmsModel sms) {
                if (!adapter.isSelectionMode()) {
                    startSelectionMode();
                }
                toggleSelection(sms.getId());
            }

            private void performDelete(SmsModel sms) {
                SmsRepository.deleteSms(GroupedMessagesActivity.this, sms.getId(), () -> {
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(GroupedMessagesActivity.this, "Message deleted", android.widget.Toast.LENGTH_SHORT).show();
                        loadGroupedMessages();
                    });
                });
            }
        });
        rvGroupedSmsList.setAdapter(adapter);
        
        loadGroupedMessages();
    }

    private void startSelectionMode() {
        adapter.setSelectionMode(true);
        actionMode = startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_selection, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.action_select_all) {
                    adapter.selectAll();
                    updateActionModeTitle(mode);
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    deleteSelectedMessages();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                adapter.setSelectionMode(false);
                actionMode = null;
            }
        });
        updateActionModeTitle(actionMode);
    }

    private void toggleSelection(String id) {
        adapter.toggleSelection(id);
        if (adapter.getSelectedCount() == 0) {
            if (actionMode != null) {
                actionMode.finish();
            }
        } else {
            updateActionModeTitle(actionMode);
        }
    }

    private void updateActionModeTitle(ActionMode mode) {
        if (mode != null) {
            mode.setTitle(adapter.getSelectedCount() + " selected");
        }
    }

    private void deleteSelectedMessages() {
        int count = adapter.getSelectedCount();
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Messages")
            .setMessage("Are you sure you want to delete " + count + " messages?")
            .setPositiveButton("Delete", (dialog, which) -> {
                List<String> idsToDelete = new ArrayList<>(adapter.getSelectedIds());
                performBulkDelete(idsToDelete);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void performBulkDelete(List<String> ids) {
        new Thread(() -> {
            for (String id : ids) {
                // We don't use the callback here for each one, just once at the end
                SmsRepository.deleteSms(this, id, null);
            }
            runOnUiThread(() -> {
                android.widget.Toast.makeText(this, ids.size() + " messages deleted", android.widget.Toast.LENGTH_SHORT).show();
                if (actionMode != null) actionMode.finish();
                loadGroupedMessages();
            });
        }).start();
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
