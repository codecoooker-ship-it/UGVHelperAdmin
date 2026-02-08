package com.example.ugvhelperadmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminBusScheduleAdapter extends RecyclerView.Adapter<AdminBusScheduleAdapter.VH> {

    private final List<AdminBusScheduleModel> list;
    private final Listener listener;
    public AdminBusScheduleAdapter(List<AdminBusScheduleModel> list, Listener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_bus, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminBusScheduleModel m = list.get(position);

        String top = (m.timeText == null ? "" : m.timeText) + " • " + (m.title == null ? "" : m.title);
        String sub = (m.from == null ? "" : m.from) + " → " + (m.to == null ? "" : m.to);

        h.txtTop.setText(top);
        h.txtSub.setText(sub);

        h.btnEdit.setOnClickListener(v -> listener.onEdit(m));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(m));
    }

    @Override public int getItemCount() { return list.size(); }

    public interface Listener {
        void onEdit(AdminBusScheduleModel m);
        void onDelete(AdminBusScheduleModel m);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtTop, txtSub;
        Button btnEdit, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            txtTop = itemView.findViewById(R.id.txtTop);
            txtSub = itemView.findViewById(R.id.txtSub);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
