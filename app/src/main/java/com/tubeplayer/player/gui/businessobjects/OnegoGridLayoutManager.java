package com.tubeplayer.player.gui.businessobjects;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by liyanju on 2018/3/23.
 */

public class OnegoGridLayoutManager extends GridLayoutManager {

    public OnegoGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public OnegoGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public OnegoGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            return super.scrollHorizontallyBy(dx, recycler, state);
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }
    }
}
