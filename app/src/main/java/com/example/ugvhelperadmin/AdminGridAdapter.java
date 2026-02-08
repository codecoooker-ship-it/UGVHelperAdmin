package com.example.ugvhelperadmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminGridAdapter extends RecyclerView.Adapter<AdminGridAdapter.VH> {

    private final List<AdminGridItemModel> items;

    public AdminGridAdapter(List<AdminGridItemModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_grid, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminGridItemModel m = items.get(position);

        h.txtTitle.setText(m.title);
        h.imgIcon.setImageResource(m.iconRes);

        h.circleWrap.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), m.activity);
            v.getContext().startActivity(i);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        FrameLayout circleWrap;
        ImageView imgIcon;
        TextView txtTitle;

        VH(@NonNull View itemView) {
            super(itemView);
            circleWrap = itemView.findViewById(R.id.circleWrap);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtTitle = itemView.findViewById(R.id.txtTitle);
        }
    }
}
