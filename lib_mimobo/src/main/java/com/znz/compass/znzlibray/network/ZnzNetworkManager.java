package com.znz.compass.znzlibray.network;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znz.compass.znzlibray.R;
import com.znz.compass.znzlibray.common.DataManager;
import com.znz.compass.znzlibray.network.znzhttp.ZnzHttpListener;
import com.znz.compass.znzlibray.network_status.NetUtils;
import com.znz.compass.znzlibray.utils.ZnzLog;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by panqiming on 2016/11/8.
 */

public class ZnzNetworkManager {
    private Context context;
    private static ZnzNetworkManager instance;
    private DataManager dataManager;

    public static synchronized ZnzNetworkManager getInstance(Context mContext) {
        if (instance == null) {
            instance = new ZnzNetworkManager(mContext.getApplicationContext());
        }
        return instance;
    }

    public ZnzNetworkManager(Context context) {
        this.context = context;
        dataManager = DataManager.getInstance(context);
    }

    public void request(Observable<ResponseBody> observable, ZnzHttpListener listener) {
        //判断网络情况
        if (!NetUtils.isNetworkAvailable(context)) {
            dataManager.showToast(R.string.NoSignalException);
            return;
        }

        if (observable == null) {
            return;
        }

        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onFail(e.getMessage());
                        dataManager.showToast(e.getMessage());
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JSONObject responseJson;
                            try {
                                responseJson = JSON.parseObject(responseBody.string());
                                ZnzLog.json(responseBody.string());
                            } catch (Exception e) {
                                listener.onFail("返回的数据无法直接转换成FastJson");
                                return;
                            }

                            if (responseJson == null) {
                                listener.onFail("返回的数据无法直接转换成FastJson");
                                return;
                            }

                            if (responseJson.getString("code").equals("1")) {
                                listener.onSuccess(responseJson);
                            } else {
                                listener.onFail(responseJson.getString("message"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            dataManager.showToast(e.getMessage());
                            listener.onFail(e.getMessage());
                        }
                    }
                });
    }

}
