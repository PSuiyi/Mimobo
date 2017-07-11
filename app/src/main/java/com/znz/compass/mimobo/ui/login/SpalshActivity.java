package com.znz.compass.mimobo.ui.login;

import android.content.Intent;
import android.view.WindowManager;

import com.znz.compass.mimobo.R;
import com.znz.compass.znzlibray.base.BaseActivity;

import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * Date： 2017/6/26 2017
 * User： PSuiyi
 * Description：
 */

public class SpalshActivity extends BaseActivity {
    @Override
    protected int[] getLayoutResource() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return new int[]{R.layout.activity_splash};
    }

    @Override
    protected void initializeVariate() {

    }

    @Override
    protected void initializeNavigation() {

    }

    @Override
    protected void initializeView() {
        //解决home键重启问题
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        Observable.timer(1, TimeUnit.SECONDS)
                .subscribe(aLong -> {
                    gotoActivity(LoginActivity.class);
                    finish();
                });
    }

    @Override
    protected void loadDataFromServer() {

    }
}
