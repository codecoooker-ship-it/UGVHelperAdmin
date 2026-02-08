package com.example.ugvhelperadmin;

public class AdminGridItemModel {
    public int iconRes;
    public String title;
    public Class<?> activity;

    public AdminGridItemModel(int iconRes, String title, Class<?> activity) {
        this.iconRes = iconRes;
        this.title = title;
        this.activity = activity;
    }
}
