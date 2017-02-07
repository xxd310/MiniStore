package com.zhihuitech.ccgljyb.entity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/28.
 */
public class LocationNumber implements Serializable{
    private String product_name;
    private String number;
    private String local_num;
    private String local_name;
    private String category_id;
    private String category_name;
    private String out_number = "";

    public LocationNumber() {
    }

    public LocationNumber(String product_name, String number, String local_num, String local_name, String category_id, String category_name, String out_number) {
        this.product_name = product_name;
        this.number = number;
        this.local_num = local_num;
        this.local_name = local_name;
        this.category_id = category_id;
        this.category_name = category_name;
        this.out_number = out_number;
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

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getOut_number() {
        return out_number;
    }

    public void setOut_number(String out_number) {
        this.out_number = out_number;
    }
}
