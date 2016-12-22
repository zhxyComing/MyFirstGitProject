package com.example.myproject.DropView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by 徐政 on 2016/12/13.
 */

public class DropLayout extends ViewGroup {
    private View dropView;
    private int dropViewIndex;
    private int maxDropHeight;
    private int contentHeight;
    private boolean isContentHeightCanChange;
    private boolean isAnimRun;//防止在动画进行时点击
    private Context context;

    public DropLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        dropViewIndex = -1;
        maxDropHeight = 400;
        isContentHeightCanChange = true;
    }

    private void setContentHeight() {
        if (isContentHeightCanChange) {
            contentHeight = getMeasuredHeight();
        }
    }

    private void setContentHeightNoChange() {
        if (isContentHeightCanChange = true) {
            isContentHeightCanChange = false;
        }
    }

    //----measure-----------------

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //---1.content child 2.自身(需要1得到wrap下的宽高) 3.drop(需要2得到自身的宽度)
        //---判断的原因:当子View为match时，本身变长，子View会跟着边长。为了防止这种情况，参与子View测量的父长必须被指定！
        //---另外，当父为wrap时，会把子View的长作为自己的长，但是父仍然能伸长，这是因为:
        //伸长是LayoutParams设定的具体数值，具体数值时父为Exactly，长度为具体数值。
        if (isContentHeightCanChange) {
            measureChild(widthMeasureSpec, heightMeasureSpec);
        } else {
            int height = MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.getMode(heightMeasureSpec));
            measureChild(widthMeasureSpec, height);
        }
        measureSelf(widthMeasureSpec, heightMeasureSpec);
        measureDrop();
        setContentHeight();
    }

    private int atMostWidth, atMostHeight;

    private void measureChild(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < getChildCount(); i++) {
            if (i == dropViewIndex) {
                continue;
            }
            View v = getChildAt(i);
            if (v.getVisibility() != GONE) {
                measureChild(v, widthMeasureSpec, heightMeasureSpec);
                if (v.getMeasuredWidth() > atMostWidth) {
                    atMostWidth = v.getMeasuredWidth();
                }
                if (v.getMeasuredHeight() > atMostHeight) {
                    atMostHeight = v.getMeasuredHeight();
                }
            }
        }
    }

    //用作继承自ViewGroup
    private void measureSelf(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heigthSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(getSize(widthMode, widthSize, WIDTH), getSize(heightMode, heigthSize, HEIGHT));
    }

    private static final int WIDTH = 0;
    private static final int HEIGHT = 1;

    private int getSize(int mode, int size, int flag) {
        if (mode == MeasureSpec.EXACTLY) {
            return size;
        } else {
            int tempSize = 0;
            if (flag == WIDTH) {
                tempSize = atMostWidth;
            } else if (flag == HEIGHT) {
                tempSize = atMostHeight;
            }
            return tempSize;
        }
    }

    private void measureDrop() {
        if (dropView != null) {
            //下滑栏宽度为content的大小，高度正常为dropView高度，layout时不超过最大值
            //父 MeasureSpec
            int parentWidth = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
            int parentHeight = MeasureSpec.makeMeasureSpec(maxDropHeight, MeasureSpec.EXACTLY);
            //子 LayoutParams
            LayoutParams layoutParams = dropView.getLayoutParams();
            layoutParams.width = LayoutParams.MATCH_PARENT;
            dropView.setLayoutParams(layoutParams);
            measureChild(dropView, parentWidth, parentHeight);
        }
    }

    //----layout---------------
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        super.onLayout(changed, l, t, r, b);
        for (int i = 0; i < getChildCount(); i++) {
            if (i == dropViewIndex) {
                layoutDropView();
                continue;
            }
            View v = getChildAt(i);
            //按照frameLayout方式布局 所以理应只放一个ViewGroup，除非有重叠需要
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        }
    }

    private int dropHeight;

    private void layoutDropView() {
        int dropWidth = dropView.getMeasuredWidth();
        dropHeight = dropView.getMeasuredHeight() > maxDropHeight ? maxDropHeight : dropView.getMeasuredHeight();
        dropView.layout(0, contentHeight, dropWidth, contentHeight + dropHeight);
    }

    //----onTouchEvent-----------------------------
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_UP) {
//            startDrop();
//            if (listener!=null){
//                listener.changed(toggle);
//            }
//        }
        return super.onTouchEvent(event);
    }

    private onToggleChangedListener listener;

    public void setToggleChangedListener(onToggleChangedListener listener) {
        this.listener = listener;
    }

    public interface onToggleChangedListener{
        void changed(boolean toggle);
    }

    private boolean toggle;//false 表示关闭

    public boolean isOpen(){
        return toggle;
    }

    public void startDrop() {
        if (dropView == null) {
            return;
        }
        //view变化前一定设置content不可变
        setContentHeightNoChange();
        if (!toggle) {
            openDropView();
        } else {
            closeDropView();
        }
    }

    public void openDropView() { //toggle为false，关闭状态。if(!toggle){open} if(toggle){return}
        if (dropView == null || toggle || isAnimRun){
            return;
        }
        isAnimRun = true;
        dropView.setVisibility(GONE);
        ValueAnimator va = ValueAnimator.ofInt(0, dropHeight);
        va.setDuration(dropTime);
        va.setInterpolator(new DecelerateInterpolator());
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (int) animation.getAnimatedValue();
                LayoutParams layoutParams = getLayoutParams();
                if (layoutParams == null){
                    return;
                }
                layoutParams.height = contentHeight + height;
                setLayoutParams(layoutParams);
            }
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationEnd(animation);
                dropView.setVisibility(VISIBLE);
                AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                alphaAnimation.setDuration(alphaTime);
                dropView.setAnimation(alphaAnimation);
                isAnimRun = false;
            }
        });
        va.start();
        toggle = true;
    }

    public void openDropViewImmediately(){
        if (dropView == null || toggle){
            return;
        }
        LayoutParams layoutParams = getLayoutParams();
        if (layoutParams == null){
            return;
        }
        layoutParams.height = contentHeight + dropHeight;
        setLayoutParams(layoutParams);
        toggle = true;
        dropView.setVisibility(VISIBLE);
    }

    public void closeDropView() {
//        dropView.setVisibility(GONE);
        if (dropView == null || !toggle || isAnimRun) {
            return;
        }
        startAnim(dropHeight, 0, 1, 0);
        if (dropView instanceof SlideViewGroup){
            ((SlideViewGroup)dropView).closeSlide();
        }
        toggle = false;
    }

    public void closeDropViewImmediately() {
//        dropView.setVisibility(GONE);
        if (dropView == null || !toggle) {
            return;
        }
        LayoutParams layoutParams = getLayoutParams();
        if (layoutParams == null){
            return;
        }
        layoutParams.height = contentHeight;
        setLayoutParams(layoutParams);
        toggle = false;
        dropView.setVisibility(GONE);
    }

    private void startAnim(int ofIntStart, int ofIntEnd, final int alphaStart, final int alphaEnd) {
        isAnimRun = true;
        ValueAnimator va = ValueAnimator.ofInt(ofIntStart, ofIntEnd);
        va.setDuration(dropTime);
        va.setInterpolator(new DecelerateInterpolator());
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (int) animation.getAnimatedValue();
                LayoutParams layoutParams = getLayoutParams();
                if (layoutParams == null){
                    return;
                }
                layoutParams.height = contentHeight + height;
                setLayoutParams(layoutParams);
            }
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                AlphaAnimation alphaAnimation = new AlphaAnimation(alphaStart, alphaEnd);
                alphaAnimation.setDuration(alphaTime);
                dropView.setAnimation(alphaAnimation);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        dropView.setVisibility(GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAnimRun = false;
            }
        });
        va.start();
    }

    private int dropTime = 500;
    private int alphaTime = 200;

    public void setDropTime(int dropTime) {
        this.dropTime = dropTime;
    }

    public int getDropTime() {
        return dropTime;
    }

    public int getAlphaTime() {
        return alphaTime;
    }

    public void setAlphaTime(int alphaTime) {
        this.alphaTime = alphaTime;
    }

    //----add and remove  可以添加普通view
    public void addDropView(View view) {
        removeDropView();
        this.dropView = view;
        this.dropViewIndex = getChildCount();
        addView(view);
    }

    public void addDropView(int id) {
        removeDropView();
        View view = LayoutInflater.from(context).inflate(id, null);
        this.dropView = view;
        this.dropViewIndex = getChildCount();
        addView(view);
    }

    public void addSlideViewToDropView(int slideViewGroupID, int slideViewID) {
        removeDropView();
        SlideViewGroup slideViewGroup = (SlideViewGroup) LayoutInflater.from(context).inflate(slideViewGroupID, null);
        View slideView = LayoutInflater.from(context).inflate(slideViewID, null);
        slideViewGroup.setSlideView(slideView);
        this.dropView = slideViewGroup;
        this.dropViewIndex = getChildCount();
        addView(slideViewGroup);
    }

    public void addSlideViewToDropView(SlideViewGroup slideViewGroup, int slideViewID) {
        removeDropView();
        View view = LayoutInflater.from(context).inflate(slideViewID, null);
        slideViewGroup.setSlideView(view);
        this.dropView = slideViewGroup;
        this.dropViewIndex = getChildCount();
        addView(slideViewGroup);
    }

    public void addSlideViewToDropView(SlideViewGroup slideViewGroup, View view) {
        removeDropView();
        slideViewGroup.setSlideView(view);
        this.dropView = slideViewGroup;
        this.dropViewIndex = getChildCount();
        addView(slideViewGroup);
    }

    public void removeDropView() {
        if (dropView != null && dropViewIndex != -1) {
            dropView = null;
            removeViewAt(dropViewIndex);
            dropViewIndex = -1;
        }
    }

    public View getDropView() {
        return dropView;
    }
}
