package com.syiyi.refresh;

import android.view.View;

/**
 * Created by Dell on 2017/5/18.
 */

public abstract class BaseRefreshHandelr implements IRefreshHandler {


    @Override
    public View getRefreshView(SwiftPullToRefresh parent) {
        return null;
    }

    @Override
    public int getBeginRefreshDistance() {
        return 0;
    }

    @Override
    public void onBeginPull() {

    }

    @Override
    public void onPullProcess(float percent) {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public boolean enableFloatModel() {
        return true;
    }
}
