package com.vypeensoft.smsmanager;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MessageDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Message Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        TextView tvSender = findViewById(R.id.tvDetailSender);
        TextView tvTimestamp = findViewById(R.id.tvDetailTimestamp);
        TextView tvBody = findViewById(R.id.tvDetailBody);

        SmsModel sms = (SmsModel) getIntent().getSerializableExtra("sms_data");
        if (sms != null) {
            tvSender.setText(sms.getSender());
            tvTimestamp.setText(sms.getTimestamp());
            tvBody.setText(sms.getBody());
            
            android.content.SharedPreferences prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE);
            int fontSize = prefs.getInt("font_size", 16);
            tvSender.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, fontSize + 4);
            tvBody.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, fontSize);
            tvTimestamp.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, Math.max(10, fontSize - 2));
            
            if (!sms.isRead()) {
                SmsRepository.markSmsAsRead(this, sms.getId());
                sms.setRead(true);
            }

            android.widget.Button btnDelete = findViewById(R.id.btnDeleteDetail);
            btnDelete.setOnClickListener(v -> {
                boolean confirmDelete = prefs.getBoolean("confirm_delete", true);

                if (confirmDelete) {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
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
            });
        }
    }

    private void performDelete(SmsModel sms) {
        SmsRepository.deleteSms(this, sms.getId(), () -> {
            runOnUiThread(() -> {
                android.widget.Toast.makeText(this, "Message deleted", android.widget.Toast.LENGTH_SHORT).show();
                finish();
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
}
