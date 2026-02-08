package com.example.ugvhelperadmin;

import com.google.firebase.Timestamp;

public class AdminNoticeModel {
    public String id;

    public String title;
    public String message;

    // ✅ NEW
    public String type;     // "text" / "pdf" / "image"
    public String fileUrl;  // drive share link

    public long priority;
    public boolean isActive;

    public Timestamp createdAt;
    public Timestamp updatedAt;

    public AdminNoticeModel() {}
}
