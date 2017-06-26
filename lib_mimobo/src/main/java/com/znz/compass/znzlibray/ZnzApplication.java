package com.znz.compass.znzlibray;


import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.znz.compass.znzlibray.common.DataManager;
import com.znz.compass.znzlibray.common.ZnzConstants;
import com.znz.compass.znzlibray.utils.StringUtil;

//import com.umeng.message.PushAgent;


/**
 * 应用全局变量
 *
 * @author zyn
 */
public class ZnzApplication extends MultiDexApplication {
    /**
     * Singleton pattern
     */
    private static ZnzApplication instance;
    private static Context context;
    protected DataManager dataManager;

    @Override
    public void onCreate() {
        super.onCreate();
        dataManager = DataManager.getInstance(this);
        context = this;

        if (StringUtil.isBlank(dataManager.readTempData(ZnzConstants.SERVICE_IP))) {
            dataManager.saveTempData(ZnzConstants.SERVICE_IP, "http://tyapi.znzkj.net/");
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static ZnzApplication getInstance() {
        synchronized (ZnzApplication.class) {
            if (instance == null) {
                instance = new ZnzApplication();
            }
        }
        return instance;
    }

    public static Context getContext() {
        return context;
    }
}
