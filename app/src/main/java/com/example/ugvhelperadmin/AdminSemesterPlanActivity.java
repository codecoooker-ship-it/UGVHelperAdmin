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

public class AdminSemesterPlanActivity extends AppCompatActivity {

    private static final String COLLECTION = "semester_plans";
    private static final String DOC_ID = "current";
    EditText edtTitle, edtPdf;
    Switch switchActive;
    Button btnSave, btnDisable;
    TextView txtStatus;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_semester_plan);

        db = FirebaseFirestore.getInstance();

        edtTitle = findViewById(R.id.edtTitle);
        edtPdf = findViewById(R.id.edtPdf);
        switchActive = findViewById(R.id.switchActive);
        btnSave = findViewById(R.id.btnSave);
        btnDisable = findViewById(R.id.btnDisable);
        txtStatus = findViewById(R.id.txtStatus);

        loadCurrentPlan();

        btnSave.setOnClickListener(v -> saveOrUpdate());
        btnDisable.setOnClickListener(v -> disablePlan());
    }

    // 🔹 Load existing data
    private void loadCurrentPlan() {
        db.collection(COLLECTION)
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

    // 🔹 Save / Update
    private void saveOrUpdate() {

        String title = edtTitle.getText().toString().trim();
        String pdf = edtPdf.getText().toString().trim();
        boolean isActive = switchActive.isChecked();

        if (pdf.isEmpty()) {
            txtStatus.setText("PDF link is required.");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", title.isEmpty() ? "Semester Plan" : title);
        data.put("pdfUrl", pdf);
        data.put("isActive", isActive);
        data.put("updatedAt", FieldValue.serverTimestamp());

        // first time only
        data.putIfAbsent("createdAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION)
                .document(DOC_ID)
                .set(data)
                .addOnSuccessListener(r ->
                        txtStatus.setText("Semester Plan saved successfully ✅"))
                .addOnFailureListener(e ->
                        txtStatus.setText("Error: " + e.getMessage()));
    }

    // 🔹 Disable instead of delete
    private void disablePlan() {
        db.collection(COLLECTION)
                .document(DOC_ID)
                .update("isActive", false,
                        "updatedAt", FieldValue.serverTimestamp())
                .addOnSuccessListener(r ->
                        txtStatus.setText("Semester Plan disabled ❌"))
                .addOnFailureListener(e ->
                        txtStatus.setText("Error: " + e.getMessage()));
    }
}
