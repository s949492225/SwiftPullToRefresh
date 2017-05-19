package com.syiyi.demo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.syiyi.refresh.BaseRefreshHandler;
import com.syiyi.refresh.DensityUtil;
import com.syiyi.refresh.SwiftPullToRefresh;


/**
 * Created by Dell on 2017/5/18.
 */

public class TextRefreshHandler extends BaseRefreshHandler {
    private Context context;
    private static final String TAG = "TextRefreshHandler";

    @Override
    public View getRefreshView(SwiftPullToRefresh parent) {
        context = parent.getContext();
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.text, parent, false);
    }

    @Override
    public int getBeginRefreshDistance() {
        return DensityUtil.dip2px(context, 200);
    }


    @Override
    public void onPullProcess(float percent) {
        Log.d(TAG, "onPullProcess: " + percent);
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh: ");
    }

    @Override
    public void onReset() {
        Log.d(TAG, "onReset: ");
    }


}
