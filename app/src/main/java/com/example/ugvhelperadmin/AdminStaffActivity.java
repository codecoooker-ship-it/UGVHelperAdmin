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

public class AdminStaffActivity extends AppCompatActivity {

    private final List<AdminStaffModel> list = new ArrayList<>();
    private AdminStaffAdapter adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin_staff);

        RecyclerView rv = findViewById(R.id.adminRecycler);
        Button btnAdd = findViewById(R.id.btnAdd);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminStaffAdapter(list, this);
        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showEditDialog(null));

        loadRealtime();
    }

    private void loadRealtime() {
        FirebaseFirestore.getInstance()
                .collection("staff")
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    list.clear();

                    value.getDocuments().forEach(d -> {
                        AdminStaffModel m = new AdminStaffModel();
                        m.id = d.getId();

                        m.name = d.getString("name");
                        m.designation = d.getString("designation");

                        // ✅ FIX: phone can be Long or String
                        Object phoneObj = d.get("phone");
                        m.phone = phoneObj == null ? "" : String.valueOf(phoneObj);

                        Boolean act = d.getBoolean("isActive");
                        m.isActive = act != null && act;

                        list.add(m);
                    });

                    adapter.notifyDataSetChanged();
                });
    }

    public void showEditDialog(AdminStaffModel m) {

        View v = LayoutInflater.from(this).inflate(R.layout.dialog_staff, null);

        EditText edtName = v.findViewById(R.id.edtName);
        EditText edtDesig = v.findViewById(R.id.edtDesig);
        EditText edtPhone = v.findViewById(R.id.edtPhone);
        Switch sw = v.findViewById(R.id.swActive);

        if (m != null) {
            edtName.setText(m.name == null ? "" : m.name);
            edtDesig.setText(m.designation == null ? "" : m.designation);
            edtPhone.setText(m.phone == null ? "" : m.phone);
            sw.setChecked(m.isActive);
        }

        new AlertDialog.Builder(this)
                .setTitle(m == null ? "Add Staff" : "Edit Staff")
                .setView(v)
                .setPositiveButton("Save", (d, w) -> {

                    String name = edtName.getText().toString().trim();
                    String desig = edtDesig.getText().toString().trim();
                    String phone = edtPhone.getText().toString().trim();

                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name);
                    data.put("designation", desig);
                    data.put("phone", phone); // ✅ Always save as String
                    data.put("isActive", sw.isChecked());
                    data.put("updatedAt", FieldValue.serverTimestamp());

                    if (m == null) {
                        data.put("createdAt", FieldValue.serverTimestamp());
                        FirebaseFirestore.getInstance()
                                .collection("staff")
                                .add(data);
                    } else {
                        FirebaseFirestore.getInstance()
                                .collection("staff")
                                .document(m.id)
                                .update(data);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
