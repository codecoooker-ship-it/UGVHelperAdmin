package com.example.ugvhelperadmin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminClubActivity extends AppCompatActivity {

    private final List<AdminClubModel> list = new ArrayList<>();
    private AdminClubAdapter adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin_club);

        RecyclerView rv = findViewById(R.id.adminRecycler);
        Button btnAdd = findViewById(R.id.btnAdd);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminClubAdapter(list, this);
        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showEditDialog(null));

        loadRealtime();
    }

    private void loadRealtime() {
        FirebaseFirestore.getInstance()
                .collection("clubs")
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    list.clear();
                    value.getDocuments().forEach(d -> {
                        AdminClubModel m = d.toObject(AdminClubModel.class);
                        if (m != null) {
                            m.id = d.getId();
                            list.add(m);
                        }
                    });

                    adapter.notifyDataSetChanged();
                });
    }

    public void showEditDialog(AdminClubModel m) {

        View v = LayoutInflater.from(this).inflate(R.layout.dialog_club, null);

        EditText edtName = v.findViewById(R.id.edtName);
        EditText edtDesc = v.findViewById(R.id.edtDesc);
        EditText edtLink = v.findViewById(R.id.edtLink);
        Switch sw = v.findViewById(R.id.switchActive);

        if (m != null) {
            edtName.setText(m.name);
            edtDesc.setText(m.description);
            edtLink.setText(m.link);
            sw.setChecked(m.isActive);
        }

        new AlertDialog.Builder(this)
                .setTitle(m == null ? "Add Club" : "Edit Club")
                .setView(v)
                .setPositiveButton("Save", (d, w) -> {

                    Map<String, Object> data = new HashMap<>();
                    data.put("name", edtName.getText().toString().trim());
                    data.put("description", edtDesc.getText().toString().trim());
                    data.put("link", edtLink.getText().toString().trim());
                    data.put("isActive", sw.isChecked());
                    data.put("updatedAt", FieldValue.serverTimestamp());

                    if (m == null) {
                        data.put("createdAt", FieldValue.serverTimestamp());
                        FirebaseFirestore.getInstance()
                                .collection("clubs")
                                .add(data);
                    } else {
                        FirebaseFirestore.getInstance()
                                .collection("clubs")
                                .document(m.id)
                                .update(data);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
