package com.example.ugvhelperadmin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AdminRoutineActivity extends AppCompatActivity {

    private final String[] depts = {
            "CSE","BBA","EEE","ME","Civil","English",
            "MSc in CSE","MPH","MA in English","MBA","EMBA"
    };
    private final String[] semesters = {"1","2","3","4","5","6","7","8"};
    private final String[] sections = {"A","B","C"};
    private Spinner spDept, spSemester, spSection;
    private EditText edtTitle, edtPdf;
    private Button btnLoad, btnSave, btnDelete;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_routine);

        db = FirebaseFirestore.getInstance();

        spDept = findViewById(R.id.spDept);
        spSemester = findViewById(R.id.spSemester);
        spSection = findViewById(R.id.spSection);

        edtTitle = findViewById(R.id.edtTitle);
        edtPdf = findViewById(R.id.edtPdf);

        btnLoad = findViewById(R.id.btn_Load);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        setupSpinners();

        btnLoad.setOnClickListener(v -> loadExisting());
        btnSave.setOnClickListener(v -> saveRoutine());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void setupSpinners() {
        spDept.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, depts));

        spSemester.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, semesters));

        spSection.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, sections));
    }

    private String getDocId() {
        String dept = spDept.getSelectedItem().toString();
        int sem = Integer.parseInt(spSemester.getSelectedItem().toString());
        String sec = spSection.getSelectedItem().toString();
        return dept + "_" + sem + "_" + sec;
    }

    private void loadExisting() {
        String docId = getDocId();

        db.collection("routines")
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "No routine found for this selection.", Toast.LENGTH_SHORT).show();
                        edtTitle.setText("");
                        edtPdf.setText("");
                        return;
                    }

                    edtTitle.setText(doc.getString("title"));
                    edtPdf.setText(doc.getString("pdfUrl"));
                    Toast.makeText(this, "Loaded. Now you can edit & save.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void saveRoutine() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Admin not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String dept = spDept.getSelectedItem().toString();
        int sem = Integer.parseInt(spSemester.getSelectedItem().toString());
        String sec = spSection.getSelectedItem().toString();

        String title = edtTitle.getText().toString().trim();
        String url = edtPdf.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (url.isEmpty()) {
            Toast.makeText(this, "PDF link empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Optional: Convert drive link to preview link (better for viewing)
        url = normalizeDriveLink(url);

        String docId = getDocId();

        Map<String, Object> data = new HashMap<>();
        data.put("departmentId", dept);
        data.put("semester", sem);
        data.put("section", sec);
        data.put("title", title);
        data.put("pdfUrl", url);
        data.put("isActive", true);
        data.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("routines")
                .document(docId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(r ->
                        Toast.makeText(this, "✅ Routine Saved/Updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Save error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Routine?")
                .setMessage("Are you sure you want to delete this routine?")
                .setPositiveButton("Delete", (d, w) -> deleteRoutine())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteRoutine() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Admin not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String docId = getDocId();

        db.collection("routines")
                .document(docId)
                .delete()
                .addOnSuccessListener(r -> {
                    Toast.makeText(this, "✅ Deleted", Toast.LENGTH_SHORT).show();
                    edtTitle.setText("");
                    edtPdf.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // ✅ Converts Google Drive share link to preview link
    private String normalizeDriveLink(String url) {
        // Example:
        // https://drive.google.com/file/d/FILE_ID/view?usp=sharing
        // -> https://drive.google.com/file/d/FILE_ID/preview
        try {
            if (url.contains("drive.google.com/file/d/") && url.contains("/view")) {
                return url.replace("/view", "/preview");
            }
        } catch (Exception ignored) {}
        return url;
    }
}
