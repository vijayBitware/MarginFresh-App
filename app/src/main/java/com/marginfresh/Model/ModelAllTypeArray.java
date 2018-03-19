package com.marginfresh.Model;

import java.util.ArrayList;

/**
 * Created by bitware on 21/6/17.
 */

public class ModelAllTypeArray {

    public String type_id;
    public String type_name;
    public String type_image_url;
    public ArrayList<ModelSubTypeArray> arrSubTypeArrayList;
    public ArrayList<ModelAllTypeArray> arrAllTypeArray;

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }

    public String getType_name() {
        return type_name;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

    public String getType_image_url() {
        return type_image_url;
    }

    public void setType_image_url(String type_image_url) {
        this.type_image_url = type_image_url;
    }

    public ArrayList<ModelSubTypeArray> getArrSubTypeArrayList() {
        return arrSubTypeArrayList;
    }

    public void setArrSubTypeArrayList(ArrayList<ModelSubTypeArray> arrSubTypeArrayList) {
        this.arrSubTypeArrayList = arrSubTypeArrayList;
    }

    public ArrayList<ModelAllTypeArray> getArrAllTypeArray() {
        return arrAllTypeArray;
    }

    public void setArrAllTypeArray(ArrayList<ModelAllTypeArray> arrAllTypeArray) {
        this.arrAllTypeArray = arrAllTypeArray;
    }
}
