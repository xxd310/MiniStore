package com.zhihuitech.ccgljyb.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/9.
 */
public class AuthorizationRecord implements Serializable {
    private String authorize_time;
    private String end_time;
    private String validity_time;

    public AuthorizationRecord() {
    }

    public AuthorizationRecord(String authorize_time, String end_time, String validity_time) {
        this.authorize_time = authorize_time;
        this.end_time = end_time;
        this.validity_time = validity_time;
    }

    public String getAuthorize_time() {
        return authorize_time;
    }

    public void setAuthorize_time(String authorize_time) {
        this.authorize_time = authorize_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getValidity_time() {
        return validity_time;
    }

    public void setValidity_time(String validity_time) {
        this.validity_time = validity_time;
    }
}
