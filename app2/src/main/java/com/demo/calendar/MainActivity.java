package com.demo.calendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarView;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private CalendarView mCalendarView;
    private Random mRandom = new Random();

    private static final String[] SCHEME_TEXTS = {"会议", "出差", "放假", "备忘", "生日", "节日", "运动", "学习"};
    private static final int[] SCHEME_COLORS = {
            0xFF304FFE, 0xFFE91E63, 0xFF4CAF50, 0xFFFF9800,
            0xFF9C27B0, 0xFF00BCD4, 0xFFF44336, 0xFF607D8B
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_custom_only_week);
        mCalendarView = findViewById(R.id.calendarView);
        initSchemeData();

        // P0 日视图（两栏）Demo 入口
        Button btnDayPanel = findViewById(R.id.btn_day_panel);
        btnDayPanel.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                startActivity(new Intent(MainActivity.this, DayPanelActivity.class));
            }
        });

        // 时间网格周视图 Demo 入口
        Button btnTimeGrid = findViewById(R.id.btn_time_grid);
        btnTimeGrid.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                startActivity(new Intent(MainActivity.this, TimeGridActivity.class));
            }
        });
    }

    private void initSchemeData() {
        int year = mCalendarView.getCurYear();
        int month = mCalendarView.getCurMonth();
        int daysInMonth = getDaysInMonth(year, month);

        Map<String, Calendar> map = new HashMap<>();
        // Add schemes to random days in current month
        for (int day = 1; day <= daysInMonth; day++) {
            if (mRandom.nextFloat() < 0.3f) {
                map.put(getCalendarKey(year, month, day),
                        getRandomSchemeCalendar(year, month, day));
            }
        }
        mCalendarView.setSchemeDate(map);
    }

    private Calendar getRandomSchemeCalendar(int year, int month, int day) {
        Calendar calendar = new Calendar();
        calendar.setYear(year);
        calendar.setMonth(month);
        calendar.setDay(day);
        int primaryColor = SCHEME_COLORS[mRandom.nextInt(SCHEME_COLORS.length)];
        calendar.setSchemeColor(primaryColor);
        calendar.setScheme(SCHEME_TEXTS[mRandom.nextInt(SCHEME_TEXTS.length)]);

        int schemeCount = mRandom.nextInt(3) + 1;
        for (int i = 0; i < schemeCount; i++) {
            int color = SCHEME_COLORS[mRandom.nextInt(SCHEME_COLORS.length)];
            String text = SCHEME_TEXTS[mRandom.nextInt(SCHEME_TEXTS.length)];
            calendar.addScheme(color, text);
        }
        return calendar;
    }

    private String getCalendarKey(int year, int month, int day) {
        return year + "" + (month < 10 ? "0" + month : month) + "" + (day < 10 ? "0" + day : day);
    }

    private int getDaysInMonth(int year, int month) {
        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                return 31;
            case 4: case 6: case 9: case 11:
                return 30;
            case 2:
                return (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28;
            default:
                return 30;
        }
    }
}