package com.zhihuitech.ccgljyb.entity;

/**
 * Created by Administrator on 2016/11/15.
 */
public class UpdateAuthEvent {
    private String mMsg;

    public UpdateAuthEvent(String msg) {
        mMsg = msg;
    }

    public String getMsg() {
        return mMsg;
    }

}
