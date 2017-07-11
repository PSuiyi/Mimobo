package com.znz.compass.mimobo;

import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.znz.compass.mimobo.common.Constants;
import com.znz.compass.mimobo.ui.login.SpalshActivity;
import com.znz.compass.znzlibray.ZnzApplication;

/**
 * Date： 2017/7/11 2017
 * User： PSuiyi
 * Description：
 */

public class AppApplication extends ZnzApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        WbSdk.install(this, new AuthInfo(this, Constants.Weibo.APP_KEY, Constants.Weibo.REDIRECT_URL, Constants.Weibo.SCOPE));

        //创建快捷方式
        dataManager.createShortcut(SpalshActivity.class, getResources().getString(R.string.app_name), R.mipmap.ic_launcher_round);
    }
}
