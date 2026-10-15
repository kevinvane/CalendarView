package com.demo.calendar;

import androidx.annotation.ColorInt;

/**
 * 单条日程（P0 Demo 的假数据模型）。
 * 注意：CalendarView 库本身没有日程数据模型，只有 Scheme 标记；
 * 真实业务里应由数据层提供，通过 key（yyyyMMdd）与日期关联。
 */
public class DayEvent {

    /** 日程标题 */
    public final String title;
    /** 时间段文案，如 "09:30 - 10:30" */
    public final String time;
    /** 卡片底色（浅色） */
    @ColorInt
    public final int cardColor;
    /** 徽标底色（同色系深色），也用作月视图上的 Scheme 圆点颜色 */
    @ColorInt
    public final int accentColor;
    /** 右侧圆形徽标文字 */
    public final String badge;

    public DayEvent(String title, String time, int cardColor, int accentColor, String badge) {
        this.title = title;
        this.time = time;
        this.cardColor = cardColor;
        this.accentColor = accentColor;
        this.badge = badge;
    }
}
