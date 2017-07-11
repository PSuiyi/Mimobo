package com.znz.compass.mimobo.ui.login;

import android.os.Bundle;
import android.widget.Button;

import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.znz.compass.mimobo.R;
import com.znz.compass.znzlibray.base.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Date： 2017/7/11 2017
 * User： PSuiyi
 * Description：
 */

public class LoginActivity extends BaseActivity {
    @Bind(R.id.btnLogin)
    Button btnLogin;

    private SsoHandler mSsoHandler;

    @Override
    protected int[] getLayoutResource() {
        return new int[]{R.layout.act_login};
    }

    @Override
    protected void initializeVariate() {
        mSsoHandler = new SsoHandler(activity);
    }

    @Override
    protected void initializeNavigation() {

    }

    @Override
    protected void initializeView() {

    }

    @Override
    protected void loadDataFromServer() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnLogin)
    public void onViewClicked() {
        mSsoHandler.authorize(new SelfWbAuthListener());
    }

    private class SelfWbAuthListener implements com.sina.weibo.sdk.auth.WbAuthListener {
        @Override
        public void onSuccess(final Oauth2AccessToken token) {
            if (token.isSessionValid()) {
                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(activity, token);
                mDataManager.showToast("授权成功");
            }
        }

        @Override
        public void cancel() {
            mDataManager.showToast("授权取消");
        }

        @Override
        public void onFailure(WbConnectErrorMessage errorMessage) {
            mDataManager.showToast("授权失败");
        }
    }
}
