package com.zhihuitech.ccgljyb.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/26.
 */
public class Location implements Serializable {
    private String id;
    private String num;
    private String name;
    private String is_delete;
    private String create_time;
    private String u_id;

    public Location() {
    }

    public Location(String id, String num, String name, String is_delete, String create_time, String u_id) {
        this.id = id;
        this.num = num;
        this.name = name;
        this.is_delete = is_delete;
        this.create_time = create_time;
        this.u_id = u_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIs_delete() {
        return is_delete;
    }

    public void setIs_delete(String is_delete) {
        this.is_delete = is_delete;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getU_id() {
        return u_id;
    }

    public void setU_id(String u_id) {
        this.u_id = u_id;
    }
}
