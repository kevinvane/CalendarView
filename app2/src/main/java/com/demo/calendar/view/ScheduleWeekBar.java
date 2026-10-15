package com.demo.calendar.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.WeekBar;

import java.util.List;

/**
 * 时间网格周视图表头（对标竞品）："Sun 24" 两行式单元格，
 * 选中日期显示红色圆圈白字。
 * WeekBar 无法感知当前周日期，数字部分由 Activity 通过 updateWeek() 驱动；
 * 高亮部分由 onDateSelected() 自行维护。
 */
public class ScheduleWeekBar extends WeekBar {

    private static final String[] WEEK_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final int COLOR_TEXT = 0xFF26262B;
    private static final int COLOR_TEXT_LIGHT = 0xFF9A9AA0;
    private static final int COLOR_ACCENT = 0xFFF45B3F;

    private final TextView[] mWeekdays = new TextView[7];
    private final TextView[] mNumbers = new TextView[7];

    public ScheduleWeekBar(Context context) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);
        // 与 TimeGridView 的时刻刻度栏（GUTTER_DP = 52dp）、calendar_padding=52dp 三处保持一致，
        // 表头 / 周选择器选中框 / 时间网格列三者才能对齐
        setPadding(dp(52), 0, 0, 0);
        for (int i = 0; i < 7; i++) {
            LinearLayout cell = new LinearLayout(context);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));

            mWeekdays[i] = buildLabel(context, 12, false);
            mNumbers[i] = buildLabel(context, 17, true);
            cell.addView(mWeekdays[i]);
            cell.addView(mNumbers[i]);
            addView(cell);
        }
    }

    private TextView buildLabel(Context context, float sizeSp, boolean number) {
        TextView view = new TextView(context);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        view.getPaint().setFakeBoldText(number);
        if (number) {
            LayoutParams params = new LayoutParams(dp(30), dp(30));
            params.topMargin = dp(2);
            view.setLayoutParams(params);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(Color.TRANSPARENT);
            view.setBackground(bg);
        } else {
            view.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }
        return view;
    }

    /** Activity 驱动：写入当前周 7 天的日期数字 */
    public void updateWeek(List<Calendar> days) {
        if (days == null || days.size() != 7) {
            return;
        }
        for (int i = 0; i < 7; i++) {
            Calendar calendar = days.get(i);
            mWeekdays[i].setText(WEEK_NAMES[i]);
            mNumbers[i].setText(String.valueOf(calendar.getDay()));
        }
    }

    /** Activity 驱动：高亮选中列 */
    public void updateSelectedIndex(int index) {
        for (int i = 0; i < 7; i++) {
            boolean selected = i == index;
            GradientDrawable bg = (GradientDrawable) mNumbers[i].getBackground();
            bg.setColor(selected ? COLOR_ACCENT : Color.TRANSPARENT);
            mNumbers[i].setTextColor(selected ? Color.WHITE : COLOR_TEXT);
            mWeekdays[i].setTextColor(selected ? COLOR_ACCENT : COLOR_TEXT_LIGHT);
        }
    }

    @Override
    protected void onDateSelected(Calendar calendar, int weekStart, boolean isClick) {
        // CalendarView setup 时就会回调一次（CalendarView#setup），因此表头可自驱动初始化
        int index = getViewIndexByCalendar(calendar, weekStart);
        updateWeekFrom(calendar, index);
    }

    /** 由任意一天反推整周日期并写入表头 */
    private void updateWeekFrom(Calendar selected, int selectedIndex) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(selected.getYear(), selected.getMonth() - 1, selected.getDay(), 12, 0, 0);
        date.add(java.util.Calendar.DATE, -selectedIndex);
        for (int i = 0; i < 7; i++) {
            mWeekdays[i].setText(WEEK_NAMES[i]);
            mNumbers[i].setText(String.valueOf(date.get(java.util.Calendar.DAY_OF_MONTH)));
            date.add(java.util.Calendar.DATE, 1);
        }
        updateSelectedIndex(selectedIndex);
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
