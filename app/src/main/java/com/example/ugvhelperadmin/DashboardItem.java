package com.example.ugvhelperadmin;

public class DashboardItem {
    public int iconRes;
    public String title;
    public String subtitle;
    public Class<?> targetActivity;

    public DashboardItem(int iconRes, String title, String subtitle, Class<?> targetActivity) {
        this.iconRes = iconRes;
        this.title = title;
        this.subtitle = subtitle;
        this.targetActivity = targetActivity;
    }
}
