package com.demo.calendar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 时间网格（周视图）事件模型：分钟级时段。
 * 与 DayPanelActivity 的 DayEvent 不同，这里必须是结构化的起止时间，
 * 才能按 y = startMinutes * pxPerMinute 定位事件块。
 */
public class TimeSlotEvent {

    /** 事件块底色：统一浅青色（对标竞品截图） */
    public static final int BLOCK_COLOR = 0xFFC3E8F2;

    /** 事件标题 */
    public final String title;
    /** 距当天 00:00 的开始分钟数，如 720 = 12:00 */
    public final int startMinutes;
    /** 距当天 00:00 的结束分钟数（不含），如 780 = 13:00 */
    public final int endMinutes;
    /** 事件块底色 */
    public final int color;
    /** 事件块内的时间文案，如 "12 - 1 PM" */
    public final String timeText;

    public TimeSlotEvent(String title, int startMinutes, int endMinutes, int color, String timeText) {
        this.title = title;
        this.startMinutes = startMinutes;
        this.endMinutes = endMinutes;
        this.color = color;
        this.timeText = timeText;
    }

    public boolean contains(int minutes) {
        return minutes >= startMinutes && minutes < endMinutes;
    }

    private static final String[] TITLES = {
            "午餐", "团队会议", "测试任务", "健身", "需求评审",
    };

    /**
     * 确定性假数据：同一天多次调用结果一致，保证翻周回来数据不跳变。
     */
    public static List<TimeSlotEvent> createFor(int year, int month, int day) {
        List<TimeSlotEvent> events = new ArrayList<>();
        long seed = year * 10000L + month * 100L + day;
        int count = (int) (seed % 4); // 0 ~ 3 条
        if (count == 0) {
            return events;
        }
        Random random = new Random(seed);
        for (int i = 0; i < count; i++) {
            int startHour = 8 + random.nextInt(11); // 8:00 ~ 18:00
            int duration = 1 + random.nextInt(2);
            int endHour = Math.min(startHour + duration, 22);
            events.add(new TimeSlotEvent(
                    TITLES[random.nextInt(TITLES.length)],
                    startHour * 60,
                    endHour * 60,
                    BLOCK_COLOR,
                    formatTimeText(startHour, endHour)));
        }
        return events;
    }

    /** "12 - 1 PM" 风格时间段文案 */
    private static String formatTimeText(int startHour, int endHour) {
        return String.format(Locale.US, "%d - %d %s",
                hour12(startHour), hour12(endHour), endHour < 12 ? "AM" : "PM");
    }

    private static int hour12(int hour) {
        int h = hour % 12;
        return h == 0 ? 12 : h;
    }
}
