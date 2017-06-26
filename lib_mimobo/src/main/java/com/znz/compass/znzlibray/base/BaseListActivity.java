package com.znz.compass.znzlibray.base;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.socks.library.KLog;
import com.znz.compass.znzlibray.R;
import com.znz.compass.znzlibray.base_znz.BaseZnzActivity;
import com.znz.compass.znzlibray.base_znz.IModel;
import com.znz.compass.znzlibray.network_status.NetUtils;
import com.znz.compass.znzlibray.utils.StringUtil;
import com.znz.compass.znzlibray.views.recyclerview.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by panqiming on 2016/10/31.
 */

public abstract class BaseListActivity<M extends IModel, T extends BaseZnzBean> extends BaseZnzActivity implements BaseQuickAdapter.RequestLoadMoreListener, SwipeRefreshLayout.OnRefreshListener {
    protected ArrayList<T> dataList = new ArrayList<>();
    protected int currentPageIndex = 1; // 当前页码
    protected RecyclerView rvRefresh;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected BaseQuickAdapter adapter;
    protected M mModel;
    protected JSONObject responseJson;
    protected Map<String, String> params;//请求参数
    private Subscriber<ResponseBody> subscriberList;
    protected int currentAction;

    public static final int ACTION_PULL_TO_REFRESH = 1;
    public static final int ACTION_LOAD_MORE_REFRESH = 2;
    private boolean isNormalList;

    @Override
    protected void initializeAdvance() {
        initializeVariate();
        initializeRefreshView();
        initializeNavigation();
        initializeView();

        if (adapter != null) {
            adapter.setOnLoadMoreListener(this, rvRefresh);
            if (isNormalList) {
                adapter.setEnableLoadMore(false);
            }
        }

        if (!NetUtils.isNetworkAvailable(context)) {
            onNetworkDisConnected();
        } else {
            loadDataFromServer();
        }
    }

    protected void initializeRefreshView() {
        //刷新控件初始化
        rvRefresh = bindViewById(activity, R.id.rvCommonRefresh);
        mSwipeRefreshLayout = bindViewById(activity, R.id.mSwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(mDataManager.getColor(R.color.colorPrimary));
        if (getLayoutManager() != null) {
            rvRefresh.setLayoutManager(getLayoutManager());
        } else {
            rvRefresh.setLayoutManager(new LinearLayoutManager(activity));
        }
        customeRefreshRequest(ACTION_PULL_TO_REFRESH);
    }

    /**
     * 加载layout，fragment中需要返回int数组，int[0]是控件id，int[1]控制是否使用znzToolbar
     *
     * @return
     */
    protected abstract int[] getLayoutResource();

    /**
     * 初始化数据
     */
    protected abstract void initializeVariate();

    /**
     * 导航栏初始化
     */
    protected abstract void initializeNavigation();

    /**
     * recyclerview加载方式
     *
     * @return
     */
    protected abstract RecyclerView.LayoutManager getLayoutManager();

    /**
     * 视图初始化
     */
    protected abstract void initializeView();

    /**
     * 调用接口
     */
    protected abstract void loadDataFromServer();

    /**
     * 接口请求成功
     *
     * @param response 返回值str
     */
    protected abstract void onRefreshSuccess(String response);

    /**
     * 接口请求失败
     *
     * @param error
     */
    protected abstract void onRefreshFail(String error);


    /**
     * 自定义请求
     *
     * @return
     */
    protected Observable<ResponseBody> requestCustomeRefreshObservable() {
        return null;
    }


    /**
     * 设置不能刷新和加载
     */
    protected void setNoRefreshAndLoad() {
        isNormalList = true;
        mSwipeRefreshLayout.setEnabled(false);
    }

    /**
     * 监听到网络连接的状态
     *
     * @param type
     */
    @Override
    protected void onNetworkConnected(NetUtils.NetType type) {
        loadDataFromServer();
        resetRefresh();
        if (znzRemind != null) {
            znzRemind.setVisibility(View.GONE);
            znzRemind.hideNoWifi();
        }
    }

    /**
     * 监听到网络未连接的状态
     */
    @Override
    protected void onNetworkDisConnected() {
//        if (znzRemind != null) {
//            znzRemind.setVisibility(View.VISIBLE);
//            znzRemind.showNoWifi();
//        }
        mDataManager.showToast(activity.getString(R.string.NoSignalException));
    }

    /**
     * 手动刷新
     */
    protected void resetRefresh() {
        currentPageIndex = 1;
        customeRefreshRequest(ACTION_PULL_TO_REFRESH);
    }

    @Override
    public void onLoadMoreRequested() {
        customeRefreshRequest(ACTION_LOAD_MORE_REFRESH);
    }

    @Override
    public void onRefresh() {
        if (!NetUtils.isNetworkAvailable(context)) {
            onNetworkDisConnected();
        } else {
            customeRefreshRequest(ACTION_PULL_TO_REFRESH);
        }
    }

    /**
     * 列表访问网络请求
     *
     * @param action
     */
    private void customeRefreshRequest(final int action) {
        if (mModel == null) {
            KLog.e("请实例化model");
            setTempDataList();
            return;
        }

        currentAction = action;

        //是用clear还是重新new都有道理，暂时无法取舍
        params = new HashMap<>();

        if (requestCustomeRefreshObservable() == null) {
            setTempDataList();
        } else {
            if (action == ACTION_PULL_TO_REFRESH) {
                currentPageIndex = 1;
            }

            params.put("limit", "10");
            params.put("page", currentPageIndex + "");

            subscriberList = new Subscriber<ResponseBody>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    onRefreshFail(e.toString());
                    e.printStackTrace();
                    mSwipeRefreshLayout.setRefreshing(false);
                    adapter.setEmptyView(R.layout.widget_pull_to_refresh_no_data);
                    adapter.loadMoreFail();
                }

                @Override
                public void onNext(ResponseBody responseBody) {
                    try {
                        //写在这数据是加载完才删除
                        if (action == ACTION_PULL_TO_REFRESH) {
                            dataList.clear();
                        }

                        JSONObject jsonObject = JSON.parseObject(responseBody.string());
                        int totalCount = 0;
                        if (jsonObject.getString("statusCode").equals("00000")) {
                            try {
                                responseJson = JSON.parseObject(jsonObject.getString("object"));
                                totalCount = StringUtil.stringToInt(JSON.parseObject(responseJson.getString("page")).getString("totalCount"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (!jsonObject.getString("object").equals("{}")) {
                                    onRefreshSuccess(jsonObject.getString("object"));
                                }
                                //页码自增
                                currentPageIndex++;

                                if (dataList.isEmpty()) {
                                    adapter.setEmptyView(R.layout.widget_pull_to_refresh_no_data);
                                    adapter.setEnableLoadMore(false);
                                } else {
                                    if (dataList.size() < totalCount) {
                                        adapter.setEnableLoadMore(true);
                                    } else {
                                        adapter.setEnableLoadMore(false);
                                        adapter.loadMoreEnd(true);
                                    }
                                }

                                if (action == ACTION_PULL_TO_REFRESH) {
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                                adapter.loadMoreComplete();
                            }
                        } else {
                            mDataManager.showToast(jsonObject.getString("msg"));
                            mSwipeRefreshLayout.setRefreshing(false);
                            adapter.setEmptyView(R.layout.widget_pull_to_refresh_no_data);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            requestCustomeRefreshObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriberList);
        }
    }


    /**
     * 临时数据
     */
    private void setTempDataList() {
        //临时数据，写UI时用
        if (dataList.isEmpty()) {
            List<BaseZnzBean> tempList = new ArrayList<>();
            tempList.add(new BaseZnzBean());
            tempList.add(new BaseZnzBean());
            tempList.add(new BaseZnzBean());
            tempList.add(new BaseZnzBean());
            tempList.add(new BaseZnzBean());
            tempList.add(new BaseZnzBean());
            tempList.add(new BaseZnzBean());
            tempList.add(new BaseZnzBean());

            dataList.clear();
            dataList.addAll((Collection<? extends T>) tempList);

            if (adapter != null) {
                adapter.loadMoreComplete();
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }

//        //没有数据的情况,真实环境中使用
//        rvRefresh.enableLoadMore(false);
//        rvRefresh.onRefreshCompleted();
//        adapter.setNoData(true);
//        adapter.notifyDataSetChanged();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscriberList != null) {
            if (!subscriberList.isUnsubscribed()) {
                subscriberList.unsubscribe();
            }
        }
    }
}
