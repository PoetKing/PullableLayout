package com.poet.test;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.poet.pullablelayout.PullableLayout;
import com.poet.pullablelayout.indicators.CircularArrow;

public class MainActivity extends Activity {

    PullableLayout mPullableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPullableLayout = new PullableLayout(this);
        mPullableLayout.setIndicator(new CircularArrow(this));
        setContentView(mPullableLayout);

        TextView tv = new TextView(this);
        tv.setBackgroundColor(Color.RED);
        tv.setText("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        mPullableLayout.addView(tv);
    }
}
