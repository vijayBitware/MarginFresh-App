package com.marginfresh.Model;

/**
 * Created by bitware on 22/6/17.
 */

public class ModelProductList {

    public String productId;
    public String product_name;
    public String product_image_url;
    public String price;
    public String new_price;
    public String product_rating_count;
    public String product_isInWislist,priceForSort,productSku;
    public String isSelected;
    public String productInStock;

    public String getPriceForSort() {
        return priceForSort;
    }

    public void setPriceForSort(String priceForSort) {
        this.priceForSort = priceForSort;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_image_url() {
        return product_image_url;
    }

    public void setProduct_image_url(String product_image_url) {
        this.product_image_url = product_image_url;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getNew_price() {
        return new_price;
    }

    public void setNew_price(String new_price) {
        this.new_price = new_price;
    }

    public String getProduct_rating_count() {
        return product_rating_count;
    }

    public void setProduct_rating_count(String product_rating_count) {
        this.product_rating_count = product_rating_count;
    }

    public String getProduct_isInWislist() {
        return product_isInWislist;
    }

    public void setProduct_isInWislist(String product_isInWislist) {
        this.product_isInWislist = product_isInWislist;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(String isSelected) {
        this.isSelected = isSelected;
    }

    public String getProductInStock() {
        return productInStock;
    }

    public void setProductInStock(String productInStock) {
        this.productInStock = productInStock;
    }
}
