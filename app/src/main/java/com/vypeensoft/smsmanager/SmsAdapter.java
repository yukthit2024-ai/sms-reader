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

    private List<SmsModel> smsList;

    public SmsAdapter(List<SmsModel> smsList) {
        this.smsList = smsList;
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
        holder.tvSender.setText(sms.getSender());
        holder.tvTimestamp.setText(sms.getTimestamp());
        holder.tvBody.setText(sms.getBody());
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
