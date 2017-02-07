package com.zhihuitech.ccgljyb.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/25.
 */
public class User implements Serializable {
    private String id;
    private String store_name;
    private String user_name;
    private String num;
    private String open_time;
    private String end_time;
    private String log_num;
    private String tel;
    private String token;

    public User() {
    }

    public User(String id, String store_name, String user_name, String num, String open_time, String end_time, String log_num, String tel, String token) {
        this.id = id;
        this.store_name = store_name;
        this.user_name = user_name;
        this.num = num;
        this.open_time = open_time;
        this.end_time = end_time;
        this.log_num = log_num;
        this.tel = tel;
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getOpen_time() {
        return open_time;
    }

    public void setOpen_time(String open_time) {
        this.open_time = open_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getLog_num() {
        return log_num;
    }

    public void setLog_num(String log_num) {
        this.log_num = log_num;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
