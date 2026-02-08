package com.example.ugvhelperadmin;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminNoticeAdapter extends RecyclerView.Adapter<AdminNoticeAdapter.VH> {

    private final List<AdminNoticeModel> list;

    public AdminNoticeAdapter(List<AdminNoticeModel> list, AdminNoticeActivity adminNoticeActivity) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.admin_notice_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminNoticeModel m = list.get(position);

        String title = (m.title == null || m.title.trim().isEmpty()) ? "(No Title)" : m.title;
        h.txtTitle.setText(title);

        // ✅ message show (text notice হলে)
        String msg = (m.message == null) ? "" : m.message;
        h.txtMsg.setText(msg);

        // ✅ type show
        String type = (m.type == null || m.type.trim().isEmpty()) ? "text" : m.type;
        h.txtType.setText("Type: " + type.toUpperCase());

        // ✅ attachment info show
        boolean hasFile = (m.fileUrl != null && !m.fileUrl.trim().isEmpty());
        h.txtAttachment.setText(hasFile ? "Attachment: Available ✅" : "Attachment: None");

        // switch listener glitch avoid
        h.switchActive.setOnCheckedChangeListener(null);
        h.switchActive.setChecked(m.isActive);

        h.switchActive.setOnCheckedChangeListener((btn, checked) -> {
            FirebaseFirestore.getInstance()
                    .collection("notices")
                    .document(m.id)
                    .update(
                            "isActive", checked,
                            "updatedAt", FieldValue.serverTimestamp()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(btn.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        h.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete?")
                    .setMessage("Are you sure you want to delete this notice?")
                    .setPositiveButton("Delete", (d, w) -> FirebaseFirestore.getInstance()
                            .collection("notices")
                            .document(m.id)
                            .delete()
                            .addOnFailureListener(e ->
                                    Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show()
                            ))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        h.btnEdit.setOnClickListener(v -> showEditDialog(v, m));
    }

    private void showEditDialog(View v, AdminNoticeModel m) {

        View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_notice, null);

        EditText edtTitle = dialogView.findViewById(R.id.edtTitle);
        EditText edtMsg = dialogView.findViewById(R.id.edtMsg);
        EditText edtPriority = dialogView.findViewById(R.id.edtPriority);

        // ✅ NEW fields
        EditText edtFileUrl = dialogView.findViewById(R.id.edtFileUrl);
        Spinner spType = dialogView.findViewById(R.id.spType);

        String[] types = {"text", "pdf", "image"};
        spType.setAdapter(new ArrayAdapter<>(v.getContext(),
                android.R.layout.simple_spinner_dropdown_item, types));

        // fill current values
        edtTitle.setText(m.title == null ? "" : m.title);
        edtMsg.setText(m.message == null ? "" : m.message);
        edtPriority.setText(String.valueOf(m.priority));

        edtFileUrl.setText(m.fileUrl == null ? "" : m.fileUrl);

        // type selection
        String currentType = (m.type == null || m.type.trim().isEmpty()) ? "text" : m.type;
        int idx = 0;
        for (int i = 0; i < types.length; i++) {
            if (types[i].equalsIgnoreCase(currentType)) { idx = i; break; }
        }
        spType.setSelection(idx);

        new AlertDialog.Builder(v.getContext())
                .setTitle("Edit Notice")
                .setView(dialogView)
                .setPositiveButton("Update", (d, which) -> {

                    String title = edtTitle.getText().toString().trim();
                    String msg = edtMsg.getText().toString().trim();
                    String type = spType.getSelectedItem().toString().trim();
                    String fileUrl = edtFileUrl.getText().toString().trim();

                    long pr = 0;
                    try {
                        pr = Long.parseLong(edtPriority.getText().toString().trim());
                    } catch (Exception ignored) {}

                    // ✅ validation
                    if ("text".equals(type) && msg.isEmpty()) {
                        Toast.makeText(v.getContext(), "Text notice হলে message দিতে হবে", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (("pdf".equals(type) || "image".equals(type)) && fileUrl.isEmpty()) {
                        Toast.makeText(v.getContext(), "PDF/Image notice হলে Drive link দিতে হবে", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("title", title);
                    data.put("message", msg);
                    data.put("type", type);
                    data.put("fileUrl", fileUrl);
                    data.put("priority", pr);
                    data.put("updatedAt", FieldValue.serverTimestamp());

                    FirebaseFirestore.getInstance()
                            .collection("notices")
                            .document(m.id)
                            .update(data)
                            .addOnFailureListener(e ->
                                    Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView txtTitle, txtMsg, txtType, txtAttachment;
        Switch switchActive;
        Button btnEdit, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtMsg = itemView.findViewById(R.id.txtMsg);

            // ✅ NEW views (admin_notice_item.xml এ add করতে হবে)
            txtType = itemView.findViewById(R.id.txtType);
            txtAttachment = itemView.findViewById(R.id.txtAttachment);

            switchActive = itemView.findViewById(R.id.switchActive);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
