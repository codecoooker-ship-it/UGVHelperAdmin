package com.example.ugvhelperadmin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminVarsityInfoActivity extends AppCompatActivity {

    private static final String COLLECTION = "varsity_information";
    private static final String DOC_ID = "current";
    private EditText edtTitle, edtPdf;
    private Switch switchActive;
    private Button btnSave, btnDisable;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_varsity_info);

        edtTitle = findViewById(R.id.edtTitle);
        edtPdf = findViewById(R.id.edtPdf);
        switchActive = findViewById(R.id.switchActive);
        btnSave = findViewById(R.id.btnSave);
        btnDisable = findViewById(R.id.btnDisable);
        txtStatus = findViewById(R.id.txtStatus);

        loadCurrent();

        btnSave.setOnClickListener(v -> saveOrUpdate());
        btnDisable.setOnClickListener(v -> disable());
    }

    private void loadCurrent() {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .document(DOC_ID)
                .get()
                .addOnSuccessListener(d -> {
                    if (!d.exists()) return;

                    edtTitle.setText(d.getString("title"));
                    edtPdf.setText(d.getString("pdfUrl"));

                    Boolean active = d.getBoolean("isActive");
                    switchActive.setChecked(active != null && active);
                });
    }

    private void saveOrUpdate() {

        String title = edtTitle.getText().toString().trim();
        String pdf = edtPdf.getText().toString().trim();
        boolean isActive = switchActive.isChecked();

        if (pdf.isEmpty()) {
            txtStatus.setText("PDF link is required.");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", title.isEmpty() ? "Varsity Information" : title);
        data.put("pdfUrl", pdf);
        data.put("isActive", isActive);
        data.put("updatedAt", FieldValue.serverTimestamp());

        // createdAt only first time
        data.putIfAbsent("createdAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .document(DOC_ID)
                .set(data)
                .addOnSuccessListener(r -> txtStatus.setText("Saved ✅"))
                .addOnFailureListener(e -> txtStatus.setText("Error: " + e.getMessage()));
    }

    private void disable() {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .document(DOC_ID)
                .update("isActive", false,
                        "updatedAt", FieldValue.serverTimestamp())
                .addOnSuccessListener(r -> txtStatus.setText("Disabled ❌"))
                .addOnFailureListener(e -> txtStatus.setText("Error: " + e.getMessage()));
    }
}
