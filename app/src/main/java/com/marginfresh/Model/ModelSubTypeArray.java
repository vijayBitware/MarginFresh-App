package com.marginfresh.Model;

import java.io.Serializable;

/**
 * Created by bitware on 21/6/17.
 */

public class ModelSubTypeArray implements Serializable{

    public String type_id;
    public String type_name;
    public String sub_type_image;

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

    public String getSub_type_image() {
        return sub_type_image;
    }

    public void setSub_type_image(String sub_type_image) {
        this.sub_type_image = sub_type_image;
    }
}
