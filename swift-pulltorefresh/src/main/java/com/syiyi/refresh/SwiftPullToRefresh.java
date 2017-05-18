package com.syiyi.refresh;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;

/**
 * pull-to-refresh
 * Created by Dell on 2017/5/17.
 */

public class SwiftPullToRefresh extends FrameLayout {
    private static final int INVALID_POINTER = -1;
    private static final String LOG_TAG = SwipeRefreshLayout.class.getSimpleName();
    private static final float DRAG_RATE = 0.5f;

    private int mTouchSlop;
    private float mInitialMotionY;
    private float mInitialDownY;
    private boolean mIsBeingDragged;
    boolean mRefreshing = false;
    private boolean mReturningToStart;
    int mCurrentTargetOffsetTop;
    protected int mOriginalOffsetTop;
    private int mActivePointerId = INVALID_POINTER;
    private View mTarget;
    private ViewGroup mRefreshView;
    private boolean hasMeasured;
    private IRefreshHandler mRefreshHandler;

    public SwiftPullToRefresh(Context context) {
        super(context, null, 0);
    }

    public SwiftPullToRefresh(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwiftPullToRefresh(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mRefreshView = new FrameLayout(getContext());
        addView(mRefreshView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void setRefreshHandler(IRefreshHandler handler) {
        mRefreshHandler = handler;
        mRefreshView.removeAllViews();
        mRefreshView.addView(handler.getRefreshView(this));
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() >= 2) {
            throw new RuntimeException("this view must have only one child that need refresh");
        }
        super.addView(child, index, params);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!hasMeasured) {
            mCurrentTargetOffsetTop = -mRefreshView.getMeasuredHeight();
            hasMeasured = true;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        final View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        int circleWidth = mRefreshView.getMeasuredWidth();
        int circleHeight = mRefreshView.getMeasuredHeight();
        if (mRefreshHandler.enableFloatModel()) {
            mRefreshView.layout((width / 2 - circleWidth / 2), mCurrentTargetOffsetTop,
                    (width / 2 + circleWidth / 2), mCurrentTargetOffsetTop + circleHeight);
        } else {
            mRefreshView.layout((width / 2 - circleWidth / 2), -circleHeight,
                    (width / 2 + circleWidth / 2), -circleHeight + circleHeight);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canChildScrollUp()
                || mRefreshing) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
                }

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = ev.getY(pointerIndex);
                startDragging(y);
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex = -1;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canChildScrollUp()
                || mRefreshing) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mRefreshHandler.enableFloatModel()) {
                    mOriginalOffsetTop = -mRefreshView.getHeight();
                } else {
                    mOriginalOffsetTop = 0;
                }
                mCurrentTargetOffsetTop = mOriginalOffsetTop;
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = ev.getY(pointerIndex);
                startDragging(y);

                if (mIsBeingDragged) {
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    if (overscrollTop > 0) {
                        moveRefresh(overscrollTop);
                    } else {
                        return false;
                    }
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG,
                            "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return false;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (mIsBeingDragged) {
                    final float y = ev.getY(pointerIndex);
                    final float overScrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    mIsBeingDragged = false;
                    releaseRefresh(overScrollTop);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                return false;
        }

        return true;
    }


    private void moveRefresh(float overScrollTop) {
        mRefreshView.bringToFront();
        float targetY;
        if (mRefreshHandler.enableFloatModel()) {
            targetY = mOriginalOffsetTop - mCurrentTargetOffsetTop + overScrollTop;
        } else {
            targetY = overScrollTop - mOriginalOffsetTop + mCurrentTargetOffsetTop;
        }
        if (!mRefreshing && !mReturningToStart) {
            float percent = overScrollTop / mRefreshHandler.getBeginRefreshDistance();
            mRefreshHandler.onPullProcess(percent);
        }
        setTargetOffsetTopAndBottom((int) targetY, true);
    }

    void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
        if (mRefreshHandler.enableFloatModel()) {
            ViewCompat.offsetTopAndBottom(mRefreshView, offset);
            mCurrentTargetOffsetTop = mRefreshView.getTop();
        } else {
            scrollBy(0, -offset);
            mCurrentTargetOffsetTop = getScrollY();
        }
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
    }

    private void releaseRefresh(float overscrollTop) {
        if (overscrollTop > mRefreshHandler.getBeginRefreshDistance()) {
//            animalToRefresh();
        } else {
            animalToStart(overscrollTop);
        }
    }

    public void animalToStart(float overscrollTop) {
        mReturningToStart = true;
        mRefreshing = false;
        if (mRefreshHandler.enableFloatModel()) {
            mOriginalOffsetTop = -mRefreshView.getHeight();
        } else {
            mOriginalOffsetTop = 0;

        }
        startScollToBackAnmial(overscrollTop, new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                moveRefresh(value);
            }
        }, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mReturningToStart = false;
                mRefreshing = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }


//
//    private void animalToRefresh() {
//        mRefreshing = true;
//        mOriginalOffsetTop = mCurrentTargetOffsetTop;
//        startScollToBackAnmial(mRefreshHandler.getBeginRefreshDistance(), new ValueAnimator.AnimatorUpdateListener() {
//
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                float value = 0 - (float) animation.getAnimatedValue();
//                moveRefresh(value);
//            }
//        }, new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                mRefreshHandler.onRefresh();
//                mOriginalOffsetTop = mCurrentTargetOffsetTop;
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//    }
//


    @SuppressLint("NewApi")
    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mIsBeingDragged = true;
            mRefreshHandler.onBeginPull();
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mRefreshView)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    public static void startScollToBackAnmial(float height, ValueAnimator.AnimatorUpdateListener valueListener, Animator.AnimatorListener listener) {
        ValueAnimator mAnimal = ValueAnimator.ofFloat(height, 0);
        mAnimal.setDuration((long) (height / 1920 * 1000));
        mAnimal.setInterpolator(new DecelerateInterpolator());
        mAnimal.addUpdateListener(valueListener);
        mAnimal.addListener(listener);
        mAnimal.start();

    }
}
