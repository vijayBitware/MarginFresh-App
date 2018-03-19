package com.marginfresh.Model;

/**
 * Created by bitware on 30/6/17.
 */

public class ModelMyWishlist {

    public String product_id;
    public String product_name;
    public String product_image;
    public String price;
    public String new_price;
    public String product_rating_count;
    public String product_isInWislist;
    public String productSku;
    public String isInStock;
    public String priceForStorage;

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

    public String getProduct_image() {
        return product_image;
    }

    public void setProduct_image(String product_image) {
        this.product_image = product_image;
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

    public String getIsInStock() {
        return isInStock;
    }

    public void setIsInStock(String isInStock) {
        this.isInStock = isInStock;
    }

    public String getPriceForStorage() {
        return priceForStorage;
    }

    public void setPriceForStorage(String priceForStorage) {
        this.priceForStorage = priceForStorage;
    }
}
