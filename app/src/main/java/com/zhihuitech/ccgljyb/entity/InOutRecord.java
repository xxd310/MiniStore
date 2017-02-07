package com.zhihuitech.ccgljyb.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/28.
 */
public class InOutRecord implements Serializable{
    private String id;
    private String product_name;
    private String product_num;
    private String category;
    private String create_user;
    private String number;
    private String create_time;
    private String local_name;

    public InOutRecord() {
    }

    public InOutRecord(String id, String product_name, String product_num, String category, String create_user, String number, String create_time, String local_name) {
        this.id = id;
        this.product_name = product_name;
        this.product_num = product_num;
        this.category = category;
        this.create_user = create_user;
        this.number = number;
        this.create_time = create_time;
        this.local_name = local_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_num() {
        return product_num;
    }

    public void setProduct_num(String product_num) {
        this.product_num = product_num;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCreate_user() {
        return create_user;
    }

    public void setCreate_user(String create_user) {
        this.create_user = create_user;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
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
}
