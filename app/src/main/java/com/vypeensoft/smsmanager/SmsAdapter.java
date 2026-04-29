package com.vypeensoft.smsmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.SmsViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(SmsModel sms);
    }

    private List<SmsModel> smsList;
    private OnItemClickListener listener;
    private boolean showTrimmedSender = false;

    public SmsAdapter(List<SmsModel> smsList, OnItemClickListener listener) {
        this.smsList = smsList;
        this.listener = listener;
    }

    public void setShowTrimmedSender(boolean showTrimmedSender) {
        this.showTrimmedSender = showTrimmedSender;
        // Don't call notifyDataSetChanged() here, because updateList is usually called right after
    }

    public void updateList(List<SmsModel> newList) {
        this.smsList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SmsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sms, parent, false);
        return new SmsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SmsViewHolder holder, int position) {
        SmsModel sms = smsList.get(position);
        if (showTrimmedSender) {
            holder.tvSender.setText(MainActivity.extractSenderName(sms.getSender()));
        } else {
            holder.tvSender.setText(sms.getSender());
        }
        holder.tvTimestamp.setText(sms.getTimestamp());
        holder.tvBody.setText(sms.getBody());
        
        android.content.SharedPreferences prefs = holder.itemView.getContext().getSharedPreferences("settings_prefs", android.content.Context.MODE_PRIVATE);
        int fontSize = prefs.getInt("font_size", 16);
        holder.tvSender.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, fontSize);
        holder.tvBody.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, fontSize);
        holder.tvTimestamp.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, Math.max(10, fontSize - 4));
        
        if (!sms.isRead()) {
            holder.tvSender.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.tvBody.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.tvSender.setTextColor(android.graphics.Color.parseColor("#000000"));
            holder.tvBody.setTextColor(android.graphics.Color.parseColor("#111111"));
            ((com.google.android.material.card.MaterialCardView) holder.itemView).setCardBackgroundColor(android.graphics.Color.parseColor("#E8F0FE")); // Light blue for unread
            ((com.google.android.material.card.MaterialCardView) holder.itemView).setStrokeColor(android.graphics.Color.parseColor("#A1C9F2")); // Darker blue border for unread
            ((com.google.android.material.card.MaterialCardView) holder.itemView).setStrokeWidth(3); // Slightly thicker border for unread
        } else {
            holder.tvSender.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.tvBody.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.tvSender.setTextColor(android.graphics.Color.parseColor("#333333"));
            holder.tvBody.setTextColor(android.graphics.Color.parseColor("#555555"));
            ((com.google.android.material.card.MaterialCardView) holder.itemView).setCardBackgroundColor(android.graphics.Color.WHITE); // White background for read
            ((com.google.android.material.card.MaterialCardView) holder.itemView).setStrokeColor(android.graphics.Color.parseColor("#DDDDDD")); // Default border
            ((com.google.android.material.card.MaterialCardView) holder.itemView).setStrokeWidth(1); // Default border width
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(sms);
            }
        });
    }

    @Override
    public int getItemCount() {
        return smsList != null ? smsList.size() : 0;
    }

    public static class SmsViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvTimestamp, tvBody;

        public SmsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvBody = itemView.findViewById(R.id.tvBody);
        }
    }
}
