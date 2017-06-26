package com.znz.compass.znzlibray.bean;

import com.znz.compass.znzlibray.base.BaseZnzBean;

/**
 * Date： 2017/5/26 2017
 * User： PSuiyi
 * Description：
 */

public class TagBean extends BaseZnzBean {
    private String id;
    private String title;
    private String state;
    private boolean isSelect;

    public TagBean() {
    }

    public TagBean(String title) {
        this.title = title;
    }

    public TagBean(String title, boolean isSelect) {
        this.title = title;
        this.isSelect = isSelect;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
