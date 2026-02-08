package com.example.ugvhelperadmin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AdminResultInfoActivity extends AppCompatActivity {

    EditText edtContent;
    Button btnSave, btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_result_info);

        edtContent = findViewById(R.id.edtContent);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        loadCurrent();

        btnSave.setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> delete());
    }

    private void loadCurrent() {
        FirebaseFirestore.getInstance()
                .collection("result_info")
                .document("main")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        edtContent.setText(doc.getString("content"));
                    }
                });
    }

    private void save() {
        String content = edtContent.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Content empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("isActive", true);
        data.put("updatedAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("result_info")
                .document("main") // ✅ merge/update same doc
                .set(data)
                .addOnSuccessListener(r -> Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void delete() {
        FirebaseFirestore.getInstance()
                .collection("result_info")
                .document("main")
                .delete()
                .addOnSuccessListener(r -> Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
