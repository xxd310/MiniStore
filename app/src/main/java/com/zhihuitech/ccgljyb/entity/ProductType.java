package com.zhihuitech.ccgljyb.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/26.
 */
public class ProductType implements Serializable {
    private String id;
    private String name;
    private String is_delete;
    private String create_time;
    private String create_user;
    private String remark;
    private String u_id;

    public ProductType() {
    }

    public ProductType(String id, String name, String is_delete, String create_time, String create_user, String remark, String u_id) {
        this.id = id;
        this.name = name;
        this.is_delete = is_delete;
        this.create_time = create_time;
        this.create_user = create_user;
        this.remark = remark;
        this.u_id = u_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCreate_user() {
        return create_user;
    }

    public void setCreate_user(String create_user) {
        this.create_user = create_user;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getU_id() {
        return u_id;
    }

    public void setU_id(String u_id) {
        this.u_id = u_id;
    }
}
