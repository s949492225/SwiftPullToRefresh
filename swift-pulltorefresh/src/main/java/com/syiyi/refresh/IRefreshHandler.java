package com.syiyi.refresh;

import android.view.View;
import android.view.ViewGroup;

/**
 * 下拉刷新的处理
 * Created by Dell on 2017/5/18.
 */

public interface IRefreshHandler {
    View getRefreshView(ViewGroup parent);

    int getBeginRefreshDistance();

    void onPullProcess(float percent);

    void onRefresh();


    void onReset();
}
