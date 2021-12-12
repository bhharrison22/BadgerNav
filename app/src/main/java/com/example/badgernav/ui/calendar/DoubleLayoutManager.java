package com.example.badgernav.ui.calendar;

import android.content.Context;
import android.os.Parcelable;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class DoubleLayoutManager extends LinearLayoutManager {


    private int pendingTargetPos = -1;
    private int pendingPosOffset = -1;

    DoubleLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (pendingTargetPos != -1 && state.getItemCount() > 0) {
            scrollToPositionWithOffset(pendingTargetPos, pendingPosOffset);
            pendingTargetPos = -1;
            pendingPosOffset = -1;
        }
        super.onLayoutChildren(recycler, state);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        pendingPosOffset = -1;
        pendingTargetPos = -1;
        super.onRestoreInstanceState(state);
    }

    public void setTargetStartPosition(int position, int offset) {
        pendingPosOffset = offset;
        pendingTargetPos = position;
    }
}
