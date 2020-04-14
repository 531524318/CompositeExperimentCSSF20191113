package com.huazhi.changsha.compositeexperiment;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * 基本Activity
 **/
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO onCreate
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
