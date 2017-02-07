package com.zhihuitech.ccgljyb.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/27.
 */
public class StoreInfo implements Serializable{
    private String id;
    private String local_name;
    private String product_num;
    private String product_name;
    private String cate_name;
    private String number;
    private String change_time;

    public StoreInfo() {
    }

    public StoreInfo(String id, String local_name, String product_num, String product_name, String cate_name, String number, String change_time) {
        this.id = id;
        this.local_name = local_name;
        this.product_num = product_num;
        this.product_name = product_name;
        this.cate_name = cate_name;
        this.number = number;
        this.change_time = change_time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocal_name() {
        return local_name;
    }

    public void setLocal_name(String local_name) {
        this.local_name = local_name;
    }

    public String getProduct_num() {
        return product_num;
    }

    public void setProduct_num(String product_num) {
        this.product_num = product_num;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getCate_name() {
        return cate_name;
    }

    public void setCate_name(String cate_name) {
        this.cate_name = cate_name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getChange_time() {
        return change_time;
    }

    public void setChange_time(String change_time) {
        this.change_time = change_time;
    }
}
