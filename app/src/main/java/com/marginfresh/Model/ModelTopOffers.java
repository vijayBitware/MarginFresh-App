package com.marginfresh.Model;

/**
 * Created by bitware on 2/6/17.
 */

public class ModelTopOffers {

    String pname;
    String offer;
    String categoryName;
    public String product_id;
    public String product_name;
    public String store_image_url;

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getStore_image_url() {
        return store_image_url;
    }

    public void setStore_image_url(String store_image_url) {
        this.store_image_url = store_image_url;
    }
}
