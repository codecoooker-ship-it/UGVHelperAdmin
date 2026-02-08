package com.example.ugvhelperadmin;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminTeachersActivity extends AppCompatActivity {

    private final List<AdminTeacherModel> list = new ArrayList<>();
    private AdminTeacherAdapter adapter;

    private Uri pickedImageUri = null;
    private ImageView dialogImgPreview;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pickedImageUri = uri;
                    if (dialogImgPreview != null) dialogImgPreview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_teachers);

        RecyclerView rv = findViewById(R.id.teacherRecycler);
        Button btnAdd = findViewById(R.id.btnAddTeacher);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminTeacherAdapter(list, this::showEditDialog);
        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showAddDialog());

        loadTeachersRealtime();
    }

    private void loadTeachersRealtime() {
        FirebaseFirestore.getInstance().collection("teachers")
                .orderBy("order", Query.Direction.ASCENDING)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value == null) return;
                    list.clear();
                    value.getDocuments().forEach(doc -> {
                        AdminTeacherModel m = doc.toObject(AdminTeacherModel.class);
                        if (m != null) {
                            m.id = doc.getId();
                            list.add(m);
                        }
                    });
                    adapter.notifyDataSetChanged();
                });
    }

    private void showAddDialog() {
        showTeacherDialog(null);
    }

    private void showEditDialog(AdminTeacherModel m) {
        showTeacherDialog(m);
    }

    private void showTeacherDialog(AdminTeacherModel m) {
        pickedImageUri = null;

        View dv = LayoutInflater.from(this).inflate(R.layout.dialog_teacher, null);
        dialogImgPreview = dv.findViewById(R.id.imgPick);

        Button btnPick = dv.findViewById(R.id.btnPickImage);
        EditText edtName = dv.findViewById(R.id.edtName);
        EditText edtDesig = dv.findViewById(R.id.edtDesig);
        EditText edtDept = dv.findViewById(R.id.edtDept);
        EditText edtPhone = dv.findViewById(R.id.edtPhone);
        EditText edtEmail = dv.findViewById(R.id.edtEmail);
        EditText edtBio = dv.findViewById(R.id.edtBio);
        EditText edtOrder = dv.findViewById(R.id.edtOrder);

        if (m != null) {
            edtName.setText(m.name);
            edtDesig.setText(m.designation);
            edtDept.setText(m.department);
            edtPhone.setText(m.phone);
            edtEmail.setText(m.email);
            edtBio.setText(m.bio);
            edtOrder.setText(String.valueOf(m.order));

            if (m.imageUrl != null && !m.imageUrl.isEmpty()) {
                Glide.with(this).load(m.imageUrl).into(dialogImgPreview);
            }
        }

        btnPick.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        String title = (m == null) ? "Add Teacher" : "Edit Teacher";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dv)
                .setPositiveButton((m == null) ? "Add" : "Update", (d, w) -> {
                    String name = edtName.getText().toString().trim();
                    String desig = edtDesig.getText().toString().trim();
                    String dept = edtDept.getText().toString().trim();
                    String phone = edtPhone.getText().toString().trim();
                    String email = edtEmail.getText().toString().trim();
                    String bio = edtBio.getText().toString().trim();

                    long order = 0;
                    try { order = Long.parseLong(edtOrder.getText().toString().trim()); }
                    catch (Exception ignored) {}

                    if (name.isEmpty() || desig.isEmpty()) {
                        Toast.makeText(this, "Name & Designation required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (m == null) createTeacher(name, desig, dept, phone, email, bio, order);
                    else updateTeacher(m.id, m.imageUrl, name, desig, dept, phone, email, bio, order);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createTeacher(String name, String desig, String dept, String phone, String email, String bio, long order) {

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("designation", desig);
        data.put("department", dept);
        data.put("phone", phone);
        data.put("email", email);
        data.put("bio", bio);
        data.put("order", order);
        data.put("isActive", true);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("teachers")
                .add(data)
                .addOnSuccessListener(docRef -> {
                    if (pickedImageUri != null) {
                        uploadTeacherImage(docRef.getId(), pickedImageUri, imageUrl ->
                                docRef.update("imageUrl", imageUrl, "updatedAt", FieldValue.serverTimestamp())
                        );
                    }
                    Toast.makeText(this, "Teacher added", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateTeacher(String id, String oldImageUrl, String name, String desig, String dept,
                               String phone, String email, String bio, long order) {

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("designation", desig);
        data.put("department", dept);
        data.put("phone", phone);
        data.put("email", email);
        data.put("bio", bio);
        data.put("order", order);
        data.put("updatedAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("teachers").document(id)
                .update(data)
                .addOnSuccessListener(r -> {
                    if (pickedImageUri != null) {
                        uploadTeacherImage(id, pickedImageUri, imageUrl ->
                                FirebaseFirestore.getInstance().collection("teachers").document(id)
                                        .update("imageUrl", imageUrl, "updatedAt", FieldValue.serverTimestamp())
                        );
                    }
                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void uploadTeacherImage(String docId, Uri uri, ImageUploadCallback cb) {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference().child("teachers/" + docId + ".jpg");

        ref.putFile(uri)
                .continueWithTask(t -> ref.getDownloadUrl())
                .addOnSuccessListener(downloadUri -> cb.onDone(downloadUri.toString()))
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private interface ImageUploadCallback {
        void onDone(String downloadUrl);
    }
}
