package com.example.myproject.DropView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by 徐政 on 2016/12/19.
 */

public class SelfSlideView extends ViewGroup {
    private static final int FIRST_SIZE = -1;
    private Scroller mScroller;

    public SelfSlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
    }

    //------------------measure---------------------
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //wrap时大小设定为第一个添加的View的大小
        measureChild(widthMeasureSpec, heightMeasureSpec);
        measureSelf(widthMeasureSpec, heightMeasureSpec);
    }

    private int width, height;

    private void measureChild(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        View v = getChildAt(0);
        measureChild(v, widthMeasureSpec, heightMeasureSpec);
        //父Exactly   子wrap(不充满) 子match(充满父) 子具体(不充满)
        //父at_most   子wrap(跟随第一子充满) 子match(跟随第一子充满) 子具体(跟随第一子充满)
        width = v.getMeasuredWidth(); //什么时候用？父为at_most,跟随第一子。后续所有view同。否则用父
        height = v.getMeasuredHeight();

        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {//都是Exactly，要求所有子无论如何随父:子设定match
            measureChildFor(widthMeasureSpec, heightMeasureSpec);
        } else if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {//height跟随第一子,后续View高度同此高
            measureChildFor(widthMeasureSpec, FIRST_SIZE);
        } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            measureChildFor(FIRST_SIZE, heightMeasureSpec);
        } else {
            measureChildFor(FIRST_SIZE, FIRST_SIZE);
        }
    }

    private void measureChildFor(int widthMeasureSpec, int heightMeasureSpec) {
        int firstHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        int firstWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams layoutParams = child.getLayoutParams();
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.height = LayoutParams.MATCH_PARENT;
            child.setLayoutParams(layoutParams);
            measureChild(child, widthMeasureSpec == FIRST_SIZE ? firstWidthMeasureSpec : widthMeasureSpec, heightMeasureSpec == FIRST_SIZE ? firstHeightMeasureSpec : heightMeasureSpec);
        }
    }

    private void measureSelf(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getWidthSize(widthMeasureSpec), getHeightSize(heightMeasureSpec));
    }

    private int getWidthSize(int widthMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            return size;
        } else {
            return width;
        }
    }

    private int getHeightSize(int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            return size;
        } else {
            return height;
        }
    }

    //----------------------layout----------------------------
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            v.layout(0, height * i, getMeasuredWidth(), height * (i + 1));
        }
    }

    //----------------------onTouchEvent-----------------------
    private float oldX, oldY;
    private float nowX, nowY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                oldX = event.getRawX();
                oldY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                nowX = event.getRawX();
                nowY = event.getRawY();
                float disX = nowX - oldX;
                float disY = nowY - oldY;
                //↑手指向上滑 画布向上 getScrollY > 0 ; disY < 0
                if (-getScrollY() + disY > 0) {
                    scrollTo(0, 0);
                } else if (-getScrollY() + disY < -(height * (getChildCount() - 1))) {
                    scrollTo(0, height * (getChildCount() - 1));
                } else {
                    scrollBy(0, (int) -disY);
                }
                oldX = nowX;
                oldY = nowY;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                int position = getScrollY() / height;
                int dir = getScrollY() % height;
                if (dir < height / 2) {
                    mScroller.startScroll(0, getScrollY(), 0, -dir);
                    if (onItemChangdListener!=null){
                        onItemChangdListener.onChanged(position);
                    }
//                    scrollBy(0, -dir);
                } else {
                    mScroller.startScroll(0, getScrollY(), 0, height - dir);
                    if (onItemChangdListener!=null){
                        onItemChangdListener.onChanged(position+1);
                    }
//                    scrollBy(0, height - dir);
                }
                invalidate();
                break;
        }
        return true;
    }

    private OnItemChangdListener onItemChangdListener;

    public void setOnItemChangdListener(OnItemChangdListener onItemChangdListener) {
        this.onItemChangdListener = onItemChangdListener;
    }

    public interface OnItemChangdListener{
        void onChanged(int nowPosition);
    }

    //假如子View有简单的点击事件，它会把其他事件一并拦截导致滑动无效，这时候就要判断在滑动的情况下cancel掉事件！
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
                float disX = nowX - oldX;
                float disY = nowY - oldY;
                if (Math.abs(disY) > Math.abs(disX)) {//横向滑动
                    return true;
                }
                oldX = nowX;
                oldY = nowY;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }
}
