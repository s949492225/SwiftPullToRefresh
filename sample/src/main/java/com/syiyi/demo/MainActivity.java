package com.syiyi.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.syiyi.refresh.SwiftPullToRefresh;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SwiftPullToRefresh refreshLayout = (SwiftPullToRefresh) findViewById(R.id.refresh);
        refreshLayout.setRefreshHandler(new TextRefreshHandler());
    }
}
