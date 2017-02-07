package com.zhihuitech.ccgljyb.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/4.
 */
public class InStorageListItem implements Serializable {
    private String product_num;
    private String product_name;
    private String number;
    private String category_id;
    private String local_num;
    private String local_name;

    public InStorageListItem() {
    }

    public InStorageListItem(String product_num, String product_name, String number, String category_id, String local_num, String local_name) {
        this.product_num = product_num;
        this.product_name = product_name;
        this.number = number;
        this.category_id = category_id;
        this.local_num = local_num;
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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getLocal_num() {
        return local_num;
    }

    public void setLocal_num(String local_num) {
        this.local_num = local_num;
    }

    public String getLocal_name() {
        return local_name;
    }

    public void setLocal_name(String local_name) {
        this.local_name = local_name;
    }
}
