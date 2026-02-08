package com.example.ugvhelperadmin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminNoticeActivity extends AppCompatActivity {

    RecyclerView adminRecycler;
    View btnAdd;

    List<AdminNoticeModel> list = new ArrayList<>();
    AdminNoticeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notice);

        adminRecycler = findViewById(R.id.adminRecycler);
        btnAdd = findViewById(R.id.btnAdd);

        adminRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminNoticeAdapter(list, this); // ✅ pass context if you need dialog from adapter
        adminRecycler.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showAddOrEditDialog(null));

        loadNoticesRealtime();
    }

    private void loadNoticesRealtime() {
        FirebaseFirestore.getInstance()
                .collection("notices")
                .orderBy("priority", Query.Direction.DESCENDING)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Load Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (value == null) return;

                    list.clear();
                    value.getDocuments().forEach(doc -> {
                        AdminNoticeModel m = doc.toObject(AdminNoticeModel.class);
                        if (m != null) {
                            m.id = doc.getId();
                            list.add(m);
                        }
                    });

                    adapter.notifyDataSetChanged();
                });
    }

    // ✅ Add/Edit same dialog
    public void showAddOrEditDialog(AdminNoticeModel editModel) {

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notice, null);

        EditText edtTitle = dialogView.findViewById(R.id.edtTitle);
        EditText edtMsg = dialogView.findViewById(R.id.edtMsg);
        EditText edtPriority = dialogView.findViewById(R.id.edtPriority);

        EditText edtFileUrl = dialogView.findViewById(R.id.edtFileUrl);
        android.widget.Spinner spType = dialogView.findViewById(R.id.spType);

        String[] types = {"text", "pdf", "image"};
        spType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));

        // ✅ if editing: fill previous data
        if (editModel != null) {
            edtTitle.setText(editModel.title == null ? "" : editModel.title);
            edtMsg.setText(editModel.message == null ? "" : editModel.message);
            edtFileUrl.setText(editModel.fileUrl == null ? "" : editModel.fileUrl);

            if (editModel.priority != 0) edtPriority.setText(String.valueOf(editModel.priority));

            // type selection
            String t = editModel.type == null ? "text" : editModel.type;
            int idx = 0;
            for (int i = 0; i < types.length; i++) {
                if (types[i].equalsIgnoreCase(t)) { idx = i; break; }
            }
            spType.setSelection(idx);
        }

        new AlertDialog.Builder(this)
                .setTitle(editModel == null ? "Add Notice" : "Edit Notice")
                .setView(dialogView)
                .setPositiveButton(editModel == null ? "Add" : "Update", (d, which) -> {

                    String title = edtTitle.getText().toString().trim();
                    String msg = edtMsg.getText().toString().trim();
                    String fileUrl = edtFileUrl.getText().toString().trim();
                    String type = spType.getSelectedItem().toString().trim();

                    long pr = 0;
                    try {
                        pr = Long.parseLong(edtPriority.getText().toString().trim());
                    } catch (Exception ignored) {}

                    // ✅ Validation rules
                    // text -> msg required
                    if ("text".equals(type) && msg.isEmpty()) {
                        Toast.makeText(this, "Text notice হলে message দিতে হবে", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // pdf/image -> fileUrl required
                    if (("pdf".equals(type) || "image".equals(type)) && fileUrl.isEmpty()) {
                        Toast.makeText(this, "PDF/Image notice হলে Drive link দিতে হবে", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("title", title);
                    data.put("message", msg);
                    data.put("type", type);
                    data.put("fileUrl", fileUrl);

                    data.put("priority", pr);
                    data.put("isActive", true);

                    // ✅ timestamps
                    data.put("updatedAt", FieldValue.serverTimestamp());
                    if (editModel == null) data.put("createdAt", FieldValue.serverTimestamp());

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    if (editModel == null) {
                        db.collection("notices")
                                .add(data)
                                .addOnSuccessListener(r -> Toast.makeText(this, "Notice added", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        db.collection("notices")
                                .document(editModel.id)
                                .update(data)
                                .addOnSuccessListener(r -> Toast.makeText(this, "Notice updated", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
