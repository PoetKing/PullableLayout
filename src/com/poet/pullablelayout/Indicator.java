/**
 * 
 */
package com.poet.pullablelayout;

import android.content.Context;
import android.view.View;

/**
 * @Description 
 * @author POET_WYD@FOXMAIL.COM
 * @date 2015-11-6
 */
public interface Indicator {

    /**
     * 指示器和PullableLayout关联上
     * @param layout
     */
    void onAttach(PullableLayout layout);

    /**
     * 指示器放置位置
     * @return
     */
    IndicatorSet getIndicatorSet();

    /**
     * 状态变化
     * @param state
     */
    void onStateChanged(State state);

    /**
     * 滑动距离/指示器高度
     * @param percent
     */
    void onPull(float percent);

    /**
     * onPull的最大值，有效值 >= 1
     * @return
     */
    float maxPullPercent();

    /**
     * 指示器View
     * @return
     */
    View getView(Context context);

    public enum IndicatorSet {
        /**
         * 指示器的位置和PullableLayout无关 
         */
        Any,
        /**
         * 指示器焊接在PullableLayout顶部
         */
        Weld,
        /**
         * 指示器悬浮在PullableLayout上面
         */
        Above;
    }
}
