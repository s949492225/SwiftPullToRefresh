package com.syiyi.demo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.syiyi.refresh.DensityUtil;
import com.syiyi.refresh.IRefreshHandler;


/**
 * 下啦刷新界面
 * Created by Dell on 2017/5/18.
 */

public class TextRefreshHandler implements IRefreshHandler {
    private Context context;
    private static final String TAG = "TextRefreshHandler";

    @Override
    public View getRefreshView(ViewGroup parent) {
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
