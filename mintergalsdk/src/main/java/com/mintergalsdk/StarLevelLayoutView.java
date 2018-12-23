package com.mintergalsdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by liyanju on 2018/12/1.
 */

public class StarLevelLayoutView extends LinearLayout {

    public StarLevelLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StarLevelLayoutView(Context context) {
        super(context);
    }

    @SuppressLint("NewApi")
    public StarLevelLayoutView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setRating(int rating) {
        removeAllViews();
        int tempRating = rating;
        if (tempRating == 0) {
            tempRating = 5;
        }
        int i = 0;
        while (i < 5) {
            ImageView imageView = new ImageView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            if (i < tempRating) {
                imageView.setImageResource(R.drawable.mintegral_demo_star_sel);
            } else {
                imageView.setImageResource(R.drawable.mintegral_demo_star_nor);
            }
            addView(imageView, lp);
            i++;
        }
    }

}
