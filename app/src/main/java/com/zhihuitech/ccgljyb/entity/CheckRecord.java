package com.zhihuitech.ccgljyb.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/7.
 */
public class CheckRecord implements Serializable {
    private String id;
    private String create_time;
    private String local_name;
    private String status;

    public CheckRecord() {
    }

    public CheckRecord(String id, String create_time, String local_name, String status) {
        this.id = id;
        this.create_time = create_time;
        this.local_name = local_name;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getLocal_name() {
        return local_name;
    }

    public void setLocal_name(String local_name) {
        this.local_name = local_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
