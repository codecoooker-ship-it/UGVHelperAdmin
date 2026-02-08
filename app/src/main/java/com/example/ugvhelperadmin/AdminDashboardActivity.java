package com.example.ugvhelperadmin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        RecyclerView rv = findViewById(R.id.rvAdminGrid);
        rv.setLayoutManager(new GridLayoutManager(this, 3));
        rv.setHasFixedSize(true);

        List<AdminGridItemModel> items = new ArrayList<>();

        // ✅ এখানে section add হবে
        items.add(new AdminGridItemModel(R.drawable.notice_menu, "Notice", AdminNoticeActivity.class));
        items.add(new AdminGridItemModel(R.drawable.teacher, "Teachers", AdminTeachersActivity.class));
        items.add(new AdminGridItemModel(R.drawable.result, "Result Info", AdminResultInfoActivity.class));
        items.add(new AdminGridItemModel(R.drawable.routine, "Routine", AdminRoutineActivity.class));
        items.add(new AdminGridItemModel(R.drawable.semester, "Semester Plan", AdminSemesterPlanActivity.class));
        items.add(new AdminGridItemModel(R.drawable.bus, "Bus Schedule", AdminBusScheduleActivity.class));
        items.add(new AdminGridItemModel(R.drawable.university, "Varsity Info", AdminVarsityInfoActivity.class));
        items.add(new AdminGridItemModel(R.drawable.club, "Club Info", AdminClubActivity.class));
        items.add(new AdminGridItemModel(R.drawable.staff, "Staff Info", AdminStaffActivity.class
        ));







        // future:
        // items.add(new AdminGridItemModel(R.drawable.xyz, "Website", AdminWebsiteActivity.class));

        rv.setAdapter(new AdminGridAdapter(items));
    }
}
