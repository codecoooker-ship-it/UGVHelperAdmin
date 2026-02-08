package com.example.ugvhelperadmin;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminTeacherAdapter extends RecyclerView.Adapter<AdminTeacherAdapter.VH> {

    private final List<AdminTeacherModel> list;
    private final TeacherActions actions;
    public AdminTeacherAdapter(List<AdminTeacherModel> list, TeacherActions actions) {
        this.list = list;
        this.actions = actions;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_admin, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminTeacherModel m = list.get(position);

        h.txtName.setText(m.name == null ? "" : m.name);
        h.txtDesig.setText(m.designation == null ? "" : m.designation);

        if (m.imageUrl != null && !m.imageUrl.trim().isEmpty()) {
            Glide.with(h.itemView.getContext()).load(m.imageUrl).into(h.imgTeacher);
        } else {
            h.imgTeacher.setImageResource(R.drawable.ic_launcher_foreground);
        }

        h.switchActive.setOnCheckedChangeListener(null);
        h.switchActive.setChecked(m.isActive);
        h.switchActive.setOnCheckedChangeListener((btn, checked) -> {
            FirebaseFirestore.getInstance().collection("teachers").document(m.id)
                    .update("isActive", checked, "updatedAt", FieldValue.serverTimestamp())
                    .addOnFailureListener(e ->
                            Toast.makeText(btn.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        h.btnEdit.setOnClickListener(v -> actions.onEdit(m));

        h.btnDelete.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle("Delete?")
                .setMessage("Teacher delete করতে চান?")
                .setPositiveButton("Delete", (d,w) ->
                        FirebaseFirestore.getInstance().collection("teachers").document(m.id)
                                .delete()
                                .addOnFailureListener(e ->
                                        Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show());
    }

    @Override public int getItemCount() { return list.size(); }

    public interface TeacherActions {
        void onEdit(AdminTeacherModel m);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgTeacher;
        TextView txtName, txtDesig;
        Switch switchActive;
        Button btnEdit, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            imgTeacher = itemView.findViewById(R.id.imgTeacher);
            txtName = itemView.findViewById(R.id.txtName);
            txtDesig = itemView.findViewById(R.id.txtDesig);
            switchActive = itemView.findViewById(R.id.switchActive);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
