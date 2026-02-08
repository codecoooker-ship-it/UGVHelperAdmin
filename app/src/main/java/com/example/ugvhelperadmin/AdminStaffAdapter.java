package com.example.ugvhelperadmin;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminStaffAdapter extends RecyclerView.Adapter<AdminStaffAdapter.VH> {

    private final List<AdminStaffModel> list;
    private final AdminStaffActivity activity;

    public AdminStaffAdapter(List<AdminStaffModel> list, AdminStaffActivity activity) {
        this.list = list;
        this.activity = activity;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AdminStaffModel m = list.get(pos);

        h.title.setText(m.name);
        h.sub.setText(m.designation + " | " + (m.isActive ? "Active" : "Inactive"));

        h.itemView.setOnClickListener(v -> activity.showEditDialog(m));

        h.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Staff?")
                    .setMessage(m.name)
                    .setPositiveButton("Delete", (d, w) ->
                            FirebaseFirestore.getInstance()
                                    .collection("staff")
                                    .document(m.id)
                                    .delete())
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, sub;
        VH(View v) {
            super(v);
            title = v.findViewById(android.R.id.text1);
            sub = v.findViewById(android.R.id.text2);
        }
    }
}
