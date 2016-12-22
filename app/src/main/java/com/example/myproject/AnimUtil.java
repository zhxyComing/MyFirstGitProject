package com.example.myproject;

import android.view.View;
import android.view.animation.RotateAnimation;

/**
 * Created by 徐政 on 2016/12/20.
 */

public class AnimUtil {
    //这就是个静态方法，不会持有View的！以为该方法没有持续存在！
    public static void RotateAnim(View view, int time, float startAngle, float endAngle) {
        RotateAnimation animation = new RotateAnimation(startAngle, endAngle, view.getMeasuredWidth() / 2, view.getMeasuredHeight() / 2);
        animation.setDuration(time);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }
}
