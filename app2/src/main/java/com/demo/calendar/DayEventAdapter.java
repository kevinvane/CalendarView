package com.demo.calendar;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 日程卡片列表适配器。圆角底色 / 圆形徽标均在代码中按事件配色动态绘制，
 * 不依赖额外 drawable 资源。
 */
public class DayEventAdapter extends RecyclerView.Adapter<DayEventAdapter.EventHolder> {

    private final List<DayEvent> mEvents = new ArrayList<>();
    private OnEventClickListener mListener;

    public interface OnEventClickListener {
        void onEventClick(DayEvent event);
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.mListener = listener;
    }

    public void replaceData(List<DayEvent> events) {
        mEvents.clear();
        if (events != null) {
            mEvents.addAll(events);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_event, parent, false);
        return new EventHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventHolder holder, int position) {
        final DayEvent event = mEvents.get(position);
        holder.tvTitle.setText(event.title);
        holder.tvTime.setText(event.time);
        holder.tvBadge.setText(event.badge);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setCornerRadius(dp(holder.itemView, 14));
        cardBg.setColor(event.cardColor);
        holder.card.setBackground(cardBg);

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setShape(GradientDrawable.OVAL);
        badgeBg.setColor(event.accentColor);
        holder.tvBadge.setBackground(badgeBg);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onEventClick(event);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    static class EventHolder extends RecyclerView.ViewHolder {
        final LinearLayout card;
        final TextView tvTitle;
        final TextView tvTime;
        final TextView tvBadge;

        EventHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.ll_event_card);
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvTime = itemView.findViewById(R.id.tv_event_time);
            tvBadge = itemView.findViewById(R.id.tv_event_badge);
        }
    }

    private static float dp(View view, int value) {
        return value * view.getResources().getDisplayMetrics().density;
    }
}
