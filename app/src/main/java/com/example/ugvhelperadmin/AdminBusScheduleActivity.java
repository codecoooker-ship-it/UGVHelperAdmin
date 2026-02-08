package com.example.ugvhelperadmin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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

public class AdminBusScheduleActivity extends AppCompatActivity {

    RecyclerView adminRecycler;
    View btnAdd;

    List<AdminBusScheduleModel> list = new ArrayList<>();
    AdminBusScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bus_schedule);

        adminRecycler = findViewById(R.id.adminRecycler);
        btnAdd = findViewById(R.id.btnAdd);

        adminRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminBusScheduleAdapter(list, new AdminBusScheduleAdapter.Listener() {
            @Override public void onEdit(AdminBusScheduleModel m) { showDialog(m); }
            @Override public void onDelete(AdminBusScheduleModel m) { deleteItem(m); }
        });
        adminRecycler.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showDialog(null));

        loadRealtime();
    }

    private void loadRealtime() {
        FirebaseFirestore.getInstance()
                .collection("bus_schedules")
                .orderBy("timeMinutes", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value == null) return;

                    list.clear();
                    value.getDocuments().forEach(doc -> {
                        AdminBusScheduleModel m = doc.toObject(AdminBusScheduleModel.class);
                        if (m != null) {
                            m.id = doc.getId();
                            list.add(m);
                        }
                    });
                    adapter.notifyDataSetChanged();
                });
    }

    private void showDialog(AdminBusScheduleModel edit) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_bus_schedule, null);

        EditText edtTitle = v.findViewById(R.id.edtTitle);
        EditText edtFrom = v.findViewById(R.id.edtFrom);
        EditText edtTo = v.findViewById(R.id.edtTo);
        EditText edtTime = v.findViewById(R.id.edtTime);
        EditText edtMinutes = v.findViewById(R.id.edtMinutes);
        EditText edtNote = v.findViewById(R.id.edtNote);

        if (edit != null) {
            edtTitle.setText(edit.title);
            edtFrom.setText(edit.from);
            edtTo.setText(edit.to);
            edtTime.setText(edit.timeText);
            edtMinutes.setText(String.valueOf(edit.timeMinutes));
            edtNote.setText(edit.note);
        }

        new AlertDialog.Builder(this)
                .setTitle(edit == null ? "Add Schedule" : "Edit Schedule")
                .setView(v)
                .setPositiveButton(edit == null ? "Add" : "Update", (d, w) -> {

                    String title = edtTitle.getText().toString().trim();
                    String from = edtFrom.getText().toString().trim();
                    String to = edtTo.getText().toString().trim();
                    String timeText = edtTime.getText().toString().trim();
                    String note = edtNote.getText().toString().trim();

                    long mins = 0;
                    try { mins = Long.parseLong(edtMinutes.getText().toString().trim()); }
                    catch (Exception ignored) {}

                    if (timeText.isEmpty()) {
                        Toast.makeText(this, "Time is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("title", title);
                    data.put("from", from);
                    data.put("to", to);
                    data.put("timeText", timeText);
                    data.put("timeMinutes", mins);
                    data.put("note", note);
                    data.put("isActive", true);
                    data.put("updatedAt", FieldValue.serverTimestamp());

                    if (edit == null) data.put("createdAt", FieldValue.serverTimestamp());

                    if (edit == null) {
                        FirebaseFirestore.getInstance()
                                .collection("bus_schedules")
                                .add(data)
                                .addOnSuccessListener(r -> Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        FirebaseFirestore.getInstance()
                                .collection("bus_schedules")
                                .document(edit.id)
                                .update(data)
                                .addOnSuccessListener(r -> Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteItem(AdminBusScheduleModel m) {
        FirebaseFirestore.getInstance()
                .collection("bus_schedules")
                .document(m.id)
                .delete()
                .addOnSuccessListener(r -> Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
