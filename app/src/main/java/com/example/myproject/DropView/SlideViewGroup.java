package com.example.myproject.DropView;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by 徐政 on 2016/12/13.
 */

public class SlideViewGroup extends ViewGroup {
    private View slideView;
    private int slideWidth = 100;
    private int slideViewIndex = -1;

    private Paint mPaint;
    private Scroller mScroller;

    public SlideViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mScroller = new Scroller(context);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    //----onMeasure----测量宽高
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChild(widthMeasureSpec, heightMeasureSpec);
        measureSelf(widthMeasureSpec, heightMeasureSpec);
    }

    private int atMostWidth, atMostHeight;

    private void measureChild(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < getChildCount(); i++) {
            if (i == slideViewIndex) {
                measureSlide();
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

    private void measureSlide() {
        if (slideView != null) {
            //父 MeasureSpec
            int parentWidth = MeasureSpec.makeMeasureSpec(slideWidth, MeasureSpec.EXACTLY);
            int parentHeight = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);//这里getMeasureSpec为0
            //子 LayoutParams
            LayoutParams layoutParams = slideView.getLayoutParams();
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.height = LayoutParams.MATCH_PARENT;
            slideView.setLayoutParams(layoutParams);

            measureChild(slideView, parentWidth, parentHeight);
        }
    }

    //----onLayout----布局子视图
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //假如本身为wrap，则本身为at_most
        //父at_most + 子match = 子wrap
        //所以本身为wrap时，子布局自适应，自身大小为子布局中的最大大小（v.getMeasureWidth）
        for (int i = 0; i < getChildCount(); i++) {
            if (i == slideViewIndex) {
                layoutSlide();
                continue;
            }
            View v = getChildAt(i);
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        }
    }

    //根据测量的宽高结果布局，测量宽高分别是slideWidth和父布局高度
    private void layoutSlide() {
        if (slideView != null) {
            int width = slideView.getMeasuredWidth();
            int height = slideView.getMeasuredHeight();
            slideView.layout(-width, 0, 0, height);
        }
    }

    //----onTouchEvent----各种手动自动滑动事件
    private float nowX, nowY;
    private float oldX, oldY;
    private float disX, disY;
    private boolean toggle;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                oldX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                nowX = event.getRawX();
                disX = nowX - oldX;
                //右滑>0
                if (getScrollX() - disX < -slideWidth) { //-300 - 99 = -399 >- 400 可以继续滑动
                    //假如超出限定距离，不是不移动，而是移动到边界
                    scrollTo(-slideWidth, 0);
                } else if (getScrollX() - disX > 0) {
                    scrollTo(0, 0);
                } else {
                    scrollBy((int) -disX, 0);
                }
                oldX = nowX;
                break;
            case MotionEvent.ACTION_UP:
                if (getScrollX() < -slideWidth / 3) {
                    mScroller.startScroll(getScrollX(), 0, -slideWidth - getScrollX(), 0);
                    invalidate();
                    break;
                }
                closeSlide();
//                if (!toggle){
//                    mScroller.startScroll(getScrollX(), 0, -slideWidth - getScrollX(), 0);
//                }else {
//                    mScroller.startScroll(getScrollX(), 0, 0 - getScrollX(), 0);
//                }
//                invalidate();
//                toggle = !toggle;
                break;
            case MotionEvent.ACTION_CANCEL:
                closeSlide();
                break;
        }
        return true;
    }

    public void closeSlide() {
        mScroller.startScroll(getScrollX(), 0, 0 - getScrollX(), 0);
        invalidate();
    }

    //----onInterceptTouchEvent----左右滑动时抢夺子View事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                oldX = ev.getRawX();
                oldY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                nowX = ev.getRawX();
                nowY = ev.getRawY();
                disX = nowX - oldX;
                disY = nowY - oldY;
                if (Math.abs(disX) > Math.abs(disY) * 3) {//横向滑动
                    return true;
                }
                oldX = nowX;
                oldY = nowY;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    //----增删侧边栏View以及参数设置

    public void setSlideWidth(int slideWidth) {
        this.slideWidth = slideWidth;
    }

    public int getSlideWidth() {
        return slideWidth;
    }

    public void setSlideView(View slideView) {
        removeSlideView();
        this.slideView = slideView;
        this.slideViewIndex = getChildCount();
        addView(slideView);
    }

    public void removeSlideView() {
        if (this.slideView != null && this.slideViewIndex != -1) {
            this.slideView = null;
            removeViewAt(slideViewIndex);
            this.slideViewIndex = -1;
        }
    }

    public View getSlideView(){
        if (slideViewIndex >0 ){
            return getChildAt(slideViewIndex);
        }
        return null;
    }
}
