package com.demo.calendar;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * P0 两栏 Demo（方案 A：使用方拼装，库内零改动）：
 * 左栏 CalendarLayout（固定 320dp，only_month_view）负责选日期；
 * 右栏普通 RecyclerView 日程面板负责展示当天日程。
 *
 * 要点：
 * 1. 左栏不声明 calendar_content_view_id，CalendarLayout 完整退化为透传容器，无手势冲突；
 * 2. 联动只依赖 OnCalendarSelectListener 一个回调；
 * 3. 初始选中日不会触发回调，需主动 getSelectedCalendar() 初始化右栏；
 * 4. 日程数据与月视图 Scheme（圆点）由同一份假数据生成，保证左点右亮。
 */
public class DayPanelActivity extends AppCompatActivity {

    /** 配色对：{卡片底色(浅), 强调色(深，同时用作月视图 Scheme 圆点)} */
    private static final int[][] COLOR_PAIRS = {
            {0xFFFFE1D3, 0xFFF4753C},
            {0xFFE3F0FF, 0xFF3D7BF4},
            {0xFFE3F7E8, 0xFF34A853},
            {0xFFFFF3D6, 0xFFF0A83C},
            {0xFFF0E9FF, 0xFF8B5CF6},
            {0xFFE0F5F5, 0xFF00A5A5},
    };

    private static final String[] EVENT_TITLES = {
            "需求评审会", "团队站会", "代码走查", "产品体验走查",
            "1v1 沟通", "项目周会", "设计方案评审", "健身运动",
    };

    private CalendarView mCalendarView;
    private TextView tvDayTitle;
    private TextView tvDaySubtitle;
    private TextView tvDayEmpty;
    private RecyclerView recyclerDayEvents;
    private final DayEventAdapter mAdapter = new DayEventAdapter();

    private final SimpleDateFormat mTitleFormat =
            new SimpleDateFormat("M月d日 EEEE", Locale.CHINA);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_panel);
        setTitle(R.string.day_panel_title);

        mCalendarView = findViewById(R.id.calendarView);
        tvDayTitle = findViewById(R.id.tv_day_title);
        tvDaySubtitle = findViewById(R.id.tv_day_subtitle);
        tvDayEmpty = findViewById(R.id.tv_day_empty);
        recyclerDayEvents = findViewById(R.id.recycler_day_events);

        recyclerDayEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerDayEvents.setAdapter(mAdapter);
        mAdapter.setOnEventClickListener(new DayEventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(DayEvent event) {
                Toast.makeText(DayPanelActivity.this, event.title, Toast.LENGTH_SHORT).show();
            }
        });

        // 日期选择 → 刷新右栏
        mCalendarView.setOnCalendarSelectListener(new CalendarView.OnCalendarSelectListener() {
            @Override
            public void onCalendarOutOfRange(Calendar calendar) {
            }

            @Override
            public void onCalendarSelect(Calendar calendar, boolean isClick) {
                updateDayPanel(calendar);
            }
        });

        initSchemeData();

        // 坑位：注册监听器不会回调初始选中日，必须主动取一次
        updateDayPanel(mCalendarView.getSelectedCalendar());
    }

    /**
     * 刷新右栏：标题 + 当天日程列表
     */
    private void updateDayPanel(Calendar calendar) {
        if (calendar == null || !calendar.isAvailable()) {
            return;
        }
        Date date = new Date(calendar.getTimeInMillis());
        tvDayTitle.setText(mTitleFormat.format(date));

        List<DayEvent> events = createEventsFor(calendar.getYear(),
                calendar.getMonth(), calendar.getDay());
        mAdapter.replaceData(events);
        tvDayEmpty.setVisibility(events.isEmpty() ? TextView.VISIBLE : TextView.GONE);
        recyclerDayEvents.setVisibility(events.isEmpty() ? RecyclerView.GONE : RecyclerView.VISIBLE);
        tvDaySubtitle.setText(events.isEmpty() ? getString(R.string.day_panel_empty)
                : events.size() + " 个日程");
    }

    /**
     * 为当前年份前后共 3 年生成 Scheme（月视图圆点），
     * 圆点颜色取当天第一条日程的强调色。
     */
    private void initSchemeData() {
        int currentYear = mCalendarView.getCurYear();
        Map<String, Calendar> schemeMap = new HashMap<>();
        for (int year = currentYear - 1; year <= currentYear + 1; year++) {
            for (int month = 1; month <= 12; month++) {
                int days = getDaysInMonth(year, month);
                for (int day = 1; day <= days; day++) {
                    List<DayEvent> events = createEventsFor(year, month, day);
                    if (events.isEmpty()) {
                        continue;
                    }
                    Calendar calendar = new Calendar();
                    calendar.setYear(year);
                    calendar.setMonth(month);
                    calendar.setDay(day);
                    calendar.setSchemeColor(events.get(0).accentColor);
                    calendar.setScheme(events.get(0).title);
                    schemeMap.put(calendar.toString(), calendar);
                }
            }
        }
        mCalendarView.setSchemeDate(schemeMap);
    }

    /**
     * 确定性假数据：同一天多次调用结果一致，左右栏才能对得上。
     */
    private List<DayEvent> createEventsFor(int year, int month, int day) {
        List<DayEvent> events = new ArrayList<>();
        long seed = year * 10000L + month * 100L + day;
        int count = (int) (seed % 4); // 0 ~ 3 条
        if (count == 0) {
            return events;
        }
        Random random = new Random(seed);
        for (int i = 0; i < count; i++) {
            int[] colors = COLOR_PAIRS[random.nextInt(COLOR_PAIRS.length)];
            String title = EVENT_TITLES[random.nextInt(EVENT_TITLES.length)];
            int startHour = 8 + random.nextInt(11);
            String startMinute = random.nextBoolean() ? "00" : "30";
            int duration = 1 + random.nextInt(2);
            String time = String.format(Locale.CHINA, "%02d:%s - %02d:00",
                    startHour, startMinute, Math.min(startHour + duration, 23));
            events.add(new DayEvent(title, time, colors[0], colors[1],
                    title.substring(0, 1)));
        }
        return events;
    }

    private static int getDaysInMonth(int year, int month) {
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
