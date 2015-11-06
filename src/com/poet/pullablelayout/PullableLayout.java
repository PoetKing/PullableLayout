/**
 * 
 */
package com.poet.pullablelayout;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import com.poet.pullablelayout.Indicator.IndicatorSet;

/**
 * @Description 
 * @author POET_WYD@FOXMAIL.COM
 * @date 2015-11-6
 */
public class PullableLayout extends FrameLayout {

    float FRICTION = 2.0f;

    private FrameLayout mIndicatorWrapper;
    private View mTargetView;

    private int mTouchSlop;
    private int mIndicatorHeight;
    private Indicator mIndicator;
    private IndicatorSet mIndicatorSet;

    private State mState = State.RESET;
    private float mDownY, mLastX, mLastY;
    private boolean mIsPulling;

    public PullableLayout(Context context) {
        super(context);
        init(context);
    }

    void init(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mIndicatorWrapper = new FrameLayout(context);
        super.addView(mIndicatorWrapper);
    }

    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        if (mTargetView != null) {
            throw new IllegalStateException("PullableLayout can host only one direct child");
        }
        if (child != mIndicatorWrapper) {
            mTargetView = child;
        }
        super.addView(child, 0, params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        System.out.println("onInterceptTouchEvent" + ev.getAction());
        if (mIndicator == null || !isReadyForPullDown()) {
            return super.onInterceptTouchEvent(ev);
        }
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mLastX = ev.getX();
            mDownY = mLastY = ev.getY();
            return mIsPulling = false;
        }
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            return mIsPulling = false;
        }
        return mIsPulling;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        System.out.println("onTouchEvent" + event.getAction() + " " + mIsPulling);
        if (mIndicator == null || !isReadyForPullDown()) {
            return false;
        }
        final int action = event.getAction();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mLastX = event.getX();
            mDownY = mLastY = event.getY();
            mIsPulling = false;
            break;
        case MotionEvent.ACTION_MOVE:
            if (!mIsPulling) {
                float x = event.getX();
                float y = event.getY();
                float dx = x - mLastX;
                float dy = y - mLastY;
                if (Math.abs(dy) > mTouchSlop && (Math.abs(dy) > Math.abs(dx))) {
                    mLastX = x;
                    mLastY = y;
                    mIsPulling = true;
                }
            } else {
                mLastX = event.getX();
                mLastY = event.getY();
                int diff = Math.round(Math.min(mDownY - mLastY, 0) / FRICTION);
                if (isRunning()) {
                    diff -= mIndicatorHeight;
                }
                float scale = Math.abs(diff) / (float) mIndicatorHeight;
                if (mIndicator.maxPullPercent() < 1 || scale <= mIndicator.maxPullPercent()) {
                    scrollToY(diff);
                } else {
                    scrollToY(-(int) (mIndicator.maxPullPercent() * mIndicatorHeight));
                    scale = mIndicator.maxPullPercent();
                }
                if (!isRunning()) {
                    mIndicator.onPull(scale);
                    if (mState != State.PULL_TO_RUN && Math.abs(diff) < mIndicatorHeight) {
                        changeState(State.PULL_TO_RUN);
                    } else if (mState == State.PULL_TO_RUN && Math.abs(diff) >= mIndicatorHeight) {
                        changeState(State.RELEASE_TO_RUN);
                    }
                }
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            if (mIsPulling) {
                mIsPulling = false;
                if (mState == State.RELEASE_TO_RUN) {
                    changeState(State.RUNNING);
                    return true;
                }
                if (isRunning()) {
                    scrollToY(-mIndicatorHeight);
                    return true;
                }
                changeState(State.RESET);
                return true;
            }
            break;
        }
        return true;
    }

    void changeState(State state) {
        mState = state;
    }

    void scrollToY(int y) {
        if (mIndicatorSet == IndicatorSet.Weld) {
            mIndicatorWrapper.scrollTo(0, y);
            mTargetView.scrollTo(0, y);
        } else if (mIndicatorSet == IndicatorSet.Above) {
            mIndicatorWrapper.scrollTo(0, y);
        }
    }

    boolean isReadyForPullDown() {
        boolean ready = false;
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTargetView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTargetView;
                ready = absListView.getChildCount() == 0
                        || (absListView.getFirstVisiblePosition() == 0 && absListView.getChildAt(0).getTop() >= absListView
                                .getPaddingTop());
            } else {
                ready = mTargetView.getScrollY() <= 0;
            }
        } else {
            ready = !ViewCompat.canScrollVertically(mTargetView, -1);
        }
        return ready;
    }

    public boolean isRunning() {
        return mState == State.RUNNING;
    }

    public void setIndicator(Indicator indicator) {
        View view = null;
        if (indicator == null || (view = indicator.getView(getContext())) == null) {
            throw new IllegalArgumentException("Indicator is null or indicator.getView() is null");
        }
        mIndicator = indicator;
        mIndicatorSet = mIndicator.getIndicatorSet();
        mIndicator.onAttach(this);
        if (mIndicatorSet == IndicatorSet.Any) {
            measureView(view);
            mIndicatorHeight = view.getMeasuredHeight();
        } else {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, -2);
            ViewGroup.LayoutParams oldParams = view.getLayoutParams();
            if (oldParams != null) {
                params.width = oldParams.width;
                params.height = oldParams.height;
                if (oldParams instanceof MarginLayoutParams) {
                    MarginLayoutParams marginParams = (MarginLayoutParams) oldParams;
                    params.leftMargin = marginParams.leftMargin;
                    params.topMargin = marginParams.topMargin;
                    params.rightMargin = marginParams.rightMargin;
                    params.bottomMargin = marginParams.bottomMargin;
                }
                if (oldParams instanceof FrameLayout.LayoutParams) {
                    FrameLayout.LayoutParams frameParams = (LayoutParams) oldParams;
                    params.gravity = frameParams.gravity;
                }
            }
            if (params.gravity == -1) {
                params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
            }
            mIndicatorWrapper.addView(view, params);
            measureView(mIndicatorWrapper);
            mIndicatorHeight = mIndicatorWrapper.getMeasuredHeight();
            ((FrameLayout.LayoutParams) mIndicatorWrapper.getLayoutParams()).topMargin = -mIndicatorHeight;
        }
    }

    public static void measureView(View view) {
        ViewGroup.LayoutParams p = view.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int nWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int nHeight = p.height;

        int nHeightSpec;
        if (nHeight > 0) {
            nHeightSpec = MeasureSpec.makeMeasureSpec(nHeight, MeasureSpec.EXACTLY);
        } else {
            nHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        view.measure(nWidthSpec, nHeightSpec);
    }
}
