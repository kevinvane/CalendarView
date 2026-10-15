package com.demo.calendar;

import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.demo.calendar.view.TimeGridView;
import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarView;

import java.util.ArrayList;
import java.util.List;

/**
 * 时间网格周视图 Demo（对标竞品）：
 * - CalendarView（only_week_view + ScheduleWeekBar）当周选择器；
 * - TimeGridView 展示 7 天时间网格，事件块按分钟定位；
 * - 联动复用库的两个回调：OnWeekChangeListener（翻周）+ OnCalendarSelectListener（选日）。
 */
public class TimeGridActivity extends AppCompatActivity {

    private CalendarView mCalendarView;
    private TimeGridView mTimeGrid;
    private ScrollView mScrollView;
    private final List<Calendar> mCurrentWeek = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_grid);
        setTitle(R.string.time_grid_title);

        mCalendarView = findViewById(R.id.calendarView);
        mTimeGrid = findViewById(R.id.timeGridView);
        mScrollView = findViewById(R.id.scroll_time_grid);

        mTimeGrid.setOnTimeSlotClickListener(new TimeGridView.OnTimeSlotClickListener() {
            @Override
            public void onTimeSlotClick(Calendar day, TimeSlotEvent event) {
                Toast.makeText(TimeGridActivity.this,
                        day.getMonth() + "月" + day.getDay() + "日 " + event.title,
                        Toast.LENGTH_SHORT).show();
            }
        });

        // 翻周 → 刷新时间网格 + 表头
        mCalendarView.setOnWeekChangeListener(new CalendarView.OnWeekChangeListener() {
            @Override
            public void onWeekChange(List<Calendar> weekCalendars) {
                updateWeek(weekCalendars);
            }
        });

        // 点日格 → 高亮对应列
        mCalendarView.setOnCalendarSelectListener(new CalendarView.OnCalendarSelectListener() {
            @Override
            public void onCalendarOutOfRange(Calendar calendar) {
            }

            @Override
            public void onCalendarSelect(Calendar calendar, boolean isClick) {
                updateSelectedColumn(calendar);
            }
        });

        // 初始周数据不会触发 OnWeekChangeListener，主动拉一次
        updateWeek(mCalendarView.getCurrentWeekCalendars());
        mTimeGrid.scrollToNow(mScrollView);
    }

    private void updateWeek(List<Calendar> weekCalendars) {
        if (weekCalendars == null || weekCalendars.size() != 7) {
            return;
        }
        mCurrentWeek.clear();
        mCurrentWeek.addAll(weekCalendars);

        List<List<TimeSlotEvent>> events = new ArrayList<>(7);
        for (Calendar day : weekCalendars) {
            events.add(TimeSlotEvent.createFor(day.getYear(), day.getMonth(), day.getDay()));
        }
        mTimeGrid.setWeek(weekCalendars, events);
        updateSelectedColumn(mCalendarView.getSelectedCalendar());
    }

    private void updateSelectedColumn(Calendar calendar) {
        if (calendar == null) {
            return;
        }
        for (int i = 0; i < mCurrentWeek.size(); i++) {
            Calendar day = mCurrentWeek.get(i);
            if (day.getYear() == calendar.getYear()
                    && day.getMonth() == calendar.getMonth()
                    && day.getDay() == calendar.getDay()) {
                mTimeGrid.setSelectedDayIndex(i);
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTimeGrid.scrollToNow(mScrollView);
    }
}
