package com.zhihuitech.ccgljyb.provider;

import com.zhihuitech.ccgljyb.util.HttpUtil;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/22.
 */
public class DataProvider {
//    private final static String ROOT_URL = "http://v.wx91go.com";
    private final static String ROOT_URL = "http://wms.jinlanzuan.com";
//    private final static String ROOT_URL = "http://172.16.2.18:8888";

    public static String login(String imei, String model, String version, String timestamp, String sign) {
        NameValuePair imeiPair = new BasicNameValuePair("imei", imei);
        NameValuePair modelPair = new BasicNameValuePair("model", model);
        NameValuePair versionPair = new BasicNameValuePair("version", version);
        NameValuePair timestampPair = new BasicNameValuePair("timestamp", timestamp);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(imeiPair);
        pairs.add(modelPair);
        pairs.add(versionPair);
        pairs.add(timestampPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/index.php?m=home&c=index&a=login&timestamp=" + timestamp + "&sign=" + sign, pairs);
        System.out.println("login.result=" + result);
        return result;
    }

    public static String authorizeRecord(String imei, String sign, String timestamp) {
        NameValuePair imeiPair = new BasicNameValuePair("imei", imei);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(imeiPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/index.php?m=home&c=index&a=authorizeRecord&timestamp=" + timestamp + "&sign=" + sign, pairs);
        System.out.println("authorizeRecord.result=" + result);
        return result;
    }

    public static String getVersion(String sign, String timestamp) {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/index.php?m=home&c=index&a=get_version&timestamp=" + timestamp + "&sign=" + sign, pairs);
        System.out.println("getVersion.result=" + result);
        return result;
    }


    public static String updateYun(String data, String sign, String timestamp) {
        NameValuePair dataPair = new BasicNameValuePair("data", data);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(dataPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/index.php?m=home&c=index&a=updateYun&timestamp=" + timestamp + "&sign=" + sign, pairs);
        System.out.println("updateYun.result=" + result);
        return result;
    }

    public static String loginCheck(String data) {
        NameValuePair dataPair = new BasicNameValuePair("data", data);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(dataPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/?m=admin&c=login&a=loginCheck", pairs);
        System.out.println("loginCheck.result=" + result);
        return result;
    }

    public static String getLocationList(String uid) {
        String result = HttpUtil.sendGetRequest(ROOT_URL + "/?m=admin&c=location&a=localUpdate&u_id=" + uid);
        System.out.println("getLocationList.result=" + result);
        return result;
    }

    public static String locationAdd(String num, String name, String uid) {
        NameValuePair numPair = new BasicNameValuePair("num", num);
        NameValuePair namePair = new BasicNameValuePair("name", name);
        NameValuePair uidPair = new BasicNameValuePair("u_id", uid);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(numPair);
        pairs.add(namePair);
        pairs.add(uidPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/?m=admin&c=location&a=localUpdate", pairs);
        System.out.println("locationAdd.result=" + result);
        return result;
    }

    public static String locationEdit(String num, String name, String uid, String id) {
        NameValuePair numPair = new BasicNameValuePair("num", num);
        NameValuePair namePair = new BasicNameValuePair("name", name);
        NameValuePair uidPair = new BasicNameValuePair("u_id", uid);
        NameValuePair idPair = new BasicNameValuePair("id", id);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(numPair);
        pairs.add(namePair);
        pairs.add(uidPair);
        pairs.add(idPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/?m=admin&c=location&a=localUpdate", pairs);
        System.out.println("locationEdit.result=" + result);
        return result;
    }

    public static String locationDel(String id, String uid) {
        NameValuePair idPair = new BasicNameValuePair("id", id);
        NameValuePair uidPair = new BasicNameValuePair("u_id", uid);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(idPair);
        pairs.add(uidPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/?m=admin&c=location&a=del", pairs);
        System.out.println("locationDel.result=" + result);
        return result;
    }

    public static String getTypeList(String uid) {
        String result = HttpUtil.sendGetRequest(ROOT_URL + "/?m=admin&c=category&a=cateUpdate&u_id=" + uid);
        System.out.println("getTypeList.result=" + result);
        return result;
    }

    public static String typeAdd(String name, String uid) {
        NameValuePair namePair = new BasicNameValuePair("name", name);
        NameValuePair uidPair = new BasicNameValuePair("u_id", uid);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(namePair);
        pairs.add(uidPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/?m=admin&c=category&a=cateUpdate", pairs);
        System.out.println("typeAdd.result=" + result);
        return result;
    }

    public static String typeEdit(String name, String uid, String id) {
        NameValuePair namePair = new BasicNameValuePair("name", name);
        NameValuePair uidPair = new BasicNameValuePair("u_id", uid);
        NameValuePair idPair = new BasicNameValuePair("id", id);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(namePair);
        pairs.add(uidPair);
        pairs.add(idPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/?m=admin&c=category&a=cateUpdate", pairs);
        System.out.println("typeEdit.result=" + result);
        return result;
    }

    public static String typeDel(String id, String uid) {
        NameValuePair idPair = new BasicNameValuePair("id", id);
        NameValuePair uidPair = new BasicNameValuePair("u_id", uid);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(idPair);
        pairs.add(uidPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/?m=admin&c=category&a=del", pairs);
        System.out.println("typeDel.result=" + result);
        return result;
    }

    public static String shopNameEdit(String id, String name) {
        NameValuePair idPair = new BasicNameValuePair("id", id);
        NameValuePair namePair = new BasicNameValuePair("name", name);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(idPair);
        pairs.add(namePair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/?m=Admin&c=Login&a=userUpdate", pairs);
        System.out.println("shopNameEdit.result=" + result);
        return result;
    }

    public static String inProduct(String num, String uid) {
        NameValuePair numPair = new BasicNameValuePair("num", num);
        NameValuePair uidPair = new BasicNameValuePair("u_id", uid);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(numPair);
        pairs.add(uidPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/?m=Admin&c=Storage&a=inProduct", pairs);
        System.out.println("inProduct.result=" + result);
        return result;
    }

    public static String inStorage(String local_num, String local_name, String uid, String product) {
        NameValuePair localNumPair = new BasicNameValuePair("local_num", local_num);
        NameValuePair localNamePair = new BasicNameValuePair("local_name", local_name);
        NameValuePair uidPair = new BasicNameValuePair("u_id", uid);
        NameValuePair productPair = new BasicNameValuePair("product", product);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(localNumPair);
        pairs.add(localNamePair);
        pairs.add(uidPair);
        pairs.add(productPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/?m=Admin&c=Storage&a=inStorage", pairs);
        System.out.println("inStorage.product=" + product);
        System.out.println("inStorage.result=" + result);
        return result;
    }

    public static String outProduct(String num, String uid) {
        NameValuePair numPair = new BasicNameValuePair("num", num);
        NameValuePair uidPair = new BasicNameValuePair("u_id", uid);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(numPair);
        pairs.add(uidPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/?m=Admin&c=Storage&a=outProduct", pairs);
        System.out.println("outProduct.result=" + result);
        return result;
    }

    public static String outStorage(String uid, String product) {
        NameValuePair uidPair = new BasicNameValuePair("u_id", uid);
        NameValuePair productPair = new BasicNameValuePair("product", product);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(uidPair);
        pairs.add(productPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/?m=Admin&c=Storage&a=outStorage", pairs);
        System.out.println("outStorage.product=" + product);
        System.out.println("outStorage.result=" + result);
        return result;
    }

    public static String record(String uid) {
        NameValuePair uidPair = new BasicNameValuePair("u_id", uid);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(uidPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/?m=Admin&c=Storage&a=record", pairs);
        System.out.println("record.result=" + result);
        return result;
    }

    public static String checkStorage(String uid) {
        NameValuePair uidPair = new BasicNameValuePair("u_id", uid);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(uidPair);
        String result = HttpUtil.sendPostRequest(ROOT_URL + "/?m=Admin&c=Storage&a=checkStorage", pairs);
        System.out.println("checkStorage.result=" + result);
        return result;
    }

}
