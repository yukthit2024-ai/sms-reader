package com.vypeensoft.smsmanager;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MessageDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        
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
        }
    }
}
