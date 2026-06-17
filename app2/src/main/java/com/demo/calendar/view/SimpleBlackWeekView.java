package com.demo.calendar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.WeekView;

import java.util.List;

public class SimpleBlackWeekView extends WeekView {

    private int mRadius;
    private Paint mEventPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SimpleBlackWeekView(Context context) {
        super(context);
        mEventPaint.setStyle(Paint.Style.FILL);
        mEventPaint.setTextAlign(Paint.Align.LEFT);
        mEventPaint.setTextSize(dipToPx(12));
        mEventPaint.setFakeBoldText(true);

        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(1);
        mRectPaint.setColor(Color.parseColor("#e5e5e5"));

    }

    @Override
    protected void onPreviewHook() {
        mRadius = Math.min(mItemWidth, mItemHeight) / 5 * 2;
        mSchemePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, boolean hasScheme) {
        // int center_x = x + mItemWidth / 2;
        // int center_y = (int) (mDayTextBaseLine / 2);
        // canvas.drawCircle(center_x, center_y, mRadius, mSelectedPaint);
        int offset = 6;
        mSelectedPaint.setStyle(Paint.Style.STROKE);
        mSelectedPaint.setStrokeWidth(6);
        canvas.drawRoundRect(
                x + offset,
                8 ,
                x + mItemWidth - offset,
                mItemHeight - 12,
                4f,
                4f,
                mSelectedPaint
        );
        return true;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x) {
        List<Calendar.Scheme> schemes = calendar.getSchemes();
        if (schemes == null) return;
        int top = (int) mDayTextBaseLine + dipToPx(12);
        for (Calendar.Scheme s : schemes) {
            mEventPaint.setColor(s.getShcemeColor());
            int left = x + dipToPx(6);
            int right = x + mItemWidth - dipToPx(6);
            int height = dipToPx(22);
            canvas.drawRoundRect(left, top, right, top + height, dipToPx(4), dipToPx(4), mEventPaint);
            mEventPaint.setColor(0xFFFFFFFF);
            mEventPaint.setTextSize(dipToPx(11));
            canvas.drawText(s.getScheme(), left + dipToPx(6), top + dipToPx(15), mEventPaint);
            top += height + dipToPx(4);
        }
    }

    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, boolean hasScheme, boolean isSelected) {
        int cx = x + mItemWidth / 2;
        Paint paint;
        int y = 0;
        canvas.drawRect(x, y, x + mItemWidth, y + mItemHeight, mRectPaint);

        if (isSelected) {
            paint = mSelectTextPaint;
        } else if (hasScheme) {
            paint = calendar.isCurrentDay() ? mCurDayTextPaint : mCurMonthTextPaint;
        } else {
            paint = calendar.isCurrentDay() ? mCurDayTextPaint :
                    calendar.isWeekend() ? mOtherMonthTextPaint : mCurMonthTextPaint;
        }
        canvas.drawText(String.valueOf(calendar.getDay()), cx, mDayTextBaseLine, paint);
    }

    private int dipToPx(float dp) {
        return (int) (getContext().getResources().getDisplayMetrics().density * dp + 0.5f);
    }
}
