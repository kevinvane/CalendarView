package com.demo.calendar.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import androidx.annotation.Nullable;

import com.demo.calendar.TimeSlotEvent;
import com.haibin.calendarview.Calendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 时间网格周视图（对标竞品）：
 * 左侧固定时刻刻度 + 7 列日列，事件块按 startMinutes/endMinutes 纵向定位，
 * 当天列绘制"当前时间红线"。
 *
 * 不消费触摸事件（onTouchEvent 返回 false），由外层 ScrollView 负责纵向滚动，
 * 点击命中通过 GestureDetector 的 onSingleTapUp 识别。
 */
@SuppressLint("ClickableViewAccessibility")
public class TimeGridView extends android.view.View {

    /** 每小时行高 */
    private static final int HOUR_HEIGHT_DP = 64;
    /** 左侧时刻刻度列宽 */
    private static final int GUTTER_DP = 52;

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(Calendar day, TimeSlotEvent event);
    }

    private final Paint mGridLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mColumnLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mEventPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTimeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mHourLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mNowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mRect = new RectF();

    private final List<Calendar> mDays = new ArrayList<>();
    private final List<List<TimeSlotEvent>> mWeekEvents = new ArrayList<>();
    private int mSelectedIndex = -1;
    private OnTimeSlotClickListener mListener;

    private final GestureDetector mDetector = new GestureDetector(getContext(),
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    handleTap(e.getX(), e.getY());
                    return true;
                }
            });

    public TimeGridView(Context context) {
        this(context, null);
    }

    /** XML 膨胀必需的构造函数 */
    public TimeGridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mGridLinePaint.setColor(0xFFEDEDEF);
        mGridLinePaint.setStrokeWidth(dp(1));

        mColumnLinePaint.setColor(0xFFF0F0F2);
        mColumnLinePaint.setStrokeWidth(dp(1));

        mHighlightPaint.setColor(0x0DF45B3F); // 选中列淡红色高亮

        mEventPaint.setStyle(Paint.Style.FILL);
        mEventPaint.setColor(TimeSlotEvent.BLOCK_COLOR);

        mTitlePaint.setColor(0xFF26262B);
        mTitlePaint.setTextSize(dp(13));
        mTitlePaint.setFakeBoldText(true);

        mTimeTextPaint.setColor(0xFF4A4A50);
        mTimeTextPaint.setTextSize(dp(11));

        mHourLabelPaint.setColor(0xFF9A9AA0);
        mHourLabelPaint.setTextSize(dp(12));

        mNowPaint.setColor(0xFFF45B3F);
        mNowPaint.setStyle(Paint.Style.FILL);

        setWillNotDraw(false);
    }

    public void setOnTimeSlotClickListener(OnTimeSlotClickListener listener) {
        mListener = listener;
    }

    /**
     * 设置当前周：days 为 7 天（周日起始），events 与之一一对应。
     */
    public void setWeek(List<Calendar> days, List<List<TimeSlotEvent>> events) {
        mDays.clear();
        mWeekEvents.clear();
        if (days != null) {
            mDays.addAll(days);
        }
        if (events != null) {
            mWeekEvents.addAll(events);
        }
        invalidate();
    }

    public void setSelectedDayIndex(int index) {
        mSelectedIndex = index;
        invalidate();
    }

    public int getSelectedDayIndex() {
        return mSelectedIndex;
    }

    /** 滚动到当前时刻附近（定位到视口上 1/3 处） */
    public void scrollToNow(final ScrollView scrollView) {
        if (scrollView == null) {
            return;
        }
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                int y = (int) (currentDayMinutes() * pixelPerMinute()
                        - scrollView.getHeight() / 3f);
                scrollView.scrollTo(0, Math.max(0, y));
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = (int) (24 * dp(HOUR_HEIGHT_DP) + dp(16));
        setMeasuredDimension(getMeasuredWidth(), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int gutter = dp(GUTTER_DP);
        int columns = Math.max(mDays.size(), 1);
        float columnWidth = (width - gutter) / (float) columns;
        float hourHeight = dp(HOUR_HEIGHT_DP);

        // 选中列背景高亮
        if (mSelectedIndex >= 0 && mSelectedIndex < columns) {
            mRect.set(gutter + mSelectedIndex * columnWidth, 0,
                    gutter + (mSelectedIndex + 1) * columnWidth, height);
            canvas.drawRect(mRect, mHighlightPaint);
        }

        // 日列分隔线
        for (int i = 0; i <= columns; i++) {
            float x = gutter + i * columnWidth;
            canvas.drawLine(x, 0, x, height, mColumnLinePaint);
        }

        // 小时横线 + 左侧刻度
        for (int hour = 0; hour <= 24; hour++) {
            float y = hour * hourHeight;
            canvas.drawLine(gutter, y, width, y, mGridLinePaint);
            if (hour < 24) {
                String label = hourLabel(hour);
                float textWidth = mHourLabelPaint.measureText(label);
                canvas.drawText(label, gutter - dp(8) - textWidth,
                        y + mHourLabelPaint.getTextSize(), mHourLabelPaint);
            }
        }

        // 事件块（按重叠分组分列）
        for (int c = 0; c < mWeekEvents.size(); c++) {
            drawColumnEvents(canvas, c, gutter + c * columnWidth,
                    columnWidth, hourHeight);
        }

        // 当前时间红线（仅当天在当前周时绘制）
        int todayIndex = indexOfToday();
        if (todayIndex >= 0) {
            float x = gutter + todayIndex * columnWidth;
            float y = currentDayMinutes() * pixelPerMinute();
            canvas.drawLine(x, y, x + columnWidth, y, mNowPaint);
            canvas.drawCircle(x + dp(5), y, dp(3.5f), mNowPaint);
        }
    }

    /**
     * 绘制一列的事件块；同列内重叠的事件做"分组均分列宽"布局。
     */
    private void drawColumnEvents(Canvas canvas, int dayIndex, float columnLeft,
                                  float columnWidth, float hourHeight) {
        if (dayIndex >= mWeekEvents.size()) {
            return;
        }
        List<TimeSlotEvent> events = new ArrayList<>(mWeekEvents.get(dayIndex));
        Collections.sort(events, (a, b) -> a.startMinutes - b.startMinutes);

        float pxPerMinute = hourHeight / 60f;
        int i = 0;
        while (i < events.size()) {
            // 1. 找一个重叠簇：簇内所有事件两两重叠（首尾相接也算一簇）
            int clusterEnd = events.get(i).endMinutes;
            int j = i + 1;
            while (j < events.size() && events.get(j).startMinutes < clusterEnd) {
                clusterEnd = Math.max(clusterEnd, events.get(j).endMinutes);
                j++;
            }
            List<TimeSlotEvent> cluster = events.subList(i, j);

            // 2. 簇内贪心分配泳道
            List<Integer> laneEnds = new ArrayList<>();
            int[] lanes = new int[cluster.size()];
            for (int k = 0; k < cluster.size(); k++) {
                TimeSlotEvent event = cluster.get(k);
                int lane = 0;
                while (lane < laneEnds.size() && laneEnds.get(lane) > event.startMinutes) {
                    lane++;
                }
                if (lane == laneEnds.size()) {
                    laneEnds.add(event.endMinutes);
                } else {
                    laneEnds.set(lane, event.endMinutes);
                }
                lanes[k] = lane;
            }

            // 3. 簇内均分列宽绘制
            float laneWidth = columnWidth / laneEnds.size();
            for (int k = 0; k < cluster.size(); k++) {
                drawEventBlock(canvas, cluster.get(k),
                        columnLeft + lanes[k] * laneWidth, laneWidth,
                        cluster.get(k).startMinutes * pxPerMinute,
                        cluster.get(k).endMinutes * pxPerMinute);
            }
            i = j;
        }
    }

    private void drawEventBlock(Canvas canvas, TimeSlotEvent event, float left,
                                float width, float top, float bottom) {
        float inset = dp(2);
        mRect.set(left + inset, top + dp(2), left + width - inset, bottom - dp(2));
        canvas.drawRoundRect(mRect, dp(8), dp(8), mEventPaint);

        float textX = left + inset + dp(8);
        canvas.drawText(event.title, textX, top + dp(19), mTitlePaint);
        canvas.drawText(event.timeText, textX, top + dp(34), mTimeTextPaint);
    }

    private void handleTap(float x, float y) {
        if (mListener == null || mDays.isEmpty()) {
            return;
        }
        int gutter = dp(GUTTER_DP);
        if (x < gutter) {
            return;
        }
        float columnWidth = (getWidth() - gutter) / (float) mDays.size();
        int column = (int) ((x - gutter) / columnWidth);
        if (column < 0 || column >= mDays.size()) {
            return;
        }
        int scrollY = getParent() instanceof View ? ((View) getParent()).getScrollY() : 0;
        int minutes = (int) ((y + scrollY) / pixelPerMinute());
        List<TimeSlotEvent> events = mWeekEvents.size() > column
                ? mWeekEvents.get(column) : Collections.<TimeSlotEvent>emptyList();
        for (TimeSlotEvent event : events) {
            if (event.contains(minutes)) {
                mListener.onTimeSlotClick(mDays.get(column), event);
                return;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        // 必须消费 ACTION_DOWN 才能继续收到 ACTION_UP 完成点击判定；
        // 纵向滚动由外层 ScrollView 在 MOVE 超过滑动阈值时接管（发 ACTION_CANCEL）
        return true;
    }

    private float pixelPerMinute() {
        return dp(HOUR_HEIGHT_DP) / 60f;
    }

    private int currentDayMinutes() {
        java.util.Calendar now = java.util.Calendar.getInstance();
        return now.get(java.util.Calendar.HOUR_OF_DAY) * 60
                + now.get(java.util.Calendar.MINUTE);
    }

    private int indexOfToday() {
        java.util.Calendar now = java.util.Calendar.getInstance();
        for (int i = 0; i < mDays.size(); i++) {
            Calendar day = mDays.get(i);
            if (day.getYear() == now.get(java.util.Calendar.YEAR)
                    && day.getMonth() == now.get(java.util.Calendar.MONTH) + 1
                    && day.getDay() == now.get(java.util.Calendar.DAY_OF_MONTH)) {
                return i;
            }
        }
        return -1;
    }

    private static String hourLabel(int hour) {
        int h = hour % 12;
        if (h == 0) {
            h = 12;
        }
        return h + (hour < 12 ? " AM" : " PM");
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
