package com.example.ugvhelperadmin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.VH> {

    private final List<DashboardItem> items;

    public DashboardAdapter(List<DashboardItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DashboardItem item = items.get(position);

        h.imgIcon.setImageResource(item.iconRes);
        h.txtName.setText(item.title);
        h.txtSub.setText(item.subtitle);

        h.itemView.setOnClickListener(v -> {
            Context c = v.getContext();
            c.startActivity(new Intent(c, item.targetActivity));
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView txtName, txtSub;

        VH(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtName = itemView.findViewById(R.id.txtName);
            txtSub = itemView.findViewById(R.id.txtSub);
        }
    }
}
